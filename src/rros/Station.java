/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

/**
 *
 * @author PETER-PC
 */
public class Station {
    public static enum Type {MAINTENANCE, NORMAL};
    public final Type type;
    public static final double minConnectingTime = 3; //3 seconds
    private final ObservableList<Passenger> stationPassengers = FXCollections.observableArrayList();
    private final String label;
    private static final ArrayList<Station> STATIONS = new ArrayList<>();
    public static final int[] PASSENGERSPERSTATION = new int[18];
    
    public Station(String label, Type type){
        this.label = label;
        this.type =  type;
        STATIONS.add(this);
    }
    
    public static void STATIONSClassSequencer(){
        //create station objects from vertices
        createStationsFromGraph(Vertex.vertices);
        //assign random population of initial passengers at way STATIONS
//        populateStations();
        //generate tickets from populateStations()
//        Ticket.generateTickets(PASSENGERSPERSTATION);
        //create passenger objects from ticket.generateTickets()
        createPassengerObjects();
    }
            
    private static void populateStations(){
        int passengersPerStationLeft = 300;
        
        for(int i=0; i < 18; i++){
            if(i == 17)
                PASSENGERSPERSTATION[i] = passengersPerStationLeft;
            else{
                PASSENGERSPERSTATION[i] = (int) (1 + Math.random() * 25);
                //if the random number is too small increase it
                if(PASSENGERSPERSTATION[i] < 10)
                    PASSENGERSPERSTATION[i] += 10;
                
                passengersPerStationLeft -= PASSENGERSPERSTATION[i];
            }
            System.out.println("Population at Station("+i+"): "+PASSENGERSPERSTATION[i]);
        }
    }
    
    //assign users to their start STATIONS
    public static void createPassengerObjects(){
//        //reset passengers
//        if(!Passenger.PASSENGERS.isEmpty())
//            Passenger.PASSENGERS.clear();
//        
//        //clear passengers in every station
//        for(Station s: STATIONS){
//            if(!s.stationPassengers.isEmpty())
//                s.stationPassengers.clear();
//        }
        
        for(int i=0; i < PASSENGERSPERSTATION.length; i++){
            try{
                String queryString = "Select trip.username, profileImage, trip.startStation, trip.endStation FROM user, trip "
                        + "where startStation = ? and user.username = trip.username order by startStation";
                PreparedStatement statement = DBConnection.connection.prepareStatement(queryString);
                statement.setString(1, STATIONS.get(i).label);
                
                ResultSet rSet = statement.executeQuery();
                while(rSet.next()){
                    STATIONS.get(i).stationPassengers.add(new Passenger(rSet.getString("username"), 
                            new Image(rSet.getBlob("profileImage").getBinaryStream()), 
                            Station.getStation(rSet.getString("startStation")),
                            Station.getStation(rSet.getString("endStation"))));
//                    System.out.println("Username: "+rSet.getString("username")+", StartStation: "+rSet.getString("startStation"));
                }
                int size = STATIONS.get(i).stationPassengers.size();
                Label passengerStatus = Vertex.getVertex(STATIONS.get(i).getLabel()).getPassengerStatus();
                Platform.runLater(()->{passengerStatus.setText( size+ "|0");});
            }catch(SQLException ex){}

        }
        
    }
    
    public static Station getStation(String s){
        for(Station station: STATIONS){
            if(station.getLabel().matches(s))
                return station;
        }
        return null;
    }
    
    public static ArrayList<Station> getStations(){
        return STATIONS;
    }
    
    public ObservableList<Passenger> getStationPassengers(){
        return stationPassengers;
    }
    
    public String getLabel(){
        return label;
    }
    
    private static void createStationsFromGraph(ArrayList<Vertex> V){
        for(Vertex v: V){
            Station station;
            if(v instanceof DirPort)
                station = new Station(v.getName(), Type.MAINTENANCE);
            else
                station = new Station(v.getName(), Type.NORMAL);
        }
    }
    
    @Override
    public String toString(){
        return getLabel();
    }
}
