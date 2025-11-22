package uy.gub.clinic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.Clinic;
import uy.gub.clinic.integration.HcenClinicalHistoryClient;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentContentResponse;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO;
import uy.gub.clinic.integration.dto.hcen.HcenDocumentListItemDTO;
import uy.gub.clinic.integration.dto.hcen.HcenPaginatedDocumentListResponse;
import uy.gub.clinic.service.dto.RemoteDocumentContent;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Fachada de alto nivel para obtener documentos remotos desde HCEN.
 */
@Stateless
public class HcenDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(HcenDocumentService.class);
    private static final int DEFAULT_PAGE_SIZE = 50;

    @Inject
    private HcenClinicalHistoryClient clinicalHistoryClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<HcenDocumentListItemDTO> fetchDocumentsForPatient(Clinic clinic, String patientCi) {
        if (!isConfigured(clinic) || patientCi == null || patientCi.isBlank()) {
            return Collections.emptyList();
        }

        Optional<HcenPaginatedDocumentListResponse> response = clinicalHistoryClient.getDocuments(
                clinic.getHcenEndpoint(),
                clinic.getId(),
                clinic.getApiKey(),
                patientCi,
                0,
                DEFAULT_PAGE_SIZE
        );

        return response.map(HcenPaginatedDocumentListResponse::getDocuments)
                .orElse(Collections.emptyList());
    }

    public Optional<HcenDocumentDetailDTO> fetchDocumentDetail(Clinic clinic, String patientCi, Long documentId) {
        if (!isConfigured(clinic) || patientCi == null || patientCi.isBlank() || documentId == null) {
            return Optional.empty();
        }

        return clinicalHistoryClient.getDocumentDetail(
                clinic.getHcenEndpoint(),
                clinic.getId(),
                clinic.getApiKey(),
                documentId,
                patientCi
        );
    }

    public Optional<RemoteDocumentContent> fetchDocumentContent(Clinic clinic,
                                                                String patientCi,
                                                                Long documentId) {
        if (!isConfigured(clinic) || patientCi == null || patientCi.isBlank() || documentId == null) {
            return Optional.empty();
        }

        Optional<Response> responseOpt = clinicalHistoryClient.getDocumentContentRaw(
                clinic.getHcenEndpoint(),
                clinic.getId(),
                clinic.getApiKey(),
                documentId,
                patientCi
        );

        if (responseOpt.isEmpty()) {
            return Optional.empty();
        }

        Response response = responseOpt.get();
        MediaType mediaType = response.getMediaType();
        try (response) {
            if (mediaType != null && mediaType.toString().contains("json")) {
                Optional<HcenDocumentContentResponse> inlineOpt = clinicalHistoryClient.parseContentResponse(response);
                return inlineOpt.map(content -> RemoteDocumentContent.inline(
                        content.getContent() != null ? content.getContent().toPrettyString() : null,
                        mediaType.toString()));
            }

            Optional<byte[]> dataOpt = clinicalHistoryClient.readBinaryContent(response);
            if (dataOpt.isPresent()) {
                String filename = "document-" + documentId;
                if (mediaType != null && mediaType.getSubtype().contains("pdf")) {
                    filename += ".pdf";
                }
                String resolvedType = mediaType != null ? mediaType.toString() : MediaType.APPLICATION_OCTET_STREAM;
                return Optional.of(RemoteDocumentContent.binary(dataOpt.get(), resolvedType, filename));
            }
        } catch (Exception ex) {
            logger.error("Error processing HCEN document content", ex);
        }

        return Optional.empty();
    }

    private boolean isConfigured(Clinic clinic) {
        return clinic != null &&
                clinic.getHcenEndpoint() != null && !clinic.getHcenEndpoint().isBlank() &&
                clinic.getApiKey() != null && !clinic.getApiKey().isBlank();
    }
}

