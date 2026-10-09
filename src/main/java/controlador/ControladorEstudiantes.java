package controlador;

import dao.EstudianteDAO;
import dao.impl.EstudianteDAOImpl;
import modelo.Estudiante;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;

// Controlador de estudiantes
public class ControladorEstudiantes {

    private final EstudianteDAO estudianteDAO;     // Tipo = interfaz, objeto = la Impl

    public ControladorEstudiantes() throws SQLException {
        this.estudianteDAO = new EstudianteDAOImpl();
    }

    // Valida los datos y lanza IllegalArgumentException con el mensaje que mostrará la vista
    private void validar(String nombre, String rut, String correo, String curso) {
        if (nombre.isEmpty() || rut.isEmpty() || correo.isEmpty() || curso.isEmpty()) {
            throw new IllegalArgumentException("Todos los campos deben estar completos.");
        }
    }

    // Retorna true si se guardó; false si MySQL lo rechazó (ej: RUT repetido)
    public boolean agregarEstudiante(String nombre, String rut, String correo, String curso) {
        validar(nombre, rut, correo, curso);
        Estudiante nuevo = new Estudiante(0, nombre, rut, correo, curso);   // id 0: lo pone MySQL (AUTO_INCREMENT)
        return estudianteDAO.insertar(nuevo);
    }

    public boolean actualizarEstudiante(int id, String nombre, String rut, String correo, String curso) {
        validar(nombre, rut, correo, curso);
        Estudiante editado = new Estudiante(id, nombre, rut, correo, curso);
        return estudianteDAO.actualizar(editado);
    }

    // false si el estudiante tiene préstamos (la llave foránea no deja borrarlo)
    public boolean eliminarEstudiante(int id) {
        return estudianteDAO.eliminar(id);
    }

    // Vacía la tabla y la vuelve a llenar con lo que hay en MySQL
    public void cargarTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Estudiante e : estudianteDAO.listarTodos()) {
            modelo.addRow(new Object[]{
                    e.getId(),
                    e.getNombre(),
                    e.getRut(),
                    e.getCorreo(),
                    e.getCurso()
            });
        }
    }
}
