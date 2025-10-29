<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Usuarios - HCEN Clínica</title>
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
            box-shadow: 0 8px 20px rgba(76, 175, 80, 0.3);
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
        
        .table {
            border-radius: 10px;
            overflow: hidden;
        }
        
        .table thead th {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            color: white;
            border: none;
            font-weight: 600;
        }
        
        .badge {
            padding: 8px 12px;
            border-radius: 20px;
            font-weight: 600;
        }
        
        .badge-admin {
            background: linear-gradient(45deg, #007bff, #0056b3);
            color: white;
        }
        
        .badge-professional {
            background: linear-gradient(45deg, #17a2b8, #138496);
            color: white;
        }
        
        .badge-super-admin {
            background: linear-gradient(45deg, #6f42c1, #5a32a3);
            color: white;
        }
        
        .badge-active {
            background: linear-gradient(45deg, #28a745, #20c997);
            color: white;
        }
        
        .badge-inactive {
            background: linear-gradient(45deg, #dc3545, #c82333);
            color: white;
        }
        
        .modal-header {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            color: white;
            border-radius: 15px 15px 0 0;
        }
        
        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(76, 175, 80, 0.25);
        }
        
        .form-select:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(76, 175, 80, 0.25);
        }
        
        .alert {
            border-radius: 10px;
            border: none;
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
                        <li><a class="dropdown-item" href="<c:url value='/admin/dashboard'/>">
                            <i class="fas fa-tachometer-alt me-2"></i>Dashboard
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

    <div class="container-fluid mt-4">
        <!-- Header -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h2 class="mb-1">
                            <i class="fas fa-users me-2 text-primary"></i>Gestión de Usuarios
                        </h2>
                        <p class="text-muted mb-0">Administrar usuarios del sistema</p>
                    </div>
                    <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addUserModal">
                        <i class="fas fa-user-plus me-2"></i>Agregar Usuario
                    </button>
                </div>
            </div>
        </div>

        <!-- Estadísticas -->
        <div class="row mb-4">
            <div class="col-md-3">
                <div class="stats-card">
                    <div class="stats-number">${users != null ? users.size() : 0}</div>
                    <div class="stats-label">Total Usuarios</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stats-card">
                    <div class="stats-number">${users != null ? users.stream().filter(u -> u.active).count() : 0}</div>
                    <div class="stats-label">Usuarios Activos</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stats-card">
                    <div class="stats-number">${users != null ? users.stream().filter(u -> 'ADMIN_CLINIC'.equals(u.role)).count() : 0}</div>
                    <div class="stats-label">Administradores</div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="stats-card">
                    <div class="stats-number">${users != null ? users.stream().filter(u -> 'PROFESSIONAL'.equals(u.role)).count() : 0}</div>
                    <div class="stats-label">Profesionales</div>
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

        <!-- Tabla de Usuarios -->
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>Usuario</th>
                                        <th>Nombre</th>
                                        <th>Email</th>
                                        <th>Rol</th>
                                        <th>Clínica</th>
                                        <th>Estado</th>
                                        <th>Último Login</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty users}">
                                            <c:forEach var="user" items="${users}">
                                                <tr>
                                                    <td>
                                                        <strong>${user.username}</strong>
                                                    </td>
                                                    <td>
                                                        ${user.firstName} ${user.lastName}
                                                    </td>
                                                    <td>
                                                        ${user.email}
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.role == 'SUPER_ADMIN'}">
                                                                <span class="badge badge-super-admin">Super Admin</span>
                                                            </c:when>
                                                            <c:when test="${user.role == 'ADMIN_CLINIC'}">
                                                                <span class="badge badge-admin">Administrador</span>
                                                            </c:when>
                                                            <c:when test="${user.role == 'PROFESSIONAL'}">
                                                                <span class="badge badge-professional">Profesional</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-secondary">${user.role}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        ${user.clinic != null ? user.clinic.name : 'Sistema'}
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.active}">
                                                                <span class="badge badge-active">Activo</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge badge-inactive">Inactivo</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.lastLogin != null}">
                                                                <fmt:formatDate value="${user.lastLogin}" pattern="dd/MM/yyyy HH:mm"/>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">Nunca</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <div class="btn-group" role="group">
                                                            <button type="button" class="btn btn-warning btn-sm" 
                                                                    onclick="editUser('${user.id}', '${user.username}', '${user.firstName}', '${user.lastName}', '${user.email}', '${user.role}', '${user.clinic != null ? user.clinic.id : ""}', '${user.active}')">
                                                                <i class="fas fa-edit"></i>
                                                            </button>
                                                            <button type="button" class="btn btn-danger btn-sm" 
                                                                    onclick="deactivateUser('${user.id}', '${user.username}')">
                                                                <i class="fas fa-ban"></i>
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="8" class="text-center text-muted py-4">
                                                    <i class="fas fa-users fa-3x mb-3"></i>
                                                    <br>No hay usuarios registrados
                                                </td>
                                            </tr>
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

    <!-- Modal para Agregar Usuario -->
    <div class="modal fade" id="addUserModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-plus me-2"></i>Agregar Usuario
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="<c:url value='/admin/users'/>" method="post">
                    <input type="hidden" name="action" value="create">
                    
                    <div class="modal-body">
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
                                    <label for="firstName" class="form-label">Nombre</label>
                                    <input type="text" class="form-control" id="firstName" name="firstName">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="lastName" class="form-label">Apellido</label>
                                    <input type="text" class="form-control" id="lastName" name="lastName">
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="email" class="form-label">Email</label>
                            <input type="email" class="form-control" id="email" name="email">
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="role" class="form-label">Rol *</label>
                                    <select class="form-select" id="role" name="role" required>
                                        <option value="">Seleccionar rol</option>
                                        <option value="ADMIN_CLINIC">Administrador de Clínica</option>
                                        <option value="PROFESSIONAL">Profesional</option>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="clinicId" class="form-label">Clínica</label>
                                    <select class="form-select" id="clinicId" name="clinicId">
                                        <option value="">Seleccionar clínica</option>
                                        <c:forEach var="clinic" items="${clinics}">
                                            <option value="${clinic.id}">${clinic.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="professionalId" class="form-label">Profesional Asociado</label>
                            <select class="form-select" id="professionalId" name="professionalId">
                                <option value="">Seleccionar profesional</option>
                                <!-- Se llenará dinámicamente según la clínica seleccionada -->
                            </select>
                            <div class="form-text">Solo para usuarios con rol Profesional</div>
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Crear Usuario
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal para Editar Usuario -->
    <div class="modal fade" id="editUserModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-edit me-2"></i>Editar Usuario
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="<c:url value='/admin/users'/>" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" id="editUserId" name="userId">
                    
                    <div class="modal-body">
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
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editClinicId" class="form-label">Clínica</label>
                                    <select class="form-select" id="editClinicId" name="clinicId">
                                        <option value="">Seleccionar clínica</option>
                                        <c:forEach var="clinic" items="${clinics}">
                                            <option value="${clinic.id}">${clinic.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editProfessionalId" class="form-label">Profesional Asociado</label>
                                    <select class="form-select" id="editProfessionalId" name="professionalId">
                                        <option value="">Seleccionar profesional</option>
                                    </select>
                                </div>
                            </div>
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
        // Función para editar usuario
        function editUser(id, username, firstName, lastName, email, role, clinicId, active) {
            document.getElementById('editUserId').value = id;
            document.getElementById('editUsername').value = username;
            document.getElementById('editFirstName').value = firstName || '';
            document.getElementById('editLastName').value = lastName || '';
            document.getElementById('editEmail').value = email || '';
            document.getElementById('editClinicId').value = clinicId || '';
            
            const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
            modal.show();
        }
        
        // Función para desactivar usuario
        function deactivateUser(id, username) {
            if (confirm('¿Está seguro de que desea desactivar el usuario "' + username + '"?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '<c:url value="/admin/users"/>';
                
                const actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = 'deactivate';
                
                const userIdInput = document.createElement('input');
                userIdInput.type = 'hidden';
                userIdInput.name = 'userId';
                userIdInput.value = id;
                
                form.appendChild(actionInput);
                form.appendChild(userIdInput);
                document.body.appendChild(form);
                form.submit();
            }
        }
        
        // Cargar profesionales según la clínica seleccionada
        document.getElementById('clinicId').addEventListener('change', function() {
            const clinicId = this.value;
            const professionalSelect = document.getElementById('professionalId');
            
            // Limpiar opciones
            professionalSelect.innerHTML = '<option value="">Seleccionar profesional</option>';
            
            if (clinicId) {
                // Aquí podrías hacer una llamada AJAX para cargar los profesionales
                // Por ahora, agregamos opciones de ejemplo
                professionalSelect.innerHTML += '<option value="1">Profesional 1</option>';
                professionalSelect.innerHTML += '<option value="2">Profesional 2</option>';
            }
        });
        
        // Mismo comportamiento para el modal de edición
        document.getElementById('editClinicId').addEventListener('change', function() {
            const clinicId = this.value;
            const professionalSelect = document.getElementById('editProfessionalId');
            
            professionalSelect.innerHTML = '<option value="">Seleccionar profesional</option>';
            
            if (clinicId) {
                professionalSelect.innerHTML += '<option value="1">Profesional 1</option>';
                professionalSelect.innerHTML += '<option value="2">Profesional 2</option>';
            }
        });
    </script>
</body>
</html>
