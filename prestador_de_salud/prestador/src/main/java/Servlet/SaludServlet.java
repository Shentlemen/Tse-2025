package Servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/salud")
public class SaludServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Prestador de Salud</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>🏥 Prestador de Salud</h1>");
            out.println("<h2>Servicios Disponibles:</h2>");
            out.println("<ul>");
            out.println("<li>Consulta de Historia Clínica</li>");
            out.println("<li>Registro de Pacientes</li>");
            out.println("<li>Gestión de Citas</li>");
            out.println("</ul>");
            out.println("<p><strong>Servidor:</strong> WildFly 33</p>");
            out.println("<p><strong>Java:</strong> " + System.getProperty("java.version") + "</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
