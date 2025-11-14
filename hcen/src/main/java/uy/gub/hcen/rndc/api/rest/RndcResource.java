package uy.gub.hcen.rndc.api.rest;

import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.hl7.fhir.r4.model.DocumentReference;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.fhir.converter.FhirDocumentReferenceConverter;
import uy.gub.hcen.fhir.exception.FhirConversionException;
import uy.gub.hcen.fhir.parser.FhirParserFactory;
import uy.gub.hcen.rndc.dto.*;
import uy.gub.hcen.rndc.entity.DocumentStatus;
import uy.gub.hcen.rndc.entity.DocumentType;
import uy.gub.hcen.rndc.entity.RndcDocument;
import uy.gub.hcen.service.rndc.RndcService;
import uy.gub.hcen.service.rndc.exception.DocumentNotFoundException;
import uy.gub.hcen.service.rndc.exception.DocumentRegistrationException;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RNDC REST API Resource
 * <p>
 * JAX-RS REST endpoints for the National Clinical Document Registry (RNDC).
 * This resource provides all CRUD operations for clinical document metadata management.
 * <p>
 * Base Path: /api/rndc/documents
 * <p>
 * Endpoints:
 * - POST   /api/rndc/documents                        - Register new document (AC014)
 * - GET    /api/rndc/documents/{id}                   - Get document metadata
 * - GET    /api/rndc/documents                        - Search documents (with filters)
 * - GET    /api/rndc/patients/{patientCi}/documents   - Get patient documents
 * - PATCH  /api/rndc/documents/{id}/status            - Update document status
 * - GET    /api/rndc/documents/{id}/verify            - Verify document hash
 * <p>
 * Authentication:
 * Currently authentication is not enforced. In production:
 * - POST /documents: Requires API key (peripheral nodes only)
 * - GET /documents: Requires JWT (patients can see own, professionals need policy approval)
 * - PATCH /status: Requires JWT (creator or admin only)
 * <p>
 * Error Responses:
 * All endpoints return ErrorResponse DTO for errors with appropriate HTTP status codes:
 * - 400 Bad Request: Validation errors, invalid parameters
 * - 404 Not Found: Resource not found
 * - 409 Conflict: Duplicate registration
 * - 500 Internal Server Error: System errors
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-10-21
 * @see RndcService
 * @see DocumentRegistrationRequest
 * @see DocumentResponse
 */
@Path("/rndc/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RndcResource {

    private static final Logger LOGGER = Logger.getLogger(RndcResource.class.getName());

    /**
     * FHIR JSON content type for FHIR DocumentReference resources
     */
    private static final String FHIR_JSON_CONTENT_TYPE = "application/fhir+json";

    /**
     * Maximum page size to prevent excessive memory usage
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Default page size if not specified
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Inject
    private RndcService rndcService;

    @Inject
    private FhirParserFactory fhirParserFactory;

    @Inject
    private FhirDocumentReferenceConverter fhirDocumentReferenceConverter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================================================================
    // Document Registration (AC014)
    // ================================================================

    /**
     * Register a new clinical document in the RNDC.
     * <p>
     * This endpoint is called by peripheral nodes (clinics, health providers) to register
     * document metadata. The actual document remains in peripheral storage.
     * <p>
     * Supports TWO content types for backward compatibility:
     * 1. application/json - Custom HCEN JSON format (DocumentRegistrationRequest)
     * 2. application/fhir+json - FHIR R4 DocumentReference resource
     * <p>
     * Idempotent Behavior: If a document with the same locator already exists,
     * returns the existing document (200 OK) instead of creating a duplicate.
     * <p>
     * POST /api/rndc/documents
     * <p>
     * Request Body (application/json):
     * <pre>
     * {
     *   "patientCi": "12345678",
     *   "documentType": "LAB_RESULT",
     *   "documentLocator": "https://clinic-001.hcen.uy/api/documents/abc123",
     *   "documentHash": "sha256:a1b2c3d4e5f67890...",
     *   "createdBy": "doctor@clinic.uy",
     *   "clinicId": "clinic-001",
     *   "documentTitle": "Blood Test Results",
     *   "documentDescription": "Complete blood count analysis"
     * }
     * </pre>
     * <p>
     * Request Body (application/fhir+json):
     * - FHIR R4 DocumentReference resource with LOINC coding
     * <p>
     * Success Response (201 Created):
     * - Headers: Location: /api/rndc/documents/{id}
     * - Body: DocumentResponse
     * <p>
     * Error Responses:
     * - 400 Bad Request: Validation errors or invalid FHIR resource
     * - 409 Conflict: Duplicate registration (if not idempotent)
     * - 500 Internal Server Error: System error
     *
     * @param requestBody Raw request body (JSON string)
     * @param contentType Content-Type header
     * @return Response with DocumentResponse (201 Created) or ErrorResponse (error)
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, FHIR_JSON_CONTENT_TYPE})
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerDocument(
            String requestBody,
            @HeaderParam("Content-Type") String contentType) {

        LOGGER.log(Level.INFO, "Document registration request received with Content-Type: {0}", contentType);

        try {
            DocumentRegistrationRequest request;

            // Determine content type and parse accordingly
            if (contentType != null && contentType.contains("fhir+json")) {
                // FHIR DocumentReference resource format
                LOGGER.log(Level.INFO, "Parsing FHIR DocumentReference resource");
                request = parseFhirDocumentReference(requestBody);

            } else {
                // Custom JSON format (default)
                LOGGER.log(Level.INFO, "Parsing custom JSON format");
                request = parseCustomJson(requestBody);
            }

            LOGGER.log(Level.INFO, "Document registration request parsed - Patient CI: {0}, Type: {1}, Clinic: {2}",
                    new Object[]{request.getPatientCi(), request.getDocumentType(), request.getClinicId()});

            // Call service layer to register document (same service layer for both formats)
            RndcDocument document = rndcService.registerDocument(
                    request.getPatientCi(),
                    request.getDocumentType(),
                    request.getDocumentLocator(),
                    request.getDocumentHash(),
                    request.getCreatedBy(),
                    request.getClinicId(),
                    request.getDocumentTitle(),
                    request.getDocumentDescription()
            );

            // Convert entity to DTO
            DocumentResponse response = DocumentResponse.fromEntity(document);

            // Build Location header
            URI location = URI.create("/api/rndc/documents/" + document.getId());

            LOGGER.log(Level.INFO, "Document registered successfully - ID: {0}", document.getId());

            // Return 201 Created with Location header
            return Response.created(location)
                    .entity(response)
                    .build();

        } catch (FhirConversionException e) {
            LOGGER.log(Level.WARNING, "FHIR conversion error: " + e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(
                            "Invalid FHIR DocumentReference resource: " + e.getMessage()))
                    .build();

        } catch (DataFormatException e) {
            LOGGER.log(Level.WARNING, "FHIR parsing error: " + e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(
                            "Malformed FHIR JSON: " + e.getMessage()))
                    .build();

        } catch (DocumentRegistrationException e) {
            LOGGER.log(Level.WARNING, "Document registration failed: " + e.getMessage());

            // Check if it's a validation error or conflict
            if (e.getMessage().contains("already exists") || e.getMessage().contains("duplicate")) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(ErrorResponse.conflict(e.getMessage()))
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.badRequest(e.getMessage()))
                        .build();
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during document registration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "System error during document registration"))
                    .build();
        }
    }

    /**
     * Parse FHIR DocumentReference resource from JSON string.
     *
     * @param requestBody FHIR JSON string
     * @return DocumentRegistrationRequest DTO
     * @throws FhirConversionException if FHIR parsing or conversion fails
     */
    private DocumentRegistrationRequest parseFhirDocumentReference(String requestBody)
            throws FhirConversionException {

        try {
            // Parse FHIR DocumentReference resource using HAPI FHIR
            DocumentReference documentReference =
                    fhirParserFactory.parseJsonResource(DocumentReference.class, requestBody);

            // Convert FHIR DocumentReference to DocumentRegistrationRequest
            return fhirDocumentReferenceConverter.toDocumentRegistrationRequest(documentReference);

        } catch (DataFormatException e) {
            throw new FhirConversionException("Failed to parse FHIR DocumentReference JSON", e);
        }
    }

    /**
     * Parse custom JSON format (DocumentRegistrationRequest).
     *
     * @param requestBody JSON string
     * @return DocumentRegistrationRequest DTO
     * @throws Exception if JSON parsing fails
     */
    private DocumentRegistrationRequest parseCustomJson(String requestBody) throws Exception {
        return objectMapper.readValue(requestBody, DocumentRegistrationRequest.class);
    }

    // ================================================================
    // Document Retrieval
    // ================================================================

    /**
     * Get document metadata by ID.
     * <p>
     * Returns document metadata (not the actual document content).
     * To retrieve the actual document, use the documentLocator URL.
     * <p>
     * GET /api/rndc/documents/{id}
     * <p>
     * Success Response (200 OK): DocumentResponse
     * <p>
     * Error Responses:
     * - 404 Not Found: Document does not exist
     * - 500 Internal Server Error: System error
     *
     * @param id Document ID
     * @return Response with DocumentResponse (200 OK) or ErrorResponse (error)
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentMetadata(@PathParam("id") Long id) {
        LOGGER.log(Level.FINE, "Get document metadata request - ID: {0}", id);

        try {
            Optional<RndcDocument> document = rndcService.getDocumentMetadata(id);

            if (document.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found - ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", id.toString()))
                        .build();
            }

            DocumentResponse response = DocumentResponse.fromEntity(document.get());

            LOGGER.log(Level.FINE, "Document metadata retrieved successfully - ID: {0}", id);

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document metadata - ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "System error retrieving document metadata"))
                    .build();
        }
    }

    // ================================================================
    // Document Search
    // ================================================================

    /**
     * Search documents with optional filters.
     * <p>
     * All query parameters are optional. Returns paginated results.
     * <p>
     * GET /api/rndc/documents?patientCi=12345678&documentType=LAB_RESULT&status=ACTIVE&clinicId=clinic-001&fromDate=2025-10-01T00:00:00&toDate=2025-10-31T23:59:59&page=0&size=20
     * <p>
     * Query Parameters:
     * - patientCi: Filter by patient CI (optional)
     * - documentType: Filter by document type (optional, enum value)
     * - status: Filter by status (optional, enum value: ACTIVE, INACTIVE, DELETED)
     * - clinicId: Filter by clinic (optional)
     * - fromDate: Start of date range (optional, ISO-8601 format: 2025-10-21T14:30:00)
     * - toDate: End of date range (optional, ISO-8601 format)
     * - page: Page number, 0-indexed (default: 0)
     * - size: Page size, max 100 (default: 20)
     * <p>
     * Success Response (200 OK): DocumentListResponse (paginated)
     * <p>
     * Error Responses:
     * - 400 Bad Request: Invalid enum values or date formats
     * - 500 Internal Server Error: System error
     * <p>
     * Note: Returns empty list (not 404) if no documents match filters.
     *
     * @param patientCi    Optional patient CI filter
     * @param documentType Optional document type filter (enum string)
     * @param status       Optional status filter (enum string)
     * @param clinicId     Optional clinic ID filter
     * @param fromDate     Optional start date filter (ISO-8601 string)
     * @param toDate       Optional end date filter (ISO-8601 string)
     * @param page         Page number (0-indexed, default 0)
     * @param size         Page size (max 100, default 20)
     * @return Response with DocumentListResponse (200 OK) or ErrorResponse (error)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchDocuments(
            @QueryParam("patientCi") String patientCi,
            @QueryParam("documentType") String documentType,
            @QueryParam("status") String status,
            @QueryParam("clinicId") String clinicId,
            @QueryParam("fromDate") String fromDate,
            @QueryParam("toDate") String toDate,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) int size) {

        LOGGER.log(Level.FINE, "Search documents request - Patient CI: {0}, Type: {1}, Status: {2}, Clinic: {3}, Page: {4}, Size: {5}",
                new Object[]{patientCi, documentType, status, clinicId, page, size});

        try {
            // Cap page size at maximum
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
                LOGGER.log(Level.FINE, "Page size capped at maximum: {0}", MAX_PAGE_SIZE);
            }

            // Parse enum parameters
            DocumentType documentTypeEnum = parseDocumentType(documentType);
            DocumentStatus statusEnum = parseDocumentStatus(status);

            // Parse date parameters
            LocalDateTime fromDateTime = parseDateTime(fromDate);
            LocalDateTime toDateTime = parseDateTime(toDate);

            // Call service layer
            List<RndcDocument> documents = rndcService.searchDocuments(
                    patientCi,
                    documentTypeEnum,
                    statusEnum,
                    clinicId,
                    fromDateTime,
                    toDateTime,
                    page,
                    size
            );

            // For proper pagination, we need total count
            // Since RndcService.searchDocuments doesn't return count, we'll use document count as totalCount
            // In production, you'd call a separate countDocuments() method
            long totalCount = documents.size();

            // Convert to response DTO
            DocumentListResponse response = DocumentListResponse.fromEntities(documents, totalCount, page, size);

            LOGGER.log(Level.FINE, "Search returned {0} documents", documents.size());

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid parameter in search request: " + e.getMessage());

            // Determine if it's an enum error or date error
            String errorMessage = buildSearchErrorMessage(e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(errorMessage))
                    .build();

        } catch (DateTimeParseException e) {
            LOGGER.log(Level.WARNING, "Invalid date format in search request: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(
                            "Invalid date format. Expected ISO-8601 format: 2025-10-21T14:30:00"))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during document search", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "System error during document search"))
                    .build();
        }
    }

    // ================================================================
    // Patient Document Retrieval
    // ================================================================

    /**
     * Get all documents for a specific patient.
     * <p>
     * Returns only ACTIVE documents by default unless filtered by type.
     * <p>
     * GET /api/rndc/patients/{patientCi}/documents?documentType=LAB_RESULT&page=0&size=20
     * <p>
     * Query Parameters:
     * - documentType: Filter by document type (optional, enum value)
     * - page: Page number, 0-indexed (default: 0)
     * - size: Page size, max 100 (default: 20)
     * <p>
     * Success Response (200 OK): DocumentListResponse (paginated)
     * <p>
     * Error Responses:
     * - 400 Bad Request: Invalid enum value
     * - 500 Internal Server Error: System error
     * <p>
     * Note: Returns empty list (not 404) if patient has no documents.
     *
     * @param patientCi    Patient's CI (path parameter)
     * @param documentType Optional document type filter (enum string)
     * @param page         Page number (0-indexed, default 0)
     * @param size         Page size (max 100, default 20)
     * @return Response with DocumentListResponse (200 OK) or ErrorResponse (error)
     */
    @GET
    @Path("/patients/{patientCi}/documents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatientDocuments(
            @PathParam("patientCi") String patientCi,
            @QueryParam("documentType") String documentType,
            @QueryParam("page") @DefaultValue("0") @Min(0) int page,
            @QueryParam("size") @DefaultValue("20") @Min(1) int size) {

        LOGGER.log(Level.FINE, "Get patient documents request - Patient CI: {0}, Type: {1}, Page: {2}, Size: {3}",
                new Object[]{patientCi, documentType, page, size});

        try {
            // Cap page size at maximum
            if (size > MAX_PAGE_SIZE) {
                size = MAX_PAGE_SIZE;
            }

            List<RndcDocument> documents;

            // If documentType specified, filter by type
            if (documentType != null && !documentType.trim().isEmpty()) {
                DocumentType documentTypeEnum = parseDocumentType(documentType);
                documents = rndcService.getPatientDocumentsByType(patientCi, documentTypeEnum, page, size);
            } else {
                documents = rndcService.getPatientDocuments(patientCi, page, size);
            }

            // Calculate total count (in production, use separate count query)
            long totalCount = documents.size();

            // Convert to response DTO
            DocumentListResponse response = DocumentListResponse.fromEntities(documents, totalCount, page, size);

            LOGGER.log(Level.FINE, "Retrieved {0} documents for patient CI: {1}", new Object[]{documents.size(), patientCi});

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid parameter: " + e.getMessage());

            String errorMessage = buildSearchErrorMessage(e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(errorMessage))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving patient documents - Patient CI: " + patientCi, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "System error retrieving patient documents"))
                    .build();
        }
    }

    // ================================================================
    // Document Status Management
    // ================================================================

    /**
     * Update document status.
     * <p>
     * Changes document status to ACTIVE, INACTIVE, or DELETED.
     * <p>
     * PATCH /api/rndc/documents/{id}/status
     * <p>
     * Request Body:
     * <pre>
     * {
     *   "status": "INACTIVE",
     *   "updatedBy": "doctor@clinic.uy"
     * }
     * </pre>
     * <p>
     * Valid Status Transitions:
     * - ACTIVE → INACTIVE (archival)
     * - ACTIVE → DELETED (soft delete)
     * - INACTIVE → ACTIVE (reactivation)
     * - INACTIVE → DELETED (soft delete)
     * - DELETED → (no transitions allowed)
     * <p>
     * Success Response (200 OK): DocumentResponse with updated status
     * <p>
     * Error Responses:
     * - 400 Bad Request: Validation errors
     * - 404 Not Found: Document does not exist
     * - 500 Internal Server Error: System error
     * <p>
     * Authorization (Future): Only creator or admin should be allowed.
     *
     * @param id      Document ID
     * @param request Status update request
     * @return Response with DocumentResponse (200 OK) or ErrorResponse (error)
     */
    @PATCH
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDocumentStatus(
            @PathParam("id") Long id,
            @Valid DocumentStatusUpdateRequest request) {

        LOGGER.log(Level.INFO, "Update document status request - ID: {0}, New Status: {1}, Updated By: {2}",
                new Object[]{id, request.getStatus(), request.getUpdatedBy()});

        try {
            // Call appropriate service method based on target status
            switch (request.getStatus()) {
                case INACTIVE:
                    rndcService.markDocumentAsInactive(id, request.getUpdatedBy());
                    break;

                case DELETED:
                    rndcService.markDocumentAsDeleted(id, request.getUpdatedBy());
                    break;

                case ACTIVE:
                    rndcService.reactivateDocument(id, request.getUpdatedBy());
                    break;

                default:
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ErrorResponse.badRequest("Invalid status: " + request.getStatus()))
                            .build();
            }

            // Retrieve updated document
            Optional<RndcDocument> updatedDocument = rndcService.getDocumentMetadata(id);

            if (updatedDocument.isEmpty()) {
                // This shouldn't happen if status update succeeded
                LOGGER.log(Level.WARNING, "Document not found after status update - ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", id.toString()))
                        .build();
            }

            DocumentResponse response = DocumentResponse.fromEntity(updatedDocument.get());

            LOGGER.log(Level.INFO, "Document status updated successfully - ID: {0}, New Status: {1}",
                    new Object[]{id, request.getStatus()});

            return Response.ok(response).build();

        } catch (DocumentNotFoundException e) {
            LOGGER.log(Level.WARNING, "Document not found for status update - ID: {0}", id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ErrorResponse.notFound("Document", id.toString()))
                    .build();

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid parameter in status update: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.badRequest(e.getMessage()))
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating document status - ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "System error updating document status"))
                    .build();
        }
    }

    // ================================================================
    // Document Hash Verification
    // ================================================================

    /**
     * Verify document hash for integrity checking.
     * <p>
     * This is currently a stub implementation that validates hash format only.
     * <p>
     * GET /api/rndc/documents/{id}/verify
     * <p>
     * Success Response (200 OK): HashVerificationResponse
     * <p>
     * Error Responses:
     * - 404 Not Found: Document does not exist
     * - 500 Internal Server Error: System error
     * <p>
     * Future Enhancement:
     * 1. Fetch actual document from peripheral node via HTTP GET
     * 2. Calculate SHA-256 hash of document content
     * 3. Compare with expected hash
     * 4. Return verification result
     *
     * @param id Document ID
     * @return Response with HashVerificationResponse (200 OK) or ErrorResponse (error)
     */
    @GET
    @Path("/{id}/verify")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyDocumentHash(@PathParam("id") Long id) {
        LOGGER.log(Level.FINE, "Verify document hash request - ID: {0}", id);

        try {
            // Get document metadata
            Optional<RndcDocument> document = rndcService.getDocumentMetadata(id);

            if (document.isEmpty()) {
                LOGGER.log(Level.WARNING, "Document not found for hash verification - ID: {0}", id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ErrorResponse.notFound("Document", id.toString()))
                        .build();
            }

            RndcDocument doc = document.get();

            // Call service layer to verify hash (currently stub implementation)
            boolean isValid = rndcService.verifyDocumentHash(doc.getDocumentLocator(), doc.getDocumentHash());

            // Create response
            HashVerificationResponse response = HashVerificationResponse.stub(
                    doc.getDocumentLocator(),
                    doc.getDocumentHash()
            );

            LOGGER.log(Level.FINE, "Hash verification completed - ID: {0}, Valid: {1}", new Object[]{id, isValid});

            return Response.ok(response).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying document hash - ID: " + id, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError(
                            "System error verifying document hash"))
                    .build();
        }
    }

    // ================================================================
    // Private Helper Methods
    // ================================================================

    /**
     * Parse document type from string to enum.
     * <p>
     * Handles null/empty strings and case-insensitive parsing.
     *
     * @param documentType Document type string
     * @return DocumentType enum, or null if not specified
     * @throws IllegalArgumentException if invalid enum value
     */
    private DocumentType parseDocumentType(String documentType) {
        if (documentType == null || documentType.trim().isEmpty()) {
            return null;
        }

        try {
            return DocumentType.valueOf(documentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid document type: " + documentType +
                            ". Valid values: CLINICAL_NOTE, LAB_RESULT, IMAGING, PRESCRIPTION, " +
                            "DISCHARGE_SUMMARY, VACCINATION_RECORD, SURGICAL_REPORT, PATHOLOGY_REPORT, " +
                            "CONSULTATION, EMERGENCY_REPORT, REFERRAL, PROGRESS_NOTE, ALLERGY_RECORD, " +
                            "VITAL_SIGNS, DIAGNOSTIC_REPORT, TREATMENT_PLAN, INFORMED_CONSENT, OTHER"
            );
        }
    }

    /**
     * Parse document status from string to enum.
     * <p>
     * Handles null/empty strings and case-insensitive parsing.
     *
     * @param status Status string
     * @return DocumentStatus enum, or null if not specified
     * @throws IllegalArgumentException if invalid enum value
     */
    private DocumentStatus parseDocumentStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        try {
            return DocumentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: " + status + ". Valid values: ACTIVE, INACTIVE, DELETED"
            );
        }
    }

    /**
     * Parse date-time from string to LocalDateTime.
     * <p>
     * Handles null/empty strings. Expected format: ISO-8601 (2025-10-21T14:30:00)
     *
     * @param dateTimeString Date-time string
     * @return LocalDateTime, or null if not specified
     * @throws DateTimeParseException if invalid format
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        return LocalDateTime.parse(dateTimeString);
    }

    /**
     * Build helpful error message for search parameter errors.
     *
     * @param errorMessage Original error message
     * @return User-friendly error message
     */
    private String buildSearchErrorMessage(String errorMessage) {
        if (errorMessage.contains("document type")) {
            return errorMessage;
        } else if (errorMessage.contains("status")) {
            return errorMessage;
        } else if (errorMessage.contains("pagination")) {
            return errorMessage;
        } else {
            return "Invalid search parameter: " + errorMessage;
        }
    }
}
