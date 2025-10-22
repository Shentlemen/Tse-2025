package uy.gub.hcen.service.rndc.exception;

/**
 * Exception thrown when a requested document is not found in the RNDC.
 * <p>
 * This exception is thrown when:
 * <ul>
 *   <li>Attempting to retrieve a document by ID that does not exist</li>
 *   <li>Attempting to update status of a non-existent document</li>
 *   <li>Attempting to delete or reactivate a document that does not exist</li>
 * </ul>
 * <p>
 * This is a checked exception to force callers to handle missing documents explicitly.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class DocumentNotFoundException extends Exception {

    /**
     * Constructs a new DocumentNotFoundException with the specified detail message.
     *
     * @param message The detail message explaining which document was not found
     */
    public DocumentNotFoundException(String message) {
        super(message);
    }
}
