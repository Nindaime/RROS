/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author PETER-PC
 */
public class Passenger {
    private final String username;
    private final Image profileImage;
    private final Station startStation, endStation;
    private Trip trip;
    public final static ObservableList<Passenger> PASSENGERS = FXCollections.observableArrayList();
    private int animCycleBeforeTripStart = 0, animCycleToTripEnd = 0;
    public enum Status {waiting, enroute, arrived}
    private Status status;
    
    public Passenger(String username, Image image, Station startStation, Station endStation){
        this.username = username;
        profileImage = image;
        this.startStation = startStation;
        this.endStation = endStation;
        trip = null;
        status = Status.waiting;
        PASSENGERS.add(this);
    }
    
    public static void resetPassengers(TCP1 t){
        for(Passenger p: PASSENGERS){
            p.trip = new Trip(t, p.startStation, p.endStation);
            p.status = Status.waiting;
            if(!p.startStation.getStationPassengers().contains(p) && p.getCurrentLocation() != null)
                Platform.runLater(()->{
                    p.startStation.getStationPassengers().add(p.getCurrentLocation().remove(p.getCurrentLocation().indexOf(p)));
                });
        }
        
        System.out.println("Passengers Reset");
    }
    
    public ObservableList<Passenger> getCurrentLocation(){
        for(Station s: Station.getStations()){
            if(s.getStationPassengers().contains(this))
                return s.getStationPassengers();
        }
        
        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
        AnchorPane container = (AnchorPane) mainPageRoot.lookup("#container");

        for (Node n : container.getChildren().filtered((Node n1) -> {
            return n1 instanceof GraphInit;
        })) {
            synchronized(n){
                if(!((GraphInit) n).lookupAll(".trainset").isEmpty()){
                    for (Node t :  n.lookupAll(".trainset")) {
                        if(((TrainSet)t).getOnBoardPassenger().contains(this))
                            return ((TrainSet)t).getOnBoardPassenger();
                    }
                }
                break;
            }
        }
        
        return null;
    }
    public Status getStatus(){
        return status;
    }
    
    public void setStatus(Status status){
        this.status = status;
    }
       
    public void setAnimCycleBeforeTripStart(int t){
        animCycleBeforeTripStart = t;
    }
    
    public int getAnimCycleBeforeTripStart(){
        return animCycleBeforeTripStart;
    }
    
    public void setAnimCycleToTripEnd(int t){
        animCycleBeforeTripStart = t;
    }
    
    public int getAnimCycleToTripEnd(){
        return animCycleToTripEnd;
    }
    
    public void setTrip(TCP1 t){
        trip = new Trip(t, this.startStation, this.endStation);
    }
    
    public Trip getTrip(){
        return trip;
    }
    
    public Station getStartStation(){
        return startStation;
    }
    
    public Station getEndStation(){
        return endStation;
    }
    
    public Image getImage(){
        return profileImage;
    }
    
    public String getUsername(){
        return username;
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof Passenger){
            return username.equals(((Passenger)o).getUsername());
        }else 
            return false;
    }
    
}
