package hilo;

import controlador.ControladorPrestamos;
import vista.VentanaPrestamos;

import javax.swing.*;

// Tarea que registra un préstamo en un hilo aparte.
// Mientras este hilo trabaja con MySQL, la ventana sigue respondiendo: no se congela.
public class TareaPrestamo implements Runnable {

    private final ControladorPrestamos controlador;   // El mismo para todos los hilos = el mismo candado
    private final VentanaPrestamos ventana;           // A quién avisar cuando termine
    private final int idEstudiante;
    private final int idLibro;

    public TareaPrestamo(ControladorPrestamos controlador, VentanaPrestamos ventana, int idEstudiante, int idLibro) {
        this.controlador = controlador;
        this.ventana = ventana;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
    }

    // run() es lo que ejecuta el hilo cuando la ventana llama a start()
    @Override
    public void run() {
        String mensaje;
        try {
            Thread.sleep(1500);                    // Simula el tiempo de proceso: la ventana no se congela
            if (controlador.registrarPrestamo(idEstudiante, idLibro)) {
                mensaje = "Préstamo registrado. Plazo de devolución: "
                        + ControladorPrestamos.DIAS_PRESTAMO + " días.";
            } else {
                mensaje = "No se pudo registrar el préstamo.";
            }
        } catch (IllegalArgumentException e) {     // Sin stock: lo lanza el controlador
            mensaje = e.getMessage();
        } catch (InterruptedException e) {         // Obligatorio por Thread.sleep
            mensaje = "El préstamo fue interrumpido.";
        }
        avisarVentana(mensaje);
    }

    // Los componentes de Swing solo se tocan desde el hilo de la interfaz.
    // invokeLater le "encarga" ese trabajo al hilo de Swing (igual que en el Main).
    private void avisarVentana(String mensaje) {
        SwingUtilities.invokeLater(() -> ventana.operacionTerminada(mensaje));
    }
}
