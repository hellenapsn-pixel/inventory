package controller;

import dao.Conexao;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/listas")
public class ListasServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect("login.html");
            return;
        }

        try {

            Usuario usuario =
                    (Usuario) sessao.getAttribute("usuario");

            int idUsuario =
                    usuario.getId();

            List<Jogo> jogos =
                    carregarTodosJogos();

            List<Lista> listas =
                    carregarListas(idUsuario);

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            StringBuilder html =
                    new StringBuilder();

            // =====================================================
            // HTML
            // =====================================================

            html.append("<!DOCTYPE html>");
            html.append("<html lang='pt-BR'>");

            html.append("<head>");
            html.append(
        "<link rel='icon' " +
        "type='image/png' " +
        "href='logo.png'>"
);

            html.append("<meta charset='UTF-8'>");

            html.append(
                    "<meta name='viewport' " +
                    "content='width=device-width, initial-scale=1.0'>"
            );

            html.append(
                    "<title>Listas - Inventory</title>"
            );

            html.append(
                    "<link rel='stylesheet' " +
                    "href='style.css'>"
            );

            // =====================================================
            // CSS
            // =====================================================

            html.append("<style>");

            html.append(
                    "body{" +
                    "margin:0;" +
                    "background:linear-gradient(" +
                    "135deg,#0d0714,#160b24,#0d0714" +
                    ");" +
                    "min-height:100vh;" +
                    "color:#fff;" +
                    "font-family:'Rajdhani',sans-serif;" +
                    "}"
            );

            html.append(
                    ".lists-page{" +
                    "max-width:1000px;" +
                    "margin:auto;" +
                    "padding:30px 20px 60px;" +
                    "}"
            );

            // =====================================================
            // CRIAR LISTA
            // =====================================================

            html.append(
                    ".create-box{" +
                    "background:linear-gradient(" +
                    "135deg,#30114b,#21172a" +
                    ");" +
                    "border:1px solid #54206f;" +
                    "border-radius:17px;" +
                    "padding:25px;" +
                    "margin-bottom:25px;" +
                    "}"
            );

            html.append(
                    ".create-title{" +
                    "font-size:26px;" +
                    "font-weight:bold;" +
                    "margin-bottom:6px;" +
                    "}"
            );

            html.append(
                    ".create-text{" +
                    "color:#b5a6bf;" +
                    "margin-bottom:20px;" +
                    "}"
            );

            html.append(
                    ".create-form{" +
                    "display:flex;" +
                    "gap:10px;" +
                    "}"
            );

            html.append(
                    ".create-input{" +
                    "flex:1;" +
                    "background:#17121d;" +
                    "border:1px solid #53345f;" +
                    "border-radius:8px;" +
                    "padding:13px;" +
                    "color:#fff;" +
                    "font-size:15px;" +
                    "}"
            );

            html.append(
                    ".create-button{" +
                    "background:#7300d1;" +
                    "border:none;" +
                    "color:#fff;" +
                    "border-radius:8px;" +
                    "padding:0 20px;" +
                    "font-weight:bold;" +
                    "cursor:pointer;" +
                    "}"
            );

            html.append(
                    ".create-button:hover{" +
                    "background:#8e00f5;" +
                    "}"
            );

            // =====================================================
            // LISTA
            // =====================================================

            html.append(
                    ".list-box{" +
                    "background:#201628;" +
                    "border:1px solid #4b285a;" +
                    "border-radius:15px;" +
                    "padding:22px;" +
                    "margin-bottom:22px;" +
                    "}"
            );

            html.append(
                    ".list-head{" +
                    "display:flex;" +
                    "justify-content:space-between;" +
                    "align-items:center;" +
                    "margin-bottom:18px;" +
                    "}"
            );

            html.append(
                    ".list-name{" +
                    "font-size:22px;" +
                    "font-weight:bold;" +
                    "}"
            );

            html.append(
                    ".delete-button{" +
                    "border:1px solid #8c3c52;" +
                    "background:transparent;" +
                    "color:#f0a1b4;" +
                    "border-radius:8px;" +
                    "padding:8px 13px;" +
                    "cursor:pointer;" +
                    "font-weight:bold;" +
                    "}"
            );

            html.append(
                    ".delete-button:hover{" +
                    "background:#4a1c2c;" +
                    "}"
            );

            // =====================================================
            // JOGOS DA LISTA
            // =====================================================

            html.append(
                    ".list-games{" +
                    "display:grid;" +
                    "grid-template-columns:" +
                    "repeat(auto-fill,minmax(125px,1fr));" +
                    "gap:15px;" +
                    "}"
            );

            html.append(
                    ".list-game{" +
                    "background:#17111c;" +
                    "border:1px solid #35243e;" +
                    "border-radius:10px;" +
                    "overflow:hidden;" +
                    "}"
            );

            html.append(
                    ".list-cover{" +
                    "width:100%;" +
                    "height:180px;" +
                    "object-fit:cover;" +
                    "display:block;" +
                    "background:transparent;" +
                    "}"
            );

            html.append(
                    ".list-cover-placeholder{" +
                    "width:100%;" +
                    "height:180px;" +
                    "display:flex;" +
                    "align-items:center;" +
                    "justify-content:center;" +
                    "text-align:center;" +
                    "padding:10px;" +
                    "background:#17111c;" +
                    "color:#777;" +
                    "font-size:12px;" +
                    "}"
            );

            html.append(
                    ".list-game-title{" +
                    "padding:10px;" +
                    "font-size:13px;" +
                    "font-weight:bold;" +
                    "line-height:1.3;" +
                    "}"
            );

            // =====================================================
            // ADICIONAR JOGO
            // =====================================================

            html.append(
                    ".add-game-form{" +
                    "margin-top:17px;" +
                    "display:flex;" +
                    "gap:10px;" +
                    "}"
            );

            html.append(
                    ".game-select{" +
                    "flex:1;" +
                    "background:#17121d;" +
                    "border:1px solid #4a3454;" +
                    "color:#fff;" +
                    "border-radius:8px;" +
                    "padding:11px;" +
                    "}"
            );

            html.append(
                    ".add-button{" +
                    "background:#6300c0;" +
                    "border:none;" +
                    "color:#fff;" +
                    "border-radius:8px;" +
                    "padding:0 18px;" +
                    "font-weight:bold;" +
                    "cursor:pointer;" +
                    "}"
            );

            html.append(
                    ".add-button:hover{" +
                    "background:#8300ed;" +
                    "}"
            );

            // =====================================================
            // VAZIO
            // =====================================================

            html.append(
                    ".empty{" +
                    "text-align:center;" +
                    "color:#847c89;" +
                    "padding:25px;" +
                    "}"
            );

            // =====================================================
            // RESPONSIVO
            // =====================================================

            html.append(
                    "@media(max-width:650px){" +
                    ".create-form," +
                    ".add-game-form{" +
                    "flex-direction:column;" +
                    "}" +
                    ".create-button," +
                    ".add-button{" +
                    "height:44px;" +
                    "}" +
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
        html.append("<link rel='stylesheet' href='").append(request.getContextPath()).append("/style.css'>");

            html.append("</head>");
            html.append("<body>");
        html.append("<style>.inventory-header{min-height:74px;height:auto;display:flex;align-items:center;justify-content:space-between;gap:22px;padding:12px clamp(18px,5vw,64px);background:rgba(12,9,17,.96);border-bottom:1px solid #30263a;position:sticky;top:0;z-index:1000;backdrop-filter:blur(16px);box-shadow:0 8px 28px rgba(0,0,0,.24)}.inventory-brand{display:flex;align-items:center;gap:11px;flex:0 0 auto;color:#f5f3f7;text-decoration:none;font:700 19px DM Sans,Arial,sans-serif;letter-spacing:.13em}.inventory-brand:hover{color:#fff}.inventory-brand-icon{width:148px;height:46px;object-fit:contain}.brand-dot{color:#a855f7}.inventory-nav{display:flex;align-items:center;justify-content:flex-end;gap:5px;flex-wrap:wrap;margin:0}.inventory-nav a{display:inline-flex;align-items:center;justify-content:center;padding:10px 12px;border-radius:8px;color:#aaa1b4;text-decoration:none;font:600 12px DM Sans,Arial,sans-serif;letter-spacing:.02em;white-space:nowrap;transition:background .2s,color .2s}.inventory-nav a:hover,.inventory-nav a.nav-current{background:rgba(124,58,237,.16);color:#fff}.inventory-nav a.inventory-exit{margin-left:5px;border:1px solid #493252;color:#cbb8dc}.inventory-nav a.inventory-exit:hover{border-color:#8b5cf6;background:rgba(124,58,237,.2)}@media(max-width:760px){.inventory-header{align-items:flex-start;flex-direction:column;gap:10px;padding:12px 16px}.inventory-brand-icon{width:120px;height:38px}.inventory-nav{width:100%;justify-content:flex-start;gap:3px}.inventory-nav a{padding:8px 9px;font-size:11px}.inventory-nav a.inventory-exit{margin-left:0}}</style>");

            // =====================================================
            // HEADER
            // =====================================================

            html.append("<header class='inventory-header'>");
        html.append("<a class='inventory-brand' href='" + request.getContextPath() + "/index.html'><img src='" + request.getContextPath() + "/logo.png' alt='' class='inventory-brand-icon'></a>");
        html.append("<nav class='inventory-nav' aria-label='Navegação principal'>");
        html.append("<a href='" + request.getContextPath() + "/index.html'>Início</a>");
        html.append("<a href='" + request.getContextPath() + "/jogos'>Jogos</a>");
        html.append("<a href='" + request.getContextPath() + "/biblioteca'>Biblioteca</a>");
        html.append("<a class='nav-current' href='" + request.getContextPath() + "/listas'>Listas</a>");
        html.append("<a href='" + request.getContextPath() + "/buscar-usuarios'>Buscar usuários</a>");
        html.append("<a href='" + request.getContextPath() + "/perfil'>Meu perfil</a>");
        html.append("<a class='inventory-exit' href='" + request.getContextPath() + "/logout'>Sair</a>");
        html.append("</nav>");
        html.append("</header>");

            // =====================================================
            // MAIN
            // =====================================================

            html.append(
                    "<main class='lists-page'>"
            );

            // =====================================================
            // CRIAR LISTA
            // =====================================================

            html.append(
                    "<section class='create-box'>"
            );

            html.append(
                    "<div class='create-title'>" +
                    "📚 Criar nova lista" +
                    "</div>"
            );

            html.append(
                    "<div class='create-text'>" +
                    "Crie coleções personalizadas com seus jogos favoritos." +
                    "</div>"
            );

            html.append(
                    "<form class='create-form' " +
                    "method='POST' " +
                    "action='criar-lista'>"
            );

            html.append(
                    "<input class='create-input' " +
                    "type='text' " +
                    "name='nome' " +
                    "placeholder='Nome da nova lista...' " +
                    "required>"
            );

            html.append(
                    "<button class='create-button' " +
                    "type='submit'>" +
                    "Criar lista" +
                    "</button>"
            );

            html.append("</form>");

            html.append("</section>");

            // =====================================================
            // LISTAS
            // =====================================================

            if (listas.isEmpty()) {

                html.append(
                        "<section class='list-box'>" +
                        "<div class='empty'>" +
                        "Você ainda não criou nenhuma lista." +
                        "</div>" +
                        "</section>"
                );

            } else {

                for (Lista lista :
                        listas) {

                    html.append(
                            "<section class='list-box'>"
                    );

                    html.append(
                            "<div class='list-head'>"
                    );

                    html.append(
                            "<div class='list-name'>" +
                            escaparHtml(lista.nome) +
                            "</div>"
                    );

                    html.append(
                            "<form method='POST' " +
                            "action='excluir-lista' " +
                            "onsubmit=\"return confirm('Excluir esta lista?');\">"
                    );

                    html.append(
                            "<input type='hidden' " +
                            "name='idLista' " +
                            "value='" +
                            lista.id +
                            "'>"
                    );

                    html.append(
                            "<button class='delete-button' " +
                            "type='submit'>" +
                            "Excluir" +
                            "</button>"
                    );

                    html.append("</form>");

                    html.append("</div>");

                    // =====================================================
                    // JOGOS
                    // =====================================================

                    if (lista.jogos.isEmpty()) {

                        html.append(
                                "<div class='empty'>" +
                                "Esta lista ainda está vazia." +
                                "</div>"
                        );

                    } else {

                        html.append(
                                "<div class='list-games'>"
                        );

                        for (Jogo jogo :
                                lista.jogos) {
                            String capa;
                            try {
                                capa = "capa?titulo=" +
                                        java.net.URLEncoder.encode(
                                                jogo.titulo == null ? "" : jogo.titulo,
                                                "UTF-8"
                                        );
                            } catch (Exception e) {
                                capa = "";
                            }

                            html.append(
                                    "<div class='list-game'>"
                            );

                            if (!capa.isEmpty()) {

                                html.append(
                                        "<img " +
                                        "class='list-cover' " +
                                        "src='" +
                                        escaparHtml(capa) +
                                        "' " +
                                        "alt='Capa de " +
                                        escaparHtml(jogo.titulo) +
                                        "' " +
                                        "onerror=\"" +
                                        "this.style.display='none';" +
                                        "this.nextElementSibling.style.display='flex';" +
                                        "\">"
                                );

                                html.append(
                                        "<div " +
                                        "class='list-cover-placeholder' " +
                                        "style='display:none;'>" +
                                        escaparHtml(jogo.titulo) +
                                        "</div>"
                                );

                            } else {

                                html.append(
                                        "<div " +
                                        "class='list-cover-placeholder'>" +
                                        escaparHtml(jogo.titulo) +
                                        "</div>"
                                );
                            }

                            html.append(
                                    "<div class='list-game-title'>" +
                                    escaparHtml(jogo.titulo) +
                                    "</div>"
                            );

                            html.append("</div>");
                        }

                        html.append("</div>");
                    }

                    // =====================================================
                    // ADICIONAR JOGO
                    // =====================================================

                    html.append(
                            "<form class='add-game-form' " +
                            "method='POST' " +
                            "action='adicionar-jogo-lista'>"
                    );

                    html.append(
                            "<input type='hidden' " +
                            "name='idLista' " +
                            "value='" +
                            lista.id +
                            "'>"
                    );

                    html.append(
                            "<select class='game-select' " +
                            "name='idJogo' required>"
                    );

                    html.append(
                            "<option value=''>" +
                            "Escolha um jogo..." +
                            "</option>"
                    );

                    for (Jogo jogo :
                            jogos) {

                        html.append(
                                "<option value='" +
                                jogo.id +
                                "'>" +
                                escaparHtml(jogo.titulo) +
                                "</option>"
                        );
                    }

                    html.append("</select>");

                    html.append(
                            "<button class='add-button' " +
                            "type='submit'>" +
                            "Adicionar jogo" +
                            "</button>"
                    );

                    html.append("</form>");

                    html.append("</section>");
                }
            }

            html.append("</main>");

            html.append("</body>");
            html.append("</html>");

            response.getWriter().println(
                    html.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect("index.html");
        }
    }

    // =====================================================
    // TODOS OS JOGOS
    // =====================================================

    private List<Jogo> carregarTodosJogos()
            throws Exception {

        List<Jogo> jogos =
                new ArrayList<Jogo>();

        Connection conexao =
                Conexao.conectar();

        if (conexao == null) {
            throw new Exception(
                    "Não foi possível conectar ao banco."
            );
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            try {
                stmt = conexao.prepareStatement(
                        "SELECT id,titulo,capa,steam_app_id " +
                        "FROM jogo ORDER BY titulo"
                );

                rs = stmt.executeQuery();

                while (rs.next()) {
                    Jogo jogo = new Jogo();
                    jogo.id = rs.getInt("id");
                    jogo.titulo = rs.getString("titulo");
                    jogo.capa = rs.getString("capa");
                    jogo.steamAppId = rs.getInt("steam_app_id");
                    jogos.add(jogo);
                }

            } catch (Exception colunaAusente) {

                if (rs != null) try { rs.close(); } catch (Exception ignored) {}
                if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}

                stmt = conexao.prepareStatement(
                        "SELECT id,titulo,capa FROM jogo ORDER BY titulo"
                );

                rs = stmt.executeQuery();

                while (rs.next()) {
                    Jogo jogo = new Jogo();
                    jogo.id = rs.getInt("id");
                    jogo.titulo = rs.getString("titulo");
                    jogo.capa = rs.getString("capa");
                    jogo.steamAppId = 0;
                    jogos.add(jogo);
                }
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
            conexao.close();
        }

        return jogos;
    }

    // =====================================================
    // LISTAS
    // =====================================================

    private List<Lista> carregarListas(
            int idUsuario)
            throws Exception {

        List<Lista> listas =
                new ArrayList<>();

        Connection conexao =
                Conexao.conectar();

        if (conexao == null) {
            throw new Exception(
                    "Não foi possível conectar ao banco."
            );
        }

        PreparedStatement stmt =
                conexao.prepareStatement(
                        "SELECT id,nome " +
                        "FROM lista " +
                        "WHERE id_usuario=? " +
                        "ORDER BY id DESC"
                );

        stmt.setInt(
                1,
                idUsuario
        );

        ResultSet rs =
                stmt.executeQuery();

        while (rs.next()) {

            Lista lista =
                    new Lista();

            lista.id =
                    rs.getInt("id");

            lista.nome =
                    rs.getString("nome");

            lista.jogos =
                    carregarJogosLista(
                            lista.id
                    );

            listas.add(lista);
        }

        rs.close();
        stmt.close();
        conexao.close();

        return listas;
    }

    // =====================================================
    // JOGOS DA LISTA
    // =====================================================

    private List<Jogo> carregarJogosLista(
            int idLista)
            throws Exception {

        List<Jogo> jogos =
                new ArrayList<Jogo>();

        Connection conexao =
                Conexao.conectar();

        if (conexao == null) {
            throw new Exception(
                    "Não foi possível conectar ao banco."
            );
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            // Padrão atual: lista_jogo guarda id_jogo.
            try {
                stmt = conexao.prepareStatement(
                        "SELECT j.id,j.titulo,j.capa " +
                        "FROM lista_jogo lj " +
                        "INNER JOIN jogo j ON j.id=lj.id_jogo " +
                        "WHERE lj.id_lista=? ORDER BY lj.id ASC"
                );

                stmt.setInt(1, idLista);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    Jogo jogo = new Jogo();
                    jogo.id = rs.getInt("id");
                    jogo.titulo = rs.getString("titulo");
                    jogo.capa = rs.getString("capa");
                    jogo.steamAppId = 0;
                    jogos.add(jogo);
                }

            } catch (Exception erroIdJogo) {
                if (rs != null) try { rs.close(); } catch (Exception ignored) {}
                if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
            }

            // Se o banco usa o padrão novo, tenta steam_app_id.
            if (jogos.isEmpty()) {

                if (rs != null) try { rs.close(); } catch (Exception ignored) {}
                if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}

                try {
                    stmt = conexao.prepareStatement(
                            "SELECT j.id,j.titulo,j.capa,j.steam_app_id " +
                            "FROM lista_jogo lj " +
                            "INNER JOIN jogo j ON j.steam_app_id=lj.steam_app_id " +
                            "WHERE lj.id_lista=? ORDER BY lj.id ASC"
                    );

                    stmt.setInt(1, idLista);
                    rs = stmt.executeQuery();

                    while (rs.next()) {
                        Jogo jogo = new Jogo();
                        jogo.id = rs.getInt("id");
                        jogo.titulo = rs.getString("titulo");
                        jogo.capa = rs.getString("capa");
                        jogo.steamAppId = rs.getInt("steam_app_id");
                        jogos.add(jogo);
                    }
                } catch (Exception ignorado) {
                    // Banco antigo: não há steam_app_id em uma das tabelas.
                }
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
            conexao.close();
        }

        return jogos;
    }

    // =====================================================
    // PREPARAR CAPA
    // =====================================================

    private String prepararCapa(
            String caminho,
            HttpServletRequest request) {

        if (caminho == null ||
                caminho.trim().isEmpty()) {

            return "";
        }

        caminho =
                caminho.trim();

        // =================================================
        // MARKDOWN
        // =================================================

        if (caminho.startsWith("[") &&
                caminho.contains("](") &&
                caminho.endsWith(")")) {

            int posicao =
                    caminho.indexOf("](");

            caminho =
                    caminho.substring(
                            posicao + 2,
                            caminho.length() - 1
                    );
        }

        // =================================================
        // URL COMPLETA
        // =================================================

        if (caminho.startsWith("http://") ||
                caminho.startsWith("https://")) {

            // Verifica se existe App ID na URL
            Pattern pattern =
                    Pattern.compile(
                            "/apps/(\\d+)"
                    );

            Matcher matcher =
                    pattern.matcher(
                            caminho
                    );

            if (matcher.find()) {

                String appId =
                        matcher.group(1);

                return "capa?appId=" + appId;
            }

            return caminho;
        }

        // =================================================
        // SOMENTE APP ID
        // =================================================

        if (caminho.matches("\\d+")) {
            return "capa?appId=" + caminho;
        }

        // =================================================
        // CAMINHO LOCAL
        // =================================================

        while (
                caminho.startsWith("/")
        ) {

            caminho =
                    caminho.substring(1);
        }

        return
                request.getContextPath() +
                "/" +
                caminho;
    }

    // =====================================================
    // ESCAPAR HTML
    // =====================================================

    private String escaparHtml(
            String texto) {

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

    // =====================================================
    // CLASSE JOGO
    // =====================================================

    private static class Jogo {

        int id;

        String titulo;

        String capa;

        int steamAppId;
    }

    // =====================================================
    // CLASSE LISTA
    // =====================================================

    private static class Lista {

        int id;

        String nome;

        List<Jogo> jogos =
                new ArrayList<>();
    }
}