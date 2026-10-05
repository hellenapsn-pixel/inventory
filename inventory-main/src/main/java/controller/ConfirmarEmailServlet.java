package controller;

import dao.UsuarioDAO;
import model.Usuario;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        String email = parametro(request, "email").toLowerCase();
        String codigo = parametro(request, "codigo");

        if (email.isEmpty() || codigo.isEmpty()) {
            response.sendRedirect("cadastro.html?erro=codigo_invalido");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.confirmarEmail(email, codigo);

            if (usuario == null) {
                response.sendRedirect("cadastro.html?erro=codigo_invalido");
                return;
            }

            response.sendRedirect("login.html?cadastro=confirmado");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("cadastro.html?erro=servidor");
        }
    }

    private String parametro(HttpServletRequest request, String nome) {
        String valor = request.getParameter(nome);
        return valor == null ? "" : valor.trim();
    }
}
