package controlador;

import dao.CategoriaDAO;
import dao.LibroDAO;
import dao.impl.CategoriaDAOImpl;
import dao.impl.LibroDAOImpl;
import modelo.Categoria;
import modelo.Libro;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

// Controlador de libros
public class ControladorLibros {

    private final LibroDAO libroDAO;               // Tipo = interfaz, objeto = la Impl
    private final CategoriaDAO categoriaDAO;       // Para el JComboBox y para mostrar el nombre de la categoría

    public ControladorLibros() throws SQLException {
        this.libroDAO = new LibroDAOImpl();
        this.categoriaDAO = new CategoriaDAOImpl();
    }

    // Valida los datos y lanza IllegalArgumentException con el mensaje que mostrará la vista
    private void validar(String titulo, String autor, String isbn, String editorial, int stock) {
        if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty() || editorial.isEmpty()) {
            throw new IllegalArgumentException("Todos los campos deben estar completos.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
    }

    // Retorna true si se guardó; false si MySQL lo rechazó (ej: ISBN repetido)
    public boolean agregarLibro(String titulo, String autor, String isbn, String editorial,
                                int stock, int idCategoria) {
        validar(titulo, autor, isbn, editorial, stock);
        Libro nuevo = new Libro(0, titulo, autor, isbn, editorial, stock, idCategoria);   // id 0: lo pone MySQL (AUTO_INCREMENT)
        return libroDAO.insertar(nuevo);
    }

    public boolean actualizarLibro(int id, String titulo, String autor, String isbn, String editorial,
                                   int stock, int idCategoria) {
        validar(titulo, autor, isbn, editorial, stock);
        Libro editado = new Libro(id, titulo, autor, isbn, editorial, stock, idCategoria);  // Aquí sí va el id real
        return libroDAO.actualizar(editado);
    }

    // false si el libro tiene préstamos (la llave foránea no deja borrarlo)
    public boolean eliminarLibro(int id) {
        return libroDAO.eliminar(id);
    }

    public List<Categoria> listarCategorias() {
        return categoriaDAO.listarTodos();
    }

    // Vacía la tabla y la vuelve a llenar con lo que hay en MySQL
    public void cargarTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);                              // Borra todas las filas
        List<Categoria> categorias = categoriaDAO.listarTodos();
        for (Libro l : libroDAO.listarTodos()) {
            modelo.addRow(new Object[]{
                    l.getId(),
                    l.getTitulo(),
                    l.getAutor(),
                    l.getIsbn(),
                    l.getEditorial(),
                    l.getStock(),
                    nombreCategoria(l.getIdCategoria(), categorias)   // Muestra "Novela" en vez de 1
            });
        }
    }

    // Busca en la lista la categoría con ese id y retorna su nombre
    private String nombreCategoria(int idCategoria, List<Categoria> categorias) {
        for (Categoria c : categorias) {
            if (c.getId() == idCategoria) {
                return c.getNombre();
            }
        }
        return "";                                          // No debería pasar: la FK obliga a que exista
    }
}
