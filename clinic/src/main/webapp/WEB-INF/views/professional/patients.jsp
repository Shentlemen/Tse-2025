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
    <title>Pacientes - Portal Profesional</title>
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
            cursor: pointer;
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
        
        .search-box {
            background: white;
            border-radius: 10px;
            padding: 1.5rem;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 1.5rem;
        }
        
        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(52, 152, 219, 0.25);
        }
        
        .modal-header {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
        }
        
        .info-row {
            padding: 0.75rem 0;
            border-bottom: 1px solid #e9ecef;
        }
        
        .info-row:last-child {
            border-bottom: none;
        }
        
        .info-label {
            font-weight: 600;
            color: #6c757d;
            margin-bottom: 0.25rem;
        }
        
        .info-value {
            color: #212529;
            font-size: 1.05rem;
        }
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="<c:url value='/professional/dashboard'/>">
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
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2 class="mb-0">
                        <i class="fas fa-users me-2"></i>Pacientes
                    </h2>
                </div>

                <!-- Filtros de Búsqueda -->
                <div class="search-box">
                    <form method="GET" action="<c:url value='/professional/patients'/>" class="row g-3">
                        <div class="col-md-10">
                            <div class="input-group">
                                <span class="input-group-text">
                                    <i class="fas fa-search"></i>
                                </span>
                                <input type="text" 
                                       class="form-control" 
                                       name="search" 
                                       placeholder="Buscar por nombre o cédula de identidad..." 
                                       value="${searchTerm}">
                            </div>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="fas fa-search me-2"></i>Buscar
                            </button>
                        </div>
                        <c:if test="${not empty searchTerm}">
                            <div class="col-12">
                                <a href="<c:url value='/professional/patients'/>" class="btn btn-outline-secondary btn-sm">
                                    <i class="fas fa-times me-2"></i>Limpiar filtros
                                </a>
                            </div>
                        </c:if>
                    </form>
                </div>

                <!-- Lista de Pacientes -->
                <div class="card">
                    <div class="card-header bg-white">
                        <h5 class="mb-0">
                            <i class="fas fa-list me-2"></i>Lista de Pacientes
                            <span class="badge bg-primary ms-2">${patients.size()}</span>
                        </h5>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty patients}">
                                <div class="text-center py-5">
                                    <i class="fas fa-users fa-3x text-muted mb-3"></i>
                                    <p class="text-muted">
                                        <c:choose>
                                            <c:when test="${not empty searchTerm}">
                                                No se encontraron pacientes que coincidan con la búsqueda.
                                            </c:when>
                                            <c:otherwise>
                                                No hay pacientes registrados en la clínica.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="row">
                                    <c:forEach var="patient" items="${patients}">
                                        <%
                                            uy.gub.clinic.entity.Patient patientItem = (uy.gub.clinic.entity.Patient) pageContext.getAttribute("patient");
                                            String birthDateStr = "";
                                            if (patientItem != null && patientItem.getBirthDate() != null) {
                                                java.util.Date birthDate = java.sql.Date.valueOf(patientItem.getBirthDate());
                                                pageContext.setAttribute("patientBirthDate", birthDate);
                                                birthDateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(birthDate);
                                            }
                                            pageContext.setAttribute("patientBirthDateStr", birthDateStr);
                                        %>
                                        <div class="col-md-6 mb-3">
                                            <div class="patient-card" 
                                                 data-patient-id="${patient.id}"
                                                 data-patient-name="${fn:escapeXml(patient.fullName)}"
                                                 data-patient-document="${fn:escapeXml(patient.documentNumber != null ? patient.documentNumber : '')}"
                                                 data-patient-inus="${fn:escapeXml(patient.inusId != null ? patient.inusId : '')}"
                                                 data-patient-birthdate="${patientBirthDateStr}"
                                                 data-patient-gender="${fn:escapeXml(patient.gender != null ? patient.gender : '')}"
                                                 data-patient-phone="${fn:escapeXml(patient.phone != null ? patient.phone : '')}"
                                                 data-patient-email="${fn:escapeXml(patient.email != null ? patient.email : '')}"
                                                 data-patient-address="${fn:escapeXml(patient.address != null ? patient.address : '')}"
                                                 onclick="openPatientModal(this)">
                                                <div class="d-flex align-items-center">
                                                    <div class="patient-avatar me-3">
                                                        <c:choose>
                                                            <c:when test="${not empty patient.name and not empty patient.lastName}">
                                                                ${fn:substring(patient.name, 0, 1)}${fn:substring(patient.lastName, 0, 1)}
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:choose>
                                                                    <c:when test="${fn:length(patient.name) >= 2}">
                                                                        ${fn:substring(patient.name, 0, 1)}${fn:substring(patient.name, 1, 2)}
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        ${fn:substring(patient.name, 0, 1)}${fn:substring(patient.name, 0, 1)}
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div class="flex-grow-1">
                                                        <h6 class="mb-1">${patient.fullName}</h6>
                                                        <small class="text-muted">
                                                            <i class="fas fa-id-card me-1"></i>${patient.documentNumber != null ? patient.documentNumber : 'Sin cédula'}
                                                            <c:if test="${patient.birthDate != null}">
                                                                <span class="ms-2">
                                                                    <i class="fas fa-birthday-cake me-1"></i>
                                                                    <c:if test="${patientBirthDate != null}">
                                                                        <fmt:formatDate value="${patientBirthDate}" pattern="dd/MM/yyyy"/>
                                                                    </c:if>
                                                                    <c:if test="${patient.age != null}">
                                                                        (${patient.age} años)
                                                                    </c:if>
                                                                </span>
                                                            </c:if>
                                                        </small>
                                                    </div>
                                                    <div>
                                                        <i class="fas fa-chevron-right text-muted"></i>
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


    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function openPatientModal(element) {
            // Obtener el ID del paciente y redirigir directamente a la página de documentos
            const patientId = element.getAttribute('data-patient-id');
            if (patientId) {
                window.location.href = '<c:url value="/professional/patient-documents"/>?patientId=' + patientId;
            }
        }

    </script>
</body>
</html>

