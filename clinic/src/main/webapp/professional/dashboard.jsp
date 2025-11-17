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
    <title>Portal Profesional - HCEN Clínica</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #3498db;
            --secondary-color: #2980b9;
            --accent-color: #e8f4fd;
        }
        
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .navbar-brand {
            font-weight: 700;
            color: var(--primary-color) !important;
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
            transition: transform 0.3s ease;
        }
        
        .card:hover {
            transform: translateY(-5px);
        }
        
        .stat-card {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            border-radius: 15px;
        }
        
        .stat-icon {
            background: rgba(255,255,255,0.2);
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }
        
        .btn-primary {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            border: none;
            border-radius: 25px;
            padding: 10px 25px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(52, 152, 219, 0.3);
        }
        
        .patient-card {
            background: white;
            border-radius: 10px;
            padding: 1rem;
            margin-bottom: 1rem;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
        }
        
        .patient-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(0,0,0,0.15);
        }
        
        .patient-avatar {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: linear-gradient(45deg, #e74c3c, #c0392b);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
        }
        
        .document-item {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 0.75rem;
            margin-bottom: 0.5rem;
            border-left: 4px solid var(--primary-color);
        }
        
        .status-badge {
            border-radius: 20px;
            padding: 4px 12px;
            font-size: 0.8rem;
            font-weight: 600;
        }
        
        .status-pending {
            background: #fff3cd;
            color: #856404;
        }
        
        .status-approved {
            background: #d4edda;
            color: #155724;
        }
        
        .status-denied {
            background: #f8d7da;
            color: #721c24;
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
                                <a class="nav-link active" href="<c:url value='/professional/dashboard'/>">
                                    <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/patients'/>">
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
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">
                        <i class="fas fa-tachometer-alt me-2"></i>Dashboard Profesional
                    </h2>
                    <div class="text-muted">
                        <i class="fas fa-calendar me-1"></i>
                        <fmt:formatDate value="<%=new java.util.Date()%>" pattern="dd/MM/yyyy HH:mm"/>
                    </div>
                </div>

                <!-- Estadísticas -->
                <div class="row mb-4">
                    <div class="col-md-4">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-file-medical"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${totalDocuments != null ? totalDocuments : 0}</h4>
                                    <small class="opacity-75">Documentos Creados</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-4">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-clock"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${pendingRequests != null ? pendingRequests : 0}</h4>
                                    <small class="opacity-75">Solicitudes Pendientes</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-4">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3" style="background: rgba(255,255,255,0.3); border: 2px solid rgba(255,255,255,0.5);">
                                    <i class="fas fa-file-lines"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${recentDocuments != null ? recentDocuments.size() : 0}</h4>
                                    <small class="opacity-75">Documentos Recientes</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Contenido Principal -->
                <div class="row">
                    <!-- Documentos Recientes -->
                    <div class="col-md-12">
                        <div class="card">
                            <div class="card-header bg-white">
                                <h5 class="mb-0">
                                    <i class="fas fa-file-lines me-2" style="color: #3498db;"></i>Documentos Recientes
                                </h5>
                            </div>
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${empty recentDocuments}">
                                        <div class="alert alert-info text-center" role="alert">
                                            <i class="fas fa-file-lines fa-3x mb-3 d-block" style="color: #3498db; opacity: 0.6;"></i>
                                            <h5>No hay documentos recientes</h5>
                                            <p class="mb-0">Aún no has creado ningún documento clínico.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="row">
                                            <c:forEach var="doc" items="${recentDocuments}">
                                                <div class="col-md-6 mb-3">
                                                    <div class="document-item">
                                                        <div class="d-flex justify-content-between align-items-center">
                                                            <div>
                                                                <strong>${doc.title != null ? doc.title : 'Sin título'}</strong>
                                                                <br>
                                                                <small class="text-muted">
                                                                    <i class="fas fa-user me-1"></i>${doc.patient.fullName}
                                                                    <c:if test="${doc.dateOfVisit != null}">
                                                                        <%
                                                                            uy.gub.clinic.entity.ClinicalDocument docItem = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                                                            if (docItem != null && docItem.getDateOfVisit() != null) {
                                                                                java.util.Date visitDate = java.sql.Date.valueOf(docItem.getDateOfVisit());
                                                                                pageContext.setAttribute("docVisitDate", visitDate);
                                                                        %>
                                                                            - <fmt:formatDate value="${docVisitDate}" pattern="dd/MM/yyyy"/>
                                                                        <%
                                                                            }
                                                                        %>
                                                                    </c:if>
                                                                </small>
                                                                <c:if test="${not empty doc.documentType}">
                                                                    <br><span class="badge bg-info mt-1">${doc.documentType}</span>
                                                                </c:if>
                                                            </div>
                                                            <a href="<c:url value='/professional/patient-documents'/>?patientId=${doc.patient.id}&action=view&documentId=${doc.id}" 
                                                               class="btn btn-sm btn-outline-primary">
                                                                <i class="fas fa-eye me-1"></i>Ver
                                                            </a>
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

                <!-- Solicitudes de Acceso -->
                <div class="row mt-4">
                    <div class="col-12">
                        <div class="card">
                            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                                <h5 class="mb-0">
                                    <i class="fas fa-exchange-alt me-2"></i>Solicitudes de Acceso Pendientes
                                </h5>
                                <a href="<c:url value='/professional/requests'/>" class="btn btn-sm btn-outline-primary">
                                    <i class="fas fa-eye me-2"></i>Ver Todas
                                </a>
                            </div>
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${empty pendingRequestsList}">
                                        <div class="alert alert-info text-center" role="alert">
                                            <i class="fas fa-inbox fa-3x mb-3 d-block"></i>
                                            <h5>No hay solicitudes pendientes</h5>
                                            <p class="mb-0">No tiene solicitudes de acceso a documentos pendientes de aprobación.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="table-responsive">
                                            <table class="table table-hover">
                                                <thead class="table-light">
                                                    <tr>
                                                        <th>Paciente</th>
                                                        <th>Especialidad</th>
                                                        <th>Fecha Solicitud</th>
                                                        <th>Motivo</th>
                                                        <th>Estado</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="request" items="${pendingRequestsList}" begin="0" end="4">
                                                        <tr>
                                                            <td>
                                                                <div class="d-flex align-items-center">
                                                                    <div class="patient-avatar me-2" style="width: 30px; height: 30px; font-size: 0.8rem;">
                                                                        <c:set var="patientName" value="${request.patient.fullName}"/>
                                                                        <c:set var="initials" value="${fn:substring(patientName, 0, 1)}${fn:substring(patientName, fn:indexOf(patientName, ' ') + 1, fn:indexOf(patientName, ' ') + 2)}"/>
                                                                        ${initials}
                                                                    </div>
                                                                    <span>${request.patient.fullName}</span>
                                                                </div>
                                                            </td>
                                                            <td>
                                                                <c:if test="${request.specialty != null}">
                                                                    <span class="badge bg-info">${request.specialty.name}</span>
                                                                </c:if>
                                                                <c:if test="${request.specialty == null}">
                                                                    <span class="text-muted">Todas</span>
                                                                </c:if>
                                                            </td>
                                                            <td>
                                                                <%
                                                                    uy.gub.clinic.entity.AccessRequest reqItem = (uy.gub.clinic.entity.AccessRequest) pageContext.getAttribute("request");
                                                                    if (reqItem != null && reqItem.getRequestedAt() != null) {
                                                                        java.time.LocalDateTime requestedAt = reqItem.getRequestedAt();
                                                                        java.util.Date requestedDate = java.sql.Timestamp.valueOf(requestedAt);
                                                                        pageContext.setAttribute("requestedDate", requestedDate);
                                                                %>
                                                                    <fmt:formatDate value="${requestedDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                                <%
                                                                    } else {
                                                                %>
                                                                    <span class="text-muted">N/A</span>
                                                                <%
                                                                    }
                                                                %>
                                                            </td>
                                                            <td>
                                                                <c:if test="${not empty request.reason}">
                                                                    ${fn:substring(request.reason, 0, 40)}${fn:length(request.reason) > 40 ? '...' : ''}
                                                                </c:if>
                                                                <c:if test="${empty request.reason}">
                                                                    <span class="text-muted">Sin motivo</span>
                                                                </c:if>
                                                            </td>
                                                            <td>
                                                                <span class="status-badge status-pending">Pendiente</span>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                        <c:if test="${pendingRequestsList.size() > 5}">
                                            <div class="text-center mt-3">
                                                <a href="<c:url value='/professional/requests'/>" class="btn btn-outline-primary">
                                                    <i class="fas fa-eye me-2"></i>Ver Todas las Solicitudes (${pendingRequestsList.size()})
                                                </a>
                                            </div>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal para Nuevo Documento -->
    <div class="modal fade" id="newDocumentModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-file-medical me-2"></i>Crear Nuevo Documento
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form>
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="patientSelect" class="form-label">Paciente *</label>
                                    <select class="form-select" id="patientSelect" required>
                                        <option value="">Seleccionar paciente</option>
                                        <option value="1">Ana Silva (12345678)</option>
                                        <option value="2">Roberto Martínez (87654321)</option>
                                        <option value="3">Lucía Fernández (11223344)</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="documentType" class="form-label">Tipo de Documento *</label>
                                    <select class="form-select" id="documentType" required>
                                        <option value="">Seleccionar tipo</option>
                                        <option value="consulta">Consulta Médica</option>
                                        <option value="ecg">Electrocardiograma</option>
                                        <option value="eco">Ecocardiograma</option>
                                        <option value="lab">Laboratorio</option>
                                        <option value="otro">Otro</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="documentTitle" class="form-label">Título del Documento *</label>
                            <input type="text" class="form-control" id="documentTitle" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="documentContent" class="form-label">Contenido *</label>
                            <textarea class="form-control" id="documentContent" rows="6" required></textarea>
                        </div>
                        
                        <div class="mb-3">
                            <label for="documentFile" class="form-label">Archivo Adjunto</label>
                            <input type="file" class="form-control" id="documentFile" accept=".pdf,.jpg,.jpeg,.png">
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

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
