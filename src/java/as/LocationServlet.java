/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package as;

import beans.Location;
import beans.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
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
 * This servlet handles all of the location related requests from the app, 
 * which is just currently adding a location and fetching a location from a
 * reference.
 */
@WebServlet(name = "LocationServlet", urlPatterns = {"/LocationServlet"})
public class LocationServlet extends HttpServlet {

    //The DataBase object used by the servlet.
    private static DataBase dataBase;
   
    static {
        dataBase = DataBase.getInstance();
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods. There are two actions used in this servlet: submitnew and 
     * sumbitid.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        int userId = -1;
        
        //First obtain cookie information.
        for(Cookie c : request.getCookies()) {
            if("user_id".equals(c.getName())) {
                userId = Integer.parseInt(c.getValue());
                break;
            }
        }
        
        //If the cookie has valid informaiton and this provides a valid
        //user, then proceed.
        User user;
        if(userId != -1 && (user = dataBase.getUserById(userId)).getUserId() != -1) {
           
            switch(request.getParameter("action")) {
                case "submitnew" : createLocation(request, response, userId); break;
                case "submitid" : fetchLocation(request, response); break;
            } 
        }
        //Not a user? Then fail the request.
        else {
            try(PrintWriter out = response.getWriter()) {
                out.println("failed");
            }
        }
        
    }
    
    /**
     * Adds a new location to the database, and returns a reference for it
     * to the requesting application.
     * 
     * @param request
     * @param response
     * @param userId - userId of the sender.
     * @throws IOException 
     */
    private void createLocation(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        //Get supplied parameters
        String lat = request.getParameter("latitude");
        String lng = request.getParameter("longitude");
        String message = "failed";
        
        //Test that required parmeters are present.
        if(lat != null && lng != null) {
            Random random = new Random();
            Location location = new Location();
            location.setLatitude(Double.parseDouble(lat));
            location.setLongitude(Double.parseDouble(lng));
            location.setUserId(userId);
            //Generate random reference for the location.
            location.setLocationId(random.nextInt(1000000));
            
            //Store in the database.
            if(dataBase.addLocation(location)){
                message = Integer.toString(location.getLocationId());
            }
            
            try (PrintWriter out = response.getWriter()) {
                out.println(message);
            }
        }
    }
    
    /**
     * Fetches a location based on the supplied reference.
     * 
     * @param request
     * @param response
     * @throws IOException 
     */
    private void fetchLocation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            //Check if parameter is available. If not, fail the request.
            String id = request.getParameter("id");
            if(id != null) {
                Location location = dataBase.getLocation(Integer.parseInt(id));

                if(location.getLocationId() > 0){
                    User user = dataBase.getUserById(location.getUserId());

                    out.println(location.getLatitude());
                    out.println(location.getLongitude());
                    out.println(user.getUserName());
                }
                else{
                    out.println("failed");
                }
            }
            else {
                out.println("failed");
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
