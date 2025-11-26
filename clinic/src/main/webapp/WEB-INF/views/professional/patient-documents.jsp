<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    // Validación básica de sesión
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Documentos del Paciente - HCEN Clínica</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .sidebar {
            background: linear-gradient(135deg, #3498db, #2980b9);
            min-height: calc(100vh - 56px);
            box-shadow: 2px 0 10px rgba(0,0,0,0.1);
        }
        
        .sidebar .nav-link {
            color: rgba(255,255,255,0.8);
            padding: 12px 20px;
            border-radius: 8px;
            margin: 2px 10px;
            transition: all 0.3s ease;
        }
        
        .sidebar .nav-link:hover,
        .sidebar .nav-link.active {
            background-color: rgba(255,255,255,0.2);
            color: white;
            transform: translateX(5px);
        }
        
        .card {
            border: none;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.08);
        }
        
        .document-card {
            border-left: 4px solid #4CAF50;
            transition: all 0.3s ease;
        }
        
        .document-card:hover {
            box-shadow: 0 8px 20px rgba(0,0,0,0.12);
            transform: translateY(-2px);
        }
        
        .badge {
            padding: 6px 12px;
            font-weight: 500;
        }
        
        /* Estilos modernos para modales */
        .modal-content {
            border: none;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
            overflow: hidden;
        }
        
        .modal-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 1.5rem 2rem;
            border-radius: 16px 16px 0 0;
        }
        
        .modal-header .modal-title {
            font-weight: 600;
            font-size: 1.25rem;
            margin: 0;
            display: flex;
            align-items: center;
        }
        
        .modal-header .btn-close {
            filter: brightness(0) invert(1);
            opacity: 0.9;
            transition: opacity 0.3s ease;
        }
        
        .modal-header .btn-close:hover {
            opacity: 1;
        }
        
        .modal-body {
            padding: 2rem;
            background: #ffffff;
        }
        
        .modal-footer {
            border-top: 1px solid #e9ecef;
            padding: 1.5rem 2rem;
            background: #f8f9fa;
            border-radius: 0 0 16px 16px;
        }
        
        .modal-footer .btn {
            border-radius: 8px;
            padding: 0.6rem 1.5rem;
            font-weight: 500;
            transition: all 0.3s ease;
        }
        
        .modal-footer .btn-secondary {
            background: #6c757d;
            border: none;
            color: white;
        }
        
        .modal-footer .btn-secondary:hover {
            background: #5a6268;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
        }
        
        .modal-footer .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
        }
        
        .modal-footer .btn-primary:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
        
        .modal-backdrop {
            background-color: rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(2px);
        }
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">
                <i class="fas fa-user-md me-2"></i>Portal Profesional - ${sessionScope.clinicName}
            </a>
            
            <div class="navbar-nav ms-auto">
                <div class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="fas fa-user me-2"></i>${sessionScope.user}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="<c:url value='/professional/profile'/>">
                            <i class="fas fa-user me-2"></i>Mi Perfil
                        </a></li>
                        <li><a class="dropdown-item" href="<c:url value='/auth/logout'/>">
                            <i class="fas fa-sign-out-alt me-2"></i>Cerrar Sesión
                        </a></li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <div class="col-md-2 p-0">
                <div class="sidebar">
                    <div class="p-3">
                        <h6 class="text-white-50 mb-3">PROFESIONAL</h6>
                        <ul class="nav flex-column">
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/dashboard'/>">
                                    <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="<c:url value='/professional/patients'/>">
                                    <i class="fas fa-users me-2"></i>Pacientes
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/requests'/>">
                                    <i class="fas fa-exchange-alt me-2"></i>Solicitudes
                                </a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- Contenido Principal -->
            <div class="col-md-10 p-4">
                <!-- Información del Paciente -->
                <c:if test="${not empty patient}">
                    <div class="card mb-4">
                        <div class="card-header bg-primary text-white">
                            <div class="d-flex justify-content-between align-items-center">
                                <h5 class="mb-0">
                                    <i class="fas fa-user me-2"></i>Paciente: ${patient.fullName}
                                </h5>
                                <a href="<c:url value='/professional/patients'/>" class="btn btn-light btn-sm">
                                    <i class="fas fa-arrow-left me-2"></i>Volver a Pacientes
                                </a>
                            </div>
                        </div>
                        <div class="card-body">
                            <div class="row mb-3">
                                <div class="col-md-6">
                                    <div class="mb-2">
                                        <strong><i class="fas fa-id-card me-2 text-primary"></i>Cédula de Identidad:</strong> 
                                        <span class="ms-2">${patient.documentNumber != null ? patient.documentNumber : 'N/A'}</span>
                                    </div>
                                    <div class="mb-2">
                                        <strong><i class="fas fa-fingerprint me-2 text-primary"></i>ID INUS:</strong> 
                                        <span class="ms-2">${patient.inusId != null ? patient.inusId : 'N/A'}</span>
                                    </div>
                                    <div class="mb-2">
                                        <strong><i class="fas fa-birthday-cake me-2 text-primary"></i>Fecha de Nacimiento:</strong>
                                        <%
                                            // Obtener el paciente del request scope
                                            uy.gub.clinic.entity.Patient patientItem = (uy.gub.clinic.entity.Patient) request.getAttribute("patient");
                                            if (patientItem != null && patientItem.getBirthDate() != null) {
                                                java.util.Date birthDate = java.sql.Date.valueOf(patientItem.getBirthDate());
                                                pageContext.setAttribute("patientBirthDate", birthDate);
                                                // Calcular edad
                                                java.time.LocalDate birthLocalDate = patientItem.getBirthDate();
                                                java.time.LocalDate now = java.time.LocalDate.now();
                                                int age = java.time.Period.between(birthLocalDate, now).getYears();
                                                pageContext.setAttribute("patientAge", age);
                                        %>
                                            <span class="ms-2">
                                                <fmt:formatDate value="${patientBirthDate}" pattern="dd/MM/yyyy"/>
                                                <c:if test="${patientAge != null}">
                                                    (${patientAge} años)
                                                </c:if>
                                            </span>
                                        <%
                                            } else {
                                        %>
                                            <span class="ms-2">N/A</span>
                                        <%
                                            }
                                        %>
                                    </div>
                                    <div class="mb-2">
                                        <strong><i class="fas fa-venus-mars me-2 text-primary"></i>Género:</strong> 
                                        <span class="ms-2">${patient.gender != null ? patient.gender : 'N/A'}</span>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="mb-2">
                                        <strong><i class="fas fa-phone me-2 text-primary"></i>Teléfono:</strong> 
                                        <span class="ms-2">${patient.phone != null ? patient.phone : 'N/A'}</span>
                                    </div>
                                    <div class="mb-2">
                                        <strong><i class="fas fa-envelope me-2 text-primary"></i>Email:</strong> 
                                        <span class="ms-2">${patient.email != null ? patient.email : 'N/A'}</span>
                                    </div>
                                    <div class="mb-2">
                                        <strong><i class="fas fa-map-marker-alt me-2 text-primary"></i>Dirección:</strong> 
                                        <span class="ms-2">${patient.address != null ? patient.address : 'N/A'}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- Botón Registrar Nuevo Documento -->
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">
                        <i class="fas fa-file-medical me-2"></i>Documentos Clínicos
                    </h2>
                    <c:if test="${not empty patient}">
                        <div class="btn-group">
                            <button type="button" class="btn btn-primary" onclick="openNewDocumentModal()">
                                <i class="fas fa-plus me-2"></i>Registrar Nuevo Documento
                            </button>
                            <button type="button" class="btn btn-outline-primary" onclick="openRequestAccessModal()" title="Solicitar acceso a documentos del paciente en otras clínicas (HCEN)">
                                <i class="fas fa-exchange-alt me-2"></i>Solicitar Acceso HCEN
                            </button>
                        </div>
                    </c:if>
                </div>

                <!-- Mensaje de Error -->
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
                
                <!-- Mensaje de Éxito -->
                <c:if test="${not empty success}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="fas fa-check-circle me-2"></i>${success}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <!-- Documentos disponibles en HCEN -->
                <div class="card mb-4">
                    <div class="card-header bg-secondary text-white d-flex justify-content-between align-items-center">
                        <h5 class="mb-0"><i class="fas fa-cloud-download-alt me-2"></i>Documentos en HCEN</h5>
                        <small class="text-white-50">Metadatos cargados automáticamente desde el HCEN</small>
                    </div>
                    <div class="card-body">
                        <c:if test="${not empty externalDocumentsError}">
                            <div class="alert alert-danger" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${externalDocumentsError}
                            </div>
                        </c:if>
                        <c:if test="${not empty externalDocumentsInfo}">
                            <div class="alert alert-info" role="alert">
                                <i class="fas fa-info-circle me-2"></i>${externalDocumentsInfo}
                            </div>
                        </c:if>
                        <c:choose>
                            <c:when test="${empty externalDocuments}">
                                <div class="alert alert-light text-center" role="alert">
                                    <i class="fas fa-file-import fa-3x mb-3 d-block text-muted"></i>
                                    <h5>No hay documentos externos disponibles.</h5>
                                    <p class="mb-0">Los documentos publicados en el HCEN aparecerán aquí cuando la clínica reciba acceso.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="row">
                                    <c:forEach var="remoteDoc" items="${externalDocuments}">
                                        <div class="col-md-6 mb-3">
                                            <div class="card border border-2 border-secondary h-100">
                                                <div class="card-body d-flex flex-column">
                                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                                        <div>
                                                            <div class="small text-muted">Documento #${remoteDoc.id}</div>
                                                            <h6 class="card-title mb-0">
                                                                ${remoteDoc.title != null ? remoteDoc.title : 'Documento sin título'}
                                                            </h6>
                                                        </div>
                                                        <span class="badge bg-secondary">
                                                            ${remoteDoc.documentTypeDisplayName != null ? remoteDoc.documentTypeDisplayName : remoteDoc.documentType}
                                                        </span>
                                                    </div>
                                                    <div class="mb-2">
                                                        <c:if test="${not empty remoteDoc.status}">
                                                            <span class="badge bg-light text-muted ms-1">${remoteDoc.status}</span>
                                                        </c:if>
                                                    </div>
                                                    <p class="card-text text-muted small mb-2">
                                                        <i class="fas fa-hospital me-1"></i>
                                                        ${remoteDoc.clinicName != null ? remoteDoc.clinicName : remoteDoc.clinicId}
                                                    </p>
                                                    <div class="row small text-muted mb-2">
                                                        <div class="col-6">
                                                            <i class="fas fa-calendar me-1"></i>
                                                            <c:choose>
                                                                <c:when test="${not empty remoteDoc.createdAtDate}">
                                                                    <fmt:formatDate value="${remoteDoc.createdAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                                </c:when>
                                                                <c:otherwise>N/A</c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                        <div class="col-6">
                                                            <i class="fas fa-user-md me-1"></i>
                                                            ${remoteDoc.professionalName != null ? remoteDoc.professionalName : 'N/A'}
                                                        </div>
                                                        <div class="col-12 mt-1">
                                                            <i class="fas fa-fingerprint me-1"></i>
                                                            <span class="text-truncate d-inline-block" style="max-width: 100%;">
                                                                ${remoteDoc.documentHash != null ? remoteDoc.documentHash : 'Sin hash'}
                                                            </span>
                                                        </div>
                                                    </div>
                                                    <div class="mt-auto w-100">
                                                        <c:choose>
                                                            <c:when test="${remoteDoc.hasContent}">
                                                                <div class="btn-group w-100" role="group">
                                                                    <button type="button" class="btn btn-sm btn-outline-secondary w-100"
                                                                            onclick="viewExternalDocument(${remoteDoc.id})">
                                                                        <i class="fas fa-eye me-1"></i>Ver
                                                                    </button>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="alert alert-warning py-2 small" role="alert">
                                                                    <i class="fas fa-lock me-1"></i>Este documento requiere solicitar acceso al HCEN.
                                                                </div>
                                                                <button type="button" class="btn btn-sm btn-outline-primary w-100"
                                                                        onclick="openRequestAccessModal(${remoteDoc.id})">
                                                                    <i class="fas fa-paper-plane me-1"></i>Solicitar acceso
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- Lista de Documentos -->
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty documents}">
                                <div class="alert alert-info text-center" role="alert">
                                    <i class="fas fa-file-medical fa-3x mb-3 d-block"></i>
                                    <h5>No hay documentos registrados para este paciente</h5>
                                    <p class="mb-0">Puede registrar un nuevo documento usando el botón de arriba.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="row">
                                    <c:forEach var="doc" items="${documents}">
                                        <div class="col-md-6 mb-3">
                                            <div class="card document-card">
                                                <div class="card-body">
                                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                                        <h6 class="card-title mb-0">${doc.title}</h6>
                                                        <span class="badge bg-info">${doc.documentType}</span>
                                                    </div>
                                                    <p class="card-text text-muted small mb-2">
                                                        <c:if test="${not empty doc.description}">
                                                            ${fn:substring(doc.description, 0, 100)}${fn:length(doc.description) > 100 ? '...' : ''}
                                                        </c:if>
                                                    </p>
                                                    <div class="row small text-muted mb-2">
                                                        <div class="col-6">
                                                            <i class="fas fa-calendar me-1"></i>
                                                            <%
                                                                uy.gub.clinic.entity.ClinicalDocument docItem = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                                                if (docItem != null && docItem.getDateOfVisit() != null) {
                                                                    java.util.Date visitDate = java.sql.Date.valueOf(docItem.getDateOfVisit());
                                                                    pageContext.setAttribute("docVisitDate", visitDate);
                                                            %>
                                                                <fmt:formatDate value="${docVisitDate}" pattern="dd/MM/yyyy"/>
                                                            <%
                                                                }
                                                            %>
                                                        </div>
                                                        <div class="col-6">
                                                            <i class="fas fa-user-md me-1"></i>${doc.professional.fullName}
                                                        </div>
                                                        <div class="col-6">
                                                            <i class="fas fa-stethoscope me-1"></i>${doc.specialty.name}
                                                        </div>
                                                    </div>
                                                    <div class="btn-group w-100" role="group">
                                                        <button type="button" class="btn btn-sm btn-outline-primary" 
                                                                onclick="viewDocument(${doc.id})">
                                                            <i class="fas fa-eye me-1"></i>Ver
                                                        </button>
                                                        <button type="button" class="btn btn-sm btn-outline-warning" 
                                                                onclick="editDocument(${doc.id})">
                                                            <i class="fas fa-edit me-1"></i>Editar
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <!-- Modal Ver Documento -->
    <div class="modal fade" id="viewDocumentModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class="fas fa-file-medical me-2"></i>Ver Documento Clínico</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="viewDocumentContent" style="max-height: 70vh; overflow-y: auto;">
                    <c:if test="${not empty selectedDocument}">
                        <c:set var="doc" value="${selectedDocument}"/>
                        
                        <!-- Información Básica -->
                        <div class="card mb-3 border-primary">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="fas fa-info-circle me-2"></i>Información Básica</h6>
                            </div>
                            <div class="card-body">
                                <div class="row mb-2">
                                    <div class="col-md-6">
                                        <strong><i class="fas fa-user me-2"></i>Paciente:</strong> 
                                        <span class="text-muted">${doc.patient.fullName}</span>
                                    </div>
                                    <div class="col-md-6">
                                        <strong><i class="fas fa-user-md me-2"></i>Profesional:</strong> 
                                        <span class="text-muted">${doc.professional.fullName}</span>
                                    </div>
                                </div>
                                <div class="row mb-2">
                                    <div class="col-md-6">
                                        <strong><i class="fas fa-stethoscope me-2"></i>Especialidad:</strong> 
                                        <span class="text-muted">${doc.specialty.name}</span>
                                    </div>
                                    <div class="col-md-6">
                                        <strong><i class="fas fa-tag me-2"></i>Tipo:</strong> 
                                        <span class="badge bg-info">${doc.documentType}</span>
                                    </div>
                                </div>
                                <div class="row mb-2">
                                    <div class="col-md-6">
                                        <strong><i class="fas fa-calendar me-2"></i>Fecha de Visita:</strong>
                                        <%
                                            uy.gub.clinic.entity.ClinicalDocument docView = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                            if (docView != null && docView.getDateOfVisit() != null) {
                                                java.util.Date date = java.sql.Date.valueOf(docView.getDateOfVisit());
                                                pageContext.setAttribute("visitDateView", date);
                                        %>
                                            <span class="text-muted"><fmt:formatDate value="${visitDateView}" pattern="dd/MM/yyyy"/></span>
                                        <%
                                            }
                                        %>
                                    </div>
                                    <div class="col-md-6">
                                        <strong><i class="fas fa-calendar-check me-2"></i>Próxima Cita:</strong>
                                        <%
                                            if (docView != null && docView.getNextAppointment() != null) {
                                                java.util.Date nextAppt = java.sql.Date.valueOf(docView.getNextAppointment());
                                                pageContext.setAttribute("nextApptView", nextAppt);
                                        %>
                                            <span class="text-muted"><fmt:formatDate value="${nextApptView}" pattern="dd/MM/yyyy"/></span>
                                        <%
                                            } else {
                                        %>
                                            <span class="text-muted">No programada</span>
                                        <%
                                            }
                                        %>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-12 mb-2">
                                        <strong><i class="fas fa-heading me-2"></i>Título:</strong> 
                                        <span class="text-muted">${doc.title}</span>
                                    </div>
                                </div>
                                <c:if test="${not empty doc.description}">
                                    <div class="row">
                                        <div class="col-md-12">
                                            <strong><i class="fas fa-align-left me-2"></i>Descripción:</strong>
                                            <p class="text-muted mb-0">${doc.description}</p>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                        
                        <!-- Datos Clínicos -->
                        <c:if test="${not empty doc.chiefComplaint || not empty doc.currentIllness}">
                            <div class="card mb-3 border-info">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0"><i class="fas fa-stethoscope me-2"></i>Datos Clínicos</h6>
                                </div>
                                <div class="card-body">
                                    <c:if test="${not empty doc.chiefComplaint}">
                                        <div class="mb-3">
                                            <strong>Motivo de Consulta:</strong>
                                            <p class="text-muted mb-0">${doc.chiefComplaint}</p>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty doc.currentIllness}">
                                        <div class="mb-0">
                                            <strong>Historia de la Enfermedad Actual:</strong>
                                            <p class="text-muted mb-0">${doc.currentIllness}</p>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                        
                        <!-- Signos Vitales -->
                        <c:if test="${not empty doc.vitalSigns}">
                            <div class="card mb-3 border-success">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0"><i class="fas fa-heartbeat me-2"></i>Signos Vitales</h6>
                                </div>
                                <div class="card-body">
                                    <%
                                        uy.gub.clinic.entity.ClinicalDocument docViewVital = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                        if (docViewVital != null && docViewVital.getVitalSigns() != null && !docViewVital.getVitalSigns().isEmpty()) {
                                            try {
                                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                                com.fasterxml.jackson.databind.JsonNode vitalSigns = mapper.readTree(docViewVital.getVitalSigns());
                                                pageContext.setAttribute("vitalSignsObj", vitalSigns);
                                    %>
                                        <div class="row g-3">
                                            <%
                                                com.fasterxml.jackson.databind.JsonNode vitalSignsNode = (com.fasterxml.jackson.databind.JsonNode) pageContext.getAttribute("vitalSignsObj");
                                                if (vitalSignsNode != null) {
                                                    if (vitalSignsNode.has("pressure")) {
                                            %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Presión Arterial</small>
                                                    <strong><%= vitalSignsNode.get("pressure").asText() %></strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("temperature")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Temperatura</small>
                                                    <strong><%= vitalSignsNode.get("temperature").asText() %> °C</strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("pulse")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Pulso</small>
                                                    <strong><%= vitalSignsNode.get("pulse").asText() %> bpm</strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("respiratoryRate")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Frecuencia Respiratoria</small>
                                                    <strong><%= vitalSignsNode.get("respiratoryRate").asText() %></strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("o2Saturation")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Saturación O2</small>
                                                    <strong><%= vitalSignsNode.get("o2Saturation").asText() %>%</strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("weight")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Peso</small>
                                                    <strong><%= vitalSignsNode.get("weight").asText() %> kg</strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("height")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">Altura</small>
                                                    <strong><%= vitalSignsNode.get("height").asText() %> cm</strong>
                                                </div>
                                            </div>
                                            <%  } if (vitalSignsNode.has("bmi")) { %>
                                            <div class="col-md-3">
                                                <div class="p-2 bg-light rounded">
                                                    <small class="text-muted d-block">IMC</small>
                                                    <strong><%= vitalSignsNode.get("bmi").asText() %></strong>
                                                </div>
                                            </div>
                                            <%
                                                    }
                                                }
                                            %>
                                        </div>
                                    <%
                                            } catch (Exception e) {
                                    %>
                                        <p class="text-muted">${doc.vitalSigns}</p>
                                    <%
                                            }
                                        }
                                    %>
                                </div>
                            </div>
                        </c:if>
                        
                        <!-- Examen y Diagnóstico -->
                        <c:if test="${not empty doc.physicalExamination || not empty doc.diagnosis || not empty doc.treatment}">
                            <div class="card mb-3 border-warning">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0"><i class="fas fa-clipboard-check me-2"></i>Examen y Diagnóstico</h6>
                                </div>
                                <div class="card-body">
                                    <c:if test="${not empty doc.physicalExamination}">
                                        <div class="mb-3">
                                            <strong>Examen Físico:</strong>
                                            <p class="text-muted mb-0">${doc.physicalExamination}</p>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty doc.diagnosis}">
                                        <div class="mb-3">
                                            <strong>Diagnóstico:</strong>
                                            <p class="text-muted mb-0">${doc.diagnosis}</p>
                                        </div>
                                    </c:if>
                                    <c:if test="${not empty doc.treatment}">
                                        <div class="mb-0">
                                            <strong>Tratamiento/Indicaciones:</strong>
                                            <p class="text-muted mb-0">${doc.treatment}</p>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                        
                        <!-- Prescripciones -->
                        <c:if test="${not empty doc.prescriptions}">
                            <div class="card mb-3 border-danger">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0"><i class="fas fa-pills me-2"></i>Prescripciones</h6>
                                </div>
                                <div class="card-body">
                                    <%
                                        uy.gub.clinic.entity.ClinicalDocument docViewPresc = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                        if (docViewPresc != null && docViewPresc.getPrescriptions() != null && !docViewPresc.getPrescriptions().isEmpty()) {
                                            try {
                                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                                com.fasterxml.jackson.databind.JsonNode prescriptions = mapper.readTree(docViewPresc.getPrescriptions());
                                                pageContext.setAttribute("prescriptionsArray", prescriptions);
                                    %>
                                        <div class="table-responsive">
                                            <table class="table table-sm table-hover">
                                                <thead class="table-light">
                                                    <tr>
                                                        <th>#</th>
                                                        <th>Medicamento</th>
                                                        <th>Dosis</th>
                                                        <th>Frecuencia</th>
                                                        <th>Duración</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <%
                                                        com.fasterxml.jackson.databind.JsonNode prescriptionsNode = (com.fasterxml.jackson.databind.JsonNode) pageContext.getAttribute("prescriptionsArray");
                                                        if (prescriptionsNode != null && prescriptionsNode.isArray()) {
                                                            for (int i = 0; i < prescriptionsNode.size(); i++) {
                                                                com.fasterxml.jackson.databind.JsonNode prescription = prescriptionsNode.get(i);
                                                    %>
                                                    <tr>
                                                        <td><%= i + 1 %></td>
                                                        <td><%= prescription.has("medication") && !prescription.get("medication").asText().isEmpty() ? prescription.get("medication").asText() : "-" %></td>
                                                        <td><%= prescription.has("dosage") && !prescription.get("dosage").asText().isEmpty() ? prescription.get("dosage").asText() : "-" %></td>
                                                        <td><%= prescription.has("frequency") && !prescription.get("frequency").asText().isEmpty() ? prescription.get("frequency").asText() : "-" %></td>
                                                        <td><%= prescription.has("duration") && !prescription.get("duration").asText().isEmpty() ? prescription.get("duration").asText() : "-" %></td>
                                                    </tr>
                                                    <%
                                                            }
                                                        }
                                                    %>
                                                </tbody>
                                            </table>
                                        </div>
                                    <%
                                            } catch (Exception e) {
                                    %>
                                        <p class="text-muted">${doc.prescriptions}</p>
                                    <%
                                            }
                                        }
                                    %>
                                </div>
                            </div>
                        </c:if>
                        
                        <!-- Observaciones -->
                        <c:if test="${not empty doc.observations}">
                            <div class="card mb-3 border-secondary">
                                <div class="card-header bg-light">
                                    <h6 class="mb-0"><i class="fas fa-comments me-2"></i>Observaciones</h6>
                                </div>
                                <div class="card-body">
                                    <p class="text-muted mb-0">${doc.observations}</p>
                                </div>
                            </div>
                        </c:if>
                        
                        <!-- Archivos Adjuntos -->
                        <%
                            uy.gub.clinic.entity.ClinicalDocument docViewAtt = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                            if (docViewAtt != null && docViewAtt.getAttachments() != null && !docViewAtt.getAttachments().isEmpty()) {
                                try {
                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    com.fasterxml.jackson.databind.JsonNode attachments = mapper.readTree(docViewAtt.getAttachments());
                                    if (attachments.isArray() && attachments.size() > 0) {
                                        pageContext.setAttribute("attachmentsArray", attachments);
                        %>
                        <div class="card mb-3 border-info">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="fas fa-paperclip me-2"></i>Archivos Adjuntos</h6>
                            </div>
                            <div class="card-body">
                                <div class="list-group">
                                    <%
                                        com.fasterxml.jackson.databind.JsonNode attachmentsNode = (com.fasterxml.jackson.databind.JsonNode) pageContext.getAttribute("attachmentsArray");
                                        if (attachmentsNode != null && attachmentsNode.isArray()) {
                                            for (int i = 0; i < attachmentsNode.size(); i++) {
                                                com.fasterxml.jackson.databind.JsonNode attachment = attachmentsNode.get(i);
                                                String fileName = attachment.has("fileName") ? attachment.get("fileName").asText() : "Archivo " + (i + 1);
                                                String mimeType = attachment.has("mimeType") ? attachment.get("mimeType").asText() : "";
                                                String iconClass = "fas fa-file";
                                                
                                                if (mimeType.contains("pdf")) {
                                                    iconClass = "fas fa-file-pdf text-danger";
                                                } else if (mimeType.contains("image")) {
                                                    iconClass = "fas fa-file-image text-info";
                                                } else if (mimeType.contains("word") || mimeType.contains("document")) {
                                                    iconClass = "fas fa-file-word text-primary";
                                                }
                                                
                                                Long docId = docViewAtt.getId();
                                                String downloadUrl = request.getContextPath() + "/admin/documents?action=download&id=" + docId + "&fileIndex=" + i;
                                    %>
                                    <div class="list-group-item d-flex justify-content-between align-items-center">
                                        <a href="<%= downloadUrl %>" class="text-decoration-none flex-grow-1" target="_blank">
                                            <i class="<%= iconClass %> me-2"></i><%= fileName %>
                                            <% if (attachment.has("fileSize")) { 
                                                long fileSize = attachment.get("fileSize").asLong();
                                                String sizeStr = "";
                                                if (fileSize < 1024) {
                                                    sizeStr = fileSize + " B";
                                                } else if (fileSize < 1024 * 1024) {
                                                    sizeStr = String.format("%.1f KB", fileSize / 1024.0);
                                                } else {
                                                    sizeStr = String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
                                                }
                                            %>
                                            <small class="text-muted ms-2"><%= sizeStr %></small>
                                            <% } %>
                                        </a>
                                    </div>
                                    <%
                                            }
                                        }
                                    %>
                                </div>
                            </div>
                        </div>
                        <%
                                    }
                                } catch (Exception e) {
                                    // Error al parsear archivos
                                }
                            }
                        %>
                    </c:if>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal Ver Documento Externo -->
    <div class="modal fade" id="viewRemoteDocumentModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header bg-secondary text-white">
                    <h5 class="modal-title"><i class="fas fa-cloud me-2"></i>Documento disponible en HCEN</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
                    <!-- Mensaje de Acceso Denegado -->
                    <c:if test="${not empty accessDeniedError}">
                        <div class="alert alert-warning fade show" role="alert">
                            <div class="d-flex align-items-start mb-3">
                                <div class="flex-grow-1">
                                    <i class="fas fa-lock me-2"></i><strong>Acceso Denegado:</strong> ${accessDeniedError}
                                </div>
                            </div>
                            <button type="button" class="btn btn-primary btn-sm" onclick="requestAccessFromModal(${deniedDocumentId})" data-bs-dismiss="modal">
                                <i class="fas fa-paper-plane me-1"></i>Solicitar Acceso a este Documento
                            </button>
                        </div>
                    </c:if>

                    <c:if test="${not empty selectedRemoteDocument}">
                        <c:set var="remoteDetail" value="${selectedRemoteDocument}"/>
                        <div class="card mb-3 border-secondary">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="fas fa-info-circle me-2"></i>Metadatos del documento</h6>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6">
                                        <p><strong>Título:</strong> ${remoteDetail.title}</p>
                                        <p><strong>Tipo:</strong> ${remoteDetail.documentType}</p>
                                        <p><strong>Clínica:</strong> ${remoteDetail.clinicName}</p>
                                        <p><strong>Profesional:</strong> ${remoteDetail.professionalName}</p>
                                    </div>
                                    <div class="col-md-6">
                                        <p><strong>Estado:</strong> ${remoteDetail.status}</p>
                                        <p><strong>Hash:</strong> ${remoteDetail.documentHash}</p>
                                        <p><strong>Creado:</strong>
                                            <c:if test="${not empty remoteDetail.createdAt}">
                                                <%
                                                    uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO rd =
                                                        (uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO) pageContext.getAttribute("remoteDetail");
                                                    if (rd != null && rd.getCreatedAt() != null) {
                                                        java.util.Date createdAtDate = java.util.Date.from(rd.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
                                                        pageContext.setAttribute("remoteDetailCreatedAtDate", createdAtDate);
                                                %>
                                                    <fmt:formatDate value="${remoteDetailCreatedAtDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                <%
                                                    }
                                                %>
                                            </c:if>
                                        </p>
                                        <p><strong>Locator HCEN:</strong>
                                            <c:out value="${remoteDetail.documentLocator != null ? remoteDetail.documentLocator : 'N/A'}"/>
                                        </p>
                                    </div>
                                </div>
                                <c:if test="${not empty remoteDetail.description}">
                                    <div class="mt-3">
                                        <strong>Descripción:</strong>
                                        <p class="text-muted">${remoteDetail.description}</p>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <c:if test="${not empty remoteDocumentInlineContent}">
                            <!-- FHIR Document Display -->
                            <div id="fhirDocumentContainer">
                                <!-- Main FHIR Content -->
                                <div class="card mb-3 border-primary">
                                    <div class="card-body" id="fhirMainContent">
                                        <p class="text-center text-muted">Cargando documento FHIR...</p>
                                    </div>
                                </div>

                                <!-- Raw FHIR JSON (collapsible) -->
                                <div class="card mb-3 border-secondary">
                                    <div class="card-header bg-light">
                                        <h6 class="mb-0">
                                            <a class="text-decoration-none text-dark" data-bs-toggle="collapse" href="#rawFhirJson">
                                                <i class="fas fa-code me-2"></i>Ver JSON FHIR completo
                                                <i class="fas fa-chevron-down float-end"></i>
                                            </a>
                                        </h6>
                                    </div>
                                    <div class="collapse" id="rawFhirJson">
                                        <div class="card-body">
                                            <pre class="bg-light p-3 rounded" style="white-space: pre-wrap; max-height: 400px; overflow-y: auto;"><c:out value="${remoteDocumentInlineContent}"/></pre>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <style>
                                .fhir-document {
                                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                                }
                                .fhir-header {
                                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                    color: white;
                                    padding: 20px;
                                    border-radius: 8px;
                                    margin-bottom: 20px;
                                }
                                .fhir-header h4 {
                                    margin: 0 0 10px 0;
                                    font-weight: 600;
                                }
                                .fhir-sections {
                                    margin-top: 20px;
                                }
                                .fhir-section {
                                    background: #f8f9fa;
                                    border-left: 4px solid #667eea;
                                    padding: 15px;
                                    margin-bottom: 15px;
                                    border-radius: 4px;
                                }
                                .fhir-section h5 {
                                    color: #667eea;
                                    margin: 0 0 10px 0;
                                    font-weight: 600;
                                    font-size: 1.1rem;
                                }
                                .fhir-narrative {
                                    line-height: 1.6;
                                    color: #333;
                                }
                                .fhir-narrative div {
                                    margin: 0;
                                }
                            </style>

                            <script>
                            // Parse and display FHIR Document
                            (function() {
                                try {
                                    var fhirDoc = <c:out value="${remoteDocumentInlineContent}" escapeXml="false"/>;

                                    if (!fhirDoc) {
                                        showError('No se pudo cargar el documento FHIR.');
                                        return;
                                    }

                                    // Validate FHIR format
                                    if (!fhirDoc.resourceType) {
                                        throw new Error('Formato FHIR inválido: falta resourceType');
                                    }

                                    displayFhirDocument(fhirDoc);

                                } catch(error) {
                                    console.error('Error parsing FHIR JSON:', error);
                                    document.getElementById('fhirMainContent').innerHTML =
                                        '<p class="text-danger">Error al cargar el documento FHIR: ' +
                                        escapeHtml(error.message) + '</p>';
                                }

                                /**
                                 * Display FHIR document in modal
                                 */
                                function displayFhirDocument(fhirDoc) {
                                    var content = '';

                                    if (fhirDoc.resourceType === 'Bundle' && fhirDoc.type === 'document') {
                                        // Parse FHIR Bundle - look for Composition
                                        var composition = null;
                                        if (fhirDoc.entry) {
                                            for (var i = 0; i < fhirDoc.entry.length; i++) {
                                                if (fhirDoc.entry[i].resource &&
                                                    fhirDoc.entry[i].resource.resourceType === 'Composition') {
                                                    composition = fhirDoc.entry[i].resource;
                                                    break;
                                                }
                                            }
                                        }

                                        if (composition) {
                                            content = renderComposition(composition);
                                        } else {
                                            content = '<p class="text-muted">No se pudo extraer el contenido del documento FHIR.</p>';
                                        }

                                    } else if (fhirDoc.resourceType === 'DocumentReference') {
                                        // Parse DocumentReference
                                        content = renderDocumentReference(fhirDoc);

                                    } else {
                                        content = '<p class="text-muted">Formato FHIR no reconocido.</p>';
                                    }

                                    // Display in container
                                    document.getElementById('fhirMainContent').innerHTML = content;
                                }

                                /**
                                 * Render FHIR Composition
                                 */
                                function renderComposition(composition) {
                                    var html = '<div class="fhir-document"><div class="fhir-header">';

                                    html += '<h4>' + escapeHtml(composition.title || 'Documento Clínico') + '</h4>';

                                    if (composition.date) {
                                        html += '<p style="margin-bottom: 5px;">📅 ' + formatFhirDate(composition.date) + '</p>';
                                    }

                                    if (composition.author && composition.author.length > 0) {
                                        var authors = composition.author.map(function(a) {
                                            return escapeHtml(a.display || a.reference || '');
                                        }).join(', ');
                                        html += '<p style="font-size: 14px; margin-bottom: 5px;"><strong>Autor:</strong> ' +
                                                authors + '</p>';
                                    }

                                    if (composition.subject) {
                                        html += '<p style="font-size: 14px;"><strong>Paciente:</strong> ' +
                                                escapeHtml(composition.subject.display || composition.subject.reference || '') + '</p>';
                                    }

                                    html += '</div><hr style="border: 1px solid #e0e6ed; margin: 20px 0;">';

                                    // Render sections
                                    if (composition.section && composition.section.length > 0) {
                                        html += '<div class="fhir-sections">';
                                        for (var i = 0; i < composition.section.length; i++) {
                                            html += renderSection(composition.section[i], 0);
                                        }
                                        html += '</div>';
                                    } else {
                                        html += '<p class="text-muted">No se encontró contenido en las secciones del documento.</p>';
                                    }

                                    html += '</div>';
                                    return html;
                                }

                                /**
                                 * Render FHIR section
                                 */
                                function renderSection(section, level) {
                                    var marginLeft = level * 10;
                                    var html = '<div class="fhir-section" style="margin-left: ' + marginLeft + 'px;">';

                                    html += '<h5>' + escapeHtml(section.title || 'Sección') + '</h5>';

                                    if (section.text && section.text.div) {
                                        // FHIR narrative (HTML content) - display as is (already sanitized by FHIR)
                                        html += '<div class="fhir-narrative">' + section.text.div + '</div>';
                                    } else if (section.code && section.code.text) {
                                        html += '<p>' + escapeHtml(section.code.text) + '</p>';
                                    } else {
                                        html += '<p style="color: #7f8c8d; font-style: italic;">Sin contenido disponible</p>';
                                    }

                                    // Render nested sections
                                    if (section.section && section.section.length > 0) {
                                        for (var i = 0; i < section.section.length; i++) {
                                            html += renderSection(section.section[i], level + 1);
                                        }
                                    }

                                    html += '</div>';
                                    return html;
                                }

                                /**
                                 * Render FHIR DocumentReference
                                 */
                                function renderDocumentReference(docRef) {
                                    var html = '<div class="fhir-document"><div class="fhir-header">';

                                    var title = 'Documento Clínico';
                                    if (docRef.type && docRef.type.coding && docRef.type.coding[0]) {
                                        title = docRef.type.coding[0].display || title;
                                    }
                                    html += '<h4>' + escapeHtml(title) + '</h4>';

                                    if (docRef.date) {
                                        html += '<p style="margin-bottom: 5px;">📅 ' + formatFhirDate(docRef.date) + '</p>';
                                    }

                                    if (docRef.author && docRef.author.length > 0) {
                                        var authors = docRef.author.map(function(a) {
                                            return escapeHtml(a.display || a.reference || '');
                                        }).join(', ');
                                        html += '<p style="font-size: 14px; margin-bottom: 5px;"><strong>Autor:</strong> ' +
                                                authors + '</p>';
                                    }

                                    if (docRef.subject) {
                                        html += '<p style="font-size: 14px; margin-bottom: 5px;"><strong>Paciente:</strong> ' +
                                                escapeHtml(docRef.subject.display || docRef.subject.reference || '') + '</p>';
                                    }

                                    if (docRef.custodian) {
                                        html += '<p style="font-size: 14px;"><strong>Institución:</strong> ' +
                                                escapeHtml(docRef.custodian.display || docRef.custodian.reference || '') + '</p>';
                                    }

                                    html += '</div><hr style="border: 1px solid #e0e6ed; margin: 20px 0;">';

                                    if (docRef.description) {
                                        html += '<p style="margin-bottom: 15px;"><strong>Descripción:</strong> ' +
                                                escapeHtml(docRef.description) + '</p>';
                                    }

                                    if (docRef.status) {
                                        html += '<p style="margin-bottom: 15px;"><strong>Estado:</strong> ' +
                                                escapeHtml(docRef.status) + '</p>';
                                    }

                                    html += '</div>';
                                    return html;
                                }

                                /**
                                 * Format FHIR date for display
                                 */
                                function formatFhirDate(dateString) {
                                    if (!dateString) return 'N/A';
                                    try {
                                        var date = new Date(dateString);
                                        return date.toLocaleDateString('es-UY', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric',
                                            hour: '2-digit',
                                            minute: '2-digit'
                                        });
                                    } catch (e) {
                                        return dateString;
                                    }
                                }

                                /**
                                 * Escape HTML to prevent XSS
                                 */
                                function escapeHtml(text) {
                                    if (!text) return '';
                                    var div = document.createElement('div');
                                    div.textContent = text;
                                    return div.innerHTML;
                                }

                                function showError(message) {
                                    document.getElementById('fhirMainContent').innerHTML =
                                        '<p class="text-danger">' + escapeHtml(message) + '</p>';
                                }
                            })();
                            </script>
                        </c:if>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal Nuevo/Editar Documento -->
    <div class="modal fade" id="addDocumentModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="modalTitle">Nuevo Documento Clínico</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form id="documentForm" method="POST" action="<c:url value='/professional/patient-documents'/>" enctype="multipart/form-data">
                    <input type="hidden" name="action" id="formAction" value="create">
                    <input type="hidden" name="documentId" id="documentId">
                    <input type="hidden" name="patientId" id="formPatientId" value="${patient.id}">
                    
                    <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
                        <style>
                            .form-section {
                                background: #f8f9fa;
                                border-radius: 10px;
                                padding: 20px;
                                margin-bottom: 20px;
                            }
                            .form-section h5 {
                                color: #4CAF50;
                                margin-bottom: 15px;
                                border-bottom: 2px solid #4CAF50;
                                padding-bottom: 10px;
                            }
                        </style>
                        
                        <!-- Sección 1: Información Básica -->
                        <div class="form-section">
                            <h5><i class="fas fa-info-circle me-2"></i>Información Básica</h5>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Paciente *</label>
                                    <select class="form-select" name="patientId" id="patientId" required>
                                        <option value="${patient.id}" selected>${patient.fullName}</option>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Profesional *</label>
                                    <select class="form-select" name="professionalId" id="professionalId" required>
                                        <option value="">Seleccionar...</option>
                                        <c:forEach var="professional" items="${professionals}">
                                            <option value="${professional.id}" data-specialty="${professional.specialty.id}" 
                                                ${loggedProfessionalId != null && loggedProfessionalId == professional.id ? 'selected' : ''}>
                                                ${professional.fullName}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Especialidad *</label>
                                    <select class="form-select" name="specialtyId" id="specialtyId" required>
                                        <option value="">Seleccionar...</option>
                                        <c:forEach var="specialty" items="${specialties}">
                                            <option value="${specialty.id}">${specialty.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Tipo de Documento *</label>
                                    <select class="form-select" name="documentType" id="documentTypeSelect" required>
                                        <option value="">Seleccionar...</option>
                                        <option value="CONSULTATION">Consulta</option>
                                        <option value="DIAGNOSTIC_REPORT">Diagnóstico</option>
                                        <option value="TREATMENT_PLAN">Tratamiento</option>
                                        <option value="PROGRESS_NOTE">Evolución</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Fecha de Consulta *</label>
                                    <input type="date" class="form-control" name="dateOfVisit" required>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Título *</label>
                                    <input type="text" class="form-control" name="title" required>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Descripción</label>
                                    <textarea class="form-control" name="description" rows="2"></textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Sección 2: Datos Clínicos -->
                        <div class="form-section">
                            <h5><i class="fas fa-stethoscope me-2"></i>Datos Clínicos</h5>
                            <div class="row g-3">
                                <div class="col-md-12">
                                    <label class="form-label">Motivo de Consulta</label>
                                    <textarea class="form-control" name="chiefComplaint" rows="3"></textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Historia de la Enfermedad Actual</label>
                                    <textarea class="form-control" name="currentIllness" rows="3"></textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Sección 3: Signos Vitales -->
                        <div class="form-section">
                            <h5><i class="fas fa-heartbeat me-2"></i>Signos Vitales</h5>
                            <div class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label">Presión Arterial</label>
                                    <input type="text" class="form-control" name="vitalPressure" placeholder="120/80">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Temperatura (°C)</label>
                                    <input type="number" step="0.1" class="form-control" name="vitalTemperature" placeholder="36.5">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Pulso (bpm)</label>
                                    <input type="number" class="form-control" name="vitalPulse" placeholder="72">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Frecuencia Respiratoria</label>
                                    <input type="number" class="form-control" name="vitalRespiratoryRate" placeholder="16">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Saturación O2 (%)</label>
                                    <input type="number" class="form-control" name="vitalO2Saturation" placeholder="98">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Peso (kg)</label>
                                    <input type="number" step="0.1" class="form-control" name="vitalWeight" placeholder="70">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Altura (cm)</label>
                                    <input type="number" step="0.1" class="form-control" name="vitalHeight" placeholder="170">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">IMC</label>
                                    <input type="text" class="form-control" id="calculatedBMI" readonly>
                                </div>
                            </div>
                        </div>

                        <!-- Sección 4: Examen y Diagnóstico -->
                        <div class="form-section">
                            <h5><i class="fas fa-clipboard-check me-2"></i>Examen y Diagnóstico</h5>
                            <div class="row g-3">
                                <div class="col-md-12">
                                    <label class="form-label">Examen Físico</label>
                                    <textarea class="form-control" name="physicalExamination" rows="4"></textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Diagnóstico</label>
                                    <textarea class="form-control" name="diagnosis" rows="3"></textarea>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label">Tratamiento/Indicaciones</label>
                                    <textarea class="form-control" name="treatment" rows="3"></textarea>
                                </div>
                            </div>
                        </div>

                        <!-- Sección 5: Prescripciones -->
                        <div class="form-section">
                            <h5><i class="fas fa-pills me-2"></i>Prescripciones</h5>
                            <div id="prescriptionsContainer">
                                <div class="prescription-item row g-3 mb-2">
                                    <div class="col-md-4">
                                        <input type="text" class="form-control" name="prescription_medication_0" placeholder="Medicamento">
                                    </div>
                                    <div class="col-md-2">
                                        <input type="text" class="form-control" name="prescription_dosage_0" placeholder="Dosis">
                                    </div>
                                    <div class="col-md-2">
                                        <input type="text" class="form-control" name="prescription_frequency_0" placeholder="Frecuencia">
                                    </div>
                                    <div class="col-md-2">
                                        <input type="text" class="form-control" name="prescription_duration_0" placeholder="Duración">
                                    </div>
                                    <div class="col-md-2">
                                        <button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)">
                                            <i class="fas fa-times"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <button type="button" class="btn btn-sm btn-secondary" onclick="addPrescription()">
                                <i class="fas fa-plus me-2"></i>Agregar Prescripción
                            </button>
                        </div>

                        <!-- Sección 6: Observaciones y Próxima Cita -->
                        <div class="form-section">
                            <h5><i class="fas fa-comments me-2"></i>Observaciones y Seguimiento</h5>
                            <div class="row g-3">
                                <div class="col-md-12">
                                    <label class="form-label">Observaciones</label>
                                    <textarea class="form-control" name="observations" rows="3"></textarea>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Próxima Cita</label>
                                    <input type="date" class="form-control" name="nextAppointment">
                                </div>
                            </div>
                        </div>

                        <!-- Sección 7: Archivos Adjuntos -->
                        <div class="form-section">
                            <h5><i class="fas fa-paperclip me-2"></i>Archivos Adjuntos</h5>
                            <div id="existingAttachments" class="mb-3">
                                <!-- Los archivos existentes se mostrarán aquí cuando se esté editando -->
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Agregar Nuevos Archivos</label>
                                <input type="file" class="form-control" name="attachments" id="attachmentsInput" multiple accept=".pdf,.jpg,.jpeg,.png,.doc,.docx">
                                <small class="form-text text-muted">Máximo 10MB por archivo. Formatos permitidos: PDF, imágenes, documentos</small>
                            </div>
                            <div id="attachmentsPreview"></div>
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Guardar Documento
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal Solicitar Acceso HCEN -->
    <div class="modal fade" id="requestAccessModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-exchange-alt me-2"></i>Solicitar Acceso a Documentos HCEN
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form id="requestAccessForm" method="POST" action="<c:url value='/professional/patient-documents'/>">
                    <input type="hidden" name="action" value="requestAccess">
                    <input type="hidden" name="patientId" value="${patient.id}">
                    <input type="hidden" name="patientCI" value="${patient.documentNumber}">
                    
                    <div class="modal-body">
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle me-2"></i>
                            Solicita acceso a documentos del paciente <strong>${patient.fullName}</strong> (CI: ${patient.documentNumber}) 
                            en otras clínicas a través del HCEN. Se solicitará acceso a <strong>todas las especialidades</strong>.
                        </div>
                        
                        <!-- Campo hidden: siempre se solicita acceso a todas las especialidades -->
                        <input type="hidden" name="specialtySelection" value="ALL">
                        
                        <div class="mb-3">
                            <label class="form-label">Motivo de la Solicitud *</label>
                            <textarea class="form-control" name="requestReason" rows="4" 
                                      placeholder="Describa el motivo por el cual necesita acceso a estos documentos (ej: continuidad de tratamiento, referencia, emergencia, etc.)" 
                                      required></textarea>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">Urgencia *</label>
                            <select class="form-select" name="urgency" required>
                                <option value="ROUTINE" selected>Rutina</option>
                                <option value="URGENT">Urgente</option>
                                <option value="EMERGENCY">Emergencia</option>
                            </select>
                            <small class="text-muted">Seleccione el nivel de urgencia de la solicitud.</small>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">Documento Específico (Opcional)</label>
                            <div class="d-flex gap-2">
                                <input type="number" class="form-control" name="documentId" id="documentIdInput"
                                       placeholder="ID del documento en HCEN">
                                <button type="button" class="btn btn-outline-secondary btn-sm" id="clearDocumentSelectionBtn"
                                        style="display: none;" onclick="clearDocumentSelection()" title="Limpiar selección de documento específico">
                                    Limpiar
                                </button>
                            </div>
                            <small class="text-muted d-block" id="documentIdHelp">
                                Complete este campo solo si quiere solicitar acceso a un documento puntual. De lo contrario, déjelo vacío.
                            </small>
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-paper-plane me-2"></i>Enviar Solicitud
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <%-- Generar variables JavaScript para controlar la apertura de modales --%>
    <%
        uy.gub.clinic.entity.ClinicalDocument selectedDoc = (uy.gub.clinic.entity.ClinicalDocument) request.getAttribute("selectedDocument");
        uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO selectedRemoteDoc =
                (uy.gub.clinic.integration.dto.hcen.HcenDocumentDetailDTO) request.getAttribute("selectedRemoteDocument");
        Boolean viewDocument = (Boolean) request.getAttribute("viewDocument");
        Boolean editDocument = (Boolean) request.getAttribute("editDocument");
        Boolean viewRemoteDocument = (Boolean) request.getAttribute("viewRemoteDocument");
        boolean hasSelectedDoc = selectedDoc != null;
        boolean hasSelectedRemoteDoc = selectedRemoteDoc != null;
        boolean isViewAction = viewDocument != null && viewDocument;
        boolean isEditAction = editDocument != null && editDocument;
        boolean isRemoteViewAction = viewRemoteDocument != null && viewRemoteDocument;
        
        java.util.function.Function<String, String> escapeJs = s -> {
            if (s == null) return "";
            return s.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("<", "\\u003C")
                    .replace(">", "\\u003E");
        };
    %>
    <script>
        var shouldOpenViewModal = <%= isViewAction && hasSelectedDoc %>;
        var shouldOpenEditModal = <%= isEditAction && hasSelectedDoc %>;
        var shouldOpenRemoteViewModal = <%= isRemoteViewAction && hasSelectedRemoteDoc %>;
        
        <% if (hasSelectedDoc && isEditAction && selectedDoc != null) { 
            String dateOfVisitStr = selectedDoc.getDateOfVisit() != null ? selectedDoc.getDateOfVisit().toString() : "";
            String nextAppointmentStr = selectedDoc.getNextAppointment() != null ? selectedDoc.getNextAppointment().toString() : "";
            String vitalSignsStr = selectedDoc.getVitalSigns() != null && !selectedDoc.getVitalSigns().isEmpty() ? 
                selectedDoc.getVitalSigns().replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") : "";
            String prescriptionsStr = selectedDoc.getPrescriptions() != null && !selectedDoc.getPrescriptions().isEmpty() ? 
                selectedDoc.getPrescriptions().replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") : "";
            String attachmentsStr = selectedDoc.getAttachments() != null && !selectedDoc.getAttachments().isEmpty() ? 
                selectedDoc.getAttachments().replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") : "";
        %>
        var editDocumentData = {
            id: '<%= selectedDoc.getId() %>',
            patientId: '<%= selectedDoc.getPatient() != null ? selectedDoc.getPatient().getId() : "" %>',
            professionalId: '<%= selectedDoc.getProfessional() != null ? selectedDoc.getProfessional().getId() : "" %>',
            specialtyId: '<%= selectedDoc.getSpecialty() != null ? selectedDoc.getSpecialty().getId() : "" %>',
            documentType: '<%= selectedDoc.getDocumentType() != null ? escapeJs.apply(selectedDoc.getDocumentType()) : "" %>',
            title: '<%= escapeJs.apply(selectedDoc.getTitle()) %>',
            description: '<%= escapeJs.apply(selectedDoc.getDescription()) %>',
            dateOfVisit: '<%= dateOfVisitStr %>',
            nextAppointment: '<%= nextAppointmentStr %>',
            chiefComplaint: '<%= escapeJs.apply(selectedDoc.getChiefComplaint()) %>',
            currentIllness: '<%= escapeJs.apply(selectedDoc.getCurrentIllness()) %>',
            physicalExamination: '<%= escapeJs.apply(selectedDoc.getPhysicalExamination()) %>',
            diagnosis: '<%= escapeJs.apply(selectedDoc.getDiagnosis()) %>',
            treatment: '<%= escapeJs.apply(selectedDoc.getTreatment()) %>',
            observations: '<%= escapeJs.apply(selectedDoc.getObservations()) %>',
            vitalSigns: <%= vitalSignsStr.isEmpty() ? "null" : "'" + vitalSignsStr + "'" %>,
            prescriptions: <%= prescriptionsStr.isEmpty() ? "null" : "'" + prescriptionsStr + "'" %>,
            attachments: <%= attachmentsStr.isEmpty() ? "null" : "'" + attachmentsStr + "'" %>
        };
        <% } %>
    </script>
    
    <script>
        function viewDocument(documentId) {
            window.location.href = '<c:url value="/professional/patient-documents"/>?patientId=${patient.id}&action=view&documentId=' + documentId;
        }

        function editDocument(documentId) {
            window.location.href = '<c:url value="/professional/patient-documents"/>?patientId=${patient.id}&action=edit&documentId=' + documentId;
        }

        function viewExternalDocument(documentId) {
            window.location.href = '<c:url value="/professional/patient-documents"/>?patientId=${patient.id}&action=viewRemote&remoteDocumentId=' + documentId;
        }

        function downloadExternalDocument(documentId) {
            window.open('<c:url value="/professional/patient-documents"/>?patientId=${patient.id}&action=downloadRemote&remoteDocumentId=' + documentId, '_blank');
        }

        function openRequestAccessModal(documentId) {
            const form = document.getElementById('requestAccessForm');
            if (form) {
                form.reset();
            }

            const documentIdInput = document.getElementById('documentIdInput');
            const clearButton = document.getElementById('clearDocumentSelectionBtn');
            if (documentIdInput) {
                if (documentId) {
                    documentIdInput.value = documentId;
                    documentIdInput.readOnly = true;
                    documentIdInput.classList.add('bg-light');
                    if (clearButton) {
                        clearButton.style.display = 'inline-block';
                    }
                    const help = document.getElementById('documentIdHelp');
                    if (help) {
                        help.textContent = 'Solicitando acceso al documento #' + documentId + ' publicado en HCEN.';
                    }
                } else {
                    documentIdInput.value = '';
                    documentIdInput.readOnly = false;
                    documentIdInput.classList.remove('bg-light');
                    if (clearButton) {
                        clearButton.style.display = 'none';
                    }
                    const help = document.getElementById('documentIdHelp');
                    if (help) {
                        help.textContent = 'Complete este campo solo si quiere solicitar acceso a un documento puntual. De lo contrario, déjelo vacío.';
                    }
                }
            }

            const modal = new bootstrap.Modal(document.getElementById('requestAccessModal'));
            modal.show();
        }

        function requestAccessFromModal(documentId) {
            // This function is called from the access denied alert in the view document modal
            // It opens the access request modal with the document ID pre-filled
            openRequestAccessModal(documentId);
        }

        function clearDocumentSelection() {
            const documentIdInput = document.getElementById('documentIdInput');
            if (documentIdInput) {
                documentIdInput.value = '';
                documentIdInput.readOnly = false;
                documentIdInput.classList.remove('bg-light');
            }
            const clearButton = document.getElementById('clearDocumentSelectionBtn');
            if (clearButton) {
                clearButton.style.display = 'none';
            }
            const help = document.getElementById('documentIdHelp');
            if (help) {
                help.textContent = 'Complete este campo solo si quiere solicitar acceso a un documento puntual. De lo contrario, déjelo vacío.';
            }
        }

        function openNewDocumentModal() {
            // Limpiar el formulario antes de abrir
            const form = document.getElementById('documentForm');
            const formAction = document.getElementById('formAction');
            const documentId = document.getElementById('documentId');
            const modalTitle = document.getElementById('modalTitle');
            
            if (form) form.reset();
            if (formAction) formAction.value = 'create';
            if (documentId) documentId.value = '';
            if (modalTitle) modalTitle.textContent = 'Nuevo Documento Clínico';
            
            // Limpiar prescripciones
            const prescriptionsContainer = document.getElementById('prescriptionsContainer');
            if (prescriptionsContainer) {
                prescriptionsContainer.innerHTML = `
                    <div class="prescription-item row g-3 mb-2">
                        <div class="col-md-4">
                            <input type="text" class="form-control" name="prescription_medication_0" placeholder="Medicamento">
                        </div>
                        <div class="col-md-2">
                            <input type="text" class="form-control" name="prescription_dosage_0" placeholder="Dosis">
                        </div>
                        <div class="col-md-2">
                            <input type="text" class="form-control" name="prescription_frequency_0" placeholder="Frecuencia">
                        </div>
                        <div class="col-md-2">
                            <input type="text" class="form-control" name="prescription_duration_0" placeholder="Duración">
                        </div>
                        <div class="col-md-2">
                            <button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    </div>
                `;
            }
            
            // Limpiar archivos adjuntos
            const existingAttachmentsDiv = document.getElementById('existingAttachments');
            if (existingAttachmentsDiv) existingAttachmentsDiv.innerHTML = '';
            
            const attachmentsInput = document.getElementById('attachmentsInput');
            if (attachmentsInput) attachmentsInput.value = '';
            
            // Establecer fecha de hoy por defecto
            const dateOfVisitEl = document.querySelector('input[name="dateOfVisit"]');
            if (dateOfVisitEl && !dateOfVisitEl.value) {
                const today = new Date().toISOString().split('T')[0];
                dateOfVisitEl.value = today;
            }
            
            // Abrir modal
            const modal = new bootstrap.Modal(document.getElementById('addDocumentModal'));
            modal.show();
        }

        let prescriptionIndex = 1;
        
        function getNextPrescriptionIndex() {
            const container = document.getElementById('prescriptionsContainer');
            if (!container) return 0;
            const inputs = container.querySelectorAll('input[name^="prescription_medication_"]');
            let maxIndex = -1;
            inputs.forEach(input => {
                const name = input.name;
                const match = name.match(/prescription_medication_(\d+)/);
                if (match) {
                    const index = parseInt(match[1], 10);
                    if (index > maxIndex) maxIndex = index;
                }
            });
            return maxIndex + 1;
        }
        
        function addPrescription() {
            const container = document.getElementById('prescriptionsContainer');
            if (!container) return;
            const nextIndex = getNextPrescriptionIndex();
            const div = document.createElement('div');
            div.className = 'prescription-item row g-3 mb-2';
            div.innerHTML = '<div class="col-md-4"><input type="text" class="form-control" name="prescription_medication_' + nextIndex + '" placeholder="Medicamento"></div>' +
                '<div class="col-md-2"><input type="text" class="form-control" name="prescription_dosage_' + nextIndex + '" placeholder="Dosis"></div>' +
                '<div class="col-md-2"><input type="text" class="form-control" name="prescription_frequency_' + nextIndex + '" placeholder="Frecuencia"></div>' +
                '<div class="col-md-2"><input type="text" class="form-control" name="prescription_duration_' + nextIndex + '" placeholder="Duración"></div>' +
                '<div class="col-md-2"><button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)"><i class="fas fa-times"></i></button></div>';
            container.appendChild(div);
            prescriptionIndex = nextIndex + 1;
        }
        
        function removePrescription(button) {
            button.closest('.prescription-item').remove();
        }
        
        function calculateBMI() {
            const weightInput = document.querySelector('input[name="vitalWeight"]');
            const heightInput = document.querySelector('input[name="vitalHeight"]');
            if (!weightInput || !heightInput) return;
            const weight = parseFloat(weightInput.value || 0);
            const height = parseFloat(heightInput.value || 0);
            const bmiField = document.getElementById('calculatedBMI');
            if (weight && height && height > 0) {
                const bmi = weight / Math.pow(height / 100, 2);
                if (bmiField) bmiField.value = bmi.toFixed(2);
            } else {
                if (bmiField) bmiField.value = '';
            }
        }
        
        // Auto-seleccionar especialidad cuando se selecciona un profesional
        document.addEventListener('DOMContentLoaded', function() {
            const professionalSelect = document.getElementById('professionalId');
            if (professionalSelect) {
                // Si ya hay un profesional seleccionado, actualizar la especialidad
                if (professionalSelect.value) {
                    const selectedOption = professionalSelect.options[professionalSelect.selectedIndex];
                    const specialtyId = selectedOption.getAttribute('data-specialty');
                    if (specialtyId) {
                        document.getElementById('specialtyId').value = specialtyId;
                    }
                }
                
                professionalSelect.addEventListener('change', function() {
                    const selectedOption = this.options[this.selectedIndex];
                    const specialtyId = selectedOption.getAttribute('data-specialty');
                    if (specialtyId) {
                        document.getElementById('specialtyId').value = specialtyId;
                    }
                });
            }
            
            // Calcular IMC
            document.querySelectorAll('input[name="vitalWeight"], input[name="vitalHeight"]').forEach(input => {
                input.addEventListener('input', calculateBMI);
            });
            
            // Verificar primero si viene con action=new (tiene prioridad)
            const urlParams = new URLSearchParams(window.location.search);
            const actionParam = urlParams.get('action');
            
            if (actionParam === 'new') {
                // Limpiar el formulario y abrir modal de nuevo documento
                const form = document.getElementById('documentForm');
                const formAction = document.getElementById('formAction');
                const documentId = document.getElementById('documentId');
                const modalTitle = document.getElementById('modalTitle');
                
                if (form) form.reset();
                if (formAction) formAction.value = 'create';
                if (documentId) documentId.value = '';
                if (modalTitle) modalTitle.textContent = 'Nuevo Documento Clínico';
                
                // Limpiar prescripciones y archivos
                const prescriptionsContainer = document.getElementById('prescriptionsContainer');
                if (prescriptionsContainer) {
                    prescriptionsContainer.innerHTML = `
                        <div class="prescription-item row g-3 mb-2">
                            <div class="col-md-4">
                                <input type="text" class="form-control" name="prescription_medication_0" placeholder="Medicamento">
                            </div>
                            <div class="col-md-2">
                                <input type="text" class="form-control" name="prescription_dosage_0" placeholder="Dosis">
                            </div>
                            <div class="col-md-2">
                                <input type="text" class="form-control" name="prescription_frequency_0" placeholder="Frecuencia">
                            </div>
                            <div class="col-md-2">
                                <input type="text" class="form-control" name="prescription_duration_0" placeholder="Duración">
                            </div>
                            <div class="col-md-2">
                                <button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)">
                                    <i class="fas fa-times"></i>
                                </button>
                            </div>
                        </div>
                    `;
                }
                
                const existingAttachmentsDiv = document.getElementById('existingAttachments');
                if (existingAttachmentsDiv) existingAttachmentsDiv.innerHTML = '';
                
                const attachmentsInput = document.getElementById('attachmentsInput');
                if (attachmentsInput) attachmentsInput.value = '';
                
                // Abrir modal de nuevo documento
                openNewDocumentModal();
                
                // Limpiar URL
                setTimeout(function() {
                    window.history.replaceState({}, document.title, window.location.pathname + '?patientId=${patient.id}');
                }, 100);
            } else if (typeof shouldOpenViewModal !== 'undefined' && shouldOpenViewModal) {
                // Abrir modal de vista
                const viewModalEl = document.getElementById('viewDocumentModal');
                if (viewModalEl) {
                    const viewModal = new bootstrap.Modal(viewModalEl);
                    viewModal.show();
                    setTimeout(function() {
                        window.history.replaceState({}, document.title, window.location.pathname + '?patientId=${patient.id}');
                    }, 100);
                }
            } else if (typeof shouldOpenEditModal !== 'undefined' && shouldOpenEditModal && typeof editDocumentData !== 'undefined') {
                var doc = editDocumentData;
                const documentIdEl = document.getElementById('documentId');
                const formActionEl = document.getElementById('formAction');
                const modalTitleEl = document.getElementById('modalTitle');
                const patientIdEl = document.getElementById('patientId');
                const professionalIdEl = document.getElementById('professionalId');
                const specialtyIdEl = document.getElementById('specialtyId');
                let documentTypeEl = document.getElementById('documentTypeSelect') || document.querySelector('select[name="documentType"]');
                
                if (documentIdEl) documentIdEl.value = doc.id || '';
                if (formActionEl) formActionEl.value = 'update';
                if (modalTitleEl) modalTitleEl.textContent = 'Editar Documento Clínico';
                if (patientIdEl) patientIdEl.value = doc.patientId || '';
                if (professionalIdEl) professionalIdEl.value = doc.professionalId || '';
                if (specialtyIdEl) specialtyIdEl.value = doc.specialtyId || '';
                if (documentTypeEl && doc.documentType) documentTypeEl.value = doc.documentType;
                
                const titleEl = document.querySelector('input[name="title"]');
                const dateOfVisitEl = document.querySelector('input[name="dateOfVisit"]');
                const descriptionEl = document.querySelector('textarea[name="description"]');
                const nextAppointmentEl = document.querySelector('input[name="nextAppointment"]');
                const chiefComplaintEl = document.querySelector('textarea[name="chiefComplaint"]');
                const currentIllnessEl = document.querySelector('textarea[name="currentIllness"]');
                const physicalExaminationEl = document.querySelector('textarea[name="physicalExamination"]');
                const diagnosisEl = document.querySelector('textarea[name="diagnosis"]');
                const treatmentEl = document.querySelector('textarea[name="treatment"]');
                const observationsEl = document.querySelector('textarea[name="observations"]');
                
                if (titleEl) titleEl.value = doc.title || '';
                if (dateOfVisitEl) dateOfVisitEl.value = doc.dateOfVisit || '';
                if (descriptionEl) descriptionEl.value = doc.description || '';
                if (nextAppointmentEl) nextAppointmentEl.value = doc.nextAppointment || '';
                if (chiefComplaintEl) chiefComplaintEl.value = doc.chiefComplaint || '';
                if (currentIllnessEl) currentIllnessEl.value = doc.currentIllness || '';
                if (physicalExaminationEl) physicalExaminationEl.value = doc.physicalExamination || '';
                if (diagnosisEl) diagnosisEl.value = doc.diagnosis || '';
                if (treatmentEl) treatmentEl.value = doc.treatment || '';
                if (observationsEl) observationsEl.value = doc.observations || '';
                
                // Cargar signos vitales
                if (doc.vitalSigns && doc.vitalSigns !== null && doc.vitalSigns !== 'null' && doc.vitalSigns !== '') {
                    try {
                        let vitalSigns = typeof doc.vitalSigns === 'string' ? JSON.parse(doc.vitalSigns) : doc.vitalSigns;
                        if (vitalSigns.pressure) {
                            const el = document.querySelector('input[name="vitalPressure"]');
                            if (el) el.value = vitalSigns.pressure;
                        }
                        if (vitalSigns.temperature) {
                            const el = document.querySelector('input[name="vitalTemperature"]');
                            if (el) el.value = vitalSigns.temperature;
                        }
                        if (vitalSigns.pulse) {
                            const el = document.querySelector('input[name="vitalPulse"]');
                            if (el) el.value = vitalSigns.pulse;
                        }
                        if (vitalSigns.respiratoryRate) {
                            const el = document.querySelector('input[name="vitalRespiratoryRate"]');
                            if (el) el.value = vitalSigns.respiratoryRate;
                        }
                        if (vitalSigns.o2Saturation) {
                            const el = document.querySelector('input[name="vitalO2Saturation"]');
                            if (el) el.value = vitalSigns.o2Saturation;
                        }
                        if (vitalSigns.weight) {
                            const el = document.querySelector('input[name="vitalWeight"]');
                            if (el) {
                                el.value = vitalSigns.weight;
                                calculateBMI();
                            }
                        }
                        if (vitalSigns.height) {
                            const el = document.querySelector('input[name="vitalHeight"]');
                            if (el) {
                                el.value = vitalSigns.height;
                                calculateBMI();
                            }
                        }
                    } catch (e) {}
                }
                
                // Cargar prescripciones
                if (doc.prescriptions && doc.prescriptions !== null && doc.prescriptions !== 'null' && doc.prescriptions !== '') {
                    try {
                        let prescriptions = typeof doc.prescriptions === 'string' ? JSON.parse(doc.prescriptions) : doc.prescriptions;
                        const container = document.getElementById('prescriptionsContainer');
                        if (container) {
                            container.innerHTML = '';
                            prescriptionIndex = 0;
                            if (prescriptions && Array.isArray(prescriptions) && prescriptions.length > 0) {
                                prescriptions.forEach(function(prescription) {
                                    const idx = prescriptionIndex;
                                    const escapeHtml = (text) => {
                                        if (!text) return '';
                                        return String(text).replace(/"/g, '&quot;').replace(/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                                    };
                                    const div = document.createElement('div');
                                    div.className = 'prescription-item row g-3 mb-2';
                                    div.innerHTML = '<div class="col-md-4"><input type="text" class="form-control" name="prescription_medication_' + idx + '" placeholder="Medicamento" value="' + escapeHtml(prescription.medication || '') + '"></div>' +
                                        '<div class="col-md-2"><input type="text" class="form-control" name="prescription_dosage_' + idx + '" placeholder="Dosis" value="' + escapeHtml(prescription.dosage || '') + '"></div>' +
                                        '<div class="col-md-2"><input type="text" class="form-control" name="prescription_frequency_' + idx + '" placeholder="Frecuencia" value="' + escapeHtml(prescription.frequency || '') + '"></div>' +
                                        '<div class="col-md-2"><input type="text" class="form-control" name="prescription_duration_' + idx + '" placeholder="Duración" value="' + escapeHtml(prescription.duration || '') + '"></div>' +
                                        '<div class="col-md-2"><button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)"><i class="fas fa-times"></i></button></div>';
                                    container.appendChild(div);
                                    prescriptionIndex++;
                                });
                            }
                            if (prescriptionIndex === 0) {
                                const div = document.createElement('div');
                                div.className = 'prescription-item row g-3 mb-2';
                                div.innerHTML = '<div class="col-md-4"><input type="text" class="form-control" name="prescription_medication_0" placeholder="Medicamento"></div>' +
                                    '<div class="col-md-2"><input type="text" class="form-control" name="prescription_dosage_0" placeholder="Dosis"></div>' +
                                    '<div class="col-md-2"><input type="text" class="form-control" name="prescription_frequency_0" placeholder="Frecuencia"></div>' +
                                    '<div class="col-md-2"><input type="text" class="form-control" name="prescription_duration_0" placeholder="Duración"></div>' +
                                    '<div class="col-md-2"><button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)"><i class="fas fa-times"></i></button></div>';
                                container.appendChild(div);
                                prescriptionIndex = 1;
                            }
                            prescriptionIndex = getNextPrescriptionIndex() + 1;
                        }
                    } catch (e) {}
                }
                
                // Cargar archivos adjuntos existentes
                const existingAttachmentsDiv = document.getElementById('existingAttachments');
                if (existingAttachmentsDiv && doc.attachments && doc.attachments !== null && doc.attachments !== 'null' && doc.attachments !== '') {
                    try {
                        let attachments = typeof doc.attachments === 'string' ? JSON.parse(doc.attachments) : doc.attachments;
                        if (attachments && Array.isArray(attachments) && attachments.length > 0) {
                            let html = '<label class="form-label">Archivos Existentes</label><div class="list-group mb-3">';
                            attachments.forEach((att, idx) => {
                                const fileName = att.fileName || 'Archivo ' + (idx + 1);
                                const mimeType = att.mimeType || '';
                                let iconClass = 'fas fa-file';
                                if (mimeType.includes('pdf')) iconClass = 'fas fa-file-pdf text-danger';
                                else if (mimeType.includes('image')) iconClass = 'fas fa-file-image text-info';
                                else if (mimeType.includes('word') || mimeType.includes('document')) iconClass = 'fas fa-file-word text-primary';
                                let sizeStr = '';
                                if (att.fileSize) {
                                    const size = parseInt(att.fileSize);
                                    if (size < 1024) sizeStr = size + ' B';
                                    else if (size < 1024 * 1024) sizeStr = (size / 1024).toFixed(1) + ' KB';
                                    else sizeStr = (size / (1024 * 1024)).toFixed(1) + ' MB';
                                }
                                html += '<div class="list-group-item d-flex justify-content-between align-items-center">';
                                html += '<span><i class="' + iconClass + ' me-2"></i>' + fileName + '</span>';
                                if (sizeStr) html += '<small class="text-muted">' + sizeStr + '</small>';
                                html += '</div>';
                            });
                            html += '</div>';
                            existingAttachmentsDiv.innerHTML = html;
                        }
                    } catch (e) {}
                }
                
                // Abrir modal de edición
                const editModalEl = document.getElementById('addDocumentModal');
                if (editModalEl) {
                    const editModal = new bootstrap.Modal(editModalEl);
                    editModal.show();
                    setTimeout(function() {
                        window.history.replaceState({}, document.title, window.location.pathname + '?patientId=${patient.id}');
                    }, 100);
                }
            } else if (typeof shouldOpenRemoteViewModal !== 'undefined' && shouldOpenRemoteViewModal) {
                const remoteModalEl = document.getElementById('viewRemoteDocumentModal');
                if (remoteModalEl) {
                    const remoteModal = new bootstrap.Modal(remoteModalEl);
                    remoteModal.show();
                    setTimeout(function() {
                        window.history.replaceState({}, document.title, window.location.pathname + '?patientId=${patient.id}');
                    }, 100);
                }
            }
        });
    </script>
</body>
</html>

