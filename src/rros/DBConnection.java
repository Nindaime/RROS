/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author PETER-PC
 */
public class DBConnection {
    public static Connection connection;


    public DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");

            connection = DriverManager.getConnection("jdbc:mysql://localhost/rros", "root", "");
            System.out.println("Database Connection");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
}
