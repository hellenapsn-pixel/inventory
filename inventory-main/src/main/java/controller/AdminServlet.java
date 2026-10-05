package controller;

import dao.CriarBanco;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Endpoint administrativo para carregar o catálogo de jogos sob demanda.
 *
 * POST /admin/carregar-catalogo
 * Autenticação: chave secreta enviada no header X-Admin-Key (ou parâmetro
 * adminKey), comparada com a variável de ambiente ADMIN_KEY ou a propriedade
 * de sistema admin.key. Se nenhuma chave estiver configurada, o endpoint
 * permanece desabilitado.
 */
@WebServlet("/admin/carregar-catalogo")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String chaveConfigurada = chaveConfigurada();

        if (chaveConfigurada == null || chaveConfigurada.isEmpty()) {
            responder(response, HttpServletResponse.SC_FORBIDDEN,
                    "{\"status\":\"erro\",\"mensagem\":\"Endpoint administrativo desabilitado.\"}");
            return;
        }

        String chaveInformada = request.getHeader("X-Admin-Key");
        if (chaveInformada == null) {
            chaveInformada = request.getParameter("adminKey");
        }

        if (!chavesIguais(chaveConfigurada, chaveInformada)) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"status\":\"erro\",\"mensagem\":\"Não autorizado.\"}");
            return;
        }

        try {
            boolean carregado = CriarBanco.carregarCatalogoOpcional();

            if (carregado) {
                responder(response, HttpServletResponse.SC_OK,
                        "{\"status\":\"ok\",\"mensagem\":\"Catálogo carregado com sucesso.\"}");
            } else {
                responder(response, HttpServletResponse.SC_OK,
                        "{\"status\":\"ja_carregado\",\"mensagem\":\"Catálogo já estava carregado.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            responder(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "{\"status\":\"erro\",\"mensagem\":\"Falha ao carregar o catálogo.\"}");
        }
    }

    private static String chaveConfigurada() {
        String chave = System.getenv("ADMIN_KEY");
        if (chave == null || chave.isEmpty()) {
            chave = System.getProperty("admin.key");
        }
        return chave;
    }

    private static boolean chavesIguais(String esperada, String informada) {
        if (informada == null) {
            return false;
        }
        return MessageDigest.isEqual(
                esperada.getBytes(StandardCharsets.UTF_8),
                informada.getBytes(StandardCharsets.UTF_8));
    }

    private static void responder(
            HttpServletResponse response,
            int status,
            String json) throws IOException {
        response.setStatus(status);
        response.getWriter().write(json);
    }
}
