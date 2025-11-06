package fing.hcen.pdi.hcenpdi.repository;

import fing.hcen.pdi.hcenpdi.model.ObjPersona;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock repository with hardcoded citizen data
 * Simulates PDI database for testing purposes
 */
public class MockPersonRepository {

    private static final Map<String, ObjPersona> mockData = new HashMap<>();

    static {
        // Initialize hardcoded test data
        // Mix of adults and minors for age verification testing

        // Adults (18+)
        addPerson("12345678", "DO", "Juan", "Carlos", "Pérez", "González",
                  "1990-05-15", "M", "UY");

        addPerson("11111111", "DO", "Pedro", "José", "López", "Suárez",
                  "1985-11-30", "M", "UY");

        addPerson("33333333", "DO", "Carlos", "Alberto", "Silva", "Díaz",
                  "1978-01-25", "M", "UY");

        addPerson("44444444", "DO", "Laura", "Beatriz", "González", "Vázquez",
                  "1995-09-12", "F", "UY");

        addPerson("66666666", "DO", "Sofía", "Isabel", "Sánchez", "Morales",
                  "1988-12-03", "F", "UY");

        addPerson("77777777", "DO", "Roberto", "Andrés", "Martínez", "Ferreira",
                  "1992-06-20", "M", "UY");

        // Minors (under 18) - for testing age restrictions
        addPerson("87654321", "DO", "María", "Fernanda", "Rodríguez", "Martínez",
                  "2010-03-20", "F", "UY");

        addPerson("22222222", "DO", "Ana", "Laura", "Fernández", "Castro",
                  "2008-07-10", "F", "UY");

        addPerson("55555555", "DO", "Diego", "Martín", "Hernández", "Ramos",
                  "2005-04-18", "M", "UY");

        // Edge case: just turned 18
        addPerson("99999999", "DO", "Valentina", "María", "Costa", "Olivera",
                  "2007-01-01", "F", "UY");
    }

    private static void addPerson(String nroDocumento, String tipoDocumento,
                                   String nombre1, String nombre2,
                                   String apellido1, String apellido2,
                                   String fechaNacimiento, String sexo, String nacionalidad) {
        ObjPersona persona = new ObjPersona();
        persona.setNroDocumento(nroDocumento);
        persona.setTipoDocumento(tipoDocumento);
        persona.setNombre1(nombre1);
        persona.setNombre2(nombre2);
        persona.setApellido1(apellido1);
        persona.setApellido2(apellido2);
        persona.setFechaNacimiento(fechaNacimiento);
        persona.setSexo(sexo);
        persona.setNacionalidad(nacionalidad);

        mockData.put(nroDocumento, persona);
    }

    /**
     * Find person by document number
     * @param nroDocumento Document number (8 digits, no formatting)
     * @return ObjPersona if found, null otherwise
     */
    public ObjPersona findByDocumento(String nroDocumento) {
        if (nroDocumento == null || nroDocumento.trim().isEmpty()) {
            return null;
        }
        return mockData.get(nroDocumento.trim());
    }

    /**
     * Check if a document number exists in the system
     * @param nroDocumento Document number
     * @return true if exists, false otherwise
     */
    public boolean exists(String nroDocumento) {
        return findByDocumento(nroDocumento) != null;
    }
}
