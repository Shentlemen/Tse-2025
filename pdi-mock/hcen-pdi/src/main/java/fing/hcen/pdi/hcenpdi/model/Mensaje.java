package fing.hcen.pdi.hcenpdi.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Error or warning message returned by PDI operations
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Mensaje", propOrder = {
    "codMensaje",
    "descripcion",
    "datoExtra"
})
public class Mensaje {

    @XmlElement(name = "CodMensaje", required = true)
    private String codMensaje;

    @XmlElement(name = "Descripcion", required = true)
    private String descripcion;

    @XmlElement(name = "DatoExtra")
    private String datoExtra;

    public Mensaje() {
    }

    public Mensaje(String codMensaje, String descripcion) {
        this.codMensaje = codMensaje;
        this.descripcion = descripcion;
    }

    public Mensaje(String codMensaje, String descripcion, String datoExtra) {
        this.codMensaje = codMensaje;
        this.descripcion = descripcion;
        this.datoExtra = datoExtra;
    }

    public String getCodMensaje() {
        return codMensaje;
    }

    public void setCodMensaje(String codMensaje) {
        this.codMensaje = codMensaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDatoExtra() {
        return datoExtra;
    }

    public void setDatoExtra(String datoExtra) {
        this.datoExtra = datoExtra;
    }
}
