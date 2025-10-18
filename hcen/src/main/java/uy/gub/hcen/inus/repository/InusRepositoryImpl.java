package uy.gub.hcen.inus.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import uy.gub.hcen.inus.entity.InusUser;
import uy.gub.hcen.inus.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * INUS Repository Implementation
 *
 * JPA-based implementation of the INUS repository interface.
 * Uses EntityManager for database operations with proper transaction management.
 *
 * Transaction Management:
 * - @Stateless EJB provides automatic transaction management
 * - Each method runs in its own transaction (default CMT behavior)
 * - Rollback occurs automatically on unchecked exceptions
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-17
 */
@Stateless
public class InusRepositoryImpl implements InusRepository {

    private static final Logger LOGGER = Logger.getLogger(InusRepositoryImpl.class.getName());

    @PersistenceContext(unitName = "hcen-pu")
    private EntityManager entityManager;

    // ================================================================
    // Lookup Methods
    // ================================================================

    @Override
    public Optional<InusUser> findByCi(String ci) {
        LOGGER.log(Level.FINE, "Finding user by CI: {0}", ci);

        if (ci == null || ci.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to find user with null or empty CI");
            return Optional.empty();
        }

        try {
            TypedQuery<InusUser> query = entityManager.createQuery(
                "SELECT u FROM InusUser u WHERE u.ci = :ci", InusUser.class);
            query.setParameter("ci", ci);

            InusUser user = query.getSingleResult();
            LOGGER.log(Level.FINE, "Found user with CI: {0}", ci);
            return Optional.of(user);
        } catch (NoResultException e) {
            LOGGER.log(Level.FINE, "No user found with CI: {0}", ci);
            return Optional.empty();
        }
    }

    @Override
    public Optional<InusUser> findByInusId(String inusId) {
        LOGGER.log(Level.FINE, "Finding user by INUS ID: {0}", inusId);

        if (inusId == null || inusId.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to find user with null or empty INUS ID");
            return Optional.empty();
        }

        try {
            TypedQuery<InusUser> query = entityManager.createQuery(
                "SELECT u FROM InusUser u WHERE u.inusId = :inusId", InusUser.class);
            query.setParameter("inusId", inusId);

            InusUser user = query.getSingleResult();
            LOGGER.log(Level.FINE, "Found user with INUS ID: {0}", inusId);
            return Optional.of(user);
        } catch (NoResultException e) {
            LOGGER.log(Level.FINE, "No user found with INUS ID: {0}", inusId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<InusUser> findByEmail(String email) {
        LOGGER.log(Level.FINE, "Finding user by email: {0}", email);

        if (email == null || email.trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Attempted to find user with null or empty email");
            return Optional.empty();
        }

        try {
            TypedQuery<InusUser> query = entityManager.createQuery(
                "SELECT u FROM InusUser u WHERE u.email = :email", InusUser.class);
            query.setParameter("email", email);

            InusUser user = query.getSingleResult();
            LOGGER.log(Level.FINE, "Found user with email: {0}", email);
            return Optional.of(user);
        } catch (NoResultException e) {
            LOGGER.log(Level.FINE, "No user found with email: {0}", email);
            return Optional.empty();
        }
    }

    // ================================================================
    // Existence Check Methods
    // ================================================================

    @Override
    public boolean existsByCi(String ci) {
        LOGGER.log(Level.FINE, "Checking existence of user with CI: {0}", ci);

        if (ci == null || ci.trim().isEmpty()) {
            return false;
        }

        Long count = entityManager.createQuery(
            "SELECT COUNT(u) FROM InusUser u WHERE u.ci = :ci", Long.class)
            .setParameter("ci", ci)
            .getSingleResult();

        boolean exists = count > 0;
        LOGGER.log(Level.FINE, "User with CI {0} exists: {1}", new Object[]{ci, exists});
        return exists;
    }

    @Override
    public boolean existsByInusId(String inusId) {
        LOGGER.log(Level.FINE, "Checking existence of user with INUS ID: {0}", inusId);

        if (inusId == null || inusId.trim().isEmpty()) {
            return false;
        }

        Long count = entityManager.createQuery(
            "SELECT COUNT(u) FROM InusUser u WHERE u.inusId = :inusId", Long.class)
            .setParameter("inusId", inusId)
            .getSingleResult();

        boolean exists = count > 0;
        LOGGER.log(Level.FINE, "User with INUS ID {0} exists: {1}", new Object[]{inusId, exists});
        return exists;
    }

    // ================================================================
    // Persistence Methods
    // ================================================================

    @Override
    public InusUser save(InusUser user) {
        LOGGER.log(Level.INFO, "Saving new user with CI: {0}", user.getCi());

        if (user == null) {
            throw new IllegalArgumentException("Cannot save null user");
        }

        if (existsByCi(user.getCi())) {
            LOGGER.log(Level.WARNING, "Attempted to save user with existing CI: {0}", user.getCi());
            throw new IllegalArgumentException("User with CI " + user.getCi() + " already exists");
        }

        if (user.getInusId() != null && existsByInusId(user.getInusId())) {
            LOGGER.log(Level.WARNING, "Attempted to save user with existing INUS ID: {0}", user.getInusId());
            throw new IllegalArgumentException("User with INUS ID " + user.getInusId() + " already exists");
        }

        entityManager.persist(user);
        entityManager.flush();

        LOGGER.log(Level.INFO, "Successfully saved user with CI: {0}, INUS ID: {1}",
            new Object[]{user.getCi(), user.getInusId()});

        return user;
    }

    @Override
    public InusUser update(InusUser user) {
        LOGGER.log(Level.INFO, "Updating user with CI: {0}", user.getCi());

        if (user == null) {
            throw new IllegalArgumentException("Cannot update null user");
        }

        if (!existsByCi(user.getCi())) {
            LOGGER.log(Level.WARNING, "Attempted to update non-existent user with CI: {0}", user.getCi());
            throw new IllegalArgumentException("User with CI " + user.getCi() + " does not exist");
        }

        InusUser updatedUser = entityManager.merge(user);
        entityManager.flush();

        LOGGER.log(Level.INFO, "Successfully updated user with CI: {0}", user.getCi());

        return updatedUser;
    }

    // ================================================================
    // Query Methods
    // ================================================================

    @Override
    public List<InusUser> findAll(int page, int size) {
        LOGGER.log(Level.FINE, "Finding all users - page: {0}, size: {1}",
            new Object[]{page, size});

        TypedQuery<InusUser> query = entityManager.createQuery(
            "SELECT u FROM InusUser u ORDER BY u.createdAt DESC", InusUser.class);

        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<InusUser> users = query.getResultList();
        LOGGER.log(Level.FINE, "Found {0} users", users.size());

        return users;
    }

    @Override
    public List<InusUser> searchUsers(String query, int page, int size) {
        LOGGER.log(Level.FINE, "Searching users with query: {0}, page: {1}, size: {2}",
            new Object[]{query, page, size});

        if (query == null || query.trim().isEmpty()) {
            return findAll(page, size);
        }

        String searchPattern = "%" + query.toLowerCase() + "%";

        TypedQuery<InusUser> typedQuery = entityManager.createQuery(
            "SELECT u FROM InusUser u WHERE " +
            "LOWER(u.ci) LIKE :query OR " +
            "LOWER(u.inusId) LIKE :query OR " +
            "LOWER(u.firstName) LIKE :query OR " +
            "LOWER(u.lastName) LIKE :query OR " +
            "LOWER(u.email) LIKE :query " +
            "ORDER BY u.createdAt DESC", InusUser.class);

        typedQuery.setParameter("query", searchPattern);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<InusUser> users = typedQuery.getResultList();
        LOGGER.log(Level.FINE, "Found {0} users matching query: {1}",
            new Object[]{users.size(), query});

        return users;
    }

    @Override
    public List<InusUser> findByStatus(UserStatus status, int page, int size) {
        LOGGER.log(Level.FINE, "Finding users by status: {0}, page: {1}, size: {2}",
            new Object[]{status, page, size});

        TypedQuery<InusUser> query = entityManager.createQuery(
            "SELECT u FROM InusUser u WHERE u.status = :status ORDER BY u.createdAt DESC",
            InusUser.class);

        query.setParameter("status", status);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<InusUser> users = query.getResultList();
        LOGGER.log(Level.FINE, "Found {0} users with status: {1}",
            new Object[]{users.size(), status});

        return users;
    }

    @Override
    public List<InusUser> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate,
                                                  int page, int size) {
        LOGGER.log(Level.FINE, "Finding users created between {0} and {1}",
            new Object[]{startDate, endDate});

        TypedQuery<InusUser> query = entityManager.createQuery(
            "SELECT u FROM InusUser u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY u.createdAt DESC", InusUser.class);

        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        List<InusUser> users = query.getResultList();
        LOGGER.log(Level.FINE, "Found {0} users created in date range", users.size());

        return users;
    }

    // ================================================================
    // Count Methods
    // ================================================================

    @Override
    public long count() {
        LOGGER.log(Level.FINE, "Counting total users");

        Long count = entityManager.createQuery(
            "SELECT COUNT(u) FROM InusUser u", Long.class)
            .getSingleResult();

        LOGGER.log(Level.FINE, "Total user count: {0}", count);
        return count;
    }

    @Override
    public long countByStatus(UserStatus status) {
        LOGGER.log(Level.FINE, "Counting users by status: {0}", status);

        Long count = entityManager.createQuery(
            "SELECT COUNT(u) FROM InusUser u WHERE u.status = :status", Long.class)
            .setParameter("status", status)
            .getSingleResult();

        LOGGER.log(Level.FINE, "User count for status {0}: {1}",
            new Object[]{status, count});
        return count;
    }

    // ================================================================
    // Delete/Deactivate Methods
    // ================================================================

    @Override
    public boolean deactivateByCi(String ci) {
        LOGGER.log(Level.INFO, "Deactivating user with CI: {0}", ci);

        Optional<InusUser> userOpt = findByCi(ci);
        if (userOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Cannot deactivate - user not found with CI: {0}", ci);
            return false;
        }

        InusUser user = userOpt.get();
        user.setStatus(UserStatus.INACTIVE);
        update(user);

        LOGGER.log(Level.INFO, "Successfully deactivated user with CI: {0}", ci);
        return true;
    }
}
