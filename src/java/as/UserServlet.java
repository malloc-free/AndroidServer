/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package as;

import beans.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.DataBase;

/**
 *
 * @author Michael Holmwood
 * 
 * Servlet used to authenticate users of the application.
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/UserServlet"})
public class UserServlet extends HttpServlet {
    //The DataBase object used by UserServlet.
    private static DataBase dataBase;
    
    static{  
        dataBase = DataBase.getInstance();
    };
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        
        //Check the action.
        String action = request.getParameter("action");
        
        if(action != null && action.equals("login")) {
            processLogin(request, response);
        }
        else {
            try (PrintWriter out = response.getWriter()) {
              out.println(dataBase.getMessage());
            }
        }
    }

    /**
     * Process login information. If it matches, then provide a cookie to be 
     * used for subsequent actions.
     * 
     * @param request
     * @param response
     * @throws IOException 
     */
    private void processLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String handle = request.getParameter("handle");
        String password = request.getParameter("password");
       
        //Check if parameters are supplied.
        if(handle != null && password != null) {
            //Get user if existitng.
            User user = dataBase.getUserByHandle(handle);
            try (PrintWriter out = response.getWriter()) {
                if(user.getUserId() != -1 && user.getUserPassword().equals(password)) {
                    out.println("accepted");
                    Cookie cookie = new Cookie("user_id", Integer.toString(user.getUserId()));
                    response.addCookie(cookie);
                }
                //Reject if no success.
                else {
                    out.println("rejected");
                }
            }
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
