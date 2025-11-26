<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Seleccionar Clínica - HCEN</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #6f42c1;
            --secondary-color: #5a32a3;
        }
        
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        
        .clinic-card {
            border: none;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.08);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            cursor: pointer;
        }
        
        .clinic-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
        }
        
        .clinic-card.selected {
            border: 2px solid var(--primary-color);
            background: linear-gradient(135deg, rgba(111, 66, 193, 0.1), rgba(90, 50, 163, 0.1));
        }
        
        .btn-primary {
            background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
            border: none;
            border-radius: 25px;
            padding: 12px 30px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(111, 66, 193, 0.3);
        }
        
        .btn-primary:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        
        .alert {
            border-radius: 10px;
            border: none;
        }
        
        .header-section {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            border-radius: 15px;
            padding: 2rem;
            margin-bottom: 2rem;
        }
    </style>
</head>
<body>
    <div class="container-fluid mt-4">
        <div class="row justify-content-center">
            <div class="col-md-10">
                <!-- Header -->
                <div class="header-section">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h1 class="mb-2">
                                <i class="fas fa-hospital me-3"></i>Seleccionar Clínica
                            </h1>
                            <p class="mb-0 opacity-75">Como super administrador, selecciona la clínica para la cual deseas gestionar información</p>
                        </div>
                        <div class="col-md-4 text-end">
                            <i class="fas fa-building fa-4x opacity-50"></i>
                        </div>
                    </div>
                </div>

                <!-- Mensajes -->
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-triangle me-2"></i>${error}
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>
                </c:if>

                <!-- Clínicas -->
                <div class="row">
                    <c:choose>
                        <c:when test="${not empty clinics}">
                            <c:forEach var="clinic" items="${clinics}">
                                <div class="col-md-6 mb-4">
                                    <div class="card clinic-card" onclick="selectClinic(${clinic.id}, '${clinic.name}')">
                                        <div class="card-body">
                                            <div class="d-flex justify-content-between align-items-start mb-3">
                                                <div>
                                                    <h5 class="card-title mb-1">
                                                        <i class="fas fa-hospital me-2 text-primary"></i>${clinic.name}
                                                    </h5>
                                                    <p class="text-muted mb-0">ID: ${clinic.id}</p>
                                                </div>
                                                <i class="fas fa-check-circle fa-2x text-muted" id="check-${clinic.id}" style="display: none;"></i>
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
                                            
                                            <div class="text-center">
                                                <span class="badge bg-primary">Hacer clic para seleccionar</span>
                                            </div>
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

                <!-- Botones de acción -->
                <div class="row mt-4">
                    <div class="col-12 text-center">
                        <button type="button" class="btn btn-primary btn-lg" id="continueBtn" disabled onclick="continueToSection()">
                            <i class="fas fa-arrow-right me-2"></i>Continuar
                        </button>
                        <a href="<c:url value='/admin/super-admin'/>" class="btn btn-secondary btn-lg ms-3">
                            <i class="fas fa-crown me-2"></i>Volver a Super Admin
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Formulario oculto para enviar la selección -->
    <form id="clinicForm" method="post" style="display: none;">
        <input type="hidden" id="clinicId" name="clinicId">
        <input type="hidden" id="targetUrl" name="targetUrl">
    </form>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let selectedClinicId = null;
        let selectedClinicName = null;
        
        function selectClinic(clinicId, clinicName) {
            // Remover selección anterior
            document.querySelectorAll('.clinic-card').forEach(card => {
                card.classList.remove('selected');
            });
            document.querySelectorAll('[id^="check-"]').forEach(check => {
                check.style.display = 'none';
            });
            
            // Seleccionar nueva clínica
            const card = document.querySelector(`[onclick="selectClinic(${clinicId}, '${clinicName}')"]`);
            card.classList.add('selected');
            document.getElementById(`check-${clinicId}`).style.display = 'block';
            
            selectedClinicId = clinicId;
            selectedClinicName = clinicName;
            
            // Habilitar botón continuar
            document.getElementById('continueBtn').disabled = false;
            document.getElementById('continueBtn').innerHTML = 
                `<i class="fas fa-arrow-right me-2"></i>Continuar con ${clinicName}`;
        }
        
        function continueToSection() {
            if (selectedClinicId) {
                // Obtener la URL objetivo de los parámetros
                const urlParams = new URLSearchParams(window.location.search);
                const targetUrl = urlParams.get('target') || '/admin/dashboard.jsp';
                
                document.getElementById('clinicId').value = selectedClinicId;
                document.getElementById('targetUrl').value = targetUrl;
                document.getElementById('clinicForm').submit();
            }
        }
    </script>
</body>
</html>
