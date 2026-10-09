package controlador;

import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import dao.impl.EstudianteDAOImpl;
import dao.impl.LibroDAOImpl;
import dao.impl.PrestamoDAOImpl;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

// Controlador de los 3 reportes. Solo LEE datos, nunca cambia nada en MySQL.
// La ventana le pasa un modelo nuevo que ya trae los títulos; aquí solo se agregan las filas.
public class ControladorReportes {

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;
    private final EstudianteDAO estudianteDAO;

    public ControladorReportes() throws SQLException {
        this.prestamoDAO = new PrestamoDAOImpl();
        this.libroDAO = new LibroDAOImpl();
        this.estudianteDAO = new EstudianteDAOImpl();
    }

    // --- Datos para el combo de estudiantes ---

    public List<Estudiante> listarEstudiantes() {
        return estudianteDAO.listarTodos();
    }

    // Para el rol estudiante: su ficha se busca por el RUT del usuario
    public Estudiante buscarEstudiantePorRut(String rut) {
        return estudianteDAO.buscarPorRut(rut);
    }

    // --- Reporte 1: libros más prestados ---
    // Se cuenta en Java con for: por cada libro, se recorren todos los préstamos
    public void cargarMasPrestados(DefaultTableModel modelo) {
        List<Libro> libros = libroDAO.listarTodos();
        List<Prestamo> prestamos = prestamoDAO.listarTodos();

        // Paso 1: contar. veces[i] guarda cuántas veces se prestó el libro que está en la posición i
        int[] veces = new int[libros.size()];
        int maximo = 0;
        for (int i = 0; i < libros.size(); i++) {
            for (Prestamo p : prestamos) {
                if (p.getIdLibro() == libros.get(i).getId()) {
                    veces[i]++;
                }
            }
            if (veces[i] > maximo) {
                maximo = veces[i];                 // El libro más prestado hasta ahora
            }
        }

        // Paso 2: ordenar de mayor a menor. Primero se agregan los libros que tienen el máximo,
        // después los que tienen máximo - 1, y así hasta 1. Los que nunca se prestaron no salen.
        for (int cantidad = maximo; cantidad >= 1; cantidad--) {
            for (int i = 0; i < libros.size(); i++) {
                if (veces[i] == cantidad) {
                    modelo.addRow(new Object[]{
                            libros.get(i).getTitulo(),
                            libros.get(i).getAutor(),
                            veces[i]
                    });
                }
            }
        }
    }

    // --- Reporte 2: historial de un estudiante (devueltos y no devueltos) ---
    public void cargarHistorial(DefaultTableModel modelo, int idEstudiante) {
        List<Libro> libros = libroDAO.listarTodos();
        for (Prestamo p : prestamoDAO.listarPorEstudiante(idEstudiante)) {
            modelo.addRow(new Object[]{
                    p.getId(),
                    tituloLibro(p.getIdLibro(), libros),
                    p.getFechaPrestamo(),
                    p.getFechaDevolucion(),
                    p.getEstado()
            });
        }
    }

    // --- Reporte 3: libros que están prestados ahora (devuelto = FALSE) ---
    public void cargarEnPrestamo(DefaultTableModel modelo) {
        List<Libro> libros = libroDAO.listarTodos();
        List<Estudiante> estudiantes = estudianteDAO.listarTodos();
        for (Prestamo p : prestamoDAO.listarNoDevueltos()) {
            modelo.addRow(new Object[]{
                    p.getId(),
                    tituloLibro(p.getIdLibro(), libros),
                    nombreEstudiante(p.getIdEstudiante(), estudiantes),
                    p.getFechaDevolucion(),
                    p.getEstado()                  // "ATRASADO" si ya venció
            });
        }
    }

    // --- Ayudantes: id -> texto (igual que nombreCategoria en ControladorLibros) ---

    private String tituloLibro(int idLibro, List<Libro> libros) {
        for (Libro l : libros) {
            if (l.getId() == idLibro) {
                return l.getTitulo();
            }
        }
        return "";
    }

    private String nombreEstudiante(int idEstudiante, List<Estudiante> estudiantes) {
        for (Estudiante e : estudiantes) {
            if (e.getId() == idEstudiante) {
                return e.getNombre();
            }
        }
        return "";
    }
}
