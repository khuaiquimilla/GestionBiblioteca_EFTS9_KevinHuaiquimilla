package dao;
import modelo.Prestamo;
import java.util.List;

// Contrato de operaciones sobre la tabla prestamos
// PrestamoDAOImpl es la clase que implementa estos métodos con SQL
public interface PrestamoDAO {

    // --- Préstamo y devolución ---
    boolean insertar(Prestamo prestamo);          // Registra un préstamo nuevo
    Prestamo buscarPorId(int id);                 // Para saber qué libro devolver y si está atrasado
    boolean marcarDevuelto(int idPrestamo);       // Devolución: devuelto = TRUE

    // --- Listados (también sirven para los reportes) ---
    List<Prestamo> listarTodos();
    List<Prestamo> listarPorEstudiante(int idEstudiante);   // Reporte: historial de un estudiante
    List<Prestamo> listarNoDevueltos();                     // Reporte: libros en préstamo
}
