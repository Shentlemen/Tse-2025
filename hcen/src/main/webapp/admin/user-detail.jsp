<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalle de Usuario - Admin HCEN</title>
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
            max-width: 1200px;
            margin: 0 auto;
        }

        .header {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 25px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .header-top {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }

        .logo {
            font-size: 28px;
            font-weight: bold;
            color: #00d4ff;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .logo-icon {
            font-size: 32px;
        }

        .breadcrumb {
            color: #a0a0a0;
            font-size: 14px;
            margin-bottom: 15px;
        }

        .breadcrumb a {
            color: #00d4ff;
            text-decoration: none;
            transition: color 0.3s ease;
        }

        .breadcrumb a:hover {
            color: #00b8e6;
        }

        .user-header-content {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .user-avatar-section {
            display: flex;
            align-items: center;
        }

        .user-avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: linear-gradient(135deg, #00d4ff, #0095ff);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            color: white;
            font-weight: 600;
            box-shadow: 0 4px 15px rgba(0, 212, 255, 0.4);
        }

        .user-info {
            margin-left: 24px;
        }

        .user-name {
            font-size: 28px;
            color: #e4e4e4;
            margin-bottom: 8px;
            font-weight: 600;
        }

        .user-meta {
            font-size: 14px;
            color: #a0a0a0;
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .status-badge {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }

        .status-badge.active {
            background: rgba(16, 185, 129, 0.2);
            color: #10b981;
            border: 1px solid rgba(16, 185, 129, 0.3);
        }

        .status-badge.inactive {
            background: rgba(239, 68, 68, 0.2);
            color: #ef4444;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        .status-badge.suspended {
            background: rgba(245, 158, 11, 0.2);
            color: #f59e0b;
            border: 1px solid rgba(245, 158, 11, 0.3);
        }

        .action-buttons {
            display: flex;
            gap: 12px;
        }

        .btn {
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        .btn-primary {
            background: linear-gradient(135deg, #00d4ff, #0095ff);
            color: white;
            box-shadow: 0 4px 15px rgba(0, 212, 255, 0.3);
        }

        .btn-primary:hover {
            background: linear-gradient(135deg, #0095ff, #0066cc);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(0, 212, 255, 0.4);
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            color: #e4e4e4;
            box-shadow: 0 4px 15px rgba(255, 255, 255, 0.1);
        }

        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.15);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(255, 255, 255, 0.15);
        }

        .btn-danger {
            background: rgba(239, 68, 68, 0.2);
            color: #ef4444;
            border: 1px solid rgba(239, 68, 68, 0.3);
        }

        .btn-danger:hover {
            background: rgba(239, 68, 68, 0.3);
            transform: translateY(-2px);
        }

        .btn-success {
            background: rgba(16, 185, 129, 0.2);
            color: #10b981;
            border: 1px solid rgba(16, 185, 129, 0.3);
        }

        .btn-success:hover {
            background: rgba(16, 185, 129, 0.3);
            transform: translateY(-2px);
        }

        .content-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 24px;
        }

        .card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            border-radius: 12px;
            padding: 32px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .card h2 {
            font-size: 20px;
            color: #00d4ff;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 2px solid rgba(0, 212, 255, 0.2);
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #a0a0a0;
            font-weight: 500;
            font-size: 14px;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid rgba(255, 255, 255, 0.1);
            border-radius: 8px;
            font-size: 14px;
            background: rgba(255, 255, 255, 0.05);
            color: #e4e4e4;
            transition: all 0.3s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #00d4ff;
            background: rgba(255, 255, 255, 0.08);
        }

        .form-group input:disabled {
            background: rgba(255, 255, 255, 0.02);
            cursor: not-allowed;
            opacity: 0.6;
        }

        .form-group select {
            cursor: pointer;
        }

        .form-group select option {
            background: #1a1a2e;
            color: #e4e4e4;
        }

        .info-item {
            display: flex;
            flex-direction: column;
            margin-bottom: 20px;
        }

        .info-item label {
            font-size: 12px;
            color: #a0a0a0;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .info-item .value {
            font-size: 16px;
            color: #e4e4e4;
            font-weight: 500;
        }

        .info-item .value.empty {
            color: #666;
            font-style: italic;
        }

        .activity-list {
            list-style: none;
        }

        .activity-item {
            padding: 16px 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .activity-item:last-child {
            border-bottom: none;
        }

        .activity-item .activity-title {
            font-weight: 600;
            color: #e4e4e4;
            margin-bottom: 4px;
        }

        .activity-item .activity-time {
            font-size: 12px;
            color: #a0a0a0;
        }

        .alert {
            padding: 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
            border: 1px solid;
        }

        .alert.show {
            display: block;
        }

        .alert-success {
            background: rgba(16, 185, 129, 0.2);
            color: #10b981;
            border-color: rgba(16, 185, 129, 0.3);
        }

        .alert-error {
            background: rgba(239, 68, 68, 0.2);
            color: #ef4444;
            border-color: rgba(239, 68, 68, 0.3);
        }

        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(26, 26, 46, 0.9);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
            backdrop-filter: blur(5px);
        }

        .loading-spinner {
            width: 50px;
            height: 50px;
            border: 4px solid rgba(255, 255, 255, 0.1);
            border-top-color: #00d4ff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        @media (max-width: 768px) {
            .content-grid {
                grid-template-columns: 1fr;
            }

            .user-header-content {
                flex-direction: column;
                gap: 20px;
            }

            .action-buttons {
                width: 100%;
                flex-direction: column;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="header-top">
                <div class="logo">
                    <span class="logo-icon">🏥</span>
                    HCEN Admin
                </div>
            </div>
            <div class="breadcrumb">
                <a href="dashboard.jsp">Dashboard</a> /
                <a href="users.jsp">Usuarios</a> /
                <span>Detalle de Usuario</span>
            </div>

            <div id="userHeader" style="display: none;">
                <div class="user-header-content">
                    <div class="user-avatar-section">
                        <div class="user-avatar" id="userAvatar">JD</div>
                        <div class="user-info">
                            <div class="user-name" id="userName">Cargando...</div>
                            <div class="user-meta">
                                <span>CI: <span id="userCI"></span></span>
                                <span id="userStatusBadge"></span>
                                <span>INUS ID: <span id="inusId"></span></span>
                            </div>
                        </div>
                    </div>
                    <div class="action-buttons" id="actionButtons">
                        <!-- Actions will be populated based on edit mode -->
                    </div>
                </div>
            </div>
        </div>

        <div id="alertSuccess" class="alert alert-success">
            ✓ Usuario actualizado correctamente
        </div>

        <div id="alertError" class="alert alert-error">
            <strong>Error:</strong> <span id="errorMessage"></span>
        </div>

        <div class="content-grid">
            <!-- Main Content -->
            <div class="card">
                <h2 id="formTitle">Información del Usuario</h2>

                <form id="userForm">
                    <div class="form-group">
                        <label>Cédula de Identidad</label>
                        <input type="text" id="ci" disabled>
                    </div>

                    <div class="form-group">
                        <label>Nombre</label>
                        <input type="text" id="firstName" required>
                    </div>

                    <div class="form-group">
                        <label>Apellido</label>
                        <input type="text" id="lastName" required>
                    </div>

                    <div class="form-group">
                        <label>Fecha de Nacimiento</label>
                        <input type="date" id="dateOfBirth" required>
                    </div>

                    <div class="form-group">
                        <label>Email</label>
                        <input type="email" id="email">
                    </div>

                    <div class="form-group">
                        <label>Teléfono</label>
                        <input type="tel" id="phoneNumber" maxlength="9">
                    </div>

                    <div class="form-group">
                        <label>Estado</label>
                        <select id="status">
                            <option value="ACTIVE">Activo</option>
                            <option value="INACTIVE">Inactivo</option>
                            <option value="SUSPENDED">Suspendido</option>
                        </select>
                    </div>
                </form>
            </div>

            <!-- Sidebar -->
            <div>
                <div class="card" style="margin-bottom: 24px;">
                    <h2>Información del Sistema</h2>

                    <div class="info-item">
                        <label>ID INUS</label>
                        <div class="value" id="sidebarInusId">-</div>
                    </div>

                    <div class="info-item">
                        <label>Fecha de Registro</label>
                        <div class="value" id="createdAt">-</div>
                    </div>

                    <div class="info-item">
                        <label>Última Actualización</label>
                        <div class="value" id="updatedAt">-</div>
                    </div>
                </div>

                <div class="card">
                    <h2>Actividad Reciente</h2>
                    <ul class="activity-list">
                        <li class="activity-item">
                            <div class="activity-title">Perfil actualizado</div>
                            <div class="activity-time" id="lastUpdate">-</div>
                        </li>
                        <li class="activity-item">
                            <div class="activity-title">Usuario registrado</div>
                            <div class="activity-time" id="registrationDate">-</div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="loading-overlay" id="loadingOverlay">
        <div class="loading-spinner"></div>
    </div>

    <script type="text/javascript">
    //<![CDATA[
        let currentUser = null;
        let editMode = false;

        function getUserCI() {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get('ci');
        }

        function isEditMode() {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get('edit') === 'true';
        }

        async function loadUser() {
            const ci = getUserCI();

            if (!ci) {
                window.location.href = 'users.jsp';
                return;
            }

            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(ci));

                if (response.ok) {
                    currentUser = await response.json();
                    displayUser(currentUser);
                    setEditMode(isEditMode());
                    document.getElementById('loadingOverlay').style.display = 'none';
                } else if (response.status === 404) {
                    showError('Usuario no encontrado');
                    document.getElementById('loadingOverlay').style.display = 'none';
                } else {
                    showError('Error al cargar el usuario');
                    document.getElementById('loadingOverlay').style.display = 'none';
                }
            } catch (error) {
                console.error('Error:', error);
                showError('Error de conexión');
                document.getElementById('loadingOverlay').style.display = 'none';
            }
        }

        function displayUser(user) {
            // Header
            const initials = (user.firstName.charAt(0) + user.lastName.charAt(0)).toUpperCase();
            document.getElementById('userAvatar').textContent = initials;
            document.getElementById('userName').textContent = user.firstName + ' ' + user.lastName;
            document.getElementById('userCI').textContent = user.ci;
            document.getElementById('inusId').textContent = user.inusId || 'N/A';
            document.getElementById('userStatusBadge').innerHTML = getStatusBadge(user.status);

            // Form
            document.getElementById('ci').value = user.ci;
            document.getElementById('firstName').value = user.firstName;
            document.getElementById('lastName').value = user.lastName;
            document.getElementById('dateOfBirth').value = user.dateOfBirth;
            document.getElementById('email').value = user.email || '';
            document.getElementById('phoneNumber').value = user.phoneNumber || '';
            document.getElementById('status').value = user.status;

            // Sidebar
            document.getElementById('sidebarInusId').textContent = user.inusId || 'No asignado';
            document.getElementById('createdAt').textContent = formatDateTime(user.createdAt);
            document.getElementById('updatedAt').textContent = formatDateTime(user.updatedAt);
            document.getElementById('lastUpdate').textContent = formatDateTime(user.updatedAt);
            document.getElementById('registrationDate').textContent = formatDateTime(user.createdAt);

            document.getElementById('userHeader').style.display = 'block';
        }

        function setEditMode(enabled) {
            editMode = enabled;

            const fields = ['firstName', 'lastName', 'dateOfBirth', 'email', 'phoneNumber', 'status'];
            fields.forEach(function(fieldId) {
                document.getElementById(fieldId).disabled = !enabled;
            });

            updateActionButtons();
            updateFormTitle();
        }

        function updateActionButtons() {
            const actionButtons = document.getElementById('actionButtons');

            if (editMode) {
                actionButtons.innerHTML =
                    '<button class="btn btn-primary" onclick="saveUser()">Guardar Cambios</button>' +
                    '<button class="btn btn-secondary" onclick="cancelEdit()">Cancelar</button>';
            } else {
                const statusActions = currentUser.status === 'ACTIVE' ?
                    '<button class="btn btn-danger" onclick="deactivateUser()">Desactivar</button>' :
                    '<button class="btn btn-success" onclick="activateUser()">Activar</button>';

                actionButtons.innerHTML =
                    '<button class="btn btn-primary" onclick="enableEditMode()">Editar</button>' +
                    statusActions;
            }
        }

        function updateFormTitle() {
            document.getElementById('formTitle').textContent =
                editMode ? 'Editar Información del Usuario' : 'Información del Usuario';
        }

        function enableEditMode() {
            setEditMode(true);
        }

        function cancelEdit() {
            setEditMode(false);
            displayUser(currentUser);
        }

        async function saveUser() {
            // Only send mutable fields - ci, dateOfBirth, and status are immutable
            const updatedData = {
                firstName: document.getElementById('firstName').value.trim(),
                lastName: document.getElementById('lastName').value.trim(),
                email: document.getElementById('email').value.trim() || null,
                phoneNumber: document.getElementById('phoneNumber').value.trim() || null
            };

            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(currentUser.ci), {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(updatedData)
                });

                if (response.ok) {
                    currentUser = await response.json();
                    displayUser(currentUser);
                    setEditMode(false);
                    showSuccess();
                } else {
                    const errorData = await response.json();
                    showError(errorData.message || 'No se pudo actualizar el usuario');
                }
            } catch (error) {
                console.error('Error:', error);
                showError('Error de conexión');
            }
        }

        async function deactivateUser() {
            if (!confirm('¿Está seguro de desactivar este usuario?')) {
                return;
            }

            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(currentUser.ci) + '/deactivate', {
                    method: 'POST'
                });

                if (response.ok) {
                    loadUser(); // Reload to refresh status
                    showSuccess();
                } else {
                    showError('Error al desactivar usuario');
                }
            } catch (error) {
                console.error('Error:', error);
                showError('Error de conexión');
            }
        }

        async function activateUser() {
            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(currentUser.ci) + '/activate', {
                    method: 'POST'
                });

                if (response.ok) {
                    loadUser(); // Reload to refresh status
                    showSuccess();
                } else {
                    showError('Error al activar usuario');
                }
            } catch (error) {
                console.error('Error:', error);
                showError('Error de conexión');
            }
        }

        function getStatusBadge(status) {
            const badges = {
                'ACTIVE': '<span class="status-badge active">Activo</span>',
                'INACTIVE': '<span class="status-badge inactive">Inactivo</span>',
                'SUSPENDED': '<span class="status-badge suspended">Suspendido</span>'
            };
            return badges[status] || '<span class="status-badge">' + status + '</span>';
        }

        function formatDateTime(dateInput) {
            if (!dateInput) return '-';

            let date;

            // Handle Java LocalDateTime array format: [year, month, day, hour, minute, second, nano]
            if (Array.isArray(dateInput)) {
                // Java months are 1-12, JavaScript months are 0-11
                date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2],
                                dateInput[3] || 0, dateInput[4] || 0, dateInput[5] || 0);
            } else {
                // Handle string format
                date = new Date(dateInput);
            }

            return date.toLocaleString('es-UY');
        }

        function showSuccess() {
            document.getElementById('alertSuccess').classList.add('show');
            setTimeout(function() {
                document.getElementById('alertSuccess').classList.remove('show');
            }, 3000);
        }

        function showError(message) {
            document.getElementById('errorMessage').textContent = message;
            document.getElementById('alertError').classList.add('show');
        }

        function logout() {
            window.location.href = '/hcen/login-admin.jsp';
        }

        window.addEventListener('DOMContentLoaded', loadUser);
    //]]>
    </script>
</body>
</html>
