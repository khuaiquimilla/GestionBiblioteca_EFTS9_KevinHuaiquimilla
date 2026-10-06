package modelo;

public class Usuario extends Persona {

    // Atributos de usuario
    private String contrasena;
    private String rol;

    // Constructor
    public Usuario(int id, String nombre, String rut, String correo, String contrasena, String rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters y setters
    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String getDescripcion() {
        return "Usuario: " + getNombre() + " (" + getRol() + ")";
    }


}
