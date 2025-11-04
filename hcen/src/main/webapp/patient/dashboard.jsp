<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Mi Historia Clínica</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            color: #2c3e50;
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
        }

        .header {
            background: white;
            padding: 25px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .header-left {
            flex: 1;
        }

        .logo {
            font-size: 28px;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .logo-icon {
            font-size: 32px;
            filter: none;
            -webkit-text-fill-color: initial;
        }

        .welcome-message {
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 5px;
            color: #2c3e50;
        }

        .user-details {
            color: #7f8c8d;
            font-size: 14px;
            margin-top: 8px;
        }

        .user-details span {
            margin-right: 20px;
            display: inline-flex;
            align-items: center;
            gap: 5px;
        }

        .logout-btn {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
        }

        .logout-btn:hover {
            background: linear-gradient(135deg, #5568d3, #6a3f8f);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 25px;
            margin-bottom: 40px;
        }

        .stat-card {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
            transition: all 0.3s ease;
            border-left: 4px solid #667eea;
        }

        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 35px rgba(102, 126, 234, 0.2);
        }

        .stat-icon {
            font-size: 40px;
            margin-bottom: 15px;
            opacity: 0.8;
        }

        .stat-value {
            font-size: 42px;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
        }

        .stat-label {
            color: #7f8c8d;
            font-size: 14px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .section-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #2c3e50;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .section-title::before {
            content: '';
            width: 4px;
            height: 30px;
            background: linear-gradient(180deg, #667eea, #764ba2);
            border-radius: 2px;
        }

        .menu-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 25px;
            margin-bottom: 40px;
        }

        .menu-card {
            background: white;
            padding: 40px 30px;
            border-radius: 15px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
            position: relative;
            overflow: hidden;
        }

        .menu-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, transparent 100%);
            opacity: 0;
            transition: opacity 0.3s ease;
        }

        .menu-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 15px 40px rgba(102, 126, 234, 0.25);
        }

        .menu-card:hover::before {
            opacity: 1;
        }

        .menu-icon {
            font-size: 56px;
            margin-bottom: 15px;
            display: block;
            transition: all 0.3s ease;
        }

        .menu-card:hover .menu-icon {
            transform: scale(1.1);
        }

        .menu-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-top: 10px;
            position: relative;
            z-index: 1;
        }

        .menu-description {
            font-size: 13px;
            color: #7f8c8d;
            margin-top: 8px;
            position: relative;
            z-index: 1;
        }

        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(102, 126, 234, 0.3);
            border-radius: 50%;
            border-top-color: #667eea;
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .error-message {
            background: rgba(231, 76, 60, 0.1);
            border: 1px solid rgba(231, 76, 60, 0.3);
            color: #e74c3c;
            padding: 15px 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            display: none;
        }

        .badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
        }

        .info-banner {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 25px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 5px 25px rgba(102, 126, 234, 0.3);
        }

        .info-banner h3 {
            font-size: 18px;
            margin-bottom: 8px;
        }

        .info-banner p {
            font-size: 14px;
            opacity: 0.9;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-message" id="errorMessage"></div>

        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">💚</span>
                    <span>Mi Historia Clínica</span>
                </div>
                <div class="welcome-message">
                    Bienvenido/a, <span id="userName">Cargando...</span>
                    <span class="badge" id="userRole">PACIENTE</span>
                </div>
                <div class="user-details">
                    <span>📋 CI: <strong id="userCi">...</strong></span>
                    <span>🔑 INUS ID: <strong id="inusId">...</strong></span>
                </div>
            </div>
            <button class="logout-btn" onclick="logout()">🚪 Cerrar Sesión</button>
        </div>

        <div class="info-banner">
            <h3>📱 Bienvenido a su Historia Clínica Electrónica Nacional</h3>
            <p>Aquí puede acceder a su información médica, gestionar quién puede ver sus documentos clínicos y revisar el historial de accesos. Su privacidad es nuestra prioridad.</p>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">📄</div>
                <div class="stat-value" id="myDocuments">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Mis Documentos</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🔒</div>
                <div class="stat-value" id="myPolicies">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Políticas Activas</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">👁️</div>
                <div class="stat-value" id="recentAccesses">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Accesos Recientes</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">⏳</div>
                <div class="stat-value" id="pendingApprovals">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Aprobaciones Pendientes</div>
            </div>
        </div>

        <div class="section-title">Mi Información de Salud</div>

        <div class="menu-grid">
            <div class="menu-card" onclick="navigateTo('/hcen/patient/clinical-history.jsp')">
                <span class="menu-icon">📋</span>
                <div class="menu-title">Mi Historial</div>
                <div class="menu-description">Ver todos mis documentos clínicos</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/patient/access-policies.jsp')">
                <span class="menu-icon">🔒</span>
                <div class="menu-title">Políticas de Acceso</div>
                <div class="menu-description">Gestionar quién puede ver mi información</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/patient/audit-logs.jsp')">
                <span class="menu-icon">🔍</span>
                <div class="menu-title">Auditoría</div>
                <div class="menu-description">Ver quién accedió a mis datos</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/patient/pending-requests.jsp')">
                <span class="menu-icon">✅</span>
                <div class="menu-title">Aprobaciones</div>
                <div class="menu-description">Autorizar solicitudes de acceso</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/patient/notifications')">
                <span class="menu-icon">🔔</span>
                <div class="menu-title">Notificaciones</div>
                <div class="menu-description">Ver alertas y mensajes del sistema</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/patient/profile')">
                <span class="menu-icon">👤</span>
                <div class="menu-title">Mi Perfil</div>
                <div class="menu-description">Actualizar información personal</div>
            </div>
        </div>
    </div>

    <script>
        // API Configuration
        const API_BASE = '/hcen/api';

        /**
         * Get JWT token from URL parameter or sessionStorage
         */
        function getTokenFromUrl() {
            const params = new URLSearchParams(window.location.search);
            const token = params.get('token');

            if (token) {
                sessionStorage.setItem('accessToken', token);
                // Remove token from URL for security
                window.history.replaceState({}, document.title, window.location.pathname);
                console.log('Token stored from URL');
            }

            return token || sessionStorage.getItem('accessToken');
        }

        /**
         * Parse JWT token to extract claims
         */
        function parseJwt(token) {
            try {
                const base64Url = token.split('.')[1];
                const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                const jsonPayload = decodeURIComponent(
                    atob(base64).split('').map(function(c) {
                        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                    }).join('')
                );
                return JSON.parse(jsonPayload);
            } catch (e) {
                console.error('Error parsing JWT:', e);
                return null;
            }
        }

        /**
         * Make authenticated API call
         */
        async function apiCall(endpoint, options = {}) {
            const token = sessionStorage.getItem('accessToken');

            if (!token) {
                console.error('No access token found');
                showError('Sesión expirada. Por favor, inicie sesión nuevamente.');
                setTimeout(() => {
                    window.location.href = '/hcen/login-patient.jsp';
                }, 2000);
                return null;
            }

            try {
                const response = await fetch(API_BASE + endpoint, {
                    ...options,
                    headers: {
                        ...options.headers,
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 401) {
                    console.error('Unauthorized - token expired or invalid');
                    sessionStorage.removeItem('accessToken');
                    showError('Sesión expirada. Redirigiendo...');
                    setTimeout(() => {
                        window.location.href = '/hcen/login-patient.jsp';
                    }, 2000);
                    return null;
                }

                if (!response.ok) {
                    throw new Error(`API call failed: ${response.status}`);
                }

                return await response.json();
            } catch (error) {
                console.error('API call error:', error);
                return null;
            }
        }

        /**
         * Show error message
         */
        function showError(message) {
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';

            setTimeout(() => {
                errorDiv.style.display = 'none';
            }, 5000);
        }

        /**
         * Load user information from JWT
         */
        function loadUserInfo() {
            const token = getTokenFromUrl();

            if (!token) {
                console.error('No token found');
                showError('No se encontró token de acceso. Redirigiendo...');
                setTimeout(() => {
                    window.location.href = '/hcen/login-patient.jsp';
                }, 2000);
                return;
            }

            const claims = parseJwt(token);

            if (!claims) {
                console.error('Could not parse JWT');
                showError('Token inválido. Redirigiendo...');
                setTimeout(() => {
                    window.location.href = '/hcen/login-patient.jsp';
                }, 2000);
                return;
            }

            console.log('JWT Claims:', claims);

            // Verify patient role
            if (claims.role !== 'PATIENT') {
                console.error('Access denied - not a patient', claims.role);
                showError('Acceso denegado. Esta página es solo para pacientes.');
                setTimeout(() => {
                    window.location.href = '/hcen/login-admin.jsp';
                }, 2000);
                return;
            }

            // Display user information
            document.getElementById('userName').textContent = claims.firstName || claims.name || 'Paciente';
            document.getElementById('userCi').textContent = claims.sub || claims.ci || 'N/A';
            document.getElementById('inusId').textContent = claims.inusId || 'N/A';
            document.getElementById('userRole').textContent = 'PACIENTE';
        }

        /**
         * Load patient statistics
         */
        async function loadStatistics() {
            try {
                // For now, show placeholder data since statistics endpoints are not implemented
                // TODO: Implement actual API endpoints for patient statistics

                // Simulate loading delay
                await new Promise(resolve => setTimeout(resolve, 1000));

                // Display mock data
                document.getElementById('myDocuments').textContent = '0';
                document.getElementById('myPolicies').textContent = '0';
                document.getElementById('recentAccesses').textContent = '0';
                document.getElementById('pendingApprovals').textContent = '0';

                // When actual endpoints are ready:
                /*
                const patientCi = parseJwt(sessionStorage.getItem('accessToken')).sub;
                const stats = await apiCall(`/patients/${patientCi}/statistics`);
                if (stats) {
                    document.getElementById('myDocuments').textContent = stats.documentCount || '0';
                    document.getElementById('myPolicies').textContent = stats.policyCount || '0';
                    document.getElementById('recentAccesses').textContent = stats.recentAccessCount || '0';
                    document.getElementById('pendingApprovals').textContent = stats.pendingApprovalCount || '0';
                }
                */
            } catch (error) {
                console.error('Error loading statistics:', error);
                document.getElementById('myDocuments').textContent = 'Error';
                document.getElementById('myPolicies').textContent = 'Error';
                document.getElementById('recentAccesses').textContent = 'Error';
                document.getElementById('pendingApprovals').textContent = 'Error';
            }
        }

        /**
         * Logout user
         */
        async function logout() {
            try {
                const token = sessionStorage.getItem('accessToken');

                if (token) {
                    // Call backend logout endpoint
                    await fetch(API_BASE + '/auth/logout', {
                        method: 'POST',
                        headers: {
                            'Authorization': 'Bearer ' + token
                        }
                    });
                }
            } catch (error) {
                console.error('Error during logout:', error);
            } finally {
                // Always clear local session
                sessionStorage.removeItem('accessToken');
                window.location.href = '/hcen/login-patient.jsp';
            }
        }

        /**
         * Navigate to page
         */
        function navigateTo(url) {
            // Check if these pages are implemented
            if (url.includes('pending-requests.jsp') ||
                url.includes('access-policies.jsp') ||
                url.includes('clinical-history.jsp') ||
                url.includes('audit-logs.jsp')) {
                window.location.href = url;
                return;
            }

            alert('Esta función estará disponible próximamente.\n\nURL: ' + url);
            // Uncomment when pages are ready:
            // window.location.href = url;
        }

        /**
         * Initialize dashboard on page load
         */
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Patient Dashboard initializing...');
            loadUserInfo();
            loadStatistics();
        });
    </script>
</body>
</html>
