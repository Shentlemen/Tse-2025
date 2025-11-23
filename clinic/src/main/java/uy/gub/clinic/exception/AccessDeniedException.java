package uy.gub.clinic.exception;

/**
 * Exception thrown when access to a document is denied (HTTP 403)
 */
public class AccessDeniedException extends RuntimeException {

    private final Long documentId;
    private final String patientCi;

    public AccessDeniedException(String message, Long documentId, String patientCi) {
        super(message);
        this.documentId = documentId;
        this.patientCi = patientCi;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getPatientCi() {
        return patientCi;
    }
}
