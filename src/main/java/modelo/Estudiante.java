package modelo;

public class Estudiante extends Persona {

    // Atributos de estudiante
    private String curso;


    // Constructor
    public Estudiante(int id, String nombre, String rut, String correo, String curso) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    // Getters y setters

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    @Override
    public String getDescripcion() {
        return "Estudiante: " + getNombre() + " (" + getCurso() + ")";
    }
}
