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
        }
        .login-container {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            backdrop-filter: blur(10px);
        }
        .logo-section {
            background: linear-gradient(45deg, #4CAF50, #45a049);
            color: white;
            border-radius: 20px 20px 0 0;
            padding: 2rem;
            text-align: center;
        }
        .btn-login {
            background: linear-gradient(45deg, #4CAF50, #45a049);
            border: none;
            border-radius: 25px;
            padding: 12px 30px;
            font-weight: 600;
            transition: all 0.3s ease;
        }
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(76, 175, 80, 0.3);
        }
        .feature-card {
            background: rgba(255, 255, 255, 0.9);
            border-radius: 15px;
            padding: 1.5rem;
            margin: 1rem 0;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s ease;
        }
        .feature-card:hover {
            transform: translateY(-5px);
        }
        .feature-icon {
            background: linear-gradient(45deg, #4CAF50, #45a049);
            color: white;
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1rem;
            font-size: 1.5rem;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row min-vh-100">
            <!-- Panel de Login -->
            <div class="col-md-6 d-flex align-items-center justify-content-center p-5">
                <div class="login-container w-100" style="max-width: 400px;">
                    <div class="logo-section">
                        <i class="fas fa-hospital fa-3x mb-3"></i>
                        <h2 class="mb-0">HCEN Clínica</h2>
                        <p class="mb-0 opacity-75">Componente Periférico</p>
                    </div>
                    
                    <div class="p-4">
                        <form action="<c:url value='/auth/login'/>" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label">
                                    <i class="fas fa-user me-2"></i>Usuario
                                </label>
                                <input type="text" class="form-control" id="username" name="username" 
                                       placeholder="Ingrese su usuario" required>
                            </div>
                            
                            <div class="mb-4">
                                <label for="password" class="form-label">
                                    <i class="fas fa-lock me-2"></i>Contraseña
                                </label>
                                <input type="password" class="form-control" id="password" name="password" 
                                       placeholder="Ingrese su contraseña" required>
                            </div>
                            
                            <div class="d-grid">
                                <button type="submit" class="btn btn-login text-white">
                                    <i class="fas fa-sign-in-alt me-2"></i>Iniciar Sesión
                                </button>
                            </div>
                        </form>
                        
                        <hr class="my-4">
                        
                        <div class="text-center">
                            <p class="text-muted mb-2">Usuarios de prueba:</p>
                            <small class="text-muted">
                                <strong>Admin:</strong> admin / admin123<br>
                                <strong>Profesional:</strong> prof / prof123
                            </small>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Panel de Información -->
            <div class="col-md-6 d-flex align-items-center p-5">
                <div class="w-100">
                    <div class="text-center text-white mb-5">
                        <h1 class="display-4 fw-bold mb-3">Sistema HCEN</h1>
                        <p class="lead">Historia Clínica Electrónica Nacional</p>
                        <p class="opacity-75">Componente Periférico para Clínicas</p>
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
