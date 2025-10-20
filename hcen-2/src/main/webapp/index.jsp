<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HCEN - Historia Clínica Electrónica Nacional</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        h1 {
            color: #0066cc;
        }
        .portal-links {
            margin-top: 30px;
        }
        .portal-links a {
            display: block;
            margin: 10px 0;
            padding: 15px;
            background-color: #f0f0f0;
            text-decoration: none;
            color: #333;
            border-radius: 5px;
        }
        .portal-links a:hover {
            background-color: #e0e0e0;
        }
    </style>
</head>
<body>
    <h1>HCEN - Historia Clínica Electrónica Nacional</h1>
    <p>Bienvenido al sistema de Historia Clínica Electrónica Nacional de Uruguay</p>

    <div class="portal-links">
        <h2>Portales</h2>
        <a href="admin/">Portal Administrador HCEN</a>
        <a href="usuario/">Portal Usuarios de Salud</a>
        <a href="api/health">API Health Check</a>
    </div>

    <hr>
    <p><small>Version 1.0.0-SNAPSHOT | Grupo 9 TSE 2025</small></p>
</body>
</html>
