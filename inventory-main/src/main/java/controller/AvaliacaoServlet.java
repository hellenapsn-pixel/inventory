package controller;

import dao.Conexao;
import model.Usuario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/avaliar")
public class AvaliacaoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        Usuario usuario =
                (Usuario) sessao.getAttribute("usuario");

        int idUsuario =
                usuario.getId();

        String idTexto =
                request.getParameter("id");

        if (idTexto == null ||
                idTexto.trim().isEmpty()) {

            response.sendRedirect("biblioteca");
            return;
        }

        try {

            int steamAppId =
                    Integer.parseInt(idTexto);

            // =====================================================
            // VERIFICAR STATUS DO JOGO
            // =====================================================

            String sqlStatus =
                    "SELECT status " +
                    "FROM biblioteca " +
                    "WHERE id_usuario = ? " +
                    "AND steam_app_id = ?";

            try (
                    Connection conexao =
                            Conexao.conectar();

                    PreparedStatement ps =
                            conexao.prepareStatement(sqlStatus)
            ) {

                ps.setInt(1, idUsuario);
                ps.setInt(2, steamAppId);

                ResultSet rs =
                        ps.executeQuery();

                if (!rs.next()) {

                    response.sendRedirect("biblioteca");
                    return;
                }

                String status =
                        rs.getString("status");

                /*
                 * Só pode avaliar quando estiver JOGANDO.
                 *
                 * Quero jogar -> NÃO pode avaliar
                 * Jogando     -> PODE avaliar
                 * Zerado      -> já foi finalizado
                 */

                if (!"jogando".equals(status)) {

                    response.sendRedirect("biblioteca");
                    return;
                }

                rs.close();
            }

            // =====================================================
            // BUSCAR NOME E CAPA
            // =====================================================

            String titulo =
                    buscarNomeSteam(steamAppId);

            // Mesma rota usada pela biblioteca: o servidor resolve a capa
            // (arquivo local, URL do banco ou Steam) em vez do navegador.
            String capa =
                    "capa?appId=" +
                    steamAppId;

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
                    "<meta charset='UTF-8'>"
            );

            html.append(
                    "<meta name='viewport' " +
                    "content='width=device-width, initial-scale=1.0'>"
            );

            html.append(
                    "<title>Avaliar " +
                    escapar(titulo) +
                    " - Inventory</title>"
            );

            html.append(
                    "<link rel='icon' type='image/png' href='icon.png'>"
            );

            html.append(
                    "<link href='https://fonts.googleapis.com/css2?" +
                    "family=Rajdhani:wght@400;500;600;700&display=swap' " +
                    "rel='stylesheet'>"
            );

            html.append(
                    "<style>" +

                    "*{box-sizing:border-box;}" +

                    "body{" +
                    "margin:0;" +
                    "min-height:100vh;" +
                    "background:radial-gradient(circle at top,#35105f,#160b22 45%,#09060d);" +
                    "color:#fff;" +
                    "font-family:'Rajdhani',sans-serif;" +
                    "}" +

                    "header{" +
                    "width:100%;" +
                    "min-height:80px;" +
                    "padding:14px 35px;" +
                    "display:flex;" +
                    "align-items:center;" +
                    "justify-content:space-between;" +
                    "gap:25px;" +
                    "background:rgba(10,6,15,.96);" +
                    "border-bottom:1px solid #322044;" +
                    "}" +

                    ".logo-area{" +
                    "display:flex;" +
                    "align-items:center;" +
                    "gap:9px;" +
                    "}" +

                    ".logo-header{" +
                    "width:40px;" +
                    "height:40px;" +
                    "object-fit:contain;" +
                    "}" +

                    ".logo-area h1{" +
                    "margin:0;" +
                    "font-size:30px;" +
                    "font-family:'Rajdhani',sans-serif;" +
                    "}" +

                    "nav{" +
                    "display:flex;" +
                    "align-items:center;" +
                    "gap:25px;" +
                    "flex-wrap:wrap;" +
                    "}" +

                    "nav a{" +
                    "color:#aaa1b5;" +
                    "text-decoration:none;" +
                    "font-size:14px;" +
                    "font-weight:bold;" +
                    "}" +

                    "nav a:hover{" +
                    "color:#b66cff;" +
                    "}" +

                    ".avaliacao-container{" +
                    "max-width:600px;" +
                    "margin:50px auto;" +
                    "padding:35px;" +
                    "background:linear-gradient(135deg,#24102f,#140b1b);" +
                    "border:1px solid #4b2464;" +
                    "border-radius:16px;" +
                    "text-align:center;" +
                    "box-shadow:0 15px 45px rgba(0,0,0,.35);" +
                    "}" +

                    ".capa-avaliacao{" +
                    "width:180px;" +
                    "height:250px;" +
                    "object-fit:cover;" +
                    "border-radius:8px;" +
                    "display:block;" +
                    "margin:0 auto 20px;" +
                    "}" +

                    "h2{" +
                    "font-size:26px;" +
                    "margin:10px 0;" +
                    "}" +

                    "p{" +
                    "color:#bbb0c2;" +
                    "}" +

                    ".estrelas{" +
                    "display:flex;" +
                    "flex-direction:row-reverse;" +
                    "justify-content:center;" +
                    "gap:5px;" +
                    "margin:22px 0;" +
                    "}" +

                    ".estrelas input{" +
                    "display:none;" +
                    "}" +

                    ".estrelas label{" +
                    "font-size:42px;" +
                    "color:#666;" +
                    "cursor:pointer;" +
                    "}" +

                    ".estrelas label:hover," +
                    ".estrelas label:hover~label," +
                    ".estrelas input:checked~label{" +
                    "color:#ffd700;" +
                    "}" +

                    ".horas-container{" +
                    "margin-top:20px;" +
                    "text-align:left;" +
                    "}" +

                    ".horas-container label{" +
                    "display:block;" +
                    "margin-bottom:8px;" +
                    "font-weight:bold;" +
                    "}" +

                    ".campo-horas," +
                    ".campo-resenha{" +
                    "width:100%;" +
                    "background:#14101a;" +
                    "color:#fff;" +
                    "border:1px solid #493252;" +
                    "border-radius:8px;" +
                    "outline:none;" +
                    "}" +

                    ".campo-horas{" +
                    "padding:12px;" +
                    "font-size:16px;" +
                    "}" +

                    ".campo-resenha{" +
                    "height:150px;" +
                    "padding:15px;" +
                    "resize:vertical;" +
                    "font-size:15px;" +
                    "font-family:'Rajdhani',sans-serif;" +
                    "margin-top:20px;" +
                    "}" +

                    ".botao-postar{" +
                    "margin-top:20px;" +
                    "padding:12px 30px;" +
                    "border:0;" +
                    "border-radius:7px;" +
                    "background:linear-gradient(135deg,#7c3aed,#9333ea);" +
                    "color:white;" +
                    "font-weight:bold;" +
                    "cursor:pointer;" +
                    "font-size:16px;" +
                    "}" +

                    ".botao-postar:hover{" +
                    "background:#a33cff;" +
                    "}" +

                    "@media(max-width:800px){" +

                    "header{" +
                    "padding:14px 20px;" +
                    "flex-direction:column;" +
                    "align-items:flex-start;" +
                    "}" +

                    "nav{" +
                    "gap:15px;" +
                    "}" +

                    "}" +

                    "</style>"
            );

            html.append("</head>");
            html.append("<body>");

            // =====================================================
            // HEADER
            // =====================================================

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

            // =====================================================
            // CONTEÚDO
            // =====================================================

            html.append(
                    "<main class='avaliacao-container'>"
            );

            html.append(
                    "<img class='capa-avaliacao' " +
                    "src='" +
                    escapar(capa) +
                    "' " +
                    "alt='Capa do jogo' " +
                    "onerror=\"this.src='https://cdn.cloudflare.steamstatic.com/steam/apps/" +
                    steamAppId +
                    "/header.jpg';\">"
            );

            html.append(
                    "<h2>" +
                    escapar(titulo) +
                    "</h2>"
            );

            html.append(
                    "<p>O que você achou desse jogo?</p>"
            );

            html.append(
                    "<form method='POST' action='avaliar'>"
            );

            html.append(
                    "<input type='hidden' " +
                    "name='steamAppId' " +
                    "value='" +
                    steamAppId +
                    "'>"
            );

            // =====================================================
            // NOTA
            // =====================================================

            html.append(
                    "<p><strong>Sua nota:</strong></p>"
            );

            html.append("<div class='estrelas'>");

            for (int i = 5; i >= 1; i--) {

                html.append(
                        "<input type='radio' " +
                        "id='estrela" +
                        i +
                        "' " +
                        "name='nota' " +
                        "value='" +
                        i +
                        "' required>"
                );

                html.append(
                        "<label for='estrela" +
                        i +
                        "'>★</label>"
                );
            }

            html.append("</div>");

            // =====================================================
            // HORAS
            // =====================================================

            html.append(
                    "<div class='horas-container'>" +

                    "<label>Horas jogadas</label>" +

                    "<input class='campo-horas' " +
                    "type='number' " +
                    "name='horasJogadas' " +
                    "min='0' " +
                    "step='0.1' " +
                    "placeholder='Ex: 25.5' " +
                    "required>" +

                    "</div>"
            );

            // =====================================================
            // RESENHA
            // =====================================================

            html.append(
                    "<textarea class='campo-resenha' " +
                    "name='comentario' " +
                    "placeholder='Escreva sua resenha...' " +
                    "required></textarea>"
            );

            // =====================================================
            // BOTÃO
            // =====================================================

            html.append(
                    "<button class='botao-postar' " +
                    "type='submit'>" +
                    "Postar avaliação" +
                    "</button>"
            );

            html.append("</form>");

            html.append("</main>");

            html.append("</body>");
            html.append("</html>");

            response.getWriter().println(
                    html.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect("biblioteca");
        }
    }

    // =============================================================
    // POST - SALVAR AVALIAÇÃO
    // =============================================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect("login.html");
            return;
        }

        Connection conexao = null;

        try {

            Usuario usuario =
                    (Usuario) sessao.getAttribute("usuario");

            int idUsuario =
                    usuario.getId();

            String appIdTexto =
                    request.getParameter("steamAppId");

            if (appIdTexto == null ||
                    appIdTexto.trim().isEmpty()) {

                response.sendRedirect("biblioteca");
                return;
            }

            int steamAppId =
                    Integer.parseInt(appIdTexto);

            double nota =
                    Double.parseDouble(
                            request.getParameter("nota")
                    );

            double horasJogadas =
                    Double.parseDouble(
                            request.getParameter(
                                    "horasJogadas"
                            )
                    );

            String comentario =
                    request.getParameter("comentario");

            // =====================================================
            // VALIDAR NOTA
            // =====================================================

            if (nota < 1 ||
                    nota > 5) {

                response.sendRedirect("biblioteca");
                return;
            }

            if (horasJogadas < 0) {

                horasJogadas = 0;
            }

            if (comentario == null) {

                comentario = "";
            }

            // =====================================================
            // VERIFICAR SE ESTÁ JOGANDO
            // =====================================================

            conexao =
                    Conexao.conectar();

            if (conexao == null) {

                throw new Exception(
                        "Banco não conectado."
                );
            }

            String sqlStatus =
                    "SELECT status " +
                    "FROM biblioteca " +
                    "WHERE id_usuario = ? " +
                    "AND steam_app_id = ?";

            PreparedStatement stmtStatus =
                    conexao.prepareStatement(
                            sqlStatus
                    );

            stmtStatus.setInt(
                    1,
                    idUsuario
            );

            stmtStatus.setInt(
                    2,
                    steamAppId
            );

            ResultSet rs =
                    stmtStatus.executeQuery();

            if (!rs.next()) {

                rs.close();
                stmtStatus.close();

                response.sendRedirect(
                        "biblioteca"
                );

                return;
            }

            String status =
                    rs.getString("status");

            rs.close();
            stmtStatus.close();

            if (!"jogando".equals(status)) {

                response.sendRedirect(
                        "biblioteca"
                );

                return;
            }

            // =====================================================
            // SALVAR / ATUALIZAR AVALIAÇÃO
            // =====================================================

            String sqlAvaliacao =
                    "INSERT INTO avaliacao " +
                    "(id_usuario, steam_app_id, nota, comentario, horas_jogadas) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT(id_usuario, steam_app_id) " +
                    "DO UPDATE SET " +
                    "nota=excluded.nota, " +
                    "comentario=excluded.comentario, " +
                    "horas_jogadas=excluded.horas_jogadas, " +
                    "data_avaliacao=CURRENT_TIMESTAMP";

            PreparedStatement stmt =
                    conexao.prepareStatement(
                            sqlAvaliacao
                    );

            stmt.setInt(
                    1,
                    idUsuario
            );

            stmt.setInt(
                    2,
                    steamAppId
            );

            stmt.setDouble(
                    3,
                    nota
            );

            stmt.setString(
                    4,
                    comentario
            );

            stmt.setDouble(
                    5,
                    horasJogadas
            );

            stmt.executeUpdate();

            stmt.close();

            // =====================================================
            // DEPOIS DA AVALIAÇÃO -> ZERADO
            // =====================================================

            String sqlBiblioteca =
                    "UPDATE biblioteca " +
                    "SET status='zerado', " +
                    "horas_jogadas=? " +
                    "WHERE id_usuario=? " +
                    "AND steam_app_id=?";

            PreparedStatement stmtBiblioteca =
                    conexao.prepareStatement(
                            sqlBiblioteca
                    );

            stmtBiblioteca.setDouble(
                    1,
                    horasJogadas
            );

            stmtBiblioteca.setInt(
                    2,
                    idUsuario
            );

            stmtBiblioteca.setInt(
                    3,
                    steamAppId
            );

            stmtBiblioteca.executeUpdate();

            stmtBiblioteca.close();

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "AVALIACAO SALVA!"
            );

            System.out.println(
                    "USUARIO: " +
                    idUsuario
            );

            System.out.println(
                    "STEAM APP ID: " +
                    steamAppId
            );

            System.out.println(
                    "NOTA: " +
                    nota
            );

            System.out.println(
                    "HORAS: " +
                    horasJogadas
            );

            System.out.println(
                    "STATUS: ZERADO"
            );

            System.out.println(
                    "========================================"
            );

            response.sendRedirect(
                    "biblioteca"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    "biblioteca"
            );

        } finally {

            try {

                if (conexao != null &&
                        !conexao.isClosed()) {

                    conexao.close();
                }

            } catch (Exception ignored) {
            }
        }
    }

    // =============================================================
    // BUSCAR NOME NA STEAM
    // =============================================================

    private String buscarNomeSteam(
            int steamAppId) {

        String nome =
                "Jogo Steam #" +
                steamAppId;

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

            conexao.setRequestMethod(
                    "GET"
            );

            conexao.setConnectTimeout(
                    5000
            );

            conexao.setReadTimeout(
                    5000
            );

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

            StringBuilder resposta =
                    new StringBuilder();

            String linha;

            while (
                    (linha =
                            leitor.readLine()) != null
            ) {

                resposta.append(
                        linha
                );
            }

            Pattern pattern =
                    Pattern.compile(
                            "\"name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
                    );

            Matcher matcher =
                    pattern.matcher(
                            resposta.toString()
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
                    "Erro ao buscar Steam: " +
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

    // =============================================================
    // ESCAPAR HTML
    // =============================================================

    private String escapar(
            String texto) {

        if (texto == null) {

            return "";
        }

        return texto
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}