package uy.gub.hcen.clinicalhistory.service;

/**
 * Custom exception for access denied (policy evaluation failure)
 */
public class ClinicalDocumentAccessDenied extends RuntimeException {
    public ClinicalDocumentAccessDenied(String message) {
        super(message);
    }
}