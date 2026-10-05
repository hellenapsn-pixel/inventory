package controller;

import dao.Conexao;
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

@WebServlet("/cadastro")

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)

public class CadastroServlet extends HttpServlet {

    private static final String PASTA_FOTOS = Conexao.getPastaFotos();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        System.out.println("=================================");
        System.out.println("CADASTRO FOI CHAMADO");
        System.out.println("=================================");

        try {

            // =========================================
            // PEGAR DADOS
            // =========================================

            String nome =
                    valor(request, "nome");

            String username =
                    valor(request, "username");

            String email =
                    valor(request, "email").toLowerCase();

            String senha =
                    valor(request, "senha");

            String pais =
                    valor(request, "pais");

            String plataforma =
                    valor(request, "plataforma");

            String bio =
                    valor(request, "bio");

            // =========================================
            // VALIDAR CAMPOS
            // =========================================

            if (nome.isEmpty()
                    || username.isEmpty()
                    || email.isEmpty()
                    || senha.isEmpty()) {

                response.sendRedirect(
                        "cadastro.html?erro=campos"
                );

                return;
            }

            // =========================================
            // VALIDAR EMAIL
            // =========================================

            if (!email.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                response.sendRedirect(
                        "cadastro.html?erro=email"
                );

                return;
            }

            UsuarioDAO dao =
                    new UsuarioDAO();

            // =========================================
            // VERIFICAR EMAIL
            // =========================================

            if (dao.buscarPorEmail(email) != null) {

                response.sendRedirect(
                        "cadastro.html?erro=email_existente"
                );

                return;
            }

            // =========================================
            // VERIFICAR USERNAME
            // =========================================

            if (dao.buscarPorUsername(username) != null) {

                response.sendRedirect(
                        "cadastro.html?erro=username"
                );

                return;
            }

            if (senha.length() < 6) {

                response.sendRedirect(
                        "cadastro.html?erro=senha"
                );

                return;
            }

            // =========================================
            // FOTO
            // =========================================

            String nomeFoto = "";

            Part arquivo =
                    request.getPart("foto");

            if (arquivo != null
                    && arquivo.getSize() > 0) {

                String nomeOriginal =
                        arquivo.getSubmittedFileName();

                if (nomeOriginal == null
                        || nomeOriginal.trim().isEmpty()) {

                    response.sendRedirect(
                            "cadastro.html?erro=foto"
                    );

                    return;
                }

                nomeOriginal =
                        new File(nomeOriginal)
                                .getName();

                String extensao = "";

                int ponto =
                        nomeOriginal.lastIndexOf(".");

                if (ponto >= 0) {

                    extensao =
                            nomeOriginal
                                    .substring(ponto)
                                    .toLowerCase();
                }

                if (!extensao.equals(".jpg")
                        && !extensao.equals(".jpeg")
                        && !extensao.equals(".png")
                        && !extensao.equals(".webp")) {

                    response.sendRedirect(
                            "cadastro.html?erro=formato"
                    );

                    return;
                }

                File diretorio =
                        new File(PASTA_FOTOS);

                if (!diretorio.exists()) {

                    if (!diretorio.mkdirs()) {

                        throw new Exception(
                                "Não foi possível criar a pasta de fotos."
                        );
                    }
                }

                nomeFoto =
                        "perfil_"
                        + System.currentTimeMillis()
                        + extensao;

                File arquivoFinal =
                        new File(
                                diretorio,
                                nomeFoto
                        );

                arquivo.write(
                        arquivoFinal.getAbsolutePath()
                );
            }

            // =========================================
            // CRIAR USUARIO
            // =========================================

            Usuario usuario =
                    new Usuario();

            usuario.setNome(nome);

            usuario.setUsername(username);

            usuario.setEmail(email);

            usuario.setSenha(PasswordUtil.hash(senha));

            usuario.setFoto(nomeFoto);

            usuario.setBio(bio);

            usuario.setDataNascimento("");

            usuario.setPais(pais);

            usuario.setPlataformaFavorita(
                    plataforma
            );

            // =========================================
            // SALVAR USUARIO
            // =========================================

            boolean salvo =
                    dao.cadastrar(usuario);

            if (!salvo) {

                System.out.println(
                        "ERRO: usuario nao foi salvo."
                );

                response.sendRedirect(
                        "cadastro.html?erro=salvar"
                );

                return;
            }

            System.out.println(
                    "USUARIO CADASTRADO: " + username
            );

            // =========================================
            // IR PARA LOGIN
            // =========================================

            response.sendRedirect(
                    "login.html?cadastro=sucesso"
            );


        } catch (Exception e) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERRO NO CADASTRO"
            );

            System.out.println(
                    "================================="
            );

            e.printStackTrace();

            response.sendRedirect(
                    "cadastro.html?erro=servidor"
            );
        }
    }

    private String valor(
            HttpServletRequest request,
            String nome) {

        String valor =
                request.getParameter(nome);

        if (valor == null) {

            return "";
        }

        return valor.trim();
    }
}