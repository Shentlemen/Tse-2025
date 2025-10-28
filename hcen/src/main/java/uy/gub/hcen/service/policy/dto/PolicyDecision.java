package uy.gub.hcen.service.policy.dto;

/**
 * Policy Decision Enumeration
 *
 * Represents the three possible outcomes of a policy evaluation by the PolicyEngine.
 * These decisions determine whether a healthcare professional can access a clinical document.
 *
 * <p>Decision Semantics:
 * <ul>
 *   <li><b>PERMIT</b>: Access is explicitly allowed by one or more policies</li>
 *   <li><b>DENY</b>: Access is explicitly denied by one or more policies (takes precedence)</li>
 *   <li><b>PENDING</b>: No applicable policy found; requires explicit patient approval</li>
 * </ul>
 *
 * <p>Conflict Resolution Rules:
 * <ol>
 *   <li>DENY always wins (explicit deny overrides everything)</li>
 *   <li>PERMIT wins if no DENY exists</li>
 *   <li>PENDING is the default when no policy matches</li>
 * </ol>
 *
 * <p>Workflow Impact:
 * <ul>
 *   <li><b>PERMIT</b>: Document is returned immediately; access logged in audit system</li>
 *   <li><b>DENY</b>: HTTP 403 Forbidden response; denial logged with reason</li>
 *   <li><b>PENDING</b>: HTTP 202 Accepted; push notification sent to patient for approval</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 * @see PolicyEvaluationResult
 */
public enum PolicyDecision {

    /**
     * Access is permitted
     *
     * <p>Meaning: At least one policy explicitly allows access, and no policy denies it.
     *
     * <p>Action: Grant access to the document, log the access event, and return
     * the document locator to the requesting professional.
     *
     * <p>Example Scenarios:
     * <ul>
     *   <li>Cardiologist accessing ECG results when patient allows CARDIOLOGY specialty</li>
     *   <li>Primary care physician accessing any document from their own clinic</li>
     *   <li>Emergency access when emergency override policy is active</li>
     * </ul>
     */
    PERMIT,

    /**
     * Access is denied
     *
     * <p>Meaning: At least one policy explicitly denies access. DENY always takes
     * precedence over PERMIT in conflict resolution (fail-safe approach).
     *
     * <p>Action: Refuse access, log the denial with reason, return HTTP 403 Forbidden
     * with explanation.
     *
     * <p>Example Scenarios:
     * <ul>
     *   <li>Patient has blacklisted a specific professional</li>
     *   <li>Access attempt outside allowed time window (e.g., after business hours)</li>
     *   <li>Professional's specialty is not in the allowed list for this document type</li>
     *   <li>Access attempt from unauthorized clinic</li>
     * </ul>
     */
    DENY,

    /**
     * Access requires patient approval (pending decision)
     *
     * <p>Meaning: No policy applies to this access request. This could mean:
     * <ul>
     *   <li>Patient has not defined any policies yet</li>
     *   <li>None of the patient's policies match the request attributes</li>
     *   <li>Policies exist but are expired or not yet valid</li>
     * </ul>
     *
     * <p>Action: Request patient approval via push notification. Return HTTP 202 Accepted
     * with message indicating approval is required. Professional may need to wait or
     * provide justification.
     *
     * <p>Example Scenarios:
     * <ul>
     *   <li>First-time access by a new professional</li>
     *   <li>Access to document type not covered by existing policies</li>
     *   <li>Access from professional with specialty not in allow/deny lists</li>
     * </ul>
     *
     * <p>Patient Notification: Mobile app receives push notification with:
     * <ul>
     *   <li>Professional's name and specialty</li>
     *   <li>Document type being requested</li>
     *   <li>Request timestamp and justification (if provided)</li>
     *   <li>Approve/Deny buttons</li>
     * </ul>
     */
    PENDING;

    /**
     * Checks if this decision allows access
     *
     * @return true if decision is PERMIT, false otherwise
     */
    public boolean isPermitted() {
        return this == PERMIT;
    }

    /**
     * Checks if this decision denies access
     *
     * @return true if decision is DENY, false otherwise
     */
    public boolean isDenied() {
        return this == DENY;
    }

    /**
     * Checks if this decision requires patient approval
     *
     * @return true if decision is PENDING, false otherwise
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Gets HTTP status code corresponding to this decision
     *
     * @return HTTP status code
     */
    public int getHttpStatusCode() {
        switch (this) {
            case PERMIT:
                return 200; // OK
            case DENY:
                return 403; // Forbidden
            case PENDING:
                return 202; // Accepted (pending approval)
            default:
                return 500; // Internal Server Error (should never happen)
        }
    }

    /**
     * Gets a human-readable description of this decision
     *
     * @return Description string
     */
    public String getDescription() {
        switch (this) {
            case PERMIT:
                return "Access permitted by policy";
            case DENY:
                return "Access denied by policy";
            case PENDING:
                return "Access requires patient approval";
            default:
                return "Unknown decision";
        }
    }
}
