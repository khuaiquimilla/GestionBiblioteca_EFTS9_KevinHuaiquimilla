package dao.impl;

import conexion.DatabaseConnection;
import dao.EstudianteDAO;
import modelo.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAOImpl implements EstudianteDAO{

    private final Connection conexion;

    // Obtiene la conexión única del Singleton.
    public EstudianteDAOImpl() throws SQLException {
        this.conexion = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean insertar(Estudiante estudiante) {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getRut());
            stmt.setString(3, estudiante.getCurso());
            stmt.setString(4, estudiante.getCorreo());
            return stmt.executeUpdate() > 0;          // True si se insertó 1 fila
        } catch (SQLException e) {                    // ej: rut repetido (es UNIQUE)
            System.err.println("Error al insertar estudiante: " + e.getMessage());
            return false;
        }
    }


    @Override
    public Estudiante buscarPorId(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);                       // Llena el primer ? con el id
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {                          // if: Esperamos una fila como máximo
                return new Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("correo"),
                        rs.getString("curso"));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar estudiante: " + e.getMessage());
        }
        return null;                                  // no se encontró o hubo error
    }

    @Override
    public List<Estudiante> listarTodos() {
        List<Estudiante> estudiantes = new ArrayList<>();         // Lista para almacenar los estudiantes
        String sql = "SELECT * FROM estudiantes";            // Consulta SQL para obtener todos los estudiantes
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) { // Se prepara la consulta
            ResultSet rs = stmt.executeQuery();         // Se ejecuta la consulta (rs = las filas que devuelve MySQL)
            while (rs.next()) {                      // Avanza fila por fila; false cuando no quedan más
                estudiantes.add(new Estudiante(                // Convierte la fila actual en un objeto Estudiante
                        rs.getInt("id"),                // Aquí van los nombres de las columnas de la tabla
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("correo"),
                        rs.getString("curso")));
            }
        } catch (SQLException e) {                          // Si algo falla, muestra el error
            System.err.println("Error al obtener estudiantes: " + e.getMessage());
        }
        return estudiantes;                                      // Devuelve la lista de estudiantes

    }

    @Override
    public boolean actualizar(Estudiante estudiante) {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, correo = ?, curso = ? WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getRut());
            stmt.setString(3, estudiante.getCorreo());
            stmt.setString(4, estudiante.getCurso());
            stmt.setInt(5, estudiante.getId());
            return stmt.executeUpdate() > 0;          // false si ese id no existía
        } catch (SQLException e) {
            System.err.println("Error al actualizar estudiante: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar estudiante: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Estudiante buscarPorRut(String rut) {
        String sql = "SELECT * FROM estudiantes WHERE rut = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, rut);                       // Llena el primer ? con el rut
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {                          // if: Esperamos una fila como máximo
                return new Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rut"),
                        rs.getString("correo"),
                        rs.getString("curso"));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar estudiante: " + e.getMessage());
        }
        return null;                                  // no se encontró o hubo error
    }
}
