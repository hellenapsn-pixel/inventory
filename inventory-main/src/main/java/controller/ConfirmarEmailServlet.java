package controller;

import dao.UsuarioDAO;
import model.Usuario;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Confirma o cadastro pendente a partir do e-mail e do código.
 * Aceita GET (link) ou POST.
 */
@WebServlet("/confirmar-email")
public class ConfirmarEmailServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        confirmar(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        confirmar(request, response);
    }

    private void confirmar(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String codigo = request.getParameter("codigo");

        email = email == null ? "" : email.trim().toLowerCase();
        codigo = codigo == null ? "" : codigo.trim();

        if (email.isEmpty() || codigo.isEmpty()) {
            response.sendRedirect("cadastro.html?erro=confirmacao");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.confirmarEmail(email, codigo);

            if (usuario == null) {
                response.sendRedirect(
                        "cadastro.html?erro=codigo_invalido");
                return;
            }

            System.out.println(
                    "E-mail confirmado. Usuário criado: "
                    + URLEncoder.encode(usuario.getUsername(), "UTF-8"));

            response.sendRedirect("login.html?cadastro=confirmado");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("cadastro.html?erro=servidor");
        }
    }
}
