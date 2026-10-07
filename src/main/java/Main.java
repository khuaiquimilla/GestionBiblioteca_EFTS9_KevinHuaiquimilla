import dao.EstudianteDAO;
import dao.impl.EstudianteDAOImpl;
import modelo.Estudiante;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            EstudianteDAO dao = new EstudianteDAOImpl();          // tipo = interfaz, objeto = la Impl
            List<Estudiante> estudiantes = dao.listarTodos();     // llamo al método del DAO

            System.out.println("Estudiantes encontrados: " + estudiantes.size());   // debería decir 10
            for (Estudiante est : estudiantes) {                  // recorro la lista
                System.out.println(est.getId() + " - " + est.getNombre()
                        + " | correo: " + est.getCorreo()
                        + " | rut: " + est.getRut()// debe tener @
                        + " | curso: " + est.getCurso());         // debe ser un curso
            }
        } catch (SQLException e) {                                // por si falla la conexión
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }
}