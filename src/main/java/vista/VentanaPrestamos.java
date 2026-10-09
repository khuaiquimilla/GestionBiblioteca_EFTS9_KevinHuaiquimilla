package vista;

import controlador.ControladorPrestamos;
import hilo.TareaDevolucion;
import hilo.TareaPrestamo;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Ventana de préstamos y devoluciones.
// Cada préstamo o devolución corre en un hilo aparte (TareaPrestamo / TareaDevolucion),
// así la ventana se puede seguir usando mientras se guarda en MySQL.
public class VentanaPrestamos extends JFrame {

    // Formulario
    private JComboBox<Estudiante> cmbEstudiante;   // Muestra lo que diga toString() de Estudiante
    private JComboBox<Libro> cmbLibro;             // Muestra título y stock (toString() de Libro)

    // Tabla
    private JTable tblPrestamos;
    private DefaultTableModel modelo;

    // Botones y mensaje de estado
    private JButton btnPrestar;
    private JButton btnDevolver;
    private JButton btnActualizar;
    private JLabel lblEstado;

    private final ControladorPrestamos controlador;
    private final boolean esEstudiante;
    private Estudiante estudianteSesion;           // Ficha del estudiante conectado (null si es bibliotecario)
    private int operacionesEnCurso = 0;            // Hilos que todavía no terminan

    public VentanaPrestamos(ControladorPrestamos controlador, Usuario usuario) {
        this.controlador = controlador;
        this.esEstudiante = usuario.getRol().equalsIgnoreCase("estudiante");
        setTitle("Biblioteca Escolar - Préstamos y devoluciones");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);  // Cierra solo esta ventana; el menú sigue abierto
        setSize(850, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        // Rol estudiante: solo ve y pide préstamos a su nombre (usuarios y estudiantes se unen por RUT)
        if (esEstudiante) {
            estudianteSesion = controlador.buscarEstudiantePorRut(usuario.getRut());
        }

        if (esEstudiante && estudianteSesion == null) {
            btnPrestar.setEnabled(false);
            btnDevolver.setEnabled(false);
            btnActualizar.setEnabled(false);
            lblEstado.setText("Tu usuario no tiene ficha de estudiante con el mismo RUT.");
        } else {
            cargarEstudiantes();
            actualizarDatos();
        }
    }

    // --- Armado de la ventana ---

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));

        cmbEstudiante = new JComboBox<>();
        cmbLibro = new JComboBox<>();

        panel.add(new JLabel("Estudiante:"));
        panel.add(cmbEstudiante);
        panel.add(new JLabel("Libro:"));
        panel.add(cmbLibro);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPrestamos = new JTable(modelo);
        tblPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(tblPrestamos);
    }

    private JPanel crearBotones() {
        // 2 filas: arriba los botones, abajo el mensaje de estado
        JPanel panel = new JPanel(new GridLayout(2, 1));
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        btnPrestar = new JButton("Prestar libro");
        btnDevolver = new JButton("Devolver seleccionado");
        btnActualizar = new JButton("Actualizar");

        btnPrestar.addActionListener(e -> prestar());
        btnDevolver.addActionListener(e -> devolver());
        btnActualizar.addActionListener(e -> actualizarDatos());

        filaBotones.add(btnPrestar);
        filaBotones.add(btnDevolver);
        filaBotones.add(btnActualizar);

        JPanel filaEstado = new JPanel();          // FlowLayout por defecto: el mensaje queda centrado
        lblEstado = new JLabel("Listo.");
        filaEstado.add(lblEstado);

        panel.add(filaBotones);
        panel.add(filaEstado);
        return panel;
    }

    // --- Carga de datos ---

    private void cargarEstudiantes() {
        cmbEstudiante.removeAllItems();
        if (esEstudiante) {
            cmbEstudiante.addItem(estudianteSesion);   // Solo él mismo
            cmbEstudiante.setEnabled(false);           // No puede pedir a nombre de otro
        } else {
            for (Estudiante e : controlador.listarEstudiantes()) {
                cmbEstudiante.addItem(e);
            }
        }
    }

    // Recarga los libros (el stock cambia) sin perder el libro que estaba elegido
    private void cargarLibros() {
        int idAnterior = -1;
        Libro elegido = (Libro) cmbLibro.getSelectedItem();
        if (elegido != null) {
            idAnterior = elegido.getId();
        }
        cmbLibro.removeAllItems();
        for (Libro l : controlador.listarLibros()) {
            cmbLibro.addItem(l);
            if (l.getId() == idAnterior) {
                cmbLibro.setSelectedItem(l);
            }
        }
    }

    private void actualizarDatos() {
        cargarLibros();
        controlador.cargarTabla(modelo, estudianteSesion);   // null = todos los préstamos
    }

    // --- Acciones ---

    private void prestar() {
        Estudiante estudiante = (Estudiante) cmbEstudiante.getSelectedItem();
        Libro libro = (Libro) cmbLibro.getSelectedItem();
        if (estudiante == null || libro == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante y un libro.");
            return;
        }
        // El trabajo pesado va en otro hilo; este método termina de inmediato y la ventana sigue libre
        iniciarHilo(new TareaPrestamo(controlador, this, estudiante.getId(), libro.getId()));
    }

    private void devolver() {
        int fila = tblPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla para devolverlo.");
            return;
        }
        int idPrestamo = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
        iniciarHilo(new TareaDevolucion(controlador, this, idPrestamo));
    }

    // Recibe cualquier Runnable (TareaPrestamo o TareaDevolucion): polimorfismo con interfaz
    private void iniciarHilo(Runnable tarea) {
        operacionesEnCurso++;
        lblEstado.setText("Procesando " + operacionesEnCurso + " operación(es)...");
        Thread hilo = new Thread(tarea);
        hilo.start();                              // start() crea el hilo nuevo y este ejecuta run()
    }

    // La llaman TareaPrestamo y TareaDevolucion con invokeLater cuando terminan
    public void operacionTerminada(String mensaje) {
        operacionesEnCurso--;
        if (operacionesEnCurso == 0) {
            lblEstado.setText("Listo.");
        } else {
            lblEstado.setText("Procesando " + operacionesEnCurso + " operación(es)...");
        }
        actualizarDatos();                         // Primero se ve el cambio en la tabla...
        JOptionPane.showMessageDialog(this, mensaje);   // ...y después el aviso
    }
}