package uy.gub.hcen.clinicalhistory.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Document Statistics DTO
 *
 * Aggregated statistics about a patient's clinical documents.
 * Used for dashboard summary cards and analytics.
 *
 * @author TSE 2025 Group 9
 * @version 1.0
 * @since 2025-11-04
 */
public class DocumentStatsDTO {

    /**
     * Total number of documents
     */
    private long totalDocuments;

    /**
     * Document count by type
     * Map of document type to count (e.g., "LAB_RESULT" -> 10, "IMAGING" -> 5)
     */
    private Map<String, Long> byType;

    /**
     * Document count by clinic
     * Map of clinic ID to count
     */
    private Map<String, Long> byClinic;

    /**
     * Document count by year
     * Map of year to count (e.g., "2025" -> 15, "2024" -> 8)
     */
    private Map<String, Long> byYear;

    /**
     * Count of active documents
     */
    private long activeDocuments;

    /**
     * Count of inactive documents
     */
    private long inactiveDocuments;

    /**
     * Default constructor
     */
    public DocumentStatsDTO() {
        this.byType = new HashMap<>();
        this.byClinic = new HashMap<>();
        this.byYear = new HashMap<>();
    }

    /**
     * Constructor with total count
     */
    public DocumentStatsDTO(long totalDocuments) {
        this();
        this.totalDocuments = totalDocuments;
    }

    /**
     * Adds a document type count
     *
     * @param documentType Document type
     * @param count Count
     */
    public void addTypeCount(String documentType, long count) {
        this.byType.put(documentType, count);
    }

    /**
     * Adds a clinic count
     *
     * @param clinicId Clinic ID
     * @param count Count
     */
    public void addClinicCount(String clinicId, long count) {
        this.byClinic.put(clinicId, count);
    }

    /**
     * Adds a year count
     *
     * @param year Year
     * @param count Count
     */
    public void addYearCount(String year, long count) {
        this.byYear.put(year, count);
    }

    // Getters and Setters

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public Map<String, Long> getByType() {
        return byType;
    }

    public void setByType(Map<String, Long> byType) {
        this.byType = byType;
    }

    public Map<String, Long> getByClinic() {
        return byClinic;
    }

    public void setByClinic(Map<String, Long> byClinic) {
        this.byClinic = byClinic;
    }

    public Map<String, Long> getByYear() {
        return byYear;
    }

    public void setByYear(Map<String, Long> byYear) {
        this.byYear = byYear;
    }

    public long getActiveDocuments() {
        return activeDocuments;
    }

    public void setActiveDocuments(long activeDocuments) {
        this.activeDocuments = activeDocuments;
    }

    public long getInactiveDocuments() {
        return inactiveDocuments;
    }

    public void setInactiveDocuments(long inactiveDocuments) {
        this.inactiveDocuments = inactiveDocuments;
    }

    @Override
    public String toString() {
        return "DocumentStatsDTO{" +
                "totalDocuments=" + totalDocuments +
                ", activeDocuments=" + activeDocuments +
                ", inactiveDocuments=" + inactiveDocuments +
                ", typeCount=" + byType.size() +
                ", clinicCount=" + byClinic.size() +
                ", yearCount=" + byYear.size() +
                '}';
    }
}
