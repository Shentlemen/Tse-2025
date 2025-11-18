<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Componente Periférico de Clínica</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 0;
        }
        
        .login-container {
            background: rgba(255, 255, 255, 0.98);
            border-radius: 20px;
            box-shadow: 0 25px 50px rgba(0,0,0,0.15);
            backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            overflow: hidden;
        }
        
        .logo-section {
            background: linear-gradient(135deg, #4CAF50, #45a049);
            color: white;
            padding: 3rem 2rem;
            text-align: center;
            position: relative;
        }
        
        .logo-section::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
            opacity: 0.3;
        }
        
        .logo-section h2 {
            position: relative;
            z-index: 1;
            font-weight: 700;
            font-size: 2.2rem;
            margin-bottom: 0.5rem;
            text-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .logo-section p {
            position: relative;
            z-index: 1;
            opacity: 0.9;
            font-size: 1.1rem;
            margin: 0;
        }
        
        .logo-icon {
            position: relative;
            z-index: 1;
            font-size: 3.5rem;
            margin-bottom: 1rem;
            filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1));
        }
        
        .form-section {
            padding: 2.5rem;
        }
        
        .form-label {
            font-weight: 600;
            color: #333;
            margin-bottom: 0.75rem;
            font-size: 0.95rem;
        }
        
        .form-control {
            border: 2px solid #e9ecef;
            border-radius: 12px;
            padding: 0.875rem 1rem;
            font-size: 1rem;
            transition: all 0.3s ease;
            background-color: #fafafa;
        }
        
        .form-control:focus {
            border-color: #4CAF50;
            box-shadow: 0 0 0 0.2rem rgba(76, 175, 80, 0.15);
            background-color: white;
            transform: translateY(-1px);
        }
        
        .btn-login {
            background: linear-gradient(135deg, #4CAF50, #45a049);
            border: none;
            border-radius: 12px;
            padding: 0.875rem 2rem;
            font-weight: 600;
            font-size: 1rem;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .btn-login::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }
        
        .btn-login:hover::before {
            left: 100%;
        }
        
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(76, 175, 80, 0.3);
        }
        
        .btn-login:active {
            transform: translateY(0);
        }
        
        .btn-outline-secondary {
            border: 2px solid #6c757d;
            border-radius: 12px;
            padding: 0.5rem 1.5rem;
            font-weight: 500;
            transition: all 0.3s ease;
        }
        
        .btn-outline-secondary:hover {
            background-color: #6c757d;
            border-color: #6c757d;
            transform: translateY(-1px);
        }
        
        .feature-card {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 20px;
            padding: 2rem;
            margin: 1rem 0;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
            border: 1px solid rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(10px);
        }
        
        .feature-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 20px 40px rgba(0,0,0,0.15);
        }
        
        .feature-icon {
            background: linear-gradient(135deg, #4CAF50, #45a049);
            color: white;
            width: 70px;
            height: 70px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.5rem;
            font-size: 1.8rem;
            box-shadow: 0 8px 20px rgba(76, 175, 80, 0.3);
        }
        
        .feature-card h5 {
            font-weight: 700;
            color: #333;
            margin-bottom: 1rem;
            text-align: center;
        }
        
        .feature-card p {
            color: #666;
            text-align: center;
            line-height: 1.6;
            margin: 0;
        }
        
        .alert {
            border-radius: 12px;
            border: none;
            padding: 1rem 1.25rem;
            margin-bottom: 1.5rem;
            font-weight: 500;
        }
        
        .alert-danger {
            background: linear-gradient(135deg, #ffebee, #ffcdd2);
            color: #c62828;
        }
        
        .alert-success {
            background: linear-gradient(135deg, #e8f5e8, #c8e6c9);
            color: #2e7d32;
        }
        
        .main-title {
            font-size: 3.5rem;
            font-weight: 800;
            margin-bottom: 1rem;
            text-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        
        .subtitle {
            font-size: 1.4rem;
            font-weight: 300;
            margin-bottom: 0.5rem;
            opacity: 0.9;
        }
        
        .description {
            font-size: 1.1rem;
            opacity: 0.8;
            font-weight: 400;
        }
        
        .floating-elements {
            position: absolute;
            width: 100%;
            height: 100%;
            overflow: hidden;
            pointer-events: none;
        }
        
        .floating-circle {
            position: absolute;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.1);
            animation: float 6s ease-in-out infinite;
        }
        
        .floating-circle:nth-child(1) {
            width: 80px;
            height: 80px;
            top: 20%;
            left: 10%;
            animation-delay: 0s;
        }
        
        .floating-circle:nth-child(2) {
            width: 120px;
            height: 120px;
            top: 60%;
            right: 15%;
            animation-delay: 2s;
        }
        
        .floating-circle:nth-child(3) {
            width: 60px;
            height: 60px;
            bottom: 20%;
            left: 20%;
            animation-delay: 4s;
        }
        
        @keyframes float {
            0%, 100% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-20px) rotate(180deg); }
        }
        
        .input-group {
            position: relative;
        }
        
        .input-group .form-control {
            padding-left: 3rem;
        }
        
        .input-group .input-icon {
            position: absolute;
            left: 1rem;
            top: 50%;
            transform: translateY(-50%);
            color: #6c757d;
            z-index: 3;
        }
        
        @media (max-width: 768px) {
            .main-title {
                font-size: 2.5rem;
            }
            
            .subtitle {
                font-size: 1.2rem;
            }
            
            .form-section {
                padding: 2rem;
            }
            
            .feature-card {
                padding: 1.5rem;
            }
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row min-vh-100">
            <!-- Panel de Login -->
            <div class="col-md-6 d-flex align-items-center justify-content-center p-5">
                <div class="login-container w-100" style="max-width: 450px;">
                    <div class="logo-section">
                        <i class="fas fa-hospital logo-icon"></i>
                        <h2>HCEN Clínica</h2>
                        <p>Componente Periférico</p>
                    </div>
                    
                    <div class="form-section">
                        <!-- Mostrar errores si existen -->
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger" role="alert">
                                <i class="fas fa-exclamation-triangle me-2"></i>${error}
                            </div>
                        </c:if>
                        
                        <!-- Mostrar mensaje de logout exitoso -->
                        <c:if test="${not empty param.logout}">
                            <div class="alert alert-success" role="alert">
                                <i class="fas fa-check-circle me-2"></i>Sesión cerrada correctamente. Puedes iniciar sesión con otra clínica.
                            </div>
                        </c:if>
                        
                        <form action="<c:url value='/auth/login'/>" method="post">
                            <div class="mb-4">
                                <label for="username" class="form-label">
                                    <i class="fas fa-user me-2"></i>Usuario
                                </label>
                                <div class="input-group">
                                    <i class="fas fa-user input-icon"></i>
                                    <input type="text" class="form-control" id="username" name="username" 
                                           placeholder="Ingrese su usuario" required>
                                </div>
                            </div>
                            
                            <div class="mb-4">
                                <label for="password" class="form-label">
                                    <i class="fas fa-lock me-2"></i>Contraseña
                                </label>
                                <div class="input-group">
                                    <i class="fas fa-lock input-icon"></i>
                                    <input type="password" class="form-control" id="password" name="password" 
                                           placeholder="Ingrese su contraseña" required>
                                </div>
                            </div>
                            
                            <div class="d-grid mb-3">
                                <button type="submit" class="btn btn-login text-white">
                                    <i class="fas fa-sign-in-alt me-2"></i>Iniciar Sesión
                                </button>
                            </div>
                        </form>
                        
                    </div>
                </div>
            </div>
            
            <!-- Panel de Información -->
            <div class="col-md-6 d-flex align-items-center p-5 position-relative">
                <div class="floating-elements">
                    <div class="floating-circle"></div>
                    <div class="floating-circle"></div>
                    <div class="floating-circle"></div>
                </div>
                
                <div class="w-100">
                    <div class="text-center text-white mb-5">
                        <h1 class="main-title">Sistema HCEN</h1>
                        <p class="subtitle">Historia Clínica Electrónica Nacional</p>
                        <p class="description">Componente Periférico para Clínicas</p>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="feature-card">
                                <div class="feature-icon">
                                    <i class="fas fa-user-md"></i>
                                </div>
                                <h5 class="text-center">Portal Profesionales</h5>
                                <p class="text-center text-muted">
                                    Gestión de documentos clínicos, acceso a historias y solicitudes de acceso.
                                </p>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="feature-card">
                                <div class="feature-icon">
                                    <i class="fas fa-cogs"></i>
                                </div>
                                <h5 class="text-center">Portal Administración</h5>
                                <p class="text-center text-muted">
                                    Gestión de profesionales, pacientes y configuración de la clínica.
                                </p>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="feature-card">
                                <div class="feature-icon">
                                    <i class="fas fa-shield-alt"></i>
                                </div>
                                <h5 class="text-center">Seguridad</h5>
                                <p class="text-center text-muted">
                                    Autenticación segura y políticas de acceso a documentos.
                                </p>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="feature-card">
                                <div class="feature-icon">
                                    <i class="fas fa-network-wired"></i>
                                </div>
                                <h5 class="text-center">Integración HCEN</h5>
                                <p class="text-center text-muted">
                                    Conectividad con el sistema central de HCEN.
                                </p>
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
