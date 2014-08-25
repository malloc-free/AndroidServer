/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import beans.Location;
import beans.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Holmwood
 * 
 * Handles all of the database transactions for the servlets.
 */
public class DataBase {
    //The connection to the database.
    private Connection conn;
    //The name of the userTable.
    private String userTableName;
    //The name of the locationTable.
    private String locationTableName;
    //Shows if the database is currently connected.
    private boolean isConnected;
    //Set if an exception has been thrown.
    private String exMessage;
    
    //Singleton Object.
    private static DataBase dataBase = null;
    
    //Prepared staements.
    private static PreparedStatement getUserByHandle;
    private static PreparedStatement getUserById;
    private static PreparedStatement addLocation;
    private static PreparedStatement getLocation;
    
    /**
     * Constructor for DataBase.
     */
    private DataBase() {
        isConnected = false;
        exMessage = "All fine";
        //Attempt to read XML, and connect to the database.
        try { 
            // connect to the database
            Properties properties = new Properties();
            properties.loadFromXML(getClass().getResourceAsStream("DataBase.xml"));
            String dbDriver = properties.get("dbDriver").toString();
            Class.forName(dbDriver);
            String dbUrl = properties.get("dbUrl").toString();
            String userName = properties.get("user").toString();
            String password = properties.get("password").toString();
            conn = DriverManager.getConnection(dbUrl, userName, password);
            //Set table names
            userTableName = properties.getProperty("dbUserTableName");
            locationTableName = properties.getProperty("dbLocationTableName");
            isConnected = true;
            createStatements();
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            exMessage = ex.getMessage() +
                    ((ex instanceof ClassNotFoundException) ? "CNF" : "IO");
        }    
    }
    
    /**
     * Create the prepared statements.
     */
    private void createStatements() {
        try {
            String stmt = "SELECT * FROM " + userTableName +
                    " WHERE user_handle = ?";
            getUserByHandle = conn.prepareStatement(stmt);
            
            stmt = "SELECT * FROM " + userTableName +
                    " WHERE user_id = ?";
            getUserById = conn.prepareStatement(stmt);
            
            stmt = "INSERT INTO " + locationTableName + 
                    " VALUES(?, ?, ?, ?)";
            addLocation = conn.prepareStatement(stmt);
            
            stmt = "SELECT * FROM " + locationTableName +
                    " WHERE location_id = ?";
            
            getLocation = conn.prepareStatement(stmt);
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Shows if the database connection succeeded.
     * @return 
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Get an exception message, if there is one.
     * @return 
     */
    public String getMessage() {
        return exMessage;
    }
    
    /**
     * Get the singleton instance for the DataBase.
     * @return 
     */
    public static DataBase getInstance() {
        if(dataBase == null) {
            dataBase = new DataBase();
        }
        
        return dataBase;
    }
    
    /**
     * Get the user by the supplied handle.
     * @param userHandle - The handle for the user to get.
     * @return - The user if found, or an object with null values.
     */
    public User getUserByHandle(String userHandle) {
        User user = new User();
        
        try {
            getUserByHandle.clearParameters();
            ResultSet rs;
            
            synchronized(this) {
                getUserByHandle.setString(1, userHandle);
            
                rs = getUserByHandle.executeQuery();
            }
            if(rs.next()) {
                user.setUserHandle(rs.getString("user_handle"));
                user.setUserId(rs.getInt("user_id"));
                user.setUserName(rs.getString("user_name"));
                user.setUserNumber(rs.getString("user_number"));
                user.setUserPassword(rs.getString("user_password"));
            }
        } catch (SQLException ex) {
            exMessage = ex.getMessage();
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return user;
    }
    
    /**
     * Get the user from the supplied id, if it exists.
     * 
     * @param id - The id of the user to get.
     * @return - The requested user, or an object with null values.
     */
    public User getUserById(int id) {
        User user = new User();

        try {
            ResultSet rs;
            getUserById.clearParameters();
            synchronized(this) {
                getUserById.setInt(1, id);
                rs = getUserById.executeQuery();    
            }
            
            if(rs.next()) {
                user.setUserHandle(rs.getString("user_handle"));
                user.setUserId(rs.getInt("user_id"));
                user.setUserName(rs.getString("user_name"));
                user.setUserNumber(rs.getString("user_number"));
                user.setUserPassword(rs.getString("user_password"));
            }
        } catch (SQLException ex) {
            exMessage = ex.getMessage();
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }
    
    /**
     * Add the specified location to the database.
     * 
     * @param location - The location to add.
     * @return - True if the addition was successful.
     */
    public boolean addLocation(Location location) {
        boolean success = false;
        
        try {
            synchronized(this) {
                addLocation.setInt(1, location.getLocationId());
                addLocation.setInt(2, location.getUserId());
                addLocation.setDouble(3, location.getLatitude());
                addLocation.setDouble(4, location.getLongitude());
                addLocation.execute();
            }
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return success;
    }
    
    /**
     * Get a location using the supplied reference.
     * 
     * @param locationId - The id of the location to find.
     * @return - The requested location, or an object with null values if not found.
     */
    public Location getLocation(int locationId) {
        Location location = new Location();
        
        try {
            ResultSet rs;
            synchronized(this) {
                getLocation.setInt(1, locationId);
                rs = getLocation.executeQuery();
            }
            
            if(rs.next()) {
                location.setUserId(rs.getInt("user_id"));
                location.setLocationId(rs.getInt("location_id"));
                location.setLatitude(rs.getDouble("latitude"));
                location.setLongitude(rs.getDouble("longitude"));
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return location;
    }
}
