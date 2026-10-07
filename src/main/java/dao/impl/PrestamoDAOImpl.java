package dao.impl;

import conexion.DatabaseConnection;
import dao.PrestamoDAO;
import modelo.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAOImpl implements PrestamoDAO {

    private final Connection conexion;

    // Obtiene la conexión única del Singleton.
    public PrestamoDAOImpl() throws SQLException {
        this.conexion = DatabaseConnection.getInstance().getConnection();
    }

    // Convierte la fila actual del ResultSet en un objeto Prestamo.
    // Es private porque solo la usan los métodos de esta clase; así no repetimos
    // el mismo new Prestamo(...) en los 4 métodos que leen préstamos.
    private Prestamo crearPrestamo(ResultSet rs) throws SQLException {
        return new Prestamo(                              // Orden del constructor de Prestamo
                rs.getInt("id"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_libro"),
                rs.getDate("fecha_prestamo").toLocalDate(),     // DATE de MySQL -> LocalDate de Java
                rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto"));                     // BOOLEAN de MySQL -> boolean
    }

    @Override
    public boolean insertar(Prestamo prestamo) {
        // devuelto no se envía: en la tabla es DEFAULT FALSE
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, prestamo.getIdEstudiante());
            stmt.setInt(2, prestamo.getIdLibro());
            stmt.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));     // LocalDate -> DATE de MySQL
            stmt.setDate(4, Date.valueOf(prestamo.getFechaDevolucion()));   // fecha de vencimiento
            return stmt.executeUpdate() > 0;              // True si se insertó 1 fila
        } catch (SQLException e) {                        // ej: id de estudiante o libro que no existe (FK)
            System.err.println("Error al insertar préstamo: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Prestamo buscarPorId(int id) {
        String sql = "SELECT * FROM prestamos WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {                              // if: Esperamos una fila como máximo
                return crearPrestamo(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar préstamo: " + e.getMessage());
        }
        return null;                                      // no se encontró o hubo error
    }

    @Override
    public boolean marcarDevuelto(int idPrestamo) {
        // AND devuelto = FALSE: si ya estaba devuelto no cambia nada y retorna false,
        // así el stock no se suma dos veces por el mismo préstamo
        String sql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idPrestamo);
            return stmt.executeUpdate() > 0;              // false si no existe o ya estaba devuelto
        } catch (SQLException e) {
            System.err.println("Error al marcar devolución: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Prestamo> listarTodos() {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = "SELECT * FROM prestamos";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {                           // Avanza fila por fila
                prestamos.add(crearPrestamo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener préstamos: " + e.getMessage());
        }
        return prestamos;
    }

    @Override
    public List<Prestamo> listarPorEstudiante(int idEstudiante) {
        // Historial: todos los préstamos de un estudiante, devueltos o no
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = "SELECT * FROM prestamos WHERE id_estudiante = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idEstudiante);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {                           // while: un estudiante puede tener varios
                prestamos.add(crearPrestamo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener historial: " + e.getMessage());
        }
        return prestamos;
    }

    @Override
    public List<Prestamo> listarNoDevueltos() {
        // Libros en préstamo: los que todavía no se devuelven
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = "SELECT * FROM prestamos WHERE devuelto = FALSE";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                prestamos.add(crearPrestamo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener préstamos pendientes: " + e.getMessage());
        }
        return prestamos;
    }
}
