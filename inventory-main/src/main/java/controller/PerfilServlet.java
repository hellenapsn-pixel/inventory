package controller;

import dao.Conexao;
import dao.UsuarioDAO;
import model.Usuario;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

@WebServlet("/perfil")
public class PerfilServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("usuario") == null) {

            response.sendRedirect("login.html");
            return;
        }

        Usuario usuarioSessao =
                (Usuario) session.getAttribute("usuario");

        UsuarioDAO usuarioDAO =
                new UsuarioDAO();

        /*
         * Busca novamente no banco.
         * Assim a foto e os dados atualizados
         * aparecem também no próprio perfil.
         */
        Usuario usuario =
                usuarioDAO.buscarPorId(
                        usuarioSessao.getId()
                );

        if (usuario == null) {
            response.sendRedirect("login.html");
            return;
        }

        int seguidores =
                usuarioDAO.contarSeguidores(
                        usuario.getId()
                );

        int seguindo =
                usuarioDAO.contarSeguindo(
                        usuario.getId()
                );

        List<JogoInfo> favoritos =
                buscarFavoritos(
                        usuario.getId()
                );

        List<JogoInfo> avaliacoes =
                buscarAvaliacoes(
                        usuario.getId()
                );

        List<ListaInfo> listas =
                buscarListas(
                        usuario.getId()
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
                "<title>" +
                escaparHtml(usuario.getNome()) +
                " - Inventory</title>"
        );

        html.append(
                "<link rel='preconnect' " +
                "href='https://fonts.googleapis.com'>"
        );

        html.append(
                "<link rel='preconnect' " +
                "href='https://fonts.gstatic.com' " +
                "crossorigin>"
        );

        html.append(
                "<link href='https://fonts.googleapis.com/css2?" +
                "family=Rajdhani:wght@400;500;600;700&display=swap' " +
                "rel='stylesheet'>"
        );

        /* =====================================================
           CSS
           ===================================================== */

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
                "background:#09090b;" +
                "color:#f4f4f5;" +
                "min-height:100vh;" +
                "}"
        );

        html.append(
                "body:before{" +
                "content:'';" +
                "position:fixed;" +
                "top:-180px;" +
                "left:50%;" +
                "transform:translateX(-50%);" +
                "width:700px;" +
                "height:450px;" +
                "background:radial-gradient(circle,#7c3aed 0%,transparent 68%);" +
                "opacity:.16;" +
                "pointer-events:none;" +
                "}"
        );

        /* HEADER */

        html.append(
                "header{" +
                "height:68px;" +
                "background:rgba(9,9,11,.94);" +
                "border-bottom:1px solid #27272a;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:space-between;" +
                "padding:0 7%;" +
                "position:sticky;" +
                "top:0;" +
                "z-index:20;" +
                "backdrop-filter:blur(15px);" +
                "}"
        );

        html.append(
                ".logo{" +
                "color:#fff;" +
                "font-size:24px;" +
                "font-weight:800;" +
                "letter-spacing:-1px;" +
                "text-decoration:none;" +
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
                "gap:26px;" +
                "}"
        );

        html.append(
                "nav a{" +
                "color:#a1a1aa;" +
                "text-decoration:none;" +
                "font-size:12px;" +
                "font-weight:500;" +
                "transition:.2s;" +
                "}"
        );

        html.append(
                "nav a:hover{" +
                "color:#c084fc;" +
                "}"
        );

        /* CONTAINER */

        html.append(
                ".container{" +
                "position:relative;" +
                "width:90%;" +
                "max-width:1150px;" +
                "margin:40px auto 80px;" +
                "}"
        );

        /* PERFIL */

        html.append(
                ".perfil{" +
                "background:#0f0b14;" +
                "border:1px solid #29292d;" +
                "border-radius:24px;" +
                "padding:30px;" +
                "display:grid;" +
                "grid-template-columns:130px 1fr auto;" +
                "align-items:center;" +
                "gap:28px;" +
                "box-shadow:0 25px 70px rgba(0,0,0,.35);" +
                "}"
        );

        html.append(
                ".foto{" +
                "width:130px;" +
                "height:130px;" +
                "border-radius:50%;" +
                "object-fit:cover;" +
                "display:block;" +
                "background:#18181b;" +
                "border:3px solid #8b5cf6;" +
                "box-shadow:0 0 0 6px rgba(139,92,246,.08);" +
                "}"
        );

        html.append(
                ".sem-foto{" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "font-size:45px;" +
                "font-weight:800;" +
                "color:#c084fc;" +
                "}"
        );

        html.append(
                ".dados{" +
                "min-width:0;" +
                "}"
        );

        html.append(
                ".dados h1{" +
                "font-size:29px;" +
                "font-weight:800;" +
                "letter-spacing:-.7px;" +
                "margin-bottom:4px;" +
                "}"
        );

        html.append(
                ".username{" +
                "color:#a78bfa;" +
                "font-size:12px;" +
                "margin-bottom:11px;" +
                "}"
        );

        html.append(
                ".bio{" +
                "color:#a1a1aa;" +
                "font-size:12px;" +
                "line-height:1.6;" +
                "max-width:650px;" +
                "}"
        );

        /* ESTATÍSTICAS */

        html.append(
                ".stats{" +
                "display:flex;" +
                "gap:25px;" +
                "margin-top:19px;" +
                "}"
        );

        html.append(
                ".stat strong{" +
                "display:block;" +
                "font-size:18px;" +
                "font-weight:700;" +
                "}"
        );

        html.append(
                ".stat span{" +
                "display:block;" +
                "font-size:9px;" +
                "text-transform:uppercase;" +
                "letter-spacing:.6px;" +
                "color:#71717a;" +
                "margin-top:2px;" +
                "}"
        );

        /* BOTÕES */

        html.append(
                ".acoes{" +
                "display:flex;" +
                "flex-direction:column;" +
                "gap:9px;" +
                "min-width:135px;" +
                "}"
        );

        html.append(
                ".btn{" +
                "display:block;" +
                "width:100%;" +
                "padding:10px 16px;" +
                "border-radius:9px;" +
                "font-size:11px;" +
                "font-weight:700;" +
                "text-decoration:none;" +
                "text-align:center;" +
                "transition:.2s;" +
                "}"
        );

        html.append(
                ".editar{" +
                "background:#8b5cf6;" +
                "color:#fff;" +
                "}"
        );

        html.append(
                ".editar:hover{" +
                "background:#a855f7;" +
                "transform:translateY(-2px);" +
                "}"
        );

        html.append(
                ".sair{" +
                "background:#18181b;" +
                "border:1px solid #3f3f46;" +
                "color:#a1a1aa;" +
                "}"
        );

        html.append(
                ".sair:hover{" +
                "border-color:#71717a;" +
                "color:#fff;" +
                "}"
        );

        /* SEÇÕES */

        html.append(
                ".secao{" +
                "margin-top:45px;" +
                "}"
        );

        html.append(
                ".topo{" +
                "display:flex;" +
                "justify-content:space-between;" +
                "align-items:center;" +
                "margin-bottom:17px;" +
                "}"
        );

        html.append(
                ".topo h2{" +
                "font-size:19px;" +
                "font-weight:700;" +
                "}"
        );

        html.append(
                ".contador{" +
                "font-size:10px;" +
                "color:#a1a1aa;" +
                "background:#18181b;" +
                "border:1px solid #2a2a2e;" +
                "border-radius:20px;" +
                "padding:5px 10px;" +
                "}"
        );

        /* JOGOS */

        html.append(
                ".grade{" +
                "display:grid;" +
                "grid-template-columns:repeat(auto-fill,minmax(170px,1fr));" +
                "gap:16px;" +
                "}"
        );

        html.append(
                ".jogo{" +
                "background:#0f0b14;" +
                "border:1px solid #27272a;" +
                "border-radius:13px;" +
                "overflow:hidden;" +
                "transition:.22s;" +
                "}"
        );

        html.append(
                ".jogo:hover{" +
                "transform:translateY(-5px);" +
                "border-color:#6d28d9;" +
                "box-shadow:0 15px 35px rgba(0,0,0,.35);" +
                "}"
        );

        html.append(
                ".capa{" +
                "height:235px;" +
                "background:#09090b;" +
                "}"
        );

        html.append(
                ".capa img{" +
                "width:100%;" +
                "height:100%;" +
                "object-fit:cover;" +
                "display:block;" +
                "}"
        );

        html.append(
                ".info{" +
                "padding:13px;" +
                "}"
        );

        html.append(
                ".info h3{" +
                "font-size:12px;" +
                "line-height:1.4;" +
                "min-height:34px;" +
                "}"
        );

        html.append(
                ".tipo{" +
                "font-size:9px;" +
                "color:#71717a;" +
                "margin-top:6px;" +
                "}"
        );

        html.append(
                ".nota{" +
                "font-size:11px;" +
                "font-weight:700;" +
                "color:#fbbf24;" +
                "margin-top:6px;" +
                "}"
        );

        html.append(
                ".comentario{" +
                "font-size:10px;" +
                "line-height:1.5;" +
                "color:#a1a1aa;" +
                "margin-top:7px;" +
                "display:-webkit-box;" +
                "-webkit-line-clamp:3;" +
                "-webkit-box-orient:vertical;" +
                "overflow:hidden;" +
                "}"
        );

        /* LISTAS */

        html.append(
                ".lista{" +
                "background:#0f0b14;" +
                "border:1px solid #29292d;" +
                "border-radius:14px;" +
                "padding:19px;" +
                "margin-bottom:11px;" +
                "display:flex;" +
                "align-items:center;" +
                "gap:15px;" +
                "transition:.2s;" +
                "}"
        );

        html.append(
                ".lista:hover{" +
                "border-color:#6d28d9;" +
                "transform:translateX(3px);" +
                "}"
        );

        html.append(
                ".lista-icone{" +
                "width:42px;" +
                "height:42px;" +
                "border-radius:10px;" +
                "background:#25143a;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "font-size:18px;" +
                "flex-shrink:0;" +
                "}"
        );

        html.append(
                ".lista-info h3{" +
                "font-size:13px;" +
                "margin-bottom:4px;" +
                "}"
        );

        html.append(
                ".lista-info p{" +
                "font-size:10px;" +
                "color:#71717a;" +
                "}"
        );

        /* VAZIO */

        html.append(
                ".vazio{" +
                "padding:30px;" +
                "border:1px dashed #3f3f46;" +
                "border-radius:13px;" +
                "text-align:center;" +
                "color:#71717a;" +
                "font-size:11px;" +
                "}"
        );

        /* RESPONSIVO */

        html.append(
                "@media(max-width:750px){"
        );

        html.append(
                "header{padding:0 20px;}"
        );

        html.append(
                "nav{gap:12px;}"
        );

        html.append(
                "nav a:nth-child(2)," +
                "nav a:nth-child(3){" +
                "display:none;" +
                "}"
        );

        html.append(
                ".container{width:94%;margin-top:25px;}"
        );

        html.append(
                ".perfil{" +
                "grid-template-columns:1fr;" +
                "text-align:center;" +
                "justify-items:center;" +
                "padding:25px 18px;" +
                "}"
        );

        html.append(
                ".dados{width:100%;}"
        );

        html.append(
                ".bio{margin:auto;}"
        );

        html.append(
                ".stats{justify-content:center;gap:18px;}"
        );

        html.append(
                ".acoes{width:100%;}"
        );

        html.append(
                ".grade{" +
                "grid-template-columns:repeat(2,1fr);" +
                "gap:11px;" +
                "}"
        );

        html.append(
                ".capa{height:205px;}"
        );

        html.append("}");


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

        html.append(".rede-social{margin-top:34px;background:#120e18;border:1px solid #30263a;border-radius:18px;padding:24px;}");
        html.append(".rede-titulo{margin-bottom:18px;}");
        html.append(".rede-titulo h2{margin:0;color:#fff;font-size:20px;font-weight:700;}");
        html.append(".rede-titulo p{margin:5px 0 0;color:#8f8797;font-size:12px;}");
        html.append(".rede-grid{display:grid;grid-template-columns:1fr 1fr;gap:18px;}");
        html.append(".rede-coluna{background:#0f0b14;border:1px solid #2b2034;border-radius:14px;padding:16px;}");
        html.append(".rede-coluna-titulo{display:flex;align-items:center;justify-content:space-between;margin-bottom:12px;}");
        html.append(".rede-coluna-titulo h3{margin:0;color:#e9d5ff;font-size:15px;font-weight:700;}");
        html.append(".rede-coluna-titulo span{font-size:11px;color:#c084fc;background:#21142d;border:1px solid #4b2d61;border-radius:20px;padding:4px 9px;}");
        html.append(".rede-pessoas{display:flex;flex-direction:column;gap:8px;max-height:330px;overflow:auto;}");
        html.append(".rede-pessoa{display:flex;align-items:center;gap:10px;padding:9px;border-radius:10px;background:#110c16;border:1px solid #2b1b35;text-decoration:none;color:#fff;transition:.2s;}");
        html.append(".rede-pessoa:hover{border-color:#7c3aed;background:#21162b;transform:translateX(2px);}");
        html.append(".rede-foto{width:40px;height:40px;min-width:40px;border-radius:50%;object-fit:cover;border:1px solid #7543a0;background:#241633;display:flex;align-items:center;justify-content:center;color:#c084fc;font-weight:700;}");
        html.append(".rede-pessoa-dados{display:flex;flex-direction:column;min-width:0;flex:1;}");
        html.append(".rede-pessoa-dados strong{font-size:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}");
        html.append(".rede-pessoa-dados span{font-size:10px;color:#8f8496;margin-top:2px;}");
        html.append(".rede-seta{color:#8b5cf6;font-size:22px;line-height:1;}");
        html.append(".rede-vazio{padding:20px 10px;text-align:center;color:#756b7d;font-size:11px;}");
        html.append(".mini-rede{margin-top:14px;padding-top:12px;border-top:1px solid #2b1b35;text-align:left;}");
        html.append(".mini-rede-contagem{display:flex;gap:16px;color:#8f8496;font-size:10px;margin-bottom:9px;}");
        html.append(".mini-rede-contagem strong{color:#c084fc;font-size:13px;}");
        html.append(".mini-rede-grupos{display:grid;grid-template-columns:1fr 1fr;gap:9px;}");
        html.append(".mini-grupo{background:#110c16;border:1px solid #2b1b35;border-radius:9px;padding:7px;}");
        html.append(".mini-grupo-titulo{display:block;color:#bca5c9;font-size:9px;margin-bottom:6px;font-weight:600;}");
        html.append(".mini-fotos{display:flex;align-items:center;}");
        html.append(".mini-fotos a{margin-right:-5px;}");
        html.append(".mini-foto{width:25px;height:25px;border-radius:50%;object-fit:cover;border:2px solid #110c16;background:#241633;display:flex;align-items:center;justify-content:center;color:#c084fc;font-size:9px;font-weight:700;}");
        html.append(".mini-sem{color:#5f5665;font-size:12px;}");
        html.append("@media(max-width:700px){.rede-grid{grid-template-columns:1fr;}.mini-rede-grupos{grid-template-columns:1fr 1fr;}}");


        html.append(".lista{background:#120e18 !important;border:1px solid #30203d !important;border-radius:16px !important;padding:18px !important;margin-bottom:18px !important;box-shadow:0 8px 25px rgba(0,0,0,.25) !important;}");
        html.append(".lista:hover{border-color:#7c3aed !important;}");
        html.append(".lista-icone{width:42px;height:42px;border-radius:12px;background:linear-gradient(135deg,#7c3aed,#4c1d95);display:flex;align-items:center;justify-content:center;font-size:20px;flex-shrink:0;}");
        html.append(".lista-info h3{color:#fff !important;margin:0;font-size:17px;font-weight:700;}");
        html.append(".lista-info p{color:#9f91aa !important;margin:4px 0 0;font-size:12px;}");
        html.append(".lista-jogos-perfil{display:grid;grid-template-columns:repeat(5,1fr);gap:12px;margin-top:14px;}");
        html.append(".lista-jogo-perfil{background:#0f0b14;border:1px solid #2b1b35;border-radius:12px;overflow:hidden;transition:.2s;}");
        html.append(".lista-jogo-perfil:hover{border-color:#8b5cf6;transform:translateY(-3px);}");
        html.append(".lista-jogo-capa{width:100%;height:190px;object-fit:cover;display:block;background:linear-gradient(135deg,#241633,#120e18);}");
        html.append(".lista-jogo-nome{padding:9px;color:#eee;font-size:11px;font-weight:600;line-height:1.3;min-height:42px;}");
        html.append(".lista-jogo-sem-capa{width:100%;height:190px;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#241633,#120e18);color:#8b5cf6;font-size:28px;}");
        html.append("@media(max-width:900px){.lista-jogos-perfil{grid-template-columns:repeat(4,1fr);}}");
        html.append("@media(max-width:700px){.lista-jogos-perfil{grid-template-columns:repeat(3,1fr);}.lista-jogo-capa,.lista-jogo-sem-capa{height:160px;}}");
        html.append("@media(max-width:500px){.lista-jogos-perfil{grid-template-columns:repeat(2,1fr);}.lista-jogo-capa,.lista-jogo-sem-capa{height:190px;}}");

                // Mantém as capas proporcionais e evita que avaliações com textos longos deformem a grade.
        html.append(".grade{align-items:start;grid-template-columns:repeat(auto-fill,minmax(min(100%,170px),1fr));}");
        html.append(".jogo{min-width:0;align-self:start;}");
        html.append(".capa{height:235px;overflow:hidden;background:#09090b;}");
        html.append(".capa img{width:100%;height:100%;object-fit:contain;background:#09090b;}");
        html.append(".comentario{overflow-wrap:anywhere;word-break:normal;}");
        html.append("@media(max-width:500px){.grade{grid-template-columns:repeat(2,minmax(0,1fr));}.capa{height:205px;}}");

        html.append("</style>");

        html.append("</head>");

        /* =====================================================
           BODY
           ===================================================== */

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

        html.append("<main class='container'>");

        /* PERFIL */

        html.append("<section class='perfil'>");

        /*
         * FOTO DO PRÓPRIO USUÁRIO
         */

        String foto =
                usuario.getFoto();

        if (foto != null &&
                !foto.trim().isEmpty()) {

            html.append(
                    "<img " +
                    "class='foto' " +
                    "src='" +
                    escaparHtml(
                            foto.startsWith("http://") || foto.startsWith("https://")
                                    ? foto
                                    : request.getContextPath() + "/foto-perfil?arquivo=" + foto
                    ) +
                    "' " +
                    "alt='Minha foto de perfil'>"
            );

        } else {

            html.append(
                    "<div class='foto sem-foto'>" +
                    primeiraLetra(
                            usuario.getNome()
                    ) +
                    "</div>"
            );
        }

        html.append("<div class='dados'>");

        html.append(
                "<h1>" +
                escaparHtml(
                        usuario.getNome()
                ) +
                "</h1>"
        );

        String username =
                usuario.getUsername();

        if (username != null &&
                !username.trim().isEmpty()) {

            if (!username.startsWith("@")) {
                username =
                        "@" + username;
            }

            html.append(
                    "<div class='username'>" +
                    escaparHtml(username) +
                    "</div>"
            );
        }

        String bio =
                usuario.getBio();

        if (bio != null &&
                !bio.trim().isEmpty()) {

            html.append(
                    "<div class='bio'>" +
                    escaparHtml(bio) +
                    "</div>"
            );
        }

        html.append("<div class='stats'>");

        html.append(
                "<div class='stat'>" +
                "<strong>" +
                seguidores +
                "</strong>" +
                "<span>Seguidores</span>" +
                "</div>"
        );

        html.append(
                "<div class='stat'>" +
                "<strong>" +
                seguindo +
                "</strong>" +
                "<span>Seguindo</span>" +
                "</div>"
        );

        html.append(
                "<div class='stat'>" +
                "<strong>" +
                favoritos.size() +
                "</strong>" +
                "<span>Favoritos</span>" +
                "</div>"
        );

        html.append(
                "<div class='stat'>" +
                "<strong>" +
                avaliacoes.size() +
                "</strong>" +
                "<span>Avaliações</span>" +
                "</div>"
        );

        html.append("</div>");

        html.append("</div>");

        /* BOTÕES */

        html.append("<div class='acoes'>");

        html.append(
                "<a href='editar-perfil' " +
                "class='btn editar'>" +
                "Editar perfil" +
                "</a>"
        );

        html.append(
                "<a href='logout' " +
                "class='btn sair'>" +
                "Sair da conta" +
                "</a>"
        );

        html.append("</div>");

        html.append("</section>");

        html.append(
                RedeSocialUtil.renderRede(
                        usuario.getId(),
                        request.getContextPath()
                )
        );

        /* FAVORITOS */

        html.append("<section class='secao'>");

        html.append("<div class='topo'>");

        html.append(
                "<h2>⭐ Favoritos</h2>"
        );

        html.append(
                "<span class='contador'>" +
                favoritos.size() +
                " jogos</span>"
        );

        html.append("</div>");

        if (favoritos.isEmpty()) {

            html.append(
                    "<div class='vazio'>" +
                    "Você ainda não possui jogos favoritos." +
                    "</div>"
            );

        } else {

            html.append("<div class='grade'>");

            for (JogoInfo jogo :
                    favoritos) {

                html.append(
                        montarCardFavorito(
                                jogo
                        )
                );
            }

            html.append("</div>");
        }

        html.append("</section>");

        /* AVALIAÇÕES */

        html.append("<section class='secao'>");

        html.append("<div class='topo'>");

        html.append(
                "<h2>📝 Avaliações</h2>"
        );

        html.append(
                "<span class='contador'>" +
                avaliacoes.size() +
                " avaliações</span>"
        );

        html.append("</div>");

        if (avaliacoes.isEmpty()) {

            html.append(
                    "<div class='vazio'>" +
                    "Você ainda não avaliou nenhum jogo." +
                    "</div>"
            );

        } else {

            html.append("<div class='grade'>");

            for (JogoInfo jogo :
                    avaliacoes) {

                html.append(
                        montarCardAvaliacao(
                                jogo
                        )
                );
            }

            html.append("</div>");
        }

        html.append("</section>");

        /* LISTAS */

        html.append("<section class='secao'>");

        html.append("<div class='topo'>");

        html.append(
                "<h2>📚 Minhas listas</h2>"
        );

        html.append(
                "<span class='contador'>" +
                listas.size() +
                " listas</span>"
        );

        html.append("</div>");

        if (listas.isEmpty()) {

            html.append(
                    "<div class='vazio'>" +
                    "Você ainda não criou nenhuma lista." +
                    "</div>"
            );

        } else {

            for (ListaInfo lista :
                    listas) {

                html.append(
                        "<div class='lista' " +
                        "style='display:block;'>"
                );

                html.append(
                        "<div style='display:flex;" +
                        "align-items:center;" +
                        "gap:12px;" +
                        "margin-bottom:12px;'>"
                );

                html.append(
                        "<div class='lista-icone'>📚</div>"
                );

                html.append(
                        "<div class='lista-info'>"
                );

                html.append(
                        "<h3>" +
                        escaparHtml(lista.nome) +
                        "</h3>"
                );

                html.append(
                        "<p>" +
                        lista.quantidade +
                        (lista.quantidade == 1
                                ? " jogo"
                                : " jogos") +
                        "</p>"
                );

                html.append("</div>");
                html.append("</div>");

                if (lista.jogos.isEmpty()) {

                    html.append(
                            "<div style='color:#8f8496;" +
                            "font-size:12px;" +
                            "padding:10px 0;'>" +
                            "Esta lista ainda está vazia." +
                            "</div>"
                    );

                } else {

                    html.append(
                            "<div style='display:flex;" +
                            "gap:10px;" +
                            "overflow-x:auto;" +
                            "padding:4px 0 8px;'>"
                    );

                    for (JogoInfo jogo :
                            lista.jogos) {

                        String capa;
                        if (jogo.titulo != null && !jogo.titulo.trim().isEmpty()) {
                            try {
                                capa = "capa?titulo=" +
                                        java.net.URLEncoder.encode(jogo.titulo, "UTF-8");
                            } catch (Exception e) {
                                capa = jogo.capa;
                            }
                        } else {
                            capa = jogo.capa;
                        }

                        html.append(
                                "<div style='min-width:90px;" +
                                "max-width:90px;'>"
                        );

                        if (capa != null &&
                                !capa.trim().isEmpty()) {

                            html.append(
                                    "<img src='" +
                                    escaparHtml(capa) +
                                    "' " +
                                    "alt='" +
                                    escaparHtml(jogo.titulo) +
                                    "' " +
                                    "title='" +
                                    escaparHtml(jogo.titulo) +
                                    "' " +
                                    "style='width:90px;" +
                                    "height:120px;" +
                                    "object-fit:cover;" +
                                    "border-radius:8px;" +
                                    "display:block;" +
                                    "background:#18111f;" +
                                    "border:1px solid #33243f;' " +
                                    "onerror=\"this.style.display='none';\">"
                            );
                        }

                        html.append(
                                "<div style='color:#ddd;" +
                                "font-size:10px;" +
                                "margin-top:5px;" +
                                "line-height:1.3;" +
                                "overflow:hidden;" +
                                "text-overflow:ellipsis;'>" +
                                escaparHtml(jogo.titulo) +
                                "</div>"
                        );

                        html.append("</div>");
                    }

                    html.append("</div>");
                }

                html.append("</div>");
            }
        }

        html.append("</section>");

        html.append("</main>");

        html.append("</body>");

        html.append("</html>");

        response.getWriter().write(
                html.toString()
        );
    }

    /* =====================================================
       FAVORITOS
       ===================================================== */

    private List<JogoInfo> buscarFavoritos(
            int idUsuario) {

        List<JogoInfo> lista =
                new ArrayList<JogoInfo>();

        String sql =
                "SELECT f.steam_app_id " +
                "FROM favorito f " +
                "WHERE f.id_usuario = ? " +
                "ORDER BY f.id DESC";

        try (
                Connection conn =
                        Conexao.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idUsuario);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    int appId =
                            rs.getInt(
                                    "steam_app_id"
                            );

                    lista.add(
                            new JogoInfo(
                                    buscarNomeJogo(
                                            appId
                                    ),
                                    appId,
                                    0,
                                    "",
                                    capaSteam(
                                            appId
                                    )
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return lista;
    }

    /* =====================================================
       AVALIAÇÕES
       ===================================================== */

    private List<JogoInfo> buscarAvaliacoes(
            int idUsuario) {

        List<JogoInfo> lista =
                new ArrayList<JogoInfo>();

        String sql =
                "SELECT " +
                "a.steam_app_id, " +
                "a.nota, " +
                "a.comentario " +
                "FROM avaliacao a " +
                "WHERE a.id_usuario = ? " +
                "ORDER BY a.data_avaliacao DESC";

        try (
                Connection conn =
                        Conexao.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idUsuario);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    int appId =
                            rs.getInt(
                                    "steam_app_id"
                            );

                    lista.add(
                            new JogoInfo(
                                    buscarNomeJogo(
                                            appId
                                    ),
                                    appId,
                                    rs.getDouble(
                                            "nota"
                                    ),
                                    rs.getString(
                                            "comentario"
                                    ),
                                    capaSteam(
                                            appId
                                    )
                            )
                    );
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return lista;
    }

    /* =====================================================
       LISTAS
       ===================================================== */

    private List<ListaInfo> buscarListas(
            int idUsuario) {

        List<ListaInfo> listas =
                new ArrayList<ListaInfo>();

        String sql =
                "SELECT l.id, l.nome " +
                "FROM lista l " +
                "WHERE l.id_usuario = ? " +
                "ORDER BY l.data_criacao DESC";

        try (
                Connection conn = Conexao.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    int idLista =
                            rs.getInt("id");

                    ListaInfo info =
                            new ListaInfo(
                                    rs.getString("nome")
                            );

                    info.jogos =
                            buscarJogosDaLista(idLista);

                    info.quantidade =
                            info.jogos.size();

                    listas.add(info);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listas;
    }

    private List<JogoInfo> buscarJogosDaLista(
            int idLista) {

        List<JogoInfo> jogos =
                new ArrayList<JogoInfo>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = Conexao.conectar();

            // Banco atual: lista_jogo referencia jogo.id.
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
                    jogos.add(new JogoInfo(
                            rs.getString("titulo"),
                            0,
                            0,
                            "",
                            rs.getString("capa")
                    ));
                }
            } catch (Exception erroIdJogo) {
                if (rs != null) try { rs.close(); } catch (Exception ignored) {}
                if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
                jogos.clear();

                // Banco antigo: lista_jogo referencia jogo.steam_app_id.
                stmt = conn.prepareStatement(
                        "SELECT j.titulo, j.capa, j.steam_app_id " +
                        "FROM lista_jogo lj " +
                        "INNER JOIN jogo j ON j.steam_app_id = lj.steam_app_id " +
                        "WHERE lj.id_lista = ? ORDER BY lj.id ASC"
                );
                stmt.setInt(1, idLista);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    jogos.add(new JogoInfo(
                            rs.getString("titulo"),
                            0,
                            rs.getInt("steam_app_id"),
                            "",
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

    /* =====================================================
       NOME DO JOGO
       ===================================================== */

    private String buscarNomeJogo(
            int steamAppId) {

        String sql =
                "SELECT titulo " +
                "FROM jogo " +
                "WHERE steam_app_id = ? " +
                "LIMIT 1";

        try (
                Connection conn =
                        Conexao.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, steamAppId);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {

                    String titulo =
                            rs.getString(
                                    "titulo"
                            );

                    if (titulo != null &&
                            !titulo.trim().isEmpty()) {

                        return titulo;
                    }
                }
            }

        } catch (Exception e) {
            // Fallback para Steam
        }

        return buscarNomeSteam(
                steamAppId
        );
    }

    /* =====================================================
       STEAM
       ===================================================== */

    private String buscarNomeSteam(
            int steamAppId) {

        String nome =
                "Jogo " + steamAppId;

        HttpURLConnection conexao =
                null;

        BufferedReader leitor =
                null;

        try {

            URL url =
                    new URL(
                            "https://store.steampowered.com/api/appdetails" +
                            "?appids=" +
                            steamAppId +
                            "&l=portuguese"
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

            if (conexao.getResponseCode() != 200) {
                return nome;
            }

            leitor =
                    new BufferedReader(
                            new InputStreamReader(
                                    conexao.getInputStream(),
                                    "UTF-8"
                            )
                    );

            StringBuilder json =
                    new StringBuilder();

            String linha;

            while (
                    (linha =
                            leitor.readLine()) != null
            ) {

                json.append(linha);
            }

            Pattern pattern =
                    Pattern.compile(
                            "\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                    );

            Matcher matcher =
                    pattern.matcher(
                            json.toString()
                    );

            if (matcher.find()) {

                nome =
                        matcher.group(1)
                                .replace(
                                        "\\/",
                                        "/"
                                )
                                .replace(
                                        "\\\"",
                                        "\""
                                )
                                .replace(
                                        "\\\\",
                                        "\\"
                                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Erro Steam: " +
                    steamAppId
            );

        } finally {

            try {

                if (leitor != null) {
                    leitor.close();
                }

            } catch (Exception ignored) {
            }

            if (conexao != null) {
                conexao.disconnect();
            }
        }

        return nome;
    }

    /* =====================================================
       CARD FAVORITO
       ===================================================== */

    private String montarCardFavorito(
            JogoInfo jogo) {

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<article class='jogo'>"
        );

        html.append(
                "<div class='capa'>"
        );

        html.append(
                "<img src='" +
                escaparHtml(jogo.capa) +
                "' " +
                "alt='" +
                escaparHtml(jogo.titulo) +
                "'>"
        );

        html.append("</div>");

        html.append("<div class='info'>");

        html.append(
                "<h3>" +
                escaparHtml(
                        jogo.titulo
                ) +
                "</h3>"
        );

        html.append(
                "<div class='tipo'>" +
                "⭐ Favorito" +
                "</div>"
        );

        html.append("</div>");

        html.append("</article>");

        return html.toString();
    }

    /* =====================================================
       CARD AVALIAÇÃO
       ===================================================== */

    private String montarCardAvaliacao(
            JogoInfo jogo) {

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<article class='jogo'>"
        );

        html.append(
                "<div class='capa'>"
        );

        html.append(
                "<img src='" +
                escaparHtml(jogo.capa) +
                "' " +
                "alt='" +
                escaparHtml(jogo.titulo) +
                "'>"
        );

        html.append("</div>");
        

        html.append(
                "<div class='nota'>" +
                "⭐ " +
                jogo.nota +
                "/5" +
                "</div>"
        );

        if (jogo.comentario != null &&
                !jogo.comentario.trim().isEmpty()) {

            html.append(
                    "<div class='comentario'>" +
                    escaparHtml(
                            jogo.comentario
                    ) +
                    "</div>"
            );
        }

        html.append("</div>");

        html.append("</article>");

        return html.toString();
    }

    /* =====================================================
       CAPA
       ===================================================== */

    private String capaSteam(
            int appId) {

        if (appId <= 0) {
            return "";
        }

        return "capa?appId=" + appId;
    }

    /* =====================================================
       PRIMEIRA LETRA
       ===================================================== */

    private String primeiraLetra(
            String nome) {

        if (nome == null ||
                nome.trim().isEmpty()) {

            return "?";
        }

        return String.valueOf(
                nome.trim().charAt(0)
        ).toUpperCase();
    }

    /* =====================================================
       ESCAPAR HTML
       ===================================================== */

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

    /* =====================================================
       JOGO
       ===================================================== */

    private static class JogoInfo {

        String titulo;
        int appId;
        double nota;
        String comentario;
        String capa;

        JogoInfo(
                String titulo,
                int appId,
                double nota,
                String comentario,
                String capa) {

            this.titulo = titulo;
            this.appId = appId;
            this.nota = nota;
            this.comentario = comentario;
            this.capa = capa;
        }
    }

    /* =====================================================
       LISTA
       ===================================================== */

    private static class ListaInfo {

        String nome;
        int quantidade;
        List<JogoInfo> jogos;

        ListaInfo(
                String nome) {

            this.nome = nome;
            this.quantidade = 0;
            this.jogos =
                    new ArrayList<JogoInfo>();
        }
    }
}