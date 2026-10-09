package hilo;

import controlador.ControladorPrestamos;
import vista.VentanaPrestamos;

import javax.swing.*;

// Tarea que registra una devolución en un hilo aparte. Misma estructura que TareaPrestamo.
public class TareaDevolucion implements Runnable {

    private final ControladorPrestamos controlador;
    private final VentanaPrestamos ventana;
    private final int idPrestamo;

    public TareaDevolucion(ControladorPrestamos controlador, VentanaPrestamos ventana, int idPrestamo) {
        this.controlador = controlador;
        this.ventana = ventana;
        this.idPrestamo = idPrestamo;
    }

    @Override
    public void run() {
        String mensaje;
        try {
            Thread.sleep(1500);                    // Simula el tiempo de proceso
            boolean atrasado = controlador.estaAtrasado(idPrestamo);   // Se revisa antes de marcarlo devuelto
            if (controlador.registrarDevolucion(idPrestamo)) {
                if (atrasado) {
                    mensaje = "Devolución registrada CON ATRASO: el libro se entregó después de la fecha de vencimiento.";
                } else {
                    mensaje = "Devolución registrada a tiempo.";
                }
            } else {
                mensaje = "No se pudo registrar la devolución.";
            }
        } catch (IllegalArgumentException e) {     // Ej: el préstamo ya estaba devuelto
            mensaje = e.getMessage();
        } catch (InterruptedException e) {
            mensaje = "La devolución fue interrumpida.";
        }
        avisarVentana(mensaje);
    }

    private void avisarVentana(String mensaje) {
        SwingUtilities.invokeLater(() -> ventana.operacionTerminada(mensaje));
    }
}
