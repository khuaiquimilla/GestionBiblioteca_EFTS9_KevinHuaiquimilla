package dao.impl;

import conexion.DatabaseConnection;
import dao.CategoriaDAO;
import modelo.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl implements CategoriaDAO {

    private final Connection conexion;

    // Obtiene la conexión única del Singleton.
    public CategoriaDAOImpl() throws SQLException {
        this.conexion = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public List<Categoria> listarTodos() {
        List<Categoria> categorias = new ArrayList<>();   // Lista para almacenar las categorías
        String sql = "SELECT * FROM categorias";
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();           // rs = las filas que devuelve MySQL
            while (rs.next()) {                           // Avanza fila por fila; false cuando no quedan más
                categorias.add(new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }
        return categorias;                                // Devuelve la lista (vacía si hubo error)
    }
}
