<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Especialidades - HCEN Clínica</title>
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
                                <a class="nav-link active" href="<c:url value='/admin/specialties-list'/>">
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
                        <i class="fas fa-stethoscope me-2"></i>Gestión de Especialidades
                    </h2>
                    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addSpecialtyModal">
                        <i class="fas fa-plus me-2"></i>Agregar Especialidad
                    </button>
                </div>
                
                <!-- Mensajes de éxito/error -->
                <c:if test="${param.success != null}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <c:choose>
                            <c:when test="${param.success == 'registered'}">
                                <i class="fas fa-check-circle me-2"></i>Especialidad registrada exitosamente
                            </c:when>
                            <c:when test="${param.success == 'updated'}">
                                <i class="fas fa-check-circle me-2"></i>Especialidad actualizada exitosamente
                            </c:when>
                            <c:when test="${param.success == 'deleted'}">
                                <i class="fas fa-check-circle me-2"></i>Especialidad eliminada exitosamente
                            </c:when>
                            <c:when test="${param.success == 'activated'}">
                                <i class="fas fa-check-circle me-2"></i>Especialidad activada exitosamente
                            </c:when>
                            <c:when test="${param.success == 'deactivated'}">
                                <i class="fas fa-check-circle me-2"></i>Especialidad desactivada exitosamente
                            </c:when>
                        </c:choose>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

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

                <!-- Tabla de Especialidades -->
                <div class="card">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th>Nombre</th>
                                        <th>Código</th>
                                        <th>Descripción</th>
                                        <th>Estado</th>
                                        <th>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty specialties}">
                                            <c:forEach var="specialty" items="${specialties}">
                                                <tr>
                                                    <td><strong>${specialty.name}</strong></td>
                                                    <td><code>${specialty.code != null ? specialty.code : 'N/A'}</code></td>
                                                    <td>${specialty.description != null ? specialty.description : '-'}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${specialty.active}">
                                                                <i class="fas fa-check-circle status-active"></i> Activo
                                                            </c:when>
                                                            <c:otherwise>
                                                                <i class="fas fa-times-circle status-inactive"></i> Inactivo
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <div class="btn-group" role="group">
                                                            <button class="btn btn-sm btn-action-edit" title="Editar" 
                                                                    onclick="editSpecialty('${specialty.id}')">
                                                                <i class="fas fa-edit"></i>
                                                            </button>
                                                            <c:choose>
                                                                <c:when test="${specialty.active}">
                                                                    <button class="btn btn-sm btn-action-delete" title="Desactivar" 
                                                                            onclick="toggleSpecialtyStatus('${specialty.id}', false)">
                                                                        <i class="fas fa-ban"></i>
                                                                    </button>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <button class="btn btn-sm btn-action-activate" title="Activar" 
                                                                            onclick="toggleSpecialtyStatus('${specialty.id}', true)">
                                                                        <i class="fas fa-check"></i>
                                                                    </button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                            <button class="btn btn-sm btn-action-delete" title="Eliminar" 
                                                                    onclick="deleteSpecialty('${specialty.id}')">
                                                                <i class="fas fa-trash"></i>
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" class="text-center text-muted">
                                                    <i class="fas fa-stethoscope fa-3x mb-3"></i>
                                                    <p>No hay especialidades registradas</p>
                                                    <p class="small">Haz clic en "Agregar Especialidad" para comenzar</p>
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

    <!-- Modal para Agregar Especialidad -->
    <div class="modal fade" id="addSpecialtyModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-plus me-2"></i>Agregar Especialidad
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <form action="${pageContext.request.contextPath}/admin/specialties-list" method="post">
                    <input type="hidden" name="action" value="register">
                    
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="name" class="form-label">Nombre *</label>
                            <input type="text" class="form-control" id="name" name="name" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="code" class="form-label">Código</label>
                            <input type="text" class="form-control" id="code" name="code" placeholder="Ej: CAR, NEU">
                        </div>
                        
                        <div class="mb-3">
                            <label for="description" class="form-label">Descripción</label>
                            <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save me-2"></i>Guardar Especialidad
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal para Editar Especialidad -->
    <div class="modal fade" id="editSpecialtyModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-warning text-white">
                    <h5 class="modal-title">
                        <i class="fas fa-edit me-2"></i>Editar Especialidad
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <form action="${pageContext.request.contextPath}/admin/specialties-list" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" id="editSpecialtyId">
                    
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="editName" class="form-label">Nombre *</label>
                            <input type="text" class="form-control" id="editName" name="name" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="editCode" class="form-label">Código</label>
                            <input type="text" class="form-control" id="editCode" name="code">
                        </div>
                        
                        <div class="mb-3">
                            <label for="editDescription" class="form-label">Descripción</label>
                            <textarea class="form-control" id="editDescription" name="description" rows="3"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                            <i class="fas fa-times me-2"></i>Cancelar
                        </button>
                        <button type="submit" class="btn btn-warning">
                            <i class="fas fa-save me-2"></i>Actualizar Especialidad
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // Función para editar especialidad
        function editSpecialty(id) {
            // Buscar los datos de la especialidad en la tabla
            const rows = document.querySelectorAll('tbody tr');
            
            for (let row of rows) {
                const buttons = row.querySelectorAll('button[onclick*="editSpecialty"]');
                for (let button of buttons) {
                    const onclickAttr = button.getAttribute('onclick');
                    if (onclickAttr && onclickAttr.includes("'" + id + "'")) {
                        const cells = row.querySelectorAll('td');
                        
                        // Extraer datos
                        const name = cells[0].querySelector('strong').textContent.trim();
                        const codeElement = cells[1].querySelector('code');
                        const code = codeElement && codeElement.textContent !== 'N/A' ? codeElement.textContent.trim() : '';
                        const description = cells[2].textContent.trim() !== '-' ? cells[2].textContent.trim() : '';
                        
                        // Cargar datos en el modal de edición
                        document.getElementById('editSpecialtyId').value = id;
                        document.getElementById('editName').value = name;
                        document.getElementById('editCode').value = code;
                        document.getElementById('editDescription').value = description;
                        
                        // Mostrar el modal
                        const modal = new bootstrap.Modal(document.getElementById('editSpecialtyModal'));
                        modal.show();
                        return;
                    }
                }
            }
        }
        
        // Función para cambiar el estado de la especialidad
        function toggleSpecialtyStatus(id, activate) {
            if (confirm(activate ? '¿Activar esta especialidad?' : '¿Desactivar esta especialidad?')) {
                // Crear un formulario oculto para enviar la acción
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/admin/specialties-list';
                
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
        
        // Función para eliminar especialidad
        function deleteSpecialty(id) {
            if (confirm('¿Está seguro que desea eliminar permanentemente esta especialidad?')) {
                // Crear un formulario oculto para enviar la acción
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/admin/specialties-list';
                
                const actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = 'delete';
                
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
        
        // Asegurar que las funciones estén disponibles globalmente
        window.editSpecialty = editSpecialty;
        window.toggleSpecialtyStatus = toggleSpecialtyStatus;
        window.deleteSpecialty = deleteSpecialty;
    </script>
</body>
</html>

