package controller;

import dao.Conexao;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Download de um snapshot consistente do inventory.db.
 *
 * GET /admin/baixar-banco
 * Autenticação: header X-Admin-Key igual à variável de ambiente ADMIN_KEY.
 * (A chave não é aceita por query string para não vazar em logs.)
 */
@WebServlet("/admin/baixar-banco")
public class BaixarBancoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String esperada = System.getenv("ADMIN_KEY");
        if (esperada == null || esperada.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String informada = request.getHeader("X-Admin-Key");
        if (informada == null || !MessageDigest.isEqual(
                esperada.getBytes(StandardCharsets.UTF_8),
                informada.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        File copia = File.createTempFile("inventory-", ".db");
        copia.delete(); // VACUUM INTO exige que o arquivo não exista
        try (Connection c = Conexao.conectar();
             Statement st = c.createStatement()) {
            st.execute("VACUUM INTO '" + copia.getAbsolutePath().replace("'", "''") + "'");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"inventory.db\"");
            response.setContentLengthLong(copia.length());
            try (OutputStream out = response.getOutputStream()) {
                Files.copy(copia.toPath(), out);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            copia.delete();
        }
    }
}
