package uy.gub.hcen.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Info Request DTO
 *
 * Data Transfer Object for patient request for more information
 * from the professional about an access request.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-03
 */
public class InfoRequestDTO {

    @NotBlank(message = "Question is required")
    @Size(min = 10, max = 500, message = "Question must be between 10 and 500 characters")
    private String question;

    // Constructors

    public InfoRequestDTO() {
    }

    public InfoRequestDTO(String question) {
        this.question = question;
    }

    // Getters and Setters

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
