<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Registrar Clínica</title>
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
            max-width: 900px;
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

        .form-container {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .form-section {
            margin-bottom: 35px;
        }

        .form-section-title {
            font-size: 18px;
            font-weight: 600;
            color: #00d4ff;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid rgba(0, 212, 255, 0.3);
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 25px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .form-group.full-width {
            grid-column: 1 / -1;
        }

        .form-group label {
            color: #a0a0a0;
            font-size: 13px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .form-group label .required {
            color: #f44336;
            margin-left: 4px;
        }

        .form-control {
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(255, 255, 255, 0.1);
            padding: 14px 16px;
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

        .form-control::placeholder {
            color: #666;
        }

        .form-control.error {
            border-color: #f44336;
            background: rgba(244, 67, 54, 0.05);
        }

        .field-error {
            color: #f44336;
            font-size: 12px;
            margin-top: 4px;
            display: none;
        }

        .field-error.show {
            display: block;
        }

        .field-hint {
            color: #888;
            font-size: 12px;
            margin-top: 4px;
        }

        .form-actions {
            display: flex;
            gap: 15px;
            justify-content: flex-end;
            margin-top: 35px;
            padding-top: 25px;
            border-top: 1px solid rgba(255, 255, 255, 0.1);
        }

        .btn {
            background: linear-gradient(135deg, #00d4ff, #0095ff);
            color: white;
            border: none;
            padding: 14px 32px;
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
            transform: none;
        }

        .btn-secondary {
            background: rgba(255, 255, 255, 0.1);
            box-shadow: 0 4px 15px rgba(255, 255, 255, 0.1);
        }

        .btn-secondary:hover:not(:disabled) {
            background: rgba(255, 255, 255, 0.15);
            box-shadow: 0 6px 20px rgba(255, 255, 255, 0.15);
        }

        .loading-spinner {
            display: inline-block;
            width: 16px;
            height: 16px;
            border: 2px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
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

        .api-key-section {
            background: rgba(255, 193, 7, 0.1);
            border: 2px solid rgba(255, 193, 7, 0.3);
            padding: 25px;
            border-radius: 10px;
            margin-top: 25px;
            display: none;
        }

        .api-key-section.show {
            display: block;
        }

        .api-key-title {
            font-size: 16px;
            font-weight: 600;
            color: #ffc107;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .api-key-value {
            background: rgba(0, 0, 0, 0.3);
            padding: 15px;
            border-radius: 8px;
            font-family: 'Courier New', monospace;
            font-size: 13px;
            word-break: break-all;
            margin-bottom: 15px;
            color: #00d4ff;
        }

        .api-key-warning {
            color: #ffc107;
            font-size: 13px;
            line-height: 1.6;
        }

        .copy-btn {
            background: rgba(255, 193, 7, 0.2);
            color: #ffc107;
            border: 1px solid rgba(255, 193, 7, 0.3);
            padding: 10px 20px;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .copy-btn:hover {
            background: rgba(255, 193, 7, 0.3);
        }

        @media (max-width: 768px) {
            .form-grid {
                grid-template-columns: 1fr;
            }

            .form-container {
                padding: 25px 20px;
            }

            .form-actions {
                flex-direction: column-reverse;
            }

            .btn {
                width: 100%;
                justify-content: center;
            }

            .header {
                flex-direction: column;
                gap: 15px;
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
                <div class="page-title">Registrar Nueva Clínica</div>
                <div class="breadcrumb">
                    <a href="/hcen/admin/dashboard.jsp">Dashboard</a> /
                    <a href="/hcen/admin/clinics.jsp">Clínicas</a> /
                    Registrar
                </div>
            </div>
        </div>

        <div class="form-container">
            <div id="successMessage" class="message"></div>
            <div id="errorMessage" class="message"></div>

            <form id="clinicRegistrationForm" onsubmit="submitForm(event)">
                <div class="form-section">
                    <div class="form-section-title">📋 Información Básica</div>
                    <div class="form-grid">
                        <div class="form-group full-width">
                            <label for="clinicName">
                                Nombre de la Clínica
                                <span class="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="clinicName"
                                name="clinicName"
                                class="form-control"
                                placeholder="Ej: Clínica Médica San Rafael"
                                required
                                maxlength="200"
                            >
                            <div class="field-error" id="clinicNameError"></div>
                        </div>

                        <div class="form-group full-width">
                            <label for="address">
                                Dirección
                                <span class="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="address"
                                name="address"
                                class="form-control"
                                placeholder="Ej: Av. 18 de Julio 1234"
                                required
                                maxlength="300"
                            >
                            <div class="field-error" id="addressError"></div>
                        </div>

                        <div class="form-group">
                            <label for="city">
                                Ciudad
                                <span class="required">*</span>
                            </label>
                            <input
                                type="text"
                                id="city"
                                name="city"
                                class="form-control"
                                placeholder="Ej: Montevideo"
                                required
                                maxlength="100"
                            >
                            <div class="field-error" id="cityError"></div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="form-section-title">📞 Información de Contacto</div>
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="phoneNumber">
                                Teléfono
                                <span class="required">*</span>
                            </label>
                            <input
                                type="tel"
                                id="phoneNumber"
                                name="phoneNumber"
                                class="form-control"
                                placeholder="Ej: 024123456 o 099123456"
                                required
                                pattern="0[0-9]{8}"
                            >
                            <div class="field-hint">Formato: 024123456 (fijo) o 099123456 (celular)</div>
                            <div class="field-error" id="phoneNumberError"></div>
                        </div>

                        <div class="form-group">
                            <label for="email">
                                Email
                                <span class="required">*</span>
                            </label>
                            <input
                                type="email"
                                id="email"
                                name="email"
                                class="form-control"
                                placeholder="Ej: contacto@clinica.com.uy"
                                required
                            >
                            <div class="field-error" id="emailError"></div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="form-section-title">🔗 Configuración Técnica</div>
                    <div class="form-grid">
                        <div class="form-group full-width">
                            <label for="peripheralNodeUrl">
                                URL del Nodo Periférico
                                <span class="required">*</span>
                            </label>
                            <input
                                type="url"
                                id="peripheralNodeUrl"
                                name="peripheralNodeUrl"
                                class="form-control"
                                placeholder="https://clinic-node.example.com/api"
                                required
                                pattern="https://.*"
                            >
                            <div class="field-hint">Debe comenzar con https:// (conexión segura requerida)</div>
                            <div class="field-error" id="peripheralNodeUrlError"></div>
                        </div>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="button" class="btn btn-secondary" onclick="cancelRegistration()">
                        ❌ Cancelar
                    </button>
                    <button type="submit" class="btn" id="submitBtn">
                        ✅ Registrar Clínica
                    </button>
                </div>
            </form>

            <div id="apiKeySection" class="api-key-section">
                <div class="api-key-title">
                    🔑 API Key Generada - ¡IMPORTANTE!
                </div>
                <div class="api-key-value" id="apiKeyValue"></div>
                <div class="api-key-warning">
                    ⚠️ <strong>Esta es la ÚNICA vez que verá esta API key completa.</strong><br>
                    Por favor, cópiela y guárdela en un lugar seguro. Necesitará esta clave para configurar el nodo periférico de la clínica.<br>
                    En consultas posteriores, la clave aparecerá enmascarada por seguridad.
                </div>
                <div style="margin-top: 15px; display: flex; gap: 10px;">
                    <button class="copy-btn" onclick="copyApiKey()">
                        📋 Copiar API Key
                    </button>
                    <button class="btn" onclick="goToClinics()">
                        📋 Ir a Lista de Clínicas
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script>
        // API Configuration
        const API_BASE = '/hcen/api';

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
                showError('Sesión expirada. Por favor, inicie sesión nuevamente.');
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
                    showError('Sesión expirada. Redirigiendo...');
                    setTimeout(() => {
                        window.location.href = '/hcen/login-admin.jsp';
                    }, 2000);
                    return null;
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
         * Show error message
         */
        function showError(message) {
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.textContent = '❌ ' + message;
            errorDiv.className = 'message error';
            errorDiv.style.display = 'flex';

            document.getElementById('successMessage').style.display = 'none';

            // Scroll to top to show error
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        /**
         * Show success message
         */
        function showSuccess(message) {
            const successDiv = document.getElementById('successMessage');
            successDiv.textContent = '✅ ' + message;
            successDiv.className = 'message success';
            successDiv.style.display = 'flex';

            document.getElementById('errorMessage').style.display = 'none';

            // Scroll to top to show success
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        /**
         * Clear all field errors
         */
        function clearFieldErrors() {
            document.querySelectorAll('.form-control').forEach(input => {
                input.classList.remove('error');
            });
            document.querySelectorAll('.field-error').forEach(error => {
                error.classList.remove('show');
                error.textContent = '';
            });
        }

        /**
         * Show field error
         */
        function showFieldError(fieldName, message) {
            const field = document.getElementById(fieldName);
            const error = document.getElementById(fieldName + 'Error');

            if (field) {
                field.classList.add('error');
            }

            if (error) {
                error.textContent = message;
                error.classList.add('show');
            }
        }

        /**
         * Validate form
         */
        function validateForm() {
            clearFieldErrors();

            let isValid = true;

            // Clinic Name
            const clinicName = document.getElementById('clinicName').value.trim();
            if (!clinicName || clinicName.length < 3) {
                showFieldError('clinicName', 'El nombre debe tener al menos 3 caracteres');
                isValid = false;
            }

            // Address
            const address = document.getElementById('address').value.trim();
            if (!address || address.length < 5) {
                showFieldError('address', 'La dirección debe tener al menos 5 caracteres');
                isValid = false;
            }

            // City
            const city = document.getElementById('city').value.trim();
            if (!city || city.length < 3) {
                showFieldError('city', 'La ciudad debe tener al menos 3 caracteres');
                isValid = false;
            }

            // Phone Number (Uruguay format)
            const phoneNumber = document.getElementById('phoneNumber').value.trim();
            const phonePattern = /^0[0-9]{8}$/;
            if (!phonePattern.test(phoneNumber)) {
                showFieldError('phoneNumber', 'Formato inválido. Debe ser 024123456 (fijo) o 099123456 (celular)');
                isValid = false;
            }

            // Email
            const email = document.getElementById('email').value.trim();
            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailPattern.test(email)) {
                showFieldError('email', 'Email inválido');
                isValid = false;
            }

            // Peripheral Node URL (must be HTTPS)
            const peripheralNodeUrl = document.getElementById('peripheralNodeUrl').value.trim();
            if (!peripheralNodeUrl.startsWith('https://')) {
                showFieldError('peripheralNodeUrl', 'La URL debe comenzar con https:// (conexión segura requerida)');
                isValid = false;
            }

            return isValid;
        }

        /**
         * Submit clinic registration form
         */
        async function submitForm(event) {
            event.preventDefault();

            // Hide previous messages
            document.getElementById('successMessage').style.display = 'none';
            document.getElementById('errorMessage').style.display = 'none';
            document.getElementById('apiKeySection').classList.remove('show');

            // Validate form
            if (!validateForm()) {
                showError('Por favor, corrija los errores en el formulario');
                return;
            }

            // Disable submit button
            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="loading-spinner"></span> Registrando...';

            try {
                // Collect form data
                const formData = {
                    clinicName: document.getElementById('clinicName').value.trim(),
                    address: document.getElementById('address').value.trim(),
                    city: document.getElementById('city').value.trim(),
                    phoneNumber: document.getElementById('phoneNumber').value.trim(),
                    email: document.getElementById('email').value.trim(),
                    peripheralNodeUrl: document.getElementById('peripheralNodeUrl').value.trim()
                };

                console.log('Submitting clinic registration:', formData);

                // Call API
                const response = await apiCall('/admin/clinics', {
                    method: 'POST',
                    body: JSON.stringify(formData)
                });

                if (response) {
                    console.log('Clinic registered successfully:', response);

                    // Show success message
                    showSuccess(`Clínica "${response.clinicName}" registrada exitosamente con ID: ${response.clinicId}`);

                    // Display API key (only shown once!)
                    document.getElementById('apiKeyValue').textContent = response.apiKey;
                    document.getElementById('apiKeySection').classList.add('show');

                    // Clear form
                    document.getElementById('clinicRegistrationForm').reset();

                    // Scroll to API key section
                    setTimeout(() => {
                        document.getElementById('apiKeySection').scrollIntoView({
                            behavior: 'smooth',
                            block: 'center'
                        });
                    }, 100);
                }

            } catch (error) {
                console.error('Error registering clinic:', error);
                showError(error.message || 'Error al registrar clínica');
            } finally {
                // Re-enable submit button
                submitBtn.disabled = false;
                submitBtn.innerHTML = '✅ Registrar Clínica';
            }
        }

        /**
         * Copy API key to clipboard
         */
        function copyApiKey() {
            const apiKey = document.getElementById('apiKeyValue').textContent;

            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(apiKey).then(() => {
                    alert('API Key copiada al portapapeles');
                }).catch(err => {
                    console.error('Error copying to clipboard:', err);
                    fallbackCopyToClipboard(apiKey);
                });
            } else {
                fallbackCopyToClipboard(apiKey);
            }
        }

        /**
         * Fallback copy method for older browsers
         */
        function fallbackCopyToClipboard(text) {
            const textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.top = '0';
            textArea.style.left = '0';
            textArea.style.opacity = '0';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();

            try {
                document.execCommand('copy');
                alert('API Key copiada al portapapeles');
            } catch (err) {
                console.error('Error copying to clipboard:', err);
                alert('No se pudo copiar automáticamente. Por favor, copie manualmente.');
            }

            document.body.removeChild(textArea);
        }

        /**
         * Cancel registration and return to clinics list
         */
        function cancelRegistration() {
            if (confirm('¿Está seguro que desea cancelar el registro? Los datos ingresados se perderán.')) {
                window.location.href = '/hcen/admin/clinics.jsp';
            }
        }

        /**
         * Navigate to clinics list
         */
        function goToClinics() {
            window.location.href = '/hcen/admin/clinics.jsp';
        }

        /**
         * Initialize page on load
         */
        window.addEventListener('DOMContentLoaded', function() {
            console.log('Clinic registration page initializing...');

            // Check for token
            if (!getToken()) {
                showError('No se encontró sesión. Redirigiendo...');
                setTimeout(() => {
                    window.location.href = '/hcen/login-admin.jsp';
                }, 2000);
                return;
            }

            // Focus first field
            document.getElementById('clinicName').focus();
        });
    </script>
</body>
</html>
