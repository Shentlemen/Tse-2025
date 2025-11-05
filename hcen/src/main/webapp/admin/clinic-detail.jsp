<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Detalle de Clínica</title>
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
        }

        .btn:hover:not(:disabled) {
            background: linear-gradient(135deg, #0095ff, #0066cc);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(0, 212, 255, 0.4);
        }

        .btn:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            box-shadow: 0 4px 15px rgba(255, 255, 255, 0.1);
        }

        .btn-secondary:hover:not(:disabled) {
            background: rgba(255, 255, 255, 0.15);
            box-shadow: 0 6px 20px rgba(255, 255, 255, 0.15);
        }

        .btn-success {
            background: linear-gradient(135deg, #4caf50, #388e3c);
        }

        .btn-success:hover:not(:disabled) {
            background: linear-gradient(135deg, #388e3c, #2e7d32);
        }

        .btn-danger {
            background: linear-gradient(135deg, #f44336, #d32f2f);
        }

        .btn-danger:hover:not(:disabled) {
            background: linear-gradient(135deg, #d32f2f, #c62828);
        }

        .content-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 25px;
        }

        .card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .card-title {
            font-size: 20px;
            font-weight: 600;
            color: #00d4ff;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 2px solid rgba(0, 212, 255, 0.3);
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .info-grid {
            display: grid;
            gap: 20px;
        }

        .info-item {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .info-label {
            color: #a0a0a0;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .info-value {
            color: #e4e4e4;
            font-size: 15px;
            word-break: break-word;
        }

        .info-value.masked {
            font-family: 'Courier New', monospace;
            color: #00d4ff;
        }

        .badge {
            display: inline-block;
            padding: 8px 16px;
            border-radius: 12px;
            font-size: 12px;
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

        .stat-card {
            background: rgba(255, 255, 255, 0.03);
            padding: 20px;
            border-radius: 12px;
            border: 1px solid rgba(255, 255, 255, 0.05);
        }

        .stat-label {
            color: #a0a0a0;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 10px;
        }

        .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #00d4ff;
        }

        .action-list {
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .action-btn {
            width: 100%;
            justify-content: center;
            padding: 14px 20px;
        }

        .message {
            padding: 15px 20px;
            border-radius: 10px;
            margin-bottom: 25px;
            display: none;
            align-items: center;
            gap: 10px;
        }

        .message.success {
            background: rgba(76, 175, 80, 0.1);
            border: 1px solid rgba(76, 175, 80, 0.3);
            color: #4caf50;
            display: flex;
        }

        .message.error {
            background: rgba(244, 67, 54, 0.1);
            border: 1px solid rgba(244, 67, 54, 0.3);
            color: #f44336;
            display: flex;
        }

        .loading {
            text-align: center;
            padding: 60px 20px;
        }

        .loading-spinner {
            display: inline-block;
            width: 40px;
            height: 40px;
            border: 4px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: #00d4ff;
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .edit-form {
            display: none;
        }

        .edit-form.show {
            display: block;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            color: #a0a0a0;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .form-control {
            width: 100%;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            padding: 12px 16px;
            border-radius: 8px;
            color: #e4e4e4;
            font-size: 14px;
            transition: all 0.3s ease;
            font-family: inherit;
        }

        .form-control:focus {
            outline: none;
            border-color: #00d4ff;
            background: rgba(255, 255, 255, 0.08);
            box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.1);
        }

        .form-actions {
            display: flex;
            gap: 12px;
            margin-top: 25px;
            padding-top: 20px;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }

        @media (max-width: 1024px) {
            .content-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 15px;
            }

            .header-actions {
                width: 100%;
                flex-direction: column;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }

            .card {
                padding: 20px;
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
                <div class="page-title" id="pageTitle">Detalle de Clínica</div>
                <div class="breadcrumb">
                    <a href="/hcen/admin/dashboard.jsp">Dashboard</a> /
                    <a href="/hcen/admin/clinics.jsp">Clínicas</a> /
                    Detalle
                </div>
            </div>
            <div class="header-actions">
                <button class="btn btn-secondary" onclick="goBack()">
                    ← Volver
                </button>
            </div>
        </div>

        <div id="loadingIndicator" class="loading">
            <div class="loading-spinner"></div>
            <div style="margin-top: 20px; color: #a0a0a0;">Cargando datos de la clínica...</div>
        </div>

        <div id="clinicContent" style="display: none;">
            <div class="content-grid">
                <!-- Main Content -->
                <div>
                    <div class="card">
                        <div class="card-title">
                            📋 Información de la Clínica
                        </div>

                        <div id="viewMode">
                            <div class="info-grid">
                                <div class="info-item">
                                    <div class="info-label">ID de Clínica</div>
                                    <div class="info-value" id="clinicId">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Nombre</div>
                                    <div class="info-value" id="clinicName">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Dirección</div>
                                    <div class="info-value" id="address">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Ciudad</div>
                                    <div class="info-value" id="city">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Teléfono</div>
                                    <div class="info-value" id="phoneNumber">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Email</div>
                                    <div class="info-value" id="email">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">URL Nodo Periférico</div>
                                    <div class="info-value" id="peripheralNodeUrl">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">API Key (Enmascarada)</div>
                                    <div class="info-value masked" id="apiKey">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Estado</div>
                                    <div id="statusBadge">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Fecha de Registro</div>
                                    <div class="info-value" id="createdAt">-</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">Última Actualización</div>
                                    <div class="info-value" id="updatedAt">-</div>
                                </div>
                            </div>

                            <div class="form-actions">
                                <button class="btn" onclick="toggleEditMode()">
                                    ✏️ Editar Información
                                </button>
                            </div>
                        </div>

                        <div id="editMode" class="edit-form">
                            <form id="editForm" onsubmit="saveChanges(event)">
                                <div class="form-group">
                                    <label for="editClinicName">Nombre de la Clínica</label>
                                    <input type="text" id="editClinicName" class="form-control" required>
                                </div>
                                <div class="form-group">
                                    <label for="editAddress">Dirección</label>
                                    <input type="text" id="editAddress" class="form-control" required>
                                </div>
                                <div class="form-group">
                                    <label for="editCity">Ciudad</label>
                                    <input type="text" id="editCity" class="form-control" required>
                                </div>
                                <div class="form-group">
                                    <label for="editPhoneNumber">Teléfono</label>
                                    <input type="tel" id="editPhoneNumber" class="form-control" pattern="0[0-9]{8}" required>
                                </div>
                                <div class="form-group">
                                    <label for="editEmail">Email</label>
                                    <input type="email" id="editEmail" class="form-control" required>
                                </div>
                                <div class="form-group">
                                    <label for="editPeripheralNodeUrl">URL Nodo Periférico</label>
                                    <input type="url" id="editPeripheralNodeUrl" class="form-control" pattern="https://.*" required>
                                </div>

                                <div class="form-actions">
                                    <button type="button" class="btn btn-secondary" onclick="cancelEdit()">
                                        ❌ Cancelar
                                    </button>
                                    <button type="submit" class="btn btn-success">
                                        ✅ Guardar Cambios
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- Sidebar -->
                <div>
                    <div class="card">
                        <div class="card-title">
                            📊 Estadísticas
                        </div>
                        <div class="info-grid">
                            <div class="stat-card">
                                <div class="stat-label">Usuarios Registrados</div>
                                <div class="stat-value" id="statUsers">-</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-label">Documentos Registrados</div>
                                <div class="stat-value" id="statDocuments">-</div>
                            </div>
                        </div>
                    </div>

                    <div class="card" style="margin-top: 25px;">
                        <div class="card-title">
                            ⚙️ Acciones
                        </div>
                        <div class="action-list">
                            <button class="btn btn-success action-btn" id="onboardBtn" onclick="onboardClinic()" style="display: none;">
                                🚀 Onboardear Clínica
                            </button>
                            <button class="btn btn-danger action-btn" id="deactivateBtn" onclick="deactivateClinic()" style="display: none;">
                                ❌ Desactivar Clínica
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // API Configuration
        const API_BASE = '/hcen/api';

        // Current clinic data
        let currentClinic = null;

        /**
         * Get JWT token from sessionStorage
         */
        function getToken() {
            return sessionStorage.getItem('accessToken');
        }

        /**
         * Get clinic ID from URL parameter
         */
        function getClinicId() {
            const params = new URLSearchParams(window.location.search);
            return params.get('id');
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

                if (response.status === 204) {
                    return { success: true };
                }

                const data = await response.json();

                if (!response.ok) {
                    throw new Error(data.message || `Error ${response.status}`);
                }

                return data;
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
            messageDiv.textContent = (type === 'error' ? '❌ ' : '✅ ') + text;
            messageDiv.className = 'message ' + type;
            messageDiv.style.display = 'flex';

            window.scrollTo({ top: 0, behavior: 'smooth' });

            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 5000);
        }

        /**
         * Load clinic details
         */
        async function loadClinicDetails() {
            const clinicId = getClinicId();

            console.log('Clinic id from URL:', clinicId);

            if (!clinicId) {
                showMessage('ID de clínica no especificado en la URL. Use: ?id=clinic-xxx', 'error');
                setTimeout(() => goBack(), 2000);
                return;
            }

            // try {
                const clinic = await apiCall(`/admin/clinics/`+ clinicId);

                // Debug: Log the response
                console.log('Clinic API Response:', clinic);
                console.log('Clinic fields:', {
                    clinicId: clinic.clinicId,
                    clinicName: clinic.clinicName,
                    address: clinic.address,
                    city: clinic.city,
                    phoneNumber: clinic.phoneNumber,
                    email: clinic.email,
                    status: clinic.status
                });

                if (!clinic) {
                    return;
                }

                currentClinic = clinic;
                displayClinicDetails(clinic);

                // Load statistics
                loadStatistics(clinicId);

            // } catch (error) {
            //     console.error('Error loading clinic:', error);
            //     showMessage('Error al cargar datos de la clínica: ' + error.message, 'error');
            //     setTimeout(() => goBack(), 3000);
            // }
        }

        /**
         * Display clinic details
         */
        function displayClinicDetails(clinic) {
            document.getElementById('pageTitle').textContent = clinic.clinicName || 'Sin nombre';
            document.getElementById('clinicId').textContent = clinic.clinicId || 'N/A';
            document.getElementById('clinicName').textContent = clinic.clinicName || 'Sin nombre';
            document.getElementById('address').textContent = clinic.address || 'No especificada';
            document.getElementById('city').textContent = clinic.city || 'No especificada';
            document.getElementById('phoneNumber').textContent = clinic.phoneNumber || 'No especificado';
            document.getElementById('email').textContent = clinic.email || 'No especificado';
            document.getElementById('peripheralNodeUrl').textContent = clinic.peripheralNodeUrl || 'No especificada';
            document.getElementById('apiKey').textContent = clinic.apiKey || 'No generada';
            document.getElementById('createdAt').textContent = formatDate(clinic.createdAt);
            document.getElementById('updatedAt').textContent = formatDate(clinic.onboardedAt);

            // Status badge
            document.getElementById('statusBadge').innerHTML = renderStatusBadge(clinic.status);

            // Show/hide action buttons based on status
            if (clinic.status === 'PENDING_ONBOARDING') {
                document.getElementById('onboardBtn').style.display = 'block';
            }
            if (clinic.status === 'ACTIVE') {
                document.getElementById('deactivateBtn').style.display = 'block';
            }

            // Hide loading, show content
            document.getElementById('loadingIndicator').style.display = 'none';
            document.getElementById('clinicContent').style.display = 'block';
        }

        /**
         * Load clinic statistics
         */
        async function loadStatistics(clinicId) {
            try {
                const stats = await apiCall(`/admin/clinics/` + clinicId + `/statistics`);

                if (stats) {
                    document.getElementById('statUsers').textContent = stats.totalUsers || 0;
                    document.getElementById('statDocuments').textContent = stats.totalDocuments || 0;
                }
            } catch (error) {
                console.error('Error loading statistics:', error);
                document.getElementById('statUsers').textContent = 'Error';
                document.getElementById('statDocuments').textContent = 'Error';
            }
        }

        /**
         * Render status badge
         */
        function renderStatusBadge(status) {
            const statusMap = {
                'ACTIVE': { class: 'badge-active', text: 'Activa' },
                'INACTIVE': { class: 'badge-inactive', text: 'Inactiva' },
                'PENDING_ONBOARDING': { class: 'badge-pending', text: 'Pendiente de Onboarding' }
            };

            const config = statusMap[status] || { class: 'badge-pending', text: status };
            return `<span class="badge ${config.class}">${config.text}</span>`;
        }

        /**
         * Format date
         */
        function formatDate(dateInput) {
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

            return date.toLocaleString('es-UY', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        /**
         * Toggle edit mode
         */
        function toggleEditMode() {
            document.getElementById('viewMode').style.display = 'none';
            document.getElementById('editMode').classList.add('show');

            // Populate edit form (handle null/undefined values)
            document.getElementById('editClinicName').value = currentClinic.clinicName || '';
            document.getElementById('editAddress').value = currentClinic.address || '';
            document.getElementById('editCity').value = currentClinic.city || '';
            document.getElementById('editPhoneNumber').value = currentClinic.phoneNumber || '';
            document.getElementById('editEmail').value = currentClinic.email || '';
            document.getElementById('editPeripheralNodeUrl').value = currentClinic.peripheralNodeUrl || '';
        }

        /**
         * Cancel edit
         */
        function cancelEdit() {
            document.getElementById('editMode').classList.remove('show');
            document.getElementById('viewMode').style.display = 'block';
        }

        /**
         * Save changes
         */
        async function saveChanges(event) {
            event.preventDefault();

            const clinicId = currentClinic.clinicId;

            const updateData = {
                clinicName: document.getElementById('editClinicName').value.trim(),
                address: document.getElementById('editAddress').value.trim(),
                city: document.getElementById('editCity').value.trim(),
                phoneNumber: document.getElementById('editPhoneNumber').value.trim(),
                email: document.getElementById('editEmail').value.trim(),
                peripheralNodeUrl: document.getElementById('editPeripheralNodeUrl').value.trim()
            };

            try {
                const updated = await apiCall(`/admin/clinics/${clinicId}`, {
                    method: 'PUT',
                    body: JSON.stringify(updateData)
                });

                if (updated) {
                    currentClinic = updated;
                    displayClinicDetails(updated);
                    cancelEdit();
                    showMessage('Clínica actualizada exitosamente', 'success');
                }
            } catch (error) {
                console.error('Error updating clinic:', error);
                showMessage('Error al actualizar clínica: ' + error.message, 'error');
            }
        }

        /**
         * Onboard clinic
         */
        async function onboardClinic() {
            if (!confirm('¿Está seguro que desea onboardear esta clínica a su nodo periférico? Se enviará la configuración al nodo y se activará la clínica.')) {
                return;
            }

            const clinicId = currentClinic.clinicId;

            try {
                const result = await apiCall(`/admin/clinics/` + clinicId + `/onboard`, {
                    method: 'POST'
                });


                if (result) {
                    showMessage('Clínica onboardeada exitosamente', 'success');
                    // Reload clinic details
                    loadClinicDetails();
                }
            } catch (error) {
                console.error('Error onboarding clinic:', error);
                showMessage('Error al onboardear clínica: ' + error.message, 'error');
            }
        }

        /**
         * Deactivate clinic
         */
        async function deactivateClinic() {
            if (!confirm('¿Está seguro que desea desactivar esta clínica? Esta acción impedirá que la clínica registre nuevos usuarios o documentos.')) {
                return;
            }

            const clinicId = currentClinic.clinicId;

            try {
                await apiCall(`/admin/clinics/${clinicId}`, {
                    method: 'DELETE'
                });

                showMessage('Clínica desactivada exitosamente', 'success');
                // Reload clinic details
                loadClinicDetails();

            } catch (error) {
                console.error('Error deactivating clinic:', error);
                showMessage('Error al desactivar clínica: ' + error.message, 'error');
            }
        }

        /**
         * Go back to clinics list
         */
        function goBack() {
            window.location.href = '/hcen/admin/clinics.jsp';
        }

        /**
         * Initialize page on load
         */
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Clinic detail page initializing...');

            // Check for token
            if (!getToken()) {
                showMessage('No se encontró sesión. Redirigiendo...', 'error');
                setTimeout(() => {
                    window.location.href = '/hcen/login-admin.jsp';
                }, 2000);
                return;
            }

            // Load clinic details
            loadClinicDetails();
        });
    </script>
</body>
</html>
