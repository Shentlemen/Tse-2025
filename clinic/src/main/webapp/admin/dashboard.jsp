<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portal Administración - HCEN Clínica</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #4CAF50;
            --secondary-color: #45a049;
            --accent-color: #e8f5e8;
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
            box-shadow: 0 5px 15px rgba(76, 175, 80, 0.3);
        }
        
        .table {
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 5px 15px rgba(0,0,0,0.08);
        }
        
        .table thead th {
            background: var(--primary-color);
            color: white;
            border: none;
            font-weight: 600;
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
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
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
                                <a class="nav-link active" href="<c:url value='/admin/dashboard'/>">
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
                                <a class="nav-link" href="<c:url value='/admin/documents'/>">
                                    <i class="fas fa-file-medical me-2"></i>Documentos
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/admin/specialties-list'/>">
                                    <i class="fas fa-stethoscope me-2"></i>Especialidades
                                </a>
                            </li>
                            <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                                <li class="nav-item">
                                    <a class="nav-link" href="<c:url value='/admin/super-admin'/>">
                                        <i class="fas fa-crown me-2"></i>Super Admin
                                    </a>
                                </li>
                            </c:if>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/admin/users'/>">
                                    <i class="fas fa-user-cog me-2"></i>Gestión de Usuarios
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
                        <i class="fas fa-tachometer-alt me-2"></i>Dashboard Administrativo
                    </h2>
                    <div class="text-muted">
                        <i class="fas fa-calendar me-1"></i>
                        <fmt:formatDate value="<%=new java.util.Date()%>" pattern="dd/MM/yyyy HH:mm"/>
                    </div>
                </div>
                
                <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                    <div class="alert alert-info">
                        <i class="fas fa-crown me-2"></i>
                        <strong>Vista Completa:</strong> Estás viendo datos de todas las clínicas del sistema.
                    </div>
                </c:if>

                <!-- Estadísticas -->
                <div class="row mb-4">
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-user-md"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${professionalsCount}</h4>
                                    <small class="opacity-75">Profesionales</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-users"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${patientsCount}</h4>
                                    <small class="opacity-75">Pacientes</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-file-medical"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${documentsCount}</h4>
                                    <small class="opacity-75">Documentos</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-stethoscope"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">${specialtiesCount}</h4>
                                    <small class="opacity-75">Especialidades</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Contenido Principal -->
                <div class="row">
                    <!-- Actividad Reciente -->
                    <div class="col-md-8">
                        <div class="card">
                            <div class="card-header bg-white">
                                <h5 class="mb-0">
                                    <i class="fas fa-file-medical me-2"></i>Documentos Recientes
                                </h5>
                            </div>
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${not empty recentDocuments}">
                                        <div class="list-group list-group-flush">
                                            <c:forEach var="doc" items="${recentDocuments}">
                                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                                    <div>
                                                        <i class="fas fa-file-medical text-primary me-2"></i>
                                                        <strong>${doc.title}</strong>
                                                        <br>
                                                        <small class="text-muted">
                                                            <c:if test="${doc.patient != null}">
                                                                ${doc.patient.fullName}
                                                            </c:if>
                                                            <c:if test="${doc.professional != null}">
                                                                - ${doc.professional.fullName}
                                                            </c:if>
                                                            <c:if test="${doc.specialty != null}">
                                                                - ${doc.specialty.name}
                                                            </c:if>
                                                        </small>
                                                        <br>
                                                        <small class="text-muted">
                                                            <%
                                                                uy.gub.clinic.entity.ClinicalDocument docItem = (uy.gub.clinic.entity.ClinicalDocument) pageContext.getAttribute("doc");
                                                                if (docItem != null && docItem.getCreatedAt() != null) {
                                                                    java.util.Date date = java.sql.Timestamp.valueOf(docItem.getCreatedAt());
                                                                    pageContext.setAttribute("docDate", date);
                                                            %>
                                                                <fmt:formatDate value="${docDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                            <%
                                                                }
                                                            %>
                                                        </small>
                                                    </div>
                                                    <a href="<c:url value='/admin/documents'/>" class="btn btn-sm btn-outline-primary">
                                                        <i class="fas fa-eye"></i>
                                                    </a>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-center text-muted py-4">
                                            <i class="fas fa-file-medical fa-3x mb-3 opacity-50"></i>
                                            <p>No hay documentos recientes</p>
                                            <a href="<c:url value='/admin/documents'/>" class="btn btn-primary btn-sm">
                                                <i class="fas fa-plus me-2"></i>Crear Primer Documento
                                            </a>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Acciones Rápidas -->
                    <div class="col-md-4">
                        <div class="card">
                            <div class="card-header bg-white">
                                <h5 class="mb-0">
                                    <i class="fas fa-bolt me-2"></i>Acciones Rápidas
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="d-grid gap-2">
                                    <a href="<c:url value='/admin/professionals'/>#addProfessionalModal" class="btn btn-primary" onclick="openProfessionalModal()">
                                        <i class="fas fa-user-plus me-2"></i>Agregar Profesional
                                    </a>
                                    
                                    <a href="<c:url value='/admin/patients-list'/>#addPatientModal" class="btn btn-outline-primary" onclick="openPatientModal()">
                                        <i class="fas fa-user-plus me-2"></i>Registrar Paciente
                                    </a>
                                    
                                    <a href="<c:url value='/admin/documents'/>" class="btn btn-outline-primary">
                                        <i class="fas fa-file-upload me-2"></i>Subir Documento
                                    </a>
                                </div>
                                
                                <script>
                                    function openProfessionalModal() {
                                        // Guardar la URL para después
                                        sessionStorage.setItem('openProfessionalModal', 'true');
                                    }
                                    
                                    function openPatientModal() {
                                        // Guardar la URL para después
                                        sessionStorage.setItem('openPatientModal', 'true');
                                    }
                                    
                                    // Verificar si necesitamos abrir el modal
                                    if (sessionStorage.getItem('openProfessionalModal') === 'true') {
                                        sessionStorage.removeItem('openProfessionalModal');
                                        // Disparar el evento cuando la página cargue
                                        window.addEventListener('load', function() {
                                            const modal = new bootstrap.Modal(document.getElementById('addProfessionalModal'));
                                            modal.show();
                                        });
                                    }
                                </script>
                                
                                <hr>
                                
                                <h6 class="text-muted mb-3">Estado del Sistema</h6>
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span>Conectividad HCEN</span>
                                    <span class="badge bg-success">
                                        <i class="fas fa-check me-1"></i>Conectado
                                    </span>
                                </div>
                                
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span>Base de Datos</span>
                                    <span class="badge bg-success">
                                        <i class="fas fa-check me-1"></i>Activa
                                    </span>
                                </div>
                                
                                <div class="d-flex justify-content-between align-items-center">
                                    <span>Última Sincronización</span>
                                    <small class="text-muted">Hace 5 min</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
