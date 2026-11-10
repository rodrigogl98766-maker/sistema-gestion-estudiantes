package com.gestionestudiantes.controller;

import com.gestionestudiantes.dao.EstudianteDAO;
import com.gestionestudiantes.model.Estudiante;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador de la vista de estudiantes.
 *
 * Coordina el formulario, la búsqueda y las operaciones CRUD
 * delegando el acceso a datos en EstudianteDAO.
 */
@Named("estudianteBean")
@ViewScoped
public class EstudianteBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EstudianteBean.class.getName());

    @Inject
    private EstudianteDAO estudianteDAO;

    private List<Estudiante> estudiantes = new ArrayList<>();
    private String filtro = "";
    private Estudiante estudiante = new Estudiante();
    private boolean edicion;

    @PostConstruct
    public void init() {
        cargarTodos();
    }

    public void cargarTodos() {
        try {
            estudiantes = estudianteDAO.listar();
        } catch (SQLException e) {
            manejarError("No se pudo cargar la lista de estudiantes.", e);
        }
    }

    public void buscar() {
        try {
            String termino = filtro == null ? "" : filtro.trim();
            estudiantes = termino.isEmpty()
                    ? estudianteDAO.listar()
                    : estudianteDAO.buscar(termino);
        } catch (SQLException e) {
            manejarError("No se pudo realizar la búsqueda.", e);
        }
    }

    public void limpiarFiltro() {
        filtro = "";
        cargarTodos();
    }

    public void nuevo() {
        estudiante = new Estudiante();
        edicion = false;
    }

    public void editar(Estudiante seleccionado) {
        if (seleccionado == null) {
            return;
        }

        estudiante = new Estudiante();
        estudiante.setId(seleccionado.getId());
        estudiante.setNombres(seleccionado.getNombres());
        estudiante.setApellidos(seleccionado.getApellidos());
        estudiante.setDni(seleccionado.getDni());
        estudiante.setCorreo(seleccionado.getCorreo());
        estudiante.setTelefono(seleccionado.getTelefono());
        estudiante.setFechaRegistro(seleccionado.getFechaRegistro());
        edicion = true;
    }

    public void guardar() {
        normalizar();

        try {
            if (estudianteDAO.existeDni(estudiante.getDni(), estudiante.getId())) {
                agregarErrorDeCampo("formEstudiante:dni", "DNI duplicado",
                        "Ya existe un estudiante registrado con este DNI.");
                return;
            }

            if (estudianteDAO.existeCorreo(estudiante.getCorreo(), estudiante.getId())) {
                agregarErrorDeCampo("formEstudiante:correo", "Correo duplicado",
                        "Ya existe un estudiante registrado con este correo electrónico.");
                return;
            }

            if (estudiante.getId() == null) {
                estudianteDAO.insertar(estudiante);
                agregarMensajeInfo("Estudiante registrado",
                        "El estudiante se registró correctamente.");
            } else {
                estudianteDAO.actualizar(estudiante);
                agregarMensajeInfo("Estudiante actualizado",
                        "Los datos del estudiante se actualizaron correctamente.");
            }

            edicion = estudiante.getId() != null;
            recargar();
            PrimeFaces.current().executeScript("PF('estudianteDialog').hide()");
        } catch (SQLException e) {
            if (esViolacionDeUnicidad(e)) {
                agregarMensajeError("Datos duplicados",
                        "El DNI o el correo electrónico ya están registrados. Verifique los datos e intente nuevamente.");
            } else {
                manejarError("No se pudo guardar el estudiante.", e);
            }
        }
    }

    public void eliminar(Estudiante seleccionado) {
        if (seleccionado == null || seleccionado.getId() == null) {
            return;
        }

        try {
            estudianteDAO.eliminar(seleccionado.getId());
            recargar();
            agregarMensajeInfo("Estudiante eliminado",
                    "El registro de " + seleccionado.getNombreCompleto() + " se eliminó correctamente.");
        } catch (SQLException e) {
            manejarError("No se pudo eliminar el estudiante.", e);
        }
    }

    private void recargar() throws SQLException {
        String termino = filtro == null ? "" : filtro.trim();
        estudiantes = termino.isEmpty()
                ? estudianteDAO.listar()
                : estudianteDAO.buscar(termino);
    }

    private void normalizar() {
        if (estudiante.getNombres() != null) {
            estudiante.setNombres(estudiante.getNombres().trim());
        }
        if (estudiante.getApellidos() != null) {
            estudiante.setApellidos(estudiante.getApellidos().trim());
        }
        if (estudiante.getDni() != null) {
            estudiante.setDni(estudiante.getDni().trim());
        }
        if (estudiante.getCorreo() != null) {
            estudiante.setCorreo(estudiante.getCorreo().trim().toLowerCase(Locale.ROOT));
        }
        if (estudiante.getTelefono() != null) {
            estudiante.setTelefono(estudiante.getTelefono().trim());
        }
    }

    private boolean esViolacionDeUnicidad(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }

    private void agregarErrorDeCampo(String clientId, String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(clientId,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resumen, detalle));
        FacesContext.getCurrentInstance().validationFailed();
    }

    private void agregarMensajeInfo(String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, resumen, detalle));
    }

    private void agregarMensajeError(String resumen, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, resumen, detalle));
    }

    private void manejarError(String mensaje, SQLException e) {
        LOGGER.log(Level.SEVERE, mensaje, e);
        agregarMensajeError("Error de base de datos",
                mensaje + " Verifique que MariaDB esté encendido y que el pool JNDI esté disponible.");
    }

    public String getTextoContador() {
        if (estudiantes == null || estudiantes.isEmpty()) {
            return (filtro != null && !filtro.isBlank())
                    ? "No se encontraron resultados"
                    : "Aún no hay estudiantes registrados";
        }

        boolean singular = estudiantes.size() == 1;
        String cantidad = singular ? "1 estudiante" : estudiantes.size() + " estudiantes";
        return (filtro != null && !filtro.isBlank())
                ? cantidad + (singular ? " encontrado" : " encontrados")
                : cantidad + (singular ? " registrado" : " registrados");
    }

    public String getTituloDialogo() {
        return edicion ? "Editar estudiante" : "Registrar nuevo estudiante";
    }

    public String getEtiquetaGuardar() {
        return edicion ? "Actualizar datos" : "Registrar estudiante";
    }

    public List<Estudiante> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(List<Estudiante> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public boolean isEdicion() {
        return edicion;
    }
}
