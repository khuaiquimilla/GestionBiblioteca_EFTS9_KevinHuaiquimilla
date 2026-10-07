package dao.impl;

import conexion.DatabaseConnection;
import dao.LibroDAO;
import modelo.Libro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


// Implementación de LibroDAO: aquí vive todo el SQL de la tabla libros.
public class LibroDAOImpl implements LibroDAO {


    private final Connection conexion;

    // Obtiene la conexión única del Singleton.
    public LibroDAOImpl() throws SQLException {
        this.conexion = DatabaseConnection.getInstance().getConnection();
    }

    // Inserta un libro nuevo (el id lo asigna MySQL)
    @Override
    public boolean insertar(Libro libro) {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setString(3, libro.getIsbn());
            stmt.setString(4, libro.getEditorial());
            stmt.setInt(5, libro.getStock());
            stmt.setInt(6, libro.getIdCategoria());
            return stmt.executeUpdate() > 0;          // True si se insertó 1 fila
        } catch (SQLException e) {                    // ej: ISBN repetido (es UNIQUE)
            System.err.println("Error al insertar libro: " + e.getMessage());
            return false;
        }
    }

    // Actualiza un libro existente
    @Override
    public boolean actualizar(Libro libro) {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, "
                + "stock = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setString(3, libro.getIsbn());
            stmt.setString(4, libro.getEditorial());
            stmt.setInt(5, libro.getStock());
            stmt.setInt(6, libro.getIdCategoria());
            stmt.setInt(7, libro.getId());            // el 7º ? es el del WHERE
            return stmt.executeUpdate() > 0;          // false si ese id no existía
        } catch (SQLException e) {
            System.err.println("Error al actualizar libro: " + e.getMessage());
            return false;
        }
    }

    // Elimina un libro por su ID
    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM libros WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar libro: " + e.getMessage());
            return false;
        }
    }

    // Préstamo: Resta 1 al stock solo si queda al menos un libro
    @Override
    public boolean descontarStock(int idLibro) {
        String sql = "UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idLibro);
            return stmt.executeUpdate() > 0;          // false = no había stock
        } catch (SQLException e) {
            System.err.println("Error al descontar stock: " + e.getMessage());
            return false;
        }
    }

    // Devolución: Suma 1 al stock
    @Override
    public boolean devolverStock(int idLibro) {
        String sql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idLibro);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al devolver stock: " + e.getMessage());
            return false;
        }
    }

    // Método para buscar un libro por su ID
    @Override
    public Libro buscarPorId(int id) {
        String sql = "SELECT * FROM libros WHERE id = ?";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);                       // Llena el primer ? con el id
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {                          // if: Esperamos una fila como máximo
                return new Libro(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        rs.getString("editorial"),
                        rs.getInt("stock"),
                        rs.getInt("id_categoria"));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar libro: " + e.getMessage());
        }
        return null;                                  // no se encontró o hubo error
    }

    // Método para obtener todos los libros
    @Override
    public List<Libro> listarTodos() {
        List<Libro> libros = new ArrayList<>();         // Lista para almacenar los libros
        String sql = "SELECT * FROM libros";            // Consulta SQL para obtener todos los libros
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) { // Se prepara la consulta
            ResultSet rs = stmt.executeQuery();         // Se ejecuta la consulta (rs = las filas que devuelve MySQL)
            while (rs.next()) {                      // Avanza fila por fila; false cuando no quedan más
                libros.add(new Libro(                // Convierte la fila actual en un objeto Libro
                        rs.getInt("id"),                // Aquí van los nombres de las columnas de la tabla
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        rs.getString("editorial"),
                        rs.getInt("stock"),
                        rs.getInt("id_categoria")));
            }
        } catch (SQLException e) {                          // Si algo falla, muestra el error
            System.err.println("Error al obtener libros: " + e.getMessage());
        }
        return libros;                                      // Devuelve la lista de libros

    }

}

