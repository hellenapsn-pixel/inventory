package controller;

import dao.Conexao;
import model.Usuario;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Consulta administrativa somente leitura do SQLite.
 * Configure INVENTORY_ADMIN_EMAILS com os e-mails autorizados, separados por vírgula.
 */
@WebServlet("/admin/banco")
public class VisualizarBancoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Content-Security-Policy",
                "default-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; frame-ancestors 'none'");

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("usuario") instanceof Usuario)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Faça login para continuar.");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String email = usuario.getEmail();
        String allowlist = System.getenv("INVENTORY_ADMIN_EMAILS");

        if (allowlist == null || allowlist.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "A página administrativa ainda não foi configurada.");
            return;
        }
        if (!emailPermitido(email, allowlist)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso não autorizado.");
            return;
        }

        try (PrintWriter out = response.getWriter();
             Connection connection = Conexao.conectar()) {
            if (connection == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Não foi possível conectar ao SQLite.");
                return;
            }

            out.println("<!doctype html><html lang='pt-BR'><head><meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>Banco Inventory</title><style>");
            out.println("body{font-family:system-ui,Arial;margin:0;padding:14px;background:#f3f4f6;color:#202124}");
            out.println("header,.panel{background:#fff;border:1px solid #ddd;border-radius:12px;padding:14px;margin-bottom:14px}");
            out.println("h1{font-size:1.3rem}h2{font-size:1.05rem;overflow-wrap:anywhere}");
            out.println(".note{font-size:.9rem;color:#555}.wrap{overflow:auto;max-width:100%}");
            out.println("table{border-collapse:collapse;width:100%;font-size:.86rem;background:white}");
            out.println("th,td{border:1px solid #ddd;padding:8px;text-align:left;vertical-align:top;overflow-wrap:anywhere;max-width:300px}");
            out.println("th{background:#edf0f4;position:sticky;top:0}.count{font-size:.85rem;color:#555}");
            out.println("</style></head><body><header><h1>Inventory — banco SQLite</h1>");
            out.println("<p class='note'>Somente leitura. Senhas criptografadas são exibidas apenas na tabela usuario; tokens e segredos continuam ocultos.</p>");
            out.println("<p class='note'>Conta autorizada: " + escapeHtml(email) + "</p>");
            out.println("<p><a href='" + escapeHtml(request.getContextPath())
                    + "/admin/banco'>Atualizar dados</a></p></header>");

            List<String> tables = new ArrayList<String>();
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(
                         "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
                while (rs.next()) {
                    tables.add(rs.getString("name"));
                }
            }

            if (tables.isEmpty()) {
                out.println("<section class='panel'>Nenhuma tabela encontrada.</section>");
            }

            for (String table : tables) {
                renderTable(out, connection, table);
            }
            out.println("</body></html>");
        } catch (Exception e) {
            getServletContext().log("Erro ao consultar o banco pela página administrativa.", e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Não foi possível carregar as tabelas. Consulte os logs do servidor.");
            }
        }
    }

    private boolean emailPermitido(String email, String allowlist) {
        if (email == null) {
            return false;
        }
        String alvo = email.trim().toLowerCase(Locale.ROOT);
        for (String permitido : allowlist.split(",")) {
            if (alvo.equals(permitido.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void renderTable(PrintWriter out, Connection connection, String table)
            throws Exception {
        String nomeSeguro = table.replace("\"", "\"\"");
        String sql = "SELECT * FROM \"" + nomeSeguro + "\";";

        out.println("<section class='panel'><h2>" + escapeHtml(table) + "</h2><div class='wrap'><table><thead><tr>");
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            List<Integer> colunasVisiveis = new ArrayList<Integer>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String coluna = meta.getColumnLabel(i).toLowerCase(Locale.ROOT);
                boolean tabelaUsuario = "usuario".equalsIgnoreCase(table);
                boolean colunaSenha = coluna.contains("senha") || coluna.contains("password");
                if ((colunaSenha && !tabelaUsuario)
                        || coluna.contains("token") || coluna.contains("secret")) {
                    continue;
                }
                colunasVisiveis.add(i);
                out.println("<th>" + escapeHtml(meta.getColumnLabel(i)) + "</th>");
            }
            out.println("</tr></thead><tbody>");
            int quantidade = 0;
            while (rs.next()) {
                quantidade++;
                out.println("<tr>");
                for (Integer coluna : colunasVisiveis) {
                    String valor = rs.getString(coluna);
                    out.println("<td>" + escapeHtml(valor == null ? "—" : valor) + "</td>");
                }
                out.println("</tr>");
            }
            if (quantidade == 0) {
                out.println("<tr><td colspan='" + Math.max(1, colunasVisiveis.size())
                        + "'>Nenhum registro nesta tabela.</td></tr>");
            }
            out.println("</tbody></table></div><p class='count'>" + quantidade
                    + " registro(s) exibido(s)"
                    + ".</p></section>");
        }
    }

    private String escapeHtml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
