package vista;

import controlador.ControladorUsuarios;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

// Ventana de inicio de sesión
public class VentanaLogin extends JFrame {

    // Componentes de la ventana
    private JTextField txtCorreo;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnSalir;

    private final ControladorUsuarios controladorUsuarios;

    // Recibe el controlador desde el Main
    public VentanaLogin(ControladorUsuarios controladorUsuarios) {
        this.controladorUsuarios = controladorUsuarios;
        setTitle("Biblioteca Escolar - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(350, 180);
        setLocationRelativeTo(null);               // Centra la ventana en la pantalla
        setResizable(false);

        // Panel con GridLayout: 3 filas y 2 columnas (etiqueta | campo)
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));   // Margen interior

        txtCorreo = new JTextField();
        txtContrasena = new JPasswordField();      // Muestra puntitos en vez de la clave
        btnIngresar = new JButton("Ingresar");
        btnSalir = new JButton("Salir");

        panel.add(new JLabel("Correo:"));
        panel.add(txtCorreo);
        panel.add(new JLabel("Contraseña:"));
        panel.add(txtContrasena);
        panel.add(btnIngresar);
        panel.add(btnSalir);

        add(panel);                                // Agrega el panel a la ventana

        // Eventos de los botones (ActionListener escrito como lambda)
        btnIngresar.addActionListener(e -> autenticarUsuario());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void autenticarUsuario() {
        String correo = txtCorreo.getText().trim();                    // trim() quita espacios sobrantes
        String contrasena = new String(txtContrasena.getPassword());   // getPassword() entrega char[], lo pasamos a String

        Usuario usuario = controladorUsuarios.autenticar(correo, contrasena);

        if (usuario != null) {
            JOptionPane.showMessageDialog(this, "Bienvenido/a, " + usuario.getNombre());
            new VentanaMenuPrincipal(usuario).setVisible(true);   // Abre el menú y le pasa el usuario logueado
            this.dispose();                                       // Cierra solo el login
        } else {
            JOptionPane.showMessageDialog(this, "Correo o contraseña incorrectos",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
