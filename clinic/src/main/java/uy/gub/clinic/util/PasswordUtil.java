package uy.gub.clinic.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para manejo de contraseñas con BCrypt
 */
public class PasswordUtil {
    
    /**
     * Generar hash de contraseña usando BCrypt
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
    
    /**
     * Verificar contraseña contra hash BCrypt
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verificar si una contraseña es válida (mínimo 6 caracteres)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
