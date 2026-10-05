package dao;

import util.PasswordUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

/**
 * Cria e atualiza a estrutura do banco do Inventory.
 * A inicialização é executada uma vez pelo listener da aplicação.
 * O catálogo de jogos é carregado apenas sob demanda, via carregarCatalogoOpcional().
 */
public final class CriarBanco {

    private CriarBanco() {
    }

    public static void criarTabela() {
        try (Connection conexao = Conexao.conectar()) {
            if (conexao == null) {
                throw new IllegalStateException("Não foi possível conectar ao SQLite.");
            }

            try (Statement stmt = conexao.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                criarTabelas(stmt);
                migrarBancoAntigo(stmt);
                criarIndices(stmt);
                migrarSenhas(conexao);
            }

            System.out.println("Banco do Inventory inicializado com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar o banco do Inventory:");
            e.printStackTrace();
        }
    }

    private static void criarTabelas(Statement stmt) throws Exception {
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS usuario (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "username TEXT NOT NULL UNIQUE," +
                "email TEXT NOT NULL UNIQUE," +
                "senha TEXT NOT NULL," +
                "foto TEXT," +
                "bio TEXT," +
                "data_nascimento TEXT," +
                "pais TEXT," +
                "plataforma_favorita TEXT" +
                ")"
        );
stmt.execute(
                "CREATE TABLE IF NOT EXISTS configuracao (" +
                "chave TEXT PRIMARY KEY," +
                "valor TEXT NOT NULL" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS jogo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "steam_app_id INTEGER UNIQUE NOT NULL," +
                "titulo TEXT NOT NULL," +
                "descricao TEXT," +
                "genero TEXT," +
                "plataforma TEXT DEFAULT 'PC'," +
                "ano_lancamento INTEGER," +
                "capa TEXT" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS seguidor (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_seguidor INTEGER NOT NULL," +
                "id_seguido INTEGER NOT NULL," +
                "data_seguida TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(id_seguidor, id_seguido)," +
                "FOREIGN KEY(id_seguidor) REFERENCES usuario(id)," +
                "FOREIGN KEY(id_seguido) REFERENCES usuario(id)" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS biblioteca (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL," +
                "steam_app_id INTEGER NOT NULL," +
                "status TEXT NOT NULL DEFAULT 'quero_jogar'," +
                "data_adicionado TEXT DEFAULT CURRENT_TIMESTAMP," +
                "horas_jogadas REAL DEFAULT 0," +
                "UNIQUE(id_usuario, steam_app_id)," +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id)" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS avaliacao (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL," +
                "steam_app_id INTEGER NOT NULL," +
                "nota REAL NOT NULL," +
                "comentario TEXT," +
                "data_avaliacao TEXT DEFAULT CURRENT_TIMESTAMP," +
                "horas_jogadas REAL DEFAULT 0," +
                "UNIQUE(id_usuario, steam_app_id)," +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id)" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS favorito (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL," +
                "steam_app_id INTEGER NOT NULL," +
                "data_adicionado TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(id_usuario, steam_app_id)," +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id)" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS lista (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_usuario INTEGER NOT NULL," +
                "nome TEXT NOT NULL," +
                "data_criacao TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id)" +
                ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS lista_jogo (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_lista INTEGER NOT NULL," +
                "id_jogo INTEGER NOT NULL," +
                "data_adicionado TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(id_lista, id_jogo)," +
                "FOREIGN KEY(id_lista) REFERENCES lista(id) ON DELETE CASCADE," +
                "FOREIGN KEY(id_jogo) REFERENCES jogo(id)" +
                ")"
        );
    }

    private static void migrarBancoAntigo(Statement stmt) throws Exception {
        garantirColuna(stmt, "usuario", "username", "TEXT");
        garantirColuna(stmt, "jogo", "steam_app_id", "INTEGER");
        garantirColuna(stmt, "biblioteca", "steam_app_id", "INTEGER");
        garantirColuna(stmt, "avaliacao", "steam_app_id", "INTEGER");
        garantirColuna(stmt, "favorito", "steam_app_id", "INTEGER");

        stmt.executeUpdate(
                "UPDATE usuario SET username = 'usuario' || id " +
                "WHERE username IS NULL OR TRIM(username) = ''"
        );

        stmt.executeUpdate(
                "UPDATE jogo SET steam_app_id = (" +
                "SELECT CASE LOWER(TRIM(jogo.titulo)) " +
                "WHEN 'resident evil 4' THEN 2050650 " +
                "WHEN 'the last of us part i' THEN 1888930 " +
                "WHEN 'god of war ragnarök' THEN 2322010 " +
                "WHEN 'god of war ragnarok' THEN 2322010 " +
                "WHEN 'minecraft' THEN NULL " +
                "WHEN 'red dead redemption 2' THEN 1174180 " +
                "WHEN 'grand theft auto v' THEN 271590 " +
                "WHEN 'gta v' THEN 271590 " +
                "WHEN 'silent hill 2' THEN 2124490 " +
                "WHEN 'elden ring' THEN 1245620 " +
                "WHEN 'resident evil village' THEN 1196590 " +
                "WHEN 'the witcher 3' THEN 292030 " +
                "WHEN 'the witcher 3: wild hunt' THEN 292030 " +
                "WHEN 'cyberpunk 2077' THEN 1091500 " +
                "WHEN 'marvel''s spider-man 2' THEN 2651280 " +
                "ELSE jogo.steam_app_id END) " +
                "WHERE jogo.steam_app_id IS NULL"
        );

        migrarRelacionamentoAntigo(stmt, "biblioteca");
        migrarRelacionamentoAntigo(stmt, "avaliacao");
        migrarRelacionamentoAntigo(stmt, "favorito");
    }

    private static void migrarRelacionamentoAntigo(
            Statement stmt,
            String tabela) throws Exception {

        if (!temColuna(stmt, tabela, "id_jogo")) {
            return;
        }

        stmt.executeUpdate(
                "UPDATE " + tabela + " SET steam_app_id = " +
                "(SELECT j.steam_app_id FROM jogo j " +
                "WHERE j.id = " + tabela + ".id_jogo) " +
                "WHERE steam_app_id IS NULL"
        );
    }

    private static void garantirColuna(
            Statement stmt,
            String tabela,
            String coluna,
            String tipo) throws Exception {

        if (!temColuna(stmt, tabela, coluna)) {
            stmt.executeUpdate(
                    "ALTER TABLE " + tabela +
                    " ADD COLUMN " + coluna + " " + tipo
            );
        }
    }

    private static boolean temColuna(
            Statement stmt,
            String tabela,
            String coluna) throws Exception {

        try (ResultSet rs = stmt.executeQuery(
                "PRAGMA table_info(" + tabela + ")")) {
            while (rs.next()) {
                if (coluna.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void criarIndices(Statement stmt) throws Exception {
        stmt.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_usuario_username " +
                "ON usuario(username)"
        );
        stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_jogo_titulo " +
                "ON jogo(titulo)"
        );
        stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_biblioteca_usuario " +
                "ON biblioteca(id_usuario)"
        );
        stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_avaliacao_usuario " +
                "ON avaliacao(id_usuario)"
        );
        stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_favorito_usuario " +
                "ON favorito(id_usuario)"
        );
        stmt.execute(
                "CREATE INDEX IF NOT EXISTS idx_lista_usuario " +
                "ON lista(id_usuario)"
        );
    }

    private static void migrarSenhas(Connection conexao) throws Exception {
        String selecionar = "SELECT id, senha FROM usuario";
        String atualizar = "UPDATE usuario SET senha = ? WHERE id = ?";

        try (
                PreparedStatement select = conexao.prepareStatement(selecionar);
                ResultSet rs = select.executeQuery();
                PreparedStatement update = conexao.prepareStatement(atualizar)
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String senha = rs.getString("senha");

                if (senha == null || senha.trim().isEmpty()) {
                    continue;
                }

                if (!PasswordUtil.isHash(senha)) {
                    String senhaMigrada =
                            "GOOGLE_LOGIN".equals(senha)
                                    ? UUID.randomUUID().toString()
                                    : senha;

                    update.setString(1, PasswordUtil.hash(senhaMigrada));
                    update.setInt(2, id);
                    update.addBatch();
                }
            }
            update.executeBatch();
        }
    }

    /**
     * Carrega o catálogo de jogos a partir de jogos.csv sob demanda.
     * Não é chamado automaticamente na inicialização da aplicação.
     *
     * @return true se o catálogo foi carregado agora, false se já estava carregado
     */
    public static boolean carregarCatalogoOpcional() throws Exception {
        try (Connection conexao = Conexao.conectar()) {
            if (conexao == null) {
                throw new IllegalStateException("Não foi possível conectar ao SQLite.");
            }

            try (Statement stmt = conexao.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                criarTabelas(stmt);
            }

            if (catalogoJaCarregado(conexao)) {
                return false;
            }

            carregarCatalogo(conexao);
            return true;
        }
    }

    public static void carregarCatalogo(Connection conexao) throws Exception {
        String sql =
                "INSERT INTO jogo " +
                "(steam_app_id, titulo, descricao, genero, plataforma, capa) " +
                "SELECT ?, ?, ?, ?, 'PC', ? " +
                "WHERE NOT EXISTS (" +
                "SELECT 1 FROM jogo " +
                "WHERE steam_app_id = ? OR LOWER(titulo) = LOWER(?)" +
                ")";

        InputStream recurso = CriarBanco.class.getClassLoader()
                .getResourceAsStream("jogos.csv");

        if (recurso == null) {
            throw new IllegalStateException("Recurso jogos.csv não encontrado.");
        }

        try (
                BufferedReader leitor = new BufferedReader(
                        new InputStreamReader(recurso, StandardCharsets.UTF_8));
                PreparedStatement stmt = conexao.prepareStatement(sql)
        ) {
            String linha;
            boolean primeira = true;

            while ((linha = leitor.readLine()) != null) {
                if (primeira) {
                    primeira = false;
                    continue;
                }

                if (linha.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linha.split("\\|", -1);
                if (partes.length != 3) {
                    continue;
                }

                int appId = Integer.parseInt(partes[1]);
                String titulo = partes[0].trim();
                String genero = partes[2].trim();
                String capa =
                        "https://cdn.akamai.steamstatic.com/steam/apps/" +
                        appId + "/library_600x900_2x.jpg";

                stmt.setInt(1, appId);
                stmt.setString(2, titulo);
                stmt.setString(3, "Jogo disponível no catálogo do Inventory.");
                stmt.setString(4, genero);
                stmt.setString(5, capa);
                stmt.setInt(6, appId);
                stmt.setString(7, titulo);
                stmt.addBatch();
            }

            stmt.executeBatch();
        }

        try (PreparedStatement marcador = conexao.prepareStatement(
                "INSERT OR REPLACE INTO configuracao(chave, valor) VALUES ('catalogo_jogos', '1')")) {
            marcador.executeUpdate();
        }
    }

    private static boolean catalogoJaCarregado(Connection conexao) throws Exception {
        String sql =
                "SELECT valor FROM configuracao WHERE chave = 'catalogo_jogos'";

        try (PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() && "1".equals(rs.getString("valor"));
        }
    }
}
