package controller;

import dao.UsuarioDAO;
import model.Usuario;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        System.out.println("==============================");
        System.out.println("LOGIN FOI CHAMADO");
        System.out.println("==============================");

        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        if (email == null) {
            email = "";
        }

        if (senha == null) {
            senha = "";
        }

        email = email.trim().toLowerCase();

        System.out.println("E-mail: " + email);

        if (email.isEmpty() || senha.trim().isEmpty()) {

            response.sendRedirect(
                    "login.html?erro=preencha"
            );

            return;
        }

        try {

            UsuarioDAO usuarioDAO = new UsuarioDAO();

            Usuario usuario = usuarioDAO.login(
                    email,
                    senha
            );

            if (usuario == null) {

                System.out.println(
                        "E-MAIL OU SENHA INCORRETOS!"
                );

                response.sendRedirect(
                        "login.html?erro=login"
                );

                return;
            }

            // =========================================
            // CRIAR SESSÃO
            // =========================================

            HttpSession sessao =
                    request.getSession(true);

            sessao.setAttribute(
                    "usuario",
                    usuario
            );

            System.out.println("==============================");
            System.out.println(
                    "LOGIN REALIZADO COM SUCESSO!"
            );
            System.out.println(
                    "ID: " + usuario.getId()
            );
            System.out.println(
                    "NOME: " + usuario.getNome()
            );
            System.out.println(
                    "USERNAME: " + usuario.getUsername()
            );
            System.out.println(
                    "EMAIL: " + usuario.getEmail()
            );
            System.out.println("==============================");

            // =========================================
            // IR PARA O PERFIL
            // =========================================

            response.sendRedirect("index.html");

        } catch (Exception e) {

            System.out.println("==============================");
            System.out.println("ERRO NO LOGIN:");
            e.printStackTrace();
            System.out.println("==============================");

            response.sendRedirect(
                    "login.html?erro=servidor"
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect("login.html");
    }
}