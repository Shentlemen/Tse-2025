package fing.hcen.pdi.hcenpdi.service;

import fing.hcen.pdi.hcenpdi.model.ObjPersona;
import fing.hcen.pdi.hcenpdi.model.ParamObtPersonaPorDoc;
import fing.hcen.pdi.hcenpdi.model.ProductInfo;
import fing.hcen.pdi.hcenpdi.model.ResultObtPersonaPorDoc;
import fing.hcen.pdi.hcenpdi.repository.MockPersonRepository;
import jakarta.jws.WebService;

/**
 * PDI SOAP Service Implementation
 * Mock implementation of DNIC Basic Information Service
 */
@WebService(
    endpointInterface = "fing.hcen.pdi.hcenpdi.service.PDIService",
    serviceName = "PDIService",
    portName = "PDIServicePort",
    targetNamespace = "http://pdi.hcen.fing/"
)
public class PDIServiceImpl implements PDIService {

    private static final String VALID_ORGANIZATION = "HCEN";
    private static final String VALID_PASSWORD = "hcen-test-2025";

    // Error codes as per PDI specification
    private static final String ERROR_PERSON_NOT_FOUND = "500";
    private static final String ERROR_INVALID_PARAMS = "10001";
    private static final String ERROR_UNAUTHORIZED = "10002";

    private final MockPersonRepository repository;

    public PDIServiceImpl() {
        this.repository = new MockPersonRepository();
    }

    @Override
    public ResultObtPersonaPorDoc obtPersonaPorDoc(ParamObtPersonaPorDoc params) {
        ResultObtPersonaPorDoc result = new ResultObtPersonaPorDoc();

        // Validate authentication
        if (!isValidCredentials(params)) {
            result.addError(ERROR_UNAUTHORIZED, "Acceso No Autorizado");
            return result;
        }

        // Validate input parameters
        if (params.getNroDocumento() == null || params.getNroDocumento().trim().isEmpty()) {
            result.addError(ERROR_INVALID_PARAMS, "Parámetros incorrectos: NroDocumento es requerido");
            return result;
        }

        // Lookup person in mock repository
        ObjPersona persona = repository.findByDocumento(params.getNroDocumento());

        if (persona == null) {
            result.addError(ERROR_PERSON_NOT_FOUND, "Persona inexistente");
            return result;
        }

        // Success: return person data
        result.setPersona(persona);
        return result;
    }

    @Override
    public ProductInfo productDesc() {
        return new ProductInfo(
            "1.0",
            "Testing",
            "Servicio de Información D.N.I.C. - Mock"
        );
    }

    /**
     * Validate organization credentials
     */
    private boolean isValidCredentials(ParamObtPersonaPorDoc params) {
        if (params == null) {
            return false;
        }

        return VALID_ORGANIZATION.equals(params.getOrganizacion())
            && VALID_PASSWORD.equals(params.getPasswordEntidad());
    }
}
