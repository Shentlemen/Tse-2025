<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Portal del Paciente - Historia Clínica Electrónica Nacional de Uruguay">
    <meta name="author" content="HCEN - AGESIC Uruguay">
    <title>HCEN - Portal del Paciente</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .login-container {
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
            padding: 48px 40px;
            max-width: 480px;
            width: 100%;
            text-align: center;
            animation: fadeIn 0.5s ease-in-out;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(-20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .logo-container {
            margin-bottom: 32px;
        }

        .logo {
            width: 120px;
            height: 120px;
            margin: 0 auto;
            background: linear-gradient(135deg, #0066cc 0%, #004999 100%);
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 16px rgba(0, 102, 204, 0.3);
        }

        .logo-text {
            color: white;
            font-size: 42px;
            font-weight: bold;
            letter-spacing: 2px;
        }

        h1 {
            color: #1a1a1a;
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 12px;
            line-height: 1.2;
        }

        .subtitle {
            color: #666666;
            font-size: 16px;
            line-height: 1.6;
            margin-bottom: 32px;
        }

        .info-box {
            background: #f0f7ff;
            border-left: 4px solid #0066cc;
            padding: 16px;
            margin-bottom: 32px;
            border-radius: 4px;
            text-align: left;
        }

        .info-box p {
            color: #333333;
            font-size: 14px;
            line-height: 1.6;
            margin: 0;
        }

        .info-box strong {
            color: #0066cc;
        }

        .login-button {
            background: linear-gradient(135deg, #0066cc 0%, #004999 100%);
            color: white;
            border: none;
            padding: 16px 32px;
            font-size: 18px;
            font-weight: 600;
            border-radius: 8px;
            cursor: pointer;
            width: 100%;
            transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(0, 102, 204, 0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
        }

        .login-button:hover {
            background: linear-gradient(135deg, #0052a3 0%, #003366 100%);
            box-shadow: 0 6px 16px rgba(0, 102, 204, 0.4);
            transform: translateY(-2px);
        }

        .login-button:active {
            transform: translateY(0);
            box-shadow: 0 2px 8px rgba(0, 102, 204, 0.3);
        }

        .login-button:disabled {
            background: #cccccc;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        .button-icon {
            width: 24px;
            height: 24px;
        }

        .loading-spinner {
            display: none;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-top-color: white;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .footer {
            margin-top: 32px;
            padding-top: 24px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-text {
            color: #999999;
            font-size: 13px;
            line-height: 1.6;
            margin-bottom: 16px;
        }

        .footer-links {
            display: flex;
            justify-content: center;
            gap: 24px;
            flex-wrap: wrap;
        }

        .footer-links a {
            color: #0066cc;
            text-decoration: none;
            font-size: 13px;
            transition: color 0.2s ease;
        }

        .footer-links a:hover {
            color: #004999;
            text-decoration: underline;
        }

        .security-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: #e8f5e9;
            color: #2e7d32;
            padding: 8px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            margin-top: 16px;
        }

        .security-icon {
            width: 16px;
            height: 16px;
        }

        /* Responsive Design */
        @media (max-width: 600px) {
            .login-container {
                padding: 36px 24px;
            }

            h1 {
                font-size: 24px;
            }

            .subtitle {
                font-size: 14px;
            }

            .login-button {
                padding: 14px 24px;
                font-size: 16px;
            }

            .logo {
                width: 100px;
                height: 100px;
            }

            .logo-text {
                font-size: 36px;
            }
        }

        /* Error message styling */
        .error-message {
            display: none;
            background: #ffebee;
            border-left: 4px solid #d32f2f;
            padding: 12px;
            margin-bottom: 24px;
            border-radius: 4px;
            color: #c62828;
            font-size: 14px;
            text-align: left;
        }

        .error-message.show {
            display: block;
            animation: slideDown 0.3s ease-out;
        }

        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <!-- Logo -->
        <div class="logo-container">
            <div class="logo">
                <span class="logo-text">HC</span>
            </div>
        </div>

        <!-- Header -->
        <h1>Portal del Paciente</h1>
        <p class="subtitle">Acceda de forma segura a su Historia Clínica Electrónica Nacional</p>

        <!-- Information Box -->
        <div class="info-box">
            <p>
                <strong>Autenticación Segura:</strong>
                Utilizamos ID Uruguay para garantizar la máxima seguridad en el acceso a su información médica.
            </p>
        </div>

        <!-- Error Message (hidden by default) -->
        <div id="errorMessage" class="error-message">
            <strong>Error:</strong> <span id="errorText">No se pudo iniciar sesión. Por favor, inténtelo nuevamente.</span>
        </div>

        <!-- Login Button -->
        <button class="login-button" id="loginButton" onclick="initiateLogin()">
            <svg class="button-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 5C13.66 5 15 6.34 15 8C15 9.66 13.66 11 12 11C10.34 11 9 9.66 9 8C9 6.34 10.34 5 12 5ZM12 19.2C9.5 19.2 7.29 17.92 6 15.98C6.03 13.99 10 12.9 12 12.9C13.99 12.9 17.97 13.99 18 15.98C16.71 17.92 14.5 19.2 12 19.2Z" fill="currentColor"/>
            </svg>
            <span id="buttonText">Ingresar con ID Uruguay</span>
            <div class="loading-spinner" id="loadingSpinner"></div>
        </button>

        <!-- Security Badge -->
        <div class="security-badge">
            <svg class="security-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 1L3 5V11C3 16.55 6.84 21.74 12 23C17.16 21.74 21 16.55 21 11V5L12 1ZM10 17L6 13L7.41 11.59L10 14.17L16.59 7.58L18 9L10 17Z" fill="currentColor"/>
            </svg>
            Conexión segura verificada
        </div>

        <!-- Footer -->
        <div class="footer">
            <p class="footer-text">
                Al ingresar, acepta nuestra política de privacidad y el uso responsable de sus datos clínicos.
            </p>
            <div class="footer-links">
                <a href="/hcen/terminos" target="_blank">Términos y Condiciones</a>
                <a href="/hcen/privacidad" target="_blank">Política de Privacidad</a>
                <a href="/hcen/ayuda" target="_blank">Ayuda</a>
            </div>
        </div>
    </div>

    <script>
        /**
         * Initiates the OAuth 2.0 login flow with gub.uy (ID Uruguay)
         * Redirects to HCEN backend which then redirects to gub.uy authorization endpoint
         */
        function initiateLogin() {
            const button = document.getElementById('loginButton');
            const buttonText = document.getElementById('buttonText');
            const spinner = document.getElementById('loadingSpinner');
            const errorMessage = document.getElementById('errorMessage');

            // Hide any previous error messages
            errorMessage.classList.remove('show');

            // Disable button and show loading state
            button.disabled = true;
            buttonText.textContent = 'Conectando...';
            spinner.style.display = 'inline-block';

            try {
                // Redirect to HCEN authentication endpoint
                // The backend will handle OAuth flow initiation and redirect to gub.uy
                window.location.href = '/hcen/api/auth/login/initiate?clientType=WEB_PATIENT';
            } catch (error) {
                // Re-enable button on error
                button.disabled = false;
                buttonText.textContent = 'Ingresar con ID Uruguay';
                spinner.style.display = 'none';

                // Show error message
                document.getElementById('errorText').textContent =
                    'No se pudo iniciar la sesión. Verifique su conexión e intente nuevamente.';
                errorMessage.classList.add('show');

                console.error('Login initiation error:', error);
            }
        }

        /**
         * Check for error messages in URL parameters (from OAuth callback failures)
         */
        window.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            const error = urlParams.get('error');
            const errorDescription = urlParams.get('error_description');

            if (error) {
                const errorMessage = document.getElementById('errorMessage');
                const errorText = document.getElementById('errorText');

                // Map common OAuth errors to user-friendly messages
                const errorMessages = {
                    'access_denied': 'Acceso denegado. No autorizó el acceso a su información.',
                    'invalid_request': 'Solicitud inválida. Por favor, inténtelo nuevamente.',
                    'server_error': 'Error del servidor. Intente nuevamente en unos momentos.',
                    'temporarily_unavailable': 'Servicio temporalmente no disponible. Intente más tarde.'
                };

                errorText.textContent = errorMessages[error] ||
                    (errorDescription || 'Error de autenticación. Por favor, inténtelo nuevamente.');
                errorMessage.classList.add('show');

                // Clean URL (remove error parameters) without reloading
                const cleanUrl = window.location.pathname;
                window.history.replaceState({}, document.title, cleanUrl);
            }
        });

        /**
         * Accessibility: Allow Enter key to trigger login
         */
        document.addEventListener('keypress', function(event) {
            if (event.key === 'Enter' && !document.getElementById('loginButton').disabled) {
                initiateLogin();
            }
        });
    </script>
</body>
</html>
