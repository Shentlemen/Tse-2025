/**
 * Audit Service Package
 *
 * <p>Provides comprehensive audit logging and compliance functionality for HCEN.
 * This package implements AC026 (patients can view who accessed their records)
 * and ensures immutable audit trails for regulatory compliance.
 *
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link uy.gub.hcen.service.audit.AuditService} - Main service for logging and querying audit events</li>
 *   <li>{@link uy.gub.hcen.service.audit.dto.AuditEventRequest} - DTO for programmatic event logging</li>
 *   <li>{@link uy.gub.hcen.service.audit.dto.AuditEventBuilder} - Fluent builder for audit events</li>
 *   <li>{@link uy.gub.hcen.service.audit.annotation.Audited} - Annotation for AOP-based audit logging</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Log Document Access</h3>
 * <pre>
 * {@literal @}Inject
 * private AuditService auditService;
 *
 * public RndcDocument getDocument(Long documentId, String professionalId, String patientCi) {
 *     RndcDocument document = documentRepository.findById(documentId);
 *
 *     // Log the access
 *     auditService.logDocumentAccess(
 *         professionalId,
 *         patientCi,
 *         documentId,
 *         document.getDocumentType(),
 *         ActionOutcome.SUCCESS,
 *         request.getRemoteAddr(),
 *         request.getHeader("User-Agent")
 *     );
 *
 *     return document;
 * }
 * </pre>
 *
 * <h3>Example 2: Log Authentication</h3>
 * <pre>
 * public void handleCallback(String authorizationCode, HttpServletRequest request) {
 *     try {
 *         UserClaims claims = oidcService.exchangeCode(authorizationCode);
 *
 *         // Log successful authentication
 *         auditService.logAuthenticationEvent(
 *             claims.getCi(),
 *             ActionOutcome.SUCCESS,
 *             request.getRemoteAddr(),
 *             request.getHeader("User-Agent"),
 *             Map.of("method", "gub.uy", "provider", "oidc")
 *         );
 *     } catch (AuthenticationException e) {
 *         // Log failed authentication
 *         auditService.logAuthenticationEvent(
 *             "unknown",
 *             ActionOutcome.FAILURE,
 *             request.getRemoteAddr(),
 *             request.getHeader("User-Agent"),
 *             Map.of("error", e.getMessage())
 *         );
 *     }
 * }
 * </pre>
 *
 * <h3>Example 3: Using Builder Pattern</h3>
 * <pre>
 * AuditEventRequest event = AuditEventBuilder.builder()
 *     .access()                              // eventType = ACCESS
 *     .professional("prof-123")              // actorId + actorType
 *     .resourceType("DOCUMENT")
 *     .resourceId("456")
 *     .success()                             // outcome = SUCCESS
 *     .ipAddress("192.168.1.100")
 *     .userAgent("Mozilla/5.0...")
 *     .detail("patientCi", "12345678")
 *     .detail("documentType", "LAB_RESULT")
 *     .build();
 *
 * auditService.logEvent(event);
 * </pre>
 *
 * <h3>Example 4: Query Patient Access History (CU05)</h3>
 * <pre>
 * {@literal @}GET
 * {@literal @}Path("/patients/{ci}/access-history")
 * public Response getPatientAccessHistory(
 *         {@literal @}PathParam("ci") String patientCi,
 *         {@literal @}QueryParam("page") {@literal @}DefaultValue("0") int page,
 *         {@literal @}QueryParam("size") {@literal @}DefaultValue("20") int size) {
 *
 *     List&lt;AuditLog&gt; history = auditService.getPatientAccessHistory(patientCi, page, size);
 *
 *     return Response.ok(history).build();
 * }
 * </pre>
 *
 * <h3>Example 5: Log Policy Change</h3>
 * <pre>
 * public void updatePolicy(String patientCi, Long policyId, PolicyDTO policyData,
 *                          HttpServletRequest request) {
 *     policyRepository.update(policyId, policyData);
 *
 *     // Log the policy change
 *     auditService.logPolicyChange(
 *         patientCi,
 *         policyId,
 *         "UPDATE",
 *         request.getRemoteAddr(),
 *         request.getHeader("User-Agent")
 *     );
 * }
 * </pre>
 *
 * <h3>Example 6: Advanced Search</h3>
 * <pre>
 * // Search all failed access attempts in the last 24 hours
 * LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
 * LocalDateTime now = LocalDateTime.now();
 *
 * List&lt;AuditLog&gt; failedAccess = auditService.searchAuditLogs(
 *     EventType.ACCESS,          // event type
 *     null,                      // any actor
 *     "DOCUMENT",                // only documents
 *     yesterday,                 // from
 *     now,                       // to
 *     ActionOutcome.DENIED,      // only denied attempts
 *     0,                         // page
 *     50                         // size
 * );
 * </pre>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>Immutable Audit Trail</b>: Append-only, no updates or deletes</li>
 *   <li><b>Fail-Safe</b>: Never throws exceptions to business logic</li>
 *   <li><b>Comprehensive Logging</b>: Who, what, when, where, how, and why</li>
 *   <li><b>Privacy by Design</b>: No sensitive data in logs (passwords, full PII)</li>
 *   <li><b>Compliance Ready</b>: AC026, Ley 18.331, AGESIC guidelines</li>
 * </ul>
 *
 * <h2>Event Types</h2>
 * <ul>
 *   <li><b>ACCESS</b>: Resource access by user</li>
 *   <li><b>CREATION</b>: New resource creation</li>
 *   <li><b>MODIFICATION</b>: Resource modification</li>
 *   <li><b>DELETION</b>: Resource deletion</li>
 *   <li><b>POLICY_CHANGE</b>: Access policy change by patient</li>
 *   <li><b>ACCESS_REQUEST</b>: Professional requests access (pending approval)</li>
 *   <li><b>ACCESS_APPROVAL</b>: Patient approves access</li>
 *   <li><b>ACCESS_DENIAL</b>: Patient denies access</li>
 *   <li><b>AUTHENTICATION_SUCCESS</b>: Successful login</li>
 *   <li><b>AUTHENTICATION_FAILURE</b>: Failed login attempt</li>
 * </ul>
 *
 * <h2>Best Practices</h2>
 * <ol>
 *   <li>Always log security-relevant events (access, authentication, authorization)</li>
 *   <li>Include sufficient context (IP address, user agent, relevant IDs)</li>
 *   <li>Use appropriate event types and outcomes</li>
 *   <li>Don't log sensitive data (passwords, full CI, document content)</li>
 *   <li>Use builder pattern for complex events</li>
 *   <li>Handle audit failures gracefully (service never throws)</li>
 *   <li>Query with pagination to avoid large result sets</li>
 * </ol>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-22
 */
package uy.gub.hcen.service.audit;
