<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Historial de Accesos - HCEN</title>
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

        .breadcrumb {
            color: #7f8c8d;
            font-size: 14px;
            margin-top: 8px;
        }

        .breadcrumb a {
            color: #667eea;
            text-decoration: none;
            transition: color 0.3s;
        }

        .breadcrumb a:hover {
            color: #5568d3;
            text-decoration: underline;
        }

        .back-btn, .logout-btn {
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
            margin-left: 10px;
        }

        .back-btn:hover, .logout-btn:hover {
            background: linear-gradient(135deg, #5568d3, #6a3f8f);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        }

        .page-title {
            font-size: 32px;
            font-weight: 600;
            margin-bottom: 10px;
            color: #2c3e50;
        }

        .page-description {
            color: #7f8c8d;
            font-size: 16px;
            margin-bottom: 30px;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: white;
            padding: 25px;
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
            font-size: 32px;
            margin-bottom: 10px;
            opacity: 0.8;
        }

        .stat-value {
            font-size: 36px;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
        }

        .stat-label {
            color: #7f8c8d;
            font-size: 13px;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .filters-card {
            background: white;
            padding: 25px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .filters-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #2c3e50;
        }

        .filters-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            align-items: end;
        }

        .form-group {
            margin-bottom: 0;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #2c3e50;
            font-weight: 600;
            font-size: 14px;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 10px 14px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            transition: border-color 0.3s;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn-filter {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
            width: 100%;
        }

        .btn-filter:hover {
            background: linear-gradient(135deg, #5568d3, #6a3f8f);
            transform: translateY(-2px);
        }

        .btn-clear {
            background: #f8f9fa;
            color: #666;
            border: 2px solid #e1e8ed;
            padding: 10px 24px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s;
            width: 100%;
        }

        .btn-clear:hover {
            background: #e1e8ed;
        }

        .alert {
            padding: 16px 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            display: none;
            animation: slideIn 0.3s ease;
        }

        .alert.show {
            display: block;
        }

        .alert-error {
            background: rgba(231, 76, 60, 0.1);
            border: 1px solid rgba(231, 76, 60, 0.3);
            color: #e74c3c;
        }

        .alert-info {
            background: rgba(52, 152, 219, 0.1);
            border: 1px solid rgba(52, 152, 219, 0.3);
            color: #3498db;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .loading {
            text-align: center;
            padding: 60px 20px;
        }

        .spinner {
            display: inline-block;
            width: 50px;
            height: 50px;
            border: 4px solid rgba(102, 126, 234, 0.3);
            border-radius: 50%;
            border-top-color: #667eea;
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .empty-state {
            background: white;
            padding: 60px 30px;
            border-radius: 15px;
            text-align: center;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .empty-state-icon {
            font-size: 80px;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .empty-state-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 10px;
            color: #2c3e50;
        }

        .empty-state-description {
            color: #7f8c8d;
            font-size: 16px;
            margin-bottom: 30px;
        }

        .audit-logs-grid {
            display: grid;
            gap: 15px;
        }

        .audit-log-card {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
            transition: all 0.3s ease;
            border-left: 4px solid #667eea;
        }

        .audit-log-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 35px rgba(102, 126, 234, 0.2);
        }

        .audit-log-header {
            display: flex;
            justify-content: space-between;
            align-items: start;
            margin-bottom: 15px;
        }

        .actor-info {
            flex: 1;
        }

        .actor-name {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 5px;
        }

        .actor-type {
            font-size: 13px;
            color: #7f8c8d;
            text-transform: uppercase;
        }

        .event-badge {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .event-access {
            background: rgba(52, 152, 219, 0.2);
            color: #3498db;
        }

        .event-modification {
            background: rgba(241, 196, 15, 0.2);
            color: #f39c12;
        }

        .event-creation {
            background: rgba(46, 213, 115, 0.2);
            color: #27ae60;
        }

        .event-deletion {
            background: rgba(231, 76, 60, 0.2);
            color: #e74c3c;
        }

        .event-policy {
            background: rgba(155, 89, 182, 0.2);
            color: #9b59b6;
        }

        .outcome-badge {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            margin-left: 8px;
        }

        .outcome-success {
            background: rgba(46, 213, 115, 0.2);
            color: #27ae60;
        }

        .outcome-denied {
            background: rgba(231, 76, 60, 0.2);
            color: #e74c3c;
        }

        .outcome-failure {
            background: rgba(241, 196, 15, 0.2);
            color: #f39c12;
        }

        .audit-log-details {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }

        .detail-item {
            font-size: 13px;
        }

        .detail-label {
            font-weight: 600;
            color: #7f8c8d;
            margin-bottom: 3px;
        }

        .detail-value {
            color: #2c3e50;
        }

        .timestamp {
            font-size: 12px;
            color: #95a5a6;
            margin-top: 10px;
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            margin-top: 30px;
            padding: 20px;
            background: white;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .pagination-btn {
            background: #f8f9fa;
            color: #667eea;
            border: 2px solid #e1e8ed;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s;
        }

        .pagination-btn:hover:not(:disabled) {
            background: #667eea;
            color: white;
            border-color: #667eea;
        }

        .pagination-btn:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        .pagination-info {
            color: #7f8c8d;
            font-size: 14px;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">🔍</span>
                    <span>Historial de Accesos</span>
                </div>
                <div class="breadcrumb">
                    <a href="/hcen/patient/dashboard.jsp">Inicio</a> &gt; Historial de Accesos
                </div>
            </div>
            <div>
                <button class="back-btn" onclick="goBack()">← Volver</button>
                <button class="logout-btn" onclick="logout()">🚪 Cerrar Sesión</button>
            </div>
        </div>

        <!-- Alerts -->
        <div id="alertError" class="alert alert-error"></div>
        <div id="alertInfo" class="alert alert-info"></div>

        <!-- Page Title -->
        <div style="background: white; padding: 30px; border-radius: 15px; margin-bottom: 30px; box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);">
            <h1 class="page-title">Historial de Accesos a Mi Información</h1>
            <p class="page-description">
                Aquí puede ver el registro completo de quién ha accedido a sus documentos clínicos y cuándo.
                Este registro es inmutable y asegura la transparencia y trazabilidad de su información de salud.
            </p>
        </div>

        <!-- Statistics -->
        <div class="stats-grid" id="statsGrid">
            <div class="stat-card">
                <div class="stat-icon">📊</div>
                <div class="stat-value" id="totalEvents"><span class="spinner" style="width: 20px; height: 20px;"></span></div>
                <div class="stat-label">Total de Eventos</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">👁️</div>
                <div class="stat-value" id="accessEvents"><span class="spinner" style="width: 20px; height: 20px;"></span></div>
                <div class="stat-label">Accesos</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">✏️</div>
                <div class="stat-value" id="modificationEvents"><span class="spinner" style="width: 20px; height: 20px;"></span></div>
                <div class="stat-label">Modificaciones</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">✅</div>
                <div class="stat-value" id="successEvents"><span class="spinner" style="width: 20px; height: 20px;"></span></div>
                <div class="stat-label">Exitosos</div>
            </div>
        </div>

        <!-- Filters -->
        <div class="filters-card">
            <h3 class="filters-title">Filtrar Registros</h3>
            <div class="filters-grid">
                <div class="form-group">
                    <label for="fromDate">Desde</label>
                    <input type="date" id="fromDate" name="fromDate">
                </div>
                <div class="form-group">
                    <label for="toDate">Hasta</label>
                    <input type="date" id="toDate" name="toDate">
                </div>
                <div class="form-group">
                    <label for="eventType">Tipo de Evento</label>
                    <select id="eventType" name="eventType">
                        <option value="">Todos</option>
                        <option value="ACCESS">Acceso</option>
                        <option value="MODIFICATION">Modificación</option>
                        <option value="CREATION">Creación</option>
                        <option value="DELETION">Eliminación</option>
                        <option value="POLICY_CHANGE">Cambio de Política</option>
                        <option value="ACCESS_REQUEST">Solicitud de Acceso</option>
                        <option value="ACCESS_APPROVAL">Aprobación de Acceso</option>
                        <option value="ACCESS_DENIAL">Denegación de Acceso</option>
                    </select>
                </div>
                <div class="form-group">
                    <button class="btn-filter" onclick="applyFilters()">🔍 Buscar</button>
                </div>
                <div class="form-group">
                    <button class="btn-clear" onclick="clearFilters()">🔄 Limpiar</button>
                </div>
            </div>
        </div>

        <!-- Loading State -->
        <div id="loadingState" class="loading">
            <div class="spinner"></div>
            <p style="margin-top: 20px; color: #7f8c8d;">Cargando historial de accesos...</p>
        </div>

        <!-- Empty State -->
        <div id="emptyState" class="empty-state" style="display: none;">
            <div class="empty-state-icon">📭</div>
            <h2 class="empty-state-title">No se encontraron registros</h2>
            <p class="empty-state-description">
                No hay registros de auditoría que coincidan con los filtros seleccionados.
                Intente ajustar los filtros o limpiarlos para ver todos los registros.
            </p>
        </div>

        <!-- Audit Logs Grid -->
        <div id="auditLogsGrid" class="audit-logs-grid" style="display: none;"></div>

        <!-- Pagination -->
        <div id="pagination" class="pagination" style="display: none;">
            <button class="pagination-btn" id="prevBtn" onclick="previousPage()">← Anterior</button>
            <span class="pagination-info" id="paginationInfo">Página 1 de 1</span>
            <button class="pagination-btn" id="nextBtn" onclick="nextPage()">Siguiente →</button>
        </div>
    </div>

    <script>
        // API Configuration
        const API_BASE = '/hcen/api';

        // Pagination state
        let currentPage = 0;
        const pageSize = 20;
        let totalPages = 0;
        let filters = {
            fromDate: null,
            toDate: null,
            eventType: null
        };

        /**
         * Get JWT token from sessionStorage
         */
        function getToken() {
            return sessionStorage.getItem('accessToken');
        }

        /**
         * Extract patient CI from JWT token
         */
        function getPatientCi() {
            const token = getToken();
            if (!token) return null;

            try {
                const payload = parseJwt(token);
                return payload.sub || payload.ci;
            } catch (e) {
                console.error('Error parsing token:', e);
                return null;
            }
        }

        /**
         * Parse JWT token to extract payload
         */
        function parseJwt(token) {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(
                atob(base64).split('').map(function(c) {
                    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                }).join('')
            );
            return JSON.parse(jsonPayload);
        }

        /**
         * Make authenticated API call
         */
        async function apiCall(endpoint, options = {}) {
            const token = getToken();

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
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'API call failed');
                }

                return await response.json();
            } catch (error) {
                console.error('API call error:', error);
                throw error;
            }
        }

        /**
         * Show error message
         */
        function showError(message) {
            const errorDiv = document.getElementById('alertError');
            errorDiv.textContent = message;
            errorDiv.classList.add('show');

            setTimeout(() => {
                errorDiv.classList.remove('show');
            }, 5000);
        }

        /**
         * Show info message
         */
        function showInfo(message) {
            const infoDiv = document.getElementById('alertInfo');
            infoDiv.textContent = message;
            infoDiv.classList.add('show');

            setTimeout(() => {
                infoDiv.classList.remove('show');
            }, 5000);
        }

        /**
         * Load audit statistics
         */
        async function loadStatistics() {
            try {
                const patientCi = getPatientCi();
                if (!patientCi) {
                    showError('No se pudo identificar al paciente');
                    return;
                }

                const stats = await apiCall('/audit-logs/stats?patientCi=' + patientCi);

                if (stats) {
                    document.getElementById('totalEvents').textContent = stats.totalEvents || '0';
                    document.getElementById('accessEvents').textContent = stats.eventsByType?.ACCESS || '0';
                    document.getElementById('modificationEvents').textContent = stats.eventsByType?.MODIFICATION || '0';
                    document.getElementById('successEvents').textContent = stats.eventsByOutcome?.SUCCESS || '0';
                }
            } catch (error) {
                console.error('Error loading statistics:', error);
                document.getElementById('totalEvents').textContent = 'Error';
                document.getElementById('accessEvents').textContent = 'Error';
                document.getElementById('modificationEvents').textContent = 'Error';
                document.getElementById('successEvents').textContent = 'Error';
            }
        }

        /**
         * Load audit logs with filters and pagination
         */
        async function loadAuditLogs() {
            // Show loading state
            document.getElementById('loadingState').style.display = 'block';
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('auditLogsGrid').style.display = 'none';
            document.getElementById('pagination').style.display = 'none';

            try {
                const patientCi = getPatientCi();
                if (!patientCi) {
                    showError('No se pudo identificar al paciente');
                    return;
                }

                // Build query parameters
                const params = new URLSearchParams({
                    page: currentPage,
                    size: pageSize,
                    patientCi: patientCi
                });

                if (filters.fromDate) params.append('fromDate', filters.fromDate);
                if (filters.toDate) params.append('toDate', filters.toDate);
                if (filters.eventType) params.append('eventType', filters.eventType);

                const response = await apiCall('/audit-logs?' + params.toString());

                if (response) {
                    // Hide loading
                    document.getElementById('loadingState').style.display = 'none';

                    // Check if empty
                    if (response.logs.length === 0) {
                        document.getElementById('emptyState').style.display = 'block';
                        return;
                    }

                    // Display audit logs
                    displayAuditLogs(response.logs);

                    // Update pagination
                    totalPages = response.totalPages;
                    updatePagination(response);
                }
            } catch (error) {
                console.error('Error loading audit logs:', error);
                document.getElementById('loadingState').style.display = 'none';
                showError('Error al cargar el historial de accesos: ' + error.message);
            }
        }

        /**
         * Display audit logs
         */
        function displayAuditLogs(logs) {
            const grid = document.getElementById('auditLogsGrid');
            grid.innerHTML = '';
            grid.style.display = 'grid';

            logs.forEach(log => {
                const card = createAuditLogCard(log);
                grid.appendChild(card);
            });
        }

        /**
         * Create audit log card element
         */
        function createAuditLogCard(log) {
            const card = document.createElement('div');
            card.className = 'audit-log-card';

            const eventClass = getEventClass(log.eventType);
            const outcomeClass = getOutcomeClass(log.actionOutcome);

            card.innerHTML = `
                <div class="audit-log-header">
                    <div class="actor-info">
                        <div class="actor-name">${'$'}{log.actorId || 'Desconocido'}</div>
                        <div class="actor-type">${'$'}{log.actorType || 'N/A'}</div>
                    </div>
                    <div>
                        <span class="event-badge ${'$'}{eventClass}">${'$'}{formatEventType(log.eventType)}</span>
                        <span class="outcome-badge ${'$'}{outcomeClass}">${'$'}{formatOutcome(log.actionOutcome)}</span>
                    </div>
                </div>
                <div class="audit-log-details">
                    <div class="detail-item">
                        <div class="detail-label">Recurso</div>
                        <div class="detail-value">${'$'}{log.resourceType || 'N/A'}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-label">ID Recurso</div>
                        <div class="detail-value">${'$'}{log.resourceId || 'N/A'}</div>
                    </div>
                    <div class="detail-item">
                        <div class="detail-label">IP Address</div>
                        <div class="detail-value">${'$'}{log.ipAddress || 'N/A'}</div>
                    </div>
                    ${'$'}{log.details && log.details.documentType ? `
                    <div class="detail-item">
                        <div class="detail-label">Tipo de Documento</div>
                        <div class="detail-value">${'$'}{log.details.documentType}</div>
                    </div>
                    ` : ''}
                </div>
                <div class="timestamp">📅 ${'$'}{formatTimestamp(log.timestamp)}</div>
            `;

            return card;
        }

        /**
         * Get event badge class
         */
        function getEventClass(eventType) {
            switch (eventType) {
                case 'ACCESS': return 'event-access';
                case 'MODIFICATION': return 'event-modification';
                case 'CREATION': return 'event-creation';
                case 'DELETION': return 'event-deletion';
                case 'POLICY_CHANGE':
                case 'ACCESS_REQUEST':
                case 'ACCESS_APPROVAL':
                case 'ACCESS_DENIAL':
                    return 'event-policy';
                default: return 'event-access';
            }
        }

        /**
         * Get outcome badge class
         */
        function getOutcomeClass(outcome) {
            switch (outcome) {
                case 'SUCCESS': return 'outcome-success';
                case 'DENIED': return 'outcome-denied';
                case 'FAILURE': return 'outcome-failure';
                default: return 'outcome-success';
            }
        }

        /**
         * Format event type for display
         */
        function formatEventType(eventType) {
            const types = {
                'ACCESS': 'Acceso',
                'MODIFICATION': 'Modificación',
                'CREATION': 'Creación',
                'DELETION': 'Eliminación',
                'POLICY_CHANGE': 'Cambio de Política',
                'ACCESS_REQUEST': 'Solicitud',
                'ACCESS_APPROVAL': 'Aprobación',
                'ACCESS_DENIAL': 'Denegación',
                'AUTHENTICATION_SUCCESS': 'Login Exitoso',
                'AUTHENTICATION_FAILURE': 'Login Fallido'
            };
            return types[eventType] || eventType;
        }

        /**
         * Format outcome for display
         */
        function formatOutcome(outcome) {
            const outcomes = {
                'SUCCESS': 'Exitoso',
                'DENIED': 'Denegado',
                'FAILURE': 'Fallido'
            };
            return outcomes[outcome] || outcome;
        }

        /**
         * Format timestamp
         */
        function formatTimestamp(timestamp) {
            if (!timestamp) return 'N/A';

            try {
                let date;

                // Check if it's an array (LocalDateTime from backend: [year, month, day, hour, minute, second, nanosecond])
                if (Array.isArray(timestamp) && timestamp.length >= 3) {
                    // Create date from array: [year, month (1-based), day, hour, minute, second]
                    const [year, month, day, hour = 0, minute = 0, second = 0] = timestamp;
                    date = new Date(year, month - 1, day, hour, minute, second);
                } else if (typeof timestamp === 'string') {
                    // Handle ISO string format
                    date = new Date(timestamp);
                } else if (typeof timestamp === 'number') {
                    // Handle timestamp
                    date = new Date(timestamp);
                } else {
                    return 'N/A';
                }

                // Check if date is valid
                if (isNaN(date.getTime())) {
                    return 'N/A';
                }

                return date.toLocaleString('es-UY', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            } catch (e) {
                console.error('Error formatting timestamp:', e, timestamp);
                return 'N/A';
            }
        }

        /**
         * Update pagination controls
         */
        function updatePagination(response) {
            const pagination = document.getElementById('pagination');
            const prevBtn = document.getElementById('prevBtn');
            const nextBtn = document.getElementById('nextBtn');
            const paginationInfo = document.getElementById('paginationInfo');

            pagination.style.display = 'flex';

            prevBtn.disabled = !response.hasPrevious;
            nextBtn.disabled = !response.hasNext;

            paginationInfo.textContent = `Página ${'$'}{response.page + 1} de ${'$'}{response.totalPages || 1} (mostrando ${'$'}{response.logs.length} de ${'$'}{response.totalCount} registros)`;
        }

        /**
         * Apply filters
         */
        function applyFilters() {
            filters.fromDate = document.getElementById('fromDate').value || null;
            filters.toDate = document.getElementById('toDate').value || null;
            filters.eventType = document.getElementById('eventType').value || null;

            currentPage = 0; // Reset to first page
            loadAuditLogs();
        }

        /**
         * Clear filters
         */
        function clearFilters() {
            document.getElementById('fromDate').value = '';
            document.getElementById('toDate').value = '';
            document.getElementById('eventType').value = '';

            filters = {
                fromDate: null,
                toDate: null,
                eventType: null
            };

            currentPage = 0;
            loadAuditLogs();
        }

        /**
         * Go to previous page
         */
        function previousPage() {
            if (currentPage > 0) {
                currentPage--;
                loadAuditLogs();
            }
        }

        /**
         * Go to next page
         */
        function nextPage() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadAuditLogs();
            }
        }

        /**
         * Go back to dashboard
         */
        function goBack() {
            window.location.href = '/hcen/patient/dashboard.jsp';
        }

        /**
         * Logout user
         */
        async function logout() {
            try {
                const token = getToken();
                if (token) {
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
                sessionStorage.removeItem('accessToken');
                window.location.href = '/hcen/login-patient.jsp';
            }
        }

        /**
         * Initialize page
         */
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Audit logs page initializing...');

            // Check authentication
            const token = getToken();
            if (!token) {
                showError('No se encontró token de acceso. Redirigiendo...');
                setTimeout(() => {
                    window.location.href = '/hcen/login-patient.jsp';
                }, 2000);
                return;
            }

            // Load statistics and audit logs
            loadStatistics();
            loadAuditLogs();
        });
    </script>
</body>
</html>
