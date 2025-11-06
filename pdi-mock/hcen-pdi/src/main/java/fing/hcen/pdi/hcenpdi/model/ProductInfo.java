package fing.hcen.pdi.hcenpdi.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Product description information for productDesc operation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProductInfo", propOrder = {
    "version",
    "modalidad",
    "descripcion"
})
public class ProductInfo {

    @XmlElement(name = "Version", required = true)
    private String version;

    @XmlElement(name = "Modalidad", required = true)
    private String modalidad; // Testing or Production

    @XmlElement(name = "Descripcion", required = true)
    private String descripcion;

    public ProductInfo() {
    }

    public ProductInfo(String version, String modalidad, String descripcion) {
        this.version = version;
        this.modalidad = modalidad;
        this.descripcion = descripcion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
