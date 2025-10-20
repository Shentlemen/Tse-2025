package uy.gub.clinic.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa las especialidades médicas
 */
@Entity
@Table(name = "specialties")
@NamedQueries({
    @NamedQuery(name = "Specialty.findAll", query = "SELECT s FROM Specialty s ORDER BY s.name"),
    @NamedQuery(name = "Specialty.findActive", query = "SELECT s FROM Specialty s WHERE s.active = true ORDER BY s.name")
})
public class Specialty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Size(max = 255)
    @Column(name = "description")
    private String description;
    
    @Size(max = 10)
    @Column(name = "code")
    private String code;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    // Relaciones
    @OneToMany(mappedBy = "specialty", fetch = FetchType.LAZY)
    private List<Professional> professionals = new ArrayList<>();
    
    // Constructores
    public Specialty() {}
    
    public Specialty(String name, String code) {
        this.name = name;
        this.code = code;
    }
    
    public Specialty(String name, String code, String description) {
        this(name, code);
        this.description = description;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public List<Professional> getProfessionals() {
        return professionals;
    }
    
    public void setProfessionals(List<Professional> professionals) {
        this.professionals = professionals;
    }
    
    @Override
    public String toString() {
        return "Specialty{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
