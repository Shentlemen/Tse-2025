package newpackage;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class TestServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Prestador de Salud - Test</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>¡Funciona! 🎉</h1>");
            out.println("<p>Tu aplicación web está funcionando correctamente con WildFly</p>");
            out.println("<p><strong>Servidor:</strong> WildFly</p>");
            out.println("<p><strong>Java:</strong> " + System.getProperty("java.version") + "</p>");
            out.println("<p><strong>Fecha:</strong> " + new java.util.Date() + "</p>");
            out.println("<p><strong>Paquete:</strong> newpackage</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
