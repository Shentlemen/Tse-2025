<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
        
        .navbar-nav .dropdown-menu {
            z-index: 1050 !important;
        }
        
        .navbar .dropdown-toggle::after {
            margin-left: 0.5em;
        }
        
        .navbar {
            z-index: 1030;
            position: relative;
        }
        
        .navbar-nav .dropdown {
            position: static;
        }
        
        .navbar-nav .dropdown-menu {
            position: absolute;
            right: 0;
            left: auto;
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
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            transition: transform 0.3s ease;
        }
        
        .card:hover {
            transform: translateY(-2px);
        }
        
        .btn-primary {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            border: none;
            border-radius: 8px;
            padding: 10px 20px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        }
        
        .table {
            border-radius: 10px;
            overflow: hidden;
        }
        
        .table thead th {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            border: none;
            font-weight: 600;
        }
        
        .status-active {
            color: #28a745;
        }
        
        .status-inactive {
            color: #dc3545;
        }
        
        .badge {
            font-size: 0.8em;
            padding: 6px 12px;
            border-radius: 20px;
        }
        
        .badge-admin {
            background-color: #6f42c1;
        }
        
        .badge-professional {
            background-color: #17a2b8;
        }
        
        .badge-super-admin {
            background-color: #fd7e14;
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
                        <h5 class="text-white mb-3">
                            <i class="fas fa-tachometer-alt me-2"></i>Panel de Control
                        </h5>
                        <nav class="nav flex-column">
                            <a class="nav-link" href="<c:url value='/admin/dashboard'/>">
                                <i class="fas fa-home me-2"></i>Dashboard
                            </a>
                            <a class="nav-link" href="<c:url value='/admin/professionals'/>">
                                <i class="fas fa-user-md me-2"></i>Profesionales
                            </a>
                            <a class="nav-link" href="<c:url value='/admin/patients-list'/>">
                                <i class="fas fa-users me-2"></i>Pacientes
                            </a>
                            <a class="nav-link" href="<c:url value='/admin/specialties-list'/>">
                                <i class="fas fa-stethoscope me-2"></i>Especialidades
                            </a>
                            <a class="nav-link" href="<c:url value='/admin/documents'/>">
                                <i class="fas fa-file-medical me-2"></i>Documentos
                            </a>
                            <c:if test="${sessionScope.role == 'ADMIN_CLINIC' or sessionScope.role == 'SUPER_ADMIN'}">
                                <a class="nav-link active" href="<c:url value='/admin/users'/>">
                                    <i class="fas fa-user-cog me-2"></i>Gestión de Usuarios
                                </a>
                            </c:if>
                            <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                                <a class="nav-link" href="<c:url value='/admin/super-admin'/>">
                                    <i class="fas fa-crown me-2"></i>Super Admin
                                </a>
                            </c:if>
                        </nav>
                    </div>
                </div>
            </div>

            <!-- Main Content -->
            <div class="col-md-10 p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>
                        <i class="fas fa-user-cog me-2"></i>Gestión de Usuarios
                    </h2>
                    <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                        <span class="badge badge-super-admin">
                            <i class="fas fa-crown me-1"></i>Vista Completa
                        </span>
                    </c:if>
                </div>

                <c:if test="${sessionScope.role == 'SUPER_ADMIN'}">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle me-2"></i>
                        <strong>Vista Completa:</strong> Estás viendo todos los usuarios del sistema.
                    </div>
                </c:if>

                <!-- Filtros -->
                <div class="card mb-4">
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3">
                                <label for="roleFilter" class="form-label">Rol</label>
                                <select class="form-select" id="roleFilter">
                                    <option value="">Todos los roles</option>
                                    <option value="ADMIN_CLINIC">Administradores</option>
                                    <option value="PROFESSIONAL">Profesionales</option>
                                    <option value="SUPER_ADMIN">Super Administradores</option>
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
                            <div class="col-md-4">
                                <label for="searchInput" class="form-label">Buscar</label>
                                <input type="text" class="form-control" id="searchInput" placeholder="Buscar por nombre, usuario o email...">
                            </div>
                            <div class="col-md-2 d-flex align-items-end">
                                <button class="btn btn-outline-secondary w-100" onclick="clearFilters()">
                                    <i class="fas fa-times me-1"></i>Limpiar
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Tabla de Usuarios -->
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty users}">
                                <div class="table-responsive">
                                    <table class="table table-hover" id="usersTable">
                                        <thead>
                                            <tr>
                                                <th>Usuario</th>
                                                <th>Nombre Completo</th>
                                                <th>Email</th>
                                                <th>Rol</th>
                                                <th>Clínica</th>
                                                <th>Estado</th>
                                                <th>Último Login</th>
                                                <th>Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="user" items="${users}">
                                                <tr>
                                                    <td><strong>${user.username}</strong></td>
                                                    <td>${user.firstName} ${user.lastName}</td>
                                                    <td>${user.email}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.role == 'ADMIN_CLINIC'}">
                                                                <span class="badge badge-admin">Administrador</span>
                                                            </c:when>
                                                            <c:when test="${user.role == 'PROFESSIONAL'}">
                                                                <span class="badge badge-professional">Profesional</span>
                                                            </c:when>
                                                            <c:when test="${user.role == 'SUPER_ADMIN'}">
                                                                <span class="badge badge-super-admin">Super Admin</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-secondary">${user.role}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.clinic != null}">
                                                                ${user.clinic.name}
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">-</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.active}">
                                                                <i class="fas fa-check-circle status-active"></i> Activo
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fas fa-times-circle status-inactive"></i> Inactivo
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${user.lastLogin != null}">
                                                                <%
                                                                    uy.gub.clinic.entity.User currentUser = (uy.gub.clinic.entity.User) pageContext.getAttribute("user");
                                                                    if (currentUser != null && currentUser.getLastLogin() != null) {
                                                                        java.time.LocalDateTime ldt = currentUser.getLastLogin();
                                                                        java.util.Date date = java.sql.Timestamp.valueOf(ldt);
                                                                        pageContext.setAttribute("lastLoginDate", date);
                                                                %>
                                                                    <fmt:formatDate value="${lastLoginDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                                <%
                                                                    }
                                                                %>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted">Nunca</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <div class="btn-group" role="group">
                                                            <button type="button" class="btn btn-sm btn-outline-primary" 
                                                                    onclick="viewUser('${user.id}')" title="Ver detalles">
                                                                <i class="fas fa-eye"></i>
                                                            </button>
                                                            <c:if test="${sessionScope.role == 'SUPER_ADMIN' or (sessionScope.role == 'ADMIN_CLINIC' and (user.role == 'PROFESSIONAL' or user.role == 'ADMIN_CLINIC'))}">
                                                                <button type="button" class="btn btn-sm btn-outline-warning" 
                                                                        onclick="editUser('${user.id}')" title="Editar">
                                                                    <i class="fas fa-edit"></i>
                                                                </button>
                                                                <c:choose>
                                                                    <c:when test="${user.active}">
                                                                        <button type="button" class="btn btn-sm btn-outline-danger" 
                                                                                onclick="toggleUserStatus('${user.id}', false)" title="Desactivar">
                                                                            <i class="fas fa-user-times"></i>
                                                                        </button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <button type="button" class="btn btn-sm btn-outline-success" 
                                                                                onclick="toggleUserStatus('${user.id}', true)" title="Activar">
                                                                            <i class="fas fa-user-check"></i>
                                                                        </button>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:if>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5">
                                    <i class="fas fa-users fa-3x text-muted mb-3"></i>
                                    <h4 class="text-muted">No hay usuarios registrados</h4>
                                    <p class="text-muted">Los usuarios se crean automáticamente al registrar profesionales.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
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
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form id="editUserForm">
                    <input type="hidden" id="editUserId" name="userId">
                    
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editUsername" class="form-label">Usuario</label>
                                    <input type="text" class="form-control" id="editUsername" name="username" readonly>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editRole" class="form-label">Rol</label>
                                    <input type="text" class="form-control" id="editRole" name="role" readonly>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editFirstName" class="form-label">Nombre</label>
                                    <input type="text" class="form-control" id="editFirstName" name="firstName" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editLastName" class="form-label">Apellido</label>
                                    <input type="text" class="form-control" id="editLastName" name="lastName" required>
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editEmail" class="form-label">Email</label>
                                    <input type="email" class="form-control" id="editEmail" name="email" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editPassword" class="form-label">Nueva Contraseña (opcional)</label>
                                    <input type="password" class="form-control" id="editPassword" name="password" placeholder="Dejar vacío para mantener la actual">
                                </div>
                            </div>
                        </div>
                        
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="editActive" class="form-label">Estado</label>
                                    <select class="form-select" id="editActive" name="active">
                                        <option value="true">Activo</option>
                                        <option value="false">Inactivo</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-1"></i>Guardar Cambios
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal para Ver Detalles de Usuario -->
    <div class="modal fade" id="viewUserModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-user-circle me-2"></i>Detalles del Usuario
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong><i class="fas fa-user me-2"></i>Usuario:</strong>
                            <p id="viewUsername" class="text-muted"></p>
                        </div>
                        <div class="col-md-6">
                            <strong><i class="fas fa-id-badge me-2"></i>Rol:</strong>
                            <p id="viewRole" class="text-muted"></p>
                        </div>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong><i class="fas fa-user-tag me-2"></i>Nombre:</strong>
                            <p id="viewFirstName" class="text-muted"></p>
                        </div>
                        <div class="col-md-6">
                            <strong><i class="fas fa-user-tag me-2"></i>Apellido:</strong>
                            <p id="viewLastName" class="text-muted"></p>
                        </div>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong><i class="fas fa-envelope me-2"></i>Email:</strong>
                            <p id="viewEmail" class="text-muted"></p>
                        </div>
                        <div class="col-md-6">
                            <strong><i class="fas fa-building me-2"></i>Clínica:</strong>
                            <p id="viewClinic" class="text-muted"></p>
                        </div>
                    </div>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong><i class="fas fa-info-circle me-2"></i>Estado:</strong>
                            <p id="viewStatus" class="text-muted"></p>
                        </div>
                        <div class="col-md-6">
                            <strong><i class="fas fa-clock me-2"></i>Último Login:</strong>
                            <p id="viewLastLogin" class="text-muted"></p>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Definir funciones globalmente - asegurar que estén disponibles ANTES de cualquier uso
        window.viewUser = function(userId) {
            // Buscar el usuario en la tabla para obtener sus datos
            const table = document.getElementById('usersTable');
            if (!table) {
                console.error('Tabla usersTable no encontrada');
                return;
            }
            const rows = table.getElementsByTagName('tr');
            
            for (let i = 1; i < rows.length; i++) {
                const row = rows[i];
                const cells = row.getElementsByTagName('td');
                
                // Buscar el botón de ver detalles en esta fila
                const viewButton = row.querySelector('button[onclick*="viewUser"]');
                if (viewButton && viewButton.getAttribute('onclick').includes(userId)) {
                    // Extraer datos de la fila
                    const username = cells[0].textContent.trim();
                    const fullName = cells[1].textContent.trim();
                    const email = cells[2].textContent.trim();
                    const role = cells[3].textContent.trim();
                    const clinic = cells[4].textContent.trim();
                    const status = cells[5].textContent.trim();
                    const lastLogin = cells[6].textContent.trim();
                    
                    // Separar nombre y apellido
                    const nameParts = fullName.split(' ');
                    const firstName = nameParts[0] || '';
                    const lastName = nameParts.slice(1).join(' ') || '';
                    
                    // Llenar el modal
                    document.getElementById('viewUsername').textContent = username;
                    document.getElementById('viewRole').textContent = role;
                    document.getElementById('viewFirstName').textContent = firstName;
                    document.getElementById('viewLastName').textContent = lastName;
                    document.getElementById('viewEmail').textContent = email;
                    document.getElementById('viewClinic').textContent = clinic;
                    document.getElementById('viewStatus').textContent = status;
                    document.getElementById('viewLastLogin').textContent = lastLogin || 'Nunca';
                    
                    // Mostrar el modal
                    const modal = new bootstrap.Modal(document.getElementById('viewUserModal'));
                    modal.show();
                    break;
                }
            }
        }

        window.editUser = function(userId) {
            // Buscar el usuario en la tabla para obtener sus datos
            const table = document.getElementById('usersTable');
            if (!table) {
                console.error('Tabla usersTable no encontrada');
                return;
            }
            const rows = table.getElementsByTagName('tr');
            
            for (let i = 1; i < rows.length; i++) {
                const row = rows[i];
                const cells = row.getElementsByTagName('td');
                
                // Buscar el botón de editar en esta fila
                const editButton = row.querySelector('button[onclick*="editUser"]');
                if (editButton && editButton.getAttribute('onclick').includes(userId)) {
                    // Extraer datos de la fila
                    const username = cells[0].textContent.trim();
                    const fullName = cells[1].textContent.trim();
                    const email = cells[2].textContent.trim();
                    const role = cells[3].textContent.trim();
                    const status = cells[5].textContent.trim();
                    
                    // Separar nombre y apellido
                    const nameParts = fullName.split(' ');
                    const firstName = nameParts[0] || '';
                    const lastName = nameParts.slice(1).join(' ') || '';
                    
                    // Determinar el rol real
                    let actualRole = 'PROFESSIONAL';
                    if (role.includes('Administrador')) {
                        actualRole = 'ADMIN_CLINIC';
                    } else if (role.includes('Super Admin')) {
                        actualRole = 'SUPER_ADMIN';
                    }
                    
                    // Determinar estado
                    const isActive = status.includes('Activo');
                    
                    // Llenar el modal
                    document.getElementById('editUserId').value = userId;
                    document.getElementById('editUsername').value = username;
                    document.getElementById('editRole').value = actualRole;
                    document.getElementById('editFirstName').value = firstName;
                    document.getElementById('editLastName').value = lastName;
                    document.getElementById('editEmail').value = email;
                    document.getElementById('editActive').value = isActive ? 'true' : 'false';
                    
                    // Mostrar el modal
                    const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
                    modal.show();
                    break;
                }
            }
        }

        window.toggleUserStatus = function(userId, activate) {
            if (confirm(activate ? '¿Activar este usuario?' : '¿Desactivar este usuario?')) {
                // Crear un formulario temporal para enviar los datos
                const tempForm = document.createElement('form');
                tempForm.method = 'POST';
                tempForm.action = '${pageContext.request.contextPath}/admin/users';
                
                // Agregar campos
                const actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = 'toggleStatus';
                tempForm.appendChild(actionInput);
                
                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'userId';
                idInput.value = userId;
                tempForm.appendChild(idInput);
                
                const activeInput = document.createElement('input');
                activeInput.type = 'hidden';
                activeInput.name = 'active';
                activeInput.value = activate ? 'true' : 'false';
                tempForm.appendChild(activeInput);
                
                // Enviar formulario
                document.body.appendChild(tempForm);
                tempForm.submit();
            }
        }

        function filterTable() {
            const roleFilter = document.getElementById('roleFilter').value.toLowerCase();
            const statusFilter = document.getElementById('statusFilter').value.toLowerCase();
            const searchInput = document.getElementById('searchInput').value.toLowerCase();
            const table = document.getElementById('usersTable');
            const rows = table.getElementsByTagName('tr');

            for (let i = 1; i < rows.length; i++) {
                const row = rows[i];
                const cells = row.getElementsByTagName('td');
                
                const role = cells[3].textContent.toLowerCase();
                const status = cells[5].textContent.toLowerCase().trim();
                const searchText = row.textContent.toLowerCase();

                // Mapear los valores del filtro de rol al texto mostrado
                let roleMatch = true;
                if (roleFilter) {
                    if (roleFilter === 'admin_clinic') {
                        roleMatch = role.includes('administrador');
                    } else if (roleFilter === 'professional') {
                        roleMatch = role.includes('profesional');
                    } else if (roleFilter === 'super_admin') {
                        roleMatch = role.includes('super admin');
                    }
                }
                
                // Mapear el estado
                let statusMatch = true;
                if (statusFilter) {
                    if (statusFilter === 'active') {
                        statusMatch = status.includes('activo') && !status.includes('inactivo');
                    } else if (statusFilter === 'inactive') {
                        statusMatch = status.includes('inactivo');
                    }
                }
                
                const searchMatch = !searchInput || searchText.includes(searchInput);

                if (roleMatch && statusMatch && searchMatch) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            }
        }

        window.clearFilters = function() {
            document.getElementById('roleFilter').value = '';
            document.getElementById('statusFilter').value = '';
            document.getElementById('searchInput').value = '';
            filterTable();
        }

        // Inicializar cuando el DOM esté listo
        document.addEventListener('DOMContentLoaded', function() {
            // Asegurar que Bootstrap dropdown funcione
            var dropdownElementList = [].slice.call(document.querySelectorAll('.dropdown-toggle'));
            var dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
                try {
                    return new bootstrap.Dropdown(dropdownToggleEl);
                } catch (e) {
                    console.error('Error al inicializar dropdown:', e);
                    return null;
                }
            });
            
            // Forzar inicialización si no funcionó
            if (dropdownList.length === 0 || dropdownList[0] === null) {
                console.log('Reintentando inicialización de dropdown...');
                setTimeout(function() {
                    var retryDropdown = document.querySelector('.navbar .dropdown-toggle');
                    if (retryDropdown) {
                        new bootstrap.Dropdown(retryDropdown);
                    }
                }, 100);
            }
            
            // Filtros
            const roleFilterEl = document.getElementById('roleFilter');
            const statusFilterEl = document.getElementById('statusFilter');
            const searchInputEl = document.getElementById('searchInput');
            
            if (roleFilterEl) {
                roleFilterEl.addEventListener('change', filterTable);
            }
            if (statusFilterEl) {
                statusFilterEl.addEventListener('change', filterTable);
            }
            if (searchInputEl) {
                searchInputEl.addEventListener('input', filterTable);
            }
            
            // Manejo del formulario de edición
            const editUserForm = document.getElementById('editUserForm');
            if (editUserForm) {
                editUserForm.addEventListener('submit', function(e) {
                    e.preventDefault();
                    
                    const formData = new FormData(this);
                    const userId = formData.get('userId');
                    
                    // Crear un formulario temporal para enviar los datos
                    const tempForm = document.createElement('form');
                    tempForm.method = 'POST';
                    tempForm.action = '${pageContext.request.contextPath}/admin/users';
                    
                    // Agregar campos
                    const actionInput = document.createElement('input');
                    actionInput.type = 'hidden';
                    actionInput.name = 'action';
                    actionInput.value = 'update';
                    tempForm.appendChild(actionInput);
                    
                    const idInput = document.createElement('input');
                    idInput.type = 'hidden';
                    idInput.name = 'id';
                    idInput.value = userId;
                    tempForm.appendChild(idInput);
                    
                    // Agregar todos los campos del formulario
                    for (const [key, value] of formData.entries()) {
                        if (key !== 'userId') {
                            const input = document.createElement('input');
                            input.type = 'hidden';
                            input.name = key;
                            input.value = value;
                            tempForm.appendChild(input);
                        }
                    }
                    
                    // Enviar formulario
                    document.body.appendChild(tempForm);
                    tempForm.submit();
                });
            }
        });
    </script>
</body>
</html>
