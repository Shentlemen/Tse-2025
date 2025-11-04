<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Políticas de Acceso - HCEN</title>
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

        .section {
            background: white;
            padding: 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 2px solid #f0f0f0;
        }

        .section-title {
            font-size: 24px;
            font-weight: 600;
            color: #2c3e50;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .btn-primary {
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

        .btn-primary:hover:not(:disabled) {
            background: linear-gradient(135deg, #5568d3, #6a3f8f);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        }

        .btn-primary:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .loading {
            text-align: center;
            padding: 40px 20px;
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
            text-align: center;
            padding: 60px 30px;
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
        }

        .policies-grid {
            display: grid;
            gap: 20px;
        }

        .policy-card {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 12px;
            border-left: 4px solid #667eea;
            transition: all 0.3s ease;
        }

        .policy-card:hover {
            transform: translateX(5px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
        }

        .policy-header {
            display: flex;
            justify-content: space-between;
            align-items: start;
            margin-bottom: 15px;
        }

        .policy-type {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 5px;
        }

        .policy-effect {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .policy-effect.permit {
            background: rgba(46, 213, 115, 0.2);
            color: #27ae60;
        }

        .policy-effect.deny {
            background: rgba(231, 76, 60, 0.2);
            color: #e74c3c;
        }

        .policy-details {
            color: #7f8c8d;
            font-size: 14px;
            margin-bottom: 15px;
            line-height: 1.6;
        }

        .policy-actions {
            display: flex;
            gap: 10px;
        }

        .btn-edit, .btn-delete {
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .btn-edit {
            background: rgba(52, 152, 219, 0.2);
            color: #3498db;
        }

        .btn-edit:hover {
            background: #3498db;
            color: white;
        }

        .btn-delete {
            background: rgba(231, 76, 60, 0.2);
            color: #e74c3c;
        }

        .btn-delete:hover {
            background: #e74c3c;
            color: white;
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
            max-width: 600px;
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

        .form-group select,
        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            transition: border-color 0.3s;
        }

        .form-group select:focus,
        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #667eea;
        }

        .form-group textarea {
            resize: vertical;
            min-height: 100px;
        }

        .form-hint {
            color: #7f8c8d;
            font-size: 12px;
            margin-top: 5px;
        }

        .modal-footer {
            display: flex;
            gap: 12px;
            margin-top: 24px;
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

        .checkbox-group {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 12px;
        }

        .checkbox-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 10px;
            background: #f8f9fa;
            border-radius: 6px;
            cursor: pointer;
            transition: background 0.3s;
        }

        .checkbox-item:hover {
            background: #e9ecef;
        }

        .checkbox-item input[type="checkbox"] {
            width: auto;
        }

        .info-box {
            background: rgba(52, 152, 219, 0.1);
            border-left: 4px solid #3498db;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }

        .info-box p {
            margin: 0;
            color: #2c3e50;
            font-size: 14px;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">🔒</span>
                    <span>Políticas de Acceso</span>
                </div>
                <div class="breadcrumb">
                    <a href="/hcen/patient/dashboard.jsp">Inicio</a> &gt; Políticas de Acceso
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
        <div class="section">
            <h1 class="page-title">Mis Políticas de Acceso</h1>
            <p class="page-description">
                Configure quién puede acceder a su información clínica. Las políticas le permiten
                controlar el acceso a sus documentos por tipo, especialidad médica, centro de salud, o profesional específico.
            </p>
            <div class="info-box">
                <p>
                    <strong>Información:</strong> Las políticas se evalúan en orden de prioridad. Una política con
                    prioridad más alta prevalece sobre una con prioridad más baja. Si no hay políticas definidas,
                    los profesionales deberán solicitar autorización para acceder a sus documentos.
                </p>
            </div>
        </div>

        <!-- Active Policies Section -->
        <div class="section">
            <div class="section-header">
                <h2 class="section-title">📋 Políticas Activas</h2>
                <button class="btn-primary" onclick="openCreateModal()">+ Nueva Política</button>
            </div>

            <!-- Loading State -->
            <div id="loadingPolicies" class="loading">
                <div class="spinner"></div>
                <p style="margin-top: 20px; color: #7f8c8d;">Cargando políticas...</p>
            </div>

            <!-- Empty State -->
            <div id="emptyPolicies" class="empty-state" style="display: none;">
                <div class="empty-state-icon">📝</div>
                <h3 class="empty-state-title">No hay políticas configuradas</h3>
                <p class="empty-state-description">
                    Cree su primera política para comenzar a controlar el acceso a su información clínica.
                </p>
            </div>

            <!-- Policies Grid -->
            <div id="policiesGrid" class="policies-grid" style="display: none;"></div>
        </div>
    </div>

    <!-- Create/Edit Policy Modal -->
    <div id="policyModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 id="modalTitle">Nueva Política</h2>
                <button class="btn-close" onclick="closeModal()">&times;</button>
            </div>
            <form id="policyForm">
                <input type="hidden" id="policyId">

                <div class="form-group">
                    <label for="policyType">Tipo de Política *</label>
                    <select id="policyType" required onchange="onPolicyTypeChange()">
                        <option value="">Seleccione un tipo...</option>
                    </select>
                    <div class="form-hint" id="policyTypeHint"></div>
                </div>

                <div class="form-group">
                    <label for="policyEffect">Efecto *</label>
                    <select id="policyEffect" required>
                        <option value="PERMIT">Permitir</option>
                        <option value="DENY">Denegar</option>
                    </select>
                    <div class="form-hint">Permitir autoriza el acceso, Denegar lo bloquea</div>
                </div>

                <div class="form-group" id="policyConfigGroup">
                    <label for="policyConfig">Configuración *</label>
                    <div id="configEditor"></div>
                    <textarea id="policyConfig" required style="display: none;"></textarea>
                    <div class="form-hint">Configuración en formato JSON</div>
                </div>

                <div class="form-group">
                    <label for="policyPriority">Prioridad</label>
                    <input type="number" id="policyPriority" min="0" max="100" value="10">
                    <div class="form-hint">Mayor número = mayor prioridad (0-100)</div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn-secondary" onclick="closeModal()">Cancelar</button>
                    <button type="submit" class="btn-primary" id="saveBtn">Guardar Política</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div id="deleteModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Confirmar Eliminación</h2>
                <button class="btn-close" onclick="closeDeleteModal()">&times;</button>
            </div>
            <p style="margin-bottom: 20px; color: #7f8c8d;">
                ¿Está seguro que desea eliminar esta política? Esta acción no se puede deshacer.
            </p>
            <input type="hidden" id="deleteP olicyId">
            <div class="modal-footer">
                <button type="button" class="btn-secondary" onclick="closeDeleteModal()">Cancelar</button>
                <button type="button" class="btn-delete" onclick="confirmDelete()">Eliminar</button>
            </div>
        </div>
    </div>

    <script>
        const API_BASE = '/hcen/api';
        let policyTemplates = [];
        let currentPolicy = null;

        // ================================================================
        // Authentication & Utilities
        // ================================================================

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

        // ================================================================
        // Load Data
        // ================================================================

        async function loadPolicyTemplates() {
            try {
                const response = await apiCall('/policies/templates');

                if (!response || !response.ok) {
                    throw new Error('Failed to load templates');
                }

                policyTemplates = await response.json();
                console.log('Loaded policy templates:', policyTemplates);

                // Populate policy type dropdown
                const select = document.getElementById('policyType');
                select.innerHTML = '<option value="">Seleccione un tipo...</option>';

                policyTemplates.forEach(template => {
                    const option = document.createElement('option');
                    option.value = template.policyType;
                    option.textContent = template.displayName;
                    select.appendChild(option);
                });

            } catch (error) {
                console.error('Error loading templates:', error);
                showError('Error al cargar las plantillas de políticas');
            }
        }

        async function loadPolicies() {
            const patientCi = getPatientCi();

            if (!patientCi) {
                showError('No se pudo identificar al paciente');
                return;
            }

            try {
                const response = await apiCall('/policies/patient/' + encodeURIComponent(patientCi));

                if (!response || !response.ok) {
                    throw new Error('Failed to load policies');
                }

                const data = await response.json();

                document.getElementById('loadingPolicies').style.display = 'none';

                if (!data.policies || data.policies.length === 0) {
                    document.getElementById('emptyPolicies').style.display = 'block';
                    document.getElementById('policiesGrid').style.display = 'none';
                } else {
                    document.getElementById('emptyPolicies').style.display = 'none';
                    displayPolicies(data.policies);
                }

            } catch (error) {
                console.error('Error loading policies:', error);
                document.getElementById('loadingPolicies').style.display = 'none';
                showError('Error al cargar las políticas');
            }
        }

        function displayPolicies(policies) {
            const grid = document.getElementById('policiesGrid');
            grid.innerHTML = '';

            policies.forEach(policy => {
                const card = createPolicyCard(policy);
                grid.appendChild(card);
            });

            grid.style.display = 'grid';
        }

        function createPolicyCard(policy) {
            const card = document.createElement('div');
            card.className = 'policy-card';

            const template = policyTemplates.find(t => t.policyType === policy.policyType);
            const displayName = template ? template.displayName : policy.policyType;

            const effectClass = policy.policyEffect === 'PERMIT' ? 'permit' : 'deny';
            const effectText = policy.policyEffect === 'PERMIT' ? 'Permitir' : 'Denegar';

            let configSummary = '';
            try {
                const config = JSON.parse(policy.policyConfig);
                configSummary = JSON.stringify(config, null, 2).substring(0, 200);
                if (configSummary.length === 200) configSummary += '...';
            } catch (e) {
                configSummary = policy.policyConfig.substring(0, 200);
            }

            card.innerHTML = `
                <div class="policy-header">
                    <div>
                        <div class="policy-type">${'$'}{escapeHtml(displayName)}</div>
                        <div style="font-size: 13px; color: #95a5a6;">Prioridad: ${'$'}{policy.priority}</div>
                    </div>
                    <span class="policy-effect ${'$'}{effectClass}">${'$'}{effectText}</span>
                </div>
                <div class="policy-details">
                    <strong>Configuración:</strong><br>
                    <pre style="margin: 5px 0 0 0; font-size: 12px; color: #555;">${'$'}{escapeHtml(configSummary)}</pre>
                </div>
                <div class="policy-actions">
                    <button class="btn-edit" onclick="editPolicy(${'$'}{policy.id})">✏️ Editar</button>
                    <button class="btn-delete" onclick="openDeleteModal(${'$'}{policy.id})">🗑️ Eliminar</button>
                </div>
            `;

            return card;
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        // ================================================================
        // Modal Management
        // ================================================================

        function openCreateModal() {
            currentPolicy = null;
            document.getElementById('modalTitle').textContent = 'Nueva Política';
            document.getElementById('policyForm').reset();
            document.getElementById('policyId').value = '';
            document.getElementById('policyConfig').value = '';
            document.getElementById('configEditor').innerHTML = '';
            document.getElementById('policyPriority').value = '10';
            document.getElementById('policyModal').classList.add('show');
        }

        async function editPolicy(policyId) {
            const patientCi = getPatientCi();

            try {
                const response = await apiCall('/policies/' + policyId);

                if (!response || !response.ok) {
                    throw new Error('Failed to load policy');
                }

                currentPolicy = await response.json();

                document.getElementById('modalTitle').textContent = 'Editar Política';
                document.getElementById('policyId').value = currentPolicy.id;
                document.getElementById('policyType').value = currentPolicy.policyType;
                document.getElementById('policyEffect').value = currentPolicy.policyEffect;
                document.getElementById('policyConfig').value = currentPolicy.policyConfig;
                document.getElementById('policyPriority').value = currentPolicy.priority || 10;

                // Disable policy type change when editing
                document.getElementById('policyType').disabled = true;

                onPolicyTypeChange();

                document.getElementById('policyModal').classList.add('show');

            } catch (error) {
                console.error('Error loading policy:', error);
                showError('Error al cargar la política');
            }
        }

        function closeModal() {
            document.getElementById('policyModal').classList.remove('show');
            document.getElementById('policyType').disabled = false;
            currentPolicy = null;
        }

        function openDeleteModal(policyId) {
            document.getElementById('deletePolicyId').value = policyId;
            document.getElementById('deleteModal').classList.add('show');
        }

        function closeDeleteModal() {
            document.getElementById('deleteModal').classList.remove('show');
        }

        // ================================================================
        // Policy Configuration Editor
        // ================================================================

        function onPolicyTypeChange() {
            const policyType = document.getElementById('policyType').value;
            const template = policyTemplates.find(t => t.policyType === policyType);

            if (!template) {
                document.getElementById('policyTypeHint').textContent = '';
                document.getElementById('configEditor').innerHTML = '';
                return;
            }

            document.getElementById('policyTypeHint').textContent = template.description;

            // Create simplified config editor based on template
            const configEditor = document.getElementById('configEditor');
            configEditor.innerHTML = '';

            // For now, show example and let user edit JSON directly
            const exampleDiv = document.createElement('div');
            exampleDiv.style.background = '#f8f9fa';
            exampleDiv.style.padding = '10px';
            exampleDiv.style.borderRadius = '6px';
            exampleDiv.style.marginBottom = '10px';
            exampleDiv.innerHTML = `
                <strong>Ejemplo:</strong><br>
                <pre style="margin: 5px 0 0 0; font-size: 12px;">${'$'}{template.exampleConfiguration}</pre>
            `;
            configEditor.appendChild(exampleDiv);

            // Show textarea for JSON editing
            const textarea = document.getElementById('policyConfig');
            textarea.style.display = 'block';

            if (!currentPolicy) {
                textarea.value = template.exampleConfiguration;
            }
        }

        // ================================================================
        // Form Submission
        // ================================================================

        document.getElementById('policyForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const patientCi = getPatientCi();
            const policyId = document.getElementById('policyId').value;
            const isEdit = !!policyId;

            const payload = {
                patientCi: patientCi,
                policyType: document.getElementById('policyType').value,
                policyEffect: document.getElementById('policyEffect').value,
                policyConfig: document.getElementById('policyConfig').value,
                priority: parseInt(document.getElementById('policyPriority').value)
            };

            // Validate JSON
            try {
                JSON.parse(payload.policyConfig);
            } catch (e) {
                showError('La configuración debe ser un JSON válido');
                return;
            }

            const btn = document.getElementById('saveBtn');
            btn.disabled = true;
            btn.textContent = 'Guardando...';

            try {
                let response;

                if (isEdit) {
                    // Update existing policy
                    response = await apiCall('/policies/' + policyId, {
                        method: 'PUT',
                        body: JSON.stringify({
                            policyConfig: payload.policyConfig,
                            policyEffect: payload.policyEffect,
                            priority: payload.priority
                        })
                    });
                } else {
                    // Create new policy
                    response = await apiCall('/policies', {
                        method: 'POST',
                        body: JSON.stringify(payload)
                    });
                }

                if (response && response.ok) {
                    closeModal();
                    showSuccess(isEdit ? 'Política actualizada correctamente' : 'Política creada correctamente');
                    setTimeout(() => loadPolicies(), 1000);
                } else {
                    const error = await response.json();
                    showError(error.message || 'Error al guardar la política');
                }
            } catch (error) {
                console.error('Error saving policy:', error);
                showError('Error al guardar la política');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Guardar Política';
            }
        });

        async function confirmDelete() {
            const policyId = document.getElementById('deletePolicyId').value;

            try {
                const response = await apiCall('/policies/' + policyId, {
                    method: 'DELETE'
                });

                if (response && response.ok) {
                    closeDeleteModal();
                    showSuccess('Política eliminada correctamente');
                    setTimeout(() => loadPolicies(), 1000);
                } else {
                    showError('Error al eliminar la política');
                }
            } catch (error) {
                console.error('Error deleting policy:', error);
                showError('Error al eliminar la política');
            }
        }

        // ================================================================
        // Navigation
        // ================================================================

        function goBack() {
            window.location.href = '/hcen/patient/dashboard.jsp';
        }

        function logout() {
            sessionStorage.removeItem('accessToken');
            window.location.href = '/hcen/login-patient.jsp';
        }

        // ================================================================
        // Initialize
        // ================================================================

        window.addEventListener('DOMContentLoaded', async function() {
            console.log('Loading access policies page...');
            await loadPolicyTemplates();
            await loadPolicies();
        });
    </script>
</body>
</html>
