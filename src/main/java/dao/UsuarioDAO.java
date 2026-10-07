package dao;
import modelo.Usuario;
import java.util.List;

// Contrato de operaciones sobre la tabla usuarios
// UsuarioDAOImpl es la clase que implementa estos métodos con SQL
public interface UsuarioDAO {

    // --- CRUD ---
    boolean insertar(Usuario usuario);
    Usuario buscarPorId(int id);
    List<Usuario> listarTodos();
    boolean actualizar(Usuario usuario);
    boolean eliminar(int id);

    // --- Login ---
    Usuario login(String correo, String contrasena);   // null si el correo o la contraseña no coinciden
}
