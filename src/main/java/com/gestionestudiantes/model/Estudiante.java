package com.gestionestudiantes.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Representa a un estudiante registrado en el sistema.
 *
 * Las restricciones de Jakarta Bean Validation se aplican automáticamente
 * cuando el formulario JSF procesa los valores del objeto.
 */
public class Estudiante implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = 100, message = "Los nombres no deben superar los 100 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = 100, message = "Los apellidos no deben superar los 100 caracteres.")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio.")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe contener exactamente 8 dígitos.")
    private String dni;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Ingrese un correo electrónico válido.")
    @Size(max = 120, message = "El correo no debe superar los 120 caracteres.")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 15, message = "El teléfono no debe superar los 15 caracteres.")
    @Pattern(regexp = "^[0-9+()\\-\\s]{6,15}$", message = "El teléfono debe tener entre 6 y 15 caracteres válidos.")
    private String telefono;

    private Timestamp fechaRegistro;

    public Estudiante() {
    }

    public Estudiante(Integer id, String nombres, String apellidos, String dni,
                      String correo, String telefono, Timestamp fechaRegistro) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.correo = correo;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Nombre completo usado en las vistas y en el dashboard.
     */
    public String getNombreCompleto() {
        String nombre = nombres == null ? "" : nombres.trim();
        String apellido = apellidos == null ? "" : apellidos.trim();
        return (nombre + " " + apellido).trim();
    }

    /**
     * Iniciales usadas en el avatar de la tabla y del dashboard.
     */
    public String getIniciales() {
        String inicialNombre = primeraLetra(nombres);
        String inicialApellido = primeraLetra(apellidos);
        String iniciales = (inicialNombre + inicialApellido).trim();
        return iniciales.isEmpty() ? "?" : iniciales.toUpperCase(java.util.Locale.ROOT);
    }

    private String primeraLetra(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return valor.trim().substring(0, 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Estudiante estudiante)) {
            return false;
        }
        return id != null && Objects.equals(id, estudiante.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Estudiante{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                '}';
    }
}
