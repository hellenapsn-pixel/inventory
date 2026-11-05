package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dao.Conexao;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/destaques")
public class DestaquesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        JsonArray lista = new JsonArray();
        String sql = "SELECT steam_app_id,titulo,plataforma,data_lancamento,capa " +
                "FROM lancamento_steam ORDER BY atualizado_em DESC LIMIT 8";
        try (Connection c = Conexao.conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                JsonObject jogo = new JsonObject();
                jogo.addProperty("id", rs.getInt("steam_app_id"));
                jogo.addProperty("titulo", rs.getString("titulo"));
                jogo.addProperty("plataforma", rs.getString("plataforma"));
                jogo.addProperty("data", rs.getString("data_lancamento"));
                jogo.addProperty("capa", rs.getString("capa"));
                lista.add(jogo);
            }
            response.getWriter().write(lista.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("[]");
            getServletContext().log("Não foi possível carregar os lançamentos do Inventory.", e);
        }
    }
}
