<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
                        <li><a class="dropdown-item" href="#">
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
                                <a class="nav-link active" href="<c:url value='/professional/dashboard.jsp'/>">
                                    <i class="fas fa-tachometer-alt me-2"></i>Dashboard
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/patients.jsp'/>">
                                    <i class="fas fa-users me-2"></i>Mis Pacientes
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/documents.jsp'/>">
                                    <i class="fas fa-file-medical me-2"></i>Documentos
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/requests.jsp'/>">
                                    <i class="fas fa-exchange-alt me-2"></i>Solicitudes
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="<c:url value='/professional/history.jsp'/>">
                                    <i class="fas fa-history me-2"></i>Historia Clínica
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
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-users"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">24</h4>
                                    <small class="opacity-75">Pacientes Activos</small>
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
                                    <h4 class="mb-0">156</h4>
                                    <small class="opacity-75">Documentos Creados</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-clock"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">8</h4>
                                    <small class="opacity-75">Solicitudes Pendientes</small>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="card stat-card">
                            <div class="card-body d-flex align-items-center">
                                <div class="stat-icon me-3">
                                    <i class="fas fa-calendar-check"></i>
                                </div>
                                <div>
                                    <h4 class="mb-0">12</h4>
                                    <small class="opacity-75">Citas Hoy</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Contenido Principal -->
                <div class="row">
                    <!-- Pacientes Recientes -->
                    <div class="col-md-6">
                        <div class="card">
                            <div class="card-header bg-white">
                                <h5 class="mb-0">
                                    <i class="fas fa-users me-2"></i>Pacientes Recientes
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="patient-card">
                                    <div class="d-flex align-items-center">
                                        <div class="patient-avatar me-3">
                                            AS
                                        </div>
                                        <div class="flex-grow-1">
                                            <h6 class="mb-1">Ana Silva</h6>
                                            <small class="text-muted">
                                                <i class="fas fa-id-card me-1"></i>12345678
                                                <span class="ms-2">
                                                    <i class="fas fa-birthday-cake me-1"></i>38 años
                                                </span>
                                            </small>
                                        </div>
                                        <div class="text-end">
                                            <small class="text-muted">Última consulta</small>
                                            <div>Hace 2 días</div>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="patient-card">
                                    <div class="d-flex align-items-center">
                                        <div class="patient-avatar me-3">
                                            RM
                                        </div>
                                        <div class="flex-grow-1">
                                            <h6 class="mb-1">Roberto Martínez</h6>
                                            <small class="text-muted">
                                                <i class="fas fa-id-card me-1"></i>87654321
                                                <span class="ms-2">
                                                    <i class="fas fa-birthday-cake me-1"></i>45 años
                                                </span>
                                            </small>
                                        </div>
                                        <div class="text-end">
                                            <small class="text-muted">Última consulta</small>
                                            <div>Hace 5 días</div>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="patient-card">
                                    <div class="d-flex align-items-center">
                                        <div class="patient-avatar me-3">
                                            LF
                                        </div>
                                        <div class="flex-grow-1">
                                            <h6 class="mb-1">Lucía Fernández</h6>
                                            <small class="text-muted">
                                                <i class="fas fa-id-card me-1"></i>11223344
                                                <span class="ms-2">
                                                    <i class="fas fa-birthday-cake me-1"></i>31 años
                                                </span>
                                            </small>
                                        </div>
                                        <div class="text-end">
                                            <small class="text-muted">Última consulta</small>
                                            <div>Hace 1 semana</div>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="text-center mt-3">
                                    <a href="<c:url value='/professional/patients.jsp'/>" class="btn btn-outline-primary">
                                        <i class="fas fa-eye me-2"></i>Ver Todos los Pacientes
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Documentos y Solicitudes -->
                    <div class="col-md-6">
                        <div class="card">
                            <div class="card-header bg-white">
                                <h5 class="mb-0">
                                    <i class="fas fa-file-medical me-2"></i>Documentos Recientes
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="document-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <strong>Consulta Cardiológica</strong>
                                            <br><small class="text-muted">Ana Silva - 15/12/2024</small>
                                        </div>
                                        <span class="status-badge status-approved">Completado</span>
                                    </div>
                                </div>
                                
                                <div class="document-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <strong>Electrocardiograma</strong>
                                            <br><small class="text-muted">Roberto Martínez - 14/12/2024</small>
                                        </div>
                                        <span class="status-badge status-pending">En Proceso</span>
                                    </div>
                                </div>
                                
                                <div class="document-item">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <div>
                                            <strong>Ecocardiograma</strong>
                                            <br><small class="text-muted">Lucía Fernández - 13/12/2024</small>
                                        </div>
                                        <span class="status-badge status-approved">Completado</span>
                                    </div>
                                </div>
                                
                                <div class="text-center mt-3">
                                    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#newDocumentModal">
                                        <i class="fas fa-plus me-2"></i>Nuevo Documento
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Solicitudes de Acceso -->
                <div class="row mt-4">
                    <div class="col-12">
                        <div class="card">
                            <div class="card-header bg-white">
                                <h5 class="mb-0">
                                    <i class="fas fa-exchange-alt me-2"></i>Solicitudes de Acceso Pendientes
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="table-responsive">
                                    <table class="table table-hover">
                                        <thead>
                                            <tr>
                                                <th>Paciente</th>
                                                <th>Tipo de Documento</th>
                                                <th>Clínica Origen</th>
                                                <th>Fecha Solicitud</th>
                                                <th>Estado</th>
                                                <th>Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td>
                                                    <div class="d-flex align-items-center">
                                                        <div class="patient-avatar me-2" style="width: 30px; height: 30px; font-size: 0.8rem;">
                                                            AS
                                                        </div>
                                                        <span>Ana Silva</span>
                                                    </div>
                                                </td>
                                                <td>Resonancia Magnética</td>
                                                <td>Centro Neurológico</td>
                                                <td>13/12/2024</td>
                                                <td><span class="status-badge status-pending">Pendiente</span></td>
                                                <td>
                                                    <div class="btn-group btn-group-sm">
                                                        <button class="btn btn-outline-success" title="Aprobar">
                                                            <i class="fas fa-check"></i>
                                                        </button>
                                                        <button class="btn btn-outline-danger" title="Rechazar">
                                                            <i class="fas fa-times"></i>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                            
                                            <tr>
                                                <td>
                                                    <div class="d-flex align-items-center">
                                                        <div class="patient-avatar me-2" style="width: 30px; height: 30px; font-size: 0.8rem;">
                                                            RM
                                                        </div>
                                                        <span>Roberto Martínez</span>
                                                    </div>
                                                </td>
                                                <td>Tomografía Computada</td>
                                                <td>Hospital Central</td>
                                                <td>12/12/2024</td>
                                                <td><span class="status-badge status-pending">Pendiente</span></td>
                                                <td>
                                                    <div class="btn-group btn-group-sm">
                                                        <button class="btn btn-outline-success" title="Aprobar">
                                                            <i class="fas fa-check"></i>
                                                        </button>
                                                        <button class="btn btn-outline-danger" title="Rechazar">
                                                            <i class="fas fa-times"></i>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
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
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
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
