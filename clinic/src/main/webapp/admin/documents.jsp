<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Documentos Clínicos - HCEN Clínica</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4CAF50;
            --secondary-color: #45a049;
        }
        
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .sidebar {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
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
        
        .btn-primary {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            border: none;
            border-radius: 25px;
            padding: 10px 25px;
            font-weight: 600;
        }
        
        .table thead th {
            background: var(--primary-color);
            color: white;
            border: none;
            font-weight: 600;
        }
        
        .form-section {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        
        .form-section h5 {
            color: var(--primary-color);
            margin-bottom: 15px;
            border-bottom: 2px solid var(--primary-color);
            padding-bottom: 10px;
        }
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="<c:url value='/admin/dashboard'/>">
                <i class="fas fa-hospital me-2"></i>HCEN - ${sessionScope.clinicName}
            </a>
            <div class="navbar-nav ms-auto">
                <div class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                        <i class="fas fa-user me-2"></i>${sessionScope.user}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
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
                        <h6 class="text-white-50 mb-3">ADMINISTRACIÓN</h6>
                        <ul class="nav flex-column">
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/admin/dashboard'/>">
                                    <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/admin/professionals'/>">
                                    <i class="fas fa-user-md me-2"></i>Profesionales
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/admin/patients-list'/>">
                                    <i class="fas fa-users me-2"></i>Pacientes
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="<c:url value='/admin/documents'/>">
                                    <i class="fas fa-file-medical me-2"></i>Documentos
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/admin/specialties-list'/>">
                                    <i class="fas fa-stethoscope me-2"></i>Especialidades
                                </a>
                            </li>
                            <c:if test="${sessionScope.role == 'ADMIN_CLINIC' or sessionScope.role == 'SUPER_ADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="<c:url value='/admin/users'/>">
                                        <i class="fas fa-user-cog me-2"></i>Gestión de Usuarios
                                    </a>
                                </li>
                            </c:if>
                            <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="<c:url value='/admin/super-admin'/>">
                                        <i class="fas fa-crown me-2"></i>Super Admin
                                    </a>
                                </li>
                            </c:if>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- Contenido Principal -->
            <div class="col-md-10 p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2><i class="fas fa-file-medical me-2"></i>Gestión de Documentos Clínicos</h2>
                    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addDocumentModal">
                        <i class="fas fa-plus me-2"></i>Nuevo Documento
                    </button>
                </div>
                
                <!-- Mensajes -->
                <c:if test="${not empty success}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="fas fa-check-circle me-2"></i>${success}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <!-- Filtros -->
                <div class="card mb-4">
                    <div class="card-body">
                        <form method="GET" action="<c:url value='/admin/documents'/>">
                            <div class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label">Especialidad</label>
                                    <select class="form-select" name="specialtyId">
                                        <option value="">Todas</option>
                                        <c:forEach var="specialty" items="${specialties}">
                                            <option value="${specialty.id}" ${param.specialtyId == specialty.id ? 'selected' : ''}>
                                                ${specialty.name}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Paciente</label>
                                    <select class="form-select" name="patientId">
                                        <option value="">Todos</option>
                                        <c:forEach var="patient" items="${patients}">
                                            <option value="${patient.id}" ${param.patientId == patient.id ? 'selected' : ''}>
                                                ${patient.fullName}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Profesional</label>
                                    <select class="form-select" name="professionalId">
                                        <option value="">Todos</option>
                                        <c:forEach var="professional" items="${professionals}">
                                            <option value="${professional.id}" ${param.professionalId == professional.id ? 'selected' : ''}>
                                                ${professional.fullName}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Tipo</label>
                                    <select class="form-select" name="documentType">
                                        <option value="">Todos</option>
                                        <option value="CONSULTA" ${param.documentType == 'CONSULTA' ? 'selected' : ''}>Consulta</option>
                                        <option value="DIAGNOSTICO" ${param.documentType == 'DIAGNOSTICO' ? 'selected' : ''}>Diagnóstico</option>
                                        <option value="TRATAMIENTO" ${param.documentType == 'TRATAMIENTO' ? 'selected' : ''}>Tratamiento</option>
                                        <option value="EVOLUCION" ${param.documentType == 'EVOLUCION' ? 'selected' : ''}>Evolución</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Fecha Desde</label>
                                    <input type="date" class="form-control" name="dateFrom" value="${param.dateFrom}">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label">Fecha Hasta</label>
                                    <input type="date" class="form-control" name="dateTo" value="${param.dateTo}">
                                </div>
                                <div class="col-md-6 d-flex align-items-end">
                                    <button type="submit" class="btn btn-primary me-2">
                                        <i class="fas fa-search me-2"></i>Filtrar
                                    </button>
                                    <a href="<c:url value='/admin/documents'/>" class="btn btn-secondary">
                                        <i class="fas fa-times me-2"></i>Limpiar
                                    </a>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Tabla de Documentos -->
                <div class="card">
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>Fecha</th>
                                        <th>Paciente</th>
                                        <th>Profesional</th>
                                        <th>Especialidad</th>
                                        <th>Tipo</th>
                                        <th>Título</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty documents}">
                                            <tr>
                                                <td colspan="7" class="text-center text-muted py-4">
                                                    <i class="fas fa-file-medical fa-3x mb-3 d-block"></i>
                                                    No hay documentos registrados
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="doc" items="${documents}">
                                                <tr>
                                                    <td>
                                                        <%
                                                            uy.gub.clinic.entity.ClinicalDocument docItem = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                                            if (docItem != null && docItem.getDateOfVisit() != null) {
                                                                java.util.Date date = java.sql.Date.valueOf(docItem.getDateOfVisit());
                                                                pageContext.setAttribute("visitDate", date);
                                                        %>
                                                            <fmt:formatDate value="${visitDate}" pattern="dd/MM/yyyy"/>
                                                        <%
                                                            }
                                                        %>
                                                    </td>
                                                    <td>${doc.patient.fullName}</td>
                                                    <td>${doc.professional.fullName}</td>
                                                    <td>${doc.specialty.name}</td>
                                                    <td><span class="badge bg-info">${doc.documentType}</span></td>
                                                    <td>${doc.title}</td>
                                                    <td>
                                                        <c:set var="docId" value="${doc.id}" />
                                                        <div class="btn-group" role="group">
                                                            <button class="btn btn-sm btn-outline-primary" title="Ver detalles" 
                                                                    onclick="viewDocument('${docId}')">
                                                                <i class="fas fa-eye"></i>
                                                            </button>
                                                            <button class="btn btn-sm btn-outline-warning" title="Editar" 
                                                                    onclick="editDocument('${docId}')">
                                                                <i class="fas fa-edit"></i>
                                                            </button>
                                                            <button class="btn btn-sm btn-outline-danger" title="Eliminar" 
                                                                    onclick="deleteDocument('${docId}')">
                                                                <i class="fas fa-trash"></i>
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
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
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form id="documentForm" method="POST" action="<c:url value='/admin/documents'/>" enctype="multipart/form-data">
                    <input type="hidden" name="action" id="formAction" value="create">
                    <input type="hidden" name="documentId" id="documentId">
                    
                    <div class="modal-body" style="max-height: 70vh; overflow-y: auto;">
                        <!-- Sección 1: Información Básica -->
                        <div class="form-section">
                            <h5><i class="fas fa-info-circle me-2"></i>Información Básica</h5>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Paciente *</label>
                                    <select class="form-select" name="patientId" id="patientId" required>
                                        <option value="">Seleccionar...</option>
                                        <c:forEach var="patient" items="${patients}">
                                            <option value="${patient.id}">${patient.fullName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Profesional *</label>
                                    <select class="form-select" name="professionalId" id="professionalId" required>
                                        <option value="">Seleccionar...</option>
                                        <c:forEach var="professional" items="${professionals}">
                                            <option value="${professional.id}" data-specialty="${professional.specialty.id}">
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
                                        <option value="CONSULTA" ${not empty selectedDocument and selectedDocument.documentType == 'CONSULTA' ? 'selected' : ''}>Consulta</option>
                                        <option value="DIAGNOSTICO" ${not empty selectedDocument and selectedDocument.documentType == 'DIAGNOSTICO' ? 'selected' : ''}>Diagnóstico</option>
                                        <option value="TRATAMIENTO" ${not empty selectedDocument and selectedDocument.documentType == 'TRATAMIENTO' ? 'selected' : ''}>Tratamiento</option>
                                        <option value="EVOLUCION" ${not empty selectedDocument and selectedDocument.documentType == 'EVOLUCION' ? 'selected' : ''}>Evolución</option>
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

    <!-- Modal Ver Documento -->
    <div class="modal fade" id="viewDocumentModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
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
                                                // Si falla el parseo, mostrar el texto crudo
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
                                                // Si falla el parseo, mostrar el texto crudo
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
                                                
                                                // Determinar icono según tipo de archivo
                                                if (mimeType.contains("pdf")) {
                                                    iconClass = "fas fa-file-pdf text-danger";
                                                } else if (mimeType.contains("image")) {
                                                    iconClass = "fas fa-file-image text-info";
                                                } else if (mimeType.contains("word") || mimeType.contains("document")) {
                                                    iconClass = "fas fa-file-word text-primary";
                                                } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
                                                    iconClass = "fas fa-file-excel text-success";
                                                }
                                                
                                                Long docId = docViewAtt.getId();
                                                String downloadUrl = request.getContextPath() + "/admin/documents?action=download&id=" + docId + "&fileIndex=" + i;
                                                String deleteUrl = request.getContextPath() + "/admin/documents?action=deleteAttachment&id=" + docId + "&fileIndex=" + i;
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
                                        <button class="btn btn-sm btn-danger ms-2" onclick="deleteAttachment('<%= docId %>', <%= i %>, '<%= fileName %>', true)" title="Eliminar archivo">
                                            <i class="fas fa-trash"></i>
                                        </button>
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

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <%-- Generar variables JavaScript para controlar la apertura de modales --%>
    <%
        // Preparar datos para JavaScript
        uy.gub.clinic.entity.ClinicalDocument selectedDoc = (uy.gub.clinic.entity.ClinicalDocument) request.getAttribute("selectedDocument");
        String actionStr = (String) request.getAttribute("action");
        boolean hasSelectedDoc = selectedDoc != null;
        boolean isEditAction = "edit".equals(actionStr);
        
        // Función helper para escapar JavaScript de forma segura
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
        var shouldOpenModal = <%= hasSelectedDoc %>;
        var modalAction = '<%= actionStr != null ? escapeJs.apply(actionStr) : "" %>';
        
        <% if (hasSelectedDoc && isEditAction && selectedDoc != null) { 
            String dateOfVisitStr = selectedDoc.getDateOfVisit() != null ? selectedDoc.getDateOfVisit().toString() : "";
            String nextAppointmentStr = selectedDoc.getNextAppointment() != null ? selectedDoc.getNextAppointment().toString() : "";
            
            // Para JSON, escapar correctamente para JavaScript - doble escape para comillas dentro de strings
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
        // DEFINIR FUNCIONES para que estén disponibles globalmente
        function viewDocument(id) {
            // Redirigir para cargar el documento
            window.location.href = '<c:url value="/admin/documents"/>?action=view&id=' + id;
        }
        
        function editDocument(id) {
            // Redirigir para cargar el documento en modo edición
            window.location.href = '<c:url value="/admin/documents"/>?action=edit&id=' + id;
        }
        
        function deleteDocument(id) {
            if (confirm('¿Está seguro de eliminar este documento?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '<c:url value="/admin/documents"/>';
                
                const actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = 'delete';
                form.appendChild(actionInput);
                
                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'documentId';
                idInput.value = id;
                form.appendChild(idInput);
                
                document.body.appendChild(form);
                form.submit();
            }
        }
        
        function deleteAttachment(documentId, fileIndex, fileName, isViewModal) {
            if (!confirm('¿Está seguro de eliminar el archivo "' + fileName + '"?')) {
                return;
            }
            
            const deleteUrl = '<c:url value="/admin/documents"/>?action=deleteAttachment&id=' + documentId + '&fileIndex=' + fileIndex;
            
            fetch(deleteUrl, {
                method: 'GET',
                headers: {
                    'Accept': 'application/json'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Si estamos en el modal de vista, recargar la página para actualizar
                    if (isViewModal) {
                        window.location.reload();
                    } else {
                        // Si estamos en el modal de edición, recargar los archivos existentes
                        const doc = typeof editDocumentData !== 'undefined' ? editDocumentData : null;
                        if (doc && doc.attachments) {
                            try {
                                let attachments = JSON.parse(doc.attachments);
                                attachments = attachments.filter((att, idx) => idx !== fileIndex);
                                doc.attachments = JSON.stringify(attachments);
                                
                                // Recargar la sección de archivos existentes
                                const existingAttachmentsDiv = document.getElementById('existingAttachments');
                                if (existingAttachmentsDiv && attachments.length > 0) {
                                    let html = '<label class="form-label">Archivos Existentes</label><div class="list-group mb-3">';
                                    attachments.forEach((att, idx) => {
                                        const attFileName = att.fileName || 'Archivo ' + (idx + 1);
                                        const mimeType = att.mimeType || '';
                                        let iconClass = 'fas fa-file';
                                        
                                        if (mimeType.includes('pdf')) {
                                            iconClass = 'fas fa-file-pdf text-danger';
                                        } else if (mimeType.includes('image')) {
                                            iconClass = 'fas fa-file-image text-info';
                                        } else if (mimeType.includes('word') || mimeType.includes('document')) {
                                            iconClass = 'fas fa-file-word text-primary';
                                        }
                                        
                                        let sizeStr = '';
                                        if (att.fileSize) {
                                            const size = parseInt(att.fileSize);
                                            if (size < 1024) {
                                                sizeStr = size + ' B';
                                            } else if (size < 1024 * 1024) {
                                                sizeStr = (size / 1024).toFixed(1) + ' KB';
                                            } else {
                                                sizeStr = (size / (1024 * 1024)).toFixed(1) + ' MB';
                                            }
                                        }
                                        
                                        html += '<div class="list-group-item d-flex justify-content-between align-items-center">';
                                        html += '<span><i class="' + iconClass + ' me-2"></i>' + attFileName + '</span>';
                                        html += '<div class="d-flex align-items-center gap-2">';
                                        if (sizeStr) {
                                            html += '<small class="text-muted">' + sizeStr + '</small>';
                                        }
                                        html += '<button class="btn btn-sm btn-danger" onclick="deleteAttachment(' + documentId + ', ' + idx + ', \'' + attFileName.replace(/'/g, "\\'") + '\', false)" title="Eliminar archivo">';
                                        html += '<i class="fas fa-trash"></i>';
                                        html += '</button>';
                                        html += '</div>';
                                        html += '</div>';
                                    });
                                    html += '</div>';
                                    existingAttachmentsDiv.innerHTML = html;
                                } else if (existingAttachmentsDiv) {
                                    existingAttachmentsDiv.innerHTML = '';
                                }
                            } catch (e) {
                                // Error al actualizar, recargar la página
                                window.location.reload();
                            }
                        } else {
                            // Si no hay más archivos, ocultar la sección
                            const existingAttachmentsDiv = document.getElementById('existingAttachments');
                            if (existingAttachmentsDiv) {
                                existingAttachmentsDiv.innerHTML = '';
                            }
                        }
                    }
                } else {
                    alert('Error al eliminar el archivo: ' + (data.error || 'Error desconocido'));
                }
            })
            .catch(error => {
                console.error('Error al eliminar archivo:', error);
                alert('Error al eliminar el archivo. Por favor, intente nuevamente.');
            });
        }
        
        // Asignar al objeto window inmediatamente
        window.viewDocument = viewDocument;
        window.editDocument = editDocument;
        window.deleteDocument = deleteDocument;
        window.deleteAttachment = deleteAttachment;
        
        let prescriptionIndex = 1;
        
        // Función para calcular el siguiente índice disponible basándose en los campos existentes
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
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                }
            });
            
            return maxIndex + 1;
        }
        
        // Auto-seleccionar especialidad cuando se selecciona un profesional
        document.addEventListener('DOMContentLoaded', function() {
            const professionalSelect = document.getElementById('professionalId');
            if (professionalSelect) {
                professionalSelect.addEventListener('change', function() {
                    const selectedOption = this.options[this.selectedIndex];
                    const specialtyId = selectedOption.getAttribute('data-specialty');
                    if (specialtyId) {
                        document.getElementById('specialtyId').value = specialtyId;
                    }
                });
            }
        });
        
        // Calcular IMC
        function calculateBMI() {
            const weightInput = document.querySelector('input[name="vitalWeight"]');
            const heightInput = document.querySelector('input[name="vitalHeight"]');
            if (!weightInput || !heightInput) return;
            
            const weight = parseFloat(weightInput.value || 0);
            const height = parseFloat(heightInput.value || 0);
            const bmiField = document.getElementById('calculatedBMI');
            
            if (weight && height && height > 0) {
                const bmi = weight / Math.pow(height / 100, 2);
                if (bmiField) {
                    bmiField.value = bmi.toFixed(2);
                }
            } else {
                if (bmiField) {
                    bmiField.value = '';
                }
            }
        }
        
        function addPrescription() {
            const container = document.getElementById('prescriptionsContainer');
            if (!container) {
                return;
            }
            
            // Calcular el siguiente índice disponible
            const nextIndex = getNextPrescriptionIndex();
            
            const div = document.createElement('div');
            div.className = 'prescription-item row g-3 mb-2';
            
            // Crear los elementos manualmente para evitar problemas con template literals
            const colMed = document.createElement('div');
            colMed.className = 'col-md-4';
            const inputMed = document.createElement('input');
            inputMed.type = 'text';
            inputMed.className = 'form-control';
            inputMed.name = 'prescription_medication_' + nextIndex;
            inputMed.placeholder = 'Medicamento';
            colMed.appendChild(inputMed);
            
            const colDosage = document.createElement('div');
            colDosage.className = 'col-md-2';
            const inputDosage = document.createElement('input');
            inputDosage.type = 'text';
            inputDosage.className = 'form-control';
            inputDosage.name = 'prescription_dosage_' + nextIndex;
            inputDosage.placeholder = 'Dosis';
            colDosage.appendChild(inputDosage);
            
            const colFreq = document.createElement('div');
            colFreq.className = 'col-md-2';
            const inputFreq = document.createElement('input');
            inputFreq.type = 'text';
            inputFreq.className = 'form-control';
            inputFreq.name = 'prescription_frequency_' + nextIndex;
            inputFreq.placeholder = 'Frecuencia';
            colFreq.appendChild(inputFreq);
            
            const colDur = document.createElement('div');
            colDur.className = 'col-md-2';
            const inputDur = document.createElement('input');
            inputDur.type = 'text';
            inputDur.className = 'form-control';
            inputDur.name = 'prescription_duration_' + nextIndex;
            inputDur.placeholder = 'Duración';
            colDur.appendChild(inputDur);
            
            const colBtn = document.createElement('div');
            colBtn.className = 'col-md-2';
            const btnRemove = document.createElement('button');
            btnRemove.type = 'button';
            btnRemove.className = 'btn btn-sm btn-danger';
            btnRemove.onclick = function() { removePrescription(this); };
            btnRemove.innerHTML = '<i class="fas fa-times"></i>';
            colBtn.appendChild(btnRemove);
            
            div.appendChild(colMed);
            div.appendChild(colDosage);
            div.appendChild(colFreq);
            div.appendChild(colDur);
            div.appendChild(colBtn);
            
            container.appendChild(div);
            
            // Actualizar el índice global para mantener consistencia
            prescriptionIndex = nextIndex + 1;
        }
        
        function removePrescription(button) {
            button.closest('.prescription-item').remove();
        }
        
        // Asignar funciones adicionales al objeto window
        window.addPrescription = addPrescription;
        window.removePrescription = removePrescription;
        window.calculateBMI = calculateBMI;
        window.deleteAttachment = deleteAttachment;
        
        // Limpiar formulario al cerrar modal
        document.addEventListener('DOMContentLoaded', function() {
            const addModal = document.getElementById('addDocumentModal');
            if (addModal) {
                addModal.addEventListener('hidden.bs.modal', function() {
                    const form = document.getElementById('documentForm');
                    const formAction = document.getElementById('formAction');
                    const documentId = document.getElementById('documentId');
                    const modalTitle = document.getElementById('modalTitle');
                    const prescriptionsContainer = document.getElementById('prescriptionsContainer');
                    const existingAttachmentsDiv = document.getElementById('existingAttachments');
                    const attachmentsInput = document.getElementById('attachmentsInput');
                    
                    if (form) form.reset();
                    if (formAction) formAction.value = 'create';
                    if (documentId) documentId.value = '';
                    if (modalTitle) modalTitle.textContent = 'Nuevo Documento Clínico';
                    if (existingAttachmentsDiv) existingAttachmentsDiv.innerHTML = '';
                    if (attachmentsInput) attachmentsInput.value = '';
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
                        // Recalcular el índice basándose en los campos existentes
                        prescriptionIndex = getNextPrescriptionIndex() + 1;
                    } else {
                        prescriptionIndex = 1;
                    }
                });
            }
        });
        
        // Auto-abrir modales cuando hay un documento seleccionado
        // Esperar a que todo esté completamente cargado
        window.addEventListener('load', function() {
            // Asegurarse de que Bootstrap esté disponible
            if (typeof bootstrap === 'undefined') {
                return;
            }
            
            // Verificar variables y abrir modales
            if (typeof shouldOpenModal !== 'undefined' && shouldOpenModal) {
                if (modalAction === 'view') {
                    const viewModalEl = document.getElementById('viewDocumentModal');
                    if (viewModalEl) {
                        const viewModal = new bootstrap.Modal(viewModalEl);
                        viewModal.show();
                        
                        // Limpiar URL después de abrir el modal
                        setTimeout(function() {
                            window.history.replaceState({}, document.title, window.location.pathname);
                        }, 100);
                    }
                } else if (modalAction === 'edit' && typeof editDocumentData !== 'undefined') {
                    var doc = editDocumentData;
                    
                    // Llenar formulario
                    const documentIdEl = document.getElementById('documentId');
                    const formActionEl = document.getElementById('formAction');
                    const modalTitleEl = document.getElementById('modalTitle');
                    const patientIdEl = document.getElementById('patientId');
                    const professionalIdEl = document.getElementById('professionalId');
                    const specialtyIdEl = document.getElementById('specialtyId');
                    let documentTypeEl = document.getElementById('documentTypeSelect') || document.querySelector('select[name="documentType"]');
                    const titleEl = document.querySelector('input[name="title"]');
                    const dateOfVisitEl = document.querySelector('input[name="dateOfVisit"]');
                    
                    if (documentIdEl) documentIdEl.value = doc.id || '';
                    if (formActionEl) formActionEl.value = 'update';
                    if (modalTitleEl) modalTitleEl.textContent = 'Editar Documento Clínico';
                    if (patientIdEl) patientIdEl.value = doc.patientId || '';
                    if (professionalIdEl) professionalIdEl.value = doc.professionalId || '';
                    if (specialtyIdEl) specialtyIdEl.value = doc.specialtyId || '';
                    if (titleEl) titleEl.value = doc.title || '';
                    if (dateOfVisitEl) dateOfVisitEl.value = doc.dateOfVisit || '';
                    
                    const descriptionEl = document.querySelector('textarea[name="description"]');
                    const nextAppointmentEl = document.querySelector('input[name="nextAppointment"]');
                    const chiefComplaintEl = document.querySelector('textarea[name="chiefComplaint"]');
                    const currentIllnessEl = document.querySelector('textarea[name="currentIllness"]');
                    const physicalExaminationEl = document.querySelector('textarea[name="physicalExamination"]');
                    const diagnosisEl = document.querySelector('textarea[name="diagnosis"]');
                    const treatmentEl = document.querySelector('textarea[name="treatment"]');
                    const observationsEl = document.querySelector('textarea[name="observations"]');
                    
                    if (descriptionEl) descriptionEl.value = doc.description || '';
                    if (nextAppointmentEl) nextAppointmentEl.value = doc.nextAppointment || '';
                    if (chiefComplaintEl) chiefComplaintEl.value = doc.chiefComplaint || '';
                    if (currentIllnessEl) currentIllnessEl.value = doc.currentIllness || '';
                    if (physicalExaminationEl) physicalExaminationEl.value = doc.physicalExamination || '';
                    if (diagnosisEl) diagnosisEl.value = doc.diagnosis || '';
                    if (treatmentEl) treatmentEl.value = doc.treatment || '';
                    if (observationsEl) observationsEl.value = doc.observations || '';
                    
                    // Cargar signos vitales si existen
                    if (doc.vitalSigns && doc.vitalSigns !== null && doc.vitalSigns !== 'null' && doc.vitalSigns !== '') {
                        try {
                            let vitalSigns;
                            if (typeof doc.vitalSigns === 'string') {
                                vitalSigns = JSON.parse(doc.vitalSigns);
                            } else {
                                vitalSigns = doc.vitalSigns;
                            }
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
                        } catch (e) {
                            // Error al parsear signos vitales, ignorar
                        }
                    }
                    
                    // Cargar prescripciones si existen
                    if (doc.prescriptions && doc.prescriptions !== null && doc.prescriptions !== 'null' && doc.prescriptions !== '') {
                        try {
                            let prescriptions;
                            if (typeof doc.prescriptions === 'string') {
                                prescriptions = JSON.parse(doc.prescriptions);
                            } else {
                                prescriptions = doc.prescriptions;
                            }
                            
                            const container = document.getElementById('prescriptionsContainer');
                            if (container) {
                                container.innerHTML = '';
                                prescriptionIndex = 0;
                                
                                if (prescriptions && Array.isArray(prescriptions) && prescriptions.length > 0) {
                                    prescriptions.forEach(function(prescription) {
                                        // Incluir todas las prescripciones, incluso si el medicamento está vacío
                                        const div = document.createElement('div');
                                        div.className = 'prescription-item row g-3 mb-2';
                                        
                                        // Función helper para escapar HTML
                                        const escapeHtml = (text) => {
                                            if (!text) return '';
                                            return String(text).replace(/"/g, '&quot;').replace(/'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
                                        };
                                        
                                        const idx = prescriptionIndex;
                                        const med = escapeHtml(prescription.medication || '');
                                        const dos = escapeHtml(prescription.dosage || '');
                                        const freq = escapeHtml(prescription.frequency || '');
                                        const dur = escapeHtml(prescription.duration || '');
                                        
                                        div.innerHTML = '<div class="col-md-4">' +
                                            '<input type="text" class="form-control" name="prescription_medication_' + idx + '" placeholder="Medicamento" value="' + med + '">' +
                                            '</div>' +
                                            '<div class="col-md-2">' +
                                            '<input type="text" class="form-control" name="prescription_dosage_' + idx + '" placeholder="Dosis" value="' + dos + '">' +
                                            '</div>' +
                                            '<div class="col-md-2">' +
                                            '<input type="text" class="form-control" name="prescription_frequency_' + idx + '" placeholder="Frecuencia" value="' + freq + '">' +
                                            '</div>' +
                                            '<div class="col-md-2">' +
                                            '<input type="text" class="form-control" name="prescription_duration_' + idx + '" placeholder="Duración" value="' + dur + '">' +
                                            '</div>' +
                                            '<div class="col-md-2">' +
                                            '<button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)">' +
                                            '<i class="fas fa-times"></i>' +
                                            '</button>' +
                                            '</div>';
                                        container.appendChild(div);
                                        prescriptionIndex++;
                                    });
                                }
                                
                                // Si no hay prescripciones o el contenedor está vacío, agregar un campo vacío
                                if (prescriptionIndex === 0) {
                                    const div = document.createElement('div');
                                    div.className = 'prescription-item row g-3 mb-2';
                                    div.innerHTML = '<div class="col-md-4">' +
                                        '<input type="text" class="form-control" name="prescription_medication_0" placeholder="Medicamento">' +
                                        '</div>' +
                                        '<div class="col-md-2">' +
                                        '<input type="text" class="form-control" name="prescription_dosage_0" placeholder="Dosis">' +
                                        '</div>' +
                                        '<div class="col-md-2">' +
                                        '<input type="text" class="form-control" name="prescription_frequency_0" placeholder="Frecuencia">' +
                                        '</div>' +
                                        '<div class="col-md-2">' +
                                        '<input type="text" class="form-control" name="prescription_duration_0" placeholder="Duración">' +
                                        '</div>' +
                                        '<div class="col-md-2">' +
                                        '<button type="button" class="btn btn-sm btn-danger" onclick="removePrescription(this)">' +
                                        '<i class="fas fa-times"></i>' +
                                        '</button>' +
                                        '</div>';
                                    container.appendChild(div);
                                    prescriptionIndex = 1;
                                }
                                
                                // Actualizar prescriptionIndex basándose en los campos existentes después de cargar
                                prescriptionIndex = getNextPrescriptionIndex() + 1;
                            }
                        } catch (e) {
                            // Error al parsear prescripciones, ignorar
                        }
                    }
                    
                    // Cargar archivos adjuntos existentes si hay
                    const existingAttachmentsDiv = document.getElementById('existingAttachments');
                    if (existingAttachmentsDiv && doc.attachments && doc.attachments !== null && doc.attachments !== 'null' && doc.attachments !== '') {
                        try {
                            let attachments;
                            if (typeof doc.attachments === 'string') {
                                attachments = JSON.parse(doc.attachments);
                            } else {
                                attachments = doc.attachments;
                            }
                            
                            if (attachments && Array.isArray(attachments) && attachments.length > 0) {
                                let html = '<label class="form-label">Archivos Existentes</label><div class="list-group mb-3">';
                                attachments.forEach((att, idx) => {
                                    const fileName = att.fileName || 'Archivo ' + (idx + 1);
                                    const mimeType = att.mimeType || '';
                                    let iconClass = 'fas fa-file';
                                    
                                    if (mimeType.includes('pdf')) {
                                        iconClass = 'fas fa-file-pdf text-danger';
                                    } else if (mimeType.includes('image')) {
                                        iconClass = 'fas fa-file-image text-info';
                                    } else if (mimeType.includes('word') || mimeType.includes('document')) {
                                        iconClass = 'fas fa-file-word text-primary';
                                    }
                                    
                                    let sizeStr = '';
                                    if (att.fileSize) {
                                        const size = parseInt(att.fileSize);
                                        if (size < 1024) {
                                            sizeStr = size + ' B';
                                        } else if (size < 1024 * 1024) {
                                            sizeStr = (size / 1024).toFixed(1) + ' KB';
                                        } else {
                                            sizeStr = (size / (1024 * 1024)).toFixed(1) + ' MB';
                                        }
                                    }
                                    
                                    html += '<div class="list-group-item d-flex justify-content-between align-items-center">';
                                    html += '<span><i class="' + iconClass + ' me-2"></i>' + fileName + '</span>';
                                    html += '<div class="d-flex align-items-center gap-2">';
                                    if (sizeStr) {
                                        html += '<small class="text-muted">' + sizeStr + '</small>';
                                    }
                                    html += '<button class="btn btn-sm btn-danger" onclick="deleteAttachment(' + doc.id + ', ' + idx + ', \'' + fileName.replace(/'/g, "\\'") + '\', false)" title="Eliminar archivo">';
                                    html += '<i class="fas fa-trash"></i>';
                                    html += '</button>';
                                    html += '</div>';
                                    html += '</div>';
                                });
                                html += '</div>';
                                existingAttachmentsDiv.innerHTML = html;
                            }
                        } catch (e) {
                            // Error al parsear archivos adjuntos, ignorar
                        }
                    } else if (existingAttachmentsDiv) {
                        existingAttachmentsDiv.innerHTML = '';
                    }
                    
                    // Asignar tipo de documento ANTES de abrir el modal
                    const docTypeValue = doc.documentType ? String(doc.documentType).trim() : '';
                    
                    // Función para asignar el tipo de documento de manera robusta
                    function setDocumentTypeValue(selectEl, value) {
                        if (!selectEl || !value) return false;
                        
                        try {
                            // Primero, remover selected de todas las opciones
                            Array.from(selectEl.options).forEach(opt => {
                                opt.selected = false;
                                opt.removeAttribute('selected');
                            });
                            
                            // Encontrar y marcar la opción correcta
                            const targetOption = Array.from(selectEl.options).find(opt => opt.value === value);
                            if (targetOption) {
                                // Método 1: Establecer selectedIndex
                                const optionIndex = Array.from(selectEl.options).indexOf(targetOption);
                                selectEl.selectedIndex = optionIndex;
                                
                                // Método 2: Establecer value
                                selectEl.value = value;
                                
                                // Método 3: Marcar la opción directamente
                                targetOption.selected = true;
                                targetOption.setAttribute('selected', 'selected');
                                
                                // Método 4: Forzar actualización mediante clonado (último recurso)
                                if (selectEl.value !== value || selectEl.selectedIndex !== optionIndex) {
                                    const parent = selectEl.parentNode;
                                    
                                    // Crear nuevo select con todos los atributos
                                    const newSelect = document.createElement('select');
                                    newSelect.className = selectEl.className;
                                    newSelect.name = selectEl.name;
                                    newSelect.id = selectEl.id;
                                    newSelect.required = selectEl.required;
                                    Array.from(selectEl.attributes).forEach(attr => {
                                        if (attr.name !== 'class' && attr.name !== 'name' && attr.name !== 'id') {
                                            newSelect.setAttribute(attr.name, attr.value);
                                        }
                                    });
                                    
                                    // Clonar opciones
                                    Array.from(selectEl.options).forEach((opt, idx) => {
                                        const newOpt = opt.cloneNode(true);
                                        if (idx === optionIndex) {
                                            newOpt.selected = true;
                                            newOpt.setAttribute('selected', 'selected');
                                        }
                                        newSelect.appendChild(newOpt);
                                    });
                                    
                                    // Reemplazar el select
                                    parent.replaceChild(newSelect, selectEl);
                                    return true;
                                }
                                
                                return selectEl.value === value || selectEl.selectedIndex === optionIndex;
                            }
                            return false;
                        } catch (e) {
                            return false;
                        }
                    }
                    
                    // Intentar asignar ANTES de abrir el modal
                    if (documentTypeEl && docTypeValue) {
                        setDocumentTypeValue(documentTypeEl, docTypeValue);
                    }
                    
                    // Abrir modal de edición
                    const editModalEl = document.getElementById('addDocumentModal');
                    if (editModalEl) {
                        const editModal = new bootstrap.Modal(editModalEl);
                        
                        // Función para asegurar que el tipo de documento se mantenga después de que el modal esté visible
                        function ensureDocumentType() {
                            // Buscar el select cada vez por si fue reemplazado
                            const currentSelectEl = document.querySelector('select[name="documentType"]');
                            if (currentSelectEl && docTypeValue) {
                                const success = setDocumentTypeValue(currentSelectEl, docTypeValue);
                                if (success) {
                                    // Forzar actualización visual disparando eventos
                                    const changeEvent = new Event('change', { bubbles: true, cancelable: true });
                                    currentSelectEl.dispatchEvent(changeEvent);
                                    
                                    // También disparar eventos nativos del navegador
                                    if (currentSelectEl.dispatchEvent) {
                                        const inputEvent = new Event('input', { bubbles: true });
                                        currentSelectEl.dispatchEvent(inputEvent);
                                    }
                                    
                                    // Actualizar la referencia global si es necesario
                                    if (documentTypeEl !== currentSelectEl) {
                                        documentTypeEl = currentSelectEl;
                                    }
                                }
                            }
                        }
                        
                        // Asegurarse de que el tipo de documento se mantenga después de que el modal esté visible
                        editModalEl.addEventListener('shown.bs.modal', function() {
                            // Usar requestAnimationFrame para asegurar que el navegador procese el cambio visual
                            requestAnimationFrame(function() {
                                ensureDocumentType();
                                // Usar múltiples timeouts para asegurar que Bootstrap haya terminado de renderizar
                                setTimeout(function() {
                                    ensureDocumentType();
                                    setTimeout(ensureDocumentType, 50);
                                    setTimeout(ensureDocumentType, 150);
                                    // Último intento después de más tiempo
                                    setTimeout(ensureDocumentType, 300);
                                }, 10);
                            });
                        }, { once: true });
                        
                        // También intentar inmediatamente después de crear el modal
                        editModal.show();
                        
                        // Intentar asegurar el valor en varios momentos después de mostrar el modal
                        requestAnimationFrame(function() {
                            setTimeout(ensureDocumentType, 20);
                            setTimeout(ensureDocumentType, 100);
                            setTimeout(ensureDocumentType, 200);
                            setTimeout(ensureDocumentType, 350);
                        });
                        
                        // Limpiar URL después de abrir el modal
                        setTimeout(function() {
                            window.history.replaceState({}, document.title, window.location.pathname);
                        }, 100);
                    }
                }
            }
        });
        
        // Limpiar URL cuando se cierra el modal de vista
        document.addEventListener('DOMContentLoaded', function() {
            const viewModal = document.getElementById('viewDocumentModal');
            if (viewModal) {
                viewModal.addEventListener('hidden.bs.modal', function() {
                    window.history.replaceState({}, document.title, window.location.pathname);
                });
            }
            
            // Calcular IMC - configurar event listeners
            document.querySelectorAll('input[name="vitalWeight"], input[name="vitalHeight"]').forEach(input => {
                input.addEventListener('input', calculateBMI);
            });
        });
    </script>
</body>
</html>

