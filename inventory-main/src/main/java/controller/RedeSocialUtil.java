package controller;

import dao.Conexao;

import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Componentes visuais e consultas da rede social do Inventory.
 */
public class RedeSocialUtil {

    private static class Pessoa {
        int id;
        String nome;
        String username;
        String foto;

        Pessoa(int id, String nome, String username, String foto) {
            this.id = id;
            this.nome = nome;
            this.username = username;
            this.foto = foto;
        }
    }

    public static String renderRede(int idUsuario, String contextPath) {
        List<Pessoa> seguidores = buscar(idUsuario, true);
        List<Pessoa> seguindo = buscar(idUsuario, false);

        StringBuilder html = new StringBuilder();

        html.append("<section class='rede-social'>");
        html.append("<div class='rede-titulo'>");
        html.append("<h2>👥 Rede social</h2>");
        html.append("<p>Seguidores e pessoas que este usuário segue.</p>");
        html.append("</div>");
        html.append("<div class='rede-grid'>");

        html.append(renderColuna(
                "Seguidores",
                seguidores,
                contextPath,
                "Este usuário ainda não tem seguidores."
        ));

        html.append(renderColuna(
                "Seguindo",
                seguindo,
                contextPath,
                "Este usuário ainda não segue ninguém."
        ));

        html.append("</div>");
        html.append("</section>");

        return html.toString();
    }

    public static String renderMiniRede(
            int idUsuario,
            String contextPath) {

        List<Pessoa> seguidores = buscar(idUsuario, true);
        List<Pessoa> seguindo = buscar(idUsuario, false);

        StringBuilder html = new StringBuilder();

        html.append("<div class='mini-rede'>");

        html.append("<div class='mini-rede-contagem'>");
        html.append("<span><strong>");
        html.append(seguidores.size());
        html.append("</strong> seguidores</span>");
        html.append("<span><strong>");
        html.append(seguindo.size());
        html.append("</strong> seguindo</span>");
        html.append("</div>");

        html.append("<div class='mini-rede-grupos'>");
        html.append(renderMiniGrupo("Seguidores", seguidores, contextPath));
        html.append(renderMiniGrupo("Seguindo", seguindo, contextPath));
        html.append("</div>");

        html.append("</div>");

        return html.toString();
    }

    private static String renderColuna(
            String titulo,
            List<Pessoa> pessoas,
            String contextPath,
            String vazio) {

        StringBuilder html = new StringBuilder();

        html.append("<div class='rede-coluna'>");
        html.append("<div class='rede-coluna-titulo'>");
        html.append("<h3>");
        html.append(escapar(titulo));
        html.append("</h3>");
        html.append("<span>");
        html.append(pessoas.size());
        html.append("</span>");
        html.append("</div>");

        if (pessoas.isEmpty()) {
            html.append("<div class='rede-vazio'>");
            html.append(escapar(vazio));
            html.append("</div>");
        } else {
            html.append("<div class='rede-pessoas'>");

            for (Pessoa pessoa : pessoas) {
                html.append(renderPessoa(pessoa, contextPath));
            }

            html.append("</div>");
        }

        html.append("</div>");

        return html.toString();
    }

    private static String renderPessoa(
            Pessoa pessoa,
            String contextPath) {

        StringBuilder html = new StringBuilder();

        html.append(
                "<a class='rede-pessoa' href='" +
                contextPath +
                "/perfil-usuario?id=" +
                pessoa.id +
                "'>"
        );

        html.append(renderFoto(pessoa, contextPath, "rede-foto"));

        html.append("<div class='rede-pessoa-dados'>");
        html.append("<strong>");
        html.append(escapar(pessoa.nome));
        html.append("</strong>");

        String username = pessoa.username;

        if (username == null || username.trim().isEmpty()) {
            username = "usuario" + pessoa.id;
        }

        if (!username.startsWith("@")) {
            username = "@" + username;
        }

        html.append("<span>");
        html.append(escapar(username));
        html.append("</span>");
        html.append("</div>");

        html.append("<span class='rede-seta'>›</span>");
        html.append("</a>");

        return html.toString();
    }

    private static String renderMiniGrupo(
            String titulo,
            List<Pessoa> pessoas,
            String contextPath) {

        StringBuilder html = new StringBuilder();

        html.append("<div class='mini-grupo'>");
        html.append("<span class='mini-grupo-titulo'>");
        html.append(escapar(titulo));
        html.append("</span>");
        html.append("<div class='mini-fotos'>");

        int limite = Math.min(pessoas.size(), 5);

        for (int i = 0; i < limite; i++) {
            Pessoa pessoa = pessoas.get(i);

            html.append(
                    "<a href='" +
                    contextPath +
                    "/perfil-usuario?id=" +
                    pessoa.id +
                    "' title='" +
                    escapar(pessoa.nome) +
                    "'>"
            );

            html.append(
                    renderFoto(
                            pessoa,
                            contextPath,
                            "mini-foto"
                    )
            );

            html.append("</a>");
        }

        if (pessoas.isEmpty()) {
            html.append("<span class='mini-sem'>—</span>");
        }

        html.append("</div>");
        html.append("</div>");

        return html.toString();
    }

    private static String renderFoto(
            Pessoa pessoa,
            String contextPath,
            String classe) {

        String foto = pessoa.foto;

        if (foto != null && !foto.trim().isEmpty()) {
            String nomeArquivo =
                    new File(foto.trim()).getName();

            String url;

            if (foto.startsWith("http://") ||
                    foto.startsWith("https://")) {
                try {
                    url = contextPath +
                            "/foto-perfil?url=" +
                            URLEncoder.encode(
                                    foto.trim(),
                                    "UTF-8"
                            );
                } catch (Exception e) {
                    url = foto;
                }
            } else {
                try {
                    url = contextPath +
                            "/foto-perfil?arquivo=" +
                            URLEncoder.encode(
                                    nomeArquivo,
                                    "UTF-8"
                            );
                } catch (Exception e) {
                    url = contextPath +
                            "/foto-perfil?arquivo=" +
                            nomeArquivo;
                }
            }

            return
                    "<img class='" + classe + "' src='" +
                    escapar(url) +
                    "' alt='Foto de " +
                    escapar(pessoa.nome) +
                    "'>";
        }

        return
                "<span class='" + classe + " rede-sem-foto'>" +
                escapar(primeiraLetra(pessoa.nome)) +
                "</span>";
    }

    private static List<Pessoa> buscar(
            int idUsuario,
            boolean seguidores) {

        List<Pessoa> pessoas = new ArrayList<Pessoa>();

        String sql;

        if (seguidores) {
            sql =
                    "SELECT u.id, u.nome, u.username, u.foto " +
                    "FROM seguidor s " +
                    "INNER JOIN usuario u ON u.id = s.id_seguidor " +
                    "WHERE s.id_seguido = ? " +
                    "ORDER BY u.nome COLLATE NOCASE ASC";
        } else {
            sql =
                    "SELECT u.id, u.nome, u.username, u.foto " +
                    "FROM seguidor s " +
                    "INNER JOIN usuario u ON u.id = s.id_seguido " +
                    "WHERE s.id_seguidor = ? " +
                    "ORDER BY u.nome COLLATE NOCASE ASC";
        }

        Connection conn = Conexao.conectar();

        if (conn == null) {
            return pessoas;
        }

        try (
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pessoas.add(
                            new Pessoa(
                                    rs.getInt("id"),
                                    rs.getString("nome"),
                                    rs.getString("username"),
                                    rs.getString("foto")
                            )
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }

        return pessoas;
    }

    private static String primeiraLetra(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return "?";
        }
        return String.valueOf(
                nome.trim().charAt(0)
        ).toUpperCase();
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
