/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author PETER-PC
 */
public class Ticket {
    
    public Ticket(){}
    
    public static void generateTickets(int[] totalPassengers){
        clearTickets();
        int passengerIndex = 0;
        for(int i = 0; i < totalPassengers.length; i++){
            
            //add tickets to the DB sequencially
            for(int k = 0; k < totalPassengers[i]; k++){
                boolean loop = true; int endStationIndex = 0;
                // generate an end station that is not same as start station
                while(loop){
                    endStationIndex = (int) (Math.random() * 18);
                    //let i i.e. start station index be equal to end station index
                    if(endStationIndex != i)
                        loop = false;
                }
                try{
                    String queryString = "insert into trip set username = 'Username"+(passengerIndex++)+"', startStation = '"+Station.getStations().get(i)
                            +"', endStation = '"+Station.getStations().get(endStationIndex)+"'";
                    PreparedStatement preparedStatement = DBConnection.connection.prepareStatement(queryString);
                    preparedStatement.execute();
                }catch(SQLException ex){}
            }
        }
        
    }
    
    public static void clearTickets(){
        try{
            String queryString = "Delete FROM Trip";
            PreparedStatement statement = DBConnection.connection.prepareStatement(queryString);
            statement.execute();
            
            queryString = "Alter Table Trip Auto_increment = 0";
            statement = DBConnection.connection.prepareStatement(queryString);
            statement.execute();
        }catch(SQLException ex){}
    }
    
    
}
