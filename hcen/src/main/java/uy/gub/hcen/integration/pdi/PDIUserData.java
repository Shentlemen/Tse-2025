package uy.gub.hcen.integration.pdi;

import java.time.LocalDate;

/**
 * PDI User Data DTO
 * <p>
 * Represents user identity data returned from PDI's Servicio Básico de Información de DNIC.
 * <p>
 * This DTO contains personal information retrieved from Uruguay's national identity database
 * for identity validation and age verification purposes during INUS registration.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-23
 */
public class PDIUserData {

    private String ci;
    private String nombreCompleto;
    private LocalDate fechaNacimiento;

    /**
     * Default constructor for JSON deserialization
     */
    public PDIUserData() {
    }

    /**
     * Constructor with all fields
     *
     * @param ci              Cédula de Identidad (national ID number)
     * @param nombreCompleto  Full name (first name + last name)
     * @param fechaNacimiento Date of birth
     */
    public PDIUserData(String ci, String nombreCompleto, LocalDate fechaNacimiento) {
        this.ci = ci;
        this.nombreCompleto = nombreCompleto;
        this.fechaNacimiento = fechaNacimiento;
    }

    // Getters and Setters

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    @Override
    public String toString() {
        return "PDIUserData{" +
                "ci='" + ci + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                '}';
    }
}
