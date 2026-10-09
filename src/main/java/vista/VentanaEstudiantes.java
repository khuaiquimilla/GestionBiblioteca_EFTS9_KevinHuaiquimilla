package vista;

import controlador.ControladorEstudiantes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Ventana de estudiantes
public class VentanaEstudiantes extends JFrame {

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtRut;
    private JTextField txtCorreo;
    private JTextField txtCurso;

    // Tabla
    private JTable tblEstudiantes;
    private DefaultTableModel modelo;

    // Botones
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    private final ControladorEstudiantes controlador;
    private int idSeleccionado = -1;               // -1 = no hay ningún estudiante seleccionado

    public VentanaEstudiantes(ControladorEstudiantes controlador) {
        this.controlador = controlador;
        setTitle("Biblioteca Escolar - Gestión de estudiantes");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);  // Cierra solo esta ventana; el menú sigue abierto
        setSize(800, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        controlador.cargarTabla(modelo);            // Llena la tabla desde MySQL al abrir
    }

    // --- Armado de la ventana ---

    private JPanel crearFormulario() {
        // 2 filas x 4 columnas: etiqueta | campo | etiqueta | campo
        JPanel panel = new JPanel(new GridLayout(2, 4, 8, 8));

        txtNombre = new JTextField();
        txtRut = new JTextField();
        txtCorreo = new JTextField();
        txtCurso = new JTextField();

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("RUT:"));
        panel.add(txtRut);
        panel.add(new JLabel("Correo:"));
        panel.add(txtCorreo);
        panel.add(new JLabel("Curso:"));
        panel.add(txtCurso);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Nombre", "RUT", "Correo", "Curso"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                       // Las celdas no se editan a mano, solo con el formulario
            }
        };
        tblEstudiantes = new JTable(modelo);
        tblEstudiantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Al hacer clic en una fila, sus datos pasan al formulario
        tblEstudiantes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
        return new JScrollPane(tblEstudiantes);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAgregar = new JButton("Agregar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(e -> agregarEstudiante());
        btnEditar.addActionListener(e -> editarEstudiante());
        btnEliminar.addActionListener(e -> eliminarEstudiante());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panel.add(btnAgregar);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        panel.add(btnLimpiar);
        return panel;
    }

    // --- Acciones ---

    private void seleccionarFila() {
        int fila = tblEstudiantes.getSelectedRow();
        if (fila >= 0) {
            idSeleccionado = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            txtRut.setText(modelo.getValueAt(fila, 2).toString());
            txtCorreo.setText(modelo.getValueAt(fila, 3).toString());
            txtCurso.setText(modelo.getValueAt(fila, 4).toString());
        }
    }

    private void agregarEstudiante() {
        try {
            boolean ok = controlador.agregarEstudiante(
                    txtNombre.getText().trim(),
                    txtRut.getText().trim(),
                    txtCorreo.getText().trim(),
                    txtCurso.getText().trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Estudiante agregado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar. Revisa que el RUT no esté repetido.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {     // Lo lanza el controlador si falta un dato
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editarEstudiante() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante de la tabla para editar.");
            return;
        }
        try {
            boolean ok = controlador.actualizarEstudiante(
                    idSeleccionado,
                    txtNombre.getText().trim(),
                    txtRut.getText().trim(),
                    txtCorreo.getText().trim(),
                    txtCurso.getText().trim());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Estudiante actualizado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar. Revisa que el RUT no esté repetido.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarEstudiante() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante de la tabla para eliminar.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar este estudiante?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (controlador.eliminarEstudiante(idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Estudiante eliminado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar: el estudiante tiene préstamos registrados.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCorreo.setText("");
        txtCurso.setText("");
        idSeleccionado = -1;
        tblEstudiantes.clearSelection();
    }
}
