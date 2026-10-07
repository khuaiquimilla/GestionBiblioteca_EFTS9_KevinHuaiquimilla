package vista;

import controlador.ControladorLibros;
import modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Ventana CRUD de libros: formulario arriba, tabla al centro, botones abajo
public class VentanaLibros extends JFrame {

    // Componentes del formulario
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtIsbn;
    private JTextField txtEditorial;
    private JSpinner spnStock;
    private JComboBox<Categoria> cmbCategoria;

    // Tabla
    private JTable tblLibros;
    private DefaultTableModel modelo;

    // Botones
    private JButton btnAgregar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    private final ControladorLibros controlador;
    private int idSeleccionado = -1;               // -1 = no hay ningún libro seleccionado

    public VentanaLibros(ControladorLibros controlador) {
        this.controlador = controlador;
        setTitle("Biblioteca Escolar - Gestión de libros");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);  // Cierra solo esta ventana; el menú sigue abierto
        setSize(850, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
        add(crearBotones(), BorderLayout.SOUTH);

        cargarCategorias();
        controlador.cargarTabla(modelo);            // Llena la tabla desde MySQL al abrir
    }

    // --- Armado de la ventana ---

    private JPanel crearFormulario() {
        // 3 filas x 4 columnas: etiqueta | campo | etiqueta | campo
        JPanel panel = new JPanel(new GridLayout(3, 4, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        txtTitulo = new JTextField();
        txtAutor = new JTextField();
        txtIsbn = new JTextField();
        txtEditorial = new JTextField();
        spnStock = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));   // valor inicial, mínimo, máximo, salto
        cmbCategoria = new JComboBox<>();

        panel.add(new JLabel("Título:"));
        panel.add(txtTitulo);
        panel.add(new JLabel("Autor:"));
        panel.add(txtAutor);
        panel.add(new JLabel("ISBN:"));
        panel.add(txtIsbn);
        panel.add(new JLabel("Editorial:"));
        panel.add(txtEditorial);
        panel.add(new JLabel("Stock:"));
        panel.add(spnStock);
        panel.add(new JLabel("Categoría:"));
        panel.add(cmbCategoria);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Título", "Autor", "ISBN", "Editorial", "Stock", "Categoría"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;                       // Las celdas no se editan a mano, solo con el formulario
            }
        };
        tblLibros = new JTable(modelo);
        tblLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);   // Una fila a la vez

        // Al hacer clic en una fila, sus datos pasan al formulario (igual que la guía)
        tblLibros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
        return new JScrollPane(tblLibros);          // El scroll muestra los títulos de las columnas y la barra
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAgregar = new JButton("Agregar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnAgregar.addActionListener(e -> agregarLibro());
        btnEditar.addActionListener(e -> editarLibro());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panel.add(btnAgregar);
        panel.add(btnEditar);
        panel.add(btnEliminar);
        panel.add(btnLimpiar);
        return panel;
    }

    private void cargarCategorias() {
        cmbCategoria.removeAllItems();
        for (Categoria c : controlador.listarCategorias()) {
            cmbCategoria.addItem(c);                // El combo muestra lo que diga toString() de Categoria
        }
    }

    // --- Acciones ---

    private void seleccionarFila() {
        int fila = tblLibros.getSelectedRow();
        if (fila >= 0) {
            idSeleccionado = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
            txtTitulo.setText(modelo.getValueAt(fila, 1).toString());
            txtAutor.setText(modelo.getValueAt(fila, 2).toString());
            txtIsbn.setText(modelo.getValueAt(fila, 3).toString());
            txtEditorial.setText(modelo.getValueAt(fila, 4).toString());
            spnStock.setValue(modelo.getValueAt(fila, 5));

            // Busca en el combo la categoría con el mismo nombre que la celda
            String nombreCategoria = modelo.getValueAt(fila, 6).toString();
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                if (cmbCategoria.getItemAt(i).getNombre().equals(nombreCategoria)) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void agregarLibro() {
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
        try {
            boolean ok = controlador.agregarLibro(
                    txtTitulo.getText().trim(),
                    txtAutor.getText().trim(),
                    txtIsbn.getText().trim(),
                    txtEditorial.getText().trim(),
                    (int) spnStock.getValue(),
                    categoria.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Libro agregado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar. Revisa que el ISBN no esté repetido.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {     // Lo lanza el controlador si falta un dato
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editarLibro() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para editar.");
            return;                                 // Sale del método sin hacer nada más
        }
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
        try {
            boolean ok = controlador.actualizarLibro(
                    idSeleccionado,
                    txtTitulo.getText().trim(),
                    txtAutor.getText().trim(),
                    txtIsbn.getText().trim(),
                    txtEditorial.getText().trim(),
                    (int) spnStock.getValue(),
                    categoria.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Libro actualizado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarLibro() {
        if (idSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para eliminar.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar este libro?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (controlador.eliminarLibro(idSeleccionado)) {
                JOptionPane.showMessageDialog(this, "Libro eliminado.");
                controlador.cargarTabla(modelo);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar: el libro tiene préstamos registrados.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        spnStock.setValue(1);
        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        idSeleccionado = -1;
        tblLibros.clearSelection();
    }
}
