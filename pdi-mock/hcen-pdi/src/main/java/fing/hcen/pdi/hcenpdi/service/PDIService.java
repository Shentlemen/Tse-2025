package fing.hcen.pdi.hcenpdi.service;

import fing.hcen.pdi.hcenpdi.model.ParamObtPersonaPorDoc;
import fing.hcen.pdi.hcenpdi.model.ProductInfo;
import fing.hcen.pdi.hcenpdi.model.ResultObtPersonaPorDoc;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * PDI (Plataforma de Interoperabilidad) SOAP Service Interface
 * Simulates DNIC "Servicio Básico de Información"
 */
@WebService(name = "PDIService", targetNamespace = "http://pdi.hcen.fing/")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL)
public interface PDIService {

    /**
     * Obtain person data by document number
     *
     * @param params Request parameters (organizacion, passwordEntidad, NroDocumento, TipoDocumento)
     * @return Person data with errors/warnings if applicable
     */
    @WebMethod(operationName = "obtPersonaPorDoc")
    ResultObtPersonaPorDoc obtPersonaPorDoc(
            @WebParam(name = "parametros") ParamObtPersonaPorDoc params
    );

    /**
     * Get product/service description and version
     *
     * @return Service metadata (version, modalidad, descripcion)
     */
    @WebMethod(operationName = "productDesc")
    ProductInfo productDesc();
}
