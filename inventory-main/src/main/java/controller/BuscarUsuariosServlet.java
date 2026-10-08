package controller;

import dao.Conexao;
import model.Usuario;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/buscar-usuarios")
public class BuscarUsuariosServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect(
                    request.getContextPath() +
                    "/login.html"
            );

            return;
        }

        String busca =
                request.getParameter("busca");

        if (busca == null) {
            busca = "";
        }

        busca = busca.trim();

        if (busca.startsWith("@")) {
            busca = busca.substring(1).trim();
        }

        ArrayList<Usuario> usuarios =
                new ArrayList<Usuario>();

        String erro = "";

        if (!busca.isEmpty()) {

            Connection conexao = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {

                conexao =
                        Conexao.conectar();

                if (conexao == null) {

                    erro =
                            "Não foi possível conectar ao banco.";

                } else {

                    String sql =
                            "SELECT id, nome, username, foto, email " +
                            "FROM usuario " +
                            "WHERE " +
                            "LOWER(REPLACE(COALESCE(username,''),'@','')) LIKE LOWER(?) " +
                            "OR LOWER(COALESCE(nome,'')) LIKE LOWER(?) " +
                            "OR LOWER(COALESCE(email,'')) LIKE LOWER(?) " +
                            "ORDER BY nome COLLATE NOCASE ASC";

                    stmt =
                            conexao.prepareStatement(sql);

                    String termo =
                            "%" + busca + "%";

                    stmt.setString(1, termo);
                    stmt.setString(2, termo);
                    stmt.setString(3, termo);

                    rs =
                            stmt.executeQuery();

                    while (rs.next()) {

                        Usuario usuario =
                                new Usuario();

                        usuario.setId(
                                rs.getInt("id")
                        );

                        usuario.setNome(
                                rs.getString("nome")
                        );

                        usuario.setUsername(
                                rs.getString("username")
                        );

                        usuario.setFoto(
                                rs.getString("foto")
                        );

                        usuarios.add(usuario);
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();

                erro =
                        "Erro ao realizar a busca.";

            } finally {

                try {
                    if (rs != null) rs.close();
                } catch (Exception ignored) {}

                try {
                    if (stmt != null) stmt.close();
                } catch (Exception ignored) {}

                try {
                    if (conexao != null) conexao.close();
                } catch (Exception ignored) {}
            }
        }

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='pt-BR'>");

        html.append("<head>");

        html.append(
                "<meta charset='UTF-8'>"
        );

        html.append(
                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>"
        );

        html.append(
                "<title>Buscar usuários - Inventory</title>"
        );

        html.append(
                "<link rel='stylesheet' href='style.css'>"
        );

        html.append("<style>");

        html.append(
                "body{" +
                "margin:0;" +
                "background:#14101b;" +
                "color:white;" +
                "font-family:'Rajdhani',sans-serif;" +
                "}"
        );

        html.append(
                ".busca-usuarios{" +
                "max-width:950px;" +
                "margin:45px auto;" +
                "padding:20px;" +
                "}"
        );

        html.append(
                ".caixa-busca{" +
                "background:#181020;" +
                "border:1px solid #45245c;" +
                "border-radius:18px;" +
                "padding:30px;" +
                "}"
        );

        html.append(
                ".titulo-busca{" +
                "text-align:center;" +
                "font-size:32px;" +
                "color:#b66cff;" +
                "margin-bottom:8px;" +
                "}"
        );

        html.append(
                ".subtitulo-busca{" +
                "text-align:center;" +
                "color:#999;" +
                "margin-bottom:25px;" +
                "}"
        );

        html.append(
                ".form-busca{" +
                "display:flex;" +
                "gap:10px;" +
                "}"
        );

        html.append(
                ".campo-busca{" +
                "flex:1;" +
                "padding:14px;" +
                "background:#100b15;" +
                "border:1px solid #49315a;" +
                "border-radius:9px;" +
                "color:white;" +
                "font-size:16px;" +
                "outline:none;" +
                "}"
        );

        html.append(
                ".campo-busca:focus{" +
                "border-color:#8b5cf6;" +
                "}"
        );

        html.append(
                ".botao-busca{" +
                "padding:14px 25px;" +
                "border:0;" +
                "border-radius:9px;" +
                "background:#7c3aed;" +
                "color:white;" +
                "font-weight:bold;" +
                "cursor:pointer;" +
                "}"
        );

        html.append(
                ".botao-busca:hover{" +
                "background:#8b5cf6;" +
                "}"
        );

        html.append(
                ".resultados{" +
                "display:grid;" +
                "grid-template-columns:" +
                "repeat(auto-fill,minmax(220px,1fr));" +
                "gap:15px;" +
                "margin-top:25px;" +
                "}"
        );

        html.append(
                ".usuario-card{" +
                "display:block;" +
                "background:#160d1e;" +
                "border:1px solid #352044;" +
                "border-radius:14px;" +
                "padding:20px;" +
                "color:white;" +
                "text-decoration:none;" +
                "text-align:center;" +
                "transition:.2s;" +
                "}"
        );

        html.append(
                ".usuario-card:hover{" +
                "transform:translateY(-4px);" +
                "border-color:#8b5cf6;" +
                "}"
        );

        html.append(
                ".foto-usuario{" +
                "width:85px;" +
                "height:85px;" +
                "border-radius:50%;" +
                "object-fit:cover;" +
                "border:3px solid #7c3aed;" +
                "margin-bottom:10px;" +
                "}"
        );

        html.append(
                ".sem-foto{" +
                "width:85px;" +
                "height:85px;" +
                "border-radius:50%;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "background:#281833;" +
                "font-size:30px;" +
                "margin:0 auto 10px;" +
                "}"
        );

        html.append(
                ".listas-busca{margin-top:16px;text-align:left;border-top:1px solid #2b1b35;padding-top:13px;}"
        );
        html.append(
                ".listas-busca-titulo{font-size:12px;color:#c084fc;font-weight:600;margin-bottom:8px;}"
        );
        html.append(
                ".lista-busca{background:#110c16;border:1px solid #2b1b35;border-radius:9px;padding:8px;margin-top:7px;}"
        );
        html.append(
                ".lista-busca-nome{font-size:11px;color:#e9d5ff;font-weight:600;margin-bottom:6px;}"
        );
        html.append(
                ".lista-busca-capas{display:flex;gap:5px;overflow:hidden;}"
        );
        html.append(
                ".lista-busca-capas img{width:34px;height:48px;object-fit:cover;border-radius:4px;background:#21162a;}"
        );

        html.append(
                ".username{" +
                "color:#b98be8;" +
                "margin-top:5px;" +
                "}"
        );

        html.append(
                ".nenhum{" +
                "margin-top:25px;" +
                "padding:25px;" +
                "text-align:center;" +
                "background:#160d1e;" +
                "border:1px dashed #493254;" +
                "border-radius:13px;" +
                "color:#999;" +
                "}"
        );

        html.append(
                ".erro{" +
                "margin-top:25px;" +
                "padding:20px;" +
                "text-align:center;" +
                "background:#2a1018;" +
                "border:1px solid #8b2746;" +
                "border-radius:13px;" +
                "color:#ff7c9e;" +
                "}"
        );

        html.append(
                "@media(max-width:600px){" +
                ".form-busca{" +
                "flex-direction:column;" +
                "}" +
                ".botao-busca{" +
                "width:100%;" +
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

        html.append(".usuario-card{position:relative;text-align:left !important;}");
        html.append(".usuario-card-link{display:block;color:white;text-decoration:none;text-align:center;}");
        html.append(".usuario-card-link:hover{color:white;text-decoration:none;}");
        html.append(".resultado-acoes{margin-top:12px;}");
        html.append(".btn-seguir-busca{width:100%;padding:9px 12px;border:1px solid #63358a;border-radius:9px;background:#21142d;color:#e9d5ff;font-family:'Rajdhani',sans-serif;font-size:11px;font-weight:600;cursor:pointer;transition:.2s;}");
        html.append(".btn-seguir-busca:hover{background:#3a2050;color:#fff;border-color:#a855f7;}");
        html.append(".btn-seguir-busca.seguindo{background:#17251c;border-color:#2e6841;color:#a7f3c0;}");

        html.append(".mini-rede{margin-top:14px;padding-top:12px;border-top:1px solid #2b1b35;text-align:left;}");
        html.append(".mini-rede-contagem{display:flex;gap:16px;color:#8f8496;font-size:10px;margin-bottom:9px;}");
        html.append(".mini-rede-contagem strong{color:#c084fc;font-size:13px;}");
        html.append(".mini-rede-grupos{display:grid;grid-template-columns:1fr 1fr;gap:9px;}");
        html.append(".mini-grupo{background:#110c16;border:1px solid #2b1b35;border-radius:9px;padding:7px;min-width:0;overflow:hidden;}");
        html.append(".mini-grupo-titulo{display:block;color:#bca5c9;font-size:9px;margin-bottom:6px;font-weight:600;}");
        html.append(".mini-fotos{display:flex;align-items:center;gap:2px;overflow:hidden;}");
        html.append(".mini-fotos a{margin-right:-5px;flex:0 0 auto;}");
        html.append(".mini-foto{width:25px;height:25px;min-width:25px;max-width:25px;max-height:25px;box-sizing:border-box;border-radius:50%;object-fit:cover;border:2px solid #110c16;background:#241633;display:flex;align-items:center;justify-content:center;color:#c084fc;font-size:9px;font-weight:700;margin:0;}");
        html.append(".rede-sem-foto{display:flex;align-items:center;justify-content:center;background:#241633;color:#c084fc;font-weight:700;}");
        html.append(".mini-sem{color:#5f5665;font-size:12px;}");

        html.append("</style>");
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

        html.append(
                "<main class='busca-usuarios'>"
        );

        html.append(
                "<div class='caixa-busca'>"
        );

        html.append(
                "<h2 class='titulo-busca'>" +
                "🔎 Buscar usuários" +
                "</h2>"
        );

        html.append(
                "<p class='subtitulo-busca'>" +
                "Encontre outros jogadores do Inventory." +
                "</p>"
        );

        html.append(
                "<form class='form-busca' " +
                "method='GET' " +
                "action='" +
                request.getContextPath() +
                "/buscar-usuarios'>"
        );

        html.append(
                "<input class='campo-busca' " +
                "type='text' " +
                "name='busca' " +
                "value='" +
                escapar(busca) +
                "' " +
                "placeholder='Nome, @username ou e-mail'>"
        );

        html.append(
                "<button class='botao-busca' " +
                "type='submit'>Buscar</button>"
        );

        html.append("</form>");

        if (!erro.isEmpty()) {

            html.append(
                    "<div class='erro'>" +
                    escapar(erro) +
                    "</div>"
            );
        }

        if (!busca.isEmpty() &&
                erro.isEmpty()) {

            if (usuarios.isEmpty()) {

                html.append(
                        "<div class='nenhum'>" +
                        "Nenhum usuário encontrado." +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='resultados'>"
                );

                for (Usuario usuario :
                        usuarios) {

                    html.append("<div class='usuario-card'>");

                    html.append(
                            "<a class='usuario-card-link' " +
                            "href='" +
                            request.getContextPath() +
                            "/perfil-usuario?id=" +
                            usuario.getId() +
                            "'>"
                    );

                    String foto =
                            usuario.getFoto();

                    if (foto != null &&
                            !foto.trim().isEmpty()) {

                        String caminho = foto.trim();

                        if (caminho.startsWith("http://") ||
                                caminho.startsWith("https://")) {
                            caminho =
                                    request.getContextPath() +
                                    "/foto-perfil?url=" +
                                    URLEncoder.encode(caminho, "UTF-8");
                        } else {
                            while (caminho.startsWith("/")) {
                                caminho = caminho.substring(1);
                            }
                            caminho =
                                    request.getContextPath() +
                                    "/foto-perfil?arquivo=" +
                                    URLEncoder.encode(caminho, "UTF-8");
                        }

                        html.append(
                                "<img class='foto-usuario' " +
                                "src='" +
                                escapar(caminho) +
                                "' " +
                                "alt='Foto'>"
                        );

                    } else {

                        html.append(
                                "<div class='sem-foto'>" +
                                "👤" +
                                "</div>"
                        );
                    }

                    html.append(
                            "<h3>" +
                            escapar(
                                    usuario.getNome()
                            ) +
                            "</h3>"
                    );

                    String username =
                            usuario.getUsername();

                    if (username == null ||
                            username.trim().isEmpty()) {

                        username =
                                "usuario" +
                                usuario.getId();
                    }

                    html.append(
                            "<div class='username'>" +
                            "@" +
                            escapar(username) +
                            "</div>"
                    );

                    List<ListaBusca> listasUsuario =
                            buscarListasUsuario(
                                    usuario.getId()
                            );

                    if (!listasUsuario.isEmpty()) {

                        html.append(
                                "<div class='listas-busca'>" +
                                "<div class='listas-busca-titulo'>📚 Listas</div>"
                        );

                        int limiteListas =
                                Math.min(listasUsuario.size(), 2);

                        for (int i = 0; i < limiteListas; i++) {

                            ListaBusca lista =
                                    listasUsuario.get(i);

                            html.append(
                                    "<div class='lista-busca'>" +
                                    "<div class='lista-busca-nome'>" +
                                    escapar(lista.nome) +
                                    " · " +
                                    lista.jogos.size() +
                                    (lista.jogos.size() == 1 ? " jogo" : " jogos") +
                                    "</div>"
                            );

                            if (!lista.jogos.isEmpty()) {

                                html.append("<div class='lista-busca-capas'>");

                                int limiteJogos =
                                        Math.min(lista.jogos.size(), 5);

                                for (int j = 0; j < limiteJogos; j++) {

                                    JogoBusca jogo =
                                            lista.jogos.get(j);

                                    String capa =
                                            capaBusca(jogo);

                                    html.append(
                                            "<img src='" +
                                            escapar(capa) +
                                            "' alt='" +
                                            escapar(jogo.titulo) +
                                            "' title='" +
                                            escapar(jogo.titulo) +
                                            "'>"
                                    );
                                }

                                html.append("</div>");
                            }

                            html.append("</div>");
                        }

                        html.append("</div>");
                    }

                    html.append("</a>");

                    boolean segue =
                            estaSeguindo(
                                    ((Usuario) sessao.getAttribute("usuario")).getId(),
                                    usuario.getId()
                            );

                    if (((Usuario) sessao.getAttribute("usuario")).getId() != usuario.getId()) {
                        html.append("<div class='resultado-acoes'>");
                        html.append("<form method='POST' action='");
                        html.append(request.getContextPath());
                        html.append("/seguir'>");
                        html.append("<input type='hidden' name='idUsuario' value='");
                        html.append(usuario.getId());
                        html.append("'>");
                        html.append("<input type='hidden' name='acao' value='");
                        html.append(segue ? "deixar" : "seguir");
                        html.append("'>");
                        html.append("<button class='btn-seguir-busca");
                        if (segue) {
                            html.append(" seguindo");
                        }
                        html.append("' type='submit'>");
                        html.append(segue ? "✓ Seguindo" : "+ Seguir");
                        html.append("</button>");
                        html.append("</form>");
                        html.append("</div>");
                    }

                    html.append(
                            RedeSocialUtil.renderMiniRede(
                                    usuario.getId(),
                                    request.getContextPath()
                            )
                    );

                    html.append("</div>");
                }

                html.append("</div>");
            }
        }

        html.append("</div>");
        html.append("</main>");
        html.append("</body>");
        html.append("</html>");

        response.getWriter().println(
                html.toString()
        );
    }

    private boolean estaSeguindo(
            int idSeguidor,
            int idSeguido) {

        if (idSeguidor == idSeguido) {
            return false;
        }

        String sql =
                "SELECT id FROM seguidor " +
                "WHERE id_seguidor = ? " +
                "AND id_seguido = ? " +
                "LIMIT 1";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            conn = Conexao.conectar();

            if (conn == null) {
                return false;
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, idSeguidor);
            ps.setInt(2, idSeguido);

            rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        } finally {

            try {
                if (rs != null) rs.close();
            } catch (Exception ignored) {}

            try {
                if (ps != null) ps.close();
            } catch (Exception ignored) {}

            try {
                if (conn != null) conn.close();
            } catch (Exception ignored) {}
        }
    }

    private List<ListaBusca> buscarListasUsuario(
            int idUsuario) {

        List<ListaBusca> listas =
                new ArrayList<ListaBusca>();

        String sql =
                "SELECT id, nome FROM lista " +
                "WHERE id_usuario = ? " +
                "ORDER BY data_criacao DESC LIMIT 3";

        try (
                Connection conn = Conexao.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    ListaBusca lista =
                            new ListaBusca(
                                    rs.getString("nome")
                            );

                    lista.jogos =
                            buscarJogosLista(
                                    rs.getInt("id")
                            );

                    listas.add(lista);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return listas;
    }

    private List<JogoBusca> buscarJogosLista(
            int idLista) {

        List<JogoBusca> jogos =
                new ArrayList<JogoBusca>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();

            try {
                stmt = conn.prepareStatement(
                        "SELECT j.titulo, j.capa " +
                        "FROM lista_jogo lj " +
                        "INNER JOIN jogo j ON j.id = lj.id_jogo " +
                        "WHERE lj.id_lista = ? ORDER BY lj.id ASC"
                );
                stmt.setInt(1, idLista);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    jogos.add(new JogoBusca(
                            rs.getString("titulo"),
                            0,
                            rs.getString("capa")
                    ));
                }
            } catch (Exception erroIdJogo) {
                if (rs != null) try { rs.close(); } catch (Exception ignored) {}
                if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
                jogos.clear();

                stmt = conn.prepareStatement(
                        "SELECT j.titulo, j.capa, j.steam_app_id " +
                        "FROM lista_jogo lj " +
                        "INNER JOIN jogo j ON j.steam_app_id = lj.steam_app_id " +
                        "WHERE lj.id_lista = ? ORDER BY lj.id ASC"
                );
                stmt.setInt(1, idLista);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    jogos.add(new JogoBusca(
                            rs.getString("titulo"),
                            rs.getInt("steam_app_id"),
                            rs.getString("capa")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
            if (conn != null) try { conn.close(); } catch (Exception ignored) {}
        }

        return jogos;
    }

    private String capaBusca(JogoBusca jogo) {

        if (jogo.titulo != null && !jogo.titulo.trim().isEmpty()) {
            try {
                return "capa?titulo=" +
                        URLEncoder.encode(jogo.titulo, "UTF-8");
            } catch (Exception e) {
                return "";
            }
        }

        if (jogo.appId > 0) {
            return "capa?appId=" + jogo.appId;
        }

        return "";
    }

    private static class ListaBusca {

        String nome;

        List<JogoBusca> jogos =
                new ArrayList<JogoBusca>();

        ListaBusca(String nome) {
            this.nome = nome;
        }
    }

    private static class JogoBusca {

        String titulo;
        int appId;
        String capa;

        JogoBusca(
                String titulo,
                int appId,
                String capa) {

            this.titulo = titulo;
            this.appId = appId;
            this.capa = capa;
        }
    }

    private String escapar(String texto) {

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