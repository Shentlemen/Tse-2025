<%--
    Document   : index
    Created on : 16 oct. 2025, 5:05:26 p. m.
    Author     : user
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Historia clinica</title>
    </head>
    <body>
        <h1>Buscar Historia clinica</h1>
        <form action="SvHistoriaclinica" method="POST">
            <p><label>codigo de historia clinica:</label><input type ="text" name="codigo"></p>
            <p><label>Cedula de identidad:</label><input type ="text" name="cedula"></p>
            <p><label>Nombres</label><input type="text" name="nombres"></p>
            <p><label>Apellidos</label><input type="text" name="apellidos"></p>
            <p><label>Especialista Encargado:</label><input type="text" name="especialista"></p>
            <button type ="submit">Enviar</button>
            
        </form>
        
    </body>
</html>
