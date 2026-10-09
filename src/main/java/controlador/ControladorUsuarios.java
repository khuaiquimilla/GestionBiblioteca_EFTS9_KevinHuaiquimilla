package controlador;

import dao.UsuarioDAO;
import dao.impl.UsuarioDAOImpl;
import modelo.Usuario;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;

// Controlador de usuarios: intermediario entre las vistas y el DAO (MVC)
// Lo usan dos ventanas: VentanaLogin (autenticar) y VentanaUsuarios (CRUD)
public class ControladorUsuarios {

    private final UsuarioDAO usuarioDAO;           // Tipo = interfaz, objeto = la Impl

    // throws SQLException porque UsuarioDAOImpl pide la conexión al Singleton
    public ControladorUsuarios() throws SQLException {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    // --- Login ---

    // Valida que no vengan campos vacíos y le pregunta al DAO
    // Retorna el Usuario si las credenciales son correctas, o null si no
    public Usuario autenticar(String correo, String contrasena) {
        if (correo.isEmpty() || contrasena.isEmpty()) {
            return null;                           // Ni siquiera vamos a la base de datos
        }
        return usuarioDAO.login(correo, contrasena);
    }

    // --- CRUD (VentanaUsuarios) ---

    // Valida los datos y lanza IllegalArgumentException con el mensaje que mostrará la vista
    // La contraseña no se revisa aquí porque al editar puede venir vacía (ver actualizarUsuario)
    private void validar(String nombre, String rut, String correo) {
        if (nombre.isEmpty() || rut.isEmpty() || correo.isEmpty()) {
            throw new IllegalArgumentException("Todos los campos deben estar completos.");
        }
    }

    // Retorna true si se guardó; false si MySQL lo rechazó (ej: RUT repetido)
    public boolean agregarUsuario(String nombre, String rut, String correo, String contrasena, String rol) {
        validar(nombre, rut, correo);
        if (contrasena.isEmpty()) {                // Un usuario nuevo sí necesita contraseña
            throw new IllegalArgumentException("Escribe una contraseña para el usuario nuevo.");
        }
        Usuario nuevo = new Usuario(0, nombre, rut, correo, contrasena, rol);   // id 0: lo pone MySQL
        return usuarioDAO.insertar(nuevo);
    }

    // Si la contraseña viene vacía, se mantiene la que el usuario ya tenía
    // (la tabla no muestra contraseñas, así que al editar el campo parte vacío)
    public boolean actualizarUsuario(int id, String nombre, String rut, String correo, String contrasena, String rol) {
        validar(nombre, rut, correo);
        if (contrasena.isEmpty()) {
            Usuario actual = usuarioDAO.buscarPorId(id);
            if (actual == null) {
                return false;                      // Ese usuario ya no existe
            }
            contrasena = actual.getContrasena();   // Se reutiliza la contraseña guardada en MySQL
        }
        Usuario editado = new Usuario(id, nombre, rut, correo, contrasena, rol);
        return usuarioDAO.actualizar(editado);
    }

    public boolean eliminarUsuario(int id) {
        return usuarioDAO.eliminar(id);
    }

    // Vacía la tabla y la vuelve a llenar con lo que hay en MySQL
    // La contraseña no se muestra en la tabla
    public void cargarTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        for (Usuario u : usuarioDAO.listarTodos()) {
            modelo.addRow(new Object[]{
                    u.getId(),
                    u.getNombre(),
                    u.getRut(),
                    u.getCorreo(),
                    u.getRol()
            });
        }
    }
}
