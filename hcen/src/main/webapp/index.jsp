<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Portal de acceso al Sistema Nacional de Historia Clínica Electrónica de Uruguay">
    <title>HCEN - Historia Clínica Electrónica Nacional</title>

    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-T3c6CoIi6uLrA9TneNEoa7RxnatzjcDSCmG1MXxSR1GAsXEV/Dwwykc2MPK8M2HN" crossorigin="anonymous">

    <!-- Font Awesome 6 CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" integrity="sha512-z3gLpd7yknf1YoNbCzqRKc4qyor8gaKU1qmn+CShxbuBusANI9QpRohGBreCFkKxLhei6S9CQXFEbbKuqLg0DA==" crossorigin="anonymous" referrerpolicy="no-referrer" />

    <style>
        :root {
            --hcen-primary: #0066cc;
            --hcen-secondary: #004d99;
            --hcen-accent: #00a3cc;
            --hcen-success: #28a745;
            --hcen-light: #f8f9fa;
            --hcen-white: #ffffff;
            --card-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            --card-shadow-hover: 0 8px 16px rgba(0, 0, 0, 0.2);
        }

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
            flex-direction: column;
            overflow-x: hidden;
        }

        .main-container {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem 1rem;
        }

        .content-wrapper {
            width: 100%;
            max-width: 900px;
        }

        .header-section {
            text-align: center;
            margin-bottom: 3rem;
            animation: fadeInDown 0.8s ease-out;
        }

        .logo-container {
            margin-bottom: 1.5rem;
        }

        .logo-icon {
            width: 100px;
            height: 100px;
            background: var(--hcen-white);
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            box-shadow: var(--card-shadow-hover);
            margin-bottom: 1rem;
        }

        .logo-icon i {
            font-size: 3rem;
            color: var(--hcen-primary);
        }

        .header-section h1 {
            color: var(--hcen-white);
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
        }

        .header-section p {
            color: rgba(255, 255, 255, 0.95);
            font-size: 1.2rem;
            font-weight: 300;
            margin-bottom: 0;
        }

        .login-cards-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 2rem;
            animation: fadeInUp 0.8s ease-out 0.2s both;
        }

        .login-card {
            background: var(--hcen-white);
            border-radius: 20px;
            padding: 2.5rem 2rem;
            text-align: center;
            box-shadow: var(--card-shadow);
            transition: all 0.3s ease;
            text-decoration: none;
            color: inherit;
            display: block;
            position: relative;
            overflow: hidden;
        }

        .login-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 5px;
            background: linear-gradient(90deg, var(--card-accent-color), var(--card-accent-color-light));
            transform: scaleX(0);
            transition: transform 0.3s ease;
        }

        .login-card:hover::before {
            transform: scaleX(1);
        }

        .login-card:hover {
            transform: translateY(-10px) scale(1.02);
            box-shadow: var(--card-shadow-hover);
        }

        .login-card.admin-card {
            --card-accent-color: #0066cc;
            --card-accent-color-light: #3399ff;
        }

        .login-card.patient-card {
            --card-accent-color: #28a745;
            --card-accent-color-light: #5cb85c;
        }

        .card-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 1.5rem;
            transition: all 0.3s ease;
        }

        .admin-card .card-icon {
            background: linear-gradient(135deg, #0066cc, #3399ff);
        }

        .patient-card .card-icon {
            background: linear-gradient(135deg, #28a745, #5cb85c);
        }

        .login-card:hover .card-icon {
            transform: scale(1.1) rotate(5deg);
        }

        .card-icon i {
            font-size: 2.5rem;
            color: var(--hcen-white);
        }

        .card-title {
            font-size: 1.75rem;
            font-weight: 700;
            margin-bottom: 1rem;
            color: #333;
        }

        .card-description {
            font-size: 1rem;
            color: #666;
            margin-bottom: 1.5rem;
            line-height: 1.6;
        }

        .card-button {
            display: inline-block;
            padding: 0.75rem 2rem;
            border-radius: 50px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            transition: all 0.3s ease;
            border: 2px solid transparent;
        }

        .admin-card .card-button {
            background: linear-gradient(135deg, #0066cc, #3399ff);
            color: var(--hcen-white);
        }

        .patient-card .card-button {
            background: linear-gradient(135deg, #28a745, #5cb85c);
            color: var(--hcen-white);
        }

        .login-card:hover .card-button {
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }

        .footer {
            background: rgba(0, 0, 0, 0.2);
            color: var(--hcen-white);
            text-align: center;
            padding: 1.5rem 1rem;
            margin-top: auto;
        }

        .footer p {
            margin: 0;
            font-size: 0.95rem;
        }

        .footer a {
            color: var(--hcen-white);
            text-decoration: none;
            font-weight: 600;
            transition: opacity 0.3s ease;
        }

        .footer a:hover {
            opacity: 0.8;
        }

        /* Accessibility improvements */
        .login-card:focus {
            outline: 3px solid #ffbf47;
            outline-offset: 3px;
        }

        /* Animations */
        @keyframes fadeInDown {
            from {
                opacity: 0;
                transform: translateY(-30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* Responsive adjustments */
        @media (max-width: 768px) {
            .header-section h1 {
                font-size: 2rem;
            }

            .header-section p {
                font-size: 1rem;
            }

            .login-cards-container {
                grid-template-columns: 1fr;
                gap: 1.5rem;
            }

            .login-card {
                padding: 2rem 1.5rem;
            }

            .logo-icon {
                width: 80px;
                height: 80px;
            }

            .logo-icon i {
                font-size: 2.5rem;
            }
        }

        @media (max-width: 480px) {
            .header-section h1 {
                font-size: 1.5rem;
            }

            .card-title {
                font-size: 1.5rem;
            }

            .card-icon {
                width: 70px;
                height: 70px;
            }

            .card-icon i {
                font-size: 2rem;
            }
        }

        /* Print styles */
        @media print {
            body {
                background: white;
            }

            .footer {
                background: transparent;
                color: black;
            }
        }
    </style>
</head>
<body>
    <div class="main-container">
        <div class="content-wrapper">
            <!-- Header Section -->
            <header class="header-section">
                <div class="logo-container">
                    <div class="logo-icon" role="img" aria-label="Logo HCEN">
                        <i class="fas fa-hospital-user"></i>
                    </div>
                </div>
                <h1>HCEN</h1>
                <p>Historia Clínica Electrónica Nacional</p>
            </header>

            <!-- Login Cards -->
            <main class="login-cards-container">
                <!-- Admin Login Card -->
                <a href="${pageContext.request.contextPath}/login-admin.jsp"
                   class="login-card admin-card"
                   role="button"
                   aria-label="Acceder como Administrador del sistema HCEN">
                    <div class="card-icon" aria-hidden="true">
                        <i class="fas fa-user-shield"></i>
                    </div>
                    <h2 class="card-title">Administrador</h2>
                    <p class="card-description">
                        Acceso para personal administrativo del HCEN. Gestión del sistema y configuración.
                    </p>
                    <span class="card-button">Ingresar</span>
                </a>

                <!-- Patient Login Card -->
                <a href="${pageContext.request.contextPath}/login-patient.jsp"
                   class="login-card patient-card"
                   role="button"
                   aria-label="Acceder como Usuario de Salud al sistema HCEN">
                    <div class="card-icon" aria-hidden="true">
                        <i class="fas fa-heartbeat"></i>
                    </div>
                    <h2 class="card-title">Usuario de Salud</h2>
                    <p class="card-description">
                        Acceso para pacientes registrados en el sistema. Consulte su historia clínica.
                    </p>
                    <span class="card-button">Ingresar</span>
                </a>
            </main>
        </div>
    </div>

    <!-- Footer -->
    <footer class="footer" role="contentinfo">
        <p>
            &copy; 2025 <strong>HCEN</strong> - Historia Clínica Electrónica Nacional |
            República Oriental del Uruguay
        </p>
    </footer>

    <!-- Bootstrap 5 JS Bundle (optional, for future enhancements) -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-C6RzsynM9kWDrMNeT87bh95OGNyZPhcTNXj1NW7RuBCsyN/o0jlpcV8Qyq46cDfL" crossorigin="anonymous"></script>
</body>
</html>
