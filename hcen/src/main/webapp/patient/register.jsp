<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro de Usuario - HCEN</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            max-width: 600px;
            width: 100%;
            padding: 40px;
        }

        .header {
            text-align: center;
            margin-bottom: 30px;
        }

        .header h1 {
            color: #333;
            font-size: 28px;
            margin-bottom: 8px;
        }

        .header p {
            color: #666;
            font-size: 14px;
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

        .form-group label .required {
            color: #e74c3c;
            margin-left: 4px;
        }

        .form-group input {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            font-size: 14px;
            transition: border-color 0.3s, box-shadow 0.3s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .form-group input.error {
            border-color: #e74c3c;
        }

        .form-group .error-message {
            color: #e74c3c;
            font-size: 12px;
            margin-top: 4px;
            display: none;
        }

        .form-group .error-message.show {
            display: block;
        }

        .form-group .help-text {
            color: #666;
            font-size: 12px;
            margin-top: 4px;
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .btn-container {
            display: flex;
            gap: 12px;
            margin-top: 30px;
        }

        .btn {
            flex: 1;
            padding: 14px 24px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .btn-primary:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
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
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .login-link {
            text-align: center;
            margin-top: 20px;
            color: #666;
            font-size: 14px;
        }

        .login-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
        }

        .login-link a:hover {
            text-decoration: underline;
        }

        .loading {
            display: inline-block;
            width: 16px;
            height: 16px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Registro de Usuario</h1>
            <p>Historia Clínica Electrónica Nacional</p>
        </div>

        <div id="alertSuccess" class="alert alert-success">
            <strong>¡Registro exitoso!</strong> Tu cuenta ha sido creada. Redirigiendo al perfil...
        </div>

        <div id="alertError" class="alert alert-error">
            <strong>Error:</strong> <span id="errorMessage"></span>
        </div>

        <form id="registerForm">
            <div class="form-group">
                <label>
                    Cédula de Identidad
                    <span class="required">*</span>
                </label>
                <input
                    type="text"
                    id="ci"
                    name="ci"
                    placeholder="12345678"
                    maxlength="8"
                    required
                >
                <div class="help-text">Ingrese su cédula sin puntos ni guiones</div>
                <div class="error-message" id="ciError">Por favor ingrese una cédula válida (8 dígitos)</div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>
                        Nombre
                        <span class="required">*</span>
                    </label>
                    <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        placeholder="Juan"
                        required
                    >
                    <div class="error-message" id="firstNameError">El nombre es requerido</div>
                </div>

                <div class="form-group">
                    <label>
                        Apellido
                        <span class="required">*</span>
                    </label>
                    <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        placeholder="Pérez"
                        required
                    >
                    <div class="error-message" id="lastNameError">El apellido es requerido</div>
                </div>
            </div>

            <div class="form-group">
                <label>
                    Fecha de Nacimiento
                    <span class="required">*</span>
                </label>
                <input
                    type="date"
                    id="dateOfBirth"
                    name="dateOfBirth"
                    required
                >
                <div class="error-message" id="dateOfBirthError">La fecha de nacimiento es requerida</div>
            </div>

            <div class="form-group">
                <label>Email</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="juan.perez@ejemplo.com"
                >
                <div class="error-message" id="emailError">Por favor ingrese un email válido</div>
            </div>

            <div class="form-group">
                <label>Teléfono</label>
                <input
                    type="tel"
                    id="phoneNumber"
                    name="phoneNumber"
                    placeholder="099123456"
                    maxlength="9"
                >
                <div class="help-text">Formato: 099123456 o 024123456</div>
                <div class="error-message" id="phoneNumberError">Formato de teléfono inválido</div>
            </div>

            <div class="btn-container">
                <button type="button" class="btn btn-secondary" onclick="window.location.href='/hcen/login-patient.jsp'">
                    Cancelar
                </button>
                <button type="submit" class="btn btn-primary" id="submitBtn">
                    Registrarse
                </button>
            </div>
        </form>

        <div class="login-link">
            ¿Ya tienes cuenta? <a href="/hcen/login-patient.jsp">Inicia sesión aquí</a>
        </div>
    </div>

    <script type="text/javascript">
    //<![CDATA[
        const form = document.getElementById('registerForm');
        const submitBtn = document.getElementById('submitBtn');
        const alertSuccess = document.getElementById('alertSuccess');
        const alertError = document.getElementById('alertError');
        const errorMessage = document.getElementById('errorMessage');

        // Validation functions
        function validateCI(ci) {
            return /^\d{7,8}$/.test(ci);
        }

        function validateEmail(email) {
            if (!email) return true; // Optional field
            return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
        }

        function validatePhone(phone) {
            if (!phone) return true; // Optional field
            return /^(09[1-9]|02[1-9])\d{6}$/.test(phone);
        }

        function showFieldError(fieldId, show) {
            const input = document.getElementById(fieldId);
            const error = document.getElementById(fieldId + 'Error');

            if (show) {
                input.classList.add('error');
                error.classList.add('show');
            } else {
                input.classList.remove('error');
                error.classList.remove('show');
            }
        }

        function validateForm() {
            let isValid = true;

            // Validate CI
            const ci = document.getElementById('ci').value.trim();
            if (!validateCI(ci)) {
                showFieldError('ci', true);
                isValid = false;
            } else {
                showFieldError('ci', false);
            }

            // Validate First Name
            const firstName = document.getElementById('firstName').value.trim();
            if (!firstName) {
                showFieldError('firstName', true);
                isValid = false;
            } else {
                showFieldError('firstName', false);
            }

            // Validate Last Name
            const lastName = document.getElementById('lastName').value.trim();
            if (!lastName) {
                showFieldError('lastName', true);
                isValid = false;
            } else {
                showFieldError('lastName', false);
            }

            // Validate Date of Birth
            const dateOfBirth = document.getElementById('dateOfBirth').value;
            if (!dateOfBirth) {
                showFieldError('dateOfBirth', true);
                isValid = false;
            } else {
                showFieldError('dateOfBirth', false);
            }

            // Validate Email (optional)
            const email = document.getElementById('email').value.trim();
            if (email && !validateEmail(email)) {
                showFieldError('email', true);
                isValid = false;
            } else {
                showFieldError('email', false);
            }

            // Validate Phone (optional)
            const phoneNumber = document.getElementById('phoneNumber').value.trim();
            if (phoneNumber && !validatePhone(phoneNumber)) {
                showFieldError('phoneNumber', true);
                isValid = false;
            } else {
                showFieldError('phoneNumber', false);
            }

            return isValid;
        }

        // Form submission
        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            // Hide alerts
            alertSuccess.classList.remove('show');
            alertError.classList.remove('show');

            // Validate form
            if (!validateForm()) {
                return;
            }

            // Disable submit button
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="loading"></span> Registrando...';

            // Prepare data
            const formData = {
                ci: document.getElementById('ci').value.trim(),
                firstName: document.getElementById('firstName').value.trim(),
                lastName: document.getElementById('lastName').value.trim(),
                dateOfBirth: document.getElementById('dateOfBirth').value,
                email: document.getElementById('email').value.trim() || null,
                phoneNumber: document.getElementById('phoneNumber').value.trim() || null
            };

            try {
                const response = await fetch('/hcen/api/inus/users', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData)
                });

                if (response.ok) {
                    const data = await response.json();

                    // Show success message
                    alertSuccess.classList.add('show');

                    // Redirect to profile after 2 seconds
                    setTimeout(function() {
                        window.location.href = '/hcen/patient/profile.jsp?ci=' + encodeURIComponent(formData.ci);
                    }, 2000);
                } else {
                    const errorData = await response.json();
                    errorMessage.textContent = errorData.message || 'No se pudo completar el registro';
                    alertError.classList.add('show');

                    // Re-enable submit button
                    submitBtn.disabled = false;
                    submitBtn.textContent = 'Registrarse';
                }
            } catch (error) {
                console.error('Error:', error);
                errorMessage.textContent = 'Error de conexión. Por favor intente nuevamente.';
                alertError.classList.add('show');

                // Re-enable submit button
                submitBtn.disabled = false;
                submitBtn.textContent = 'Registrarse';
            }
        });

        // Real-time validation
        document.getElementById('ci').addEventListener('blur', function() {
            const ci = this.value.trim();
            if (ci) {
                showFieldError('ci', !validateCI(ci));
            }
        });

        document.getElementById('email').addEventListener('blur', function() {
            const email = this.value.trim();
            if (email) {
                showFieldError('email', !validateEmail(email));
            }
        });

        document.getElementById('phoneNumber').addEventListener('blur', function() {
            const phone = this.value.trim();
            if (phone) {
                showFieldError('phoneNumber', !validatePhone(phone));
            }
        });
    //]]>
    </script>
</body>
</html>
