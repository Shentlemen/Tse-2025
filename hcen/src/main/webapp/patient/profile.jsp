<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi Perfil - HCEN</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: #f5f7fa;
            min-height: 100vh;
        }

        .navbar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 16px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .navbar h1 {
            font-size: 24px;
            font-weight: 600;
        }

        .navbar .user-info {
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .navbar .btn-logout {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            transition: background 0.3s;
        }

        .navbar .btn-logout:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 32px 24px;
        }

        .profile-header {
            background: white;
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .profile-header .header-content {
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .profile-header .user-avatar {
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

        .profile-header .user-details {
            flex: 1;
            margin-left: 24px;
        }

        .profile-header .user-name {
            font-size: 28px;
            color: #333;
            margin-bottom: 4px;
        }

        .profile-header .user-ci {
            font-size: 14px;
            color: #666;
        }

        .profile-header .btn-edit {
            background: #667eea;
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s;
        }

        .profile-header .btn-edit:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .quick-actions {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }

        .action-card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            color: inherit;
        }

        .action-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
        }

        .action-card .icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            margin-bottom: 16px;
        }

        .action-card .icon.blue { background: #e8f0fe; color: #1a73e8; }
        .action-card .icon.purple { background: #f3e8ff; color: #7c3aed; }
        .action-card .icon.green { background: #d1f4e0; color: #059669; }
        .action-card .icon.orange { background: #fff3e0; color: #f57c00; }

        .action-card h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 8px;
        }

        .action-card p {
            font-size: 14px;
            color: #666;
            line-height: 1.5;
        }

        .profile-info {
            background: white;
            border-radius: 12px;
            padding: 32px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .profile-info h2 {
            font-size: 20px;
            color: #333;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 2px solid #f0f0f0;
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 24px;
        }

        .info-item {
            display: flex;
            flex-direction: column;
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

        .alert {
            padding: 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
        }

        .alert.show {
            display: block;
        }

        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
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

        .loading-overlay .spinner {
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

        /* Edit Modal */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            z-index: 2000;
            align-items: center;
            justify-content: center;
        }

        .modal.show {
            display: flex;
        }

        .modal-content {
            background: white;
            border-radius: 16px;
            padding: 32px;
            max-width: 600px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
        }

        .modal-header h2 {
            font-size: 24px;
            color: #333;
        }

        .modal-header .btn-close {
            background: none;
            border: none;
            font-size: 24px;
            color: #999;
            cursor: pointer;
            padding: 0;
            width: 32px;
            height: 32px;
            border-radius: 50%;
            transition: background 0.3s;
        }

        .modal-header .btn-close:hover {
            background: #f0f0f0;
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

        .form-group input {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
        }

        .form-group input:disabled {
            background: #f5f5f5;
            cursor: not-allowed;
        }

        .modal-footer {
            display: flex;
            gap: 12px;
            margin-top: 24px;
        }

        .btn {
            flex: 1;
            padding: 12px 24px;
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

        .btn-primary:hover:not(:disabled) {
            background: #5568d3;
        }

        .btn-primary:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .btn-secondary {
            background: #f8f9fa;
            color: #666;
            border: 2px solid #e1e8ed;
        }

        .btn-secondary:hover {
            background: #e1e8ed;
        }
    </style>
</head>
<body>
    <div class="navbar">
        <h1>HCEN - Mi Perfil</h1>
        <div class="user-info">
            <span id="navbarUserName">Cargando...</span>
            <button class="btn-logout" onclick="logout()">Cerrar Sesión</button>
        </div>
    </div>

    <div class="container">
        <div id="alertError" class="alert alert-error">
            <strong>Error:</strong> <span id="errorMessage"></span>
        </div>

        <div class="profile-header" style="display: none;" id="profileHeader">
            <div class="header-content">
                <div style="display: flex; align-items: center;">
                    <div class="user-avatar" id="userAvatar">JD</div>
                    <div class="user-details">
                        <div class="user-name" id="userName">Cargando...</div>
                        <div class="user-ci">CI: <span id="userCI"></span> | INUS ID: <span id="inusId"></span></div>
                    </div>
                </div>
                <button class="btn-edit" onclick="openEditModal()">Editar Perfil</button>
            </div>
        </div>

        <div class="quick-actions">
            <a href="#" class="action-card" onclick="alert('Función disponible próximamente'); return false;">
                <div class="icon blue">📋</div>
                <h3>Historia Clínica</h3>
                <p>Accede a todos tus documentos médicos y resultados</p>
            </a>

            <a href="#" class="action-card" onclick="alert('Función disponible próximamente'); return false;">
                <div class="icon purple">🔒</div>
                <h3>Políticas de Privacidad</h3>
                <p>Administra quién puede acceder a tu información</p>
            </a>

            <a href="#" class="action-card" onclick="alert('Función disponible próximamente'); return false;">
                <div class="icon green">👁️</div>
                <h3>Auditoría de Accesos</h3>
                <p>Ver quién ha accedido a tus registros médicos</p>
            </a>

            <a href="#" class="action-card" onclick="alert('Función disponible próximamente'); return false;">
                <div class="icon orange">📱</div>
                <h3>Aplicación Móvil</h3>
                <p>Descarga la app para acceder desde tu teléfono</p>
            </a>
        </div>

        <div class="profile-info" style="display: none;" id="profileInfo">
            <h2>Información Personal</h2>
            <div class="info-grid">
                <div class="info-item">
                    <label>Nombre Completo</label>
                    <div class="value" id="fullName">-</div>
                </div>

                <div class="info-item">
                    <label>Fecha de Nacimiento</label>
                    <div class="value" id="dateOfBirth">-</div>
                </div>

                <div class="info-item">
                    <label>Email</label>
                    <div class="value" id="email" class="value empty">No especificado</div>
                </div>

                <div class="info-item">
                    <label>Teléfono</label>
                    <div class="value" id="phoneNumber" class="value empty">No especificado</div>
                </div>

                <div class="info-item">
                    <label>Estado</label>
                    <div class="value" id="status">-</div>
                </div>

                <div class="info-item">
                    <label>Fecha de Registro</label>
                    <div class="value" id="createdAt">-</div>
                </div>
            </div>
        </div>
    </div>

    <!-- Edit Modal -->
    <div class="modal" id="editModal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Editar Perfil</h2>
                <button class="btn-close" onclick="closeEditModal()">&times;</button>
            </div>

            <form id="editForm">
                <div class="form-group">
                    <label>Cédula de Identidad</label>
                    <input type="text" id="editCI" disabled>
                </div>

                <div class="form-group">
                    <label>Nombre</label>
                    <input type="text" id="editFirstName" required>
                </div>

                <div class="form-group">
                    <label>Apellido</label>
                    <input type="text" id="editLastName" required>
                </div>

                <div class="form-group">
                    <label>Fecha de Nacimiento</label>
                    <input type="date" id="editDateOfBirth" required>
                </div>

                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="editEmail">
                </div>

                <div class="form-group">
                    <label>Teléfono</label>
                    <input type="tel" id="editPhoneNumber" maxlength="9">
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeEditModal()">Cancelar</button>
                    <button type="submit" class="btn btn-primary" id="saveBtn">Guardar Cambios</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Loading Overlay -->
    <div class="loading-overlay" id="loadingOverlay">
        <div class="spinner"></div>
    </div>

    <script type="text/javascript">
    //<![CDATA[
        let currentUser = null;

        // Get CI from URL parameter or session
        function getCurrentCI() {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get('ci') || sessionStorage.getItem('userCI');
        }

        // Load user profile
        async function loadProfile() {
            const ci = getCurrentCI();

            if (!ci) {
                window.location.href = '/hcen/login-patient.jsp';
                return;
            }

            try {
                const response = await fetch('/hcen/api/inus/users/' + encodeURIComponent(ci));

                if (response.ok) {
                    currentUser = await response.json();
                    displayProfile(currentUser);
                    document.getElementById('loadingOverlay').style.display = 'none';
                } else if (response.status === 404) {
                    showError('Usuario no encontrado');
                    document.getElementById('loadingOverlay').style.display = 'none';
                } else {
                    showError('Error al cargar el perfil');
                    document.getElementById('loadingOverlay').style.display = 'none';
                }
            } catch (error) {
                console.error('Error:', error);
                showError('Error de conexión');
                document.getElementById('loadingOverlay').style.display = 'none';
            }
        }

        function displayProfile(user) {
            // Navbar
            document.getElementById('navbarUserName').textContent = user.firstName + ' ' + user.lastName;

            // Profile header
            const initials = (user.firstName.charAt(0) + user.lastName.charAt(0)).toUpperCase();
            document.getElementById('userAvatar').textContent = initials;
            document.getElementById('userName').textContent = user.firstName + ' ' + user.lastName;
            document.getElementById('userCI').textContent = user.ci;
            document.getElementById('inusId').textContent = user.inusId || 'N/A';

            // Profile info
            document.getElementById('fullName').textContent = user.firstName + ' ' + user.lastName;
            document.getElementById('dateOfBirth').textContent = formatDate(user.dateOfBirth);

            const emailEl = document.getElementById('email');
            if (user.email) {
                emailEl.textContent = user.email;
                emailEl.classList.remove('empty');
            } else {
                emailEl.textContent = 'No especificado';
                emailEl.classList.add('empty');
            }

            const phoneEl = document.getElementById('phoneNumber');
            if (user.phoneNumber) {
                phoneEl.textContent = user.phoneNumber;
                phoneEl.classList.remove('empty');
            } else {
                phoneEl.textContent = 'No especificado';
                phoneEl.classList.add('empty');
            }

            document.getElementById('status').textContent = translateStatus(user.status);
            document.getElementById('createdAt').textContent = formatDateTime(user.createdAt);

            // Show sections
            document.getElementById('profileHeader').style.display = 'block';
            document.getElementById('profileInfo').style.display = 'block';

            // Store CI in session
            sessionStorage.setItem('userCI', user.ci);
        }

        function formatDate(dateStr) {
            if (!dateStr) return '-';
            const date = new Date(dateStr);
            return date.toLocaleDateString('es-UY', { year: 'numeric', month: 'long', day: 'numeric' });
        }

        function formatDateTime(dateStr) {
            if (!dateStr) return '-';
            const date = new Date(dateStr);
            return date.toLocaleString('es-UY');
        }

        function translateStatus(status) {
            const translations = {
                'ACTIVE': 'Activo',
                'INACTIVE': 'Inactivo',
                'SUSPENDED': 'Suspendido'
            };
            return translations[status] || status;
        }

        function showError(message) {
            document.getElementById('errorMessage').textContent = message;
            document.getElementById('alertError').classList.add('show');
        }

        // Edit Modal Functions
        function openEditModal() {
            if (!currentUser) return;

            document.getElementById('editCI').value = currentUser.ci;
            document.getElementById('editFirstName').value = currentUser.firstName;
            document.getElementById('editLastName').value = currentUser.lastName;
            document.getElementById('editDateOfBirth').value = currentUser.dateOfBirth;
            document.getElementById('editEmail').value = currentUser.email || '';
            document.getElementById('editPhoneNumber').value = currentUser.phoneNumber || '';

            document.getElementById('editModal').classList.add('show');
        }

        function closeEditModal() {
            document.getElementById('editModal').classList.remove('show');
        }

        // Save Profile Changes
        document.getElementById('editForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const saveBtn = document.getElementById('saveBtn');
            saveBtn.disabled = true;
            saveBtn.textContent = 'Guardando...';

            const updatedData = {
                ci: currentUser.ci,
                firstName: document.getElementById('editFirstName').value.trim(),
                lastName: document.getElementById('editLastName').value.trim(),
                dateOfBirth: document.getElementById('editDateOfBirth').value,
                email: document.getElementById('editEmail').value.trim() || null,
                phoneNumber: document.getElementById('editPhoneNumber').value.trim() || null
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
                    displayProfile(currentUser);
                    closeEditModal();
                } else {
                    const errorData = await response.json();
                    alert('Error: ' + (errorData.message || 'No se pudo actualizar el perfil'));
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Error de conexión');
            } finally {
                saveBtn.disabled = false;
                saveBtn.textContent = 'Guardar Cambios';
            }
        });

        function logout() {
            sessionStorage.clear();
            window.location.href = '/hcen/login-patient.jsp';
        }

        // Load profile on page load
        window.addEventListener('DOMContentLoaded', loadProfile);
    //]]>
    </script>
</body>
</html>
