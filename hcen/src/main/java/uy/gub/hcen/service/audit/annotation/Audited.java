package uy.gub.hcen.service.audit.annotation;

import jakarta.interceptor.InterceptorBinding;
import uy.gub.hcen.audit.entity.AuditLog.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audited Annotation
 *
 * Marks methods for automatic audit logging via AOP (Aspect-Oriented Programming).
 * When a method annotated with @Audited is invoked, the AuditInterceptor
 * automatically logs the event to the audit trail.
 *
 * <p>This annotation enables declarative audit logging:
 * <ul>
 *   <li>Automatic logging without boilerplate code in business logic</li>
 *   <li>Consistent audit trail across all services</li>
 *   <li>Centralized audit logic in interceptor</li>
 *   <li>Separation of concerns (business logic vs. auditing)</li>
 * </ul>
 *
 * Example Usage:
 * <pre>
 * {@literal @}Stateless
 * public class DocumentService {
 *
 *     {@literal @}Audited(eventType = EventType.ACCESS, resourceType = "DOCUMENT",
 *              description = "Professional accesses patient document")
 *     public RndcDocument getDocument(Long documentId) {
 *         // Method implementation
 *         return document;
 *     }
 *
 *     {@literal @}Audited(eventType = EventType.CREATION, resourceType = "DOCUMENT",
 *              description = "New document created in RNDC")
 *     public RndcDocument createDocument(RndcDocument document) {
 *         // Method implementation
 *         return savedDocument;
 *     }
 * }
 * </pre>
 *
 * How it works:
 * <ol>
 *   <li>Method annotated with @Audited is invoked</li>
 *   <li>AuditInterceptor intercepts the call</li>
 *   <li>Interceptor extracts actor info from security context (JWT)</li>
 *   <li>Interceptor executes the method</li>
 *   <li>Interceptor logs success/failure to AuditService</li>
 *   <li>Interceptor returns the method result</li>
 * </ol>
 *
 * Note: The interceptor implementation is optional for now and can be
 * implemented in a future iteration. For now, use programmatic logging
 * with AuditService directly.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 * @see uy.gub.hcen.service.audit.AuditService
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * Type of event to log
     * Required - specifies what kind of action is being audited
     *
     * @return Event type (ACCESS, MODIFICATION, CREATION, etc.)
     */
    EventType eventType();

    /**
     * Type of resource being acted upon
     * Optional - defaults to empty string (can be inferred from method context)
     *
     * @return Resource type (DOCUMENT, USER, POLICY, CLINIC, etc.)
     */
    String resourceType() default "";

    /**
     * Human-readable description of the audited action
     * Optional - used for documentation and debugging
     *
     * @return Description of what this method does
     */
    String description() default "";

    /**
     * Whether to log only on success (true) or always (false)
     * Default: false (log both success and failure)
     *
     * @return true to log only successful operations
     */
    boolean onlyOnSuccess() default false;

    /**
     * Whether to capture method parameters in audit details
     * Default: false (for privacy/security - don't log sensitive params)
     *
     * @return true to include method parameters in audit log
     */
    boolean captureParameters() default false;
}
