package com.gestionestudiantes.dao;

import com.gestionestudiantes.config.DatabaseConfig;
import com.gestionestudiantes.model.Estudiante;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Acceso a datos de estudiantes mediante JDBC puro y PreparedStatement.
 *
 * Todas las conexiones, sentencias y result sets se cierran con
 * try-with-resources para devolver las conexiones al pool de Payara.
 */
@ApplicationScoped
public class EstudianteDAO {

    private static final String SELECT_BASE = """
            SELECT id, nombres, apellidos, dni, correo, telefono, fecha_registro
            FROM estudiantes
            """;

    private static final String ORDER_BY = " ORDER BY apellidos ASC, nombres ASC";

    @Inject
    private DatabaseConfig databaseConfig;

    public List<Estudiante> listar() throws SQLException {
        String sql = SELECT_BASE + ORDER_BY;
        List<Estudiante> estudiantes = new ArrayList<>();

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                estudiantes.add(mapear(resultado));
            }
        }
        return estudiantes;
    }

    /**
     * Busca por nombres, apellidos o DNI usando parámetros preparados.
     * La búsqueda es insensible a mayúsculas/minúsculas.
     */
    public List<Estudiante> buscar(String termino) throws SQLException {
        String sql = SELECT_BASE +
                " WHERE LOWER(nombres) LIKE ? " +
                "    OR LOWER(apellidos) LIKE ? " +
                "    OR dni LIKE ?" +
                ORDER_BY;

        String patron = "%" + termino.trim().toLowerCase(Locale.ROOT) + "%";
        List<Estudiante> estudiantes = new ArrayList<>();

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, patron);
            sentencia.setString(2, patron);
            sentencia.setString(3, patron);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    estudiantes.add(mapear(resultado));
                }
            }
        }
        return estudiantes;
    }

    public List<Estudiante> listarRecientes(int limite) throws SQLException {
        String sql = SELECT_BASE + " ORDER BY fecha_registro DESC, id DESC LIMIT ?";
        List<Estudiante> estudiantes = new ArrayList<>();

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, limite);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    estudiantes.add(mapear(resultado));
                }
            }
        }
        return estudiantes;
    }

    public long contar() throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiantes";

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            return resultado.next() ? resultado.getLong(1) : 0L;
        }
    }

    public long contarNuevosEsteMes() throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM estudiantes
                WHERE fecha_registro >= DATE_FORMAT(CURRENT_DATE, '%Y-%m-01')
                """;

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            return resultado.next() ? resultado.getLong(1) : 0L;
        }
    }

    public boolean existeDni(String dni, Integer idExcluir) throws SQLException {
        String sql = "SELECT 1 FROM estudiantes WHERE dni = ?" +
                (idExcluir != null ? " AND id <> ?" : "") +
                " LIMIT 1";

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, dni);
            if (idExcluir != null) {
                sentencia.setInt(2, idExcluir);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public boolean existeCorreo(String correo, Integer idExcluir) throws SQLException {
        String sql = "SELECT 1 FROM estudiantes WHERE correo = ?" +
                (idExcluir != null ? " AND id <> ?" : "") +
                " LIMIT 1";

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, correo);
            if (idExcluir != null) {
                sentencia.setInt(2, idExcluir);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public void insertar(Estudiante estudiante) throws SQLException {
        String sql = """
                INSERT INTO estudiantes (nombres, apellidos, dni, correo, telefono)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            asignarParametros(estudiante, sentencia);

            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo registrar el estudiante.");
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    estudiante.setId(claves.getInt(1));
                }
            }
        }
    }

    public void actualizar(Estudiante estudiante) throws SQLException {
        String sql = """
                UPDATE estudiantes
                SET nombres = ?, apellidos = ?, dni = ?, correo = ?, telefono = ?
                WHERE id = ?
                """;

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            asignarParametros(estudiante, sentencia);
            sentencia.setInt(6, estudiante.getId());

            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se encontró el estudiante que se desea actualizar.");
            }
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";

        try (Connection conexion = databaseConfig.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            int filas = sentencia.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se encontró el estudiante que se desea eliminar.");
            }
        }
    }

    private void asignarParametros(Estudiante estudiante, PreparedStatement sentencia) throws SQLException {
        sentencia.setString(1, estudiante.getNombres());
        sentencia.setString(2, estudiante.getApellidos());
        sentencia.setString(3, estudiante.getDni());
        sentencia.setString(4, estudiante.getCorreo());
        sentencia.setString(5, estudiante.getTelefono());
    }

    private Estudiante mapear(ResultSet resultado) throws SQLException {
        return new Estudiante(
                resultado.getInt("id"),
                resultado.getString("nombres"),
                resultado.getString("apellidos"),
                resultado.getString("dni"),
                resultado.getString("correo"),
                resultado.getString("telefono"),
                resultado.getTimestamp("fecha_registro")
        );
    }
}
