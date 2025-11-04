package uy.gub.hcen.clinicalhistory.api.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import uy.gub.hcen.api.dto.ErrorResponse;
import uy.gub.hcen.clinicalhistory.dto.*;
import uy.gub.hcen.clinicalhistory.service.ClinicalHistoryService;
import uy.gub.hcen.rndc.entity.DocumentType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
            @QueryParam("size") @DefaultValue("20") int size) {

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

            // Call service
            PaginatedDocumentListResponse response = clinicalHistoryService.getClinicalHistory(
                    patientCi,
                    docType,
                    from,
                    to,
                    clinicId,
                    page,
                    size
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
     * Retrieves document content URL for download/viewing.
     *
     * <p>This endpoint returns a URL to retrieve the actual document content.
     * In the future, this will integrate with peripheral nodes to fetch content.
     *
     * <p>Example:
     * GET /api/clinical-history/documents/123/content?patientCi=12345678
     *
     * @param documentId Document ID
     * @param patientCi Patient's CI (for authorization)
     * @return 200 OK with DocumentContentResponse
     *         404 Not Found if document doesn't exist or content unavailable
     *         500 Internal Server Error if operation fails
     */
    @GET
    @Path("/documents/{documentId}/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentContent(
            @PathParam("documentId") Long documentId,
            @QueryParam("patientCi") String patientCi) {

        LOGGER.log(Level.INFO, "GET /api/clinical-history/documents/{0}/content - patientCi: {1}",
                new Object[]{documentId, patientCi});

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
            DocumentContentResponse content = clinicalHistoryService.getDocumentContent(documentId, patientCi);

            if (content == null || !content.isAvailable()) {
                LOGGER.log(Level.WARNING, "Document content not available: {0}", documentId);
                return Response.ok(content != null ? content :
                        DocumentContentResponse.unavailable("Contenido no disponible")).build();
            }

            LOGGER.log(Level.INFO, "Returning document content URL: {0}", documentId);

            return Response.ok(content).build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving document content: " + documentId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ErrorResponse.internalServerError("Failed to retrieve document content: " + e.getMessage()))
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
