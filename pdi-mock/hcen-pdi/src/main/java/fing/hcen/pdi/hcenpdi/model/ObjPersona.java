package fing.hcen.pdi.hcenpdi.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Person object representing citizen data from DNIC
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjPersona", propOrder = {
    "nroDocumento",
    "tipoDocumento",
    "nombre1",
    "nombre2",
    "apellido1",
    "apellido2",
    "fechaNacimiento",
    "sexo",
    "nacionalidad"
})
public class ObjPersona {

    @XmlElement(name = "NroDocumento", required = true)
    private String nroDocumento;

    @XmlElement(name = "TipoDocumento", required = true)
    private String tipoDocumento;

    @XmlElement(name = "Nombre1", required = true)
    private String nombre1;

    @XmlElement(name = "Nombre2")
    private String nombre2;

    @XmlElement(name = "Apellido1", required = true)
    private String apellido1;

    @XmlElement(name = "Apellido2")
    private String apellido2;

    @XmlElement(name = "FechaNacimiento", required = true)
    private String fechaNacimiento; // Format: YYYY-MM-DD

    @XmlElement(name = "Sexo", required = true)
    private String sexo; // M or F

    @XmlElement(name = "Nacionalidad")
    private String nacionalidad;

    public ObjPersona() {
    }

    public ObjPersona(String nroDocumento, String tipoDocumento, String nombre1, String apellido1,
                     String fechaNacimiento, String sexo) {
        this.nroDocumento = nroDocumento;
        this.tipoDocumento = tipoDocumento;
        this.nombre1 = nombre1;
        this.apellido1 = apellido1;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.nacionalidad = "UY"; // Default to Uruguayan
    }

    // Getters and Setters
    public String getNroDocumento() {
        return nroDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombre1() {
        return nombre1;
    }

    public void setNombre1(String nombre1) {
        this.nombre1 = nombre1;
    }

    public String getNombre2() {
        return nombre2;
    }

    public void setNombre2(String nombre2) {
        this.nombre2 = nombre2;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }
}
