package controller;

import dao.Conexao;
import dao.CriarBanco;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Endpoints administrativos.
 * Autenticação via header X-Admin-Key, comparado com a variável de
 * ambiente ADMIN_KEY ou a propriedade de sistema admin.key.
 */
@WebServlet("/admin/carregar-catalogo")
public class AdminServlet extends HttpServlet {

    private static final String HEADER_CHAVE = "X-Admin-Key";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        String origem = request.getRemoteAddr();
        String chaveConfigurada = obterChaveConfigurada();

        if (chaveConfigurada == null) {
            log("Chave administrativa não configurada. Acesso negado. IP: "
                    + origem);
            responder(response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    false,
                    "Chave administrativa não configurada.");
            return;
        }

        String chaveRecebida = request.getHeader(HEADER_CHAVE);

        if (!chavesIguais(chaveConfigurada, chaveRecebida)) {
            log("Tentativa não autorizada em /admin/carregar-catalogo. IP: "
                    + origem);
            responder(response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    false,
                    "Não autorizado.");
            return;
        }

        log("Carga do catálogo solicitada. IP: " + origem);

        try (Connection conexao = Conexao.conectar()) {

            if (conexao == null) {
                throw new IllegalStateException(
                        "Não foi possível conectar ao SQLite.");
            }

            CriarBanco.carregarCatalogo(conexao);

            log("Carga do catálogo concluída com sucesso.");
            responder(response,
                    HttpServletResponse.SC_OK,
                    true,
                    "Catálogo carregado com sucesso.");

        } catch (Exception e) {
            log("Erro ao carregar o catálogo: " + e.getMessage(), e);
            responder(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    false,
                    "Erro ao carregar o catálogo.");
        }
    }

    private String obterChaveConfigurada() {
        String chave = System.getenv("ADMIN_KEY");

        if (chave == null || chave.trim().isEmpty()) {
            chave = System.getProperty("admin.key");
        }

        if (chave == null || chave.trim().isEmpty()) {
            return null;
        }

        return chave;
    }

    private boolean chavesIguais(String esperada, String recebida) {
        if (recebida == null) {
            return false;
        }

        return MessageDigest.isEqual(
                esperada.getBytes(StandardCharsets.UTF_8),
                recebida.getBytes(StandardCharsets.UTF_8));
    }

    private void responder(
            HttpServletResponse response,
            int status,
            boolean sucesso,
            String mensagem)
            throws IOException {

        response.setStatus(status);
        response.getWriter().write(
                "{\"sucesso\":" + sucesso
                        + ",\"mensagem\":\"" + mensagem.replace("\"", "\\\"")
                        + "\"}");
    }
}
