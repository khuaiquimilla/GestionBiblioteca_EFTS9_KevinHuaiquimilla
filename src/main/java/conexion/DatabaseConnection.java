package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Patrón Singleton: Una unica conexión compartida por toda la aplicación
public class DatabaseConnection {

    // Datos de conexión a MySQL (base de datos "biblioteca").
    private static final String URL = "jdbc:mysql://localhost:3306/biblioteca";
    private static final String USER = "root";
    private static final String PASSWORD = "Tremonti@0511";

    // Esta variable guarda la instancia única
    private static DatabaseConnection instancia;
    private Connection conexion;                         // Esta es la conexión que ese objeto mantiene abierta


    // Constructor privado para que no se pueda instanciar directamente
    private DatabaseConnection() throws SQLException {
        conexion = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Esta es la puerta de entrada a la clase
    public static DatabaseConnection getInstance() throws SQLException {
        if (instancia == null) {                         // ¿Todavía no existe?
            instancia = new DatabaseConnection();        // Entonces se crea (y se conecta)
        }
        return instancia;                                // Siempre devuelve el mismo
    }

    // Los DAO usaran este método para pedir la conexión
    public Connection getConnection() {
        return conexion;
    }
}