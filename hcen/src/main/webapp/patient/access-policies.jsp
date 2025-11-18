<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Politicas de Acceso - HCEN</title>
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
            display: flex;
            justify-content: center;
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
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
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

        .policy-info {
            flex: 1;
        }

        .policy-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 5px;
        }

        .policy-subtitle {
            font-size: 14px;
            color: #7f8c8d;
        }

        .policy-status {
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .policy-status.granted {
            background: rgba(46, 213, 115, 0.2);
            color: #27ae60;
        }

        .policy-status.pending {
            background: rgba(241, 196, 15, 0.2);
            color: #f39c12;
        }

        .policy-status.revoked {
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

        .btn-revoke, .btn-delete {
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .btn-revoke {
            background: rgba(241, 196, 15, 0.2);
            color: #f39c12;
        }

        .btn-revoke:hover {
            background: #f39c12;
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

        .form-group select,
        .form-group input {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            transition: border-color 0.3s;
        }

        .form-group select:focus,
        .form-group input:focus {
            outline: none;
            border-color: #667eea;
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

        .filters-container {
            display: flex;
            gap: 15px;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }

        .filter-group {
            flex: 1;
            min-width: 200px;
        }

        .filter-group label {
            display: block;
            margin-bottom: 8px;
            color: #2c3e50;
            font-weight: 600;
            font-size: 14px;
        }

        .filter-group select {
            width: 100%;
            padding: 10px 14px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            font-family: inherit;
            background: white;
            transition: border-color 0.3s;
            cursor: pointer;
        }

        .filter-group select:focus {
            outline: none;
            border-color: #667eea;
        }

        .policy-clinic {
            font-size: 16px;
            font-weight: 500;
            color: #667eea;
            margin-bottom: 8px;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">🔐</span>
                    <span>Políticas de Acceso</span>
                </div>
                <div class="breadcrumb">
                    <a href="/hcen/patient/dashboard.jsp">Inicio</a> &gt; Políticas de Acceso
                </div>
            </div>
            <button class="back-btn" onclick="goBack()">← Volver al Panel</button>
            <button class="logout-btn" onclick="logout()">🚪 Cerrar Sesión</button>
        </div>

        <!-- Alerts -->
        <div id="alertSuccess" class="alert alert-success"></div>
        <div id="alertError" class="alert alert-error"></div>
        <div id="alertInfo" class="alert alert-info"></div>

        <!-- Page Title -->
        <div class="section">
            <h1 class="page-title">Mis Politicas de Acceso</h1>
            <p class="page-description">
                Configure quien puede acceder a su informacion clinica. Las politicas le permiten
                otorgar acceso a profesionales de salud segun su especialidad y centro de salud.
            </p>
            <div class="info-box">
                <p>
                    <strong>Informacion:</strong> Cuando crea una politica, otorga acceso a todos los profesionales
                    con la especialidad indicada del centro de salud seleccionado. Si no hay politicas definidas,
                    los profesionales deberan solicitar autorizacion para acceder a sus documentos.
                </p>
            </div>
        </div>

        <!-- Active Policies Section -->
        <div class="section">
            <div class="section-header">
                <h2 class="section-title">Politicas Activas</h2>
                <button class="btn-primary" onclick="openCreateModal()">+ Nueva Politica</button>
            </div>

            <!-- Filters -->
            <div class="filters-container" id="filtersContainer" style="display: none;">
                <div class="filter-group">
                    <label for="filterClinic">Centro de Salud</label>
                    <select id="filterClinic" onchange="applyFilters()">
                        <option value="">Todos los centros</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="filterSpecialty">Especialidad</label>
                    <select id="filterSpecialty" onchange="applyFilters()">
                        <option value="">Todas las especialidades</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label for="filterStatus">Estado</label>
                    <select id="filterStatus" onchange="applyFilters()">
                        <option value="">Todos los estados</option>
                        <option value="GRANTED">Otorgado</option>
                        <option value="PENDING">Pendiente</option>
                        <option value="REVOKED">Revocado</option>
                    </select>
                </div>
            </div>

            <!-- Loading State -->
            <div id="loadingPolicies" class="loading">
                <div class="spinner"></div>
                <p style="margin-top: 20px; color: #7f8c8d;">Cargando politicas...</p>
            </div>

            <!-- Empty State -->
            <div id="emptyPolicies" class="empty-state" style="display: none;">
                <h3 class="empty-state-title">No hay politicas configuradas</h3>
                <p class="empty-state-description">
                    Cree su primera politica para comenzar a controlar el acceso a su informacion clinica.
                </p>
            </div>

            <!-- Policies Grid -->
            <div id="policiesGrid" class="policies-grid" style="display: none;"></div>
        </div>
    </div>

    <!-- Create Policy Modal -->
    <div id="policyModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Nueva Politica</h2>
                <button class="btn-close" onclick="closeModal()">&times;</button>
            </div>
            <form id="policyForm">
                <div class="form-group">
                    <label for="clinicId">Centro de Salud *</label>
                    <select id="clinicId" required>
                        <option value="">Seleccione un centro...</option>
                    </select>
                    <div class="form-hint">Seleccione el centro de salud cuyos profesionales podran acceder a sus documentos</div>
                </div>

                <div class="form-group">
                    <label for="specialty">Especialidad Medica *</label>
                    <select id="specialty" required>
                        <option value="">Seleccione una especialidad...</option>
                    </select>
                    <div class="form-hint">Solo los profesionales con esta especialidad tendran acceso</div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn-secondary" onclick="closeModal()">Cancelar</button>
                    <button type="submit" class="btn-primary" id="saveBtn">Crear Politica</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div id="deleteModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Confirmar Eliminacion</h2>
                <button class="btn-close" onclick="closeDeleteModal()">&times;</button>
            </div>
            <p style="margin-bottom: 20px; color: #7f8c8d;">
                Esta seguro que desea eliminar esta politica? Esta accion no se puede deshacer.
            </p>
            <input type="hidden" id="deletePolicyId">
            <div class="modal-footer">
                <button type="button" class="btn-secondary" onclick="closeDeleteModal()">Cancelar</button>
                <button type="button" class="btn-delete" onclick="confirmDelete()">Eliminar</button>
            </div>
        </div>
    </div>

    <script>
        const API_BASE = '/hcen/api';
        let clinics = [];
        let specialties = [];
        let allPolicies = [];

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
                showError('Sesion expirada. Por favor, inicie sesion nuevamente.');
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
                    showError('Sesion expirada. Redirigiendo...');
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

        async function loadClinics() {
            try {
                const response = await apiCall('/policies/clinics');

                if (!response || !response.ok) {
                    throw new Error('Failed to load clinics');
                }

                clinics = await response.json();
                console.log('Loaded clinics:', clinics);

                const select = document.getElementById('clinicId');
                select.innerHTML = '<option value="">Seleccione un centro...</option>';

                clinics.forEach(clinic => {
                    const option = document.createElement('option');
                    option.value = clinic.value;
                    option.textContent = clinic.label;
                    select.appendChild(option);
                });

            } catch (error) {
                console.error('Error loading clinics:', error);
                showError('Error al cargar los centros de salud');
            }
        }

        async function loadSpecialties() {
            try {
                const response = await apiCall('/policies/specialties');

                if (!response || !response.ok) {
                    throw new Error('Failed to load specialties');
                }

                specialties = await response.json();
                console.log('Loaded specialties:', specialties);

                const select = document.getElementById('specialty');
                select.innerHTML = '<option value="">Seleccione una especialidad...</option>';

                specialties.forEach(specialty => {
                    const option = document.createElement('option');
                    option.value = specialty.value;
                    option.textContent = specialty.label;
                    select.appendChild(option);
                });

            } catch (error) {
                console.error('Error loading specialties:', error);
                showError('Error al cargar las especialidades');
            }
        }

        async function loadPolicies() {
            const patientCi = getPatientCi();

            if (!patientCi) {
                showError('No se pudo identificar al paciente');
                document.getElementById('loadingPolicies').style.display = 'none';
                return;
            }

            try {
                const response = await apiCall('/policies?patientCi=' + patientCi);

                if (!response) {
                    throw new Error('Failed to load policies - no response');
                }

                if (!response.ok) {
                    const errorText = await response.text();
                    console.error('API error:', response.status, errorText);
                    throw new Error('Failed to load policies: ' + response.status);
                }

                const data = await response.json();
                console.log('Policies response:', data);

                document.getElementById('loadingPolicies').style.display = 'none';

                // Handle both array and object with policies property
                allPolicies = Array.isArray(data) ? data : (data.policies || []);

                if (allPolicies.length === 0) {
                    document.getElementById('emptyPolicies').style.display = 'block';
                    document.getElementById('policiesGrid').style.display = 'none';
                    document.getElementById('filtersContainer').style.display = 'none';
                } else {
                    document.getElementById('emptyPolicies').style.display = 'none';
                    populateFilterDropdowns();
                    displayPolicies(allPolicies);
                }

            } catch (error) {
                console.error('Error loading policies:', error);
                document.getElementById('loadingPolicies').style.display = 'none';
                showError('Error al cargar las politicas: ' + error.message);
            }
        }

        function populateFilterDropdowns() {
            // Show filters
            document.getElementById('filtersContainer').style.display = 'flex';

            // Populate clinic filter
            const clinicFilter = document.getElementById('filterClinic');
            clinicFilter.innerHTML = '<option value="">Todos los centros</option>';
            const uniqueClinicIds = [...new Set(allPolicies.map(p => p.clinicId))];
            uniqueClinicIds.forEach(clinicId => {
                const clinic = clinics.find(c => c.value === clinicId);
                if (clinic) {
                    const option = document.createElement('option');
                    option.value = clinicId;
                    option.textContent = clinic.label;
                    clinicFilter.appendChild(option);
                }
            });

            // Populate specialty filter
            const specialtyFilter = document.getElementById('filterSpecialty');
            specialtyFilter.innerHTML = '<option value="">Todas las especialidades</option>';
            const uniqueSpecialties = [...new Set(allPolicies.map(p => p.specialty))];
            uniqueSpecialties.forEach(specialty => {
                const spec = specialties.find(s => s.value === specialty);
                if (spec) {
                    const option = document.createElement('option');
                    option.value = specialty;
                    option.textContent = spec.label;
                    specialtyFilter.appendChild(option);
                }
            });
        }

        function applyFilters() {
            const clinicFilter = document.getElementById('filterClinic').value;
            const specialtyFilter = document.getElementById('filterSpecialty').value;
            const statusFilter = document.getElementById('filterStatus').value;

            const filtered = allPolicies.filter(policy => {
                const matchesClinic = !clinicFilter || policy.clinicId === clinicFilter;
                const matchesSpecialty = !specialtyFilter || policy.specialty === specialtyFilter;
                const matchesStatus = !statusFilter || policy.status === statusFilter;
                return matchesClinic && matchesSpecialty && matchesStatus;
            });

            displayPolicies(filtered);
        }

        function displayPolicies(policies) {
            const grid = document.getElementById('policiesGrid');
            const emptyGrid = document.getElementById('emptyPolicies');
            grid.innerHTML = '';

            if (policies.length === 0) {
                emptyGrid.style.display = 'block';
                emptyGrid.innerHTML = '<div class="empty-state"><p class="empty-state-description">No se encontraron politicas con los filtros seleccionados.</p></div>';
            } else {
                emptyGrid.style.display = 'none';
                policies.forEach(policy => {
                    const card = createPolicyCard(policy);
                    grid.appendChild(card);
                });
            }

            grid.style.display = 'grid';
        }

        function createPolicyCard(policy) {
            const card = document.createElement('div');
            card.className = 'policy-card';

            // Get specialty name from specialties array
            const specialtyObj = specialties.find(s => s.value === policy.specialty);
            const specialtyName = specialtyObj ? specialtyObj.label : policy.specialty;

            // Get clinic name from clinics array
            const clinicObj = clinics.find(c => c.value === policy.clinicId);
            const clinicName = clinicObj ? clinicObj.label : policy.clinicId;

            const statusClass = policy.status.toLowerCase();

            // Map status to Spanish
            const statusMap = {
                'GRANTED': 'Otorgado',
                'PENDING': 'Pendiente',
                'REVOKED': 'Revocado'
            };
            const statusName = statusMap[policy.status] || policy.status;

            const scopeText = policy.documentId
                ? 'Documento especifico: ' + policy.documentId
                : 'Todos los documentos';

            card.innerHTML = `
                <div class="policy-header">
                    <div class="policy-info">
                        <div class="policy-clinic">\${escapeHtml(clinicName)}</div>
                        <div class="policy-title">\${escapeHtml(specialtyName)}</div>
                    </div>
                    <span class="policy-status \${statusClass}">\${escapeHtml(statusName)}</span>
                </div>
                <div class="policy-details">
                    <strong>Alcance:</strong> \${escapeHtml(scopeText)}<br>
                    <strong>Creado:</strong> \${formatDate(policy.createdAt)}
                </div>
                <div class="policy-actions">
                    \${policy.status === 'GRANTED' ?
                        '<button class="btn-revoke" onclick="revokePolicy(' + policy.id + ')">Revocar</button>' : ''}
                    <button class="btn-delete" onclick="openDeleteModal(' + policy.id + ')">Eliminar</button>
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

        function formatDate(dateValue) {
            if (!dateValue) return 'N/A';

            try {
                let date;

                // Check if it's an array (LocalDateTime from backend: [year, month, day, hour, minute, second, nanosecond])
                if (Array.isArray(dateValue) && dateValue.length >= 3) {
                    // Create date from array: [year, month (1-based), day, hour, minute, second]
                    const [year, month, day, hour = 0, minute = 0, second = 0] = dateValue;
                    date = new Date(year, month - 1, day, hour, minute, second);
                } else if (typeof dateValue === 'string') {
                    // Handle ISO string format
                    date = new Date(dateValue);
                } else if (typeof dateValue === 'number') {
                    // Handle timestamp
                    date = new Date(dateValue);
                } else {
                    return 'N/A';
                }

                // Check if date is valid
                if (isNaN(date.getTime())) {
                    return 'N/A';
                }

                return date.toLocaleDateString('es-UY', {
                    year: 'numeric',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            } catch (error) {
                console.error('Error formatting date:', error, dateValue);
                return 'N/A';
            }
        }

        // ================================================================
        // Modal Management
        // ================================================================

        function openCreateModal() {
            document.getElementById('policyForm').reset();
            document.getElementById('policyModal').classList.add('show');
        }

        function closeModal() {
            document.getElementById('policyModal').classList.remove('show');
        }

        function openDeleteModal(policyId) {
            document.getElementById('deletePolicyId').value = policyId;
            document.getElementById('deleteModal').classList.add('show');
        }

        function closeDeleteModal() {
            document.getElementById('deleteModal').classList.remove('show');
        }

        // ================================================================
        // Form Submission
        // ================================================================

        document.getElementById('policyForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const patientCi = getPatientCi();
            if (!patientCi) {
                showError('No se pudo identificar al paciente');
                return;
            }

            const payload = {
                patientCi: patientCi,
                clinicId: document.getElementById('clinicId').value,
                specialty: document.getElementById('specialty').value
            };

            if (!payload.clinicId || !payload.specialty) {
                showError('Por favor complete todos los campos');
                return;
            }

            const btn = document.getElementById('saveBtn');
            btn.disabled = true;
            btn.textContent = 'Creando...';

            try {
                const response = await apiCall('/policies', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                if (response && response.ok) {
                    closeModal();
                    showSuccess('Politica creada correctamente');
                    setTimeout(() => loadPolicies(), 1000);
                } else {
                    const error = await response.json();
                    showError(error.message || 'Error al crear la politica');
                }
            } catch (error) {
                console.error('Error creating policy:', error);
                showError('Error al crear la politica');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Crear Politica';
            }
        });

        async function revokePolicy(policyId) {
            const patientCi = getPatientCi();
            if (!patientCi) {
                showError('No se pudo identificar al paciente');
                return;
            }

            try {
                const response = await apiCall('/policies/' + policyId + '/revoke?patientCi=' + patientCi, {
                    method: 'PUT'
                });

                if (response && response.ok) {
                    showSuccess('Politica revocada correctamente');
                    setTimeout(() => loadPolicies(), 1000);
                } else {
                    showError('Error al revocar la politica');
                }
            } catch (error) {
                console.error('Error revoking policy:', error);
                showError('Error al revocar la politica');
            }
        }

        async function confirmDelete() {
            const policyId = document.getElementById('deletePolicyId').value;
            const patientCi = getPatientCi();
            if (!patientCi) {
                showError('No se pudo identificar al paciente');
                return;
            }

            try {
                const response = await apiCall('/policies/' + policyId + '?patientCi=' + patientCi, {
                    method: 'DELETE'
                });

                if (response && response.ok) {
                    closeDeleteModal();
                    showSuccess('Politica eliminada correctamente');
                    setTimeout(() => loadPolicies(), 1000);
                } else {
                    showError('Error al eliminar la politica');
                }
            } catch (error) {
                console.error('Error deleting policy:', error);
                showError('Error al eliminar la politica');
            }
        }

        // ================================================================
        // Navigation
        // ================================================================

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

        // ================================================================
        // Initialize
        // ================================================================

        window.addEventListener('DOMContentLoaded', async function() {
            console.log('Loading access policies page...');
            await Promise.all([
                loadClinics(),
                loadSpecialties()
            ]);
            await loadPolicies();
        });
    </script>
</body>
</html>
