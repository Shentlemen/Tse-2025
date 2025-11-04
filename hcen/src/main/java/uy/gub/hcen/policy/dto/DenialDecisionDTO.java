package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Denial Decision DTO
 *
 * Data Transfer Object for patient denial of access requests.
 * Requires a reason for denial.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
public class DenialDecisionDTO {

    @NotBlank(message = "Denial reason is required")
    @Size(min = 10, max = 500, message = "Denial reason must be between 10 and 500 characters")
    private String reason;

    // Constructors

    public DenialDecisionDTO() {
    }

    public DenialDecisionDTO(String reason) {
        this.reason = reason;
    }

    // Getters and Setters

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
