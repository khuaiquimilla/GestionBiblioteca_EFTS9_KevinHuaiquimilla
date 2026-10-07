package dao.impl;

import conexion.DatabaseConnection;
import dao.UsuarioDAO;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    private final Connection conexion;

    // Obtiene la conexión única del Singleton.
    public UsuarioDAOImpl() throws SQLException {
        this.conexion = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, contraseña, rol) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getRut());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, usuario.getContrasena());   // En Java sin ñ, en la tabla con ñ
            stmt.setString(5, usuario.getRol());          // 'bibliotecario' o 'estudiante'
            return stmt.executeUpdate() > 0;              // True si se insertó 1 fila
        } catch (SQLException e) {                        // ej: rut repetido (es UNIQUE) o rol inválido
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);                           // Llena el primer ? con el id
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {                              // if: Esperamos una fila como máximo
                return new Usuario(                       // Orden del constructor de Usuario
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("correo"),
                        rs.getString("contraseña"),
                        rs.getString("rol"));             // El ENUM de MySQL se lee como String
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        return null;                                      // no se encontró o hubo error
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();       // Lista para almacenar los usuarios
        String sql = "SELECT * FROM usuarios";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();           // rs = las filas que devuelve MySQL
            while (rs.next()) {                           // Avanza fila por fila; false cuando no quedan más
                usuarios.add(new Usuario(                 // Convierte la fila actual en un objeto Usuario
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("correo"),
                        rs.getString("contraseña"),
                        rs.getString("rol")));
            }
        } catch (SQLException e) {                        // Si algo falla, muestra el error
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;                                  // Devuelve la lista (vacía si hubo error)
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, contraseña = ?, rol = ? "
                + "WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getRut());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, usuario.getContrasena());
            stmt.setString(5, usuario.getRol());
            stmt.setInt(6, usuario.getId());              // El 6° ? es el id del WHERE
            return stmt.executeUpdate() > 0;              // false si ese id no existía
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;              // false si ese id no existía
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Usuario login(String correo, String contrasena) {
        // Busca un usuario que tenga ESE correo Y ESA contraseña a la vez
        String sql = "SELECT * FROM usuarios WHERE correo = ? AND contraseña = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, correo);                    // 1° ? = correo
            stmt.setString(2, contrasena);                // 2° ? = contraseña
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {                              // Si hay fila, los datos son correctos
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("correo"),
                        rs.getString("contraseña"),
                        rs.getString("rol"));             // La vista usa el rol para habilitar botones
            }
        } catch (SQLException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
        }
        return null;                                      // null = correo o contraseña incorrectos
    }
}
