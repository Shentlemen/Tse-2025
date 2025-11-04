<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Solicitudes de Acceso Pendientes - HCEN</title>
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
            max-width: 1200px;
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

        .alert-success {
            background: rgba(46, 213, 115, 0.1);
            border: 1px solid rgba(46, 213, 115, 0.3);
            color: #27ae60;
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

        .requests-grid {
            display: grid;
            gap: 20px;
        }

        .request-card {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
            transition: all 0.3s ease;
            border-left: 4px solid #667eea;
        }

        .request-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 35px rgba(102, 126, 234, 0.2);
        }

        .request-header {
            display: flex;
            justify-content: space-between;
            align-items: start;
            margin-bottom: 20px;
        }

        .professional-info {
            flex: 1;
        }

        .professional-name {
            font-size: 20px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        .clinic-name {
            font-size: 14px;
            color: #7f8c8d;
            margin-bottom: 4px;
        }

        .request-time {
            font-size: 13px;
            color: #95a5a6;
        }

        .status-badge {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .status-pending {
            background: rgba(241, 196, 15, 0.2);
            color: #f39c12;
        }

        .request-details {
            margin-bottom: 25px;
        }

        .detail-row {
            display: flex;
            align-items: center;
            margin-bottom: 12px;
            font-size: 14px;
        }

        .detail-label {
            font-weight: 600;
            color: #7f8c8d;
            min-width: 130px;
        }

        .detail-value {
            color: #2c3e50;
        }

        .request-reason {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-top: 15px;
            font-size: 14px;
            color: #2c3e50;
            line-height: 1.6;
        }

        .request-actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .btn {
            flex: 1;
            min-width: 120px;
            padding: 12px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-align: center;
        }

        .btn-approve {
            background: linear-gradient(135deg, #2ecc71, #27ae60);
            color: white;
            box-shadow: 0 4px 15px rgba(46, 204, 113, 0.3);
        }

        .btn-approve:hover {
            background: linear-gradient(135deg, #27ae60, #229954);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(46, 204, 113, 0.4);
        }

        .btn-deny {
            background: linear-gradient(135deg, #e74c3c, #c0392b);
            color: white;
            box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);
        }

        .btn-deny:hover {
            background: linear-gradient(135deg, #c0392b, #a93226);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(231, 76, 60, 0.4);
        }

        .btn-info {
            background: #f8f9fa;
            color: #667eea;
            border: 2px solid #667eea;
        }

        .btn-info:hover {
            background: #667eea;
            color: white;
        }

        .btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none !important;
        }

        /* Modal Styles */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.5);
            z-index: 1000;
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
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
        }

        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
        }

        .modal-header h2 {
            font-size: 24px;
            color: #2c3e50;
        }

        .btn-close {
            background: none;
            border: none;
            font-size: 28px;
            color: #7f8c8d;
            cursor: pointer;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            transition: background 0.3s;
        }

        .btn-close:hover {
            background: #f0f0f0;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #2c3e50;
            font-weight: 600;
            font-size: 14px;
        }

        .form-group textarea {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            resize: vertical;
            min-height: 120px;
            transition: border-color 0.3s;
        }

        .form-group textarea:focus {
            outline: none;
            border-color: #667eea;
        }

        .modal-footer {
            display: flex;
            gap: 12px;
            margin-top: 24px;
        }

        .btn-primary {
            flex: 1;
            background: #667eea;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary:hover:not(:disabled) {
            background: #5568d3;
        }

        .btn-secondary {
            flex: 1;
            background: #f8f9fa;
            color: #666;
            padding: 12px 24px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-secondary:hover {
            background: #e1e8ed;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">✅</span>
                    <span>Solicitudes de Acceso</span>
                </div>
                <div class="breadcrumb">
                    <a href="/hcen/patient/dashboard.jsp">Inicio</a> &gt; Solicitudes Pendientes
                </div>
            </div>
            <div>
                <button class="back-btn" onclick="goBack()">← Volver</button>
                <button class="logout-btn" onclick="logout()">🚪 Cerrar Sesión</button>
            </div>
        </div>

        <!-- Alerts -->
        <div id="alertSuccess" class="alert alert-success"></div>
        <div id="alertError" class="alert alert-error"></div>
        <div id="alertInfo" class="alert alert-info"></div>

        <!-- Page Title -->
        <div style="background: white; padding: 30px; border-radius: 15px; margin-bottom: 30px; box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);">
            <h1 class="page-title">Solicitudes de Acceso Pendientes</h1>
            <p class="page-description">
                Aquí puede revisar y gestionar las solicitudes de profesionales de la salud que desean acceder a sus documentos clínicos.
            </p>
        </div>

        <!-- Loading State -->
        <div id="loadingState" class="loading">
            <div class="spinner"></div>
            <p style="margin-top: 20px; color: #7f8c8d;">Cargando solicitudes...</p>
        </div>

        <!-- Empty State -->
        <div id="emptyState" class="empty-state" style="display: none;">
            <div class="empty-state-icon">✨</div>
            <h2 class="empty-state-title">No tienes solicitudes pendientes</h2>
            <p class="empty-state-description">
                Cuando un profesional de la salud solicite acceso a tus documentos, las solicitudes aparecerán aquí.
            </p>
            <button class="back-btn" onclick="goBack()">Volver al Panel</button>
        </div>

        <!-- Requests Grid -->
        <div id="requestsGrid" class="requests-grid" style="display: none;"></div>
    </div>

    <!-- Approve Modal -->
    <div id="approveModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Aprobar Solicitud</h2>
                <button class="btn-close" onclick="closeModal('approveModal')">&times;</button>
            </div>
            <p style="margin-bottom: 20px; color: #7f8c8d;">
                Está a punto de aprobar el acceso del profesional a sus documentos clínicos.
                Opcionalmente puede agregar un comentario.
            </p>
            <form id="approveForm">
                <input type="hidden" id="approveRequestId">
                <div class="form-group">
                    <label>Comentario (opcional)</label>
                    <textarea id="approveReason" placeholder="Ej: Aprobado para seguimiento de tratamiento..."></textarea>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary" onclick="closeModal('approveModal')">Cancelar</button>
                    <button type="submit" class="btn-primary" id="approveBtn">Aprobar Acceso</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Deny Modal -->
    <div id="denyModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Denegar Solicitud</h2>
                <button class="btn-close" onclick="closeModal('denyModal')">&times;</button>
            </div>
            <p style="margin-bottom: 20px; color: #7f8c8d;">
                Por favor indique el motivo por el cual deniega el acceso. Esto ayudará al profesional a comprender su decisión.
            </p>
            <form id="denyForm">
                <input type="hidden" id="denyRequestId">
                <div class="form-group">
                    <label>Motivo de denegación (requerido) *</label>
                    <textarea id="denyReason" required placeholder="Ej: No conozco a este profesional, Solicitud no corresponde a mi tratamiento actual..."></textarea>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary" onclick="closeModal('denyModal')">Cancelar</button>
                    <button type="submit" class="btn-primary" id="denyBtn">Denegar Acceso</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Info Request Modal -->
    <div id="infoModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Solicitar Más Información</h2>
                <button class="btn-close" onclick="closeModal('infoModal')">&times;</button>
            </div>
            <p style="margin-bottom: 20px; color: #7f8c8d;">
                Puede solicitar información adicional al profesional antes de tomar una decisión.
            </p>
            <form id="infoForm">
                <input type="hidden" id="infoRequestId">
                <div class="form-group">
                    <label>Su pregunta (requerido) *</label>
                    <textarea id="infoQuestion" required placeholder="Ej: ¿Por qué necesita acceder a mis resultados de laboratorio?"></textarea>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn-secondary" onclick="closeModal('infoModal')">Cancelar</button>
                    <button type="submit" class="btn-primary" id="infoBtn">Enviar Pregunta</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        const API_BASE = '/hcen/api';

        // Get patient CI from JWT token
        function getPatientCi() {
            const token = sessionStorage.getItem('accessToken');
            if (!token) return null;

            try {
                const payload = parseJwt(token);
                return payload.sub || payload.ci;
            } catch (e) {
                console.error('Error parsing token:', e);
                return null;
            }
        }

        // Parse JWT token
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

        // Make authenticated API call
        async function apiCall(endpoint, options = {}) {
            const token = sessionStorage.getItem('accessToken');

            if (!token) {
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
                    sessionStorage.removeItem('accessToken');
                    showError('Sesión expirada. Redirigiendo...');
                    setTimeout(() => {
                        window.location.href = '/hcen/login-patient.jsp';
                    }, 2000);
                    return null;
                }

                return response;
            } catch (error) {
                console.error('API call error:', error);
                return null;
            }
        }

        // Load pending requests
        async function loadPendingRequests() {
            const patientCi = getPatientCi();

            if (!patientCi) {
                showError('No se pudo identificar al paciente. Redirigiendo...');
                setTimeout(() => {
                    window.location.href = '/hcen/login-patient.jsp';
                }, 2000);
                return;
            }

            try {
                const response = await apiCall('/access-requests?patientCi=' + encodeURIComponent(patientCi) + '&status=PENDING');

                if (!response || !response.ok) {
                    throw new Error('Failed to load requests');
                }

                const data = await response.json();

                document.getElementById('loadingState').style.display = 'none';

                if (!data.requests || data.requests.length === 0) {
                    document.getElementById('emptyState').style.display = 'block';
                } else {
                    displayRequests(data.requests);
                }

            } catch (error) {
                console.error('Error loading requests:', error);
                document.getElementById('loadingState').style.display = 'none';
                showError('Error al cargar las solicitudes. Por favor, intente nuevamente.');
            }
        }

        // Display requests
        function displayRequests(requests) {
            const grid = document.getElementById('requestsGrid');
            grid.innerHTML = '';

            requests.forEach(request => {
                const card = createRequestCard(request);
                grid.appendChild(card);
            });

            grid.style.display = 'grid';
        }

        // Create request card
        function createRequestCard(request) {
            const card = document.createElement('div');
            card.className = 'request-card';
            card.innerHTML = `
                <div class="request-header">
                    <div class="professional-info">
                        <div class="professional-name">${'$'}{escapeHtml(request.professionalName || 'Profesional Desconocido')}</div>
                        <div class="clinic-name">🏥 ${'$'}{escapeHtml(request.clinicName || 'Clínica No Especificada')}</div>
                        <div class="request-time">⏰ Solicitado: ${'$'}{formatDateTime(request.requestedAt)}</div>
                    </div>
                    <span class="status-badge status-pending">Pendiente</span>
                </div>
                <div class="request-details">
                    <div class="detail-row">
                        <span class="detail-label">Tipo de Documento:</span>
                        <span class="detail-value">${'$'}{escapeHtml(request.documentType || 'Acceso General')}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Expira:</span>
                        <span class="detail-value">${'$'}{formatDateTime(request.expiresAt)}</span>
                    </div>
                    ${'$'}{request.requestReason ? `
                        <div class="request-reason">
                            <strong>Motivo de la solicitud:</strong><br>
                            ${'$'}{escapeHtml(request.requestReason)}
                        </div>
                    ` : ''}
                </div>
                <div class="request-actions">
                    <button class="btn btn-approve" onclick="openApproveModal(${'$'}{request.id})">
                        ✓ Aprobar
                    </button>
                    <button class="btn btn-deny" onclick="openDenyModal(${'$'}{request.id})">
                        ✗ Denegar
                    </button>
                    <button class="btn btn-info" onclick="openInfoModal(${'$'}{request.id})">
                        ? Más Info
                    </button>
                </div>
            `;
            return card;
        }

        // Format date time
        function formatDateTime(dateStr) {
            if (!dateStr) return 'N/A';
            const date = new Date(dateStr);
            return date.toLocaleString('es-UY', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        // Escape HTML
        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        // Show alerts
        function showSuccess(message) {
            const alert = document.getElementById('alertSuccess');
            alert.textContent = message;
            alert.classList.add('show');
            setTimeout(() => alert.classList.remove('show'), 5000);
        }

        function showError(message) {
            const alert = document.getElementById('alertError');
            alert.textContent = message;
            alert.classList.add('show');
            setTimeout(() => alert.classList.remove('show'), 5000);
        }

        function showInfo(message) {
            const alert = document.getElementById('alertInfo');
            alert.textContent = message;
            alert.classList.add('show');
            setTimeout(() => alert.classList.remove('show'), 5000);
        }

        // Modal functions
        function openApproveModal(requestId) {
            document.getElementById('approveRequestId').value = requestId;
            document.getElementById('approveReason').value = '';
            document.getElementById('approveModal').classList.add('show');
        }

        function openDenyModal(requestId) {
            document.getElementById('denyRequestId').value = requestId;
            document.getElementById('denyReason').value = '';
            document.getElementById('denyModal').classList.add('show');
        }

        function openInfoModal(requestId) {
            document.getElementById('infoRequestId').value = requestId;
            document.getElementById('infoQuestion').value = '';
            document.getElementById('infoModal').classList.add('show');
        }

        function closeModal(modalId) {
            document.getElementById(modalId).classList.remove('show');
        }

        // Form handlers
        document.getElementById('approveForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const requestId = document.getElementById('approveRequestId').value;
            const reason = document.getElementById('approveReason').value;

            const btn = document.getElementById('approveBtn');
            btn.disabled = true;
            btn.textContent = 'Procesando...';

            try {
                const response = await apiCall('/access-requests/' + requestId + '/approve', {
                    method: 'POST',
                    body: JSON.stringify({ reason: reason || null })
                });

                if (response && response.ok) {
                    closeModal('approveModal');
                    showSuccess('Solicitud aprobada correctamente');
                    setTimeout(() => loadPendingRequests(), 1500);
                } else {
                    const error = await response.json();
                    showError(error.message || 'Error al aprobar la solicitud');
                }
            } catch (error) {
                console.error('Error approving request:', error);
                showError('Error al aprobar la solicitud');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Aprobar Acceso';
            }
        });

        document.getElementById('denyForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const requestId = document.getElementById('denyRequestId').value;
            const reason = document.getElementById('denyReason').value;

            if (reason.length < 10) {
                showError('El motivo debe tener al menos 10 caracteres');
                return;
            }

            const btn = document.getElementById('denyBtn');
            btn.disabled = true;
            btn.textContent = 'Procesando...';

            try {
                const response = await apiCall('/access-requests/' + requestId + '/deny', {
                    method: 'POST',
                    body: JSON.stringify({ reason: reason })
                });

                if (response && response.ok) {
                    closeModal('denyModal');
                    showSuccess('Solicitud denegada correctamente');
                    setTimeout(() => loadPendingRequests(), 1500);
                } else {
                    const error = await response.json();
                    showError(error.message || 'Error al denegar la solicitud');
                }
            } catch (error) {
                console.error('Error denying request:', error);
                showError('Error al denegar la solicitud');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Denegar Acceso';
            }
        });

        document.getElementById('infoForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const requestId = document.getElementById('infoRequestId').value;
            const question = document.getElementById('infoQuestion').value;

            if (question.length < 10) {
                showError('La pregunta debe tener al menos 10 caracteres');
                return;
            }

            const btn = document.getElementById('infoBtn');
            btn.disabled = true;
            btn.textContent = 'Enviando...';

            try {
                const response = await apiCall('/access-requests/' + requestId + '/request-info', {
                    method: 'POST',
                    body: JSON.stringify({ question: question })
                });

                if (response && response.ok) {
                    closeModal('infoModal');
                    showInfo('Su pregunta ha sido enviada al profesional');
                } else {
                    const error = await response.json();
                    showError(error.message || 'Error al enviar la pregunta');
                }
            } catch (error) {
                console.error('Error requesting info:', error);
                showError('Error al enviar la pregunta');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Enviar Pregunta';
            }
        });

        // Navigation
        function goBack() {
            window.location.href = '/hcen/patient/dashboard.jsp';
        }

        function logout() {
            sessionStorage.removeItem('accessToken');
            window.location.href = '/hcen/login-patient.jsp';
        }

        // Initialize
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Loading pending access requests...');
            loadPendingRequests();
        });
    </script>
</body>
</html>
