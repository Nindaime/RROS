/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;

/**
 *
 * @author PETER-PC
 */
public class Trip {
    public Station startStation, endStation;
    public boolean isValidTrip;
    public enum tripType{connecting, nonConnecting};
    private tripType type;
    public static ArrayList<Trip> Trips = new ArrayList<>();
    private final Queue<Station> connectingStations = new LinkedList<>();
    
    public Trip(TCP1 t, Station startStation, Station endStation){
        this.startStation = startStation;
        this.endStation = endStation;
        
        Double[] tripCost = new Double[6];
        Arrays.fill(tripCost, 5000.0); double weight;
//        TCP t = TCP.TCPs.get(tcpIndex);
        
        for(int i = 0; i < 6; i++){
            
            //ensure a passenger takes the shortest route to destination given the generate TCPs
            if(t.stopPoints[i].indexOf(startStation) != -1 && 
                    t.stopPoints[i].indexOf(endStation) != -1 && 
                    t.stopPoints[i].indexOf(startStation) < t.stopPoints[i].indexOf(endStation)){
                    //reset weight where TCP is a superSet of the route from startStation to endStation
                    weight = 0;
//                    System.out.println("TCP "+t+" contains startStation ("+startStation+") and endStation ("+endStation+")");
                    for(int index  = t.stopPoints[i].indexOf(startStation); index < t.stopPoints[i].indexOf(endStation); index++){
                        WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(t.stopPoints[i].get(index).getLabel()), 
                                Vertex.getVertex(t.stopPoints[i].get(index + 1).getLabel()));
//                        System.out.println("Start Vertex: "+ Vertex.getVertex(t.stopPoints.get(index).getLabel()).getName()+
//                                " End Vertex: "+ Vertex.getVertex(t.stopPoints.get(index + 1).getLabel()).getName()+" weight: "+weight);
                        if(w != null)
                            weight += w.getWeight();
                    }
                tripCost[i] = weight;
            }
            
        }
        int minIndex = 0;
        
        for(int d = 0; d < tripCost.length; d++){
            if(d != 0 && tripCost[minIndex] > tripCost[d]){
                minIndex = d;
            }
        }
        
        if(tripCost[minIndex] != 5000.0){
            isValidTrip = true;
            type = tripType.nonConnecting;
        }else{//improve Connecting trip algo
            FilteredList<ArrayList<Station>> tcpStartStationList = 
                    FXCollections.observableArrayList(t.stopPoints).filtered((ArrayList<Station> sPoints) -> {
                return sPoints.contains(this.startStation);
            });
            
            FilteredList<ArrayList<Station>> tcpEndStationList = 
                    FXCollections.observableArrayList(t.stopPoints).filtered((ArrayList<Station> sPoints) -> {
                return sPoints.contains(this.endStation);
            });

            ArrayList<Station>[] startStationArray = new ArrayList[tcpStartStationList.size()];
            ArrayList<Station>[] endStationArray = new ArrayList[tcpEndStationList.size()];
            
            Arrays.sort(tcpStartStationList.toArray(startStationArray), (ArrayList<Station> t1, ArrayList<Station> t2) -> {
                if (t1.indexOf(getStartStation()) < t2.indexOf(getStartStation())) {
                    return -1;
                } else if (t1.indexOf(getStartStation()) > t2.indexOf(getStartStation())) {
                    return 1;
                } else {
                    return 0;
                }
            });
            
            Arrays.sort(tcpEndStationList.toArray(endStationArray), (ArrayList<Station> t1, ArrayList<Station> t2) -> {
                if (t1.indexOf(getEndStation()) < t2.indexOf(getEndStation())) {
                    return -1;
                } else if (t1.indexOf(getEndStation()) > t2.indexOf(getEndStation())) {
                    return 1;
                } else {
                    return 0;
                }
            });
            
            ArrayList<ArrayList<Station>> startStationTCPs = new ArrayList<>(FXCollections.observableArrayList(startStationArray));
            ArrayList<ArrayList<Station>> endStationTCPs = new ArrayList<>(FXCollections.observableArrayList(endStationArray));
            
            //use connecting time to generate sublist
            for(ArrayList<Station> sPoints: startStationTCPs){
                if(connectingStations.isEmpty()){
                    int startStationIndex = sPoints.indexOf(this.startStation);
                    for(Station s1: sPoints.subList(startStationIndex, sPoints.size())){
                        for(ArrayList<Station> sPoints2: endStationTCPs){
                            if(connectingStations.isEmpty()){
                                int endStationIndex = sPoints2.indexOf(this.endStation);
                                if(startStationIndex < endStationIndex){
                                    for(Station s2: sPoints2.subList(startStationIndex+1, endStationIndex+1)){
                                        if(Vertex.getVertex(s1.getLabel()).getNeighbours().contains(Vertex.getVertex(s2.getLabel()))){
//                                            connectingStations.offer(s2);
                                            connectingStations.offer(this.endStation);
                                            this.endStation = s2;
                                            isValidTrip = true;
                                            type = tripType.connecting;
                                            break;
                                        }
                                    }
                                }
                            }else
                                break;
                        }
                    }
                }else
                    break;
            }
            
            if(connectingStations.isEmpty())
                isValidTrip = false;
            
        }
        
        Trips.add(this);
        
    }
    
    public tripType getTripType(){
        return type;
    }
    
    public Queue<Station> getConnectingStations(){
        return connectingStations;
    }
    
    public void setStartStation(Station s){
        startStation = s;
    }
    
    public void setEndStation(Station s){
        endStation = s;
    }
    
    public Station getStartStation(){
        return startStation;
    }
    
    public Station getEndStation(){
        return endStation;
    }
}
