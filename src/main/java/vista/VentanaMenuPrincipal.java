package vista;

import controlador.ControladorEstudiantes;
import controlador.ControladorLibros;
import controlador.ControladorPrestamos;
import controlador.ControladorReportes;
import controlador.ControladorUsuarios;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// Menú principal: panel de navegación hacia cada módulo, armado a mano con layouts
public class VentanaMenuPrincipal extends JFrame {

    // Componentes de la ventana
    private JLabel lblBienvenida;
    private JButton btnLibros;
    private JButton btnEstudiantes;
    private JButton btnUsuarios;
    private JButton btnPrestamos;
    private JButton btnReportes;
    private JButton btnCerrarSesion;

    private final Usuario usuario;                 // Quién inició sesión

    // UN solo controlador de préstamos para todas las ventanas de préstamos que se abran.
    // synchronized bloquea por objeto: si cada ventana tuviera su propio controlador,
    // cada una tendría su propio candado y dos hilos podrían cambiar el stock a la vez.
    private ControladorPrestamos controladorPrestamos;

    public VentanaMenuPrincipal(Usuario usuario) {
        this.usuario = usuario;
        setTitle("Biblioteca Escolar - Rol: " + usuario.getRol());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));       // NORTH = saludo, CENTER = botones

        // Arriba: saludo con el método abstracto de Persona (polimorfismo).
        // El JPanel usa FlowLayout por defecto, que deja el saludo centrado.
        JPanel panelSaludo = new JPanel();
        lblBienvenida = new JLabel(usuario.getDescripcion());
        panelSaludo.add(lblBienvenida);
        add(panelSaludo, BorderLayout.NORTH);

        // Centro: un botón debajo de otro (GridLayout de 6 filas y 1 columna)
        JPanel panelBotones = new JPanel(new GridLayout(6, 1, 5, 5));

        btnLibros = new JButton("Libros");
        btnEstudiantes = new JButton("Estudiantes");
        btnUsuarios = new JButton("Usuarios");
        btnPrestamos = new JButton("Préstamos y devoluciones");
        btnReportes = new JButton("Reportes");
        btnCerrarSesion = new JButton("Cerrar sesión");

        panelBotones.add(btnLibros);
        panelBotones.add(btnEstudiantes);
        panelBotones.add(btnUsuarios);
        panelBotones.add(btnPrestamos);
        panelBotones.add(btnReportes);
        panelBotones.add(btnCerrarSesion);
        add(panelBotones, BorderLayout.CENTER);

        aplicarRestriccionesPorRol();
        inicializarBotones();
    }

    // Estudiante: solo préstamo, devolución y consulta (lo dice el enunciado)
    private void aplicarRestriccionesPorRol() {
        if (usuario.getRol().equalsIgnoreCase("estudiante")) {
            btnLibros.setEnabled(false);
            btnEstudiantes.setEnabled(false);
            btnUsuarios.setEnabled(false);
            setTitle(getTitle() + " (Vista restringida)");
        }
    }

    private void inicializarBotones() {
        btnLibros.addActionListener(e -> abrirLibros());
        btnEstudiantes.addActionListener(e -> abrirEstudiantes());
        btnUsuarios.addActionListener(e -> abrirUsuarios());
        btnPrestamos.addActionListener(e -> abrirPrestamos());
        btnReportes.addActionListener(e -> abrirReportes());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
    }

    // Cada ventana se abre sin cerrar el menú (todas usan DISPOSE_ON_CLOSE).
    // El try/catch es obligatorio porque los constructores de los controladores declaran throws SQLException.

    private void abrirLibros() {
        try {
            new VentanaLibros(new ControladorLibros()).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void abrirEstudiantes() {
        try {
            new VentanaEstudiantes(new ControladorEstudiantes()).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void abrirUsuarios() {
        try {
            // Se le pasa el usuario conectado para que no pueda eliminarse a sí mismo
            new VentanaUsuarios(new ControladorUsuarios(), usuario).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void abrirPrestamos() {
        try {
            if (controladorPrestamos == null) {    // Se crea la primera vez y después se reutiliza
                controladorPrestamos = new ControladorPrestamos();
            }
            new VentanaPrestamos(controladorPrestamos, usuario).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    private void abrirReportes() {
        try {
            new VentanaReportes(new ControladorReportes(), usuario).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }

    // Vuelve al login (sirve para probar los dos roles sin reiniciar el programa)
    private void cerrarSesion() {
        try {
            new VentanaLogin(new ControladorUsuarios()).setVisible(true);
            this.dispose();
        } catch (SQLException ex) {                // Java obliga el catch porque el constructor lo declara
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage());
        }
    }
}