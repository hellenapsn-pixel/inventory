package controller;

import dao.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/biblioteca")
public class BibliotecaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static class Jogo {

        int steamAppId;
        String nome;
        String capa;
        String status;
        double horas;

        Jogo(
                int steamAppId,
                String nome,
                String capa,
                String status,
                double horas
        ) {
            this.steamAppId = steamAppId;
            this.nome = nome;
            this.capa = capa;
            this.status = status;
            this.horas = horas;
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html"
            );

            return;
        }

        Usuario usuario =
                (Usuario) sessao.getAttribute("usuario");

        int idUsuario =
                usuario.getId();

        List<Jogo> queroJogar =
                buscarJogos(
                        idUsuario,
                        "quero_jogar"
                );

        List<Jogo> jogando =
                buscarJogos(
                        idUsuario,
                        "jogando"
                );

        List<Jogo> zerados =
                buscarJogos(
                        idUsuario,
                        "zerado"
                );

        int total =
                queroJogar.size()
                + jogando.size()
                + zerados.size();

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");

        html.append("<head>");

        html.append("<meta charset='UTF-8'>");

        html.append(
                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>"
        );

        html.append(
                "<title>Minha Biblioteca - Inventory</title>"
        );

        html.append(
                "<link rel='preconnect' href='https://fonts.googleapis.com'>"
        );

        html.append(
                "<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>"
        );

        html.append(
                "<link href='https://fonts.googleapis.com/css2?family=Rajdhani:wght@400;500;600;700&display=swap' rel='stylesheet'>"
        );

        html.append("<style>");

        html.append(
                "*{" +
                "box-sizing:border-box;" +
                "margin:0;" +
                "padding:0;" +
                "}"
        );

        html.append(
                "body{" +
                "font-family:'Rajdhani',sans-serif;" +
                "background:#0b0710;" +
                "color:#fff;" +
                "min-height:100vh;" +
                "}"
        );

        /* HEADER */

        html.append(
                "header{" +
                "height:74px;" +
                "background:#100b17;" +
                "border-bottom:1px solid #24152f;" +
                "display:flex;" +
                "align-items:center;" +
                "padding:0 5%;" +
                "position:sticky;" +
                "top:0;" +
                "z-index:100;" +
                "}"
        );

        html.append(
                ".logo{" +
                "font-size:24px;" +
                "font-weight:700;" +
                "color:#fff;" +
                "text-decoration:none;" +
                "letter-spacing:-1px;" +
                "font-family:'Rajdhani',sans-serif;" +
                "}"
        );

        html.append(
                ".logo span{" +
                "color:#a855f7;" +
                "}"
        );

        html.append(
                "nav{" +
                "display:flex;" +
                "gap:28px;" +
                "margin-left:55px;" +
                "}"
        );

        html.append(
                "nav a{" +
                "color:#aaa;" +
                "text-decoration:none;" +
                "font-size:14px;" +
                "font-weight:500;" +
                "transition:.2s;" +
                "}"
        );

        html.append(
                "nav a:hover{" +
                "color:#fff;" +
                "}"
        );

        html.append(
                "nav a.ativo{" +
                "color:#c084fc;" +
                "}"
        );

        /* CONTAINER */

        html.append(
                ".container{" +
                "width:90%;" +
                "max-width:1380px;" +
                "margin:0 auto;" +
                "padding:55px 0 80px;" +
                "}"
        );

        /* TOPO */

        html.append(
                ".topo{" +
                "display:flex;" +
                "justify-content:space-between;" +
                "align-items:flex-end;" +
                "margin-bottom:45px;" +
                "gap:20px;" +
                "}"
        );

        html.append(
                ".titulo-area h1{" +
                "font-size:36px;" +
                "font-weight:700;" +
                "letter-spacing:-1px;" +
                "margin-bottom:8px;" +
                "}"
        );

        html.append(
                ".titulo-area p{" +
                "color:#918999;" +
                "font-size:14px;" +
                "}"
        );

        html.append(
                ".contador-total{" +
                "background:#18101f;" +
                "border:1px solid #342044;" +
                "padding:12px 20px;" +
                "border-radius:12px;" +
                "color:#c084fc;" +
                "font-size:13px;" +
                "font-weight:600;" +
                "}"
        );

        /* SEÇÕES */

        html.append(
                ".secao{" +
                "margin-bottom:55px;" +
                "}"
        );

        html.append(
                ".cabecalho-secao{" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:space-between;" +
                "margin-bottom:22px;" +
                "}"
        );

        html.append(
                ".titulo-secao{" +
                "display:flex;" +
                "align-items:center;" +
                "gap:12px;" +
                "}"
        );

        html.append(
                ".icone-secao{" +
                "width:38px;" +
                "height:38px;" +
                "border-radius:10px;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "background:#1b1024;" +
                "border:1px solid #362047;" +
                "font-size:18px;" +
                "}"
        );

        html.append(
                ".titulo-secao h2{" +
                "font-size:21px;" +
                "font-weight:600;" +
                "}"
        );

        html.append(
                ".titulo-secao p{" +
                "font-size:12px;" +
                "color:#817888;" +
                "margin-top:2px;" +
                "}"
        );

        html.append(
                ".quantidade{" +
                "font-size:12px;" +
                "color:#9d8ba8;" +
                "background:#151019;" +
                "border:1px solid #2b1b36;" +
                "padding:7px 12px;" +
                "border-radius:20px;" +
                "}"
        );

        /* GRID */

        html.append(
                ".grid{" +
                "display:grid;" +
                "grid-template-columns:repeat(5,minmax(0,1fr));" +
                "gap:22px;" +
                "}"
        );

        /* CARD */

        html.append(
                ".card{" +
                "background:#120d18;" +
                "border:1px solid #25182e;" +
                "border-radius:14px;" +
                "overflow:hidden;" +
                "transition:.25s;" +
                "min-width:0;" +
                "}"
        );

        html.append(
                ".card:hover{" +
                "transform:translateY(-5px);" +
                "border-color:#5d3480;" +
                "box-shadow:0 15px 35px rgba(0,0,0,.35);" +
                "}"
        );

        html.append(
                ".capa-container{" +
                "width:100%;" +
                "aspect-ratio:2/3;" +
                "background:#18111f;" +
                "overflow:hidden;" +
                "}"
        );

        html.append(
                ".capa{" +
                "width:100%;" +
                "height:100%;" +
                "object-fit:cover;" +
                "display:block;" +
                "}"
        );

        html.append(
                ".conteudo-card{" +
                "padding:15px;" +
                "}"
        );

        html.append(
                ".nome-jogo{" +
                "font-size:14px;" +
                "font-weight:600;" +
                "line-height:1.4;" +
                "min-height:40px;" +
                "margin-bottom:10px;" +
                "}"
        );

        html.append(
                ".horas{" +
                "font-size:11px;" +
                "color:#94879c;" +
                "margin-bottom:12px;" +
                "}"
        );

        /* BOTÕES */

        html.append(
                ".acoes{" +
                "display:flex;" +
                "flex-direction:column;" +
                "gap:8px;" +
                "}"
        );

        html.append(
                ".btn{" +
                "width:100%;" +
                "padding:10px 12px;" +
                "border-radius:9px;" +
                "font-family:'Rajdhani',sans-serif;" +
                "font-size:11px;" +
                "font-weight:600;" +
                "text-align:center;" +
                "text-decoration:none;" +
                "cursor:pointer;" +
                "transition:.2s;" +
                "border:1px solid transparent;" +
                "}"
        );

        html.append(
                ".btn-roxo{" +
                "background:linear-gradient(135deg,#7c3aed,#9333ea);" +
                "color:#fff;" +
                "border-color:#8b5cf6;" +
                "}"
        );

        html.append(
                ".btn-roxo:hover{" +
                "background:linear-gradient(135deg,#8b5cf6,#a855f7);" +
                "transform:translateY(-1px);" +
                "box-shadow:0 5px 15px rgba(124,58,237,.35);" +
                "}"
        );

        html.append(
                ".btn-secundario{" +
                "background:#21142d;" +
                "color:#d8b4fe;" +
                "border-color:#56316f;" +
                "}"
        );

        html.append(
                ".btn-secundario:hover{" +
                "background:#2d1b3c;" +
                "color:#fff;" +
                "}"
        );

        html.append(
                ".btn-verde{" +
                "background:#17251c;" +
                "color:#a7f3c0;" +
                "border-color:#2e6841;" +
                "}"
        );

        html.append(
                ".btn-verde:hover{" +
                "background:#1e3426;" +
                "}"
        );

        html.append(
                ".tag{" +
                "display:inline-block;" +
                "font-size:10px;" +
                "padding:4px 8px;" +
                "border-radius:20px;" +
                "margin-bottom:9px;" +
                "background:#21142d;" +
                "color:#c084fc;" +
                "border:1px solid #49305c;" +
                "}"
        );

        /* VAZIO */

        html.append(
                ".vazio{" +
                "border:1px dashed #33213e;" +
                "background:#100b15;" +
                "border-radius:14px;" +
                "padding:45px 20px;" +
                "text-align:center;" +
                "color:#766b7d;" +
                "}"
        );

        html.append(
                ".vazio .emoji{" +
                "font-size:32px;" +
                "margin-bottom:12px;" +
                "}"
        );

        html.append(
                ".vazio h3{" +
                "font-size:15px;" +
                "color:#a69aaa;" +
                "margin-bottom:5px;" +
                "}"
        );

        html.append(
                ".vazio p{" +
                "font-size:12px;" +
                "}"
        );

        /* RESPONSIVO */

        html.append(
                "@media(max-width:1200px){" +
                ".grid{grid-template-columns:repeat(4,minmax(0,1fr));}" +
                "}"
        );

        html.append(
                "@media(max-width:900px){" +
                ".grid{grid-template-columns:repeat(3,minmax(0,1fr));}" +
                "nav{gap:15px;margin-left:30px;}" +
                "}"
        );

        html.append(
                "@media(max-width:650px){" +
                "header{padding:0 20px;}" +
                "nav{display:none;}" +
                ".container{width:92%;padding-top:35px;}" +
                ".topo{align-items:flex-start;flex-direction:column;}" +
                ".titulo-area h1{font-size:29px;}" +
                ".grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:14px;}" +
                ".conteudo-card{padding:11px;}" +
                ".nome-jogo{font-size:12px;}" +
                "}"
        );

        html.append(
                "@media(max-width:400px){" +
                ".grid{grid-template-columns:1fr;}" +
                "}"
        );


        html.append("html,body{margin:0;padding:0;min-height:100%;}");
        html.append("body{font-family:'Rajdhani',sans-serif !important;background:radial-gradient(circle at top,#24143a 0%,#0b0910 45%) !important;color:#f4f4f5;min-height:100vh;}");
        html.append("header{width:100% !important;box-sizing:border-box;display:flex !important;align-items:center !important;justify-content:space-between !important;padding:18px 40px !important;background:#0d0914 !important;border-bottom:1px solid #30263a !important;position:relative !important;z-index:20 !important;backdrop-filter:none !important;}");
        html.append(".logo-area{display:flex !important;align-items:center !important;gap:9px !important;}");
        html.append(".logo-header{width:40px !important;height:40px !important;object-fit:contain;display:block;}");
        html.append(".logo-area h1{margin:0 !important;color:white !important;font-size:30px !important;font-weight:700 !important;font-family:'Rajdhani',sans-serif !important;}");
        html.append("header nav{display:flex !important;align-items:center !important;gap:28px !important;margin:0 !important;}");
        html.append("header nav a{color:#b9afc5 !important;text-decoration:none !important;font-size:14px !important;font-family:'Rajdhani',sans-serif !important;font-weight:400 !important;transition:.2s;}");
        html.append("header nav a:hover{color:#c084fc !important;}");
        html.append("@media(max-width:850px){header{padding:14px 20px !important;flex-wrap:wrap;gap:12px;}header nav{gap:15px !important;flex-wrap:wrap;}header nav a{font-size:13px !important;}}");
        html.append("</style>");

        html.append("</head>");

        html.append("<body>");

        /* HEADER */

        html.append("<header>");

        html.append(
                "<div class='logo-area'>" +
                "<img src='icon.png' alt='Logo Inventory' class='logo-header'>" +
                "<h1>Inventory</h1>" +
                "</div>"
        );

        html.append("<nav>");
        html.append("<a href='index.html'>Início</a>");
        html.append("<a href='buscar-usuarios'>Buscar usuários</a>");
        html.append("<a href='jogos'>Jogos</a>");
        html.append("<a href='perfil'>Meu Perfil</a>");
        html.append("<a href='biblioteca'>Biblioteca</a>");
        html.append("<a href='" + request.getContextPath() + "/listas'>Listas</a>");
        html.append("<a href='logout'>Sair</a>");
        html.append("</nav>");

        html.append("</header>");

        /* CONTEÚDO */

        html.append("<main class='container'>");

        html.append("<div class='topo'>");

        html.append("<div class='titulo-area'>");

        html.append("<h1>Minha biblioteca</h1>");

        html.append(
                "<p>Organize seus jogos e acompanhe sua jornada.</p>"
        );

        html.append("</div>");

        html.append(
                "<div class='contador-total'>" +
                total +
                (total == 1 ? " jogo" : " jogos") +
                "</div>"
        );

        html.append("</div>");

        criarSecao(
                html,
                "🎮",
                "Quero jogar",
                "Jogos que você adicionou para jogar depois.",
                queroJogar,
                "quero_jogar"
        );

        criarSecao(
                html,
                "▶",
                "Jogando",
                "Jogos que você está jogando atualmente.",
                jogando,
                "jogando"
        );

        criarSecao(
                html,
                "✓",
                "Zerados",
                "Jogos que você já terminou.",
                zerados,
                "zerado"
        );

        html.append("</main>");

        html.append("</body>");

        html.append("</html>");

        response.getWriter().write(
                html.toString()
        );
    }

    private void criarSecao(
            StringBuilder html,
            String icone,
            String titulo,
            String descricao,
            List<Jogo> jogos,
            String status
    ) {

        html.append("<section class='secao'>");

        html.append("<div class='cabecalho-secao'>");

        html.append("<div class='titulo-secao'>");

        html.append(
                "<div class='icone-secao'>" +
                icone +
                "</div>"
        );

        html.append("<div>");

        html.append(
                "<h2>" +
                escaparHtml(titulo) +
                "</h2>"
        );

        html.append(
                "<p>" +
                escaparHtml(descricao) +
                "</p>"
        );

        html.append("</div>");

        html.append("</div>");

        html.append(
                "<span class='quantidade'>" +
                jogos.size() +
                (jogos.size() == 1 ? " jogo" : " jogos") +
                "</span>"
        );

        html.append("</div>");

        if (jogos.isEmpty()) {

            html.append("<div class='vazio'>");

            html.append(
                    "<div class='emoji'>" +
                    icone +
                    "</div>"
            );

            html.append(
                    "<h3>Nenhum jogo aqui ainda</h3>"
            );

            html.append(
                    "<p>Adicione jogos à sua biblioteca para eles aparecerem nesta seção.</p>"
            );

            html.append("</div>");

            html.append("</section>");

            return;
        }

        html.append("<div class='grid'>");

        for (Jogo jogo : jogos) {

            String capa;

            try {
                capa =
                        "capa?titulo=" +
                        java.net.URLEncoder.encode(
                                jogo.nome == null ? "" : jogo.nome,
                                "UTF-8"
                        );
            } catch (java.io.UnsupportedEncodingException e) {
                capa = "capa?appId=" + jogo.steamAppId;
            }

            html.append("<article class='card'>");

            html.append(
                    "<div class='capa-container'>"
            );

            html.append(
                    "<img class='capa' " +
                    "src='" +
                    escaparHtml(capa) +
                    "' " +
                    "alt='" +
                    escaparHtml(jogo.nome) +
                    "' " +
                    "onerror=\"this.style.display='none';\">"
            );

            html.append("</div>");

            html.append(
                    "<div class='conteudo-card'>"
            );

            if ("quero_jogar".equals(status)) {

                html.append(
                        "<span class='tag'>QUERO JOGAR</span>"
                );

            } else if ("jogando".equals(status)) {

                html.append(
                        "<span class='tag'>JOGANDO</span>"
                );

            } else {

                html.append(
                        "<span class='tag'>ZERADO</span>"
                );
            }

            html.append(
                    "<div class='nome-jogo'>" +
                    escaparHtml(jogo.nome) +
                    "</div>"
            );

            if (jogo.horas > 0) {

                html.append(
                        "<div class='horas'>" +
                        formatarHoras(jogo.horas) +
                        " jogadas" +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='horas'>Sem horas registradas</div>"
                );
            }

            html.append("<div class='acoes'>");

            if ("quero_jogar".equals(status)) {

                html.append(
                        "<form method='POST' action='biblioteca-status'>"
                );

                html.append(
                        "<input type='hidden' name='steamAppId' value='" +
                        jogo.steamAppId +
                        "'>"
                );

                html.append(
                        "<input type='hidden' name='status' value='jogando'>"
                );

                html.append(
                        "<button class='btn btn-roxo' type='submit'>" +
                        "▶ Começar a jogar" +
                        "</button>"
                );

                html.append("</form>");

            } else if ("jogando".equals(status)) {

                html.append(
                        "<a class='btn btn-roxo' href='avaliar?id=" +
                        jogo.steamAppId +
                        "'>" +
                        "★ Avaliar jogo" +
                        "</a>"
                );

                html.append(
                        "<form method='POST' action='biblioteca-status'>"
                );

                html.append(
                        "<input type='hidden' name='steamAppId' value='" +
                        jogo.steamAppId +
                        "'>"
                );

                html.append(
                        "<input type='hidden' name='status' value='zerado'>"
                );

                html.append(
                        "<button class='btn btn-verde' type='submit'>" +
                        "✓ Marcar como zerado" +
                        "</button>"
                );

                html.append("</form>");
            }

            html.append("</div>");

            html.append("</div>");

            html.append("</article>");
        }

        html.append("</div>");

        html.append("</section>");
    }

    private List<Jogo> buscarJogos(
            int idUsuario,
            String status
    ) {

        List<Jogo> jogos =
                new ArrayList<Jogo>();

        String sql =
                "SELECT steam_app_id, status, horas_jogadas " +
                "FROM biblioteca " +
                "WHERE id_usuario = ? " +
                "AND status = ? " +
                "ORDER BY id DESC";

        Connection conexao =
                Conexao.conectar();

        if (conexao == null) {
            return jogos;
        }

        try (
                PreparedStatement ps =
                        conexao.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    idUsuario
            );

            ps.setString(
                    2,
                    status
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    int steamAppId =
                            rs.getInt(
                                    "steam_app_id"
                            );

                    String nome =
                            buscarNomeJogo(
                                    conexao,
                                    steamAppId
                            );

                    String capa =
                            "https://cdn.cloudflare.steamstatic.com/steam/apps/"
                            + steamAppId
                            + "/library_600x900.jpg";

                    double horas =
                            rs.getDouble(
                                    "horas_jogadas"
                            );

                    jogos.add(
                            new Jogo(
                                    steamAppId,
                                    nome,
                                    capa,
                                    rs.getString("status"),
                                    horas
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {
                conexao.close();
            } catch (Exception ignored) {
            }
        }

        return jogos;
    }

    private String buscarNomeJogo(
            Connection conexao,
            int steamAppId
    ) {

        String sql =
                "SELECT titulo " +
                "FROM jogo " +
                "WHERE steam_app_id = ? " +
                "LIMIT 1";

        try (
                PreparedStatement ps =
                        conexao.prepareStatement(sql)
        ) {

            ps.setInt(
                    1,
                    steamAppId
            );

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                if (rs.next()) {

                    String titulo =
                            rs.getString("titulo");

                    if (titulo != null &&
                            !titulo.trim().isEmpty()) {

                        return titulo;
                    }
                }
            }

        } catch (Exception e) {

            /*
             * Caso a coluna steam_app_id ainda não
             * exista no banco antigo, continua usando
             * a API da Steam.
             */
        }

        String nomeSteam =
                buscarNomeSteam(
                        steamAppId
                );

        if (nomeSteam != null &&
                !nomeSteam.trim().isEmpty()) {

            return nomeSteam;
        }

        return "Jogo " + steamAppId;
    }

    private String buscarNomeSteam(
            int steamAppId
    ) {

        HttpURLConnection conexao =
                null;

        try {

            URL url =
                    new URL(
                            "https://store.steampowered.com/api/appdetails?appids="
                            + steamAppId
                    );

            conexao =
                    (HttpURLConnection)
                    url.openConnection();

            conexao.setRequestMethod("GET");

            conexao.setConnectTimeout(5000);

            conexao.setReadTimeout(5000);

            conexao.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
            );

            int codigo =
                    conexao.getResponseCode();

            if (codigo != 200) {
                return null;
            }

            StringBuilder resposta =
                    new StringBuilder();

            try (
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(
                                            conexao.getInputStream(),
                                            "UTF-8"
                                    )
                            )
            ) {

                String linha;

                while (
                        (linha = br.readLine())
                                != null
                ) {

                    resposta.append(linha);
                }
            }

            Pattern pattern =
                    Pattern.compile(
                            "\"name\"\\s*:\\s*\"([^\"]+)\""
                    );

            Matcher matcher =
                    pattern.matcher(
                            resposta.toString()
                    );

            if (matcher.find()) {

                return matcher.group(1)
                        .replace("\\/", "/")
                        .replace("\\\"", "\"");
            }

        } catch (Exception e) {

            System.out.println(
                    "Erro ao buscar jogo na Steam: "
                    + e.getMessage()
            );

        } finally {

            if (conexao != null) {

                conexao.disconnect();
            }
        }

        return null;
    }

    private String formatarHoras(
            double horas
    ) {

        if (horas == (long) horas) {

            return String.valueOf(
                    (long) horas
            );
        }

        return String.format(
                "%.1f",
                horas
        );
    }

    private String escaparHtml(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}