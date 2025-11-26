<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Super Administrador - HCEN Clínica</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #6f42c1;
            --secondary-color: #5a32a3;
            --accent-color: #e8e3f3;
        }
        
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .navbar-brand {
            font-weight: 700;
            color: var(--primary-color) !important;
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
            box-shadow: 0 8px 20px rgba(111, 66, 193, 0.3);
        }
        
        .btn-success {
            background: linear-gradient(45deg, #28a745, #20c997);
            border: none;
            border-radius: 20px;
            padding: 8px 20px;
            font-weight: 600;
        }
        
        .btn-warning {
            background: linear-gradient(45deg, #ffc107, #fd7e14);
            border: none;
            border-radius: 20px;
            padding: 8px 20px;
            font-weight: 600;
        }
        
        .btn-danger {
            background: linear-gradient(45deg, #dc3545, #e83e8c);
            border: none;
            border-radius: 20px;
            padding: 8px 20px;
            font-weight: 600;
        }
        
        .super-admin-header {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            border-radius: 15px;
            padding: 2rem;
            margin-bottom: 2rem;
        }
        
        .clinic-card {
            border-left: 4px solid var(--primary-color);
            transition: transform 0.3s ease;
        }
        
        .clinic-card:hover {
            transform: translateY(-5px);
        }
        
        .stats-card {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            border-radius: 15px;
            padding: 1.5rem;
        }
        
        .stats-number {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }
        
        .stats-label {
            font-size: 0.9rem;
            opacity: 0.9;
        }
        
        .modal-header {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            color: white;
            border-radius: 15px 15px 0 0;
        }
        
        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(111, 66, 193, 0.25);
        }
        
        .form-select:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(111, 66, 193, 0.25);
        }
        
        .alert {
            border-radius: 10px;
            border: none;
        }
        
        .badge-super-admin {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            color: white;
            padding: 8px 12px;
            border-radius: 20px;
            font-weight: 600;
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
    </style>
</head>
<body>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="<c:url value='/admin/dashboard'/>">
                <i class="fas fa-crown me-2"></i>HCEN - Super Administrador
            </a>
            
            <div class="navbar-nav ms-auto">
                <div class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="fas fa-user-shield me-2"></i>${sessionScope.user}
                        <span class="badge badge-super-admin ms-2">Super Admin</span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a class="dropdown-item" href="<c:url value='/admin/dashboard'/>">
                            <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                        </a></li>
                        <li><a class="dropdown-item" href="<c:url value='/admin/users'/>">
                            <i class="fas fa-users me-2"></i>Gestión de Usuarios
                        </a></li>
                        <li><hr class="dropdown-divider"></li>
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
                                    <a class="nav-link active" href="<c:url value='/admin/super-admin'/>">
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
                <!-- Header Super Admin -->
                <div class="super-admin-header">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h1 class="mb-2">
                                <i class="fas fa-crown me-3"></i>Super Administrador
                            </h1>
                            <p class="mb-0 opacity-75">Gestión global del sistema HCEN - Registro de administradores iniciales</p>
                        </div>
                        <div class="col-md-4 text-end">
                            <i class="fas fa-shield-alt fa-4x opacity-50"></i>
                        </div>
                    </div>
                </div>

        <!-- Estadísticas -->
        <div class="row mb-4">
            <div class="col-md-2">
                <div class="stats-card">
                    <div class="stats-number">${totalClinics}</div>
                    <div class="stats-label">Clínicas Registradas</div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stats-card">
                    <div class="stats-number">${adminUsers}</div>
                    <div class="stats-label">Administradores Activos</div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stats-card">
                    <div class="stats-number">${professionalUsers}</div>
                    <div class="stats-label">Profesionales Activos</div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stats-card">
                    <div class="stats-number">${activePatients}</div>
                    <div class="stats-label">Pacientes Activos</div>
                </div>
            </div>
            <div class="col-md-2">
                <div class="stats-card">
                    <div class="stats-number">${totalUsers}</div>
                    <div class="stats-label">Total Usuarios</div>
                </div>
            </div>
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
                <i class="fas fa-exclamation-triangle me-2"></i>${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- Clínicas y sus administradores -->
        <div class="row">
            <c:choose>
                <c:when test="${not empty clinics}">
                    <c:forEach var="clinic" items="${clinics}">
                        <div class="col-md-6 mb-4">
                            <div class="card clinic-card">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between align-items-start mb-3">
                                        <div>
                                            <h5 class="card-title mb-1">
                                                <i class="fas fa-hospital me-2 text-primary"></i>${clinic.name}
                                            </h5>
                                            <p class="text-muted mb-0">ID: ${clinic.id}</p>
                                        </div>
                                        <c:set var="hasAdmin" value="false" />
                                        <c:forEach var="user" items="${users}">
                                            <c:if test="${user.clinic != null && user.clinic.id == clinic.id && user.role == 'ADMIN_CLINIC' && user.active}">
                                                <c:set var="hasAdmin" value="true" />
                                            </c:if>
                                        </c:forEach>
                                        <c:choose>
                                            <c:when test="${hasAdmin}">
                                                <span class="badge bg-success">
                                                    <i class="fas fa-check me-1"></i>Admin Registrado
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-warning">
                                                    <i class="fas fa-exclamation me-1"></i>Sin Admin
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    
                                    <p class="card-text text-muted mb-3">${clinic.description}</p>
                                    
                                    <div class="row mb-3">
                                        <div class="col-6">
                                            <small class="text-muted">
                                                <i class="fas fa-map-marker-alt me-1"></i>${clinic.address}
                                            </small>
                                        </div>
                                        <div class="col-6">
                                            <small class="text-muted">
                                                <i class="fas fa-phone me-1"></i>${clinic.phone}
                                            </small>
                                        </div>
                                    </div>
                                    
                                    <c:choose>
                                        <c:when test="${hasAdmin}">
                                            <div class="alert alert-success mb-3">
                                                <i class="fas fa-check-circle me-2"></i>
                                                <strong>Administrador registrado:</strong> Esta clínica ya tiene un administrador activo.
                                            </div>
                                            <c:forEach var="user" items="${users}">
                                                <c:if test="${user.clinic != null && user.clinic.id == clinic.id && user.role == 'ADMIN_CLINIC' && user.active}">
                                                    <button type="button" class="btn btn-warning btn-sm" 
                                                            onclick="editClinicAdmin('${user.id}', '${user.username}', '${user.firstName}', '${user.lastName}', '${user.email}', '${clinic.name}')">
                                                        <i class="fas fa-edit me-2"></i>Editar Admin
                                                    </button>
                                                </c:if>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="alert alert-warning mb-3">
                                                <i class="fas fa-exclamation-triangle me-2"></i>
                                                <strong>Sin administrador:</strong> Esta clínica necesita un administrador inicial.
                                            </div>
                                            <button type="button" class="btn btn-primary btn-sm" 
                                                    onclick="registerClinicAdmin('${clinic.id}', '${clinic.name}')">
                                                <i class="fas fa-user-plus me-2"></i>Registrar Admin
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="col-12">
                        <div class="card">
                            <div class="card-body text-center py-5">
                                <i class="fas fa-hospital fa-3x text-muted mb-3"></i>
                                <h5 class="text-muted">No hay clínicas registradas</h5>
                                <p class="text-muted">Primero debe registrar las clínicas en el sistema.</p>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal para Registrar Administrador de Clínica -->
    <div class="modal fade" id="registerAdminModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-plus me-2"></i>Registrar Administrador de Clínica
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="<c:url value='/super-admin'/>" method="post">
                    <input type="hidden" name="action" value="createClinicAdmin">
                    <input type="hidden" id="clinicId" name="clinicId">
                    
                    <div class="modal-body">
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle me-2"></i>
                            <strong>Registrando administrador para:</strong> <span id="clinicName"></span>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="username" class="form-label">Usuario *</label>
                                    <input type="text" class="form-control" id="username" name="username" required>
                                    <div class="form-text">Nombre de usuario único para el login</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="password" class="form-label">Contraseña *</label>
                                    <input type="password" class="form-control" id="password" name="password" required minlength="6">
                                    <div class="form-text">Mínimo 6 caracteres</div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="firstName" class="form-label">Nombre *</label>
                                    <input type="text" class="form-control" id="firstName" name="firstName" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="lastName" class="form-label">Apellido *</label>
                                    <input type="text" class="form-control" id="lastName" name="lastName" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="email" class="form-label">Email *</label>
                            <input type="email" class="form-control" id="email" name="email" required>
                        </div>
                        
                        <div class="alert alert-warning">
                            <i class="fas fa-shield-alt me-2"></i>
                            <strong>Importante:</strong> Este será el primer administrador de la clínica. 
                            Tendrá acceso completo a la gestión de usuarios, pacientes y profesionales de esta clínica.
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Registrar Administrador
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal para Editar Administrador de Clínica -->
    <div class="modal fade" id="editAdminModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-edit me-2"></i>Editar Administrador de Clínica
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="<c:url value='/super-admin'/>" method="post">
                    <input type="hidden" name="action" value="updateClinicAdmin">
                    <input type="hidden" id="editUserId" name="userId">
                    
                    <div class="modal-body">
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle me-2"></i>
                            <strong>Editando administrador de:</strong> <span id="editClinicName"></span>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editUsername" class="form-label">Usuario</label>
                                    <input type="text" class="form-control" id="editUsername" readonly>
                                    <div class="form-text">El nombre de usuario no se puede cambiar</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editEmail" class="form-label">Email</label>
                                    <input type="email" class="form-control" id="editEmail" name="email">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editFirstName" class="form-label">Nombre</label>
                                    <input type="text" class="form-control" id="editFirstName" name="firstName">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editLastName" class="form-label">Apellido</label>
                                    <input type="text" class="form-control" id="editLastName" name="lastName">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="editPassword" class="form-label">Nueva Contraseña</label>
                            <input type="password" class="form-control" id="editPassword" name="password" minlength="6">
                            <div class="form-text">Dejar vacío para mantener la contraseña actual</div>
                        </div>
                        
                        <div class="alert alert-warning">
                            <i class="fas fa-shield-alt me-2"></i>
                            <strong>Importante:</strong> Los cambios se aplicarán inmediatamente. 
                            Si cambia la contraseña, el administrador deberá usar la nueva contraseña en el próximo login.
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Guardar Cambios
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Función para registrar administrador de clínica
        function registerClinicAdmin(clinicId, clinicName) {
            document.getElementById('clinicId').value = clinicId;
            document.getElementById('clinicName').textContent = clinicName;
            
            // Limpiar formulario
            document.getElementById('username').value = '';
            document.getElementById('password').value = '';
            document.getElementById('firstName').value = '';
            document.getElementById('lastName').value = '';
            document.getElementById('email').value = '';
            
            const modal = new bootstrap.Modal(document.getElementById('registerAdminModal'));
            modal.show();
        }
        
        // Función para editar administrador de clínica
        function editClinicAdmin(userId, username, firstName, lastName, email, clinicName) {
            document.getElementById('editUserId').value = userId;
            document.getElementById('editUsername').value = username;
            document.getElementById('editFirstName').value = firstName || '';
            document.getElementById('editLastName').value = lastName || '';
            document.getElementById('editEmail').value = email || '';
            document.getElementById('editPassword').value = '';
            document.getElementById('editClinicName').textContent = clinicName;
            
            const modal = new bootstrap.Modal(document.getElementById('editAdminModal'));
            modal.show();
        }
    </script>
</body>
</html>
