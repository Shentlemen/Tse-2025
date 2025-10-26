<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalle de Usuario - Admin HCEN</title>
    <link rel="stylesheet" href="../css/admin-common.css">
    <style>
        .back-button {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            color: #667eea;
            text-decoration: none;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 20px;
            transition: gap 0.3s;
        }

        .back-button:hover {
            gap: 12px;
        }

        .user-header {
            background: white;
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 32px;
            color: white;
            font-weight: 600;
        }

        .user-info {
            margin-left: 24px;
        }

        .user-name {
            font-size: 28px;
            color: #333;
            margin-bottom: 4px;
        }

        .user-meta {
            font-size: 14px;
            color: #666;
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .status-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }

        .status-badge.active {
            background: #d1f4e0;
            color: #059669;
        }

        .status-badge.inactive {
            background: #fee;
            color: #dc2626;
        }

        .status-badge.suspended {
            background: #fff3cd;
            color: #856404;
        }

        .action-buttons {
            display: flex;
            gap: 12px;
        }

        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary {
            background: #667eea;
            color: white;
        }

        .btn-primary:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .btn-danger {
            background: #dc2626;
            color: white;
        }

        .btn-danger:hover {
            background: #b91c1c;
        }

        .btn-success {
            background: #059669;
            color: white;
        }

        .btn-success:hover {
            background: #047857;
        }

        .content-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 24px;
        }

        .card {
            background: white;
            border-radius: 12px;
            padding: 32px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .card h2 {
            font-size: 20px;
            color: #333;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 2px solid #f0f0f0;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
            font-size: 14px;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }

        .form-group input:disabled {
            background: #f5f5f5;
            cursor: not-allowed;
        }

        .info-item {
            display: flex;
            flex-direction: column;
            margin-bottom: 20px;
        }

        .info-item label {
            font-size: 12px;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .info-item .value {
            font-size: 16px;
            color: #333;
            font-weight: 500;
        }

        .info-item .value.empty {
            color: #999;
            font-style: italic;
        }

        .activity-list {
            list-style: none;
        }

        .activity-item {
            padding: 16px 0;
            border-bottom: 1px solid #f0f0f0;
        }

        .activity-item:last-child {
            border-bottom: none;
        }

        .activity-item .activity-title {
            font-weight: 600;
            color: #333;
            margin-bottom: 4px;
        }

        .activity-item .activity-time {
            font-size: 12px;
            color: #999;
        }

        .alert {
            padding: 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
        }

        .alert.show {
            display: block;
        }

        .alert-success {
            background: #d1f4e0;
            color: #059669;
            border: 1px solid #a7e6c8;
        }

        .alert-error {
            background: #fee;
            color: #dc2626;
            border: 1px solid #fcc;
        }

        .loading-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(255, 255, 255, 0.9);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 1000;
        }

        .loading-spinner {
            width: 50px;
            height: 50px;
            border: 4px solid #f0f0f0;
            border-top-color: #667eea;
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
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <div class="navbar-brand">
            <h1>HCEN Admin</h1>
        </div>
        <div class="navbar-menu">
            <a href="dashboard.jsp">Dashboard</a>
            <a href="users.jsp" class="active">Usuarios</a>
            <a href="clinics.jsp">Clínicas</a>
            <a href="#" onclick="alert('Función disponible próximamente')">Reportes</a>
            <button class="btn-logout" onclick="logout()">Cerrar Sesión</button>
        </div>
    </nav>

    <div class="container">
        <a href="users.jsp" class="back-button">← Volver a Usuarios</a>

        <div id="alertSuccess" class="alert alert-success">
            Usuario actualizado correctamente
        </div>

        <div id="alertError" class="alert alert-error">
            <strong>Error:</strong> <span id="errorMessage"></span>
        </div>

        <div class="user-header" id="userHeader" style="display: none;">
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
            const updatedData = {
                ci: currentUser.ci,
                firstName: document.getElementById('firstName').value.trim(),
                lastName: document.getElementById('lastName').value.trim(),
                dateOfBirth: document.getElementById('dateOfBirth').value,
                email: document.getElementById('email').value.trim() || null,
                phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
                status: document.getElementById('status').value
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

        function formatDateTime(dateStr) {
            if (!dateStr) return '-';
            const date = new Date(dateStr);
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
