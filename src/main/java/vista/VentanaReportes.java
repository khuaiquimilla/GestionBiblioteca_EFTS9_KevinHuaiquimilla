package vista;

import controlador.ControladorReportes;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Ventana de reportes: 3 botones y una tabla que cambia según el reporte elegido
public class VentanaReportes extends JFrame {

    private JButton btnMasPrestados;
    private JButton btnEnPrestamo;
    private JComboBox<Estudiante> cmbEstudiante;
    private JButton btnHistorial;

    private JTable tblReporte;
    private JLabel lblResumen;                     // Dice qué reporte se está viendo

    private final ControladorReportes controlador;
    private final boolean esEstudiante;

    public VentanaReportes(ControladorReportes controlador, Usuario usuario) {
        this.controlador = controlador;
        this.esEstudiante = usuario.getRol().equalsIgnoreCase("estudiante");
        setTitle("Biblioteca Escolar - Reportes");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(850, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(crearBotones(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);

        // El JPanel usa FlowLayout por defecto, que deja el texto centrado
        JPanel panelResumen = new JPanel();
        lblResumen = new JLabel(" ");
        panelResumen.add(lblResumen);
        add(panelResumen, BorderLayout.SOUTH);

        cargarEstudiantes(usuario);
        verMasPrestados();                         // Al abrir se muestra el primer reporte
    }

    // --- Armado de la ventana ---

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnMasPrestados = new JButton("Libros más prestados");
        btnEnPrestamo = new JButton("Libros en préstamo");
        cmbEstudiante = new JComboBox<>();
        btnHistorial = new JButton("Ver historial");

        btnMasPrestados.addActionListener(e -> verMasPrestados());
        btnEnPrestamo.addActionListener(e -> verEnPrestamo());
        btnHistorial.addActionListener(e -> verHistorial());

        panel.add(btnMasPrestados);
        panel.add(btnEnPrestamo);
        panel.add(new JLabel("   Estudiante:"));
        panel.add(cmbEstudiante);
        panel.add(btnHistorial);
        return panel;
    }

    private JScrollPane crearTabla() {
        tblReporte = new JTable();                 // Vacía al principio: cada reporte le pone su modelo
        return new JScrollPane(tblReporte);
    }

    // Cada reporte tiene columnas distintas, así que cada vez se crea un modelo nuevo
    // con sus títulos y se le pone a la tabla con setModel (como en la guía S8)
    private DefaultTableModel crearModelo(String[] columnas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                      // La tabla es solo para mirar
            }
        };
        tblReporte.setModel(modelo);
        return modelo;
    }

    // Bibliotecario: todos los estudiantes. Estudiante: solo él y no puede cambiarlo.
    private void cargarEstudiantes(Usuario usuario) {
        if (esEstudiante) {
            Estudiante propio = controlador.buscarEstudiantePorRut(usuario.getRut());
            if (propio != null) {
                cmbEstudiante.addItem(propio);
            } else {
                btnHistorial.setEnabled(false);    // Sin ficha de estudiante no hay historial que mostrar
            }
            cmbEstudiante.setEnabled(false);
            btnEnPrestamo.setEnabled(false);       // Muestra préstamos de otros: solo para el bibliotecario
        } else {
            for (Estudiante e : controlador.listarEstudiantes()) {
                cmbEstudiante.addItem(e);
            }
        }
    }

    // --- Acciones: la ventana pone los títulos; el controlador llena las filas ---

    private void verMasPrestados() {
        String[] columnas = {"Libro", "Autor", "Veces prestado"};
        DefaultTableModel modelo = crearModelo(columnas);
        controlador.cargarMasPrestados(modelo);
        lblResumen.setText("Reporte: libros más prestados, de mayor a menor.");
    }

    private void verEnPrestamo() {
        String[] columnas = {"ID", "Libro", "Estudiante", "Vence", "Estado"};
        DefaultTableModel modelo = crearModelo(columnas);
        controlador.cargarEnPrestamo(modelo);
        lblResumen.setText("Reporte: libros que están prestados ahora.");
    }

    private void verHistorial() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        if (estudiante == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante.");
            return;
        }
        String[] columnas = {"ID", "Libro", "Fecha préstamo", "Vence", "Estado"};
        DefaultTableModel modelo = crearModelo(columnas);
        controlador.cargarHistorial(modelo, estudiante.getId());
        lblResumen.setText("Reporte: historial de " + estudiante.getNombre() + ".");
    }
}
