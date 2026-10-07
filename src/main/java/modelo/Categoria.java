package modelo;

public class Categoria {

    // Atributos de categoría
    private int id;
    private String nombre;

    // Constructor
    public Categoria(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // El JComboBox muestra lo que retorna toString(); sin esto saldría "modelo.Categoria@1b6d3586"
    @Override
    public String toString() {
        return nombre;
    }
}
