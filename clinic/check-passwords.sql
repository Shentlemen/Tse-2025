-- Verificar contraseñas en la base de datos
SELECT username, password, LENGTH(password) as longitud_password
FROM users 
WHERE active = true
ORDER BY username;
