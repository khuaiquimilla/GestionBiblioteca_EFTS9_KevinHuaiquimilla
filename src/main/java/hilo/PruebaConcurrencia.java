package hilo;

import controlador.ControladorPrestamos;
import dao.LibroDAO;
import dao.impl.LibroDAOImpl;
import modelo.Libro;

import java.sql.SQLException;

// Prueba de concurrencia: 3 hilos piden el MISMO libro, que tiene stock 1.
// Si synchronized funciona, solo 1 hilo consigue el préstamo y el stock termina en 0 (nunca en -1).
// Se ejecuta aparte del programa: clic derecho en esta clase -> Run 'PruebaConcurrencia.main()'
public class PruebaConcurrencia {

    public static void main(String[] args) throws SQLException, InterruptedException {
        int idLibro = 3;                                   // "Breve Historia del Tiempo"
        LibroDAO libroDAO = new LibroDAOImpl();

        // Preparación: dejar el libro con stock 1
        Libro libro = libroDAO.buscarPorId(idLibro);
        libro.setStock(1);
        libroDAO.actualizar(libro);
        System.out.println("Stock inicial de \"" + libro.getTitulo() + "\": 1");

        // Un controlador compartido = Un candado para los 3 hilos
        ControladorPrestamos controlador = new ControladorPrestamos();

        // 3 hilos, uno por estudiante (ids 1, 2 y 3), que piden el mismo libro
        Thread hilo1 = new Thread(() -> pedirLibro(controlador, 1, idLibro));
        Thread hilo2 = new Thread(() -> pedirLibro(controlador, 2, idLibro));
        Thread hilo3 = new Thread(() -> pedirLibro(controlador, 3, idLibro));
        hilo1.start();                                     // Los 3 hilos parten casi al mismo tiempo
        hilo2.start();
        hilo3.start();

        Thread.sleep(3000);                                // Espera a que los 3 terminen (como el Main de la guía Exp2)
        int stockFinal = libroDAO.buscarPorId(idLibro).getStock();
        System.out.println("Stock final: " + stockFinal + " (debe ser 0)");
    }

    // Lo que hace cada hilo: intentar el préstamo y mostrar cómo le fue
    private static void pedirLibro(ControladorPrestamos controlador, int idEstudiante, int idLibro) {
        try {
            if (controlador.registrarPrestamo(idEstudiante, idLibro)) {
                System.out.println("Estudiante " + idEstudiante + ": PRÉSTAMO OK");
            } else {
                System.out.println("Estudiante " + idEstudiante + ": no se pudo registrar");
            }
        } catch (IllegalArgumentException e) {             // Sin stock
            System.out.println("Estudiante " + idEstudiante + ": RECHAZADO (" + e.getMessage() + ")");
        }
    }
}
