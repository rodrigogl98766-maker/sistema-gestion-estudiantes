package com.gestionestudiantes.controller;

import com.gestionestudiantes.dao.EstudianteDAO;
import com.gestionestudiantes.model.Estudiante;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Datos reales del dashboard: totales y últimos estudiantes registrados.
 */
@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DashboardBean.class.getName());
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private EstudianteDAO estudianteDAO;

    private long totalEstudiantes;
    private long nuevosEsteMes;
    private List<Estudiante> estudiantesRecientes = new ArrayList<>();
    private boolean conexionOk;
    private String ultimoRegistro = "Sin registros";
    private String ultimoRegistroFecha = "Aún no hay estudiantes";

    @PostConstruct
    public void init() {
        cargar();
    }

    public void cargar() {
        try {
            totalEstudiantes = estudianteDAO.contar();
            nuevosEsteMes = estudianteDAO.contarNuevosEsteMes();
            estudiantesRecientes = estudianteDAO.listarRecientes(5);
            conexionOk = true;

            if (estudiantesRecientes.isEmpty()) {
                ultimoRegistro = "Sin registros";
                ultimoRegistroFecha = "Aún no hay estudiantes";
            } else {
                Estudiante ultimo = estudiantesRecientes.get(0);
                ultimoRegistro = ultimo.getNombreCompleto();
                ultimoRegistroFecha = ultimo.getFechaRegistro() == null
                        ? "Fecha no disponible"
                        : FORMATO_FECHA.format(ultimo.getFechaRegistro().toLocalDateTime());
            }
        } catch (SQLException e) {
            conexionOk = false;
            totalEstudiantes = 0;
            nuevosEsteMes = 0;
            estudiantesRecientes = new ArrayList<>();
            ultimoRegistro = "No disponible";
            ultimoRegistroFecha = "Sin conexión a MariaDB";

            LOGGER.log(Level.SEVERE, "No se pudo cargar la información del dashboard.", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error de conexión",
                            "No se pudo consultar MariaDB. Revise el pool JNDI 'jdbc/GestionEstudiantes'."));
        }
    }

    public long getTotalEstudiantes() {
        return totalEstudiantes;
    }

    public long getNuevosEsteMes() {
        return nuevosEsteMes;
    }

    public List<Estudiante> getEstudiantesRecientes() {
        return estudiantesRecientes;
    }

    public boolean isConexionOk() {
        return conexionOk;
    }

    public String getUltimoRegistro() {
        return ultimoRegistro;
    }

    public String getUltimoRegistroFecha() {
        return ultimoRegistroFecha;
    }

    public String getEstadoConexion() {
        return conexionOk ? "Operativa" : "Con problemas";
    }

    public String getEstadoConexionClass() {
        return conexionOk ? "status-pill status-ok" : "status-pill status-error";
    }
}
