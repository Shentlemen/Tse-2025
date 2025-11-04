package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.Size;

/**
 * Approval Decision DTO
 *
 * Data Transfer Object for patient approval of access requests.
 * Contains optional reason for approval.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
public class ApprovalDecisionDTO {

    @Size(max = 500, message = "Approval reason must not exceed 500 characters")
    private String reason;

    // Constructors

    public ApprovalDecisionDTO() {
    }

    public ApprovalDecisionDTO(String reason) {
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
