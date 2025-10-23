<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Panel de Administración</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            color: #e4e4e4;
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
        }

        .header {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 25px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .header-left {
            flex: 1;
        }

        .logo {
            font-size: 28px;
            font-weight: bold;
            color: #00d4ff;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .logo-icon {
            font-size: 32px;
        }

        .welcome-message {
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 5px;
        }

        .user-details {
            color: #a0a0a0;
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
            background: linear-gradient(135deg, #ff4444, #cc0000);
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(255, 68, 68, 0.3);
        }

        .logout-btn:hover {
            background: linear-gradient(135deg, #cc0000, #990000);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(255, 68, 68, 0.4);
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 25px;
            margin-bottom: 40px;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 30px;
            border-radius: 15px;
            border-left: 4px solid #00d4ff;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }

        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, transparent 100%);
            opacity: 0;
            transition: opacity 0.3s ease;
        }

        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 12px 40px rgba(0, 212, 255, 0.2);
        }

        .stat-card:hover::before {
            opacity: 1;
        }

        .stat-icon {
            font-size: 40px;
            margin-bottom: 15px;
            opacity: 0.8;
        }

        .stat-value {
            font-size: 42px;
            font-weight: bold;
            color: #00d4ff;
            margin-bottom: 8px;
            position: relative;
            z-index: 1;
        }

        .stat-label {
            color: #a0a0a0;
            font-size: 14px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .section-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #fff;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .section-title::before {
            content: '';
            width: 4px;
            height: 30px;
            background: linear-gradient(180deg, #00d4ff, #0095ff);
            border-radius: 2px;
        }

        .menu-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 25px;
            margin-bottom: 40px;
        }

        .menu-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 40px 30px;
            border-radius: 15px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
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
            background: linear-gradient(135deg, rgba(0, 212, 255, 0.1) 0%, transparent 100%);
            opacity: 0;
            transition: opacity 0.3s ease;
        }

        .menu-card:hover {
            background: rgba(255, 255, 255, 0.1);
            transform: translateY(-8px);
            box-shadow: 0 16px 48px rgba(0, 212, 255, 0.25);
        }

        .menu-card:hover::before {
            opacity: 1;
        }

        .menu-icon {
            font-size: 56px;
            margin-bottom: 15px;
            display: block;
            filter: grayscale(20%);
            transition: all 0.3s ease;
        }

        .menu-card:hover .menu-icon {
            filter: grayscale(0%);
            transform: scale(1.1);
        }

        .menu-title {
            font-size: 18px;
            font-weight: 600;
            color: #fff;
            margin-top: 10px;
            position: relative;
            z-index: 1;
        }

        .menu-description {
            font-size: 13px;
            color: #a0a0a0;
            margin-top: 8px;
            position: relative;
            z-index: 1;
        }

        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: #00d4ff;
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .error-message {
            background: rgba(255, 68, 68, 0.1);
            border: 1px solid rgba(255, 68, 68, 0.3);
            color: #ff4444;
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
            background: rgba(0, 212, 255, 0.2);
            color: #00d4ff;
            border: 1px solid rgba(0, 212, 255, 0.3);
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-message" id="errorMessage"></div>

        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">🏥</span>
                    <span>HCEN</span>
                </div>
                <div class="welcome-message">
                    Bienvenido, <span id="userName">Cargando...</span>
                    <span class="badge" id="userRole">ADMIN</span>
                </div>
                <div class="user-details">
                    <span>📋 CI: <strong id="userCi">...</strong></span>
                    <span>🔑 INUS ID: <strong id="inusId">...</strong></span>
                </div>
            </div>
            <button class="logout-btn" onclick="logout()">🚪 Cerrar Sesión</button>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">👥</div>
                <div class="stat-value" id="totalUsers">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Usuarios Registrados</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">📄</div>
                <div class="stat-value" id="totalDocuments">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Documentos en RNDC</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🔒</div>
                <div class="stat-value" id="totalPolicies">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Políticas Activas</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🏥</div>
                <div class="stat-value" id="totalClinics">
                    <span class="loading"></span>
                </div>
                <div class="stat-label">Clínicas Registradas</div>
            </div>
        </div>

        <div class="section-title">Gestión del Sistema</div>

        <div class="menu-grid">
            <div class="menu-card" onclick="navigateTo('/hcen/admin/clinics')">
                <span class="menu-icon">🏥</span>
                <div class="menu-title">Gestión de Clínicas</div>
                <div class="menu-description">Administrar clínicas y centros de salud</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/admin/users')">
                <span class="menu-icon">👥</span>
                <div class="menu-title">Usuarios INUS</div>
                <div class="menu-description">Gestionar usuarios del sistema nacional</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/admin/reports')">
                <span class="menu-icon">📊</span>
                <div class="menu-title">Reportes</div>
                <div class="menu-description">Estadísticas y análisis del sistema</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/admin/audit')">
                <span class="menu-icon">🔍</span>
                <div class="menu-title">Auditoría</div>
                <div class="menu-description">Registro de accesos y actividades</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/admin/policies')">
                <span class="menu-icon">🔒</span>
                <div class="menu-title">Políticas Globales</div>
                <div class="menu-description">Configurar políticas de acceso</div>
            </div>
            <div class="menu-card" onclick="navigateTo('/hcen/admin/settings')">
                <span class="menu-icon">⚙️</span>
                <div class="menu-title">Configuración</div>
                <div class="menu-description">Ajustes del sistema HCEN</div>
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
                    window.location.href = '/hcen/login-admin.jsp';
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
                        window.location.href = '/hcen/login-admin.jsp';
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
                    window.location.href = '/hcen/login-admin.jsp';
                }, 2000);
                return;
            }

            const claims = parseJwt(token);

            if (!claims) {
                console.error('Could not parse JWT');
                showError('Token inválido. Redirigiendo...');
                setTimeout(() => {
                    window.location.href = '/hcen/login-admin.jsp';
                }, 2000);
                return;
            }

            console.log('JWT Claims:', claims);

            // Verify admin role
            if (claims.role !== 'ADMIN') {
                console.error('Access denied - not an admin', claims.role);
                showError('Acceso denegado. Esta página es solo para administradores.');
                setTimeout(() => {
                    window.location.href = '/hcen/login-patient.jsp';
                }, 2000);
                return;
            }

            // Display user information
            document.getElementById('userName').textContent = claims.firstName || claims.name || 'Admin';
            document.getElementById('userCi').textContent = claims.sub || claims.ci || 'N/A';
            document.getElementById('inusId').textContent = claims.inusId || 'N/A';
            document.getElementById('userRole').textContent = claims.role || 'ADMIN';
        }

        /**
         * Load system statistics
         */
        async function loadStatistics() {
            try {
                // For now, show placeholder data since statistics endpoints are not implemented
                // TODO: Implement actual API endpoints for statistics

                // Simulate loading delay
                await new Promise(resolve => setTimeout(resolve, 1000));

                // Display mock data
                document.getElementById('totalUsers').textContent = '0';
                document.getElementById('totalDocuments').textContent = '0';
                document.getElementById('totalPolicies').textContent = '0';
                document.getElementById('totalClinics').textContent = '0';

                // When actual endpoints are ready:
                /*
                const stats = await apiCall('/admin/statistics');
                if (stats) {
                    document.getElementById('totalUsers').textContent = stats.totalUsers || '0';
                    document.getElementById('totalDocuments').textContent = stats.totalDocuments || '0';
                    document.getElementById('totalPolicies').textContent = stats.totalPolicies || '0';
                    document.getElementById('totalClinics').textContent = stats.totalClinics || '0';
                }
                */
            } catch (error) {
                console.error('Error loading statistics:', error);
                document.getElementById('totalUsers').textContent = 'Error';
                document.getElementById('totalDocuments').textContent = 'Error';
                document.getElementById('totalPolicies').textContent = 'Error';
                document.getElementById('totalClinics').textContent = 'Error';
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
                window.location.href = '/hcen/login-admin.jsp';
            }
        }

        /**
         * Navigate to page
         */
        function navigateTo(url) {
            alert('Esta función estará disponible próximamente.\n\nURL: ' + url);
            // Uncomment when pages are ready:
            // window.location.href = url;
        }

        /**
         * Initialize dashboard on page load
         */
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Admin Dashboard initializing...');
            loadUserInfo();
            loadStatistics();
        });
    </script>
</body>
</html>
