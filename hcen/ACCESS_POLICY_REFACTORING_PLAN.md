Access Policies Refactoring Plan                                                                                                                                                                                                                                                                                                                                     
Overview

Simplify the policy system from flexible ABAC to a clinic+specialty permission model where patients grant access to professionals with a specific specialty from a specific clinic.
                                                                                                                                                                                     
---                                                                                                                                                                                 
Phase 1: Database Migration

Create V008__refactor_access_policies.sql:
- Drop columns: policy_type, policy_config, policy_effect
- Add columns: clinic_id (NOT NULL), specialty (NOT NULL), document_id (nullable), status (DEFAULT 'GRANTED')
- Add foreign key to clinics table
- Add index on (patient_ci, clinic_id, specialty)

 ---                                                                                                                                                                                 
Phase 2: Backend - New Enum & DTO

1. Create MedicalSpecialty.java enum with 10 specialties:
- CARDIOLOGIA, MEDICINA_GENERAL, ONCOLOGIA, PEDIATRIA, NEUROLOGIA
- CIRUGIA, GINECOLOGIA, DERMATOLOGIA, PSIQUIATRIA, TRAUMATOLOGIA
2. Create PolicyStatus.java enum:
- GRANTED (default), PENDING, REVOKED
3. Update PolicyCreateRequest.java:
- Remove: policyType, policyConfig, policyEffect
- Add: clinicId (required), specialty (required), documentId (optional)

 ---                                                                                                                                                                                 
Phase 3: Backend - Entity & Repository

1. Update AccessPolicy.java:
- Remove: PolicyType, PolicyEffect enums, policyConfig
- Add: clinicId, specialty, documentId, status (PolicyStatus)
2. Update AccessPolicyRepository:
- Add methods for new query patterns (findByPatientAndClinicAndSpecialty)

 ---                                                                                                                                                                                 
Phase 4: Backend - Service & Evaluation

1. Simplify PolicyEngine.java:
- New evaluation: Check if professional's clinic+specialty matches any GRANTED policy
- If no matching policy → DENY
2. Delete old evaluators (no longer needed):
- SpecialtyPolicyEvaluator, ClinicPolicyEvaluator, DocumentTypePolicyEvaluator, etc.
3. Update PolicyManagementService.java:
- Simplify createPolicy() to use new fields
- Add validation for clinic exists and specialty is valid

 ---                                                                                                                                                                                 
Phase 5: Backend - REST API

1. Update PolicyManagementResource.java:
- Modify POST /api/policies for new request format
2. Add new endpoints:
- GET /api/clinics/active - List clinics for dropdown
- GET /api/specialties - List medical specialties
3. Update responses to return new fields (clinicName, specialty, status)

 ---                                                                                                                                                                                 
Phase 6: Frontend - access-policies.jsp

1. Replace JSON editor with dropdowns:
- Clinic dropdown (populated from /api/clinics/active)
- Specialty dropdown (fixed list in Spanish)
2. Remove:
- Policy type selector
- Effect selector (always GRANTED)
- JSON textarea
3. Simplify policy display:
- Format: "Cardiólogos de Hospital de Clínicas - Otorgado"
4. Add create policy form with:
- Clinic select, Specialty select, Submit button

 ---                                                                                                                                                                                 
Phase 7: Update TODO.md

Document all changes:
- Files created/modified
- Lines of code
- New endpoints
- Migration details
- Update completion percentage

 ---                                                                                                                                                                                 
Expected Outcome

New Policy Creation Flow:
1. Patient selects clinic from dropdown
2. Patient selects specialty from dropdown
3. System creates policy with status=GRANTED, documentId=null (ALL documents)

New Response Format:                                                                                                                                                                
{                                                                                                                                                                                   
"id": 1,                                                                                                                                                                          
"patientCi": "12345678",                                                                                                                                                          
"clinicId": "clinic-001",                                                                                                                                                         
"clinicName": "Hospital de Clínicas",                                                                                                                                             
"specialty": "CARDIOLOGIA",                                                                                                                                                       
"specialtyName": "Cardiología",                                                                                                                                                   
"documentId": null,                                                                                                                                                               
"status": "GRANTED",                                                                                                                                                              
"createdAt": "2025-11-18T10:00:00"                                                                                                                                                
}
                                                                                                                                                                                     
---                                                                                                                                                                                 
Breaking Changes

⚠️ All existing policies will be invalidated - schema incompatible
- Recommend clearing policies table or creating data migration

 ---                                                                                                                                                                                
Estimated Files to Modify

| Action | Count                                          |                                                                                                                        
 |--------|------------------------------------------------|                                                                                                                        
| Create | 4 files (enum, DTO, migration, endpoints)      |                                                                                                                        
| Modify | 8 files (entity, service, resource, JSP, etc.) |                                                                                                                        
| Delete | 6 files (old evaluators)                       |