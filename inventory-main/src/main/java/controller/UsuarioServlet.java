package controller;

import dao.CriarBanco;
import dao.UsuarioDAO;
import model.Usuario;
import util.PasswordUtil;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
@WebServlet("/usuario")
public class UsuarioServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("=================================");
        System.out.println("USUARIOSERVLET INICIADO");
        System.out.println("=================================");
        CriarBanco.criarTabela();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nome = limpar(request.getParameter("nome"));
        String username = limpar(request.getParameter("username"));
        String email = limpar(request.getParameter("email")).toLowerCase();
        String senha = request.getParameter("senha");

        String dataNascimento =
                limpar(request.getParameter("dataNascimento"));
        String pais =
                limpar(request.getParameter("pais"));
        String plataforma =
                limpar(request.getParameter("plataforma"));
        String bio =
                limpar(request.getParameter("bio"));

        if (nome.isEmpty()
                || username.isEmpty()
                || email.isEmpty()
                || senha == null
                || senha.trim().isEmpty()) {

            mostrarErro(
                    response,
                    "Preencha todos os campos obrigatórios."
            );
            return;
        }

        if (!emailValido(email)) {
            mostrarErro(
                    response,
                    "Digite um e-mail válido."
            );
            return;
        }

        if (senha.length() < 6) {
            mostrarErro(
                    response,
                    "A senha deve ter pelo menos 6 caracteres."
            );
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        Usuario usuarioExistente =
                usuarioDAO.buscarPorEmail(email);

        if (usuarioExistente != null) {
            mostrarErro(
                    response,
                    "Este e-mail já está cadastrado."
            );
            return;
        }

        Usuario usernameExistente =
                usuarioDAO.buscarPorUsername(username);

        if (usernameExistente != null) {
            mostrarErro(
                    response,
                    "Este nome de usuário já está em uso."
            );
            return;
        }

        Part arquivoFoto = request.getPart("foto");
        String nomeFoto = null;

        if (arquivoFoto != null && arquivoFoto.getSize() > 0) {

            String nomeOriginal =
                    arquivoFoto.getSubmittedFileName();

            if (nomeOriginal == null
                    || nomeOriginal.trim().isEmpty()) {
                nomeOriginal = "foto.jpg";
            }

            nomeOriginal =
                    new File(nomeOriginal).getName();

            String nomeMinusculo =
                    nomeOriginal.toLowerCase();

            if (!nomeMinusculo.endsWith(".jpg")
                    && !nomeMinusculo.endsWith(".jpeg")
                    && !nomeMinusculo.endsWith(".png")
                    && !nomeMinusculo.endsWith(".webp")) {

                mostrarErro(
                        response,
                        "Formato de foto inválido. "
                        + "Use JPG, JPEG, PNG ou WEBP."
                );
                return;
            }

            nomeFoto =
                    System.currentTimeMillis()
                    + "_"
                    + nomeOriginal;

            String uploadsPath =
                    System.getenv("UPLOADS_PATH");

            String caminhoBase;

            if (uploadsPath != null
                    && !uploadsPath.trim().isEmpty()) {

                caminhoBase =
                        uploadsPath
                        + File.separator
                        + "perfil";

            } else {

                String sistema =
                        System.getProperty("os.name")
                                .toLowerCase();

                if (sistema.contains("win")) {
                    caminhoBase =
                            "C:\\GameBoxdUploads\\data\\perfil";
                } else {
                    caminhoBase =
                            "/app/data/perfil";
                }
            }

            File pasta = new File(caminhoBase);

            if (!pasta.exists() && !pasta.mkdirs()) {
                mostrarErro(
                        response,
                        "Não foi possível criar a pasta da foto."
                );
                return;
            }

            File arquivo =
                    new File(pasta, nomeFoto);

            arquivoFoto.write(
                    arquivo.getAbsolutePath()
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);

        // A senha é armazenada somente como hash BCrypt.
        usuario.setSenha(
                PasswordUtil.hash(senha)
        );

        usuario.setFoto(nomeFoto);
        usuario.setBio(bio);
        usuario.setDataNascimento(dataNascimento);
        usuario.setPais(pais);
        usuario.setPlataformaFavorita(plataforma);

        boolean salvo =
                usuarioDAO.cadastrar(usuario);

        if (!salvo) {
            mostrarErro(
                    response,
                    "Não foi possível realizar o cadastro."
            );
            return;
        }

        response.sendRedirect(
                request.getContextPath()
                + "/login.html?cadastro=sucesso"
        );
    }

    private void mostrarErro(
            HttpServletResponse response,
            String mensagem)
            throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        response.getWriter().println(
                "<!DOCTYPE html>"
                + "<html lang='pt-BR'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' "
                + "content='width=device-width, initial-scale=1.0'>"
                + "<title>Cadastro - Inventory</title>"
                + "<style>"
                + "body{"
                + "background:#100814;"
                + "color:white;"
                + "font-family:Arial,sans-serif;"
                + "display:flex;"
                + "justify-content:center;"
                + "align-items:center;"
                + "min-height:100vh;"
                + "margin:0;"
                + "}"
                + ".box{"
                + "background:#1d1226;"
                + "padding:35px;"
                + "border-radius:15px;"
                + "text-align:center;"
                + "max-width:500px;"
                + "}"
                + "h2{color:#c084fc;}"
                + "p{color:#ddd;line-height:1.5;}"
                + "a{color:#a855f7;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='box'>"
                + "<h2>Não foi possível continuar</h2>"
                + "<p>"
                + escaparHTML(mensagem)
                + "</p>"
                + "<a href='cadastro.html'>"
                + "Voltar para cadastro"
                + "</a>"
                + "</div>"
                + "</body>"
                + "</html>"
        );
    }

    private String limpar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }

    private boolean emailValido(String email) {
        return email != null
                && email.matches(
                        "^[A-Za-z0-9+_.-]+@"
                        + "[A-Za-z0-9.-]+$"
                );
    }

    private String escaparHTML(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace(""", "&quot;")
                .replace("'", "&#39;");
    }
}
