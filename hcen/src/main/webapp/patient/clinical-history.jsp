<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mi Historia Clínica - HCEN</title>
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

        /* Header */
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
        }

        .back-btn:hover {
            background: linear-gradient(135deg, #5568d3, #6a3f8f);
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        }

        /* Statistics Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
            text-align: center;
            transition: transform 0.3s ease;
        }

        .stat-card:hover {
            transform: translateY(-5px);
        }

        .stat-icon {
            font-size: 36px;
            margin-bottom: 10px;
        }

        .stat-value {
            font-size: 32px;
            font-weight: bold;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 5px;
        }

        .stat-label {
            color: #7f8c8d;
            font-size: 13px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        /* Filter Panel */
        .filter-panel {
            background: white;
            padding: 25px;
            border-radius: 15px;
            margin-bottom: 25px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .filter-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 20px;
            color: #2c3e50;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .filter-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }

        .filter-group {
            display: flex;
            flex-direction: column;
        }

        .filter-label {
            font-size: 13px;
            font-weight: 600;
            color: #34495e;
            margin-bottom: 6px;
        }

        .filter-input, .filter-select {
            padding: 10px 12px;
            border: 2px solid #e0e6ed;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.3s ease;
        }

        .filter-input:focus, .filter-select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .filter-actions {
            display: flex;
            gap: 10px;
            margin-top: 15px;
        }

        .btn-filter {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-filter:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .btn-clear {
            background: #ecf0f1;
            color: #34495e;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-clear:hover {
            background: #d5dbdb;
        }

        /* Document Grid */
        .documents-section {
            background: white;
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .section-title {
            font-size: 22px;
            font-weight: 600;
            color: #2c3e50;
        }

        .sort-select {
            padding: 8px 12px;
            border: 2px solid #e0e6ed;
            border-radius: 8px;
            font-size: 14px;
            cursor: pointer;
        }

        .document-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 20px;
            margin-bottom: 25px;
        }

        .document-card {
            background: #f8f9fa;
            border-radius: 12px;
            padding: 20px;
            border-left: 4px solid;
            transition: all 0.3s ease;
            cursor: pointer;
        }

        .document-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
        }

        .document-card.lab { border-left-color: #3498db; }
        .document-card.imaging { border-left-color: #9b59b6; }
        .document-card.prescription { border-left-color: #e74c3c; }
        .document-card.clinical-note { border-left-color: #2ecc71; }
        .document-card.vaccination { border-left-color: #f39c12; }
        .document-card.discharge { border-left-color: #1abc9c; }
        .document-card.other { border-left-color: #95a5a6; }

        .doc-header {
            display: flex;
            justify-content: space-between;
            align-items: start;
            margin-bottom: 12px;
        }

        .doc-type {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            font-weight: 600;
            color: #7f8c8d;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .doc-type-icon {
            font-size: 20px;
        }

        .doc-status {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .status-active {
            background: rgba(46, 213, 115, 0.2);
            color: #27ae60;
        }

        .status-unavailable {
            background: rgba(149, 165, 166, 0.2);
            color: #7f8c8d;
        }

        .doc-title {
            font-size: 18px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 8px;
        }

        .doc-info {
            font-size: 13px;
            color: #7f8c8d;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .doc-actions {
            display: flex;
            gap: 8px;
            margin-top: 15px;
        }

        .btn-view {
            flex: 1;
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-view:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .btn-download {
            background: #ecf0f1;
            color: #34495e;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-download:hover {
            background: #d5dbdb;
        }

        /* Empty State */
        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }

        .empty-icon {
            font-size: 64px;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .empty-title {
            font-size: 20px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 10px;
        }

        .empty-text {
            color: #7f8c8d;
            font-size: 14px;
        }

        /* Loading State */
        .loading {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 60px;
        }

        .spinner {
            width: 50px;
            height: 50px;
            border: 4px solid rgba(102, 126, 234, 0.2);
            border-top-color: #667eea;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        /* Pagination */
        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 10px;
            margin-top: 25px;
        }

        .page-btn {
            background: white;
            border: 2px solid #e0e6ed;
            padding: 8px 16px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            color: #34495e;
            transition: all 0.3s ease;
        }

        .page-btn:hover:not(:disabled) {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border-color: #667eea;
        }

        .page-btn:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        .page-info {
            color: #7f8c8d;
            font-size: 14px;
        }

        /* Modal */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.6);
            z-index: 1000;
            animation: fadeIn 0.3s ease;
        }

        .modal.show {
            display: flex;
            justify-content: center;
            align-items: center;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        .modal-content {
            background: white;
            border-radius: 15px;
            max-width: 700px;
            width: 90%;
            max-height: 85vh;
            overflow-y: auto;
            animation: slideUp 0.3s ease;
            box-shadow: 0 15px 50px rgba(0, 0, 0, 0.3);
        }

        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .modal-header {
            padding: 25px 30px;
            border-bottom: 2px solid #ecf0f1;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .modal-title {
            font-size: 24px;
            font-weight: 600;
            color: #2c3e50;
        }

        .modal-close {
            background: none;
            border: none;
            font-size: 28px;
            cursor: pointer;
            color: #95a5a6;
            transition: color 0.3s;
            padding: 0;
            width: 32px;
            height: 32px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .modal-close:hover {
            color: #e74c3c;
        }

        .modal-body {
            padding: 30px;
        }

        .detail-group {
            margin-bottom: 20px;
        }

        .detail-label {
            font-size: 13px;
            font-weight: 600;
            color: #7f8c8d;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 6px;
        }

        .detail-value {
            font-size: 15px;
            color: #2c3e50;
            padding: 10px 0;
        }

        .metadata-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 15px;
        }

        .modal-actions {
            display: flex;
            gap: 10px;
            padding: 20px 30px;
            border-top: 2px solid #ecf0f1;
        }

        .btn-modal {
            flex: 1;
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            background: #ecf0f1;
            color: #34495e;
        }

        .btn-secondary:hover {
            background: #d5dbdb;
        }

        /* FHIR Document Styles */
        .fhir-document {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .fhir-header {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }

        .fhir-header h4 {
            margin-bottom: 10px;
            color: #0056b3;
        }

        .fhir-section {
            border-left: 3px solid #667eea;
            padding-left: 15px;
            margin-left: 10px;
            margin-bottom: 20px;
        }

        .fhir-section h5 {
            font-weight: 600;
            margin-bottom: 10px;
            color: #667eea;
        }

        .fhir-narrative {
            background-color: #ffffff;
            padding: 10px;
            border-radius: 3px;
            line-height: 1.6;
        }

        .fhir-narrative p {
            margin-bottom: 8px;
        }

        .fhir-attachments {
            margin-top: 20px;
            padding: 15px;
            background-color: #f8f9fa;
            border-radius: 5px;
        }

        .fhir-attachments ul {
            list-style-type: none;
            padding-left: 0;
        }

        .fhir-attachments li {
            padding: 8px 0;
            border-bottom: 1px solid #dee2e6;
        }

        .fhir-attachments li:last-child {
            border-bottom: none;
        }

        .fhir-modal-content {
            max-width: 900px;
        }

        .btn-fhir {
            background: linear-gradient(135deg, #17a2b8, #138496);
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .btn-fhir:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(23, 162, 184, 0.3);
        }

        .btn-group-vertical {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        /* Alerts */
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

        /* Responsive */
        @media (max-width: 768px) {
            .document-grid {
                grid-template-columns: 1fr;
            }

            .filter-grid {
                grid-template-columns: 1fr;
            }

            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <div class="header-left">
                <div class="logo">
                    <span class="logo-icon">📋</span>
                    <span>Mi Historia Clínica</span>
                </div>
                <div class="breadcrumb">
                    <a href="/hcen/patient/dashboard.jsp">Inicio</a> &gt; Mi Historia Clínica
                </div>
            </div>
            <button class="back-btn" onclick="goBack()">← Volver al Panel</button>
            <button class="logout-btn" onclick="logout()">🚪 Cerrar Sesión</button>
        </div>

        <!-- Alerts -->
        <div id="errorAlert" class="alert alert-error"></div>
        <div id="infoAlert" class="alert alert-info"></div>

        <!-- Statistics Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">📄</div>
                <div class="stat-value" id="totalDocs">-</div>
                <div class="stat-label">Total Documentos</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">🧪</div>
                <div class="stat-value" id="labResults">-</div>
                <div class="stat-label">Laboratorios</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">📸</div>
                <div class="stat-value" id="imaging">-</div>
                <div class="stat-label">Imágenes</div>
            </div>
            <div class="stat-card">
                <div class="stat-icon">💊</div>
                <div class="stat-value" id="prescriptions">-</div>
                <div class="stat-label">Recetas</div>
            </div>
        </div>

        <!-- Filter Panel -->
        <div class="filter-panel">
            <div class="filter-title">🔍 Filtrar Documentos</div>
            <div class="filter-grid">
                <div class="filter-group">
                    <label class="filter-label">Tipo de Documento</label>
                    <select id="filterType" class="filter-select">
                        <option value="">Todos los tipos</option>
                        <option value="LAB_RESULT">Resultados de Laboratorio</option>
                        <option value="IMAGING">Estudios de Imágenes</option>
                        <option value="PRESCRIPTION">Recetas Médicas</option>
                        <option value="CLINICAL_NOTE">Notas Clínicas</option>
                        <option value="VACCINATION_RECORD">Registros de Vacunación</option>
                        <option value="DISCHARGE_SUMMARY">Resúmenes de Alta</option>
                        <option value="SURGICAL_REPORT">Reportes Quirúrgicos</option>
                        <option value="OTHER">Otros</option>
                    </select>
                </div>
                <div class="filter-group">
                    <label class="filter-label">Desde</label>
                    <input type="date" id="filterFromDate" class="filter-input">
                </div>
                <div class="filter-group">
                    <label class="filter-label">Hasta</label>
                    <input type="date" id="filterToDate" class="filter-input">
                </div>
                <div class="filter-group">
                    <label class="filter-label">Buscar en título</label>
                    <input type="text" id="filterSearch" class="filter-input" placeholder="Ej: hemograma">
                </div>
            </div>
            <div class="filter-actions">
                <button class="btn-filter" onclick="applyFilters()">🔍 Aplicar Filtros</button>
                <button class="btn-clear" onclick="clearFilters()">✕ Limpiar</button>
            </div>
        </div>

        <!-- Documents Section -->
        <div class="documents-section">
            <div class="section-header">
                <div class="section-title" id="documentsTitle">Mis Documentos Clínicos</div>
                <select class="sort-select" id="sortBy" onchange="applySort()">
                    <option value="date-desc">Más recientes primero</option>
                    <option value="date-asc">Más antiguos primero</option>
                    <option value="type">Por tipo</option>
                    <option value="clinic">Por centro médico</option>
                </select>
            </div>

            <!-- Loading State -->
            <div id="loadingState" class="loading">
                <div class="spinner"></div>
            </div>

            <!-- Empty State -->
            <div id="emptyState" class="empty-state" style="display: none;">
                <div class="empty-icon">📭</div>
                <div class="empty-title">No se encontraron documentos</div>
                <div class="empty-text">
                    No hay documentos que coincidan con los filtros seleccionados.
                    <br>Intenta ajustar los criterios de búsqueda.
                </div>
            </div>

            <!-- Document Grid -->
            <div id="documentGrid" class="document-grid" style="display: none;">
                <!-- Documents will be populated here by JavaScript -->
            </div>

            <!-- Pagination -->
            <div id="paginationContainer" class="pagination" style="display: none;">
                <button class="page-btn" id="prevBtn" onclick="previousPage()">← Anterior</button>
                <span class="page-info" id="pageInfo">Página 1 de 1</span>
                <button class="page-btn" id="nextBtn" onclick="nextPage()">Siguiente →</button>
            </div>
        </div>
    </div>

    <!-- Document Detail Modal -->
    <div id="documentModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <div class="modal-title" id="modalTitle">Detalle del Documento</div>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div class="detail-group">
                    <div class="detail-label">Título</div>
                    <div class="detail-value" id="detailTitle">-</div>
                </div>
                <div class="detail-group">
                    <div class="detail-label">Descripción</div>
                    <div class="detail-value" id="detailDescription">Sin descripción</div>
                </div>
                <div class="metadata-grid">
                    <div class="detail-group">
                        <div class="detail-label">Tipo</div>
                        <div class="detail-value" id="detailType">-</div>
                    </div>
                    <div class="detail-group">
                        <div class="detail-label">Estado</div>
                        <div class="detail-value" id="detailStatus">-</div>
                    </div>
                    <div class="detail-group">
                        <div class="detail-label">Fecha de Creación</div>
                        <div class="detail-value" id="detailDate">-</div>
                    </div>
                    <div class="detail-group">
                        <div class="detail-label">Centro Médico</div>
                        <div class="detail-value" id="detailClinic">-</div>
                    </div>
                    <div class="detail-group">
                        <div class="detail-label">Profesional</div>
                        <div class="detail-value" id="detailProfessional">-</div>
                    </div>
                    <div class="detail-group">
                        <div class="detail-label">Hash del Documento</div>
                        <div class="detail-value" id="detailHash" style="font-size: 12px; word-break: break-all;">-</div>
                    </div>
                </div>
            </div>
            <div class="modal-actions">
                <button class="btn-modal btn-secondary" onclick="closeModal()">Cerrar</button>
            </div>
        </div>
    </div>

    <!-- FHIR Document Modal -->
    <div id="fhirDocumentModal" class="modal">
        <div class="modal-content fhir-modal-content">
            <div class="modal-header">
                <div class="modal-title">📋 Documento Clínico FHIR</div>
                <button class="modal-close" onclick="closeFhirModal()">×</button>
            </div>
            <div class="modal-body" id="fhir-modal-body">
                <!-- FHIR content will be inserted here -->
            </div>
            <div class="modal-actions">
                <button class="btn-modal btn-secondary" onclick="closeFhirModal()">Cerrar</button>
            </div>
        </div>
    </div>

    <script>
        // API Configuration
        const API_BASE = '/hcen/api';

        // State
        let currentPage = 0;
        const pageSize = 12;
        let totalPages = 1;
        let allDocuments = [];
        let filteredDocuments = [];
        let currentDocumentId = null;
        let patientCi = null;

        // Document type icons and classes
        const docTypeConfig = {
            'LAB_RESULT': { icon: '🧪', class: 'lab' },
            'IMAGING': { icon: '📸', class: 'imaging' },
            'PRESCRIPTION': { icon: '💊', class: 'prescription' },
            'CLINICAL_NOTE': { icon: '📝', class: 'clinical-note' },
            'VACCINATION_RECORD': { icon: '💉', class: 'vaccination' },
            'DISCHARGE_SUMMARY': { icon: '🏥', class: 'discharge' },
            'SURGICAL_REPORT': { icon: '🔬', class: 'discharge' },
            'PATHOLOGY_REPORT': { icon: '🔬', class: 'lab' },
            'CONSULTATION': { icon: '👨‍⚕️', class: 'clinical-note' },
            'EMERGENCY_REPORT': { icon: '🚑', class: 'discharge' },
            'REFERRAL': { icon: '➡️', class: 'other' },
            'OTHER': { icon: '📄', class: 'other' }
        };

        /**
         * Get JWT token from sessionStorage
         */
        function getToken() {
            return sessionStorage.getItem('accessToken');
        }

        /**
         * Parse JWT to extract patient CI
         */
        function parseJwt(token) {
            try {
                const base64Url = token.split('.')[1];
                const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                const jsonPayload = decodeURIComponent(
                    atob(base64).split('').map(function(c) {
                        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                    }).join('')
                );
                return JSON.parse(jsonPayload);
            } catch (e) {
                console.error('Error parsing JWT:', e);
                return null;
            }
        }

        /**
         * Make authenticated API call
         */
        async function apiCall(endpoint, options = {}) {
            const token = getToken();
            if (!token) {
                showError('Sesión expirada. Por favor, inicie sesión nuevamente.');
                setTimeout(() => window.location.href = '/hcen/login-patient.jsp', 2000);
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
                    setTimeout(() => window.location.href = '/hcen/login-patient.jsp', 2000);
                    return null;
                }

                if (!response.ok) {
                    throw new Error('API call failed: ' + response.status);
                }

                return await response.json();
            } catch (error) {
                console.error('API call error:', error);
                return null;
            }
        }

        /**
         * Show error message
         */
        function showError(message) {
            const errorDiv = document.getElementById('errorAlert');
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
            setTimeout(() => errorDiv.classList.remove('show'), 5000);
        }

        /**
         * Show info message
         */
        function showInfo(message) {
            const infoDiv = document.getElementById('infoAlert');
            infoDiv.textContent = message;
            infoDiv.classList.add('show');
            setTimeout(() => infoDiv.classList.remove('show'), 5000);
        }

        /**
         * Load statistics
         */
        async function loadStatistics() {
            try {
                const stats = await apiCall('/clinical-history/stats?patientCi=' + patientCi);
                if (stats) {
                    document.getElementById('totalDocs').textContent = stats.totalDocuments || 0;
                    document.getElementById('labResults').textContent = stats.byType['Resultados de Laboratorio'] || stats.byType['Laboratory Result'] || 0;
                    document.getElementById('imaging').textContent = stats.byType['Estudios de Imágenes'] || stats.byType['Medical Imaging'] || 0;
                    document.getElementById('prescriptions').textContent = stats.byType['Receta Médica'] || stats.byType['Prescription'] || 0;
                }
            } catch (error) {
                console.error('Error loading statistics:', error);
            }
        }

        /**
         * Load clinical history documents
         */
        async function loadDocuments() {
            showLoading();

            try {
                // Build query params
                const params = new URLSearchParams({
                    patientCi: patientCi,
                    page: currentPage,
                    size: pageSize
                });

                // Add filters
                const docType = document.getElementById('filterType').value;
                if (docType) params.append('documentType', docType);

                const fromDate = document.getElementById('filterFromDate').value;
                if (fromDate) params.append('fromDate', fromDate + 'T00:00:00');

                const toDate = document.getElementById('filterToDate').value;
                if (toDate) params.append('toDate', toDate + 'T23:59:59');

                const response = await apiCall('/clinical-history?' + params.toString());

                if (response) {
                    console.log('API response:', response);

                    // Handle response format - could be array or object with documents property
                    allDocuments = Array.isArray(response) ? response : (response.documents || []);
                    totalPages = response.totalPages || 1;

                    console.log('Documents loaded:', allDocuments.length);

                    // Apply client-side search filter
                    const searchTerm = document.getElementById('filterSearch').value.toLowerCase();
                    filteredDocuments = searchTerm
                        ? allDocuments.filter(doc => doc.title.toLowerCase().includes(searchTerm))
                        : [...allDocuments];

                    console.log('Filtered documents:', filteredDocuments.length);

                    // Apply sorting
                    applySortToDocuments();

                    // Update UI
                    hideLoading();
                    updateDocumentGrid();
                    updatePagination();
                } else {
                    console.error('No response from API');
                    hideLoading();
                    showEmptyState();
                }
            } catch (error) {
                console.error('Error loading documents:', error);
                showError('Error al cargar los documentos');
                hideLoading();
            }
        }

        /**
         * Update document grid
         */
        function updateDocumentGrid() {
            const grid = document.getElementById('documentGrid');
            const emptyState = document.getElementById('emptyState');

            grid.innerHTML = '';

            console.log('Updating grid with', filteredDocuments.length, 'documents');

            if (!filteredDocuments || filteredDocuments.length === 0) {
                console.log('No documents to display - showing empty state');
                grid.style.display = 'none';
                emptyState.style.display = 'block';
                return;
            }

            grid.style.display = 'grid';
            emptyState.style.display = 'none';

            filteredDocuments.forEach(doc => {
                const config = docTypeConfig[doc.documentType] || docTypeConfig['OTHER'];

                const card = document.createElement('div');
                card.className = 'document-card ' + config.class;
                card.innerHTML = `
                    <div class="doc-header">
                        <div class="doc-type">
                            <span class="doc-type-icon">\${config.icon}</span>
                            \${doc.documentTypeDisplayName}
                        </div>
                        <span class="doc-status \${doc.status === 'ACTIVE' ? 'status-active' : 'status-unavailable'}">
                            \${doc.status === 'ACTIVE' ? 'Disponible' : 'No disponible'}
                        </span>
                    </div>
                    <div class="doc-title">\${doc.title}</div>
                    <div class="doc-info">🏥 \${doc.clinicName}</div>
                    <div class="doc-info">👨‍⚕️ \${doc.professionalName}</div>
                    <div class="doc-info">📅 \${formatDate(doc.createdAt)}</div>
                    <div class="doc-actions">
                        <button class="btn-fhir" onclick="viewFhirDocument(\${doc.id})">📋 Ver Documento</button>
                        <button class="btn-view" onclick="viewDocument(\${doc.id})">Ver Detalles</button>
                    </div>
                `;
                grid.appendChild(card);
            });

            document.getElementById('documentsTitle').textContent =
                `Mis Documentos Clínicos (\${filteredDocuments.length})`;
        }

        /**
         * Format date for display
         */
        function formatDate(dateString) {
            const date = new Date(dateString);
            return date.toLocaleDateString('es-UY', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }

        /**
         * View document details
         */
        async function viewDocument(documentId) {
            currentDocumentId = documentId;

            try {
                const detail = await apiCall('/clinical-history/documents/' + documentId + '?patientCi=' + patientCi);

                if (detail) {
                    document.getElementById('modalTitle').textContent = detail.title;
                    document.getElementById('detailTitle').textContent = detail.title;
                    document.getElementById('detailDescription').textContent = detail.description || 'Sin descripción';
                    document.getElementById('detailType').textContent = detail.documentType;
                    document.getElementById('detailStatus').textContent = detail.status;
                    document.getElementById('detailDate').textContent = formatDate(detail.createdAt);
                    document.getElementById('detailClinic').textContent = detail.clinicName;
                    document.getElementById('detailProfessional').textContent = detail.professionalName;
                    document.getElementById('detailHash').textContent = detail.documentHash;

                    document.getElementById('documentModal').classList.add('show');
                }
            } catch (error) {
                console.error('Error loading document detail:', error);
                showError('Error al cargar detalles del documento');
            }
        }

        /**
         * Download document
         * DISABLED: Download functionality removed per user request
         */
        /*
        async function downloadDocument() {
            if (!currentDocumentId) return;
            downloadDocumentById(currentDocumentId);
        }

        /**
         * Download document by ID
         * DISABLED: Download functionality removed per user request
         */
        /*
        async function downloadDocumentById(documentId) {
            try {
                const content = await apiCall('/clinical-history/documents/' + documentId + '/content?patientCi=' + patientCi);

                if (content && content.available) {
                    showInfo('Redirigiendo a descarga...');
                    // Open document URL in new tab
                    window.open(content.contentUrl, '_blank');
                } else {
                    showInfo(content.message || 'El contenido del documento no está disponible en este momento.');
                }
            } catch (error) {
                console.error('Error downloading document:', error);
                showError('Error al descargar el documento');
            }
        }
        */

        /**
         * Close modal
         */
        function closeModal() {
            document.getElementById('documentModal').classList.remove('show');
            currentDocumentId = null;
        }

        /**
         * View FHIR document
         */
        async function viewFhirDocument(documentId) {
            try {
                showLoading();

                const fhirDoc = await apiCall(
                    `/clinical-history/documents/` + documentId + `/fhir?patientCi=` + patientCi,
                    {
                        method: 'GET',
                        headers: {
                            'Accept': 'application/fhir+json'
                        }
                    }
                );

                hideLoading();

                if (!fhirDoc) {
                    showError('No se pudo cargar el documento FHIR.');
                    return;
                }

                // Validate FHIR format
                if (!fhirDoc.resourceType) {
                    throw new Error('Formato FHIR inválido: falta resourceType');
                }

                displayFhirDocument(fhirDoc);

            } catch (error) {
                hideLoading();
                console.error('Error fetching FHIR document:', error);
                showError('Error al cargar el documento FHIR: ' + error.message);
            }
        }

        /**
         * Display FHIR document in modal
         */
        function displayFhirDocument(fhirDoc) {
            let content = '';

            if (fhirDoc.resourceType === 'Bundle' && fhirDoc.type === 'document') {
                // Parse FHIR Bundle
                const composition = fhirDoc.entry?.find(e =>
                    e.resource && e.resource.resourceType === 'Composition'
                )?.resource;

                if (composition) {
                    content = renderComposition(composition);
                } else {
                    content = '<p class="empty-text">No se pudo extraer el contenido del documento FHIR.</p>';
                }

            } else if (fhirDoc.resourceType === 'DocumentReference') {
                // Parse DocumentReference
                content = renderDocumentReference(fhirDoc);

            } else {
                content = '<p class="empty-text">Formato FHIR no reconocido.</p>';
            }

            // Display in modal
            document.getElementById('fhir-modal-body').innerHTML = content;
            document.getElementById('fhirDocumentModal').classList.add('show');
        }

        /**
         * Render FHIR Composition
         */
        function renderComposition(composition) {
            let html = `
                <div class="fhir-document">
                    <div class="fhir-header">
                        <h4>\${composition.title || 'Documento Clínico'}</h4>
                        <p style="color: #7f8c8d; font-size: 14px; margin-bottom: 5px;">
                            📅 \${formatFhirDate(composition.date)}
                        </p>
            `;

            if (composition.author && composition.author.length > 0) {
                html += `
                    <p style="font-size: 14px; margin-bottom: 5px;">
                        <strong>Autor:</strong> \${composition.author.map(a => a.display || a.reference).join(', ')}
                    </p>
                `;
            }

            if (composition.subject) {
                html += `
                    <p style="font-size: 14px;">
                        <strong>Paciente:</strong> \${composition.subject.display || composition.subject.reference}
                    </p>
                `;
            }

            html += `
                    </div>
                    <hr style="border: 1px solid #e0e6ed; margin: 20px 0;">
            `;

            // Render sections
            if (composition.section && composition.section.length > 0) {
                html += '<div class="fhir-sections">';
                composition.section.forEach(section => {
                    html += renderSection(section);
                });
                html += '</div>';
            } else {
                html += '<p class="empty-text">No se encontró contenido en las secciones del documento.</p>';
            }

            html += '</div>';
            return html;
        }

        /**
         * Render FHIR section
         */
        function renderSection(section, level = 0) {
            let html = `
                <div class="fhir-section" style="margin-left: \${level * 10}px;">
                    <h5>\${section.title || 'Sección'}</h5>
            `;

            if (section.text && section.text.div) {
                // FHIR narrative (HTML content) - sanitize before display
                const narrativeDiv = document.createElement('div');
                narrativeDiv.innerHTML = section.text.div;
                html += `<div class="fhir-narrative">\${narrativeDiv.innerHTML}</div>`;
            } else if (section.code && section.code.text) {
                html += `<p>\${escapeHtml(section.code.text)}</p>`;
            } else {
                html += '<p style="color: #7f8c8d; font-style: italic;">Sin contenido disponible</p>';
            }

            // Render nested sections
            if (section.section && section.section.length > 0) {
                section.section.forEach(subsection => {
                    html += renderSection(subsection, level + 1);
                });
            }

            html += '</div>';
            return html;
        }

        /**
         * Render FHIR DocumentReference
         */
        function renderDocumentReference(docRef) {
            let html = `
                <div class="fhir-document">
                    <div class="fhir-header">
                        <h4>\${docRef.type?.coding?.[0]?.display || 'Documento Clínico'}</h4>
                        <p style="color: #7f8c8d; font-size: 14px; margin-bottom: 5px;">
                            📅 \${formatFhirDate(docRef.date)}
                        </p>
            `;

            if (docRef.author && docRef.author.length > 0) {
                html += `
                    <p style="font-size: 14px; margin-bottom: 5px;">
                        <strong>Autor:</strong> \${docRef.author.map(a => a.display || a.reference).join(', ')}
                    </p>
                `;
            }

            if (docRef.subject) {
                html += `
                    <p style="font-size: 14px; margin-bottom: 5px;">
                        <strong>Paciente:</strong> \${docRef.subject.display || docRef.subject.reference}
                    </p>
                `;
            }

            if (docRef.custodian) {
                html += `
                    <p style="font-size: 14px;">
                        <strong>Institución:</strong> \${docRef.custodian.display || docRef.custodian.reference}
                    </p>
                `;
            }

            html += `
                    </div>
                    <hr style="border: 1px solid #e0e6ed; margin: 20px 0;">
            `;

            if (docRef.description) {
                html += `<p style="margin-bottom: 15px;"><strong>Descripción:</strong> \${escapeHtml(docRef.description)}</p>`;
            }

            if (docRef.status) {
                html += `<p style="margin-bottom: 15px;"><strong>Estado:</strong> \${escapeHtml(docRef.status)}</p>`;
            }

            if (docRef.content && docRef.content.length > 0) {
                html += '<div class="fhir-attachments"><h5>Archivos Adjuntos:</h5><ul>';
                docRef.content.forEach(content => {
                    const attachment = content.attachment;
                    html += `
                        <li style="display: flex; justify-content: space-between; align-items: center;">
                            <span>\${escapeHtml(attachment.title || 'Documento')}</span>
                    `;
                    if (attachment.url) {
                        html += `
                            <a href="\${escapeHtml(attachment.url)}" target="_blank" class="btn-view"
                               style="padding: 6px 12px; font-size: 12px; text-decoration: none; display: inline-block;">
                                🔗 Abrir
                            </a>
                        `;
                    }
                    html += '</li>';
                });
                html += '</ul></div>';
            }

            html += '</div>';
            return html;
        }

        /**
         * Format FHIR date for display
         */
        function formatFhirDate(dateString) {
            if (!dateString) return 'N/A';
            try {
                const date = new Date(dateString);
                return date.toLocaleDateString('es-UY', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            } catch (e) {
                return dateString;
            }
        }

        /**
         * Escape HTML to prevent XSS
         */
        function escapeHtml(unsafe) {
            if (!unsafe) return '';
            return unsafe
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        /**
         * Close FHIR modal
         */
        function closeFhirModal() {
            document.getElementById('fhirDocumentModal').classList.remove('show');
        }

        /**
         * Apply filters
         */
        function applyFilters() {
            currentPage = 0;
            loadDocuments();
        }

        /**
         * Clear filters
         */
        function clearFilters() {
            document.getElementById('filterType').value = '';
            document.getElementById('filterFromDate').value = '';
            document.getElementById('filterToDate').value = '';
            document.getElementById('filterSearch').value = '';
            currentPage = 0;
            loadDocuments();
        }

        /**
         * Apply sort
         */
        function applySort() {
            applySortToDocuments();
            updateDocumentGrid();
        }

        /**
         * Apply sorting to documents
         */
        function applySortToDocuments() {
            const sortBy = document.getElementById('sortBy').value;

            switch (sortBy) {
                case 'date-desc':
                    filteredDocuments.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                    break;
                case 'date-asc':
                    filteredDocuments.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
                    break;
                case 'type':
                    filteredDocuments.sort((a, b) => a.documentTypeDisplayName.localeCompare(b.documentTypeDisplayName));
                    break;
                case 'clinic':
                    filteredDocuments.sort((a, b) => a.clinicName.localeCompare(b.clinicName));
                    break;
            }
        }

        /**
         * Pagination
         */
        function previousPage() {
            if (currentPage > 0) {
                currentPage--;
                loadDocuments();
            }
        }

        function nextPage() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadDocuments();
            }
        }

        function updatePagination() {
            const container = document.getElementById('paginationContainer');
            if (totalPages <= 1) {
                container.style.display = 'none';
                return;
            }

            container.style.display = 'flex';
            document.getElementById('pageInfo').textContent = `Página \${currentPage + 1} de \${totalPages}`;
            document.getElementById('prevBtn').disabled = currentPage === 0;
            document.getElementById('nextBtn').disabled = currentPage >= totalPages - 1;
        }

        /**
         * Loading states
         */
        function showLoading() {
            document.getElementById('loadingState').style.display = 'flex';
            document.getElementById('documentGrid').style.display = 'none';
            document.getElementById('emptyState').style.display = 'none';
        }

        function hideLoading() {
            document.getElementById('loadingState').style.display = 'none';
            document.getElementById('documentGrid').style.display = 'grid';
        }

        function showEmptyState() {
            document.getElementById('emptyState').style.display = 'block';
            document.getElementById('documentGrid').style.display = 'none';
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
            // Get patient CI from JWT
            const token = getToken();
            if (!token) {
                showError('No se encontró token de acceso. Redirigiendo...');
                setTimeout(() => window.location.href = '/hcen/login-patient.jsp', 2000);
                return;
            }

            const claims = parseJwt(token);
            if (!claims || claims.role !== 'PATIENT') {
                showError('Acceso denegado. Esta página es solo para pacientes.');
                setTimeout(() => window.location.href = '/hcen/login-patient.jsp', 2000);
                return;
            }

            patientCi = claims.sub || claims.ci;

            // Load data
            loadStatistics();
            loadDocuments();
        });

        // Close modal on outside click
        document.getElementById('documentModal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });

        // Close FHIR modal on outside click
        document.getElementById('fhirDocumentModal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeFhirModal();
            }
        });
    </script>
</body>
</html>
