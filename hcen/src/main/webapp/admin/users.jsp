<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Usuarios - Admin HCEN</title>
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

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
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
            border-left: 4px solid #00d4ff;
        }

        .stat-label {
            color: #a0a0a0;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .stat-value {
            font-size: 32px;
            font-weight: bold;
            color: #00d4ff;
        }

        .controls {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 25px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .controls-row {
            display: grid;
            grid-template-columns: 2fr 1fr 1fr auto;
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
            width: 100%;
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

        .status-badge {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .status-badge.active {
            background: rgba(76, 175, 80, 0.2);
            color: #4caf50;
            border: 1px solid rgba(76, 175, 80, 0.3);
        }

        .status-badge.inactive {
            background: rgba(244, 67, 54, 0.2);
            color: #f44336;
            border: 1px solid rgba(244, 67, 54, 0.3);
        }

        .status-badge.suspended {
            background: rgba(255, 193, 7, 0.2);
            color: #ffc107;
            border: 1px solid rgba(255, 193, 7, 0.3);
        }

        .actions {
            display: flex;
            gap: 8px;
        }

        .btn-action {
            background: none;
            border: none;
            color: #00d4ff;
            cursor: pointer;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 13px;
            transition: all 0.3s ease;
        }

        .btn-action:hover {
            background: rgba(0, 212, 255, 0.1);
        }

        .btn-action.danger {
            color: #f44336;
        }

        .btn-action.danger:hover {
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

        .loading-state {
            text-align: center;
            padding: 40px;
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

        @media (max-width: 1200px) {
            .controls-row {
                grid-template-columns: 1fr 1fr;
            }

            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 768px) {
            .controls-row {
                grid-template-columns: 1fr;
            }

            .stats-grid {
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
        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">🏥</span>
                    <span>HCEN</span>
                </div>
                <div class="page-title">Gestión de Usuarios INUS</div>
                <div class="breadcrumb">
                    <a href="/hcen/admin/dashboard.jsp">Dashboard</a> / Usuarios
                </div>
            </div>
            <div class="header-actions">
                <a href="/hcen/patient/register.jsp" class="btn">
                    ➕ Registrar Usuario
                </a>
                <a href="/hcen/admin/dashboard.jsp" class="btn btn-secondary">
                    🏠 Volver al Dashboard
                </a>
            </div>
        </div>

        <!-- Statistics -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-label">Total Usuarios</div>
                <div class="stat-value" id="totalUsers">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Usuarios Activos</div>
                <div class="stat-value" id="activeUsers">-</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Usuarios Inactivos</div>
                <div class="stat-value" id="inactiveUsers">-</div>
            </div>
        </div>

        <!-- Controls -->
        <div class="controls">
            <div class="controls-row">
                <div class="form-group">
                    <label for="searchInput">Buscar</label>
                    <input
                        type="text"
                        id="searchInput"
                        class="form-control"
                        placeholder="Buscar por CI, nombre o apellido..."
                        onkeyup="handleSearch()"
                    >
                </div>

                <div class="form-group">
                    <label for="filterStatus">Estado</label>
                    <select id="filterStatus" class="form-control" onchange="loadUsers(0)">
                        <option value="">Todos</option>
                        <option value="ACTIVE">Activos</option>
                        <option value="INACTIVE">Inactivos</option>
                        <option value="SUSPENDED">Suspendidos</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="pageSize">Resultados por página</label>
                    <select id="pageSize" class="form-control" onchange="loadUsers(0)">
                        <option value="10">10</option>
                        <option value="20" selected>20</option>
                        <option value="50">50</option>
                        <option value="100">100</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>&nbsp;</label>
                    <button class="btn" onclick="searchUsers()">🔍 Buscar</button>
                </div>
            </div>
        </div>

        <!-- Table -->
        <div class="table-container">
            <table id="usersTable">
                <thead>
                    <tr>
                        <th>CI</th>
                        <th>Nombre Completo</th>
                        <th>Fecha Nacimiento</th>
                        <th>Email</th>
                        <th>Estado</th>
                        <th>Fecha Registro</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody id="usersTableBody">
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 40px;">
                            <span class="loading"></span>
                            <div style="margin-top: 10px;">Cargando usuarios...</div>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div id="paginationContainer" class="pagination" style="display: none;">
                <button class="pagination-btn" id="btnPrev" onclick="loadUsers(currentPage - 1)">← Anterior</button>
                <span class="pagination-info" id="paginationInfo">Página 1 de 1</span>
                <button class="pagination-btn" id="btnNext" onclick="loadUsers(currentPage + 1)">Siguiente →</button>
            </div>
        </div>
    </div>

    <script type="text/javascript">
    //<![CDATA[
        let currentPage = 0;
        let totalPages = 0;
        let searchTimeout = null;

        async function loadUsers(page = 0) {
            currentPage = page;

            const status = document.getElementById('filterStatus').value;
            const size = document.getElementById('pageSize').value;
            const search = document.getElementById('searchInput').value.trim();

            let endpoint = '/hcen/api/inus/users?page=' + page + '&size=' + size;

            if (status) {
                endpoint += '&status=' + status;
            }

            if (search) {
                endpoint += '&search=' + encodeURIComponent(search);
            }

            // Show loading state
            const tbody = document.getElementById('usersTableBody');
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 40px;">' +
                '<span class="loading"></span>' +
                '<div style="margin-top: 10px;">Cargando usuarios...</div>' +
                '</td></tr>';

            try {
                const response = await fetch(endpoint);

                if (response.ok) {
                    const data = await response.json();
                    displayUsers(data);
                } else {
                    showEmpty();
                }
            } catch (error) {
                console.error('Error:', error);
                showEmpty();
            }
        }

        async function loadStatistics() {
            try {
                const response = await fetch('/hcen/api/admin/statistics');
                if (response.ok) {
                    const stats = await response.json();
                    document.getElementById('totalUsers').textContent = formatNumber(stats.totalUsers || 0);
                    document.getElementById('activeUsers').textContent = formatNumber(stats.activeUsers || 0);
                    document.getElementById('inactiveUsers').textContent = formatNumber(stats.inactiveUsers || 0);
                }
            } catch (error) {
                console.error('Error loading statistics:', error);
            }
        }

        function displayUsers(data) {
            const tbody = document.getElementById('usersTableBody');
            tbody.innerHTML = '';

            if (!data.content || data.content.length === 0) {
                showEmpty();
                return;
            }

            data.content.forEach(function(user) {
                const row = document.createElement('tr');
                row.innerHTML =
                    '<td>' + (user.ci || '-') + '</td>' +
                    '<td>' + (user.firstName || '') + ' ' + (user.lastName || '') + '</td>' +
                    '<td>' + formatDate(user.dateOfBirth) + '</td>' +
                    '<td>' + (user.email || '-') + '</td>' +
                    '<td>' + getStatusBadge(user.status) + '</td>' +
                    '<td>' + formatDateTime(user.createdAt) + '</td>' +
                    '<td>' +
                        '<div class="actions">' +
                            '<button class="btn-action" onclick="viewUser(\'' + user.ci + '\')" title="Ver detalles">👁️ Ver</button>' +
                            (user.status === 'ACTIVE' ?
                                '<button class="btn-action danger" onclick="deactivateUser(\'' + user.ci + '\')" title="Desactivar">❌ Desactivar</button>' :
                                '<button class="btn-action" onclick="activateUser(\'' + user.ci + '\')" title="Activar">✅ Activar</button>') +
                        '</div>' +
                    '</td>';
                tbody.appendChild(row);
            });

            // Update pagination
            totalPages = data.totalPages || 1;
            updatePagination(data);

            // Show pagination if needed
            document.getElementById('paginationContainer').style.display = totalPages > 1 ? 'flex' : 'none';
        }

        function updatePagination(data) {
            const paginationInfo = document.getElementById('paginationInfo');
            paginationInfo.textContent = 'Página ' + (data.number + 1) + ' de ' + data.totalPages + ' (' + data.totalElements + ' resultados)';

            document.getElementById('btnPrev').disabled = data.first;
            document.getElementById('btnNext').disabled = data.last;
        }

        function showEmpty() {
            const tbody = document.getElementById('usersTableBody');
            let html = '<tr><td colspan="7"><div class="empty-state">';
            html += '<div class="empty-state-icon">👥</div>';
            html += '<div class="empty-state-title">No se encontraron usuarios</div>';
            html += '<div style="font-size: 14px; margin-bottom: 20px;">Intenta ajustar los filtros de búsqueda.</div>';
            html += '</div></td></tr>';
            tbody.innerHTML = html;
            document.getElementById('paginationContainer').style.display = 'none';
        }

        function searchUsers() {
            loadUsers(0);
        }

        function getStatusBadge(status) {
            const badges = {
                'ACTIVE': '<span class="status-badge active">Activo</span>',
                'INACTIVE': '<span class="status-badge inactive">Inactivo</span>',
                'SUSPENDED': '<span class="status-badge suspended">Suspendido</span>'
            };
            return badges[status] || '<span class="status-badge">' + status + '</span>';
        }

        function formatDate(dateStr) {
            if (!dateStr) return '-';
            const date = new Date(dateStr);
            return date.toLocaleDateString('es-UY');
        }

        function formatDateTime(dateStr) {
            if (!dateStr) return '-';
            const date = new Date(dateStr);
            return date.toLocaleDateString('es-UY');
        }

        function formatNumber(num) {
            return num.toLocaleString('es-UY');
        }

        function handleSearch() {
            if (searchTimeout) {
                clearTimeout(searchTimeout);
            }
            searchTimeout = setTimeout(function() {
                loadUsers(0);
            }, 500);
        }

        function viewUser(ci) {
            window.location.href = 'user-detail.jsp?ci=' + encodeURIComponent(ci);
        }

        function editUser(ci) {
            window.location.href = 'user-detail.jsp?ci=' + encodeURIComponent(ci) + '&edit=true';
        }

        async function deactivateUser(ci) {
            if (!confirm('¿Está seguro de desactivar este usuario?')) {
                return;
            }

            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(ci) + '/deactivate', {
                    method: 'POST'
                });

                if (response.ok) {
                    alert('Usuario desactivado correctamente');
                    loadUsers(currentPage);
                    loadStatistics();
                } else {
                    alert('Error al desactivar usuario');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Error de conexión');
            }
        }

        async function activateUser(ci) {
            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(ci) + '/activate', {
                    method: 'POST'
                });

                if (response.ok) {
                    alert('Usuario activado correctamente');
                    loadUsers(currentPage);
                    loadStatistics();
                } else {
                    alert('Error al activar usuario');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Error de conexión');
            }
        }

        // Load data on page load
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Users page initializing...');
            loadUsers(0);
            loadStatistics();
        });
    //]]>
    </script>
</body>
</html>
