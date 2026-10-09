package controller;

import dao.Conexao;
import model.Usuario;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/adicionar-biblioteca")
public class AdicionarBibliotecaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private int buscarSteamAppIdPorTitulo(
            Connection conexao,
            String titulo)
            throws Exception {

        String sql =
                "SELECT steam_app_id " +
                "FROM jogo " +
                "WHERE titulo = ? COLLATE NOCASE " +
                "LIMIT 1";

        try (PreparedStatement ps =
                     conexao.prepareStatement(sql)) {

            ps.setString(1, titulo);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt("steam_app_id");
                }
            }
        }

        return 0;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao =
                request.getSession(false);

        // ==========================================
        // VERIFICAR LOGIN
        // ==========================================

        if (sessao == null ||
                sessao.getAttribute("usuario") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html"
            );

            return;
        }

        // ==========================================
        // PEGAR STEAM APP ID
        // ==========================================

        String idTexto =
                request.getParameter("id");

        String tituloTexto =
                request.getParameter("titulo");

        boolean temTitulo =
                tituloTexto != null &&
                !tituloTexto.trim().isEmpty();

        if (!temTitulo &&
                (idTexto == null ||
                idTexto.trim().isEmpty())) {

            response.sendRedirect(
                    request.getContextPath() + "/jogos"
            );

            return;
        }

        Connection conexao = null;
        PreparedStatement stmtVerificar = null;
        PreparedStatement stmtInserir = null;
        PreparedStatement stmtAtualizar = null;
        ResultSet resultado = null;

        try {

            int steamAppId = 0;

            if (!temTitulo) {

                steamAppId =
                        Integer.parseInt(idTexto.trim());
            }

            Usuario usuario =
                    (Usuario) sessao.getAttribute("usuario");

            int idUsuario =
                    usuario.getId();

            // ==========================================
            // STATUS PADRÃO
            // ==========================================

            String status =
                    request.getParameter("status");

            if (status == null ||
                    status.trim().isEmpty()) {

                status = "quero_jogar";
            }

            // ==========================================
            // PADRONIZAR STATUS ANTIGOS
            // ==========================================

            if (status.equals("quero jogar")) {

                status = "quero_jogar";

            } else if (status.equals("jogando")) {

                status = "jogando";

            } else if (status.equals("zerado")) {

                status = "zerado";
            }

            // ==========================================
            // VALIDAR STATUS
            // ==========================================

            if (!status.equals("quero_jogar") &&
                    !status.equals("jogando") &&
                    !status.equals("zerado")) {

                status = "quero_jogar";
            }

            // ==========================================
            // CONECTAR AO BANCO
            // ==========================================

            conexao =
                    Conexao.conectar();

            if (conexao == null) {

                throw new Exception(
                        "Não foi possível conectar ao banco."
                );
            }

            // ==========================================
            // RESOLVER STEAM APP ID PELO TÍTULO
            // ==========================================

            if (temTitulo) {

                steamAppId =
                        buscarSteamAppIdPorTitulo(
                                conexao,
                                tituloTexto.trim()
                        );

                if (steamAppId <= 0) {

                    System.out.println(
                            "Jogo não encontrado pelo título: " +
                            tituloTexto
                    );

                    response.sendRedirect(
                            request.getContextPath() + "/jogos"
                    );

                    return;
                }
            }

            // ==========================================
            // VERIFICAR SE JÁ ESTÁ NA BIBLIOTECA
            // ==========================================

            String verificar =
                    "SELECT id " +
                    "FROM biblioteca " +
                    "WHERE id_usuario = ? " +
                    "AND steam_app_id = ?";

            stmtVerificar =
                    conexao.prepareStatement(verificar);

            stmtVerificar.setInt(
                    1,
                    idUsuario
            );

            stmtVerificar.setInt(
                    2,
                    steamAppId
            );

            resultado =
                    stmtVerificar.executeQuery();

            boolean existe =
                    resultado.next();

            resultado.close();
            resultado = null;

            stmtVerificar.close();
            stmtVerificar = null;

            // ==========================================
            // JÁ EXISTE
            // ==========================================

            if (existe) {

                String atualizar =
                        "UPDATE biblioteca " +
                        "SET status = ? " +
                        "WHERE id_usuario = ? " +
                        "AND steam_app_id = ?";

                stmtAtualizar =
                        conexao.prepareStatement(atualizar);

                stmtAtualizar.setString(
                        1,
                        status
                );

                stmtAtualizar.setInt(
                        2,
                        idUsuario
                );

                stmtAtualizar.setInt(
                        3,
                        steamAppId
                );

                stmtAtualizar.executeUpdate();

                System.out.println(
                        "================================="
                );

                System.out.println(
                        "BIBLIOTECA ATUALIZADA"
                );

                System.out.println(
                        "USUARIO: " + idUsuario
                );

                System.out.println(
                        "STEAM APP ID: " + steamAppId
                );

                System.out.println(
                        "NOVO STATUS: " + status
                );

                System.out.println(
                        "================================="
                );

            } else {

                // ==========================================
                // ADICIONAR NOVO JOGO
                // ==========================================

                String inserir =
                        "INSERT INTO biblioteca " +
                        "(id_usuario, steam_app_id, status, horas_jogadas) " +
                        "VALUES (?, ?, ?, 0)";

                stmtInserir =
                        conexao.prepareStatement(inserir);

                stmtInserir.setInt(
                        1,
                        idUsuario
                );

                stmtInserir.setInt(
                        2,
                        steamAppId
                );

                stmtInserir.setString(
                        3,
                        status
                );

                stmtInserir.executeUpdate();

                System.out.println(
                        "================================="
                );

                System.out.println(
                        "JOGO ADICIONADO À BIBLIOTECA"
                );

                System.out.println(
                        "USUARIO: " + idUsuario
                );

                System.out.println(
                        "STEAM APP ID: " + steamAppId
                );

                System.out.println(
                        "STATUS: " + status
                );

                System.out.println(
                        "================================="
                );
            }

            // ==========================================
            // FECHAR
            // ==========================================

            if (stmtAtualizar != null) {
                stmtAtualizar.close();
            }

            if (stmtInserir != null) {
                stmtInserir.close();
            }

            if (conexao != null) {
                conexao.close();
                conexao = null;
            }

            // ==========================================
            // IR PARA A BIBLIOTECA
            // ==========================================

            response.sendRedirect(
                    request.getContextPath() + "/biblioteca"
            );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Steam App ID inválido: " + idTexto
            );

            response.sendRedirect(
                    request.getContextPath() + "/jogos"
            );

        } catch (Exception e) {

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "ERRO AO ADICIONAR À BIBLIOTECA:"
            );

            e.printStackTrace();

            System.out.println(
                    "================================="
            );

            response.sendRedirect(
                    request.getContextPath() + "/jogos"
            );

        } finally {

            try {
                if (resultado != null) {
                    resultado.close();
                }
            } catch (Exception ignored) {
            }

            try {
                if (stmtVerificar != null) {
                    stmtVerificar.close();
                }
            } catch (Exception ignored) {
            }

            try {
                if (stmtInserir != null) {
                    stmtInserir.close();
                }
            } catch (Exception ignored) {
            }

            try {
                if (stmtAtualizar != null) {
                    stmtAtualizar.close();
                }
            } catch (Exception ignored) {
            }

            try {
                if (conexao != null) {
                    conexao.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}