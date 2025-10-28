package uy.gub.clinic.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet de prueba para diagnosticar problemas
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head><title>Test Servlet</title></head>");
        out.println("<body>");
        out.println("<h1>Test Servlet Funcionando</h1>");
        out.println("<p>El servlet está funcionando correctamente.</p>");
        out.println("<p>Context Path: " + request.getContextPath() + "</p>");
        out.println("<p>Request URI: " + request.getRequestURI() + "</p>");
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head><title>Test POST</title></head>");
        out.println("<body>");
        out.println("<h1>Test POST Funcionando</h1>");
        out.println("<p>Parámetros recibidos:</p>");
        out.println("<ul>");
        
        request.getParameterMap().forEach((key, values) -> {
            out.println("<li>" + key + ": " + String.join(", ", values) + "</li>");
        });
        
        out.println("</ul>");
        out.println("</body>");
        out.println("</html>");
    }
}
