package dao;
import modelo.Estudiante;
import java.util.List;

// Contrato de operaciones sobre la tabla estudiantes
public interface EstudianteDAO {

    // --- CRUD ---
    boolean insertar(Estudiante estudiante);
    Estudiante buscarPorId(int id);
    List<Estudiante> listarTodos();
    boolean actualizar(Estudiante estudiante);
    boolean eliminar(int id);

    // Busqueda por rut
    Estudiante buscarPorRut(String rut); // Une usuario logueado con su registro de estudiante
}
