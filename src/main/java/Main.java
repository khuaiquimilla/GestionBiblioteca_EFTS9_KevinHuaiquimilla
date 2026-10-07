import controlador.ControladorUsuarios;
import vista.VentanaLogin;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // La interfaz gráfica se crea en el hilo de Swing (como en la guía)
        SwingUtilities.invokeLater(() -> {
            try {
                ControladorUsuarios controlador = new ControladorUsuarios();
                new VentanaLogin(controlador).setVisible(true);
            } catch (SQLException e) {             // MySQL apagado o contraseña mala en DatabaseConnection
                JOptionPane.showMessageDialog(null, "No se pudo conectar a MySQL: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
