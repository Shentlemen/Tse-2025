package uy.gub.hcen.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegistrationPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("ci")
    private String ci;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String clinicId;

    public UserRegistrationPayload() {}

    // existing getters/setters unchanged...
    public String getCi() { return ci; }
    public void setCi(String ci) { this.ci = normalizeCi(ci); }

    // keep your alias for "id" if you still get that:
    @JsonProperty("id")
    public void setIdentifierId(String identifier) {
        if (identifier != null && !identifier.isBlank()) {
            this.ci = normalizeCi(identifier);
        }
    }

    // FHIR: "identifier": [ { "system": "...", "value": "34567890" }, ... ]
    @JsonProperty("identifier")
    public void setIdentifierArray(JsonNode identifierNode) {
        if (identifierNode == null || identifierNode.isNull()) return;
        if (identifierNode.isArray()) {
            for (JsonNode idNode : identifierNode) {
                JsonNode valueNode = idNode.get("value");
                if (valueNode != null && !valueNode.isNull() && !valueNode.asText().isBlank()) {
                    this.ci = normalizeCi(valueNode.asText());
                    return; // take first identifier value by default
                }
            }
        } else {
            // sometimes it's sent as object
            JsonNode valueNode = identifierNode.get("value");
            if (valueNode != null) this.ci = normalizeCi(valueNode.asText());
        }
    }

    // FHIR: "name": [ { "given": ["Carlos"], "family": "López" , "text": "Carlos López" } ]
    @JsonProperty("name")
    public void setNameArray(JsonNode nameNode) {
        if (nameNode == null || nameNode.isNull()) return;
        JsonNode first = nameNode.isArray() ? nameNode.get(0) : nameNode;
        if (first == null || first.isNull()) return;

        JsonNode given = first.get("given");
        if (given != null && given.isArray() && given.size() > 0) {
            this.firstName = given.get(0).asText();
        } else if (first.has("text")) {
            // fallback to text and try to split
            String text = first.get("text").asText();
            if (text != null && !text.isBlank()) {
                String[] parts = text.split("\\s+", 2);
                this.firstName = parts[0];
                if (parts.length > 1 && (this.lastName == null || this.lastName.isBlank())) {
                    this.lastName = parts[1];
                }
            }
        }

        JsonNode family = first.get("family");
        if (family != null) this.lastName = family.asText();
    }

    // FHIR: "managingOrganization": { "reference": "Organization/clinic-1", "display": "Clinic 1" }
    @JsonProperty("managingOrganization")
    public void setManagingOrganization(JsonNode orgNode) {
        if (orgNode == null || orgNode.isNull()) return;
        JsonNode ref = orgNode.get("reference");
        if (ref != null) {
            // keep the reference (Organization/clinic-1) or extract suffix
            String reference = ref.asText();
            if (reference != null && !reference.isBlank()) {
                // optional: extract id after slash
                int slash = reference.indexOf('/');
                this.clinicId = (slash >= 0 && slash < reference.length()-1)
                        ? reference.substring(slash + 1)
                        : reference;
            }
        } else if (orgNode.has("display")) {
            this.clinicId = orgNode.get("display").asText();
        }
    }

    // Example: FHIR may include birthDate as "1978-11-30"
    @JsonProperty("birthDate")
    public void setBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) return;
        try {
            this.dateOfBirth = LocalDate.parse(birthDate); // ISO yyyy-MM-dd
        } catch (DateTimeParseException e) {
            // ignore or log and try other formats if needed
        }
    }

    /**
     * Accepts FHIR-style telecom array and extracts email and phone.
     * Example element: { "system": "email", "use": "home", "value": "foo@bar" }
     */
    @JsonProperty("telecom")
    public void setTelecom(JsonNode telecomNode) {
        if (telecomNode == null || telecomNode.isNull()) return;

        // support either array or single object
        if (telecomNode.isArray()) {
            for (JsonNode t : telecomNode) {
                extractTelecomEntry(t);
            }
        } else {
            extractTelecomEntry(telecomNode);
        }
    }

    private void extractTelecomEntry(JsonNode entry) {
        if (entry == null || entry.isNull()) return;
        JsonNode system = entry.get("system");
        JsonNode value  = entry.get("value");

        if (value == null || value.isNull()) return;
        String val = value.asText().trim();
        if (val.isEmpty()) return;

        String sys = system != null && !system.isNull() ? system.asText().toLowerCase() : "";

        switch (sys) {
            case "email":
                // optionally validate simple email format before setting
                this.email = val;
                break;
            case "phone":
            case "mobile":
            case "fax":
                // normalize phone: remove non-digits but preserve leading + if present
                this.phoneNumber = normalizePhone(val);
                break;
            default:
                // if system is missing, try to guess by value format
                if (val.contains("@")) this.email = val;
                else this.phoneNumber = normalizePhone(val);
                break;
        }
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        // keep '+' if present, then digits
        if (raw.startsWith("+")) {
            return "+" + raw.substring(1).replaceAll("\\D", "");
        }
        // local format: remove any non-digits
        String digits = raw.replaceAll("\\D", "");
        // optional: if it looks like a local Uruguayan mobile without +598, normalize to +598...
        if (digits.length() == 8 && digits.startsWith("09")) {
            // example: 099333333 -> +59899333333
            return "+598" + digits.substring(1);
        }
        return digits;
    }

    // other existing getters/setters...
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getClinicId() { return clinicId; }
    public void setClinicId(String clinicId) { this.clinicId = clinicId; }

    // normalization helper
    private String normalizeCi(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("[^0-9]", ""); // remove dots/dashes, leaves digits only
    }

    @Override
    public boolean equals(Object o) { /* keep your implementation */
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationPayload that = (UserRegistrationPayload) o;
        return Objects.equals(ci, that.ci);
    }

    @Override
    public int hashCode() { return Objects.hash(ci); }

    @Override
    public String toString() {
        return "UserRegistrationPayload{" +
                "ci='" + ci + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", clinicId='" + clinicId + '\'' +
                '}';
    }
}
