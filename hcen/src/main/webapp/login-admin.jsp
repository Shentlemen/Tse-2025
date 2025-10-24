<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title>HCEN - Portal Administrativo</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 50%, #1a1a1a 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
            position: relative;
            overflow: hidden;
        }

        /* Animated background pattern */
        body::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background-image:
                repeating-linear-gradient(45deg, transparent, transparent 35px, rgba(139, 0, 0, 0.03) 35px, rgba(139, 0, 0, 0.03) 70px);
            animation: slidePattern 30s linear infinite;
            z-index: 0;
        }

        @keyframes slidePattern {
            0% {
                transform: translateX(0) translateY(0);
            }
            100% {
                transform: translateX(70px) translateY(70px);
            }
        }

        .login-container {
            background: #ffffff;
            padding: 50px 45px;
            border-radius: 12px;
            box-shadow:
                0 10px 40px rgba(0, 0, 0, 0.3),
                0 0 0 1px rgba(139, 0, 0, 0.1);
            text-align: center;
            max-width: 480px;
            width: 100%;
            position: relative;
            z-index: 1;
            animation: fadeIn 0.5s ease-out;
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

        .header-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            background: linear-gradient(135deg, #8B0000 0%, #cc3300 100%);
            color: white;
            padding: 8px 18px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 25px;
            box-shadow: 0 4px 12px rgba(139, 0, 0, 0.2);
        }

        .header-badge::before {
            content: '🔒';
            font-size: 14px;
        }

        .logo-container {
            margin-bottom: 25px;
        }

        .logo {
            width: 140px;
            height: 140px;
            margin: 0 auto;
            background: linear-gradient(135deg, #8B0000 0%, #cc3300 100%);
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 4px 12px rgba(139, 0, 0, 0.2);
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
            font-weight: 700;
            margin-bottom: 12px;
            letter-spacing: -0.5px;
        }

        .subtitle {
            color: #555555;
            font-size: 15px;
            line-height: 1.6;
            margin-bottom: 35px;
            font-weight: 400;
        }

        .info-box {
            background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 100%);
            border-left: 4px solid #8B0000;
            padding: 16px;
            margin-bottom: 30px;
            border-radius: 6px;
            text-align: left;
        }

        .info-box-title {
            font-size: 13px;
            font-weight: 600;
            color: #8B0000;
            margin-bottom: 8px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .info-box-title::before {
            content: '⚠';
            font-size: 16px;
        }

        .info-box-text {
            font-size: 13px;
            color: #666666;
            line-height: 1.5;
        }

        .button-icon {
            width: 24px;
            height: 24px;
        }

        .login-button {
            background: linear-gradient(135deg, #8B0000 0%, #cc3300 100%);
            color: white;
            border: none;
            padding: 16px 35px;
            font-size: 16px;
            font-weight: 600;
            border-radius: 8px;
            cursor: pointer;
            width: 100%;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 14px rgba(139, 0, 0, 0.3);
            position: relative;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }

        .login-button::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
            transition: left 0.5s;
        }

        .login-button:hover::before {
            left: 100%;
        }

        .login-button:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(139, 0, 0, 0.4);
        }

        .login-button:active {
            transform: translateY(0);
            box-shadow: 0 3px 10px rgba(139, 0, 0, 0.3);
        }

        .login-button-icon {
            font-size: 20px;
        }

        .footer {
            margin-top: 30px;
            padding-top: 25px;
            border-top: 1px solid #e0e0e0;
        }

        .footer-text {
            font-size: 12px;
            color: #888888;
            line-height: 1.6;
            margin-bottom: 12px;
        }

        .footer-legal {
            font-size: 11px;
            color: #aaaaaa;
            line-height: 1.5;
        }

        .footer-legal a {
            color: #8B0000;
            text-decoration: none;
            transition: color 0.2s;
        }

        .footer-legal a:hover {
            color: #cc3300;
            text-decoration: underline;
        }

        .security-features {
            display: flex;
            justify-content: space-around;
            margin-top: 25px;
            padding-top: 20px;
            border-top: 1px solid #f0f0f0;
        }

        .security-feature {
            text-align: center;
            flex: 1;
        }

        .security-feature-icon {
            font-size: 24px;
            margin-bottom: 6px;
        }

        .security-feature-text {
            font-size: 10px;
            color: #999999;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-weight: 600;
        }

        /* Loading state */
        .login-button.loading {
            pointer-events: none;
            opacity: 0.8;
        }

        .login-button.loading::after {
            content: '';
            position: absolute;
            width: 16px;
            height: 16px;
            border: 2px solid #ffffff;
            border-top-color: transparent;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
            to {
                transform: rotate(360deg);
            }
        }

        /* Responsive design */
        @media (max-width: 600px) {
            .login-container {
                padding: 40px 30px;
            }

            h1 {
                font-size: 24px;
            }

            .subtitle {
                font-size: 14px;
            }

            .login-button {
                padding: 14px 28px;
                font-size: 15px;
            }

            .security-features {
                flex-direction: column;
                gap: 15px;
            }
        }

        /* High contrast mode support */
        @media (prefers-contrast: high) {
            .login-container {
                border: 2px solid #000000;
            }

            .login-button {
                border: 2px solid #ffffff;
            }
        }

        /* Reduced motion support */
        @media (prefers-reduced-motion: reduce) {
            *,
            *::before,
            *::after {
                animation-duration: 0.01ms !important;
                animation-iteration-count: 1 !important;
                transition-duration: 0.01ms !important;
            }
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="header-badge">
            Acceso Restringido
        </div>

        <div class="logo-container">
            <div class="logo">
                <span class="logo-text">HCEN</span>
            </div>
        </div>

        <h1>Portal Administrativo</h1>
        <p class="subtitle">Sistema de administración central de HCEN</p>

        <div class="info-box">
            <div class="info-box-title">Solo Personal Autorizado</div>
            <div class="info-box-text">
                Este portal es de acceso exclusivo para administradores del sistema HCEN.
                Todos los accesos son monitoreados y registrados conforme a las normativas de seguridad vigentes.
            </div>
        </div>

        <button class="login-button" onclick="initiateLogin()" id="loginButton">
            <svg class="button-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 5C13.66 5 15 6.34 15 8C15 9.66 13.66 11 12 11C10.34 11 9 9.66 9 8C9 6.34 10.34 5 12 5ZM12 19.2C9.5 19.2 7.29 17.92 6 15.98C6.03 13.99 10 12.9 12 12.9C13.99 12.9 17.97 13.99 18 15.98C16.71 17.92 14.5 19.2 12 19.2Z" fill="currentColor"/>
            </svg>
            <span>Ingresar con ID Uruguay</span>
        </button>

        <div class="security-features">
            <div class="security-feature">
                <div class="security-feature-icon">🔐</div>
                <div class="security-feature-text">Conexión Cifrada</div>
            </div>
            <div class="security-feature">
                <div class="security-feature-icon">📋</div>
                <div class="security-feature-text">Auditoría Total</div>
            </div>
            <div class="security-feature">
                <div class="security-feature-icon">✓</div>
                <div class="security-feature-text">Autenticación Nacional</div>
            </div>
        </div>

        <div class="footer">
            <p class="footer-text">
                Al acceder, declara estar autorizado para administrar el sistema HCEN
            </p>
            <p class="footer-legal">
                Acceso protegido por <a href="https://www.gub.uy/agencia-gobierno-electronico-sociedad-informacion-conocimiento/" target="_blank" rel="noopener noreferrer">AGESIC</a>
                | Cumple con <a href="https://www.gub.uy/unidad-reguladora-control-datos-personales/institucional/normativa/ley-ndeg-18331-proteccion-datos-personales-accion-habeas-data" target="_blank" rel="noopener noreferrer">Ley N° 18.331</a>
            </p>
        </div>
    </div>

    <script>
        /**
         * Initiates OAuth 2.0 authentication flow via gub.uy (ID Uruguay)
         * Redirects to HCEN backend which orchestrates the authorization flow
         */
        function initiateLogin() {
            const button = document.getElementById('loginButton');

            // Prevent double-clicks
            if (button.classList.contains('loading')) {
                return;
            }

            // Add loading state
            button.classList.add('loading');
            button.innerHTML = '<span>Redirigiendo...</span>';

            // Small delay for visual feedback
            setTimeout(() => {
                // Redirect to backend to initiate OAuth flow
                // Backend will generate state, build authorization URL, and redirect to gub.uy
                window.location.href = '/hcen/api/auth/login/initiate?clientType=WEB_ADMIN';
            }, 300);
        }

        // Optional: Allow Enter key to trigger login
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Enter') {
                initiateLogin();
            }
        });

        // Optional: Check for error/success query parameters
        window.addEventListener('DOMContentLoaded', function() {
            const urlParams = new URLSearchParams(window.location.search);
            const error = urlParams.get('error');
            const errorDescription = urlParams.get('error_description');

            if (error) {
                // Display error message (you can enhance this with a proper error UI)
                const errorMessage = errorDescription || 'Error durante la autenticación. Por favor intente nuevamente.';
                alert('Error de autenticación: ' + errorMessage);

                // Clear error from URL
                window.history.replaceState({}, document.title, window.location.pathname);
            }
        });
    </script>
</body>
</html>
