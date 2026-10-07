package vista;

import controlador.ControladorLibros;
import controlador.ControladorUsuarios;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

// Menú principal: panel de navegación hacia cada módulo
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

    public VentanaMenuPrincipal(Usuario usuario) {
        this.usuario = usuario;
        setTitle("Biblioteca Escolar - Rol: " + usuario.getRol());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Arriba: saludo con el método abstracto de Persona (polimorfismo)
        lblBienvenida = new JLabel(usuario.getDescripcion(), SwingConstants.CENTER);
        add(lblBienvenida, BorderLayout.NORTH);

        // Centro: un botón debajo de otro (GridLayout de 6 filas y 1 columna)
        JPanel panelBotones = new JPanel(new GridLayout(6, 1, 5, 5));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

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
        // Los que aún muestran un aviso se reemplazan en los bloques 5, 6 y 7
        btnLibros.addActionListener(e -> abrirLibros());
        btnEstudiantes.addActionListener(e -> JOptionPane.showMessageDialog(this, "Gestión de estudiantes (bloque 5)"));
        btnUsuarios.addActionListener(e -> JOptionPane.showMessageDialog(this, "Gestión de usuarios (bloque 5)"));
        btnPrestamos.addActionListener(e -> JOptionPane.showMessageDialog(this, "Préstamos (bloque 6)"));
        btnReportes.addActionListener(e -> JOptionPane.showMessageDialog(this, "Reportes (bloque 7)"));
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
    }

    // Abre la ventana de libros sin cerrar el menú (VentanaLibros usa DISPOSE_ON_CLOSE)
    private void abrirLibros() {
        try {
            new VentanaLibros(new ControladorLibros()).setVisible(true);
        } catch (SQLException ex) {                // El constructor del controlador declara throws SQLException
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
