package dao;
import modelo.Libro;
import java.util.List;


// Contrato de operaciones sobre la tabla libros
// LibroDAOImpl es la clase que implementa estos métodos con SQL
public interface LibroDAO {

    // --- CRUD ---
    boolean insertar(Libro libro);
    Libro buscarPorId(int id);
    List<Libro> listarTodos();
    boolean actualizar(Libro libro);
    boolean eliminar(int id);

    // --- Stock (Préstamos y devoluciones)
    boolean descontarStock(int idLibro);   // Préstamo: resta 1 si hay stock
    boolean devolverStock(int idLibro);    // Devolución: suma 1 al stock

}
