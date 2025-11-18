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
    <title>Solicitudes - Portal Profesional</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #3498db;
            --secondary-color: #2980b9;
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
        
        .status-rejected {
            background: #f8d7da;
            color: #721c24;
        }
        
        .patient-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(45deg, #e74c3c, #c0392b);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 0.9rem;
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
                                <a class="nav-link" href="<c:url value='/professional/patients'/>">
                                    <i class="fas fa-users me-2"></i>Pacientes
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="<c:url value='/professional/requests'/>">
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
                        <i class="fas fa-exchange-alt me-2"></i>Solicitudes de Acceso Pendientes
                    </h2>
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

                <!-- Lista de Solicitudes -->
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty requests}">
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
                                            <c:forEach var="request" items="${requests}">
                                                <%
                                                    // Preparar datos para el modal - obtener del page scope (variable del forEach)
                                                    uy.gub.clinic.entity.AccessRequest reqItem = (uy.gub.clinic.entity.AccessRequest) pageContext.findAttribute("request");
                                                    String requestDateStr = "";
                                                    if (reqItem != null && reqItem.getRequestedAt() != null) {
                                                        java.time.LocalDateTime requestedAt = reqItem.getRequestedAt();
                                                        java.util.Date requestedDate = java.sql.Timestamp.valueOf(requestedAt);
                                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                                                        requestDateStr = sdf.format(requestedDate);
                                                    }
                                                    pageContext.setAttribute("requestDateStr", requestDateStr);
                                                %>
                                                <tr style="cursor: pointer;" 
                                                    onclick="openRequestModal(this)"
                                                    data-request-id="${request.id}"
                                                    data-patient-name="${fn:escapeXml(request.patient.fullName)}"
                                                    data-patient-document="${fn:escapeXml(request.patient.documentNumber != null ? request.patient.documentNumber : '')}"
                                                    data-patient-inus="${fn:escapeXml(request.patient.inusId != null ? request.patient.inusId : '')}"
                                                    data-specialty="${fn:escapeXml(request.specialty != null ? request.specialty.name : 'Todas las especialidades')}"
                                                    data-reason="${fn:escapeXml(request.reason != null ? request.reason : 'Sin motivo especificado')}"
                                                    data-status="${request.status}"
                                                    data-request-date="${requestDateStr}">
                                                    <td>
                                                        <div class="d-flex align-items-center">
                                                            <div class="patient-avatar me-2">
                                                                <c:set var="patientName" value="${request.patient.fullName}"/>
                                                                <c:set var="initials" value="${fn:substring(patientName, 0, 1)}${fn:substring(patientName, fn:indexOf(patientName, ' ') + 1, fn:indexOf(patientName, ' ') + 2)}"/>
                                                                ${initials}
                                                            </div>
                                                            <div>
                                                                <strong>${request.patient.fullName}</strong>
                                                                <br>
                                                                <small class="text-muted">
                                                                    <i class="fas fa-id-card me-1"></i>
                                                                    ${request.patient.documentNumber != null ? request.patient.documentNumber : 'N/A'}
                                                                </small>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <c:if test="${request.specialty != null}">
                                                            <span class="badge bg-info">${request.specialty.name}</span>
                                                        </c:if>
                                                        <c:if test="${request.specialty == null}">
                                                            <span class="text-muted">Todas las especialidades</span>
                                                        </c:if>
                                                    </td>
                                                    <td>
                                                        ${requestDateStr != null && !requestDateStr.isEmpty() ? requestDateStr : 'N/A'}
                                                    </td>
                                                    <td>
                                                        <c:if test="${not empty request.reason}">
                                                            ${fn:substring(request.reason, 0, 50)}${fn:length(request.reason) > 50 ? '...' : ''}
                                                        </c:if>
                                                        <c:if test="${empty request.reason}">
                                                            <span class="text-muted">Sin motivo especificado</span>
                                                        </c:if>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${request.status == 'PENDING'}">
                                                                <span class="status-badge status-pending">Pendiente</span>
                                                            </c:when>
                                                            <c:when test="${request.status == 'APPROVED'}">
                                                                <span class="status-badge status-approved">Aprobada</span>
                                                            </c:when>
                                                            <c:when test="${request.status == 'REJECTED'}">
                                                                <span class="status-badge status-rejected">Rechazada</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="status-badge">${request.status}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal de Detalles de Solicitud -->
    <div class="modal fade" id="requestDetailModal" tabindex="-1" aria-labelledby="requestDetailModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="requestDetailModalLabel">
                        <i class="fas fa-exchange-alt me-2"></i>Detalles de la Solicitud
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <h6 class="text-primary mb-3">
                                <i class="fas fa-user me-2"></i>Información del Paciente
                            </h6>
                            <div class="mb-2">
                                <strong>Nombre Completo:</strong>
                                <div id="modalPatientName" class="text-muted">-</div>
                            </div>
                            <div class="mb-2">
                                <strong>Cédula de Identidad:</strong>
                                <div id="modalPatientDocument" class="text-muted">-</div>
                            </div>
                            <div class="mb-2">
                                <strong>ID INUS:</strong>
                                <div id="modalPatientInus" class="text-muted">-</div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <h6 class="text-primary mb-3">
                                <i class="fas fa-info-circle me-2"></i>Información de la Solicitud
                            </h6>
                            <div class="mb-2">
                                <strong>Estado:</strong>
                                <div id="modalRequestStatus">-</div>
                            </div>
                            <div class="mb-2">
                                <strong>Especialidad:</strong>
                                <div id="modalRequestSpecialty" class="text-muted">-</div>
                            </div>
                            <div class="mb-2">
                                <strong>Fecha de Solicitud:</strong>
                                <div id="modalRequestDate" class="text-muted">-</div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="mb-3">
                        <h6 class="text-primary mb-2">
                            <i class="fas fa-comment-alt me-2"></i>Motivo de la Solicitud
                        </h6>
                        <div id="modalRequestReason" class="p-3 bg-light rounded" style="min-height: 80px; white-space: pre-wrap;">
                            Sin motivo especificado
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        <i class="fas fa-times me-2"></i>Cerrar
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function openRequestModal(element) {
            // Obtener datos del elemento
            const patientName = element.getAttribute('data-patient-name') || '-';
            const patientDocument = element.getAttribute('data-patient-document') || 'N/A';
            const patientInus = element.getAttribute('data-patient-inus') || 'N/A';
            const specialty = element.getAttribute('data-specialty') || 'Todas las especialidades';
            const reason = element.getAttribute('data-reason') || 'Sin motivo especificado';
            const status = element.getAttribute('data-status') || 'PENDING';
            const requestDate = element.getAttribute('data-request-date') || 'N/A';
            
            // Función helper para mostrar valores o '-'
            function displayValue(value) {
                return (value && value.trim() !== '' && value !== 'N/A') ? value : '-';
            }
            
            // Llenar el modal con los datos
            document.getElementById('modalPatientName').textContent = displayValue(patientName);
            document.getElementById('modalPatientDocument').textContent = displayValue(patientDocument);
            document.getElementById('modalPatientInus').textContent = displayValue(patientInus);
            document.getElementById('modalRequestSpecialty').textContent = specialty;
            document.getElementById('modalRequestDate').textContent = requestDate;
            
            // Mostrar motivo completo (sin truncar)
            const reasonElement = document.getElementById('modalRequestReason');
            if (reason && reason.trim() !== '' && reason !== 'Sin motivo especificado') {
                reasonElement.textContent = reason;
            } else {
                reasonElement.textContent = 'Sin motivo especificado';
            }
            
            // Mostrar estado con badge
            const statusElement = document.getElementById('modalRequestStatus');
            statusElement.innerHTML = '';
            const statusBadge = document.createElement('span');
            statusBadge.className = 'status-badge ';
            
            if (status === 'PENDING') {
                statusBadge.className += 'status-pending';
                statusBadge.textContent = 'Pendiente';
            } else if (status === 'APPROVED') {
                statusBadge.className += 'status-approved';
                statusBadge.textContent = 'Aprobada';
            } else if (status === 'REJECTED') {
                statusBadge.className += 'status-rejected';
                statusBadge.textContent = 'Rechazada';
            } else {
                statusBadge.textContent = status;
            }
            
            statusElement.appendChild(statusBadge);
            
            // Abrir modal
            const modal = new bootstrap.Modal(document.getElementById('requestDetailModal'));
            modal.show();
        }
    </script>
</body>
</html>

