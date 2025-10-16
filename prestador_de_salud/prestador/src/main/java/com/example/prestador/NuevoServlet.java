package com.example.prestador;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/nuevo-servlet")
public class NuevoServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Nuevo Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>🆕 Nuevo Servlet Creado</h1>");
            out.println("<p>Este servlet fue creado manualmente</p>");
            out.println("<p><strong>URL:</strong> /nuevo-servlet</p>");
            out.println("<p><strong>Método:</strong> GET</p>");
            out.println("<p><strong>Paquete:</strong> com.example.prestador</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
