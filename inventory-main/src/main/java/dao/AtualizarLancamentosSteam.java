package dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sincroniza diariamente os lançamentos destacados pela Steam com o catálogo
 * e mantém uma lista própria para o banner inicial.
 */
public final class AtualizarLancamentosSteam {
    private static final int LIMITE = 8;
    private static final ScheduledExecutorService EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "inventory-steam-sync");
                t.setDaemon(true);
                return t;
            });

    private AtualizarLancamentosSteam() {}

    public static void iniciar() {
        EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                sincronizar();
            } catch (Exception e) {
                System.err.println("Falha ao atualizar lançamentos da Steam: " + e.getMessage());
            }
        }, 0, 12, TimeUnit.HOURS);
    }

    public static void parar() {
        EXECUTOR.shutdownNow();
    }

    public static void sincronizar() throws Exception {
        try (Connection conexao = Conexao.conectar()) {
            if (conexao == null) throw new IllegalStateException("SQLite indisponível.");

            try (Statement st = conexao.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS lancamento_steam (" +
                        "steam_app_id INTEGER PRIMARY KEY," +
                        "titulo TEXT NOT NULL," +
                        "plataforma TEXT," +
                        "data_lancamento TEXT," +
                        "capa TEXT," +
                        "atualizado_em TEXT DEFAULT CURRENT_TIMESTAMP)");
            }

            JsonObject destaques = requisitarJson(
                    "https://store.steampowered.com/api/featuredcategories/?cc=br&l=portuguese");
            JsonObject novos = destaques.has("new_releases")
                    && destaques.get("new_releases").isJsonObject()
                    ? destaques.getAsJsonObject("new_releases") : null;
            if (novos == null || !novos.has("items") || !novos.get("items").isJsonArray()) {
                throw new IllegalStateException("A Steam não retornou a lista de novos lançamentos.");
            }

            JsonArray itens = novos.getAsJsonArray("items");
            int processados = 0;
            for (JsonElement elemento : itens) {
                if (processados >= LIMITE) break;
                if (!elemento.isJsonObject()) continue;
                JsonObject item = elemento.getAsJsonObject();
                if (!item.has("id") || !item.get("id").isJsonPrimitive()) continue;
                int appId = item.get("id").getAsInt();

                try {
                    JsonObject detalheResposta = requisitarJson(
                            "https://store.steampowered.com/api/appdetails?appids=" +
                            appId + "&cc=br&l=portuguese");
                    JsonObject detalheApp = detalheResposta.getAsJsonObject(String.valueOf(appId));
                    if (detalheApp == null || !detalheApp.has("success")
                            || !detalheApp.get("success").getAsBoolean()) continue;
                    JsonObject dados = detalheApp.getAsJsonObject("data");

                    String titulo = texto(dados, "name");
                    if (titulo.isEmpty()) continue;
                    JsonObject plataformasJson = dados.has("platforms")
                            && dados.get("platforms").isJsonObject()
                            ? dados.getAsJsonObject("platforms") : new JsonObject();
                    StringBuilder plataformas = new StringBuilder();
                    adicionarPlataforma(plataformas, plataformasJson, "windows", "PC");
                    adicionarPlataforma(plataformas, plataformasJson, "mac", "Mac");
                    adicionarPlataforma(plataformas, plataformasJson, "linux", "Linux");
                    if (plataformas.length() == 0) plataformas.append("PC");

                    String dataLancamento = "";
                    if (dados.has("release_date") && dados.get("release_date").isJsonObject()) {
                        dataLancamento = texto(dados.getAsJsonObject("release_date"), "date");
                    }
                    String capa = texto(dados, "header_image");
                    if (capa.isEmpty()) capa = "https://cdn.akamai.steamstatic.com/steam/apps/" +
                            appId + "/library_600x900_2x.jpg";
                    String genero = "Jogos";
                    if (dados.has("genres") && dados.get("genres").isJsonArray()
                            && dados.getAsJsonArray("genres").size() > 0) {
                        genero = texto(dados.getAsJsonArray("genres").get(0).getAsJsonObject(), "description");
                    }
                    int ano = extrairAno(dataLancamento);

                    salvarJogo(conexao, appId, titulo, genero, plataformas.toString(), ano, capa,
                            dataLancamento);
                    processados++;
                } catch (Exception jogoErro) {
                    System.err.println("Não foi possível importar o jogo Steam " + appId +
                            ": " + jogoErro.getMessage());
                }
            }
            System.out.println("Inventory: " + processados + " lançamentos Steam sincronizados.");
        }
    }

    private static void salvarJogo(Connection c, int id, String nome, String genero,
            String plataformas, int ano, String capa, String data) throws Exception {
        String sql = "INSERT INTO jogo (steam_app_id,titulo,descricao,genero,plataforma,ano_lancamento,capa) " +
                "VALUES (?,?,? ,?,?,?,?) ON CONFLICT(steam_app_id) DO UPDATE SET " +
                "titulo=excluded.titulo, plataforma=excluded.plataforma, " +
                "ano_lancamento=excluded.ano_lancamento, capa=excluded.capa";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nome);
            ps.setString(3, "Jogo importado automaticamente dos lançamentos da Steam.");
            ps.setString(4, genero);
            ps.setString(5, plataformas);
            if (ano > 0) ps.setInt(6, ano); else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, capa);
            ps.executeUpdate();
        }

        String destaque = "INSERT INTO lancamento_steam " +
                "(steam_app_id,titulo,plataforma,data_lancamento,capa,atualizado_em) " +
                "VALUES (?,?,?,?,?,CURRENT_TIMESTAMP) ON CONFLICT(steam_app_id) DO UPDATE SET " +
                "titulo=excluded.titulo, plataforma=excluded.plataforma, " +
                "data_lancamento=excluded.data_lancamento, capa=excluded.capa, " +
                "atualizado_em=CURRENT_TIMESTAMP";
        try (PreparedStatement ps = c.prepareStatement(destaque)) {
            ps.setInt(1, id);
            ps.setString(2, nome);
            ps.setString(3, plataformas);
            ps.setString(4, data);
            ps.setString(5, capa);
            ps.executeUpdate();
        }
    }

    private static void adicionarPlataforma(StringBuilder sb, JsonObject json,
            String chave, String nome) {
        if (json.has(chave) && json.get(chave).getAsBoolean()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(nome);
        }
    }

    private static int extrairAno(String data) {
        Matcher m = Pattern.compile("(19|20)\\d{2}").matcher(data == null ? "" : data);
        return m.find() ? Integer.parseInt(m.group()) : 0;
    }

    private static String texto(JsonObject obj, String chave) {
        return obj != null && obj.has(chave) && !obj.get(chave).isJsonNull()
                ? obj.get(chave).getAsString() : "";
    }

    private static JsonObject requisitarJson(String endereco) throws Exception {
        HttpURLConnection conexao = (HttpURLConnection) new URL(endereco).openConnection();
        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(10000);
        conexao.setReadTimeout(15000);
        conexao.setRequestProperty("User-Agent", "InventoryGameCatalog/1.0");
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(
                conexao.getInputStream(), StandardCharsets.UTF_8))) {
            return JsonParser.parseReader(leitor).getAsJsonObject();
        } finally {
            conexao.disconnect();
        }
    }
}
