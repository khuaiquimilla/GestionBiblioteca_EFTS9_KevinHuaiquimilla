package controlador;

import dao.UsuarioDAO;
import dao.impl.UsuarioDAOImpl;
import modelo.Usuario;

import java.sql.SQLException;

// Controlador de usuarios: intermediario entre las vistas y el DAO
// La vista nunca usa el DAO directamente, siempre pasa por aquí
public class ControladorUsuarios {

    private final UsuarioDAO usuarioDAO;           // Tipo = interfaz, objeto = la Impl

    // throws SQLException porque UsuarioDAOImpl pide la conexión al Singleton
    public ControladorUsuarios() throws SQLException {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    // Valida que no vengan campos vacíos y le pregunta al DAO
    // Retorna el Usuario si las credenciales son correctas, o null si no
    public Usuario autenticar(String correo, String contrasena) {
        if (correo.isEmpty() || contrasena.isEmpty()) {
            return null;                           // Ni siquiera vamos a la base de datos
        }
        return usuarioDAO.login(correo, contrasena);
    }
}
