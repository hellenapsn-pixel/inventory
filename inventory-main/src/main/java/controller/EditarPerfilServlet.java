package controller;

import dao.Conexao;
import dao.UsuarioDAO;
import model.Usuario;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/editar-perfil")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class EditarPerfilServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String PASTA_FOTOS = Conexao.getPastaFotos();

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

            Usuario usuarioSessao =
                    (Usuario) sessao.getAttribute("usuario");

            UsuarioDAO dao =
                    new UsuarioDAO();

            Usuario usuario =
                    dao.buscarPorId(
                            usuarioSessao.getId()
                    );

            if (usuario == null) {

                response.sendRedirect("login.html");
                return;
            }

            String nome =
                    usuario.getNome() == null
                            ? ""
                            : usuario.getNome();

            String username =
                    usuario.getUsername() == null
                            ? ""
                            : usuario.getUsername();

            String bio =
                    usuario.getBio() == null
                            ? ""
                            : usuario.getBio();

            String pais =
                    usuario.getPais() == null
                            ? ""
                            : usuario.getPais();

            String foto =
                    usuario.getFoto() == null
                            ? ""
                            : usuario.getFoto();

            String fotoUrl =
                    prepararFoto(
                            foto,
                            request
                    );

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            StringBuilder html =
                    new StringBuilder();

            html.append("<!DOCTYPE html>");
            html.append("<html lang='pt-BR'>");

            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            html.append("<title>Editar Perfil - Inventory</title>");

            html.append("<link rel='preconnect' href='https://fonts.googleapis.com'>");
            html.append("<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>");
            html.append("<link href='https://fonts.googleapis.com/css2?family=Rajdhani:wght@400;500;600;700&display=swap' rel='stylesheet'>");

            html.append("<style>");

            html.append("*{");
            html.append("box-sizing:border-box;");
            html.append("margin:0;");
            html.append("padding:0;");
            html.append("}");

            html.append("body{");
            html.append("font-family:'Rajdhani',sans-serif;");
            html.append("background:#0b0610;");
            html.append("color:#fff;");
            html.append("min-height:100vh;");
            html.append("}");

            html.append("header{");
            html.append("height:70px;");
            html.append("background:#100818;");
            html.append("border-bottom:1px solid #24142f;");
            html.append("display:flex;");
            html.append("align-items:center;");
            html.append("padding:0 40px;");
            html.append("}");

            html.append(".logo{");
            html.append("font-size:25px;");
            html.append("font-weight:700;");
            html.append("color:#fff;");
            html.append("text-decoration:none;");
            html.append("font-family:'Rajdhani',sans-serif;");
            html.append("}");

            html.append(".logo span{");
            html.append("color:#a855f7;");
            html.append("}");

            html.append(".voltar{");
            html.append("margin-left:auto;");
            html.append("text-decoration:none;");
            html.append("color:#d8b4fe;");
            html.append("font-size:14px;");
            html.append("}");

            html.append(".voltar:hover{");
            html.append("color:#fff;");
            html.append("}");

            html.append(".container{");
            html.append("width:100%;");
            html.append("max-width:850px;");
            html.append("margin:45px auto;");
            html.append("padding:0 20px;");
            html.append("}");

            html.append(".titulo{");
            html.append("margin-bottom:25px;");
            html.append("}");

            html.append(".titulo h1{");
            html.append("font-size:30px;");
            html.append("margin-bottom:5px;");
            html.append("}");

            html.append(".titulo p{");
            html.append("color:#a8a0ae;");
            html.append("font-size:14px;");
            html.append("}");

            html.append(".card{");
            html.append("background:#120b18;");
            html.append("border:1px solid #2a1935;");
            html.append("border-radius:18px;");
            html.append("padding:30px;");
            html.append("}");

            html.append(".foto-area{");
            html.append("display:flex;");
            html.append("align-items:center;");
            html.append("gap:25px;");
            html.append("padding-bottom:30px;");
            html.append("margin-bottom:25px;");
            html.append("border-bottom:1px solid #2a1935;");
            html.append("}");

            html.append(".foto-atual{");
            html.append("width:110px;");
            html.append("height:110px;");
            html.append("border-radius:50%;");
            html.append("object-fit:cover;");
            html.append("border:3px solid #8b5cf6;");
            html.append("background:#1d1027;");
            html.append("}");

            html.append(".sem-foto{");
            html.append("width:110px;");
            html.append("height:110px;");
            html.append("border-radius:50%;");
            html.append("background:linear-gradient(135deg,#7c3aed,#a855f7);");
            html.append("display:flex;");
            html.append("align-items:center;");
            html.append("justify-content:center;");
            html.append("font-size:40px;");
            html.append("font-weight:700;");
            html.append("border:3px solid #8b5cf6;");
            html.append("}");

            html.append(".foto-info h3{");
            html.append("font-size:17px;");
            html.append("margin-bottom:6px;");
            html.append("}");

            html.append(".foto-info p{");
            html.append("font-size:13px;");
            html.append("color:#958b9d;");
            html.append("}");

            html.append(".campo{");
            html.append("margin-bottom:20px;");
            html.append("}");

            html.append(".campo label{");
            html.append("display:block;");
            html.append("font-size:13px;");
            html.append("font-weight:600;");
            html.append("margin-bottom:8px;");
            html.append("color:#e9d5ff;");
            html.append("}");

            html.append(".campo input,");
            html.append(".campo textarea,");
            html.append(".campo select{");
            html.append("width:100%;");
            html.append("background:#0d0712;");
            html.append("border:1px solid #34203f;");
            html.append("border-radius:10px;");
            html.append("padding:12px 14px;");
            html.append("color:#fff;");
            html.append("font-family:'Rajdhani',sans-serif;");
            html.append("font-size:13px;");
            html.append("outline:none;");
            html.append("}");

            html.append(".campo input:focus,");
            html.append(".campo textarea:focus,");
            html.append(".campo select:focus{");
            html.append("border-color:#8b5cf6;");
            html.append("box-shadow:0 0 0 2px rgba(139,92,246,.12);");
            html.append("}");

            html.append(".campo textarea{");
            html.append("min-height:110px;");
            html.append("resize:vertical;");
            html.append("}");

            html.append(".arquivo{");
            html.append("padding:10px;");
            html.append("cursor:pointer;");
            html.append("}");

            html.append(".arquivo::file-selector-button{");
            html.append("background:#7c3aed;");
            html.append("color:#fff;");
            html.append("border:0;");
            html.append("border-radius:7px;");
            html.append("padding:8px 12px;");
            html.append("margin-right:10px;");
            html.append("font-family:'Rajdhani',sans-serif;");
            html.append("font-weight:600;");
            html.append("cursor:pointer;");
            html.append("}");

            html.append(".botoes{");
            html.append("display:flex;");
            html.append("gap:12px;");
            html.append("margin-top:30px;");
            html.append("}");

            html.append(".btn{");
            html.append("flex:1;");
            html.append("padding:13px 18px;");
            html.append("border-radius:10px;");
            html.append("font-family:'Rajdhani',sans-serif;");
            html.append("font-weight:600;");
            html.append("font-size:13px;");
            html.append("text-align:center;");
            html.append("text-decoration:none;");
            html.append("cursor:pointer;");
            html.append("}");

            html.append(".btn-salvar{");
            html.append("border:0;");
            html.append("background:linear-gradient(135deg,#7c3aed,#9333ea);");
            html.append("color:#fff;");
            html.append("}");

            html.append(".btn-salvar:hover{");
            html.append("background:linear-gradient(135deg,#8b5cf6,#a855f7);");
            html.append("}");

            html.append(".btn-cancelar{");
            html.append("border:1px solid #392348;");
            html.append("background:#1a0f22;");
            html.append("color:#d8b4fe;");
            html.append("}");

            html.append(".btn-cancelar:hover{");
            html.append("background:#251431;");
            html.append("}");

            html.append(".aviso{");
            html.append("font-size:12px;");
            html.append("color:#887d91;");
            html.append("margin-top:8px;");
            html.append("}");

            html.append("@media(max-width:600px){");

            html.append("header{");
            html.append("padding:0 20px;");
            html.append("}");

            html.append(".container{");
            html.append("margin:30px auto;");
            html.append("}");

            html.append(".card{");
            html.append("padding:20px;");
            html.append("}");

            html.append(".foto-area{");
            html.append("flex-direction:column;");
            html.append("text-align:center;");
            html.append("}");

            html.append(".botoes{");
            html.append("flex-direction:column;");
            html.append("}");

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

            html.append("<main class='container'>");

            html.append("<div class='titulo'>");
            html.append("<h1>Editar perfil</h1>");
            html.append("<p>Atualize suas informações e sua foto de perfil.</p>");
            html.append("</div>");

            html.append("<div class='card'>");

            html.append("<form method='POST' action='editar-perfil' enctype='multipart/form-data'>");

            html.append("<div class='foto-area'>");

            if (!fotoUrl.isEmpty()) {

                html.append(
                        "<img class='foto-atual' " +
                        "src='" +
                        escaparHtml(fotoUrl) +
                        "' " +
                        "alt='Foto atual'>"
                );

            } else {

                String inicial =
                        primeiraLetra(nome);

                html.append(
                        "<div class='sem-foto'>" +
                        escaparHtml(inicial) +
                        "</div>"
                );
            }

            html.append("<div class='foto-info'>");

            html.append("<h3>Foto de perfil</h3>");

            html.append(
                    "<p>Escolha uma imagem JPG, JPEG, PNG ou WEBP.</p>"
            );

            html.append(
                    "<p>O tamanho máximo é de 5 MB.</p>"
            );

            html.append("</div>");

            html.append("</div>");

            html.append("<div class='campo'>");

            html.append("<label for='foto'>Nova foto</label>");

            html.append(
                    "<input " +
                    "class='arquivo' " +
                    "type='file' " +
                    "id='foto' " +
                    "name='foto' " +
                    "accept='image/png,image/jpeg,image/webp'>"
            );

            html.append(
                    "<div class='aviso'>" +
                    "Deixe vazio caso não queira alterar sua foto." +
                    "</div>"
            );

            html.append("</div>");

            html.append("<div class='campo'>");

            html.append("<label for='nome'>Nome</label>");

            html.append(
                    "<input " +
                    "type='text' " +
                    "id='nome' " +
                    "name='nome' " +
                    "value='" +
                    escaparHtml(nome) +
                    "' " +
                    "required>"
            );

            html.append("</div>");

            html.append("<div class='campo'>");

            html.append("<label for='username'>Nome de usuário</label>");

            html.append(
                    "<input " +
                    "type='text' " +
                    "id='username' " +
                    "name='username' " +
                    "value='" +
                    escaparHtml(username) +
                    "' " +
                    "required>"
            );

            html.append("</div>");

            html.append("<div class='campo'>");

            html.append("<label for='bio'>Biografia</label>");

            html.append(
                    "<textarea " +
                    "id='bio' " +
                    "name='bio' " +
                    "placeholder='Fale um pouco sobre você...'>" +
                    escaparHtml(bio) +
                    "</textarea>"
            );

            html.append("</div>");

            html.append("<div class='campo'>");

            html.append("<label for='pais'>País</label>");

            html.append(
                    "<input " +
                    "type='text' " +
                    "id='pais' " +
                    "name='pais' " +
                    "value='" +
                    escaparHtml(pais) +
                    "'>"
            );

            html.append("</div>");

            html.append("<div class='botoes'>");

            html.append(
                    "<a class='btn btn-cancelar' href='perfil'>" +
                    "Cancelar" +
                    "</a>"
            );

            html.append(
                    "<button class='btn btn-salvar' type='submit'>" +
                    "Salvar alterações" +
                    "</button>"
            );

            html.append("</div>");

            html.append("</form>");

            html.append("</div>");

            html.append("</main>");

            html.append("</body>");
            html.append("</html>");

            response.getWriter().write(
                    html.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao carregar edição do perfil."
            );
        }
    }

    @Override
    protected void doPost(
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

            Usuario usuarioSessao =
                    (Usuario) sessao.getAttribute("usuario");

            int idUsuario =
                    usuarioSessao.getId();

            UsuarioDAO dao =
                    new UsuarioDAO();

            Usuario usuario =
                    dao.buscarPorId(idUsuario);

            if (usuario == null) {

                response.sendRedirect("login.html");
                return;
            }

            String nome =
                    limpar(
                            request.getParameter("nome")
                    );

            String username =
                    limpar(
                            request.getParameter("username")
                    );

            String bio =
                    limpar(
                            request.getParameter("bio")
                    );

            String pais =
                    limpar(
                            request.getParameter("pais")
                    );

            String foto =
                    usuario.getFoto();

            Part parteFoto =
                    request.getPart("foto");

            /*
             * Só altera a foto se o usuário
             * realmente escolher uma nova.
             */
            if (parteFoto != null &&
                    parteFoto.getSize() > 0) {

                String nomeOriginal =
                        parteFoto.getSubmittedFileName();

                if (nomeOriginal == null ||
                        nomeOriginal.trim().isEmpty()) {

                    throw new Exception(
                            "Nome da imagem inválido."
                    );
                }

                nomeOriginal =
                        new File(nomeOriginal)
                                .getName();

                String nomeMinusculo =
                        nomeOriginal.toLowerCase();

                String extensao = "";

                if (nomeMinusculo.endsWith(".jpg")) {

                    extensao = ".jpg";

                } else if (
                        nomeMinusculo.endsWith(".jpeg")) {

                    extensao = ".jpeg";

                } else if (
                        nomeMinusculo.endsWith(".png")) {

                    extensao = ".png";

                } else if (
                        nomeMinusculo.endsWith(".webp")) {

                    extensao = ".webp";

                } else {

                    throw new Exception(
                            "Formato de imagem não permitido. " +
                            "Use JPG, JPEG, PNG ou WEBP."
                    );
                }

                String nomeArquivo =
                        "perfil_" +
                        idUsuario +
                        "_" +
                        System.currentTimeMillis() +
                        extensao;

                File pasta =
                        new File(PASTA_FOTOS);

                if (!pasta.exists() &&
                        !pasta.mkdirs()) {

                    throw new Exception(
                            "Não foi possível criar a pasta das fotos."
                    );
                }

                File arquivoFoto =
                        new File(
                                pasta,
                                nomeArquivo
                        );

                try (
                        InputStream entrada =
                                parteFoto.getInputStream()
                ) {

                    Files.copy(
                            entrada,
                            arquivoFoto.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                foto = nomeArquivo;

                System.out.println(
                        "================================="
                );

                System.out.println(
                        "FOTO DE PERFIL SALVA:"
                );

                System.out.println(
                        arquivoFoto.getAbsolutePath()
                );

                System.out.println(
                        "NOME SALVO NO BANCO:"
                );

                System.out.println(foto);

                System.out.println(
                        "================================="
                );
            }

            String sql =
                    "UPDATE usuario SET " +
                    "nome = ?, " +
                    "username = ?, " +
                    "bio = ?, " +
                    "pais = ?, " +
                    "foto = ? " +
                    "WHERE id = ?";

            try (
                    Connection conexao =
                            Conexao.conectar();

                    PreparedStatement ps =
                            conexao.prepareStatement(sql)
            ) {

                ps.setString(1, nome);
                ps.setString(2, username);
                ps.setString(3, bio);
                ps.setString(4, pais);
                ps.setString(5, foto);
                ps.setInt(6, idUsuario);

                ps.executeUpdate();
            }

            /*
             * Atualiza os dados do usuário
             * que estão na sessão.
             */
            usuarioSessao.setNome(nome);
            usuarioSessao.setUsername(username);
            usuarioSessao.setBio(bio);
            usuarioSessao.setPais(pais);
            usuarioSessao.setFoto(foto);

            sessao.setAttribute(
                    "usuario",
                    usuarioSessao
            );

            response.sendRedirect("perfil");

        } catch (Exception e) {

            e.printStackTrace();

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            response.getWriter().println(
                    "<h2>Erro ao atualizar o perfil</h2>"
            );

            response.getWriter().println(
                    "<p>" +
                    escaparHtml(
                            e.getMessage() == null
                                    ? "Erro desconhecido."
                                    : e.getMessage()
                    ) +
                    "</p>"
            );

            response.getWriter().println(
                    "<a href='editar-perfil'>Voltar</a>"
            );
        }
    }

    private String prepararFoto(
            String foto,
            HttpServletRequest request) {

        if (foto == null ||
                foto.trim().isEmpty()) {

            return "";
        }

        foto = foto.trim();

        if (foto.startsWith("http://") ||
                foto.startsWith("https://")) {

            return foto;
        }

        if (foto.startsWith("/")) {

            return request.getContextPath() + foto;
        }

        return request.getContextPath()
                + "/foto-perfil?arquivo="
                + foto;
    }

    private String limpar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.trim();
    }

    private String primeiraLetra(String nome) {

        if (nome == null ||
                nome.trim().isEmpty()) {

            return "?";
        }

        return nome
                .trim()
                .substring(0, 1)
                .toUpperCase();
    }

    private String escaparHtml(String texto) {

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