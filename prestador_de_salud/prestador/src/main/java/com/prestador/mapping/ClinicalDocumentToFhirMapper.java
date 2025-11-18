package com.prestador.mapping;

import com.prestador.entity.ClinicalDocument;
import org.hl7.fhir.r4.model.*;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.codesystems.CompositionStatus;

import java.time.ZoneId;
import java.util.Date;

public class ClinicalDocumentToFhirMapper {

    private final FhirContext ctx = FhirContext.forR4();

    public String clinicalDocumentToFhirBundleJson(ClinicalDocument doc, byte[] attachmentBytes) {
        // Bundle raíz
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.DOCUMENT);

        // 1) Patient reference (referencia simple; podrías crear el recurso Patient completo)
        Patient patient = new Patient();
        patient.setId("Patient/" + doc.getPatientId());
        patient.addIdentifier().setSystem("http://tu.sistema/ids/patient").setValue(doc.getPatientId());
        // opcional: nombre, sexo, birthDate si los tienes
        bundle.addEntry().setFullUrl(patient.getId()).setResource(patient);

        // 2) Practitioner referencia
        Practitioner practitioner = new Practitioner();
        practitioner.setId("Practitioner/" + doc.getProfessionalId());
        practitioner.addIdentifier().setSystem("http://tu.sistema/ids/practitioner").setValue(doc.getProfessionalId());
        bundle.addEntry().setFullUrl(practitioner.getId()).setResource(practitioner);

        // 3) Document Binary + DocumentReference (si viene un archivo)
        if (attachmentBytes != null && doc.getFileName() != null) {
            Binary binary = new Binary();
            binary.setId("Binary/" + doc.getId() + "-binary");
            binary.setContentType(doc.getMimeType() != null ? doc.getMimeType() : "application/octet-stream");
            binary.setData(attachmentBytes);
            bundle.addEntry().setFullUrl(binary.getId()).setResource(binary);

            DocumentReference docRef = new DocumentReference();
            docRef.setId("DocumentReference/" + doc.getId());
            docRef.addIdentifier().setSystem("http://localhost:8080/prestador-salud").setValue(doc.getRndcId() != null ? doc.getRndcId() : doc.getId().toString());
            docRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
            docRef.setDocStatus(DocumentReference.ReferredDocumentStatus.FINAL);
            docRef.setType(new CodeableConcept().setText(doc.getDocumentType()));
            docRef.setSubject(new Reference(patient.getId()));
            docRef.setDate(doc.getDateOfVisit() != null ?
                Date.from(doc.getDateOfVisit().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                new Date());
            // apuntamiento al Binary
            DocumentReference.DocumentReferenceContentComponent content = new DocumentReference.DocumentReferenceContentComponent();
            Attachment att = new Attachment();
            att.setUrl(binary.getId()); // referencia relativa
            att.setTitle(doc.getFileName());
            if (doc.getFileSize() != null) {
                att.setSize(Math.toIntExact(doc.getFileSize()));
            }
            att.setContentType(doc.getMimeType());
            content.setAttachment(att);
            docRef.addContent(content);

            bundle.addEntry().setFullUrl(docRef.getId()).setResource(docRef);
        }

        // 4) Vital Signs as Observations
        // Since vital signs are stored as TEXT, we create a simple observation with the text
        if (doc.getVitalSigns() != null && !doc.getVitalSigns().trim().isEmpty()) {
            Observation vitalSignsObs = new Observation();
            vitalSignsObs.setId("Observation/" + doc.getId() + "-vital-signs");
            vitalSignsObs.setStatus(Observation.ObservationStatus.FINAL);
            vitalSignsObs.setCode(new CodeableConcept()
                .addCoding(new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("8716-3")
                    .setDisplay("Vital signs")));
            vitalSignsObs.setSubject(new Reference(patient.getId()));
            vitalSignsObs.setEffective(new DateTimeType(doc.getDateOfVisit() != null ?
                Date.from(doc.getDateOfVisit().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                new Date()));
            vitalSignsObs.setValue(new StringType(doc.getVitalSigns()));
            bundle.addEntry().setFullUrl(vitalSignsObs.getId()).setResource(vitalSignsObs);
        }

        // 5) Composition (document root)
        Composition composition = new Composition();
        composition.setId("Composition/" + doc.getId());
        composition.setStatus(Composition.CompositionStatus.FINAL);
        composition.setType(new CodeableConcept().setText(doc.getDocumentType()));
        composition.setTitle(doc.getTitle());
        composition.setDate(doc.getCreatedAt() != null ? java.util.Date.from(doc.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()) : new Date());
        composition.setSubject(new Reference(patient.getId()));
        composition.addAuthor().setReference(practitioner.getId());

        // añadir secciones — p. ej. chief complaint, examenes, diagnosis
        if (doc.getChiefComplaint() != null && !doc.getChiefComplaint().trim().isEmpty()) {
            Composition.SectionComponent secChief = composition.addSection();
            secChief.setTitle("Chief Complaint");
            secChief.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secChief.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getChiefComplaint()) + "</div>");
        }

        // Current illness section
        if (doc.getCurrentIllness() != null && !doc.getCurrentIllness().trim().isEmpty()) {
            Composition.SectionComponent secCurrentIllness = composition.addSection();
            secCurrentIllness.setTitle("History of Present Illness");
            secCurrentIllness.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secCurrentIllness.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getCurrentIllness()) + "</div>");
        }

        // Physical examination section
        if (doc.getPhysicalExamination() != null && !doc.getPhysicalExamination().trim().isEmpty()) {
            Composition.SectionComponent secPhysicalExam = composition.addSection();
            secPhysicalExam.setTitle("Physical Examination");
            secPhysicalExam.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secPhysicalExam.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getPhysicalExamination()) + "</div>");
        }

        // Vital signs section (reference to observation if present)
        if (doc.getVitalSigns() != null && !doc.getVitalSigns().trim().isEmpty()) {
            Composition.SectionComponent secVitals = composition.addSection();
            secVitals.setTitle("Vital Signs");
            secVitals.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secVitals.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getVitalSigns()) + "</div>");
            secVitals.addEntry(new Reference("Observation/" + doc.getId() + "-vital-signs"));
        }

        // Diagnosis section
        if (doc.getDiagnosis() != null && !doc.getDiagnosis().trim().isEmpty()) {
            Composition.SectionComponent secDiag = composition.addSection();
            secDiag.setTitle("Diagnosis");
            secDiag.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secDiag.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getDiagnosis()) + "</div>");
        }

        // Treatment plan section
        if (doc.getTreatment() != null && !doc.getTreatment().trim().isEmpty()) {
            Composition.SectionComponent secTreatment = composition.addSection();
            secTreatment.setTitle("Treatment Plan");
            secTreatment.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secTreatment.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getTreatment()) + "</div>");
        }

        // Prescriptions section
        if (doc.getPrescriptions() != null && !doc.getPrescriptions().trim().isEmpty()) {
            Composition.SectionComponent secPrescriptions = composition.addSection();
            secPrescriptions.setTitle("Medications");
            secPrescriptions.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secPrescriptions.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getPrescriptions()) + "</div>");
        }

        // Observations/Notes section
        if (doc.getObservations() != null && !doc.getObservations().trim().isEmpty()) {
            Composition.SectionComponent secObservations = composition.addSection();
            secObservations.setTitle("Clinical Notes");
            secObservations.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secObservations.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + escapeHtml(doc.getObservations()) + "</div>");
        }

        // Referencia a DocumentReference si existe
        if (doc.getFileName() != null && !doc.getFileName().trim().isEmpty()) {
            Composition.SectionComponent secAttachment = composition.addSection();
            secAttachment.setTitle("Attachments");
            secAttachment.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            secAttachment.getText().setDivAsString("<div xmlns=\"http://www.w3.org/1999/xhtml\">See attached document: " + escapeHtml(doc.getFileName()) + "</div>");
            secAttachment.addEntry(new Reference("DocumentReference/" + doc.getId()));
        }

        bundle.addEntry().setFullUrl(composition.getId()).setResource(composition);

        // serializar a JSON
        IParser jsonParser = ctx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        return jsonParser.encodeResourceToString(bundle);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
