<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Profesionales - HCEN Clínica</title>
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
        
        .badge-specialty {
            background: linear-gradient(45deg, #e3f2fd, #bbdefb);
            color: #1976d2;
            border-radius: 20px;
            padding: 5px 12px;
            font-size: 0.85em;
        }
        
        .status-active {
            color: #4caf50;
        }
        
        .status-inactive {
            color: #f44336;
        }
        
        /* Estilos profesionales para botones de acción */
        .btn-action-view {
            background-color: #f8f9fa;
            border: 1px solid #dee2e6;
            color: #495057;
            transition: all 0.3s ease;
        }
        
        .btn-action-view:hover {
            background-color: #e9ecef;
            border-color: #adb5bd;
            color: #212529;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .btn-action-edit {
            background-color: #fff3cd;
            border: 1px solid #ffc107;
            color: #856404;
            transition: all 0.3s ease;
        }
        
        .btn-action-edit:hover {
            background-color: #ffc107;
            border-color: #ffc107;
            color: #000;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(255, 193, 7, 0.3);
        }
        
        .btn-action-delete {
            background-color: #f8d7da;
            border: 1px solid #dc3545;
            color: #721c24;
            transition: all 0.3s ease;
        }
        
        .btn-action-delete:hover {
            background-color: #dc3545;
            border-color: #dc3545;
            color: #fff;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(220, 53, 69, 0.3);
        }
        
        .btn-action-activate {
            background-color: #d1e7dd;
            border: 1px solid #198754;
            color: #0f5132;
            transition: all 0.3s ease;
        }
        
        .btn-action-activate:hover {
            background-color: #198754;
            border-color: #198754;
            color: #fff;
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(25, 135, 84, 0.3);
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
                                <a class="nav-link" href="<c:url value='/admin/dashboard'/>">
                                    <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link active" href="<c:url value='/admin/professionals'/>">
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
                    <div>
                        <h2 class="mb-0">
                            <i class="fas fa-user-md me-2"></i>Gestión de Profesionales
                            <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                                <span class="badge bg-warning ms-2">
                                    <i class="fas fa-crown me-1"></i>Vista Completa
                                </span>
                            </c:if>
                        </h2>
                        <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                            <p class="text-muted mb-0">Mostrando profesionales de todas las clínicas</p>
                        </c:if>
                    </div>
                    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addProfessionalModal">
                        <i class="fas fa-plus me-2"></i>Agregar Profesional
                    </button>
                </div>
                
                <!-- Mensajes de éxito/error -->
                <c:if test="${param.success != null}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <c:choose>
                            <c:when test="${param.success == 'registered'}">
                                <i class="fas fa-check-circle me-2"></i>Profesional registrado exitosamente
                            </c:when>
                            <c:when test="${param.success == 'updated'}">
                                <i class="fas fa-check-circle me-2"></i>Profesional actualizado exitosamente
                            </c:when>
                            <c:when test="${param.success == 'deleted'}">
                                <i class="fas fa-check-circle me-2"></i>Profesional eliminado exitosamente
                            </c:when>
                            <c:when test="${param.success == 'activated'}">
                                <i class="fas fa-check-circle me-2"></i>Profesional activado exitosamente
                            </c:when>
                            <c:when test="${param.success == 'deactivated'}">
                                <i class="fas fa-check-circle me-2"></i>Profesional desactivado exitosamente
                            </c:when>
                        </c:choose>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <!-- Mensajes de éxito y error -->
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
                        <div class="row">
                            <div class="col-md-4">
                                <label for="searchInput" class="form-label">Buscar Profesional</label>
                                <input type="text" class="form-control" id="searchInput" placeholder="Nombre, apellido o matrícula...">
                            </div>
                            <div class="col-md-3">
                                <label for="specialtyFilter" class="form-label">Especialidad</label>
                                <select class="form-select" id="specialtyFilter">
                                    <option value="">Todas las especialidades</option>
                                    <c:forEach var="specialty" items="${specialties}">
                                        <option value="${fn:toLowerCase(fn:replace(specialty.name, 'í', 'i'))}">${specialty.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label for="statusFilter" class="form-label">Estado</label>
                                <select class="form-select" id="statusFilter">
                                    <option value="">Todos</option>
                                    <option value="active">Activo</option>
                                    <option value="inactive">Inactivo</option>
                                </select>
                            </div>
                            <div class="col-md-2 d-flex align-items-end">
                                <button class="btn btn-outline-secondary w-100" onclick="filterProfessionals()">
                                    <i class="fas fa-filter me-2"></i>Filtrar
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Tabla de Profesionales -->
                <div class="card">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Nombre</th>
                                        <th>Matrícula</th>
                                        <th>Especialidad</th>
                                        <th>Email</th>
                                        <th>Teléfono</th>
                                        <th>Estado</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty professionals}">
                                            <c:forEach var="professional" items="${professionals}">
                                                <tr>
                                                    <td>
                                                        <div class="d-flex align-items-center">
                                                            <div class="avatar-circle me-3">
                                                                ${fn:substring(professional.name, 0, 1)}${fn:substring(professional.lastName, 0, 1)}
                                                            </div>
                                                            <div>
                                                                <strong>${professional.fullName}</strong>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td><code>${professional.licenseNumber}</code></td>
                                                    <td><span class="badge-specialty">${professional.specialty.name}</span></td>
                                                    <td>${professional.email}</td>
                                                    <td>${professional.phone}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${professional.active}">
                                                                <i class="fas fa-check-circle status-active"></i> Activo
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fas fa-times-circle status-inactive"></i> Inactivo
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <div class="btn-group" role="group">
                                                            <button class="btn btn-sm btn-action-view" title="Ver detalles" 
                                                                    onclick="viewProfessional('${professional.id}')">
                                                                <i class="fas fa-eye"></i>
                                                            </button>
                                                            <button class="btn btn-sm btn-action-edit" title="Editar" 
                                                                    onclick="editProfessional('${professional.id}')">
                                                                <i class="fas fa-edit"></i>
                                                            </button>
                                                            <c:choose>
                                                                <c:when test="${professional.active}">
                                                                    <button class="btn btn-sm btn-action-delete" title="Desactivar" 
                                                                            onclick="toggleProfessionalStatus('${professional.id}', false)">
                                                                        <i class="fas fa-ban"></i>
                                                                    </button>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <button class="btn btn-sm btn-action-activate" title="Activar" 
                                                                            onclick="toggleProfessionalStatus('${professional.id}', true)">
                                                                        <i class="fas fa-check"></i>
                                                                    </button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="7" class="text-center text-muted">
                                                    <i class="fas fa-user-md fa-3x mb-3"></i>
                                                    <p>No hay profesionales registrados</p>
                                                    <p class="small">Haz clic en "Agregar Profesional" para comenzar</p>
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    
                    <!-- Paginación -->
                    <div class="card-footer bg-white">
                        <div class="d-flex justify-content-between align-items-center">
                            <small class="text-muted">
                                Mostrando 4 de 4 profesionales
                            </small>
                            <nav>
                                <ul class="pagination pagination-sm mb-0">
                                    <li class="page-item disabled">
                                        <a class="page-link" href="#">Anterior</a>
                                    </li>
                                    <li class="page-item active">
                                        <a class="page-link" href="#">1</a>
                                    </li>
                                    <li class="page-item disabled">
                                        <a class="page-link" href="#">Siguiente</a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal para Agregar Profesional -->
    <div class="modal fade" id="addProfessionalModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-plus me-2"></i>Agregar Profesional
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form action="${pageContext.request.contextPath}/admin/professionals" method="post">
                    <input type="hidden" name="action" value="register">
                    
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="name" class="form-label">Nombre *</label>
                                    <input type="text" class="form-control" id="name" name="name" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="lastName" class="form-label">Apellido *</label>
                                    <input type="text" class="form-control" id="lastName" name="lastName" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="licenseNumber" class="form-label">Matrícula *</label>
                                    <input type="text" class="form-control" id="licenseNumber" name="licenseNumber" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="specialtyId" class="form-label">Especialidad *</label>
                                    <select class="form-select" id="specialtyId" name="specialtyId" required>
                                        <option value="">Seleccionar especialidad</option>
                                        <c:forEach var="specialty" items="${specialties}">
                                            <option value="${specialty.id}">${specialty.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="email" class="form-label">Email</label>
                                    <input type="email" class="form-control" id="email" name="email">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="phone" class="form-label">Teléfono</label>
                                    <input type="tel" class="form-control" id="phone" name="phone">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Guardar Profesional
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal para Ver Detalles del Profesional -->
    <div class="modal fade" id="viewProfessionalModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-md me-2"></i>Detalles del Profesional
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="professionalDetails">
                        <!-- Los detalles se cargarán aquí dinámicamente -->
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        <i class="fas fa-times me-2"></i>Cerrar
                    </button>
                    <button type="button" class="btn btn-warning" onclick="editProfessionalFromModal()">
                        <i class="fas fa-edit me-2"></i>Editar
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal para Editar Profesional -->
    <div class="modal fade" id="editProfessionalModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-warning text-white">
                    <h5 class="modal-title">
                        <i class="fas fa-user-edit me-2"></i>Editar Profesional
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="${pageContext.request.contextPath}/admin/professionals" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" id="editProfessionalId">
                    
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editName" class="form-label">Nombre *</label>
                                    <input type="text" class="form-control" id="editName" name="name" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editLastName" class="form-label">Apellido *</label>
                                    <input type="text" class="form-control" id="editLastName" name="lastName" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editLicenseNumber" class="form-label">Matrícula *</label>
                                    <input type="text" class="form-control" id="editLicenseNumber" name="licenseNumber" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editSpecialtyId" class="form-label">Especialidad *</label>
                                    <select class="form-select" id="editSpecialtyId" name="specialtyId" required>
                                        <option value="">Seleccionar especialidad</option>
                                        <c:forEach var="specialty" items="${specialties}">
                                            <option value="${specialty.id}">${specialty.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editEmail" class="form-label">Email</label>
                                    <input type="email" class="form-control" id="editEmail" name="email">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editPhone" class="form-label">Teléfono</label>
                                    <input type="tel" class="form-control" id="editPhone" name="phone">
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                            <i class="fas fa-times me-2"></i>Cancelar
                        </button>
                        <button type="submit" class="btn btn-warning">
                            <i class="fas fa-save me-2"></i>Actualizar Profesional
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <style>
        .avatar-circle {
            width: 42px;
            height: 42px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 0.85rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #ffffff;
            box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);
            border: 2px solid rgba(255, 255, 255, 0.3);
            transition: all 0.3s ease;
        }
        
        .avatar-circle:hover {
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
        }
        
        /* Estilos para el modal de detalles */
        .professional-avatar-large {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 2.2rem;
            margin: 0 auto 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #ffffff;
            box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
            border: 3px solid rgba(255, 255, 255, 0.4);
        }
        
        .detail-item {
            padding: 12px 0;
            border-bottom: 1px solid #eee;
        }
        
        .detail-item:last-child {
            border-bottom: none;
        }
        
        .detail-label {
            font-weight: 600;
            color: #666;
            margin-bottom: 5px;
        }
        
        .detail-value {
            color: #333;
            font-size: 1.1rem;
        }
        
        .status-badge {
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 0.9rem;
            font-weight: 600;
        }
        
        .status-active-badge {
            background-color: #d4edda;
            color: #155724;
        }
        
        .status-inactive-badge {
            background-color: #f8d7da;
            color: #721c24;
        }
        
        .specialty-badge {
            background: linear-gradient(45deg, #e3f2fd, #bbdefb);
            color: #1976d2;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 600;
        }
    </style>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // Variable global para almacenar el ID del profesional actual
        let currentProfessionalId = null;
        
        // Función para ver detalles del profesional
        function viewProfessional(id) {
            currentProfessionalId = id;
            
            // Buscar los datos del profesional en la tabla
            const professionalData = findProfessionalData(id);
            
            if (professionalData) {
                showProfessionalDetails(professionalData);
            } else {
                alert('No se encontraron los datos del profesional');
            }
        }
        
        // Función para buscar los datos del profesional en la tabla
        function findProfessionalData(id) {
            const rows = document.querySelectorAll('tbody tr');
            
            for (let row of rows) {
                const buttons = row.querySelectorAll('button[onclick*="viewProfessional"]');
                for (let button of buttons) {
                    const onclickAttr = button.getAttribute('onclick');
                    if (onclickAttr && onclickAttr.includes("'" + id + "'")) {
                        return extractProfessionalDataFromRow(row);
                    }
                }
            }
            return null;
        }
        
        // Función para extraer los datos del profesional de una fila de la tabla
        function extractProfessionalDataFromRow(row) {
            const cells = row.querySelectorAll('td');
            
            // Extraer nombre completo
            const nameElement = cells[0].querySelector('strong');
            const fullName = nameElement ? nameElement.textContent.trim() : 'N/A';
            
            // Extraer matrícula
            const licenseElement = cells[1].querySelector('code');
            const licenseNumber = licenseElement ? licenseElement.textContent.trim() : 'N/A';
            
            // Extraer especialidad
            const specialtyElement = cells[2].querySelector('.badge-specialty');
            const specialty = specialtyElement ? specialtyElement.textContent.trim() : 'N/A';
            
            // Extraer email
            const email = cells[3].textContent.trim();
            
            // Extraer teléfono
            const phone = cells[4].textContent.trim();
            
            // Extraer estado
            const statusElement = cells[5];
            const isActive = statusElement.textContent.includes('Activo');
            const status = isActive ? 'Activo' : 'Inactivo';
            
            // Extraer ID del profesional del botón
            const btnElement = row.querySelector('button[onclick*="viewProfessional"]');
            let professionalId = null;
            if (btnElement) {
                const onclickAttr = btnElement.getAttribute('onclick');
                const match = onclickAttr.match(/viewProfessional\('(\d+)'\)/);
                if (match) {
                    professionalId = match[1];
                }
            }
            
            const professionalData = {
                fullName: fullName,
                licenseNumber: licenseNumber,
                specialty: specialty,
                email: email,
                phone: phone,
                status: status,
                isActive: isActive,
                id: professionalId
            };
            
            return professionalData;
        }
        
        // Función para mostrar los detalles del profesional en el modal
        function showProfessionalDetails(data) {
            const modalBody = document.getElementById('professionalDetails');
            
            // Generar las iniciales del nombre
            const nameParts = data.fullName.split(' ');
            const initials = nameParts.map(part => part.charAt(0)).join('').toUpperCase();
            
            modalBody.innerHTML = 
                '<div class="text-center mb-4">' +
                    '<div class="professional-avatar-large">' +
                        initials +
                    '</div>' +
                    '<h4 class="mt-3 mb-1">' + data.fullName + '</h4>' +
                    '<p class="text-muted mb-0">Profesional de la Salud' + (data.id ? ' (ID: ' + data.id + ')' : '') + '</p>' +
                '</div>' +
                
                '<div class="row">' +
                    '<div class="col-md-6">' +
                        '<div class="detail-item">' +
                            '<div class="detail-label">' +
                                '<i class="fas fa-id-card me-2"></i>Matrícula Profesional' +
                            '</div>' +
                            '<div class="detail-value">' +
                                '<code>' + (data.licenseNumber || 'N/A') + '</code>' +
                            '</div>' +
                        '</div>' +
                        
                        '<div class="detail-item">' +
                            '<div class="detail-label">' +
                                '<i class="fas fa-stethoscope me-2"></i>Especialidad' +
                            '</div>' +
                            '<div class="detail-value">' +
                                '<span class="specialty-badge">' + (data.specialty || 'N/A') + '</span>' +
                            '</div>' +
                        '</div>' +
                        
                        '<div class="detail-item">' +
                            '<div class="detail-label">' +
                                '<i class="fas fa-envelope me-2"></i>Correo Electrónico' +
                            '</div>' +
                            '<div class="detail-value">' + (data.email || 'N/A') + '</div>' +
                        '</div>' +
                    '</div>' +
                    
                    '<div class="col-md-6">' +
                        '<div class="detail-item">' +
                            '<div class="detail-label">' +
                                '<i class="fas fa-phone me-2"></i>Teléfono' +
                            '</div>' +
                            '<div class="detail-value">' + (data.phone || 'N/A') + '</div>' +
                        '</div>' +
                        
                        '<div class="detail-item">' +
                            '<div class="detail-label">' +
                                '<i class="fas fa-info-circle me-2"></i>Estado' +
                            '</div>' +
                            '<div class="detail-value">' +
                                '<span class="status-badge ' + (data.isActive ? 'status-active-badge' : 'status-inactive-badge') + '">' +
                                    '<i class="fas ' + (data.isActive ? 'fa-check-circle' : 'fa-times-circle') + ' me-1"></i>' +
                                    data.status +
                                '</span>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                '</div>';
            
            // Mostrar el modal
            const modal = new bootstrap.Modal(document.getElementById('viewProfessionalModal'));
            modal.show();
        }
        
        // Función para editar desde el modal
        function editProfessionalFromModal() {
            if (currentProfessionalId) {
                editProfessional(currentProfessionalId);
            }
        }
        
        // Función para editar profesional
        function editProfessional(id) {
            // Buscar los datos del profesional en la tabla
            const professionalData = findProfessionalData(id);
            
            if (professionalData) {
                loadProfessionalDataInEditModal(professionalData, id);
            } else {
                alert('No se encontraron los datos del profesional');
            }
        }
        
        // Función para cargar los datos del profesional en el modal de edición
        function loadProfessionalDataInEditModal(data, id) {
            // Establecer el ID del profesional
            document.getElementById('editProfessionalId').value = id;
            
            // Separar nombre y apellido
            const nameParts = data.fullName.split(' ');
            const firstName = nameParts[0] || '';
            const lastName = nameParts.slice(1).join(' ') || '';
            
            // Cargar los datos en los campos del formulario
            document.getElementById('editName').value = firstName;
            document.getElementById('editLastName').value = lastName;
            document.getElementById('editLicenseNumber').value = data.licenseNumber;
            document.getElementById('editEmail').value = data.email;
            document.getElementById('editPhone').value = data.phone;
            
            // Mapear especialidad a ID (esto es temporal, después se puede mejorar)
            const specialtyMapping = {
                'Medicina General': '1',
                'Cardiología': '2',
                'Dermatología': '3',
                'Ginecología': '4',
                'Pediatría': '5',
                'Oftalmología': '6',
                'Otorrinolaringología': '7',
                'Psicología': '8',
                'Traumatología': '9',
                'Urología': '10'
            };
            
            const specialtyId = specialtyMapping[data.specialty] || '';
            document.getElementById('editSpecialtyId').value = specialtyId;
            
            // Mostrar el modal
            const modal = new bootstrap.Modal(document.getElementById('editProfessionalModal'));
            modal.show();
        }
        
        // Función para cambiar el estado del profesional (activar/desactivar)
        function toggleProfessionalStatus(id, activate) {
            if (confirm(activate ? '¿Activar este profesional?' : '¿Desactivar este profesional?')) {
                // Crear un formulario oculto para enviar la acción
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/admin/professionals';
                
                const actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = activate ? 'activate' : 'deactivate';
                
                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'id';
                idInput.value = id;
                
                form.appendChild(actionInput);
                form.appendChild(idInput);
                document.body.appendChild(form);
                form.submit();
            }
        }
        
        // Función para filtrar profesionales
        function filterProfessionals() {
            const searchInput = document.getElementById('searchInput').value.toLowerCase();
            const specialtyFilter = document.getElementById('specialtyFilter').value;
            const statusFilter = document.getElementById('statusFilter').value;
            
            const rows = document.querySelectorAll('tbody tr');
            
            rows.forEach(row => {
                // Extraer datos de la fila
                const cells = row.querySelectorAll('td');
                const name = cells[0].querySelector('strong').textContent.toLowerCase();
                const license = cells[1].querySelector('code').textContent.toLowerCase();
                
                // Verificar especialidad
                const specialtyBadge = cells[2].querySelector('.badge-specialty');
                const professionalSpecialty = specialtyBadge ? specialtyBadge.textContent.toLowerCase().trim() : '';
                
                // Verificar estado
                const statusText = cells[5].textContent.trim();
                const isActive = statusText.includes('Activo');
                
                // Aplicar filtros
                let shouldShow = true;
                
                // Filtro de búsqueda
                if (searchInput && !name.includes(searchInput) && !license.includes(searchInput)) {
                    shouldShow = false;
                }
                
                // Filtro de especialidad
                if (specialtyFilter) {
                    const specialtyNormalized = specialtyFilter.replace(/í/g, 'i').replace(/ó/g, 'o');
                    const professionalSpecialtyNormalized = professionalSpecialty.replace(/í/g, 'i').replace(/ó/g, 'o');
                    if (!professionalSpecialtyNormalized.includes(specialtyNormalized)) {
                        shouldShow = false;
                    }
                }
                
                // Filtro de estado
                if (statusFilter) {
                    if ((statusFilter === 'active' && !isActive) ||
                        (statusFilter === 'inactive' && isActive)) {
                        shouldShow = false;
                    }
                }
                
                // Mostrar u ocultar fila
                row.style.display = shouldShow ? '' : 'none';
            });
        }
        
        // Agregar event listener para búsqueda en tiempo real
        document.addEventListener('DOMContentLoaded', function() {
            const searchInput = document.getElementById('searchInput');
            if (searchInput) {
                searchInput.addEventListener('input', filterProfessionals);
            }
            
            // Verificar si debemos abrir el modal de agregar profesional desde el dashboard
            if (window.location.hash === '#addProfessionalModal' || sessionStorage.getItem('openProfessionalModal') === 'true') {
                sessionStorage.removeItem('openProfessionalModal');
                const modal = new bootstrap.Modal(document.getElementById('addProfessionalModal'));
                modal.show();
            }
        });
        
        // Asegurar que las funciones estén disponibles globalmente
        window.viewProfessional = viewProfessional;
        window.editProfessional = editProfessional;
        window.toggleProfessionalStatus = toggleProfessionalStatus;
        window.editProfessionalFromModal = editProfessionalFromModal;
        window.filterProfessionals = filterProfessionals;
    </script>
</body>
</html>
