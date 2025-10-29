package uy.gub.clinic.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet de prueba para diagnosticar problemas con ProfessionalServlet
 */
@WebServlet("/admin/test-professionals")
public class TestProfessionalServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<html>");
        out.println("<head><title>Test Professional Servlet</title></head>");
        out.println("<body>");
        out.println("<h1>Test Professional Servlet Funcionando</h1>");
        out.println("<p>El servlet de profesionales está funcionando correctamente.</p>");
        out.println("<p>Context Path: " + request.getContextPath() + "</p>");
        out.println("<p>Request URI: " + request.getRequestURI() + "</p>");
        out.println("</body>");
        out.println("</html>");
    }
}
