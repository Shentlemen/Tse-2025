package fing.hcen.pdi.hcenpdi.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Response object for obtPersonaPorDoc operation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultObtPersonaPorDoc", propOrder = {
    "persona",
    "errores",
    "warnings"
})
public class ResultObtPersonaPorDoc {

    @XmlElement(name = "Persona")
    private ObjPersona persona;

    @XmlElement(name = "Errores")
    private List<Mensaje> errores;

    @XmlElement(name = "Warnings")
    private List<Mensaje> warnings;

    public ResultObtPersonaPorDoc() {
        this.errores = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public ObjPersona getPersona() {
        return persona;
    }

    public void setPersona(ObjPersona persona) {
        this.persona = persona;
    }

    public List<Mensaje> getErrores() {
        return errores;
    }

    public void setErrores(List<Mensaje> errores) {
        this.errores = errores;
    }

    public void addError(Mensaje error) {
        if (this.errores == null) {
            this.errores = new ArrayList<>();
        }
        this.errores.add(error);
    }

    public void addError(String codigo, String descripcion) {
        addError(new Mensaje(codigo, descripcion));
    }

    public List<Mensaje> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Mensaje> warnings) {
        this.warnings = warnings;
    }

    public void addWarning(Mensaje warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addWarning(String codigo, String descripcion) {
        addWarning(new Mensaje(codigo, descripcion));
    }

    public boolean hasErrors() {
        return errores != null && !errores.isEmpty();
    }
}
