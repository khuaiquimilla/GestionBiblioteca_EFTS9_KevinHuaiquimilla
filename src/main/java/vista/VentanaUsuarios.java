package vista;

import controlador.ControladorUsuarios;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Ventana CRUD de usuarios
// Diferencias: JPasswordField para la contraseña y JComboBox<String> para el rol
public class VentanaUsuarios extends JFrame {

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtRut;
    private JTextField txtCorreo;
    private JPasswordField txtContrasena;           // Muestra puntitos en vez de letras
    private JComboBox<String> cmbRol;

    // Tabla
    private JTable tblUsuarios;
    private DefaultTableModel modelo;

    // Botones
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    private final ControladorUsuarios controlador;
    private final Usuario usuarioSesion;           // Quién está conectado (no puede borrarse a sí mismo)
    private int idSeleccionado = -1;               // -1 = no hay ningún usuario seleccionado

    public VentanaUsuarios(ControladorUsuarios controlador, Usuario usuarioSesion) {
        this.controlador = controlador;
        this.usuarioSesion = usuarioSesion;
        setTitle("Biblioteca Escolar - Gestión de usuarios");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);  // Cierra solo esta ventana; el menú sigue abierto
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        controlador.cargarTabla(modelo);            // Llena la tabla desde MySQL al abrir
    }

    // --- Armado de la ventana ---

    private JPanel crearFormulario() {
        // 3 filas x 4 columnas: etiqueta | campo | etiqueta | campo
        JPanel panel = new JPanel(new GridLayout(3, 4, 8, 8));

        txtNombre = new JTextField();
        txtRut = new JTextField();
        txtCorreo = new JTextField();
        txtContrasena = new JPasswordField();
        // Las dos opciones son las mismas del ENUM de la tabla usuarios
        cmbRol = new JComboBox<>(new String[]{"bibliotecario", "estudiante"});

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("RUT:"));
        panel.add(txtRut);
        panel.add(new JLabel("Correo:"));
        panel.add(txtCorreo);
        panel.add(new JLabel("Contraseña:"));
        panel.add(txtContrasena);
        panel.add(new JLabel("Rol:"));
        panel.add(cmbRol);
        panel.add(new JLabel(""));                 // Celda vacía para completar la fila
        panel.add(new JLabel("Al editar: vacía = no cambia"));
        return panel;
    }

    private JScrollPane crearTabla() {
        // Sin columna de contraseña: no se muestra en pantalla
        String[] columnas = {"ID", "Nombre", "RUT", "Correo", "Rol"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                       // Las celdas no se editan a mano, solo con el formulario
            }
        };
        tblUsuarios = new JTable(modelo);
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Al hacer clic en una fila, sus datos pasan al formulario
        tblUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
        return new JScrollPane(tblUsuarios);
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAgregar = new JButton("Agregar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(e -> agregarUsuario());
        btnEditar.addActionListener(e -> editarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panel.add(btnAgregar);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        panel.add(btnLimpiar);
        return panel;
    }

    // --- Acciones ---

    private void seleccionarFila() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila >= 0) {
            idSeleccionado = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            txtNombre.setText(modelo.getValueAt(fila, 1).toString());
            txtRut.setText(modelo.getValueAt(fila, 2).toString());
            txtCorreo.setText(modelo.getValueAt(fila, 3).toString());
            txtContrasena.setText("");              // La contraseña no está en la tabla: parte vacía
            cmbRol.setSelectedItem(modelo.getValueAt(fila, 4).toString());
        }
    }

    // getPassword() devuelve un arreglo de char; new String(...) lo convierte a texto (como en la guía S6)
    private String leerContrasena() {
        return new String(txtContrasena.getPassword()).trim();
    }

    private void agregarUsuario() {
        try {
            boolean ok = controlador.agregarUsuario(
                    txtNombre.getText().trim(),
                    txtRut.getText().trim(),
                    txtCorreo.getText().trim(),
                    leerContrasena(),
                    (String) cmbRol.getSelectedItem());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Usuario agregado.");
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

    private void editarUsuario() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla para editar.");
            return;
        }
        try {
            boolean ok = controlador.actualizarUsuario(
                    idSeleccionado,
                    txtNombre.getText().trim(),
                    txtRut.getText().trim(),
                    txtCorreo.getText().trim(),
                    leerContrasena(),
                    (String) cmbRol.getSelectedItem());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Usuario actualizado.");
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

    private void eliminarUsuario() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla para eliminar.");
            return;
        }
        if (idSeleccionado == usuarioSesion.getId()) {   // Si se borra a sí mismo, nadie podría volver a entrar
            JOptionPane.showMessageDialog(this, "No puedes eliminar el usuario con el que iniciaste sesión.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar este usuario?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (controlador.eliminarUsuario(idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtRut.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        cmbRol.setSelectedIndex(0);
        idSeleccionado = -1;
        tblUsuarios.clearSelection();
    }
}
