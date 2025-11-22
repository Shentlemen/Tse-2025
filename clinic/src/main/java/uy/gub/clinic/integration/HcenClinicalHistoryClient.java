package uy.gub.clinic.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentContentResponse;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO;
import uy.gub.clinic.integration.dto.hcen.HcenPaginatedDocumentListResponse;

import java.io.InputStream;
import java.util.Optional;

/**
 * Cliente REST específico para los endpoints de historia clínica del HCEN.
 */
@Stateless
public class HcenClinicalHistoryClient {

    private static final Logger logger = LoggerFactory.getLogger(HcenClinicalHistoryClient.class);

    private final Client httpClient;
    private final ObjectMapper objectMapper;

    public HcenClinicalHistoryClient() {
        this.httpClient = ClientBuilder.newClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Optional<HcenPaginatedDocumentListResponse> getDocuments(String baseUrl,
                                                                    String clinicId,
                                                                    String apiKey,
                                                                    String patientCi,
                                                                    int page,
                                                                    int size) {
        String endpoint = buildBaseUrl(baseUrl) + "/clinical-history";

        try {
            WebTarget target = httpClient.target(endpoint)
                    .queryParam("patientCi", patientCi)
                    .queryParam("page", page)
                    .queryParam("size", size);

            Response response = prepareAuthorizedRequest(target, clinicId, apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                logger.warn("HCEN documents list request failed: status={}, body={}",
                        response.getStatus(), safeReadBody(response));
                return Optional.empty();
            }

            String json = response.readEntity(String.class);
            HcenPaginatedDocumentListResponse result =
                    objectMapper.readValue(json, HcenPaginatedDocumentListResponse.class);
            return Optional.ofNullable(result);
        } catch (ProcessingException ex) {
            logger.error("Error calling HCEN documents endpoint", ex);
            return Optional.empty();
        } catch (Exception ex) {
            logger.error("Error parsing HCEN paginated response", ex);
            return Optional.empty();
        }
    }

    public Optional<HcenDocumentDetailDTO> getDocumentDetail(String baseUrl,
                                                             String clinicId,
                                                             String apiKey,
                                                             Long documentId,
                                                             String patientCi) {
        String endpoint = buildBaseUrl(baseUrl) + "/clinical-history/documents/" + documentId;

        try {
            WebTarget target = httpClient.target(endpoint)
                    .queryParam("patientCi", patientCi);

            Response response = prepareAuthorizedRequest(target, clinicId, apiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                logger.warn("HCEN document detail request failed: status={}, body={}",
                        response.getStatus(), safeReadBody(response));
                return Optional.empty();
            }

            String json = response.readEntity(String.class);
            HcenDocumentDetailDTO detail = objectMapper.readValue(json, HcenDocumentDetailDTO.class);
            return Optional.ofNullable(detail);
        } catch (ProcessingException ex) {
            logger.error("Error calling HCEN document detail endpoint", ex);
            return Optional.empty();
        } catch (Exception ex) {
            logger.error("Error parsing HCEN document detail response", ex);
            return Optional.empty();
        }
    }

    public Optional<Response> getDocumentContentRaw(String baseUrl,
                                                    String clinicId,
                                                    String apiKey,
                                                    Long documentId,
                                                    String patientCi,
                                                    String specialtyName) {
        String endpoint = buildBaseUrl(baseUrl) + "/clinical-history/documents/" + documentId + "/content";

        try {
        WebTarget target = httpClient.target(endpoint)
                .queryParam("patientCi", patientCi);

        // Add specialty parameter if provided
        if (specialtyName != null && !specialtyName.isBlank()) {
            target = target.queryParam("specialty", specialtyName);
        }

        Response response = prepareAuthorizedRequest(target, clinicId, apiKey)
                .accept(MediaType.WILDCARD_TYPE)
                .get();
            return Optional.of(response);
        } catch (ProcessingException ex) {
            logger.error("Error calling HCEN document content endpoint", ex);
            return Optional.empty();
        }
    }

    public Optional<HcenDocumentContentResponse> parseContentResponse(Response response) {
        if (response == null) {
            return Optional.empty();
        }

        MediaType mediaType = response.getMediaType();
        if (mediaType != null && mediaType.toString().contains("json")) {
            try {
                String json = response.readEntity(String.class);
                HcenDocumentContentResponse content =
                        objectMapper.readValue(json, HcenDocumentContentResponse.class);
                return Optional.of(content);
            } catch (Exception ex) {
                logger.error("Error parsing HCEN document content JSON", ex);
            }
        }

        return Optional.empty();
    }

    public Optional<byte[]> readBinaryContent(Response response) {
        if (response == null) {
            return Optional.empty();
        }

        try (InputStream inputStream = response.readEntity(InputStream.class)) {
            return Optional.ofNullable(inputStream).map(is -> {
                try {
                    return is.readAllBytes();
                } catch (Exception e) {
                    logger.error("Error reading binary response from HCEN", e);
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.error("Error processing binary stream from HCEN", ex);
            return Optional.empty();
        }
    }

    private Invocation.Builder prepareAuthorizedRequest(WebTarget target, String clinicId, String apiKey) {
        return target
                .request()
                .header("X-Clinic-Id", clinicId)
                .header("X-API-Key", apiKey != null ? apiKey : "");
    }

    private String buildBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("El endpoint HCEN no está configurado para la clínica");
        }

        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        if (!normalized.endsWith("/api")) {
            normalized += "/api";
        }
        return normalized;
    }

    private String safeReadBody(Response response) {
        try {
            return response.readEntity(String.class);
        } catch (Exception ex) {
            return "<unable to read body>";
        }
    }

    @PreDestroy
    public void close() {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}

