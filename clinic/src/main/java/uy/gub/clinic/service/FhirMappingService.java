package uy.gub.clinic.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Stateless;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.clinic.entity.ClinicalDocument;
import uy.gub.clinic.entity.Professional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Servicio para mapear entidades del sistema a recursos FHIR
 */
@Stateless
public class FhirMappingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FhirMappingService.class);
    private static final FhirContext fhirContext = FhirContext.forR4();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convierte un Patient a recurso FHIR Patient
     */
    public Patient convertToFhirPatient(uy.gub.clinic.entity.Patient patient) {
        Patient fhirPatient = new Patient();
        
        // ID
        fhirPatient.setId(patient.getId().toString());
        
        // Identificadores
        List<Identifier> identifiers = new ArrayList<>();
        
        if (patient.getDocumentNumber() != null && !patient.getDocumentNumber().isEmpty()) {
            Identifier docId = new Identifier();
            docId.setSystem("urn:oid:2.16.858.1.1.3"); // Sistema para Cédula de Identidad Uruguaya
            docId.setValue(patient.getDocumentNumber());
            docId.setType(new CodeableConcept().addCoding(
                new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "SS", "Social Security Number")
            ));
            identifiers.add(docId);
        }
        
        if (patient.getInusId() != null && !patient.getInusId().isEmpty()) {
            Identifier inusId = new Identifier();
            inusId.setSystem("urn:oid:2.16.858.1.1.1"); // INUS Uruguay
            inusId.setValue(patient.getInusId());
            identifiers.add(inusId);
        }
        
        fhirPatient.setIdentifier(identifiers);
        
        // Nombre
        HumanName name = new HumanName();
        name.setFamily(patient.getLastName());
        name.addGiven(patient.getName());
        name.setUse(HumanName.NameUse.OFFICIAL);
        fhirPatient.addName(name);
        
        // Fecha de nacimiento
        if (patient.getBirthDate() != null) {
            fhirPatient.setBirthDate(convertToDate(patient.getBirthDate()));
        }
        
        // Género
        if (patient.getGender() != null) {
            switch (patient.getGender().toUpperCase()) {
                case "M":
                case "MALE":
                case "MASCULINO":
                    fhirPatient.setGender(Enumerations.AdministrativeGender.MALE);
                    break;
                case "F":
                case "FEMALE":
                case "FEMENINO":
                    fhirPatient.setGender(Enumerations.AdministrativeGender.FEMALE);
                    break;
                case "O":
                case "OTHER":
                case "OTRO":
                    fhirPatient.setGender(Enumerations.AdministrativeGender.OTHER);
                    break;
                default:
                    fhirPatient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }
        
        // Contacto
        List<ContactPoint> contacts = new ArrayList<>();
        if (patient.getPhone() != null && !patient.getPhone().isEmpty()) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(patient.getPhone());
            contacts.add(phone);
        }
        if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
            ContactPoint email = new ContactPoint();
            email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            email.setValue(patient.getEmail());
            contacts.add(email);
        }
        fhirPatient.setTelecom(contacts);
        
        // Dirección
        if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
            Address address = new Address();
            address.setText(patient.getAddress());
            address.setUse(Address.AddressUse.HOME);
            fhirPatient.addAddress(address);
        }
        
        return fhirPatient;
    }
    
    /**
     * Convierte un ClinicalDocument a Bundle FHIR (sin incluir Patient, ya que se agrega en el endpoint)
     */
    public Bundle convertDocumentToFhirBundle(ClinicalDocument document) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        bundle.setId("bundle-" + document.getId());
        
        // Convertir Patient para usarlo en las referencias (pero NO agregarlo al bundle)
        Patient fhirPatient = convertToFhirPatient(document.getPatient());
        
        // 1. Practitioner Resource (Profesional)
        Practitioner practitioner = convertToFhirPractitioner(document.getProfessional());
        bundle.addEntry().setResource(practitioner).setFullUrl("Practitioner/" + practitioner.getId());
        
        // 2. Encounter Resource (Encuentro/Consulta)
        Encounter encounter = convertToFhirEncounter(document, fhirPatient, practitioner);
        bundle.addEntry().setResource(encounter).setFullUrl("Encounter/" + encounter.getId());
        
        // 3. Condition Resources (Diagnósticos)
        if (document.getDiagnosis() != null && !document.getDiagnosis().trim().isEmpty()) {
            Condition condition = convertToFhirCondition(document, fhirPatient, encounter);
            bundle.addEntry().setResource(condition).setFullUrl("Condition/" + condition.getId());
        }
        
        // 4. Observation Resources (Signos Vitales)
        List<Observation> vitalSigns = convertVitalSignsToObservations(document, fhirPatient, encounter);
        for (Observation obs : vitalSigns) {
            bundle.addEntry().setResource(obs).setFullUrl("Observation/" + obs.getId());
        }
        
        // 5. MedicationRequest Resources (Prescripciones)
        List<MedicationRequest> medications = convertPrescriptionsToMedicationRequests(document, fhirPatient, encounter);
        for (MedicationRequest med : medications) {
            bundle.addEntry().setResource(med).setFullUrl("MedicationRequest/" + med.getId());
        }
        
        // 6. DocumentReference Resource (Referencia al documento)
        DocumentReference docRef = convertToFhirDocumentReference(document, fhirPatient, encounter);
        bundle.addEntry().setResource(docRef).setFullUrl("DocumentReference/" + docRef.getId());
        
        return bundle;
    }
    
    /**
     * Convierte un Professional a recurso FHIR Practitioner
     */
    private Practitioner convertToFhirPractitioner(Professional professional) {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(professional.getId().toString());
        
        // Nombre
        HumanName name = new HumanName();
        name.setFamily(professional.getLastName());
        name.addGiven(professional.getName());
        name.setUse(HumanName.NameUse.OFFICIAL);
        practitioner.addName(name);
        
        // Identificador (número de licencia)
        if (professional.getLicenseNumber() != null && !professional.getLicenseNumber().isEmpty()) {
            Identifier identifier = new Identifier();
            identifier.setSystem("urn:oid:2.16.858.1.1.2"); // Sistema de licencias médicas
            identifier.setValue(professional.getLicenseNumber());
            practitioner.addIdentifier(identifier);
        }
        
        // Contacto
        if (professional.getEmail() != null && !professional.getEmail().isEmpty()) {
            ContactPoint email = new ContactPoint();
            email.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            email.setValue(professional.getEmail());
            practitioner.addTelecom(email);
        }
        
        if (professional.getPhone() != null && !professional.getPhone().isEmpty()) {
            ContactPoint phone = new ContactPoint();
            phone.setSystem(ContactPoint.ContactPointSystem.PHONE);
            phone.setValue(professional.getPhone());
            practitioner.addTelecom(phone);
        }
        
        return practitioner;
    }
    
    /**
     * Convierte un ClinicalDocument a recurso FHIR Encounter
     */
    private Encounter convertToFhirEncounter(ClinicalDocument document, org.hl7.fhir.r4.model.Patient patient, Practitioner practitioner) {
        Encounter encounter = new Encounter();
        encounter.setId("encounter-" + document.getId());
        encounter.setStatus(Encounter.EncounterStatus.FINISHED);
        encounter.setClass_(new Coding("http://terminology.hl7.org/CodeSystem/v3-ActCode", "AMB", "ambulatory"));
        
        // Paciente
        encounter.setSubject(new Reference("Patient/" + patient.getId()));
        
        // Período de la consulta
        Period period = new Period();
        period.setStart(convertToDate(document.getDateOfVisit()));
        encounter.setPeriod(period);
        
        // Participantes (profesional)
        Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
        participant.setIndividual(new Reference("Practitioner/" + practitioner.getId()));
        encounter.addParticipant(participant);
        
        // Motivo de consulta
        if (document.getChiefComplaint() != null && !document.getChiefComplaint().trim().isEmpty()) {
            encounter.addReasonCode()
                .addCoding()
                .setDisplay(document.getChiefComplaint());
        }
        
        // Tipo de servicio (especialidad)
        if (document.getSpecialty() != null) {
            encounter.addType().addCoding()
                .setCode(document.getSpecialty().getCode())
                .setDisplay(document.getSpecialty().getName());
        }
        
        return encounter;
    }
    
    /**
     * Convierte el diagnóstico a recurso FHIR Condition
     */
    private Condition convertToFhirCondition(ClinicalDocument document, org.hl7.fhir.r4.model.Patient patient, Encounter encounter) {
        Condition condition = new Condition();
        condition.setId("condition-" + document.getId());
        condition.setClinicalStatus(new CodeableConcept().addCoding(
            new Coding("http://terminology.hl7.org/CodeSystem/condition-clinical", "active", "Active")
        ));
        
        // Paciente
        condition.setSubject(new Reference("Patient/" + patient.getId()));
        
        // Encuentro
        condition.setEncounter(new Reference("Encounter/" + encounter.getId()));
        
        // Diagnóstico (texto libre por ahora, idealmente usar códigos SNOMED CT)
        condition.setCode(new CodeableConcept().setText(document.getDiagnosis()));
        
        // Fecha de diagnóstico
        condition.setOnset(new DateTimeType(convertToDate(document.getDateOfVisit())));
        
        return condition;
    }
    
    /**
     * Convierte signos vitales JSON a recursos FHIR Observation
     */
    private List<Observation> convertVitalSignsToObservations(ClinicalDocument document, org.hl7.fhir.r4.model.Patient patient, Encounter encounter) {
        List<Observation> observations = new ArrayList<>();
        
        if (document.getVitalSigns() == null || document.getVitalSigns().trim().isEmpty()) {
            return observations;
        }
        
        try {
            JsonNode vitalSignsJson = objectMapper.readTree(document.getVitalSigns());
            Date observationDate = convertToDate(document.getDateOfVisit());
            
            // Presión arterial
            if (vitalSignsJson.has("pressure") && !vitalSignsJson.get("pressure").asText().isEmpty()) {
                Observation obs = createVitalSignObservation(
                    "85354-9", "Blood pressure panel", 
                    vitalSignsJson.get("pressure").asText(), 
                    patient, encounter, observationDate, "pressure-" + document.getId()
                );
                observations.add(obs);
            }
            
            // Temperatura
            if (vitalSignsJson.has("temperature") && vitalSignsJson.get("temperature").isNumber()) {
                Observation obs = createVitalSignObservation(
                    "8310-5", "Body temperature", 
                    vitalSignsJson.get("temperature").asText() + " °C", 
                    patient, encounter, observationDate, "temp-" + document.getId()
                );
                observations.add(obs);
            }
            
            // Pulso
            if (vitalSignsJson.has("pulse") && vitalSignsJson.get("pulse").isNumber()) {
                Observation obs = createVitalSignObservation(
                    "8867-4", "Heart rate", 
                    vitalSignsJson.get("pulse").asText() + " bpm", 
                    patient, encounter, observationDate, "pulse-" + document.getId()
                );
                observations.add(obs);
            }
            
            // Frecuencia respiratoria
            if (vitalSignsJson.has("respiratoryRate") && vitalSignsJson.get("respiratoryRate").isNumber()) {
                Observation obs = createVitalSignObservation(
                    "9279-1", "Respiratory rate", 
                    vitalSignsJson.get("respiratoryRate").asText() + " /min", 
                    patient, encounter, observationDate, "resp-" + document.getId()
                );
                observations.add(obs);
            }
            
            // Saturación O2
            if (vitalSignsJson.has("o2Saturation") && vitalSignsJson.get("o2Saturation").isNumber()) {
                Observation obs = createVitalSignObservation(
                    "2708-6", "Oxygen saturation in Arterial blood", 
                    vitalSignsJson.get("o2Saturation").asText() + " %", 
                    patient, encounter, observationDate, "o2-" + document.getId()
                );
                observations.add(obs);
            }
            
            // Peso
            if (vitalSignsJson.has("weight") && vitalSignsJson.get("weight").isNumber()) {
                Observation obs = createVitalSignObservation(
                    "29463-7", "Body weight", 
                    vitalSignsJson.get("weight").asText() + " kg", 
                    patient, encounter, observationDate, "weight-" + document.getId()
                );
                observations.add(obs);
            }
            
            // Altura
            if (vitalSignsJson.has("height") && vitalSignsJson.get("height").isNumber()) {
                Observation obs = createVitalSignObservation(
                    "8302-2", "Body height", 
                    vitalSignsJson.get("height").asText() + " cm", 
                    patient, encounter, observationDate, "height-" + document.getId()
                );
                observations.add(obs);
            }
            
        } catch (Exception e) {
            logger.warn("Error al parsear signos vitales JSON: {}", e.getMessage());
        }
        
        return observations;
    }
    
    /**
     * Crea un Observation para un signo vital
     */
    private Observation createVitalSignObservation(String loincCode, String display, String value, 
                                                   org.hl7.fhir.r4.model.Patient patient, Encounter encounter, 
                                                   Date date, String id) {
        Observation obs = new Observation();
        obs.setId(id);
        obs.setStatus(Observation.ObservationStatus.FINAL);
        
        // Código LOINC
        obs.getCode().addCoding()
            .setSystem("http://loinc.org")
            .setCode(loincCode)
            .setDisplay(display);
        
        // Valor
        obs.setValue(new StringType(value));
        
        // Paciente
        obs.setSubject(new Reference("Patient/" + patient.getId()));
        
        // Encuentro
        obs.setEncounter(new Reference("Encounter/" + encounter.getId()));
        
        // Fecha
        obs.setEffective(new DateTimeType(date));
        
        return obs;
    }
    
    /**
     * Convierte prescripciones JSON a recursos FHIR MedicationRequest
     */
    private List<MedicationRequest> convertPrescriptionsToMedicationRequests(ClinicalDocument document, 
                                                                              org.hl7.fhir.r4.model.Patient patient, Encounter encounter) {
        List<MedicationRequest> medications = new ArrayList<>();
        
        if (document.getPrescriptions() == null || document.getPrescriptions().trim().isEmpty()) {
            return medications;
        }
        
        try {
            JsonNode prescriptionsJson = objectMapper.readTree(document.getPrescriptions());
            
            if (prescriptionsJson.isArray()) {
                int index = 0;
                for (JsonNode prescription : prescriptionsJson) {
                    MedicationRequest medRequest = new MedicationRequest();
                    medRequest.setId("medication-" + document.getId() + "-" + index);
                    medRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
                    medRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
                    
                    // Medicamento
                    if (prescription.has("medication")) {
                        medRequest.getMedicationCodeableConcept()
                            .setText(prescription.get("medication").asText());
                    }
                    
                    // Paciente
                    medRequest.setSubject(new Reference("Patient/" + patient.getId()));
                    
                    // Encuentro
                    medRequest.setEncounter(new Reference("Encounter/" + encounter.getId()));
                    
                    // Dosificación
                    if (prescription.has("dosage") || prescription.has("frequency") || prescription.has("duration")) {
                        Dosage dosage = new Dosage();
                        if (prescription.has("dosage")) {
                            dosage.setText(prescription.get("dosage").asText());
                        }
                        if (prescription.has("frequency") || prescription.has("duration")) {
                            Timing timing = new Timing();
                            Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
                            if (prescription.has("frequency")) {
                                repeat.setFrequency(prescription.get("frequency").asInt());
                            }
                            if (prescription.has("duration")) {
                                repeat.setDuration(prescription.get("duration").asDouble());
                            }
                            timing.setRepeat(repeat);
                            dosage.setTiming(timing);
                        }
                        medRequest.addDosageInstruction(dosage);
                    }
                    
                    medications.add(medRequest);
                    index++;
                }
            }
        } catch (Exception e) {
            logger.warn("Error al parsear prescripciones JSON: {}", e.getMessage());
        }
        
        return medications;
    }
    
    /**
     * Convierte un ClinicalDocument a recurso FHIR DocumentReference
     */
    private DocumentReference convertToFhirDocumentReference(ClinicalDocument document, org.hl7.fhir.r4.model.Patient patient, Encounter encounter) {
        DocumentReference docRef = new DocumentReference();
        docRef.setId("document-" + document.getId());
        docRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        
        // Tipo de documento
        CodeableConcept type = new CodeableConcept();
        type.setText(document.getDocumentType());
        docRef.setType(type);
        
        // Fecha
        docRef.setDate(convertToDate(document.getDateOfVisit()));
        
        // Paciente
        docRef.setSubject(new Reference("Patient/" + patient.getId()));
        
        // Contexto (encuentro)
        DocumentReference.DocumentReferenceContextComponent context = new DocumentReference.DocumentReferenceContextComponent();
        context.addEncounter(new Reference("Encounter/" + encounter.getId()));
        docRef.setContext(context);
        
        // Contenido (descripción)
        if (document.getDescription() != null && !document.getDescription().trim().isEmpty()) {
            DocumentReference.DocumentReferenceContentComponent content = new DocumentReference.DocumentReferenceContentComponent();
            Attachment attachment = new Attachment();
            attachment.setContentType("text/plain");
            attachment.setData(document.getDescription().getBytes());
            content.setAttachment(attachment);
            docRef.addContent(content);
        }
        
        // Título
        docRef.setDescription(document.getTitle());
        
        return docRef;
    }
    
    /**
     * Convierte LocalDate a Date
     */
    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Serializa un Bundle FHIR a JSON
     */
    public String bundleToJson(Bundle bundle) {
        IParser parser = fhirContext.newJsonParser();
        parser.setPrettyPrint(true);
        return parser.encodeResourceToString(bundle);
    }
}

