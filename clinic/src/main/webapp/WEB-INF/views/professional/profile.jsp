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
    <title>Mi Perfil - Portal Profesional</title>
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
        
        .form-label {
            font-weight: 600;
            color: #495057;
        }
        
        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(52, 152, 219, 0.25);
        }
        
        .btn-primary {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            border: none;
            border-radius: 25px;
            padding: 10px 25px;
            font-weight: 600;
        }
        
        .info-badge {
            background: #e8f4fd;
            color: var(--primary-color);
            padding: 8px 15px;
            border-radius: 8px;
            font-weight: 600;
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
            <div class="col-md-2 sidebar p-0">
                <nav class="nav flex-column pt-3">
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
                            <a class="nav-link" href="<c:url value='/professional/requests'/>">
                                <i class="fas fa-exchange-alt me-2"></i>Solicitudes
                            </a>
                        </li>
                    </ul>
                </nav>
            </div>

            <!-- Contenido Principal -->
            <div class="col-md-10 p-4">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h2>
                        <i class="fas fa-user-circle me-2 text-primary"></i>Mi Perfil
                    </h2>
                </div>

                <!-- Mensajes -->
                <c:if test="${not empty success}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="fas fa-check-circle me-2"></i>Perfil actualizado correctamente.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                
                <c:if test="${param.error == 'password_empty'}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>Todos los campos de contraseña son requeridos.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                
                <c:if test="${param.error == 'password_invalid'}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>La nueva contraseña debe tener al menos 6 caracteres.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                
                <c:if test="${param.error == 'password_mismatch'}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>Las nuevas contraseñas no coinciden.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                
                <c:if test="${param.error == 'password_incorrect'}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>La contraseña actual es incorrecta.
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                
                <c:if test="${param.error != null && fn:contains(param.error, 'nombre de usuario')}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle me-2"></i>${param.error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <c:if test="${not empty professional}">
                    <form method="POST" action="<c:url value='/professional/profile'/>">
                        <div class="row">
                            <!-- Información Personal -->
                            <div class="col-md-8">
                                <div class="card mb-4">
                                    <div class="card-header bg-primary text-white">
                                        <h5 class="mb-0">
                                            <i class="fas fa-user me-2"></i>Información Personal
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="name" class="form-label">
                                                    <i class="fas fa-signature me-1"></i>Nombre <span class="text-danger">*</span>
                                                </label>
                                                <input type="text" class="form-control" id="name" name="name" 
                                                       value="${professional.name != null ? professional.name : ''}" 
                                                       required>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="lastName" class="form-label">
                                                    <i class="fas fa-signature me-1"></i>Apellido
                                                </label>
                                                <input type="text" class="form-control" id="lastName" name="lastName" 
                                                       value="${professional.lastName != null ? professional.lastName : ''}">
                                            </div>
                                        </div>

                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="username" class="form-label">
                                                    <i class="fas fa-user me-1"></i>Nombre de Usuario
                                                </label>
                                                <input type="text" class="form-control" id="username" name="username" 
                                                       value="${user != null ? user.username : sessionScope.user}" 
                                                       pattern="[a-zA-Z0-9_]+" 
                                                       title="Solo letras, números y guiones bajos">
                                                <small class="form-text text-muted">Solo letras, números y guiones bajos</small>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="email" class="form-label">
                                                    <i class="fas fa-envelope me-1"></i>Email
                                                </label>
                                                <input type="email" class="form-control" id="email" name="email" 
                                                       value="${professional.email != null ? professional.email : ''}">
                                            </div>
                                        </div>

                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="phone" class="form-label">
                                                    <i class="fas fa-phone me-1"></i>Teléfono
                                                </label>
                                                <input type="tel" class="form-control" id="phone" name="phone" 
                                                       value="${professional.phone != null ? professional.phone : ''}">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="licenseNumber" class="form-label">
                                                    <i class="fas fa-id-badge me-1"></i>Número de Matrícula
                                                </label>
                                                <input type="text" class="form-control" id="licenseNumber" name="licenseNumber" 
                                                       value="${professional.licenseNumber != null ? professional.licenseNumber : ''}">
                                            </div>
                                        </div>

                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="specialtyId" class="form-label">
                                                    <i class="fas fa-stethoscope me-1"></i>Especialidad
                                                </label>
                                                <select class="form-select" id="specialtyId" name="specialtyId">
                                                    <option value="">Seleccionar especialidad</option>
                                                    <c:forEach var="specialty" items="${specialties}">
                                                        <option value="${specialty.id}" 
                                                                ${professional.specialty != null && professional.specialty.id == specialty.id ? 'selected' : ''}>
                                                            ${specialty.name}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                        </div>

                                        <!-- Sección de Cambio de Contraseña (Opcional) -->
                                        <hr class="my-4">
                                        <h6 class="text-muted mb-3">
                                            <i class="fas fa-key me-2"></i>Cambiar Contraseña (Opcional)
                                        </h6>
                                        <div class="row mb-3">
                                            <div class="col-md-12">
                                                <label for="currentPassword" class="form-label">
                                                    <i class="fas fa-lock me-1"></i>Contraseña Actual
                                                </label>
                                                <input type="password" class="form-control" id="currentPassword" name="currentPassword" 
                                                       placeholder="Dejar vacío si no desea cambiar la contraseña">
                                                <small class="form-text text-muted">Complete solo si desea cambiar su contraseña</small>
                                            </div>
                                        </div>

                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="newPassword" class="form-label">
                                                    <i class="fas fa-key me-1"></i>Nueva Contraseña
                                                </label>
                                                <input type="password" class="form-control" id="newPassword" name="newPassword" 
                                                       minlength="6" placeholder="Mínimo 6 caracteres">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="confirmPassword" class="form-label">
                                                    <i class="fas fa-key me-1"></i>Confirmar Nueva Contraseña
                                                </label>
                                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" 
                                                       minlength="6">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Información de Solo Lectura -->
                            <div class="col-md-4">
                                <div class="card mb-4">
                                    <div class="card-header bg-secondary text-white">
                                        <h5 class="mb-0">
                                            <i class="fas fa-info-circle me-2"></i>Información del Sistema
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="mb-3">
                                            <label class="form-label">ID de Profesional</label>
                                            <div class="info-badge">${professional.id}</div>
                                        </div>
                                        
                                        <div class="mb-3">
                                            <label class="form-label">Clínica</label>
                                            <div class="info-badge">
                                                ${professional.clinic != null ? professional.clinic.name : 'N/A'}
                                            </div>
                                        </div>
                                        
                                        <div class="mb-3">
                                            <label class="form-label">Estado</label>
                                            <div>
                                                <c:choose>
                                                    <c:when test="${professional.active}">
                                                        <span class="badge bg-success">Activo</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger">Inactivo</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                        
                                        <div class="mb-3">
                                            <label class="form-label">Fecha de Registro</label>
                                            <div class="text-muted small">
                                                <c:if test="${professional.createdAt != null}">
                                                    <%
                                                        uy.gub.clinic.entity.Professional profItem = (uy.gub.clinic.entity.Professional) request.getAttribute("professional");
                                                        if (profItem != null && profItem.getCreatedAt() != null) {
                                                            java.util.Date createdDate = java.sql.Timestamp.valueOf(profItem.getCreatedAt());
                                                            pageContext.setAttribute("createdDate", createdDate);
                                                    %>
                                                        <fmt:formatDate value="${createdDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                    <%
                                                        }
                                                    %>
                                                </c:if>
                                            </div>
                                        </div>
                                        
                                        <div class="mb-3">
                                            <label class="form-label">Última Actualización</label>
                                            <div class="text-muted small">
                                                <c:if test="${professional.updatedAt != null}">
                                                    <%
                                                        uy.gub.clinic.entity.Professional profItem = (uy.gub.clinic.entity.Professional) request.getAttribute("professional");
                                                        if (profItem != null && profItem.getUpdatedAt() != null) {
                                                            java.util.Date updatedDate = java.sql.Timestamp.valueOf(profItem.getUpdatedAt());
                                                            pageContext.setAttribute("updatedDate", updatedDate);
                                                    %>
                                                        <fmt:formatDate value="${updatedDate}" pattern="dd/MM/yyyy HH:mm"/>
                                                    <%
                                                        }
                                                    %>
                                                </c:if>
                                                <c:if test="${professional.updatedAt == null}">
                                                    <span class="text-muted">Nunca</span>
                                                </c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Botones de Acción -->
                        <div class="d-flex justify-content-end gap-2">
                            <a href="<c:url value='/professional/dashboard'/>" class="btn btn-secondary">
                                <i class="fas fa-times me-2"></i>Cancelar
                            </a>
                            <button type="submit" class="btn btn-primary">
                                <i class="fas fa-save me-2"></i>Guardar Cambios
                            </button>
                        </div>
                    </form>
                </c:if>

                <c:if test="${empty professional}">
                    <div class="alert alert-warning" role="alert">
                        <i class="fas fa-exclamation-triangle me-2"></i>No se pudo cargar la información del perfil.
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Validación del formulario unificado
        document.querySelector('form')?.addEventListener('submit', function(e) {
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            // Solo validar contraseña si se intenta cambiar (al menos un campo tiene valor)
            if (currentPassword || newPassword || confirmPassword) {
                // Si se intenta cambiar, todos los campos deben estar completos
                if (!currentPassword || !newPassword || !confirmPassword) {
                    e.preventDefault();
                    alert('Para cambiar la contraseña, debe completar todos los campos de contraseña');
                    return false;
                }
                
                if (newPassword !== confirmPassword) {
                    e.preventDefault();
                    alert('Las nuevas contraseñas no coinciden');
                    return false;
                }
                
                if (newPassword.length < 6) {
                    e.preventDefault();
                    alert('La nueva contraseña debe tener al menos 6 caracteres');
                    return false;
                }
            }
        });
    </script>
</body>
</html>

