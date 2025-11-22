package uy.gub.clinic.integration.dto.hcen;

import java.util.List;

/**
 * Respuesta paginada del endpoint de historia clínica de HCEN.
 */
public class HcenPaginatedDocumentListResponse {

    private List<HcenDocumentListItemDTO> documents;
    private int currentPage;
    private int totalPages;
    private long totalDocuments;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    public List<HcenDocumentListItemDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<HcenDocumentListItemDTO> documents) {
        this.documents = documents;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}

