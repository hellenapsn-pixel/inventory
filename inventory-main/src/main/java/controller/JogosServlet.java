package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/jogos")
public class JogosServlet extends HttpServlet {

    private static final int POR_PAGINA = 20;

    private static class Jogo {
        String nome;
        int appId;
        String genero;

        Jogo(String nome, int appId, String genero) {
            this.nome = nome;
            this.appId = appId;
            this.genero = genero;
        }
    }

    private List<Jogo> carregarJogos() {

        List<Jogo> jogos = new ArrayList<Jogo>();

        String sql =
                "SELECT titulo, steam_app_id, genero " +
                "FROM jogo " +
                "WHERE steam_app_id IS NOT NULL " +
                "ORDER BY id";

        try (
                java.sql.Connection conexao = dao.Conexao.conectar();
                java.sql.PreparedStatement stmt = conexao.prepareStatement(sql);
                java.sql.ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                jogos.add(new Jogo(
                        rs.getString("titulo"),
                        rs.getInt("steam_app_id"),
                        rs.getString("genero")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jogos;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        String busca = request.getParameter("busca");
        String generoFiltro = request.getParameter("genero");
        String paginaTexto = request.getParameter("pagina");

        if (busca == null) {
            busca = "";
        }

        if (generoFiltro == null) {
            generoFiltro = "";
        }

        busca = busca.trim();
        generoFiltro = generoFiltro.trim();

        int pagina = 1;

        try {
            if (paginaTexto != null) {
                pagina = Integer.parseInt(paginaTexto);
            }

            if (pagina < 1) {
                pagina = 1;
            }

        } catch (Exception e) {
            pagina = 1;
        }

        List<Jogo> jogos = carregarJogos();
        List<Jogo> filtrados = new ArrayList<Jogo>();

        for (Jogo jogo : jogos) {

            boolean passaBusca =
                    busca.isEmpty()
                    || jogo.nome.toLowerCase(Locale.ROOT)
                    .contains(busca.toLowerCase(Locale.ROOT));

            boolean passaGenero =
                    generoFiltro.isEmpty()
                    || jogo.genero.equalsIgnoreCase(generoFiltro);

            if (passaBusca && passaGenero) {
                filtrados.add(jogo);
            }
        }

        int total = filtrados.size();

        int totalPaginas = Math.max(
                1,
                (int) Math.ceil(total / (double) POR_PAGINA)
        );

        if (pagina > totalPaginas) {
            pagina = totalPaginas;
        }

        int inicio = (pagina - 1) * POR_PAGINA;

        int fim = Math.min(
                inicio + POR_PAGINA,
                total
        );

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Jogos - Inventory</title>");

        html.append("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        html.append("<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>");
        html.append("<link href='https://fonts.googleapis.com/css2?family=Rajdhani:wght@400;500;600;700&display=swap' rel='stylesheet'>");

        html.append("<style>");

        html.append("*{box-sizing:border-box}");

        html.append(
            "body{margin:0;" +
            "background:linear-gradient(135deg,#0d0714,#160b24,#0d0714);" +
            "min-height:100vh;color:#fff;" +
            "font-family:'Rajdhani',sans-serif}"
        );

        html.append(
            "header{display:flex;align-items:center;" +
            "justify-content:space-between;gap:20px;" +
            "padding:20px 6%;background:#10091a;" +
            "border-bottom:1px solid #2e1a40;" +
            "position:sticky;top:0;z-index:10}"
        );

        html.append(
            "header h1{margin:0;color:#fff;font-size:27px;font-family:'Rajdhani',sans-serif;font-weight:700;letter-spacing:1px}"
        );

        html.append(
            "nav{display:flex;gap:20px;flex-wrap:wrap;" +
            "justify-content:center}"
        );

        html.append(
            "nav a{color:#ddd;text-decoration:none;font-size:14px}"
        );

        html.append(
            "nav a:hover{color:#c084fc}"
        );

        html.append(
            ".container{max-width:1250px;margin:0 auto;" +
            "padding:35px 20px 60px}"
        );

        html.append(
            ".titulo{text-align:center;color:#c084fc;" +
            "font-size:38px;margin:5px 0}"
        );

        html.append(
            ".filtros{display:flex;justify-content:center;" +
            "gap:10px;flex-wrap:wrap;margin-bottom:28px}"
        );

        html.append(
            ".filtros input,.filtros select{" +
            "background:#21152d;color:#fff;" +
            "border:1px solid #5b2a80;border-radius:9px;" +
            "padding:12px 14px;font-family:inherit;outline:none}"
        );

        html.append(
            ".filtros input{width:min(430px,90vw)}"
        );

        html.append(
            ".filtros button{border:0;border-radius:9px;" +
            "padding:12px 18px;" +
            "background:linear-gradient(135deg,#7c3aed,#9333ea);" +
            "color:white;font-weight:700;cursor:pointer}"
        );

        html.append(
            ".contador{text-align:center;color:#999;" +
            "margin:0 0 22px;font-size:14px}"
        );

        html.append(
            ".grid{display:grid;" +
            "grid-template-columns:repeat(5,minmax(0,1fr));" +
            "gap:22px}"
        );

        html.append(
            ".card{background:linear-gradient(145deg,#21152d,#17101f);" +
            "border:1px solid #38204d;padding:10px;" +
            "border-radius:15px;overflow:hidden;" +
            "transition:.25s;" +
            "box-shadow:0 8px 25px rgba(0,0,0,.3)}"
        );

        html.append(
            ".card:hover{transform:translateY(-6px);" +
            "border-color:#8b5cf6;" +
            "box-shadow:0 15px 35px rgba(124,58,237,.3)}"
        );

        html.append(
            ".capa{width:100%;aspect-ratio:2/3;" +
            "object-fit:cover;border-radius:10px;" +
            "display:block;background:#120d18}"
        );

        html.append(
            ".card h3{font-size:16px;line-height:1.3;" +
            "margin:13px 3px 8px;min-height:42px}"
        );

        html.append(
            ".tag{display:inline-block;background:#2d183e;" +
            "border:1px solid #4c2670;color:#c084fc;" +
            "border-radius:20px;padding:4px 8px;" +
            "font-size:11px;margin:2px}"
        );

        // =====================================================
        // BOTÕES DOS CARDS
        // =====================================================

        html.append(
            ".acoes{" +
            "display:flex;" +
            "flex-direction:column;" +
            "width:100%;" +
            "margin-top:10px;" +
            "gap:8px}"
        );

        html.append(
            ".acoes form{" +
            "margin:0;" +
            "padding:0;" +
            "width:100%}"
        );

        html.append(
            ".btn-biblioteca{" +
            "display:flex;" +
            "align-items:center;" +
            "justify-content:center;" +
            "width:100%;" +
            "min-height:40px;" +
            "padding:10px 12px;" +
            "background:linear-gradient(135deg,#7c3aed,#9333ea);" +
            "border:1px solid #8b5cf6;" +
            "border-radius:8px;" +
            "color:#fff;" +
            "text-decoration:none;" +
            "font-family:'Rajdhani',sans-serif;" +
            "font-size:12px;" +
            "font-weight:600;" +
            "text-align:center;" +
            "cursor:pointer;" +
            "transition:all .2s ease;" +
            "white-space:nowrap}"
        );

        html.append(
            ".btn-biblioteca:hover{" +
            "background:linear-gradient(135deg,#9333ea,#a855f7);" +
            "border-color:#c084fc;" +
            "transform:translateY(-1px);" +
            "box-shadow:0 5px 15px rgba(124,58,237,.35)}"
        );

        html.append(
            ".btn-favorito{" +
            "display:flex;" +
            "align-items:center;" +
            "justify-content:center;" +
            "width:100%;" +
            "min-height:40px;" +
            "padding:10px 12px;" +
            "background:#21152d;" +
            "border:1px solid #7c3aed;" +
            "border-radius:8px;" +
            "color:#c084fc;" +
            "font-family:'Rajdhani',sans-serif;" +
            "font-size:12px;" +
            "font-weight:600;" +
            "text-align:center;" +
            "cursor:pointer;" +
            "transition:all .2s ease;" +
            "white-space:nowrap}"
        );

        html.append(
            ".btn-favorito:hover{" +
            "background:#7c3aed;" +
            "border-color:#a855f7;" +
            "color:#fff;" +
            "transform:translateY(-1px);" +
            "box-shadow:0 5px 15px rgba(124,58,237,.3)}"
        );

        html.append(
            ".paginacao{display:flex;justify-content:center;" +
            "gap:7px;flex-wrap:wrap;margin-top:32px}"
        );

        html.append(
            ".pagina{padding:9px 13px;border-radius:8px;" +
            "background:#21152d;border:1px solid #4c2670;" +
            "color:#ddd;text-decoration:none;font-size:14px}"
        );

        html.append(
            ".pagina:hover,.pagina.ativa{" +
            "background:#7c3aed;color:#fff;" +
            "border-color:#8b5cf6}"
        );

        html.append(
            ".vazio{grid-column:1/-1;text-align:center;" +
            "padding:50px;background:#17101f;" +
            "border:1px solid #38204d;border-radius:15px;" +
            "color:#aaa}"
        );

        html.append(
            "@media(max-width:1050px){" +
            ".grid{grid-template-columns:repeat(4,1fr)}}"
        );

        html.append(
            "@media(max-width:800px){" +
            "header{flex-direction:column}" +
            ".grid{grid-template-columns:repeat(3,1fr)}}"
        );

        html.append(
            "@media(max-width:600px){" +
            ".container{padding:22px 12px 45px}" +
            ".titulo{font-size:30px}" +
            ".grid{grid-template-columns:repeat(2,1fr);gap:13px}" +
            ".card{padding:8px}" +
            ".card h3{font-size:14px}" +
            ".btn-biblioteca,.btn-favorito{" +
            "font-size:10px;padding:9px 6px}" +
            "nav{gap:12px}}"
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
        html.append("<link rel='stylesheet' href='").append(request.getContextPath()).append("/style.css'>");
        html.append("</head>");
        html.append("<body>");

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

        html.append("<main class='container'>");

        html.append("<h2 class='titulo'>Explore os Jogos</h2>");

        html.append(
            "<form class='filtros' method='GET' action='jogos'>"
        );

        html.append(
            "<input type='search' name='busca' " +
            "placeholder='Pesquisar jogo...' value='"
        );

        html.append(escapar(busca));

        html.append("'>");

        html.append("<select name='genero'>");

        html.append(
            "<option value=''>Todos os gêneros</option>"
        );

        String[] generos = {
            "Ação",
            "Aventura",
            "RPG",
            "Terror",
            "Tiro",
            "Estratégia",
            "Corrida",
            "Esporte",
            "Simulação",
            "Plataforma",
            "Puzzle",
            "Sobrevivência",
            "Casual",
            "Luta"
        };

        for (String genero : generos) {

            html.append("<option value='");
            html.append(escapar(genero));
            html.append("'");

            if (genero.equalsIgnoreCase(generoFiltro)) {
                html.append(" selected");
            }

            html.append(">");
            html.append(escapar(genero));
            html.append("</option>");
        }

        html.append("</select>");
        html.append("<button type='submit'>Pesquisar</button>");
        html.append("</form>");

        html.append("<p class='contador'>Mostrando ");

        if (total == 0) {
            html.append("0");
        } else {
            html.append(inicio + 1);
            html.append("–");
            html.append(fim);
        }

        html.append(" de ");
        html.append(total);
        html.append(" jogos</p>");

        html.append("<section class='grid'>");

        if (total == 0) {

            html.append(
                "<div class='vazio'>Nenhum jogo encontrado.</div>"
            );

        } else {

            for (int i = inicio; i < fim; i++) {

                Jogo jogo = filtrados.get(i);

                String capa =
                    "capa?appId=" + jogo.appId;

                String capaFallback = capa;

                html.append("<article class='card'>");

                html.append("<img class='capa' src='");
                html.append(capa);
                html.append("' alt='Capa de ");
                html.append(escapar(jogo.nome));
                html.append("' loading='lazy' ");

                html.append(
                    "onerror=\"this.onerror=null;" +
                    "this.src='"
                );

                html.append(capaFallback);
                html.append("';\">");

                html.append("<h3>");
                html.append(escapar(jogo.nome));
                html.append("</h3>");

                html.append("<span class='tag'>");
                html.append(escapar(jogo.genero));
                html.append("</span>");

                html.append("<div class='acoes'>");

                html.append(
                    "<a class='btn-biblioteca' " +
                    "href='adicionar-biblioteca?id="
                );

                html.append(jogo.appId);

                html.append("'>");
                html.append("+ Minha biblioteca");
                html.append("</a>");

                html.append(
                    "<form method='post' action='favorito'>"
                );

                html.append(
                    "<input type='hidden' " +
                    "name='steamAppId' value='"
                );

                html.append(jogo.appId);

                html.append("'>");

                html.append(
                    "<button type='submit' " +
                    "class='btn-favorito'>"
                );

                html.append("♡ Adicionar aos favoritos");

                html.append("</button>");

                html.append("</form>");

                html.append("</div>");

                html.append("</article>");
            }
        }

        html.append("</section>");

        if (totalPaginas > 1) {

            html.append("<div class='paginacao'>");

            if (pagina > 1) {

                html.append(
                    linkPagina(
                        pagina - 1,
                        busca,
                        generoFiltro,
                        "‹ Anterior"
                    )
                );
            }

            for (int p = 1; p <= totalPaginas; p++) {

                if (
                    p == pagina
                    || p == 1
                    || p == totalPaginas
                    || Math.abs(p - pagina) <= 2
                ) {

                    html.append(
                        linkPagina(
                            p,
                            busca,
                            generoFiltro,
                            String.valueOf(p),
                            p == pagina
                        )
                    );
                }
            }

            if (pagina < totalPaginas) {

                html.append(
                    linkPagina(
                        pagina + 1,
                        busca,
                        generoFiltro,
                        "Próxima ›"
                    )
                );
            }

            html.append("</div>");
        }

        html.append("</main>");

        html.append("</body>");
        html.append("</html>");

        response.getWriter().print(html.toString());
    }

    private static String linkPagina(
            int pagina,
            String busca,
            String genero,
            String texto) {

        return linkPagina(
            pagina,
            busca,
            genero,
            texto,
            false
        );
    }

    private static String linkPagina(
            int pagina,
            String busca,
            String genero,
            String texto,
            boolean ativa) {

        StringBuilder url =
            new StringBuilder("jogos?pagina=")
            .append(pagina);

        if (busca != null && !busca.isEmpty()) {
            url.append("&busca=");
            url.append(urlEncode(busca));
        }

        if (genero != null && !genero.isEmpty()) {
            url.append("&genero=");
            url.append(urlEncode(genero));
        }

        return "<a class='pagina"
                + (ativa ? " ativa" : "")
                + "' href='"
                + url.toString()
                + "'>"
                + escapar(texto)
                + "</a>";
    }

    private static String urlEncode(String texto) {

        try {
            return java.net.URLEncoder.encode(texto, "UTF-8");

        } catch (Exception e) {
            return texto;
        }
    }

    private static String escapar(String texto) {

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