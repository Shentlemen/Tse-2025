package uy.gub.clinic.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.User;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.entity.Professional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de usuarios del sistema
 */
@ApplicationScoped
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private ClinicService clinicService;
    
    @Inject
    private ProfessionalService professionalService;
    
    /**
     * Buscar usuario por nombre de usuario
     */
    public Optional<User> findByUsername(String username) {
        try {
            // Usar JOIN FETCH para cargar las relaciones lazy de una vez
            TypedQuery<User> query = entityManager.createQuery(
                "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.clinic LEFT JOIN FETCH u.professional WHERE u.username = :username", 
                User.class);
            query.setParameter("username", username);
            User user = query.getSingleResult();
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.debug("Usuario no encontrado: {}", username);
            return Optional.empty();
        }
    }
    
    /**
     * Buscar usuario por ID
     */
    public Optional<User> findById(Long id) {
        try {
            User user = entityManager.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error al buscar usuario por ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * Buscar usuarios por clínica
     */
    public List<User> findByClinic(String clinicId) {
        try {
            if (clinicId == null || clinicId.trim().isEmpty()) {
                logger.warn("Intento de buscar usuarios con clinicId nulo o vacío");
                return List.of();
            }
            
            String trimmedClinicId = clinicId.trim();
            logger.debug("Buscando usuarios para clínica: '{}'", trimmedClinicId);
            
            // Primero verificar que la clínica existe
            TypedQuery<Long> clinicCheck = entityManager.createQuery(
                "SELECT COUNT(c) FROM Clinic c WHERE c.id = :clinicId",
                Long.class);
            clinicCheck.setParameter("clinicId", trimmedClinicId);
            Long clinicCount = clinicCheck.getSingleResult();
            logger.debug("Clínicas encontradas con ID '{}': {}", trimmedClinicId, clinicCount);
            
            // Verificar usuarios directamente por clinic_id (sin JOIN)
            TypedQuery<Object[]> directQuery = entityManager.createQuery(
                "SELECT u.id, u.username, u.clinic.id FROM User u WHERE u.clinic.id = :clinicId",
                Object[].class);
            directQuery.setParameter("clinicId", trimmedClinicId);
            List<Object[]> directResults = directQuery.getResultList();
            logger.debug("Usuarios encontrados directamente (sin JOIN): {}", directResults.size());
            for (Object[] row : directResults) {
                logger.debug("  - User ID: {}, Username: {}, Clinic ID: {}", row[0], row[1], row[2]);
            }
            
            // Usar JOIN FETCH para cargar las relaciones lazy de una vez
            // NO filtrar por active = true para mostrar todos los usuarios (activos e inactivos)
            TypedQuery<User> query = entityManager.createQuery(
                "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.clinic LEFT JOIN FETCH u.professional WHERE u.clinic.id = :clinicId ORDER BY u.username", 
                User.class);
            query.setParameter("clinicId", trimmedClinicId);
            List<User> users = query.getResultList();
            
            logger.info("Usuarios encontrados para clínica '{}': {}", trimmedClinicId, users.size());
            for (User user : users) {
                logger.debug("  - Usuario: {} (ID: {}, Role: {}, Clinic: {})", 
                    user.getUsername(), user.getId(), user.getRole(),
                    user.getClinic() != null ? user.getClinic().getId() : "NULL");
            }
            
            return users;
        } catch (Exception e) {
            logger.error("Error al buscar usuarios por clínica: {}", clinicId, e);
            return List.of();
        }
    }
    
    /**
     * Obtener todos los usuarios activos
     */
    public List<User> findAllActive() {
        try {
            // Usar JOIN FETCH para cargar las relaciones lazy de una vez
            // NO filtrar por active = true para mostrar todos los usuarios (activos e inactivos)
            TypedQuery<User> query = entityManager.createQuery(
                "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.clinic LEFT JOIN FETCH u.professional ORDER BY u.username", 
                User.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener todos los usuarios", e);
            return List.of();
        }
    }
    
    /**
     * Obtener todos los usuarios
     */
    public List<User> findAll() {
        try {
            TypedQuery<User> query = entityManager.createNamedQuery("User.findAll", User.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error al obtener todos los usuarios", e);
            return List.of();
        }
    }
    
    /**
     * Crear nuevo usuario
     */
    @Transactional
    public User createUser(User user) {
        try {
            // Validar que el username no exista
            if (findByUsername(user.getUsername()).isPresent()) {
                throw new IllegalArgumentException("El nombre de usuario ya existe: " + user.getUsername());
            }
            
            // Validar clínica si es necesario
            if (user.getClinic() != null && user.getClinic().getId() != null) {
                Optional<Clinic> clinic = clinicService.getClinicById(user.getClinic().getId());
                if (clinic.isEmpty()) {
                    throw new IllegalArgumentException("Clínica no encontrada: " + user.getClinic().getId());
                }
                user.setClinic(clinic.get());
            }
            
            // Validar profesional si es necesario
            if (user.getProfessional() != null && user.getProfessional().getId() != null) {
                Optional<Professional> professional = professionalService.getProfessionalById(user.getProfessional().getId());
                if (professional.isEmpty()) {
                    throw new IllegalArgumentException("Profesional no encontrado: " + user.getProfessional().getId());
                }
                user.setProfessional(professional.get());
            }
            
            entityManager.persist(user);
            entityManager.flush();
            
            logger.info("Usuario creado exitosamente: {}", user.getUsername());
            return user;
            
        } catch (Exception e) {
            logger.error("Error al crear usuario: {}", user.getUsername(), e);
            throw e;
        }
    }
    
    /**
     * Actualizar usuario
     */
    @Transactional
    public User updateUser(User user) {
        try {
            User existingUser = entityManager.find(User.class, user.getId());
            if (existingUser == null) {
                throw new IllegalArgumentException("Usuario no encontrado: " + user.getId());
            }
            
            // Actualizar campos permitidos
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                existingUser.setUsername(user.getUsername().trim());
            }
            if (user.getRole() != null && !user.getRole().trim().isEmpty()) {
                existingUser.setRole(user.getRole().trim());
            }
            existingUser.setEmail(user.getEmail());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setActive(user.getActive());
            
            // Actualizar contraseña si se proporcionó (y no está vacía)
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                existingUser.setPassword(user.getPassword());
            }
            
            // Actualizar clínica si es necesario
            if (user.getClinic() != null && user.getClinic().getId() != null) {
                Optional<Clinic> clinic = clinicService.getClinicById(user.getClinic().getId());
                if (clinic.isEmpty()) {
                    throw new IllegalArgumentException("Clínica no encontrada: " + user.getClinic().getId());
                }
                existingUser.setClinic(clinic.get());
            }
            
            // Actualizar profesional si es necesario
            if (user.getProfessional() != null && user.getProfessional().getId() != null) {
                Optional<Professional> professional = professionalService.getProfessionalById(user.getProfessional().getId());
                if (professional.isEmpty()) {
                    throw new IllegalArgumentException("Profesional no encontrado: " + user.getProfessional().getId());
                }
                existingUser.setProfessional(professional.get());
            }
            
            entityManager.merge(existingUser);
            entityManager.flush();
            
            logger.info("Usuario actualizado exitosamente: {}", existingUser.getUsername());
            return existingUser;
            
        } catch (Exception e) {
            logger.error("Error al actualizar usuario: {}", user.getId(), e);
            throw e;
        }
    }
    
    /**
     * Cambiar contraseña de usuario
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        try {
            User user = entityManager.find(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("Usuario no encontrado: " + userId);
            }
            
            user.setPassword(newPassword);
            entityManager.merge(user);
            entityManager.flush();
            
            logger.info("Contraseña actualizada para usuario: {}", user.getUsername());
            
        } catch (Exception e) {
            logger.error("Error al cambiar contraseña para usuario: {}", userId, e);
            throw e;
        }
    }
    
    /**
     * Cambiar username de usuario
     */
    @Transactional
    public void changeUsername(Long userId, String newUsername) {
        try {
            User user = entityManager.find(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("Usuario no encontrado: " + userId);
            }
            
            // Validar que el nuevo username no esté vacío
            if (newUsername == null || newUsername.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
            }
            
            newUsername = newUsername.trim();
            
            // Verificar que el nuevo username no esté en uso por otro usuario
            Optional<User> existingUser = findByUsername(newUsername);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso: " + newUsername);
            }
            
            // Si el username no cambió, no hacer nada
            if (newUsername.equals(user.getUsername())) {
                return;
            }
            
            String oldUsername = user.getUsername();
            user.setUsername(newUsername);
            entityManager.merge(user);
            entityManager.flush();
            
            logger.info("Username actualizado para usuario ID {}: {} -> {}", userId, oldUsername, newUsername);
            
        } catch (Exception e) {
            logger.error("Error al cambiar username para usuario: {}", userId, e);
            throw e;
        }
    }
    
    /**
     * Actualizar último login
     */
    @Transactional
    public void updateLastLogin(String username) {
        try {
            Optional<User> userOpt = findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLogin(LocalDateTime.now());
                entityManager.merge(user);
                entityManager.flush();
                
                logger.debug("Último login actualizado para usuario: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error al actualizar último login para usuario: {}", username, e);
        }
    }
    
    /**
     * Desactivar usuario
     */
    @Transactional
    public void deactivateUser(Long userId) {
        try {
            User user = entityManager.find(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("Usuario no encontrado: " + userId);
            }
            
            user.setActive(false);
            entityManager.merge(user);
            entityManager.flush();
            
            logger.info("Usuario desactivado: {}", user.getUsername());
            
        } catch (Exception e) {
            logger.error("Error al desactivar usuario: {}", userId, e);
            throw e;
        }
    }
    
    /**
     * Verificar si existe algún usuario en el sistema
     */
    public boolean hasUsers() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u", Long.class);
            Long count = query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de usuarios", e);
            return false;
        }
    }
    
    /**
     * Verificar si existe algún usuario super administrador
     */
    public boolean hasSuperAdmin() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.role = 'SUPER_ADMIN' AND u.active = true", Long.class);
            Long count = query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de super administrador", e);
            return false;
        }
    }
    
    /**
     * Crear usuario super administrador inicial
     */
    @Transactional
    public User createSuperAdmin(String username, String password, String email, String firstName, String lastName) {
        try {
            // Verificar que no exista ya un super admin
            if (hasSuperAdmin()) {
                throw new IllegalArgumentException("Ya existe un super administrador en el sistema");
            }
            
            User superAdmin = new User();
            superAdmin.setUsername(username);
            superAdmin.setPassword(password);
            superAdmin.setEmail(email);
            superAdmin.setFirstName(firstName);
            superAdmin.setLastName(lastName);
            superAdmin.setRole("SUPER_ADMIN");
            superAdmin.setActive(true);
            superAdmin.setClinic(null); // Super admin no pertenece a ninguna clínica
            
            return createUser(superAdmin);
            
        } catch (Exception e) {
            logger.error("Error al crear super administrador", e);
            throw e;
        }
    }
}
