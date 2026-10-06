import conexion.DatabaseConnection;
import java.sql.SQLException;                  // necesario para poder nombrar el error en el catch

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConnection a = DatabaseConnection.getInstance();  // 1ª vez: crea y conecta
            DatabaseConnection b = DatabaseConnection.getInstance();  // 2ª vez: devuelve la misma
            System.out.println("Conectado. ¿Es la misma instancia? " + (a == b));
        } catch (SQLException e) {             // si MySQL está apagado, la clave está mala, etc.
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }
}