package controller;

import dao.CriarBanco;
import dao.Conexao;
import model.Usuario;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/google-callback")
public class GoogleCallbackServlet extends HttpServlet {

    private static final String CLIENT_ID =
            System.getenv("GOOGLE_CLIENT_ID");

    private static final String CLIENT_SECRET =
            System.getenv("GOOGLE_CLIENT_SECRET");

    private static final String REDIRECT_URI =
            System.getenv("GOOGLE_REDIRECT_URI");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            System.out.println("==============================");
            System.out.println("GOOGLE CALLBACK FOI CHAMADO");
            System.out.println("==============================");

            // =================================================
            // VERIFICAR CONFIGURAÇÃO
            // =================================================

            if (CLIENT_ID == null ||
                    CLIENT_ID.trim().isEmpty()) {

                System.out.println(
                        "ERRO: GOOGLE_CLIENT_ID não configurado."
                );

                response.sendRedirect(
                        "login.html?erro=config_google"
                );

                return;
            }

            if (CLIENT_SECRET == null ||
                    CLIENT_SECRET.trim().isEmpty()) {

                System.out.println(
                        "ERRO: GOOGLE_CLIENT_SECRET não configurado."
                );

                response.sendRedirect(
                        "login.html?erro=config_google"
                );

                return;
            }

            if (REDIRECT_URI == null ||
                    REDIRECT_URI.trim().isEmpty()) {

                System.out.println(
                        "ERRO: GOOGLE_REDIRECT_URI não configurado."
                );

                response.sendRedirect(
                        "login.html?erro=config_google"
                );

                return;
            }

            // =================================================
            // PEGAR PARÂMETROS DO GOOGLE
            // =================================================

            String code =
                    request.getParameter("code");

            String state =
                    request.getParameter("state");

            String erro =
                    request.getParameter("error");

            // Usuário cancelou o login
            if (erro != null) {

                System.out.println(
                        "USUÁRIO CANCELou O LOGIN GOOGLE."
                );

                response.sendRedirect(
                        "login.html?erro=google"
                );

                return;
            }

            // Código não recebido
            if (code == null ||
                    code.trim().isEmpty()) {

                System.out.println(
                        "ERRO: código Google não recebido."
                );

                response.sendRedirect(
                        "login.html?erro=sem_codigo"
                );

                return;
            }

            // =================================================
            // RECUPERAR SESSÃO
            // =================================================

            HttpSession sessao =
                    request.getSession(false);

            if (sessao == null) {

                System.out.println(
                        "ERRO: sessão não encontrada."
                );

                response.sendRedirect(
                        "login.html?erro=sessao"
                );

                return;
            }

            // =================================================
            // VALIDAR STATE
            // =================================================

            String stateSalvo =
                    (String) sessao.getAttribute(
                            "google_oauth_state"
                    );

            if (stateSalvo == null ||
                    state == null ||
                    !stateSalvo.equals(state)) {

                System.out.println(
                        "ERRO: STATE GOOGLE INVÁLIDO."
                );

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "State inválido."
                );

                return;
            }

            // State só pode ser usado uma vez
            sessao.removeAttribute(
                    "google_oauth_state"
            );

            // =================================================
            // TROCAR CODE POR ACCESS TOKEN
            // =================================================

            String dadosToken =
                    "code="
                    + URLEncoder.encode(
                            code,
                            "UTF-8"
                    )
                    + "&client_id="
                    + URLEncoder.encode(
                            CLIENT_ID,
                            "UTF-8"
                    )
                    + "&client_secret="
                    + URLEncoder.encode(
                            CLIENT_SECRET,
                            "UTF-8"
                    )
                    + "&redirect_uri="
                    + URLEncoder.encode(
                            REDIRECT_URI,
                            "UTF-8"
                    )
                    + "&grant_type=authorization_code";

            URL urlToken =
                    new URL(
                            "https://oauth2.googleapis.com/token"
                    );

            HttpURLConnection conexaoToken =
                    (HttpURLConnection)
                            urlToken.openConnection();

            conexaoToken.setRequestMethod("POST");

            conexaoToken.setDoOutput(true);

            conexaoToken.setConnectTimeout(10000);
            conexaoToken.setReadTimeout(10000);

            conexaoToken.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded"
            );

            try (
                    OutputStream saida =
                            conexaoToken.getOutputStream()
            ) {

                saida.write(
                        dadosToken.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
            }

            int codigoResposta =
                    conexaoToken.getResponseCode();

            InputStream entradaToken;

            if (codigoResposta >= 200 &&
                    codigoResposta < 300) {

                entradaToken =
                        conexaoToken.getInputStream();

            } else {

                entradaToken =
                        conexaoToken.getErrorStream();
            }

            String respostaToken =
                    lerResposta(
                            entradaToken
                    );

            conexaoToken.disconnect();

            if (codigoResposta < 200 ||
                    codigoResposta >= 300) {

                System.out.println(
                        "ERRO AO TROCAR CODE POR TOKEN:"
                );

                System.out.println(
                        respostaToken
                );

                response.sendRedirect(
                        "login.html?erro=token"
                );

                return;
            }

            // =================================================
            // LER TOKEN
            // =================================================

            JsonObject tokenJson =
                    JsonParser.parseString(
                            respostaToken
                    ).getAsJsonObject();

            if (!tokenJson.has("access_token")) {

                System.out.println(
                        "ERRO: access_token não encontrado."
                );

                response.sendRedirect(
                        "login.html?erro=token"
                );

                return;
            }

            String accessToken =
                    tokenJson.get(
                            "access_token"
                    ).getAsString();

            // =================================================
            // PEGAR DADOS DO USUÁRIO GOOGLE
            // =================================================

            URL urlUsuario =
                    new URL(
                            "https://openidconnect.googleapis.com/v1/userinfo"
                    );

            HttpURLConnection conexaoUsuario =
                    (HttpURLConnection)
                            urlUsuario.openConnection();

            conexaoUsuario.setRequestMethod("GET");

            conexaoUsuario.setConnectTimeout(10000);
            conexaoUsuario.setReadTimeout(10000);

            conexaoUsuario.setRequestProperty(
                    "Authorization",
                    "Bearer " + accessToken
            );

            int codigoUsuario =
                    conexaoUsuario.getResponseCode();

            InputStream entradaUsuario;

            if (codigoUsuario >= 200 &&
                    codigoUsuario < 300) {

                entradaUsuario =
                        conexaoUsuario.getInputStream();

            } else {

                entradaUsuario =
                        conexaoUsuario.getErrorStream();
            }

            String respostaUsuario =
                    lerResposta(
                            entradaUsuario
                    );

            conexaoUsuario.disconnect();

            if (codigoUsuario < 200 ||
                    codigoUsuario >= 300) {

                System.out.println(
                        "ERRO AO PEGAR USUÁRIO GOOGLE:"
                );

                System.out.println(
                        respostaUsuario
                );

                response.sendRedirect(
                        "login.html?erro=usuario_google"
                );

                return;
            }

            // =================================================
            // LER DADOS DO USUÁRIO
            // =================================================

            JsonObject dados =
                    JsonParser.parseString(
                            respostaUsuario
                    ).getAsJsonObject();

            String nome =
                    dados.has("name")
                    ? dados.get("name").getAsString()
                    : "Usuário Google";

            String email =
                    dados.has("email")
                    ? dados.get("email").getAsString()
                    : null;

            String foto =
                    dados.has("picture")
                    ? dados.get("picture").getAsString()
                    : "";

            if (email == null ||
                    email.trim().isEmpty()) {

                System.out.println(
                        "ERRO: Google não forneceu e-mail."
                );

                response.sendRedirect(
                        "login.html?erro=sem_email"
                );

                return;
            }

            email =
                    email.trim().toLowerCase();

            // =================================================
            // GARANTIR BANCO
            // =================================================

            CriarBanco.criarTabela();

            // =================================================
            // PROCURAR USUÁRIO PELO E-MAIL
            // =================================================

            Usuario usuario =
                    buscarUsuarioPorEmail(email);

            // =================================================
            // SE NÃO EXISTIR, CRIAR
            // =================================================

            if (usuario == null) {

                usuario =
                        criarUsuarioGoogle(
                                nome,
                                email,
                                foto
                        );
            }

            if (usuario == null) {

                System.out.println(
                        "ERRO: não foi possível criar usuário."
                );

                response.sendRedirect(
                        "login.html?erro=criar_usuario"
                );

                return;
            }

            // =================================================
            // CRIAR NOVA SESSÃO
            // =================================================

            HttpSession novaSessao =
                    request.getSession(true);

            novaSessao.setAttribute(
                    "usuario",
                    usuario
            );

            System.out.println(
                    "=============================="
            );

            System.out.println(
                    "LOGIN GOOGLE REALIZADO!"
            );

            System.out.println(
                    "USUÁRIO: "
                    + usuario.getUsername()
            );

            System.out.println(
                    "EMAIL: "
                    + usuario.getEmail()
            );

            System.out.println(
                    "=============================="
            );

            // =================================================
            // IR PARA O PERFIL
            // =================================================

            response.sendRedirect(
                    "perfil"
            );

        } catch (Exception e) {

            System.out.println(
                    "=============================="
            );

            System.out.println(
                    "ERRO NO LOGIN GOOGLE"
            );

            System.out.println(
                    "=============================="
            );

            e.printStackTrace();

            response.sendRedirect(
                    "login.html?erro=google"
            );
        }
    }

    // =========================================================
    // LER RESPOSTA HTTP
    // =========================================================

    private String lerResposta(
            InputStream entrada)
            throws IOException {

        if (entrada == null) {

            return "";
        }

        StringBuilder resultado =
                new StringBuilder();

        try (
                BufferedReader leitor =
                        new BufferedReader(
                                new InputStreamReader(
                                        entrada,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String linha;

            while (
                    (linha = leitor.readLine()) != null
            ) {

                resultado.append(linha);
            }
        }

        return resultado.toString();
    }

    // =========================================================
    // BUSCAR USUÁRIO POR E-MAIL
    // =========================================================

    private Usuario buscarUsuarioPorEmail(
            String email)
            throws Exception {

        String sql =
                "SELECT * "
                + "FROM usuario "
                + "WHERE email = ?";

        try (Connection conexao = Conexao.conectar()) {

            if (conexao == null) {

                throw new Exception(
                        "Não foi possível conectar ao banco."
                );
            }

            try (PreparedStatement stmt =
                    conexao.prepareStatement(sql)) {

                stmt.setString(1, email);

                try (ResultSet rs = stmt.executeQuery()) {

                    Usuario usuario = null;

                    if (rs.next()) {

                        usuario =
                                new Usuario();

                        usuario.setId(
                                rs.getInt("id")
                        );

                        usuario.setNome(
                                rs.getString("nome")
                        );

                        usuario.setUsername(
                                rs.getString("username")
                        );

                        usuario.setEmail(
                                rs.getString("email")
                        );

                        usuario.setSenha(
                                rs.getString("senha")
                        );

                        usuario.setFoto(
                                rs.getString("foto")
                        );

                        usuario.setBio(
                                rs.getString("bio")
                        );

                        usuario.setPais(
                                rs.getString("pais")
                        );

                    }

                    return usuario;
                }
            }
        }
    }

    // =========================================================
    // CRIAR USUÁRIO GOOGLE
    // =========================================================

    private Usuario criarUsuarioGoogle(
            String nome,
            String email,
            String foto)
            throws Exception {

        try (Connection conexao = Conexao.conectar()) {

            if (conexao == null) {

                throw new Exception(
                        "Não foi possível conectar ao banco."
                );
            }

            // =================================================
            // GERAR USERNAME
            // =================================================

            String usernameBase =
                    nome
                            .toLowerCase()
                            .replaceAll(
                                    "[^a-z0-9]",
                                    ""
                            );

            if (usernameBase.isEmpty()) {

                usernameBase =
                        "googleuser";
            }

            String username =
                    usernameBase;

            int contador = 1;

            while (
                    usernameExiste(
                            conexao,
                            username
                    )
            ) {

                username =
                        usernameBase
                        + contador;

                contador++;
            }

            // =================================================
            // INSERIR USUÁRIO
            // =================================================

            String sql =
                    "INSERT INTO usuario "
                    + "(nome, username, email, senha, foto) "
                    + "VALUES (?, ?, ?, ?, ?)";

            int id = 0;

            try (PreparedStatement stmt =
                    conexao.prepareStatement(
                            sql,
                            java.sql.Statement.RETURN_GENERATED_KEYS
                    )) {

                stmt.setString(1, nome);
                stmt.setString(2, username);
                stmt.setString(3, email);

                /*
                 * Usuário criado pelo Google não possui
                 * senha local.
                 */
                stmt.setString(4, "GOOGLE_LOGIN");
                stmt.setString(5, foto);

                stmt.executeUpdate();

                // =============================================
                // PEGAR ID GERADO
                // =============================================

                try (ResultSet chaves = stmt.getGeneratedKeys()) {

                    if (chaves.next()) {
                        id = chaves.getInt(1);
                    }
                }
            }

            if (id == 0) {

                throw new Exception(
                        "Não foi possível obter o ID do usuário."
                );
            }

            // =================================================
            // CRIAR OBJETO USUÁRIO
            // =================================================

            Usuario usuario =
                    new Usuario();

            usuario.setId(id);
            usuario.setNome(nome);
            usuario.setUsername(username);
            usuario.setEmail(email);
            usuario.setSenha("GOOGLE_LOGIN");
            usuario.setFoto(foto);

            return usuario;
        }
    }

    // =========================================================
    // VERIFICAR USERNAME
    // =========================================================

    private boolean usernameExiste(
            Connection conexao,
            String username)
            throws Exception {

        String sql =
                "SELECT id "
                + "FROM usuario "
                + "WHERE username = ?";

        try (PreparedStatement stmt =
                conexao.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {

                return rs.next();
            }
        }
    }
}