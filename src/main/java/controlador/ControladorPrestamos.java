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
import java.time.LocalDate;
import java.util.List;

// Controlador de préstamos y devoluciones.
// Es la ÚNICA clase que cambia el stock, y lo hace dentro de métodos synchronized.
// El menú crea un solo objeto de esta clase y lo comparte: así todos los hilos usan el mismo candado.
public class ControladorPrestamos {

    public static final int DIAS_PRESTAMO = 7;     // Plazo para devolver un libro

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;               // Para revisar y cambiar el stock
    private final EstudianteDAO estudianteDAO;     // Para el combo y para mostrar nombres en la tabla

    public ControladorPrestamos() throws SQLException {
        this.prestamoDAO = new PrestamoDAOImpl();
        this.libroDAO = new LibroDAOImpl();
        this.estudianteDAO = new EstudianteDAOImpl();
    }

    // --- Operaciones que cambian el stock (las ejecutan los hilos) ---

    // synchronized: si dos hilos piden un libro al mismo tiempo, el segundo ESPERA a que el primero termine.
    // Revisar el stock, descontarlo e insertar el préstamo es UNA sola operación:
    // ningún otro hilo puede leer "stock 1" mientras este hilo todavía no lo descuenta.
    public synchronized boolean registrarPrestamo(int idEstudiante, int idLibro) {
        Libro libro = libroDAO.buscarPorId(idLibro);           // Stock actual, leído desde MySQL
        if (libro == null || libro.getStock() <= 0) {
            throw new IllegalArgumentException("No quedan copias disponibles de este libro.");
        }
        if (!libroDAO.descontarStock(idLibro)) {               // El SQL tiene AND stock > 0 (segunda defensa)
            return false;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate vence = hoy.plusDays(DIAS_PRESTAMO);         // Fecha de vencimiento = hoy + 7 días
        Prestamo nuevo = new Prestamo(0, idEstudiante, idLibro, hoy, vence, false);
        if (!prestamoDAO.insertar(nuevo)) {
            libroDAO.devolverStock(idLibro);                   // Si el INSERT falló, se repone el stock
            return false;
        }
        return true;
    }

    // También synchronized porque suma stock
    public synchronized boolean registrarDevolucion(int idPrestamo) {
        Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
        if (prestamo == null) {
            throw new IllegalArgumentException("El préstamo no existe.");
        }
        if (prestamo.isDevuelto()) {
            throw new IllegalArgumentException("Ese préstamo ya fue devuelto.");
        }
        if (!prestamoDAO.marcarDevuelto(idPrestamo)) {         // devuelto = TRUE
            return false;
        }
        return libroDAO.devolverStock(prestamo.getIdLibro());  // stock + 1
    }

    // Se pregunta antes de devolver: después de devolverlo ya no cuenta como atrasado
    public boolean estaAtrasado(int idPrestamo) {
        Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);
        return prestamo != null && prestamo.estaAtrasado();
    }

    // --- Datos para la ventana ---

    // Usuarios y estudiantes son tablas distintas: se unen por el RUT
    // Retorna null si ese RUT no tiene ficha de estudiante
    public Estudiante buscarEstudiantePorRut(String rut) {
        return estudianteDAO.buscarPorRut(rut);
    }

    public List<Estudiante> listarEstudiantes() {
        return estudianteDAO.listarTodos();
    }

    public List<Libro> listarLibros() {
        return libroDAO.listarTodos();
    }

    // soloEstudiante == null -> todos los préstamos (bibliotecario)
    // soloEstudiante != null -> solo los préstamos de ese estudiante (rol estudiante)
    public void cargarTabla(DefaultTableModel modelo, Estudiante soloEstudiante) {
        modelo.setRowCount(0);
        List<Prestamo> prestamos;
        if (soloEstudiante == null) {
            prestamos = prestamoDAO.listarTodos();
        } else {
            prestamos = prestamoDAO.listarPorEstudiante(soloEstudiante.getId());
        }
        List<Estudiante> estudiantes = estudianteDAO.listarTodos();
        List<Libro> libros = libroDAO.listarTodos();
        for (Prestamo p : prestamos) {
            modelo.addRow(new Object[]{
                    p.getId(),
                    nombreEstudiante(p.getIdEstudiante(), estudiantes),   // Nombre en vez del id
                    tituloLibro(p.getIdLibro(), libros),                  // Título en vez del id
                    p.getFechaPrestamo(),
                    p.getFechaDevolucion(),
                    p.getEstado()                                         // Devuelto / ATRASADO / En préstamo
            });
        }
    }

    // Mismo recorrido que nombreCategoria en ControladorLibros
    private String nombreEstudiante(int idEstudiante, List<Estudiante> estudiantes) {
        for (Estudiante e : estudiantes) {
            if (e.getId() == idEstudiante) {
                return e.getNombre();
            }
        }
        return "";
    }

    private String tituloLibro(int idLibro, List<Libro> libros) {
        for (Libro l : libros) {
            if (l.getId() == idLibro) {
                return l.getTitulo();
            }
        }
        return "";
    }
}
