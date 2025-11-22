package uy.gub.hcen.clinicalhistory.api.rest;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.audit.entity.AuditLog;
import uy.gub.hcen.clinicalhistory.dto.*;
import uy.gub.hcen.clinicalhistory.service.ClinicalHistoryService;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.rndc.repository.RndcRepository;
import uy.gub.hcen.service.audit.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clinical History REST Resource
 *
 * JAX-RS resource providing REST API endpoints for patient clinical history visualization.
 * Allows patients to view their clinical documents, statistics, and document details.
 *
 * <p>Base Path: /api/clinical-history
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/clinical-history - Get patient's clinical history (document list)</li>
 *   <li>GET /api/clinical-history/documents/{documentId} - Get document details</li>
 *   <li>GET /api/clinical-history/documents/{documentId}/content - Get document content URL</li>
 *   <li>GET /api/clinical-history/stats - Get document statistics</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *   <li>JWT authentication required (to be implemented via SecurityContext)</li>
 *   <li>Patients can only access their own documents</li>
 *   <li>All access attempts are logged in audit system</li>
 * </ul>
 *
 * <p>Error Handling:
 * <ul>
 *   <li>400 Bad Request - Invalid parameters or validation errors</li>
 *   <li>404 Not Found - Document not found or unauthorized</li>
 *   <li>500 Internal Server Error - System errors</li>
 * </ul>
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
@Path("/clinical-history")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClinicalHistoryResource {

    private static final Logger LOGGER = Logger.getLogger(ClinicalHistoryResource.class.getName());
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Inject
    private ClinicalHistoryService clinicalHistoryService;

    @Inject
    private RndcRepository rndcRepository;

    @Inject
    private AuditService auditService;

    // ================================================================
    // GET /api/clinical-history - Get Clinical History
    // ================================================================

    /**
     * Retrieves patient's clinical history with optional filters and pagination.
     *
     * <p>Supports filtering by:
     * <ul>
     *   <li>Document type (documentType query param)</li>
     *   <li>Date range (fromDate, toDate query params)</li>
     *   <li>Clinic (clinicId query param)</li>
     * </ul>
     *
     * <p>Flow Variants:
     * <ul>
     *   <li><b>Flow 1 - Patient accessing own history:</b> No X-Clinic-Id header. Returns ALL documents.</li>
     *   <li><b>Flow 2 - Clinic accessing patient metadata:</b> X-Clinic-Id header present. Filters out documents
     *       from the requesting clinic (clinic already has these locally).</li>
     * </ul>
     *
     * <p>Example:
     * GET /api/clinical-history?patientCi=12345678&documentType=LAB_RESULT&page=0&size=20
     *
     * @param patientCi Patient's CI (required)
     * @param documentType Optional document type filter
     * @param fromDate Optional start date (ISO format: 2025-01-01T00:00:00)
     * @param toDate Optional end date (ISO format: 2025-12-31T23:59:59)
     * @param clinicId Optional clinic filter
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @param securityContext Security context (injected by JAX-RS, contains clinic ID if present)
     * @return 200 OK with PaginatedDocumentListResponse
     *         400 Bad Request if parameters are invalid
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClinicalHistory(
            @QueryParam("patientCi") String patientCi,
            @QueryParam("documentType") String documentType,
            @QueryParam("fromDate") String fromDate,
            @QueryParam("toDate") String toDate,
            @QueryParam("clinicId") String clinicId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @Context jakarta.ws.rs.core.SecurityContext securityContext) {

        LOGGER.log(Level.INFO, "GET /api/clinical-history - patientCi: {0}, type: {1}, page: {2}, size: {3}",
                new Object[]{patientCi, documentType, page, size});

        try {
            // Validate patientCi
            if (patientCi == null || patientCi.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Missing patientCi parameter");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
                        .build();
            }

            // Extract requesting clinic ID from security context (if present)
            // Flow 1: Patient accessing their own history -> no clinic ID in context
            // Flow 2: Clinic accessing patient metadata -> clinic ID present in context
            String requestingClinicId = null;
            if (securityContext != null && securityContext.getUserPrincipal() != null) {
                // Check if this is a clinic request (clinic API key authentication)
                if (securityContext.isUserInRole("CLINIC")) {
                    requestingClinicId = securityContext.getUserPrincipal().getName();
                    LOGGER.log(Level.INFO, "Clinic request detected - requestingClinicId: {0}", requestingClinicId);
                }
            }

            // TODO: Extract patientCi from JWT SecurityContext instead of query param
            // For now, accept it as query param for development
            // In production:
            // @Context SecurityContext securityContext;
            // String patientCi = securityContext.getUserPrincipal().getName();

            // Validate pagination
            if (page < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Page number must be non-negative"))
                        .build();
            }

            if (size <= 0 || size > MAX_SIZE) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Page size must be between 1 and " + MAX_SIZE))
                        .build();
            }

            // Parse document type
            DocumentType docType = null;
            if (documentType != null && !documentType.trim().isEmpty()) {
                try {
                    docType = DocumentType.valueOf(documentType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Invalid document type: {0}", documentType);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid document type: " + documentType))
                            .build();
                }
            }

            // Parse dates
            LocalDateTime from = null;
            LocalDateTime to = null;

            if (fromDate != null && !fromDate.trim().isEmpty()) {
                try {
                    from = LocalDateTime.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    LOGGER.log(Level.WARNING, "Invalid fromDate format: {0}", fromDate);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid fromDate format. Use ISO format: 2025-01-01T00:00:00"))
                            .build();
                }
            }

            if (toDate != null && !toDate.trim().isEmpty()) {
                try {
                    to = LocalDateTime.parse(toDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    LOGGER.log(Level.WARNING, "Invalid toDate format: {0}", toDate);
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.validationError("Invalid toDate format. Use ISO format: 2025-12-31T23:59:59"))
                            .build();
                }
            }

            // Validate date range
            if (from != null && to != null && from.isAfter(to)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("fromDate must be before toDate"))
                        .build();
            }

            // Call service with requesting clinic ID
            PaginatedDocumentListResponse response = clinicalHistoryService.getClinicalHistory(
                    patientCi,
                    docType,
                    from,
                    to,
                    clinicId,
                    page,
                    size,
                    requestingClinicId
            );

            LOGGER.log(Level.INFO, "Returning {0} documents for patient: {1} (page {2})",
                    new Object[]{response.getDocuments().size(), patientCi, page});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving clinical history for patient: " + patientCi, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve clinical history: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/clinical-history/documents/{documentId} - Get Document Details
    // ================================================================

    /**
     * Retrieves detailed information about a specific document.
     *
     * <p>Example:
     * GET /api/clinical-history/documents/123?patientCi=12345678
     *
     * @param documentId Document ID
     * @param patientCi Patient's CI (for authorization)
     * @return 200 OK with DocumentDetailDTO
     *         404 Not Found if document doesn't exist or patient is unauthorized
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/documents/{documentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentDetail(
            @PathParam("documentId") Long documentId,
            @QueryParam("patientCi") String patientCi) {

        LOGGER.log(Level.INFO, "GET /api/clinical-history/documents/{0} - patientCi: {1}",
                new Object[]{documentId, patientCi});

        try {
            // Validate patientCi
            if (patientCi == null || patientCi.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Missing patientCi parameter");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
                        .build();
            }

            if(!patientCi.startsWith("uy-ci-")){
                patientCi = "uy-ci-" + patientCi;
            }

            // TODO: Extract patientCi from JWT SecurityContext

            // Call service
            DocumentDetailDTO detail = clinicalHistoryService.getDocumentDetail(documentId, patientCi);

            if (detail == null) {
                LOGGER.log(Level.WARNING, "Document not found or unauthorized: {0}", documentId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", documentId.toString()))
                        .build();
            }

            LOGGER.log(Level.INFO, "Returning document detail: {0}", documentId);

            return Response.ok(detail).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document detail: " + documentId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve document detail: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/clinical-history/documents/{documentId}/content - Get Document Content
    // ================================================================

    /**
     * Retrieves actual document content from peripheral node for inline display.
     *
     * <p>This endpoint performs the complete document retrieval flow:
     * <ol>
     *   <li>Validates patient authorization</li>
     *   <li>Retrieves document from peripheral node via PeripheralNodeClient</li>
     *   <li>Verifies document integrity (hash verification)</li>
     *   <li>For structured formats (JSON/FHIR/HL7): Parses content and returns as JSON</li>
     *   <li>For binary formats (PDF): Returns with inline disposition for browser display</li>
     *   <li>Logs all access attempts in audit system</li>
     * </ol>
     *
     * <p>Response Format:
     * <ul>
     *   <li>Structured (JSON/FHIR): Returns DocumentContentResponse with parsed content</li>
     *   <li>Binary (PDF): Returns raw bytes with Content-Disposition: inline</li>
     * </ul>
     *
     * <p>Example:
     * GET /api/clinical-history/documents/123/content?patientCi=12345678
     *
     * @param documentId Document ID
     * @param patientCi Patient's CI (for authorization)
     * @param request HTTP servlet request (for IP address and user agent extraction)
     * @return 200 OK with document content (JSON response or binary PDF)
     *         400 Bad Request if patientCi is missing
     *         403 Forbidden if access is denied
     *         404 Not Found if document doesn't exist
     *         503 Service Unavailable if peripheral node is down
     *         500 Internal Server Error for hash mismatches or system errors
     */
    @GET
    @Path("/documents/{documentId}/content")
    @Produces({MediaType.APPLICATION_JSON, "application/pdf", "application/xml", "application/fhir+json"})
    public Response getDocumentContent(
            @PathParam("documentId") Long documentId,
            @QueryParam("patientCi") String patientCi,
            @Context HttpServletRequest request) {

        LOGGER.log(Level.INFO, "GET /api/clinical-history/documents/{0}/content - patientCi: {1}",
                new Object[]{documentId, patientCi});

        // Extract IP address and user agent for audit logging
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        try {
            // Validate patientCi
            if (patientCi == null || patientCi.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Missing patientCi parameter");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
                        .build();
            }

            // TODO: Extract patientCi from JWT SecurityContext instead of query param
            // For now, accept it as query param for development
            // In production:
            // @Context SecurityContext securityContext;
            // String patientCi = securityContext.getUserPrincipal().getName();

            // Get document metadata from RNDC
            Optional<RndcDocument> documentOpt = rndcRepository.findById(documentId);
            if (documentOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found: {0}", documentId);
                auditService.logDocumentAccess(
                        patientCi,
                        patientCi,
                        documentId,
                        null,
                        AuditLog.ActionOutcome.FAILURE,
                        ipAddress,
                        userAgent
                );
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", documentId.toString()))
                        .build();
            }

            RndcDocument document = documentOpt.get();

            // Call service to retrieve document bytes from peripheral node
            byte[] documentBytes = clinicalHistoryService.getDocumentContent(documentId, patientCi);

            // Determine content type from document type
            String contentType = determineContentType(document.getDocumentType());

            // Log successful access
            auditService.logDocumentAccess(
                    patientCi,
                    patientCi,
                    documentId,
                    document.getDocumentType(),
                    AuditLog.ActionOutcome.SUCCESS,
                    ipAddress,
                    userAgent
            );

            LOGGER.log(Level.INFO, "Retrieved document content: {0} ({1} bytes, type: {2})",
                    new Object[]{documentId, documentBytes.length, contentType});

            // Check if this is a binary format (PDF) - return as binary for inline display
            if (contentType.equals("application/pdf")) {
                String filename = "document_" + documentId + ".pdf";

                LOGGER.log(Level.INFO, "Returning PDF for inline display: {0}", filename);

                return Response.ok(documentBytes, contentType)
                        .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                        .header("Content-Length", documentBytes.length)
                        .header("X-Frame-Options", "SAMEORIGIN")
                        .header("Cache-Control", "no-cache, no-store, must-revalidate")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .build();
            }

            // For structured formats (JSON/FHIR/HL7), parse and return as JSON response
            Object parsedContent = parseDocumentContent(documentBytes, contentType);

            // Build metadata
            DocumentMetadata metadata = buildDocumentMetadata(document);

            // Create response DTO
            DocumentContentResponse response = DocumentContentResponse.inline(
                    documentId,
                    document.getDocumentType().name(),
                    contentType,
                    parsedContent,
                    metadata
            );

            LOGGER.log(Level.INFO, "Returning structured content for inline display: {0}", documentId);

            return Response.ok(response, MediaType.APPLICATION_JSON)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build();

        } catch (ClinicalHistoryService.DocumentRetrievalException e) {
            LOGGER.log(Level.WARNING, "Document retrieval failed: {0}", e.getMessage());

            // Log failed access
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "DOCUMENT",
                    documentId.toString(),
                    AuditLog.ActionOutcome.FAILURE,
                    ipAddress,
                    userAgent,
                    null
            );

            // Map business exceptions to appropriate HTTP status codes
            if (e.getMessage().contains("no autorizado")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden("Access denied to document " + documentId))
                        .build();
            } else if (e.getMessage().contains("no encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", documentId.toString()))
                        .build();
            } else if (e.getMessage().contains("nodo periférico")) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(ErrorResponse.internalServerError("Peripheral node unavailable: " + e.getMessage()))
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ErrorResponse.internalServerError("Document retrieval failed: " + e.getMessage()))
                        .build();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving document content: " + documentId, e);

            // Log failed access
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "DOCUMENT",
                    documentId.toString(),
                    AuditLog.ActionOutcome.FAILURE,
                    ipAddress,
                    userAgent,
                    null
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Unexpected error: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Parses document content based on content type
     *
     * @param documentBytes Raw document bytes
     * @param contentType Content type (MIME type)
     * @return Parsed content object (JsonNode for JSON, String for XML/text)
     */
    private Object parseDocumentContent(byte[] documentBytes, String contentType) {
        try {
            String contentString = new String(documentBytes, java.nio.charset.StandardCharsets.UTF_8);

            // Parse JSON/FHIR content
            if (contentType.contains("json") || contentType.contains("fhir")) {
                LOGGER.log(Level.FINE, "Parsing JSON content");
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readTree(contentString);
            }

            // Return XML/HL7 as formatted string
            if (contentType.contains("xml") || contentType.contains("hl7")) {
                LOGGER.log(Level.FINE, "Returning XML content as string");
                return contentString;
            }

            // Default: return as string
            return contentString;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse document content, returning as string", e);
            // Fallback: return as base64 string if parsing fails
            return java.util.Base64.getEncoder().encodeToString(documentBytes);
        }
    }

    /**
     * Builds document metadata from RNDC document entity
     *
     * @param document RNDC document entity
     * @return DocumentMetadata DTO
     */
    private DocumentMetadata buildDocumentMetadata(RndcDocument document) {
        DocumentMetadata metadata = new DocumentMetadata();

        // Set basic metadata
        metadata.setClinicName(document.getClinicId()); // TODO: Lookup actual clinic name
        metadata.setProfessionalName(document.getCreatedBy());
        metadata.setCreatedAt(document.getCreatedAt());
        metadata.setDocumentType(document.getDocumentType().getDisplayName());
        metadata.setDocumentHash(document.getDocumentHash());
        metadata.setClinicId(document.getClinicId());

        // TODO: Enrich with patient name from INUS
        // TODO: Enrich with professional details (specialty) from professional registry

        return metadata;
    }

    /**
     * Determines content type (MIME type) from document type
     *
     * <p>Content Type Mapping:
     * <ul>
     *   <li>FHIR-compatible types: application/fhir+json</li>
     *   <li>Traditional documents: application/pdf</li>
     *   <li>Structured data: application/json</li>
     * </ul>
     */
    private String determineContentType(DocumentType documentType) {
        switch (documentType) {
            // FHIR-compatible structured data (inline display)
            case ALLERGY_RECORD:
            case VITAL_SIGNS:
                return "application/fhir+json";

            // PDF documents (inline display in browser)
            case CLINICAL_NOTE:
            case DISCHARGE_SUMMARY:
            case PRESCRIPTION:
            case VACCINATION_RECORD:
            case REFERRAL:
            case INFORMED_CONSENT:
            case SURGICAL_REPORT:
            case PATHOLOGY_REPORT:
            case CONSULTATION:
            case EMERGENCY_REPORT:
            case PROGRESS_NOTE:
            case TREATMENT_PLAN:
            case LAB_RESULT:
            case IMAGING:
            case DIAGNOSTIC_REPORT:
                return "application/pdf";

            default:
                return "application/json"; // Default to JSON for unknown types
        }
    }

    /**
     * Determines file extension from document type
     */
    private String determineFileExtension(DocumentType documentType) {
        switch (documentType) {
            case CLINICAL_NOTE:
            case DISCHARGE_SUMMARY:
            case PRESCRIPTION:
            case VACCINATION_RECORD:
            case REFERRAL:
            case INFORMED_CONSENT:
            case SURGICAL_REPORT:
            case PATHOLOGY_REPORT:
            case CONSULTATION:
            case EMERGENCY_REPORT:
            case PROGRESS_NOTE:
            case TREATMENT_PLAN:
            case LAB_RESULT:
            case IMAGING:
            case DIAGNOSTIC_REPORT:
                return ".pdf";

            case ALLERGY_RECORD:
            case VITAL_SIGNS:
                return ".json";

            default:
                return ".bin";
        }
    }

    /**
     * Extracts IP address from HTTP request
     */
    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // Check for proxy headers (X-Forwarded-For, X-Real-IP)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs, take the first one
            return ipAddress.split(",")[0].trim();
        }

        ipAddress = request.getHeader("X-Real-IP");
        if (ipAddress != null && !ipAddress.isEmpty()) {
            return ipAddress;
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    /**
     * Extracts user agent from HTTP request
     */
    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String userAgent = request.getHeader("User-Agent");
        // Truncate if too long (database column might have length limit)
        if (userAgent != null && userAgent.length() > 500) {
            return userAgent.substring(0, 500);
        }
        return userAgent;
    }

    // ================================================================
    // GET /api/clinical-history/documents/{documentId}/fhir - Get FHIR Document
    // ================================================================

    /**
     * Retrieves clinical document in FHIR format from peripheral node.
     *
     * <p>This endpoint returns the raw FHIR document as JSON without any transformation.
     * The peripheral node is expected to return FHIR-compliant resources such as:
     * <ul>
     *   <li>Bundle (type: document) - Complete clinical document</li>
     *   <li>DocumentReference - Reference to a document</li>
     *   <li>DiagnosticReport - Diagnostic results (labs, imaging)</li>
     *   <li>Observation - Individual observations (vital signs, allergies)</li>
     * </ul>
     *
     * <p>Flow:
     * <ol>
     *   <li>Authenticates patient from SecurityContext (or query param in development)</li>
     *   <li>Retrieves document metadata from RNDC (verifies patient authorization)</li>
     *   <li>Calls peripheral node at documentLocator URL with Accept: application/fhir+json</li>
     *   <li>Returns FHIR JSON directly to frontend (no transformation)</li>
     *   <li>Logs access in audit system</li>
     * </ol>
     *
     * <p>Example FHIR Response:
     * <pre>
     * {
     *   "resourceType": "Bundle",
     *   "type": "document",
     *   "entry": [
     *     {
     *       "resource": {
     *         "resourceType": "Composition",
     *         "title": "Clinical Note",
     *         "date": "2025-11-18T10:30:00Z",
     *         "author": [{"reference": "Practitioner/123"}],
     *         "section": [...]
     *       }
     *     }
     *   ]
     * }
     * </pre>
     *
     * <p>Example:
     * GET /api/clinical-history/documents/123/fhir?patientCi=12345678
     *
     * @param documentId Document ID from RNDC
     * @param patientCi Patient's CI (for authorization, in development - use JWT in production)
     * @param request HTTP servlet request (for IP address and user agent extraction)
     * @return 200 OK with FHIR JSON document (Content-Type: application/fhir+json)
     *         400 Bad Request if patientCi is missing
     *         403 Forbidden if patient doesn't own the document
     *         404 Not Found if document doesn't exist
     *         502 Bad Gateway if peripheral node is unreachable
     *         500 Internal Server Error for other failures
     */
    @GET
    @Path("/documents/{documentId}/fhir")
    @Produces({"application/fhir+json", MediaType.APPLICATION_JSON})
    public Response getFhirDocument(
            @PathParam("documentId") Long documentId,
            @QueryParam("patientCi") String patientCi,
            @Context HttpServletRequest request) {

        LOGGER.log(Level.INFO, "GET /api/clinical-history/documents/{0}/fhir - patientCi: {1}",
                new Object[]{documentId, patientCi});

        // Extract IP address and user agent for audit logging
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        try {
            // Validate patientCi
            if (patientCi == null || patientCi.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Missing patientCi parameter");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
                        .build();
            }

            // TODO: Extract patientCi from JWT SecurityContext instead of query param
            // For now, accept it as query param for development
            // In production:
            // @Context SecurityContext securityContext;
            // String patientCi = securityContext.getUserPrincipal().getName();

            // Call service to retrieve FHIR document
            String fhirJson = clinicalHistoryService.getFhirDocument(documentId, patientCi);

            LOGGER.log(Level.INFO, "Returning FHIR document: {0} ({1} characters)",
                    new Object[]{documentId, fhirJson.length()});

            // Return raw FHIR JSON with appropriate content type
            return Response.ok(fhirJson)
                    .header("Content-Type", "application/fhir+json; charset=utf-8")
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build();

        } catch (ClinicalHistoryService.DocumentRetrievalException e) {
            LOGGER.log(Level.WARNING, "FHIR document retrieval failed: {0}", e.getMessage());

            // Log failed access attempt
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "DOCUMENT",
                    documentId.toString(),
                    AuditLog.ActionOutcome.FAILURE,
                    ipAddress,
                    userAgent,
                    null
            );

            // Map business exceptions to appropriate HTTP status codes
            if (e.getMessage().contains("no autorizado")) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(ErrorResponse.forbidden("Access denied to document " + documentId))
                        .build();
            } else if (e.getMessage().contains("no encontrado")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", documentId.toString()))
                        .build();
            } else if (e.getMessage().contains("nodo periférico")) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity(ErrorResponse.internalServerError("Peripheral node unavailable: " + e.getMessage()))
                        .build();
            } else if (e.getMessage().contains("Formato de documento inválido")) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ErrorResponse.internalServerError("Invalid document format: " + e.getMessage()))
                        .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ErrorResponse.internalServerError("Document retrieval failed: " + e.getMessage()))
                        .build();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error retrieving FHIR document: " + documentId, e);

            // Log failed access attempt
            auditService.logAccessEvent(
                    patientCi,
                    "PATIENT",
                    "DOCUMENT",
                    documentId.toString(),
                    AuditLog.ActionOutcome.FAILURE,
                    ipAddress,
                    userAgent,
                    null
            );

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Unexpected error: " + e.getMessage()))
                    .build();
        }
    }

    // ================================================================
    // GET /api/clinical-history/stats - Get Document Statistics
    // ================================================================

    /**
     * Retrieves document statistics for a patient.
     *
     * <p>Returns aggregated counts by type, clinic, year, etc.
     *
     * <p>Example:
     * GET /api/clinical-history/stats?patientCi=12345678
     *
     * @param patientCi Patient's CI
     * @return 200 OK with DocumentStatsDTO
     *         400 Bad Request if patientCi is missing
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentStatistics(@QueryParam("patientCi") String patientCi) {

        LOGGER.log(Level.INFO, "GET /api/clinical-history/stats - patientCi: {0}", patientCi);

        try {
            // Validate patientCi
            if (patientCi == null || patientCi.trim().isEmpty()) {
                LOGGER.log(Level.WARNING, "Missing patientCi parameter");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.validationError("Patient CI is required"))
                        .build();
            }

            // TODO: Extract patientCi from JWT SecurityContext

            // Call service
            DocumentStatsDTO stats = clinicalHistoryService.getDocumentStatistics(patientCi);

            LOGGER.log(Level.INFO, "Returning statistics for patient: {0}", patientCi);

            return Response.ok(stats).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving statistics for patient: " + patientCi, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve statistics: " + e.getMessage()))
                    .build();
        }
    }
}
