package dao;
import modelo.Categoria;
import java.util.List;

// Contrato de operaciones sobre la tabla categorias
// Solo se necesita listar, para llenar el JComboBox de la ventana de libros
public interface CategoriaDAO {

    List<Categoria> listarTodos();
}
 