package fing.hcen.pdi.hcenpdi.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Request parameters for obtPersonaPorDoc operation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamObtPersonaPorDoc", propOrder = {
    "organizacion",
    "passwordEntidad",
    "nroDocumento",
    "tipoDocumento"
})
public class ParamObtPersonaPorDoc {

    @XmlElement(name = "organizacion", required = true)
    private String organizacion;

    @XmlElement(name = "passwordEntidad", required = true)
    private String passwordEntidad;

    @XmlElement(name = "NroDocumento", required = true)
    private String nroDocumento;

    @XmlElement(name = "TipoDocumento", required = true)
    private String tipoDocumento; // CI = Cédula de Identidad

    public ParamObtPersonaPorDoc() {
    }

    public ParamObtPersonaPorDoc(String organizacion, String passwordEntidad,
                                String nroDocumento, String tipoDocumento) {
        this.organizacion = organizacion;
        this.passwordEntidad = passwordEntidad;
        this.nroDocumento = nroDocumento;
        this.tipoDocumento = tipoDocumento;
    }

    public String getOrganizacion() {
        return organizacion;
    }

    public void setOrganizacion(String organizacion) {
        this.organizacion = organizacion;
    }

    public String getPasswordEntidad() {
        return passwordEntidad;
    }

    public void setPasswordEntidad(String passwordEntidad) {
        this.passwordEntidad = passwordEntidad;
    }

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
}
