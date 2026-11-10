package com.gestionestudiantes.config;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Punto único de acceso al DataSource configurado en Payara.
 *
 * El recurso JNDI se crea en Payara con el nombre jdbc/GestionEstudiantes
 * y apunta al JDBC Connection Pool que administra las conexiones a MariaDB.
 * No se almacenan credenciales en el código de la aplicación.
 */
@ApplicationScoped
public class DatabaseConfig {

    public static final String JNDI_NAME = "jdbc/GestionEstudiantes";

    @Resource(lookup = JNDI_NAME)
    private DataSource dataSource;

    /**
     * Obtiene una conexión del pool administrado por Payara.
     *
     * @return conexión JDBC
     * @throws SQLException si el DataSource no está disponible o el pool falla
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException(
                    "No se pudo inyectar el DataSource JNDI '" + JNDI_NAME + "'. " +
                    "Verifique que el recurso JDBC y el pool estén creados y habilitados en Payara.");
        }
        return dataSource.getConnection();
    }
}
