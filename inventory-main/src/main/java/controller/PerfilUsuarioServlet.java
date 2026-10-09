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

@WebServlet("/perfil-usuario")
public class PerfilUsuarioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect("login.html");
            return;
        }

        try {

            Usuario logado =
                    (Usuario) sessao.getAttribute("usuario");

            String idTexto =
                    request.getParameter("id");

            if (idTexto == null ||
                    idTexto.trim().isEmpty()) {

                response.sendRedirect("buscar-usuarios");
                return;
            }

            int idPerfil =
                    Integer.parseInt(idTexto);

            UsuarioDAO usuarioDAO =
                    new UsuarioDAO();

            Usuario usuario =
                    usuarioDAO.buscarPorId(idPerfil);

            if (usuario == null) {

                response.sendRedirect("buscar-usuarios");
                return;
            }

            int seguidores =
                    usuarioDAO.contarSeguidores(idPerfil);

            int seguindo =
                    usuarioDAO.contarSeguindo(idPerfil);

            boolean mesmoUsuario =
                    logado.getId() == idPerfil;

            boolean seguindoUsuario =
                    verificarSeguindo(
                            logado.getId(),
                            idPerfil
                    );

            List<JogoInfo> favoritos =
                    buscarFavoritos(idPerfil);

            List<AvaliacaoInfo> avaliacoes =
                    buscarAvaliacoes(idPerfil);

            List<ListaInfo> listas =
                    buscarListas(idPerfil);

            StringBuilder html =
                    new StringBuilder();

            // =====================================================
            // HTML
            // =====================================================

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

            // =====================================================
            // CSS
            // =====================================================

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
                    "top:0;" +
                    "left:0;" +
                    "right:0;" +
                    "height:430px;" +
                    "background:radial-gradient(circle at 50% 0%,rgba(139,92,246,.20),transparent 65%);" +
                    "pointer-events:none;" +
                    "}"
            );

            // =====================================================
            // HEADER
            // =====================================================

            html.append(
                    "header{" +
                    "height:70px;" +
                    "background:rgba(9,9,11,.92);" +
                    "border-bottom:1px solid #27272a;" +
                    "display:flex;" +
                    "align-items:center;" +
                    "justify-content:space-between;" +
                    "padding:0 7%;" +
                    "position:sticky;" +
                    "top:0;" +
                    "z-index:20;" +
                    "backdrop-filter:blur(14px);" +
                    "}"
            );

            html.append(
                    ".logo{" +
                    "font-size:24px;" +
                    "font-weight:800;" +
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
                    "gap:25px;" +
                    "}"
            );

            html.append(
                    "nav a{" +
                    "color:#a1a1aa;" +
                    "text-decoration:none;" +
                    "font-size:13px;" +
                    "font-weight:500;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    "nav a:hover{" +
                    "color:#c084fc;" +
                    "}"
            );

            // =====================================================
            // CONTAINER
            // =====================================================

            html.append(
                    ".container{" +
                    "position:relative;" +
                    "width:90%;" +
                    "max-width:1120px;" +
                    "margin:42px auto 80px;" +
                    "}"
            );

            // =====================================================
            // VOLTAR
            // =====================================================

            html.append(
                    ".voltar{" +
                    "display:inline-flex;" +
                    "align-items:center;" +
                    "gap:7px;" +
                    "color:#a1a1aa;" +
                    "font-size:12px;" +
                    "text-decoration:none;" +
                    "margin-bottom:22px;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".voltar:hover{" +
                    "color:#c084fc;" +
                    "}"
            );

            // =====================================================
            // PERFIL
            // =====================================================

            html.append(
                    ".perfil{" +
                    "background:linear-gradient(145deg,#18181b,#111113);" +
                    "border:1px solid #2a2a2e;" +
                    "border-radius:22px;" +
                    "padding:30px;" +
                    "display:flex;" +
                    "align-items:center;" +
                    "gap:25px;" +
                    "box-shadow:0 20px 60px rgba(0,0,0,.35);" +
                    "}"
            );

            html.append(
                    ".foto{" +
                    "width:110px;" +
                    "height:110px;" +
                    "border-radius:50%;" +
                    "object-fit:cover;" +
                    "border:2px solid #8b5cf6;" +
                    "background:#27272a;" +
                    "flex-shrink:0;" +
                    "}"
            );

            html.append(
                    ".sem-foto{" +
                    "display:flex;" +
                    "align-items:center;" +
                    "justify-content:center;" +
                    "font-size:38px;" +
                    "font-weight:800;" +
                    "color:#c084fc;" +
                    "}"
            );

            html.append(
                    ".dados{" +
                    "flex:1;" +
                    "min-width:0;" +
                    "}"
            );

            html.append(
                    ".dados h1{" +
                    "font-size:28px;" +
                    "font-weight:700;" +
                    "margin-bottom:4px;" +
                    "}"
            );

            html.append(
                    ".username{" +
                    "color:#a78bfa;" +
                    "font-size:13px;" +
                    "margin-bottom:9px;" +
                    "}"
            );

            html.append(
                    ".bio{" +
                    "font-size:13px;" +
                    "line-height:1.6;" +
                    "color:#a1a1aa;" +
                    "max-width:650px;" +
                    "}"
            );

            // =====================================================
            // ESTATÍSTICAS
            // =====================================================

            html.append(
                    ".stats{" +
                    "display:flex;" +
                    "gap:28px;" +
                    "margin-top:18px;" +
                    "}"
            );

            html.append(
                    ".stat strong{" +
                    "display:block;" +
                    "font-size:19px;" +
                    "}"
            );

            html.append(
                    ".stat span{" +
                    "display:block;" +
                    "font-size:10px;" +
                    "color:#71717a;" +
                    "margin-top:2px;" +
                    "}"
            );

            // =====================================================
            // BOTÃO SEGUIR
            // =====================================================

            html.append(
                    ".seguir-area{" +
                    "flex-shrink:0;" +
                    "}"
            );

            html.append(
                    ".btn-seguir{" +
                    "border:0;" +
                    "background:#8b5cf6;" +
                    "color:#fff;" +
                    "padding:11px 23px;" +
                    "border-radius:9px;" +
                    "font-size:12px;" +
                    "font-weight:700;" +
                    "cursor:pointer;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".btn-seguir:hover{" +
                    "background:#a855f7;" +
                    "transform:translateY(-1px);" +
                    "}"
            );

            html.append(
                    ".seguindo{" +
                    "background:#27272a;" +
                    "border:1px solid #3f3f46;" +
                    "color:#d4d4d8;" +
                    "}"
            );

            // =====================================================
            // SEÇÕES
            // =====================================================

            html.append(
                    ".secao{" +
                    "margin-top:45px;" +
                    "}"
            );

            html.append(
                    ".topo-secao{" +
                    "display:flex;" +
                    "align-items:center;" +
                    "justify-content:space-between;" +
                    "margin-bottom:20px;" +
                    "}"
            );

            html.append(
                    ".topo-secao h2{" +
                    "font-size:20px;" +
                    "font-weight:700;" +
                    "}"
            );

            html.append(
                    ".contador{" +
                    "font-size:11px;" +
                    "color:#a1a1aa;" +
                    "background:#18181b;" +
                    "border:1px solid #2a2a2e;" +
                    "padding:6px 11px;" +
                    "border-radius:20px;" +
                    "}"
            );

            // =====================================================
            // GRID DE JOGOS
            // =====================================================

            html.append(
                    ".jogos-grid{" +
                    "display:grid;" +
                    "grid-template-columns:repeat(auto-fill,minmax(170px,1fr));" +
                    "gap:16px;" +
                    "}"
            );

            html.append(
                    ".jogo-card{" +
                    "background:#141416;" +
                    "border:1px solid #27272a;" +
                    "border-radius:14px;" +
                    "overflow:hidden;" +
                    "transition:.22s;" +
                    "}"
            );

            html.append(
                    ".jogo-card:hover{" +
                    "transform:translateY(-4px);" +
                    "border-color:#6d28d9;" +
                    "box-shadow:0 12px 30px rgba(0,0,0,.35);" +
                    "}"
            );

            html.append(
                    ".jogo-capa{" +
                    "height:230px;" +
                    "background:#09090b;" +
                    "}"
            );

            html.append(
                    ".jogo-capa img{" +
                    "width:100%;" +
                    "height:100%;" +
                    "object-fit:cover;" +
                    "display:block;" +
                    "}"
            );

            html.append(
                    ".jogo-info{" +
                    "padding:13px;" +
                    "}"
            );

            html.append(
                    ".jogo-info h3{" +
                    "font-size:13px;" +
                    "line-height:1.4;" +
                    "min-height:37px;" +
                    "}"
            );

            html.append(
                    ".jogo-tipo{" +
                    "font-size:10px;" +
                    "color:#71717a;" +
                    "margin-top:6px;" +
                    "}"
            );

            // =====================================================
            // AVALIAÇÕES
            // =====================================================

            html.append(
                    ".avaliacoes-grid{" +
                    "display:grid;" +
                    "grid-template-columns:repeat(auto-fill,minmax(280px,1fr));" +
                    "gap:16px;" +
                    "}"
            );

            html.append(
                    ".avaliacao{" +
                    "display:flex;" +
                    "background:#141416;" +
                    "border:1px solid #27272a;" +
                    "border-radius:14px;" +
                    "overflow:hidden;" +
                    "min-height:175px;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".avaliacao:hover{" +
                    "border-color:#6d28d9;" +
                    "transform:translateY(-3px);" +
                    "}"
            );

            html.append(
                    ".avaliacao-capa{" +
                    "width:110px;" +
                    "min-width:110px;" +
                    "background:#09090b;" +
                    "}"
            );

            html.append(
                    ".avaliacao-capa img{" +
                    "width:100%;" +
                    "height:100%;" +
                    "object-fit:cover;" +
                    "display:block;" +
                    "}"
            );

            html.append(
                    ".avaliacao-info{" +
                    "padding:16px;" +
                    "display:flex;" +
                    "flex-direction:column;" +
                    "min-width:0;" +
                    "}"
            );

            html.append(
                    ".avaliacao-info h3{" +
                    "font-size:14px;" +
                    "line-height:1.4;" +
                    "margin-bottom:8px;" +
                    "}"
            );

            html.append(
                    ".nota{" +
                    "color:#fbbf24;" +
                    "font-size:12px;" +
                    "font-weight:700;" +
                    "margin-bottom:9px;" +
                    "}"
            );

            html.append(
                    ".comentario{" +
                    "font-size:11px;" +
                    "line-height:1.55;" +
                    "color:#a1a1aa;" +
                    "display:-webkit-box;" +
                    "-webkit-line-clamp:5;" +
                    "-webkit-box-orient:vertical;" +
                    "overflow:hidden;" +
                    "}"
            );

            html.append(
                    ".horas{" +
                    "font-size:10px;" +
                    "color:#71717a;" +
                    "margin-top:auto;" +
                    "padding-top:10px;" +
                    "}"
            );

            // =====================================================
            // LISTAS
            // =====================================================

            html.append(
                    ".listas{" +
                    "display:grid;" +
                    "grid-template-columns:repeat(auto-fill,minmax(250px,1fr));" +
                    "gap:15px;" +
                    "}"
            );

            html.append(
                    ".lista{" +
                    "background:linear-gradient(145deg,#18181b,#111113);" +
                    "border:1px solid #29292d;" +
                    "border-radius:15px;" +
                    "padding:20px;" +
                    "transition:.2s;" +
                    "}"
            );

            html.append(
                    ".lista:hover{" +
                    "border-color:#6d28d9;" +
                    "transform:translateY(-3px);" +
                    "}"
            );

            html.append(
                    ".lista-icone{" +
                    "width:40px;" +
                    "height:40px;" +
                    "border-radius:10px;" +
                    "background:#24153a;" +
                    "display:flex;" +
                    "align-items:center;" +
                    "justify-content:center;" +
                    "font-size:18px;" +
                    "margin-bottom:13px;" +
                    "}"
            );

            html.append(
                    ".lista h3{" +
                    "font-size:15px;" +
                    "font-weight:600;" +
                    "margin-bottom:6px;" +
                    "}"
            );

            html.append(
                    ".lista p{" +
                    "font-size:11px;" +
                    "color:#71717a;" +
                    "}"
            );

            // =====================================================
            // VAZIO
            // =====================================================

            html.append(
                    ".vazio{" +
                    "border:1px dashed #3f3f46;" +
                    "background:#111113;" +
                    "border-radius:14px;" +
                    "padding:35px;" +
                    "text-align:center;" +
                    "color:#71717a;" +
                    "font-size:12px;" +
                    "}"
            );

            // =====================================================
            // RESPONSIVO
            // =====================================================

            html.append(
                    "@media(max-width:700px){"
            );

            html.append(
                    "header{" +
                    "padding:0 20px;" +
                    "}"
            );

            html.append(
                    "nav{" +
                    "gap:12px;" +
                    "}"
            );

            html.append(
                    "nav a:nth-child(2)," +
                    "nav a:nth-child(3){" +
                    "display:none;" +
                    "}"
            );

            html.append(
                    ".container{" +
                    "width:94%;" +
                    "margin-top:25px;" +
                    "}"
            );

            html.append(
                    ".perfil{" +
                    "flex-direction:column;" +
                    "text-align:center;" +
                    "padding:25px 18px;" +
                    "}"
            );

            html.append(
                    ".dados{" +
                    "width:100%;" +
                    "}"
            );

            html.append(
                    ".bio{" +
                    "margin:auto;" +
                    "}"
            );

            html.append(
                    ".stats{" +
                    "justify-content:center;" +
                    "gap:20px;" +
                    "}"
            );

            html.append(
                    ".seguir-area{" +
                    "width:100%;" +
                    "}"
            );

            html.append(
                    ".btn-seguir{" +
                    "width:100%;" +
                    "}"
            );

            html.append(
                    ".jogos-grid{" +
                    "grid-template-columns:repeat(2,1fr);" +
                    "gap:12px;" +
                    "}"
            );

            html.append(
                    ".jogo-capa{" +
                    "height:205px;" +
                    "}"
            );

            html.append(
                    ".avaliacoes-grid{" +
                    "grid-template-columns:1fr;" +
                    "}"
            );

            html.append(
                    ".lista-topo{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px;}"
            );
            html.append(
                    ".lista-topo h3{margin:0;color:#fff;font-size:18px;}"
            );
            html.append(
                    ".lista-topo p{margin:4px 0 0;color:#a1a1aa;font-size:12px;}"
            );
            html.append(
                    ".lista-capas{display:flex;gap:8px;overflow:hidden;}"
            );
            html.append(
                    ".lista-capas img{width:58px;aspect-ratio:2/3;height:auto;object-fit:cover;border-radius:7px;border:1px solid #33243f;background:#18111f;}"
            );
            html.append(
                    ".lista-vazia{padding:18px;border:1px dashed #3a2945;border-radius:10px;color:#8f8496;font-size:12px;text-align:center;}"
            );

            html.append(
                    ".listas{" +
                    "grid-template-columns:1fr;" +
                    "}"
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
        html.append(".rede-pessoa{display:flex;align-items:center;gap:10px;padding:9px;border-radius:10px;background:#17111e;border:1px solid #29202f;text-decoration:none;color:#fff;transition:.2s;}");
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

        html.append("</style>");

            html.append("</head>");

            // =====================================================
            // BODY
            // =====================================================

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
        html.append("<a href='listas'>Listas</a>");
        html.append("<a href='logout'>Sair</a>");
        html.append("</nav>");

        html.append("</header>");

            html.append("<main class='container'>");

            // VOLTAR

            html.append(
                    "<a class='voltar' " +
                    "href='buscar-usuarios'>" +
                    "← Voltar para usuários" +
                    "</a>"
            );

            // =====================================================
            // PERFIL
            // =====================================================

            html.append("<section class='perfil'>");

            String foto =
                    usuario.getFoto();

            if (foto != null &&
                    !foto.trim().isEmpty()) {

                String fotoValor = foto.trim();
                String fotoUrl;

                if (fotoValor.startsWith("http://") ||
                        fotoValor.startsWith("https://")) {
                    fotoUrl =
                            request.getContextPath() +
                            "/foto-perfil?url=" +
                            java.net.URLEncoder.encode(
                                    fotoValor,
                                    "UTF-8"
                            );
                } else {
                    fotoUrl =
                            request.getContextPath() +
                            "/foto-perfil?arquivo=" +
                            java.net.URLEncoder.encode(
                                    new java.io.File(fotoValor).getName(),
                                    "UTF-8"
                            );
                }

                html.append(
                        "<img class='foto' " +
                        "src='" +
                        escaparHtml(fotoUrl) +
                        "' " +
                        "alt='Foto de perfil' " +
                        "onerror=\"this.style.display='none'\">"
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
                    username = "@" + username;
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

            // SEGUIR

            if (!mesmoUsuario) {

                html.append(
                        "<div class='seguir-area'>"
                );

                html.append(
                        "<form method='POST' " +
                        "action='" +
                        request.getContextPath() +
                        "/seguir'>"
                );

                html.append(
                        "<input type='hidden' " +
                        "name='idUsuario' " +
                        "value='" +
                        idPerfil +
                        "'>"
                );

                if (seguindoUsuario) {

                    html.append(
                            "<input type='hidden' " +
                            "name='acao' " +
                            "value='deixar'>"
                    );

                    html.append(
                            "<button class='btn-seguir seguindo' " +
                            "type='submit'>" +
                            "✓ Seguindo" +
                            "</button>"
                    );

                } else {

                    html.append(
                            "<input type='hidden' " +
                            "name='acao' " +
                            "value='seguir'>"
                    );

                    html.append(
                            "<button class='btn-seguir' " +
                            "type='submit'>" +
                            "+ Seguir" +
                            "</button>"
                    );
                }

                html.append("</form>");

                html.append("</div>");
            }

            html.append("</section>");

            html.append(
                    RedeSocialUtil.renderRede(
                            idPerfil,
                            request.getContextPath()
                    )
            );

            // =====================================================
            // FAVORITOS
            // =====================================================

            html.append("<section class='secao'>");

            html.append("<div class='topo-secao'>");

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
                        "Este usuário ainda não adicionou jogos aos favoritos." +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='jogos-grid'>"
                );

                for (JogoInfo jogo :
                        favoritos) {

                    html.append(
                            montarCardJogo(
                                    jogo,
                                    "⭐ Favorito"
                            )
                    );
                }

                html.append("</div>");
            }

            html.append("</section>");

            // =====================================================
            // AVALIAÇÕES
            // =====================================================

            html.append("<section class='secao'>");

            html.append("<div class='topo-secao'>");

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
                        "Este usuário ainda não avaliou nenhum jogo." +
                        "</div>"
                );

            } else {

                html.append(
                        "<div class='avaliacoes-grid'>"
                );

                for (AvaliacaoInfo avaliacao :
                        avaliacoes) {

                    html.append(
                            montarCardAvaliacao(
                                    avaliacao
                            )
                    );
                }

                html.append("</div>");
            }

            html.append("</section>");

            // =====================================================
            // LISTAS
            // =====================================================

            html.append("<section class='secao'>");

            html.append("<div class='topo-secao'>");

            html.append(
                    "<h2>📚 Listas</h2>"
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
                        "Este usuário ainda não criou nenhuma lista." +
                        "</div>"
                );

            } else {

                html.append("<div class='listas'>");

                for (ListaInfo lista :
                        listas) {

                    html.append(
                            "<div class='lista'>"
                    );

                    html.append(
                            "<div class='lista-topo'>" +
                            "<div>" +
                            "<h3>" +
                            escaparHtml(lista.nome) +
                            "</h3>" +
                            "<p>" +
                            lista.quantidade +
                            (lista.quantidade == 1 ? " jogo" : " jogos") +
                            "</p>" +
                            "</div>" +
                            "</div>"
                    );

                    if (lista.jogos.isEmpty()) {

                        html.append(
                                "<div class='lista-vazia'>Lista sem jogos</div>"
                        );

                    } else {

                        html.append("<div class='lista-capas'>");

                        int limite = Math.min(
                                lista.jogos.size(),
                                5
                        );

                        for (int i = 0; i < limite; i++) {

                            JogoInfo jogoLista =
                                    lista.jogos.get(i);

                            String capa =
                                    capaLista(
                                            jogoLista
                                    );

                            html.append(
                                    "<img src='" +
                                    escaparHtml(capa) +
                                    "' alt='" +
                                    escaparHtml(jogoLista.titulo) +
                                    "' title='" +
                                    escaparHtml(jogoLista.titulo) +
                                    "' onerror=\"this.style.display=\'none\';\">"
                            );
                        }

                        html.append("</div>");
                    }

                    html.append("</div>");
                }

                html.append("</div>");
            }

            html.append("</section>");

            html.append("</main>");

            html.append("</body>");

            html.append("</html>");

            response.getWriter().write(
                    html.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    "buscar-usuarios"
            );
        }
    }

    // =====================================================
    // VERIFICAR SE SEGUE
    // =====================================================

    private boolean verificarSeguindo(
            int idSeguidor,
            int idSeguido) {

        if (idSeguidor == idSeguido) {
            return false;
        }

        String sql =
                "SELECT id " +
                "FROM seguidor " +
                "WHERE id_seguidor = ? " +
                "AND id_seguido = ? " +
                "LIMIT 1";

        try (
                Connection conn =
                        Conexao.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, idSeguidor);
            stmt.setInt(2, idSeguido);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                return rs.next();
            }

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // FAVORITOS
    // =====================================================

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

            stmt.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    int appId =
                            rs.getInt(
                                    "steam_app_id"
                            );

                    String titulo =
                            buscarNomeJogo(appId);

                    lista.add(
                            new JogoInfo(
                                    titulo,
                                    appId
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO BUSCAR FAVORITOS"
            );

            e.printStackTrace();
        }

        return lista;
    }

    // =====================================================
    // AVALIAÇÕES
    // =====================================================

    private List<AvaliacaoInfo> buscarAvaliacoes(
            int idUsuario) {

        List<AvaliacaoInfo> lista =
                new ArrayList<AvaliacaoInfo>();

        String sql =
                "SELECT " +
                "a.steam_app_id, " +
                "a.nota, " +
                "a.comentario, " +
                "a.horas_jogadas " +
                "FROM avaliacao a " +
                "WHERE a.id_usuario = ? " +
                "ORDER BY a.data_avaliacao DESC";

        try (
                Connection conn =
                        Conexao.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    int appId =
                            rs.getInt(
                                    "steam_app_id"
                            );

                    String titulo =
                            buscarNomeJogo(appId);

                    double nota =
                            rs.getDouble("nota");

                    String comentario =
                            rs.getString(
                                    "comentario"
                            );

                    double horas =
                            rs.getDouble(
                                    "horas_jogadas"
                            );

                    lista.add(
                            new AvaliacaoInfo(
                                    titulo,
                                    appId,
                                    nota,
                                    comentario,
                                    horas
                            )
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO BUSCAR AVALIACOES"
            );

            e.printStackTrace();
        }

        return lista;
    }

    // =====================================================
    // LISTAS
    // =====================================================

    private List<ListaInfo> buscarListas(
            int idUsuario) {

        List<ListaInfo> lista =
                new ArrayList<ListaInfo>();

        String sql =
                "SELECT " +
                "l.id, " +
                "l.nome, " +
                "COUNT(lj.id) AS quantidade " +
                "FROM lista l " +
                "LEFT JOIN lista_jogo lj " +
                "ON lj.id_lista = l.id " +
                "WHERE l.id_usuario = ? " +
                "GROUP BY l.id, l.nome " +
                "ORDER BY l.data_criacao DESC";

        try (
                Connection conn =
                        Conexao.conectar();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    int idLista =
                            rs.getInt("id");

                    ListaInfo info =
                            new ListaInfo(
                                    rs.getString("nome"),
                                    rs.getInt("quantidade")
                            );

                    info.jogos =
                            buscarJogosLista(idLista);

                    lista.add(info);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO BUSCAR LISTAS"
            );

            e.printStackTrace();
        }

        return lista;
    }

    // =====================================================
    // JOGOS DAS LISTAS
    // =====================================================

    private List<JogoInfo> buscarJogosLista(
            int idLista) {

        List<JogoInfo> jogos =
                new ArrayList<JogoInfo>();

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
                    jogos.add(new JogoInfo(
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
                    jogos.add(new JogoInfo(
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

    private String capaLista(JogoInfo jogo) {

        if (jogo.titulo != null && !jogo.titulo.trim().isEmpty()) {
            try {
                return "capa?titulo=" +
                        java.net.URLEncoder.encode(jogo.titulo, "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                return "";
            }
        }

        if (jogo.appId > 0) {
            return capaSteam(jogo.appId);
        }

        return "";
    }

    // =====================================================
    // NOME DO JOGO
    // =====================================================

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

            stmt.setInt(
                    1,
                    steamAppId
            );

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

            // Usa a Steam como fallback.
        }

        return buscarNomeSteam(
                steamAppId
        );
    }

    // =====================================================
    // NOME PELA STEAM
    // =====================================================

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
                    "Erro ao buscar jogo na Steam: " +
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

    // =====================================================
    // CARD DE JOGO
    // =====================================================

    private String montarCardJogo(
            JogoInfo jogo,
            String tipo) {

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<article class='jogo-card'>"
        );

        html.append(
                "<div class='jogo-capa'>"
        );

        html.append(
                "<img src='" +
                escaparHtml(
                        capaSteam(jogo.appId)
                ) +
                "' " +
                "alt='" +
                escaparHtml(jogo.titulo) +
                "' " +
                "onerror=\"this.style.display='none';\">"
        );

        html.append("</div>");

        html.append(
                "<div class='jogo-info'>"
        );

        html.append(
                "<h3>" +
                escaparHtml(
                        jogo.titulo
                ) +
                "</h3>"
        );

        html.append(
                "<div class='jogo-tipo'>" +
                tipo +
                "</div>"
        );

        html.append("</div>");

        html.append("</article>");

        return html.toString();
    }

    // =====================================================
    // CARD AVALIAÇÃO
    // =====================================================

    private String montarCardAvaliacao(
            AvaliacaoInfo avaliacao) {

        StringBuilder html =
                new StringBuilder();

        html.append(
                "<article class='avaliacao'>"
        );

        html.append(
                "<div class='avaliacao-capa'>"
        );

        html.append(
                "<img src='" +
                escaparHtml(
                        capaSteam(
                                avaliacao.appId
                        )
                ) +
                "' " +
                "alt='" +
                escaparHtml(
                        avaliacao.titulo
                ) +
                "' " +
                "onerror=\"this.style.display='none';\">"
        );

        html.append("</div>");

        html.append(
                "<div class='avaliacao-info'>"
        );

        html.append(
                "<h3>" +
                escaparHtml(
                        avaliacao.titulo
                ) +
                "</h3>"
        );

        html.append(
                "<div class='nota'>" +
                "⭐ " +
                avaliacao.nota +
                "/5" +
                "</div>"
        );

        if (avaliacao.comentario != null &&
                !avaliacao.comentario.trim().isEmpty()) {

            html.append(
                    "<div class='comentario'>" +
                    escaparHtml(
                            avaliacao.comentario
                    ) +
                    "</div>"
            );
        }

        if (avaliacao.horas > 0) {

            html.append(
                    "<div class='horas'>" +
                    "🎮 " +
                    avaliacao.horas +
                    " horas jogadas" +
                    "</div>"
            );
        }

        html.append("</div>");

        html.append("</article>");

        return html.toString();
    }

    // =====================================================
    // CAPA STEAM
    // =====================================================

    private String capaSteam(
            int appId) {

        if (appId <= 0) {
            return "";
        }

        return "capa?appId=" + appId;
    }

    // =====================================================
    // PRIMEIRA LETRA
    // =====================================================

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

    private static class JogoInfo {

        String titulo;

        int appId;

        String capa;

        JogoInfo(
                String titulo,
                int appId) {

            this(titulo, appId, null);
        }

        JogoInfo(
                String titulo,
                int appId,
                String capa) {

            this.titulo =
                    titulo;

            this.appId =
                    appId;

            this.capa =
                    capa;
        }
    }

    // =====================================================
    // CLASSE AVALIAÇÃO
    // =====================================================

    private static class AvaliacaoInfo {

        String titulo;

        int appId;

        double nota;

        String comentario;

        double horas;

        AvaliacaoInfo(
                String titulo,
                int appId,
                double nota,
                String comentario,
                double horas) {

            this.titulo =
                    titulo;

            this.appId =
                    appId;

            this.nota =
                    nota;

            this.comentario =
                    comentario;

            this.horas =
                    horas;
        }
    }

    // =====================================================
    // CLASSE LISTA
    // =====================================================

    private static class ListaInfo {

        String nome;

        int quantidade;

        List<JogoInfo> jogos =
                new ArrayList<JogoInfo>();

        ListaInfo(
                String nome,
                int quantidade) {

            this.nome =
                    nome;

            this.quantidade =
                    quantidade;
        }
    }
}