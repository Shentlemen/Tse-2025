package uy.gub.hcen.service.inus.exception;

/**
 * User Not Found Exception
 * <p>
 * Thrown when an operation attempts to access a user that does not exist
 * in the INUS (Índice Nacional de Usuarios de Salud) system.
 * <p>
 * Common scenarios:
 * - Update operation on non-existent user
 * - Status change for non-existent user
 * - Profile retrieval for invalid CI or INUS ID
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 */
public class UserNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message Detail message explaining which user was not found
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
