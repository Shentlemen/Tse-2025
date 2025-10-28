<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Gestión de Clínicas</title>
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

        .page-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 5px;
        }

        .breadcrumb {
            color: #a0a0a0;
            font-size: 14px;
            margin-top: 8px;
        }

        .breadcrumb a {
            color: #00d4ff;
            text-decoration: none;
            transition: color 0.3s ease;
        }

        .breadcrumb a:hover {
            color: #00b8e6;
        }

        .header-actions {
            display: flex;
            gap: 15px;
        }

        .btn {
            background: linear-gradient(135deg, #00d4ff, #0095ff);
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(0, 212, 255, 0.3);
            display: inline-flex;
            align-items: center;
            gap: 8px;
            text-decoration: none;
        }

        .btn:hover {
            background: linear-gradient(135deg, #0095ff, #0066cc);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(0, 212, 255, 0.4);
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            box-shadow: 0 4px 15px rgba(255, 255, 255, 0.1);
        }

        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.15);
            box-shadow: 0 6px 20px rgba(255, 255, 255, 0.15);
        }

        .search-section {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 25px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .search-grid {
            display: grid;
            grid-template-columns: 1fr 1fr 1fr auto;
            gap: 20px;
            align-items: end;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .form-group label {
            color: #a0a0a0;
            font-size: 13px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .form-control {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            padding: 12px 16px;
            border-radius: 8px;
            color: #e4e4e4;
            font-size: 14px;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            outline: none;
            border-color: #00d4ff;
            background: rgba(255, 255, 255, 0.08);
            box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
        }

        select.form-control {
            cursor: pointer;
        }

        .stats-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 20px;
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
        }

        .stat-card-label {
            color: #a0a0a0;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .stat-card-value {
            font-size: 32px;
            font-weight: bold;
            color: #00d4ff;
        }

        .table-container {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            border-radius: 15px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
            overflow: hidden;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead {
            background: rgba(0, 212, 255, 0.1);
        }

        th {
            padding: 18px 20px;
            text-align: left;
            font-weight: 600;
            color: #00d4ff;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        td {
            padding: 16px 20px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
            color: #e4e4e4;
        }

        tbody tr {
            transition: all 0.3s ease;
        }

        tbody tr:hover {
            background: rgba(255, 255, 255, 0.05);
        }

        .badge {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .badge-active {
            background: rgba(76, 175, 80, 0.2);
            color: #4caf50;
            border: 1px solid rgba(76, 175, 80, 0.3);
        }

        .badge-inactive {
            background: rgba(244, 67, 54, 0.2);
            color: #f44336;
            border: 1px solid rgba(244, 67, 54, 0.3);
        }

        .badge-pending {
            background: rgba(255, 193, 7, 0.2);
            color: #ffc107;
            border: 1px solid rgba(255, 193, 7, 0.3);
        }

        .action-btn {
            background: none;
            border: none;
            color: #00d4ff;
            cursor: pointer;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 13px;
            transition: all 0.3s ease;
            margin-right: 8px;
        }

        .action-btn:hover {
            background: rgba(0, 212, 255, 0.1);
        }

        .action-btn.danger {
            color: #f44336;
        }

        .action-btn.danger:hover {
            background: rgba(244, 67, 54, 0.1);
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            margin-top: 25px;
            padding: 20px;
        }

        .pagination-btn {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            color: #e4e4e4;
            padding: 10px 16px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.3s ease;
        }

        .pagination-btn:hover:not(:disabled) {
            background: rgba(0, 212, 255, 0.1);
            border-color: #00d4ff;
            color: #00d4ff;
        }

        .pagination-btn:disabled {
            opacity: 0.3;
            cursor: not-allowed;
        }

        .pagination-info {
            color: #a0a0a0;
            font-size: 14px;
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

        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #a0a0a0;
        }

        .empty-state-icon {
            font-size: 64px;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .empty-state-title {
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 10px;
            color: #e4e4e4;
        }

        .empty-state-text {
            font-size: 14px;
            margin-bottom: 20px;
        }

        .message {
            padding: 15px 20px;
            border-radius: 10px;
            margin-bottom: 20px;
            display: none;
        }

        .message.success {
            background: rgba(76, 175, 80, 0.1);
            border: 1px solid rgba(76, 175, 80, 0.3);
            color: #4caf50;
        }

        .message.error {
            background: rgba(244, 67, 54, 0.1);
            border: 1px solid rgba(244, 67, 54, 0.3);
            color: #f44336;
        }

        @media (max-width: 1200px) {
            .search-grid {
                grid-template-columns: 1fr 1fr;
            }

            .stats-row {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 768px) {
            .search-grid {
                grid-template-columns: 1fr;
            }

            .stats-row {
                grid-template-columns: 1fr;
            }

            .header {
                flex-direction: column;
                gap: 20px;
            }

            .header-actions {
                width: 100%;
                flex-direction: column;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }

            table {
                font-size: 12px;
            }

            th, td {
                padding: 12px 10px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="message" id="message"></div>

        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">🏥</span>
                    <span>HCEN</span>
                </div>
                <div class="page-title">Gestión de Clínicas</div>
                <div class="breadcrumb">
                    <a href="/hcen/admin/dashboard.jsp">Dashboard</a> / Clínicas
                </div>
            </div>
            <div class="header-actions">
                <a href="/hcen/admin/clinic-register.jsp" class="btn">
                    ➕ Registrar Clínica
                </a>
                <a href="/hcen/admin/dashboard.jsp" class="btn btn-secondary">
                    🏠 Volver al Dashboard
                </a>
            </div>
        </div>

        <div class="stats-row">
            <div class="stat-card">
                <div class="stat-card-label">Total</div>
                <div class="stat-card-value" id="statTotal">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-card-label">Activas</div>
                <div class="stat-card-value" id="statActive">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-card-label">Pendientes</div>
                <div class="stat-card-value" id="statPending">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-card-label">Inactivas</div>
                <div class="stat-card-value" id="statInactive">-</div>
            </div>
        </div>

        <div class="search-section">
            <div class="search-grid">
                <div class="form-group">
                    <label for="filterStatus">Estado</label>
                    <select id="filterStatus" class="form-control">
                        <option value="">Todos</option>
                        <option value="ACTIVE">Activa</option>
                        <option value="PENDING_ONBOARDING">Pendiente</option>
                        <option value="INACTIVE">Inactiva</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="filterCity">Ciudad</label>
                    <input type="text" id="filterCity" class="form-control" placeholder="Filtrar por ciudad">
                </div>
                <div class="form-group">
                    <label for="pageSize">Resultados por página</label>
                    <select id="pageSize" class="form-control">
                        <option value="10">10</option>
                        <option value="20" selected>20</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>&nbsp;</label>
                    <button class="btn" onclick="searchClinics()">🔍 Buscar</button>
                </div>
            </div>
        </div>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Ciudad</th>
                        <th>Teléfono</th>
                        <th>Email</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody id="clinicsTableBody">
                    <tr>
                        <td colspan="6" style="text-align: center; padding: 40px;">
                            <span class="loading"></span>
                            <div style="margin-top: 10px;">Cargando clínicas...</div>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="pagination" id="pagination" style="display: none;">
                <button class="pagination-btn" id="prevBtn" onclick="previousPage()">← Anterior</button>
                <span class="pagination-info" id="paginationInfo">Página 1 de 1</span>
                <button class="pagination-btn" id="nextBtn" onclick="nextPage()">Siguiente →</button>
            </div>
        </div>
    </div>

    <script type="text/javascript">
    //<![CDATA[
        // API Configuration
        const API_BASE = '/hcen/api';

        // Pagination state
        let currentPage = 0;
        let totalPages = 0;
        let totalElements = 0;

        /**
         * Get JWT token from sessionStorage
         */
        function getToken() {
            return sessionStorage.getItem('accessToken');
        }

        /**
         * Make authenticated API call
         */
        async function apiCall(endpoint, options = {}) {
            const token = getToken();

            if (!token) {
                showMessage('Sesión expirada. Por favor, inicie sesión nuevamente.', 'error');
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
                    sessionStorage.removeItem('accessToken');
                    showMessage('Sesión expirada. Redirigiendo...', 'error');
                    setTimeout(() => {
                        window.location.href = '/hcen/login-admin.jsp';
                    }, 2000);
                    return null;
                }

                if (!response.ok) {
                    const error = await response.json();
                    throw new Error(error.message || 'Error ' + response.status);
                }

                return await response.json();
            } catch (error) {
                console.error('API call error:', error);
                throw error;
            }
        }

        /**
         * Show message to user
         */
        function showMessage(text, type = 'success') {
            const messageDiv = document.getElementById('message');
            messageDiv.textContent = text;
            messageDiv.className = 'message ' + type;
            messageDiv.style.display = 'block';

            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 5000);
        }

        /**
         * Load clinics from API
         */
        async function loadClinics(page = 0) {
            const status = document.getElementById('filterStatus').value;
            const city = document.getElementById('filterCity').value.trim();
            const size = document.getElementById('pageSize').value;

            let endpoint = '/admin/clinics?page=' + page + '&size=' + size;

            if (status) {
                endpoint += '&status=' + status;
            }
            if (city) {
                endpoint += '&city=' + encodeURIComponent(city);
            }

            try {
                const data = await apiCall(endpoint);

                if (!data) {
                    return;
                }

                currentPage = data.currentPage;
                totalPages = data.totalPages;
                totalElements = data.totalElements;

                renderClinicsTable(data.clinics);
                updatePagination();

            } catch (error) {
                console.error('Error loading clinics:', error);
                showMessage('Error al cargar clínicas: ' + error.message, 'error');
                renderEmptyState('Error al cargar datos');
            }
        }

        /**
         * Render clinics table
         */
        function renderClinicsTable(clinics) {
            const tbody = document.getElementById('clinicsTableBody');

            if (!clinics || clinics.length === 0) {
                renderEmptyState('No se encontraron clínicas con los filtros seleccionados');
                return;
            }

            tbody.innerHTML = clinics.map(clinic => {
                let html = '<tr>';
                html += '<td><strong>' + escapeHtml(clinic.clinicName) + '</strong></td>';
                html += '<td>' + escapeHtml(clinic.city) + '</td>';
                html += '<td>' + escapeHtml(clinic.phoneNumber) + '</td>';
                html += '<td>' + escapeHtml(clinic.email) + '</td>';
                html += '<td>' + renderStatusBadge(clinic.status) + '</td>';
                html += '<td>';
                html += '<button class="action-btn" onclick="viewClinic(\'' + clinic.clinicId + '\')" title="Ver detalles">👁️ Ver</button>';
                if (clinic.status === 'PENDING_ONBOARDING') {
                    html += '<button class="action-btn" onclick="onboardClinic(\'' + clinic.clinicId + '\')" title="Onboardear">🚀 Onboard</button>';
                }
                if (clinic.status === 'ACTIVE') {
                    html += '<button class="action-btn danger" onclick="deactivateClinic(\'' + clinic.clinicId + '\')" title="Desactivar">❌ Desactivar</button>';
                }
                html += '</td>';
                html += '</tr>';
                return html;
            }).join('');
        }

        /**
         * Render empty state
         */
        function renderEmptyState(message) {
            const tbody = document.getElementById('clinicsTableBody');
            let html = '<tr><td colspan="6"><div class="empty-state">';
            html += '<div class="empty-state-icon">🏥</div>';
            html += '<div class="empty-state-title">' + message + '</div>';
            html += '<div class="empty-state-text">';
            html += message.includes('filtros') ? 'Intenta ajustar los filtros de búsqueda.' : 'Registra la primera clínica para comenzar.';
            html += '</div>';
            if (!message.includes('filtros')) {
                html += '<a href="/hcen/admin/clinic-register.jsp" class="btn">➕ Registrar Primera Clínica</a>';
            }
            html += '</div></td></tr>';
            tbody.innerHTML = html;
            document.getElementById('pagination').style.display = 'none';
        }

        /**
         * Render status badge
         */
        function renderStatusBadge(status) {
            const statusMap = {
                'ACTIVE': { class: 'badge-active', text: 'Activa' },
                'INACTIVE': { class: 'badge-inactive', text: 'Inactiva' },
                'PENDING_ONBOARDING': { class: 'badge-pending', text: 'Pendiente' }
            };

            const config = statusMap[status] || { class: 'badge-pending', text: status };
            return '<span class="badge ' + config.class + '">' + config.text + '</span>';
        }

        /**
         * Update pagination controls
         */
        function updatePagination() {
            const paginationDiv = document.getElementById('pagination');
            const paginationInfo = document.getElementById('paginationInfo');
            const prevBtn = document.getElementById('prevBtn');
            const nextBtn = document.getElementById('nextBtn');

            if (totalPages <= 1) {
                paginationDiv.style.display = 'none';
                return;
            }

            paginationDiv.style.display = 'flex';
            paginationInfo.textContent = 'Página ' + (currentPage + 1) + ' de ' + totalPages + ' (' + totalElements + ' resultados)';

            prevBtn.disabled = currentPage === 0;
            nextBtn.disabled = currentPage >= totalPages - 1;
        }

        /**
         * Load statistics
         */
        async function loadStatistics() {
            try {
                // Load all clinics to calculate stats
                const [allData, activeData, pendingData, inactiveData] = await Promise.all([
                    apiCall('/admin/clinics?page=0&size=1'),
                    apiCall('/admin/clinics?status=ACTIVE&page=0&size=1'),
                    apiCall('/admin/clinics?status=PENDING_ONBOARDING&page=0&size=1'),
                    apiCall('/admin/clinics?status=INACTIVE&page=0&size=1')
                ]);

                document.getElementById('statTotal').textContent = allData?.totalElements || 0;
                document.getElementById('statActive').textContent = activeData?.totalElements || 0;
                document.getElementById('statPending').textContent = pendingData?.totalElements || 0;
                document.getElementById('statInactive').textContent = inactiveData?.totalElements || 0;

            } catch (error) {
                console.error('Error loading statistics:', error);
                document.getElementById('statTotal').textContent = 'Error';
                document.getElementById('statActive').textContent = 'Error';
                document.getElementById('statPending').textContent = 'Error';
                document.getElementById('statInactive').textContent = 'Error';
            }
        }

        /**
         * Search clinics with current filters
         */
        function searchClinics() {
            currentPage = 0;
            loadClinics(0);
        }

        /**
         * Navigate to previous page
         */
        function previousPage() {
            if (currentPage > 0) {
                loadClinics(currentPage - 1);
            }
        }

        /**
         * Navigate to next page
         */
        function nextPage() {
            if (currentPage < totalPages - 1) {
                loadClinics(currentPage + 1);
            }
        }

        /**
         * View clinic details
         */
        function viewClinic(clinicId) {
            window.location.href = '/hcen/admin/clinic-detail.jsp?id=' + clinicId;
        }

        /**
         * Onboard clinic
         */
        async function onboardClinic(clinicId) {
            if (!confirm('¿Está seguro que desea onboardear esta clínica a su nodo periférico?')) {
                return;
            }

            try {
                const result = await apiCall('/admin/clinics/' + clinicId + '/onboard', {
                    method: 'POST'
                });

                if (result) {
                    showMessage('Clínica onboardeada exitosamente', 'success');
                    loadClinics(currentPage);
                    loadStatistics();
                }
            } catch (error) {
                console.error('Error onboarding clinic:', error);
                showMessage('Error al onboardear clínica: ' + error.message, 'error');
            }
        }

        /**
         * Deactivate clinic
         */
        async function deactivateClinic(clinicId) {
            if (!confirm('¿Está seguro que desea desactivar esta clínica? Esta acción no se puede deshacer fácilmente.')) {
                return;
            }

            try {
                await apiCall('/admin/clinics/' + clinicId, {
                    method: 'DELETE'
                });

                showMessage('Clínica desactivada exitosamente', 'success');
                loadClinics(currentPage);
                loadStatistics();

            } catch (error) {
                console.error('Error deactivating clinic:', error);
                showMessage('Error al desactivar clínica: ' + error.message, 'error');
            }
        }

        /**
         * Escape HTML to prevent XSS
         */
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        /**
         * Initialize page on load
         */
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Clinics page initializing...');

            // Check for token
            if (!getToken()) {
                showMessage('No se encontró sesión. Redirigiendo...', 'error');
                setTimeout(() => {
                    window.location.href = '/hcen/login-admin.jsp';
                }, 2000);
                return;
            }

            // Load data
            loadClinics(0);
            loadStatistics();
        });
    //]]>
    </script>
</body>
</html>
