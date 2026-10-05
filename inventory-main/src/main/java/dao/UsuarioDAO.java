package dao;

import model.Usuario;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;

public class UsuarioDAO {

    // =====================================================
    // CADASTRAR USUARIO
    // =====================================================

    public boolean cadastrar(Usuario usuario) {

        String sql =
                "INSERT INTO usuario "
                + "(nome, username, email, senha, foto, bio, "
                + "data_nascimento, pais, plataforma_favorita) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conexao = null;
        PreparedStatement stmt = null;

        try {

            System.out.println("==============================");
            System.out.println("TENTANDO CADASTRAR USUARIO");
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Username: " + usuario.getUsername());
            System.out.println("Email: " + usuario.getEmail());
            System.out.println("==============================");

            conexao = Conexao.conectar();

            if (conexao == null) {

                System.out.println(
                        "ERRO: CONEXAO COM BANCO NULL!"
                );

                return false;
            }

            stmt = conexao.prepareStatement(sql);

            stmt.setString(
                    1,
                    usuario.getNome()
            );

            stmt.setString(
                    2,
                    usuario.getUsername()
            );

            stmt.setString(
                    3,
                    usuario.getEmail()
            );

            stmt.setString(
                    4,
                    usuario.getSenha()
            );

            stmt.setString(
                    5,
                    usuario.getFoto()
            );

            stmt.setString(
                    6,
                    usuario.getBio()
            );

            stmt.setString(
                    7,
                    usuario.getDataNascimento()
            );

            stmt.setString(
                    8,
                    usuario.getPais()
            );

            stmt.setString(
                    9,
                    usuario.getPlataformaFavorita()
            );

            int resultado =
                    stmt.executeUpdate();

            System.out.println("==============================");
            System.out.println(
                    "USUARIO INSERIDO NO BANCO!"
            );
            System.out.println(
                    "RESULTADO: " + resultado
            );
            System.out.println("==============================");

            return resultado > 0;

        } catch (Exception e) {

            System.out.println("==============================");
            System.out.println("ERRO REAL AO CADASTRAR");
            System.out.println("==============================");

            e.printStackTrace();

            System.out.println("==============================");

            return false;

        } finally {

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // LOGIN
    // =====================================================

    public Usuario login(
            String email,
            String senha) {

        String sql =
                "SELECT * FROM usuario WHERE email = ?";

        try (Connection conexao = Conexao.conectar()) {
            if (conexao == null) {
                return null;
            }

            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, email);

                try (ResultSet resultado = stmt.executeQuery()) {
                    if (!resultado.next()) {
                        return null;
                    }

                    String hash = resultado.getString("senha");

                    if (!PasswordUtil.matches(senha, hash)) {
                        return null;
                    }

                    return criarUsuario(resultado);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro no login: " + e.getMessage());
            return null;
        }
    }

    // =====================================================
    // BUSCAR POR ID
    // =====================================================

    public Usuario buscarPorId(
            int id) {

        String sql =
                "SELECT * "
                + "FROM usuario "
                + "WHERE id = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet resultado = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return null;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(
                    1,
                    id
            );

            resultado =
                    stmt.executeQuery();

            if (resultado.next()) {

                return criarUsuario(
                        resultado
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO BUSCAR USUARIO:"
            );

            e.printStackTrace();

        } finally {

            try {

                if (resultado != null) {
                    resultado.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // =====================================================
    // BUSCAR POR USERNAME
    // =====================================================

    public Usuario buscarPorUsername(
            String username) {

        String sql =
                "SELECT * "
                + "FROM usuario "
                + "WHERE username = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet resultado = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return null;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setString(
                    1,
                    username
            );

            resultado =
                    stmt.executeQuery();

            if (resultado.next()) {

                return criarUsuario(
                        resultado
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO BUSCAR USERNAME:"
            );

            e.printStackTrace();

        } finally {

            try {

                if (resultado != null) {
                    resultado.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // =====================================================
    // BUSCAR POR EMAIL
    // =====================================================

    public Usuario buscarPorEmail(
            String email) {

        String sql =
                "SELECT * "
                + "FROM usuario "
                + "WHERE email = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet resultado = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return null;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setString(
                    1,
                    email
            );

            resultado =
                    stmt.executeQuery();

            if (resultado.next()) {

                return criarUsuario(
                        resultado
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO BUSCAR EMAIL:"
            );

            e.printStackTrace();

        } finally {

            try {

                if (resultado != null) {
                    resultado.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // =====================================================
    // SEGUIR USUARIO
    // =====================================================

    public boolean seguir(
            int idSeguidor,
            int idSeguido) {

        if (idSeguidor == idSeguido) {
            return false;
        }

        String sql =
                "INSERT OR IGNORE INTO seguidor "
                + "(id_seguidor, id_seguido) "
                + "VALUES (?, ?)";

        Connection conexao = null;
        PreparedStatement stmt = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return false;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(
                    1,
                    idSeguidor
            );

            stmt.setInt(
                    2,
                    idSeguido
            );

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO SEGUIR USUARIO:"
            );

            e.printStackTrace();

            return false;

        } finally {

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // DEIXAR DE SEGUIR
    // =====================================================

    public boolean deixarDeSeguir(
            int idSeguidor,
            int idSeguido) {

        String sql =
                "DELETE FROM seguidor "
                + "WHERE id_seguidor = ? "
                + "AND id_seguido = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return false;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(
                    1,
                    idSeguidor
            );

            stmt.setInt(
                    2,
                    idSeguido
            );

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO DEIXAR DE SEGUIR:"
            );

            e.printStackTrace();

            return false;

        } finally {

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // VERIFICAR SE SEGUE
    // =====================================================

    public boolean seguindo(
            int idSeguidor,
            int idSeguido) {

        String sql =
                "SELECT id "
                + "FROM seguidor "
                + "WHERE id_seguidor = ? "
                + "AND id_seguido = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return false;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(
                    1,
                    idSeguidor
            );

            stmt.setInt(
                    2,
                    idSeguido
            );

            rs =
                    stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO VERIFICAR SEGUIMENTO:"
            );

            e.printStackTrace();

            return false;

        } finally {

            try {

                if (rs != null) {
                    rs.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // CONTAR SEGUIDORES
    // =====================================================

    public int contarSeguidores(
            int idUsuario) {

        String sql =
                "SELECT COUNT(*) "
                + "FROM seguidor "
                + "WHERE id_seguido = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return 0;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(
                    1,
                    idUsuario
            );

            rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO CONTAR SEGUIDORES:"
            );

            e.printStackTrace();

        } finally {

            try {

                if (rs != null) {
                    rs.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    // =====================================================
    // CONTAR QUANTOS SEGUE
    // =====================================================

    public int contarSeguindo(
            int idUsuario) {

        String sql =
                "SELECT COUNT(*) "
                + "FROM seguidor "
                + "WHERE id_seguidor = ?";

        Connection conexao = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            conexao =
                    Conexao.conectar();

            if (conexao == null) {
                return 0;
            }

            stmt =
                    conexao.prepareStatement(sql);

            stmt.setInt(
                    1,
                    idUsuario
            );

            rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {

            System.out.println(
                    "ERRO AO CONTAR SEGUINDO:"
            );

            e.printStackTrace();

        } finally {

            try {

                if (rs != null) {
                    rs.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (stmt != null) {
                    stmt.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                if (conexao != null) {
                    conexao.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    // =====================================================
    // CRIAR OBJETO USUARIO
    // =====================================================

    private Usuario criarUsuario(
            ResultSet resultado)
            throws Exception {

        Usuario usuario =
                new Usuario();

        usuario.setId(
                resultado.getInt("id")
        );

        usuario.setNome(
                resultado.getString("nome")
        );

        usuario.setUsername(
                resultado.getString("username")
        );

        usuario.setEmail(
                resultado.getString("email")
        );

        usuario.setFoto(
                resultado.getString("foto")
        );

        usuario.setBio(
                resultado.getString("bio")
        );

        usuario.setDataNascimento(
                resultado.getString(
                        "data_nascimento"
                )
        );

        usuario.setPais(
                resultado.getString("pais")
        );

        usuario.setPlataformaFavorita(
                resultado.getString(
                        "plataforma_favorita"
                )
        );

        return usuario;
    }
}