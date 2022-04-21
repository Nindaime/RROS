/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Duration;
import static rros.FXMLDocumentController.FitnessChart;
import static rros.FXMLDocumentController.displaySubMenu;
import static rros.TCP1.playCurrentAnimation;


/**
 *
 * @author PETER-PC
 */
public class TCP implements Comparable{
    private TrainSet[] trainset;
    public static final int TRIPCOUNTPERDAY = 18, INDIVIDUAL = 10;
    public static ObservableList<TCP> TCPs = FXCollections.observableArrayList();
    public final ArrayList<Station>[] stopPoints;
    private final int[] maintenanceCount;
    public final int connectingTime = 1, maintenanceTime = 3;
    public static final double MAINTENANCERUNNINGDISTANCE = 1000, MAINTENANCERUNNINGTIME = 30; // 1000km and 20seconds
    private Path[] animPath; 
    private final double[] animDurationInSecs, distTraveledBeyondThreshold, totalConnectingDistance, runningDistance;
    private SequentialTransition[] animation;
    private final HashMap<String, Double> fitness;
    public final ParallelTransition pTs = new ParallelTransition();
    public static ParallelTransition currentAnimation;
//    private final boolean[] undergoingRepairs = new boolean[TRIPCOUNTPERDAY];
    
    public TCP(TrainSet[] t){
        //assign a trainset to this TCP
        runningDistance = new double[6];
        animDurationInSecs = new double[6];
        distTraveledBeyondThreshold = new double[6];
        totalConnectingDistance = new double[6];
        //ensure this initialize a new array for all variables
        trainset = t;
        maintenanceCount = new int[6];
        stopPoints = new ArrayList[6];
        for (int i = 0; i < 6; i++) 
            stopPoints[i] = new ArrayList<>();
        
        fitness = new HashMap<>();
        TCPs.add(this);
    }
    
    public TCP(TrainSet[] t, String s){
        //this construtor does not add this TCP to the list of TCPs
        runningDistance = new double[6];
        animDurationInSecs = new double[6];
        distTraveledBeyondThreshold = new double[6];
        totalConnectingDistance = new double[6];
        maintenanceCount = new int[6];
        trainset = t;
        stopPoints = new ArrayList[6];
        for (int i = 0; i < 6; i++) 
            stopPoints[i] = new ArrayList<>();
        
        fitness = new HashMap<>();
//        TCPs.add(this);
    }
    
    public void setTrainset(TrainSet[] t){
        trainset = t;
    }
    
    public final TrainSet[] getTrainSet(){
        return trainset;
    }
    
    public void setFitness(int tcpIndex){
        double passengerSatisfaction = .0;
            
        int passengersSize = Passenger.PASSENGERS.size();
        for (Passenger p : Passenger.PASSENGERS) {
            switch (p.getStatus()) {
                case arrived:
                    passengerSatisfaction += 10.0 / passengersSize;
                    System.out.println("Passenger "+p.getUsername()+" arrived destination");
                    break;
                case enroute:
                    passengerSatisfaction += 5.0 / passengersSize;
                    System.out.println("Passenger "+p.getUsername()+" enroute");
                    break;
                case waiting:
                    System.out.println("Passenger "+p.getUsername()+" still at start station");
                    break;
            }
        }
        
        double tcpTrainUtilizationEfficiency = 0;
        TCP t = TCPs.get(tcpIndex);
        for(int trainsetIndex = 0; trainsetIndex < t.getTrainSet().length; trainsetIndex++){
            double trainUtilizationEfficieny = 0;
            double maxTrainUtilization = 30.0 / t.getTrainSet().length;
            if (t.getDistTraveledBeyondThreshold(trainsetIndex) == 0) {
                trainUtilizationEfficieny += maxTrainUtilization;
                System.out.println("TCP (" + tcpIndex + ") Distance Traveled Beyond Threshold: " +
                        t.getDistTraveledBeyondThreshold(trainsetIndex) + ", Total Connecting Distance: " + t.getTotalConnectingDistance(trainsetIndex));
            } else {
                trainUtilizationEfficieny += maxTrainUtilization
                        - (t.getDistTraveledBeyondThreshold(trainsetIndex) / t.getTotalConnectingDistance(trainsetIndex)) * maxTrainUtilization;
                System.out.println("TCP (" + tcpIndex + ") Distance Traveled Beyond Threshold: "+
                        t.getDistTraveledBeyondThreshold(trainsetIndex) +", train utilization of train"+trainsetIndex+": "+trainUtilizationEfficieny
                        +", Total Connecting Distance: " + t.getTotalConnectingDistance(trainsetIndex));
            }

            double maintenanceEffeciency = (t.getMaintenanceCount()[trainsetIndex] == 0 ? 5 : 5 - (t.getMaintenanceCount()[trainsetIndex] / 5.0));
            System.out.println("TCP (" + tcpIndex + ")Maintenance Effeciency of Trainset "+trainsetIndex+": " + maintenanceEffeciency);

            trainUtilizationEfficieny += maintenanceEffeciency;
            tcpTrainUtilizationEfficiency += trainUtilizationEfficieny;
        }
        
        tcpTrainUtilizationEfficiency /= t.getTrainSet().length;
        
        fitness.put("passengerSatisfaction", passengerSatisfaction);
        fitness.put("tcpTrainUtilizationEfficiency", tcpTrainUtilizationEfficiency);
        
        System.out.println("Passenger Satisfaction: " + t.getFitness().get("passengerSatisfaction")
                + ", Train utilization efficiency: " + t.getFitness().get("tcpTrainUtilizationEfficiency"));
        
    }
    
    public synchronized HashMap<String, Double> getFitness(){
        try{
            if(pTs.getChildren().containsAll(Arrays.asList(getAnimation()))){
                while(pTs.getStatus() == Animation.Status.RUNNING){
                    System.out.println("cannot get fitness till animation ends");
                    this.wait();
                }
            }
        }catch(InterruptedException ex){}
        return fitness;
    }
    
    public final void TCPToAnimPath() {
        animPath = new Path[6];
        for(int i = 0; i < stopPoints.length; i++){
            animPath[i] = new Path();
            for(int k = 0; k < TRIPCOUNTPERDAY; k++){
//                System.out.println("Station from TCPToAnimPath: "+stopPoints[i].get(k)+" for Trainset "+i);
                if(k == 0){
                    MoveTo m = new MoveTo(Vertex.getVertex(stopPoints[i].get(k).getLabel()).getCenterX(), Vertex.getVertex(stopPoints[i].get(k).getLabel()).getCenterY());
                    animPath[i].getElements().add(m);
                }else{
                    LineTo lT = new LineTo(Vertex.getVertex(stopPoints[i].get(k).getLabel()).getCenterX(), Vertex.getVertex(stopPoints[i].get(k).getLabel()).getCenterY());
                    animPath[i].getElements().add(lT);
                }
            }
        }
        
        setAnimDuration();
    }
    
    public void setAnimation(){
        animation = new SequentialTransition[6];
        for (int i = 0; i < 6; i++) {
            animation[i] = new SequentialTransition();
            final int trainsetIndex = i;
            for(PathElement pE: getAnimPath()[i].getElements()){
                int index = getAnimPath()[i].getElements().indexOf(pE);
                if(pE instanceof MoveTo){
                    PathTransition pT = new PathTransition(Duration.ZERO, new Path((MoveTo)pE, new LineTo(((MoveTo) pE).getX(), ((MoveTo) pE).getY())), getTrainSet()[i]);
                    pT.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
                    pT.setOnFinished(e ->{
                        FilteredList<Passenger> filteredPassengers = FXCollections.observableArrayList(Passenger.PASSENGERS).filtered((Passenger p) -> {
                            return p.getTrip().isValidTrip && 
                                    Vertex.getVertex(p.getTrip().getStartStation().getLabel()).contains(((MoveTo) pE).getX(), ((MoveTo) pE).getY());
                        });
                        for(Passenger p: filteredPassengers){
                            int passengerIndex = p.getTrip().getStartStation().getStationPassengers().indexOf(p);
                            p.setAnimCycleBeforeTripStart(index+1);
                            if(passengerIndex != -1){
                                getTrainSet()[trainsetIndex].getOnBoardPassenger().add(p.getTrip().getStartStation().getStationPassengers().remove(passengerIndex));
                                p.setStatus(Passenger.Status.enroute);
//                                Passenger.PASSENGERS.get(Passenger.PASSENGERS.indexOf(p)).setStatus(Passenger.Status.enroute);
                                System.out.println("Passengers "+p.getUsername()+" entered "+getTrainSet()[trainsetIndex].getName()+" at Station: "+
                                        p.getTrip().getStartStation()+" trip-type: "+p.getTrip().getTripType());
                            }                      
                        }
                    });
                    animation[i].getChildren().add(pT);
                }
                else{
                    Point2D point1, point2;
                    if (index == 1) 
                        point1 = new Point2D(((MoveTo) getAnimPath()[i].getElements().get(index - 1)).getX(), ((MoveTo) getAnimPath()[i].getElements().get(index - 1)).getY());
                    else 
                        point1 = new Point2D(((LineTo)getAnimPath()[i].getElements().get(index - 1)).getX(), ((LineTo)getAnimPath()[i].getElements().get(index - 1)).getY());

                    point2 = new Point2D(((LineTo) pE).getX(), ((LineTo) pE).getY());
                    double distance = point1.distance(point2);

                    Duration d = Duration.seconds(getAnimDuration(i) * (distance / getTotalConnectingDistance(i)));

                    PathTransition pT = new PathTransition(d, new Path(new MoveTo(point1.getX(), point1.getY()), (LineTo) pE), getTrainSet()[i]);
                    //delay animation to simulate connecting time between adjacent way stations
                    pT.setDelay(Duration.seconds(connectingTime));
                    pT.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
//                    pT.setRate(10);
                    pT.setOnFinished(e -> {
                        FilteredList<Passenger> filteredPassengers = FXCollections.observableArrayList(Passenger.PASSENGERS).filtered((Passenger p) -> {
                            return p.getTrip().isValidTrip && p.getStatus() != Passenger.Status.arrived;
//                                    && ((Vertex.getVertex(p.getTrip().getStartStation().getLabel()).contains(((LineTo) pE).getX(), ((LineTo) pE).getY()))
//                                    || (Vertex.getVertex(p.getTrip().getEndStation().getLabel()).contains(((LineTo) pE).getX(), ((LineTo) pE).getY())));
                        });
                        for(Passenger p: filteredPassengers){
                            if(Vertex.getVertex(p.getTrip().getStartStation().getLabel()).contains(((LineTo) pE).getX(), ((LineTo) pE).getY())){
                                int passengerIndex = p.getTrip().getStartStation().getStationPassengers().indexOf(p);
                                if(passengerIndex != -1){
                                    getTrainSet()[trainsetIndex].getOnBoardPassenger().add(p.getTrip().getStartStation().getStationPassengers().remove(passengerIndex));
                                    System.out.println("Passengers "+p.getUsername()+" entered "+getTrainSet()[trainsetIndex].getName() +" at Station "+p.getTrip().getStartStation());
                                }
                            }else if(Vertex.getVertex(p.getTrip().getEndStation().getLabel()).contains(((LineTo) pE).getX(), ((LineTo) pE).getY())){
                                int passengerIndex = getTrainSet()[trainsetIndex].getOnBoardPassenger().indexOf(p);
                                if(passengerIndex != -1){
                                    p.getTrip().getEndStation().getStationPassengers().add(getTrainSet()[trainsetIndex].getOnBoardPassenger().remove(passengerIndex));
                                    
                                    //set passenger status to arrived
                                    if(p.getTrip().getEndStation() == p.getEndStation()){
                                        p.setStatus(Passenger.Status.arrived);
                                        System.out.println("Passenger Status: "+p.getStatus());
                                    }

                                    //set new start station and end station for connecting trip
                                    if(!p.getTrip().getConnectingStations().isEmpty()){
                                        p.getTrip().setStartStation(p.getTrip().getEndStation());
                                        p.getTrip().setEndStation(p.getTrip().getConnectingStations().poll());
                                    }

                                    System.out.println("Passengers "+p.getUsername()+" alighted "+getTrainSet()[trainsetIndex].getName()+" at Station "
                                            +p.getEndStation()+" trip-type: "+p.getTrip().getTripType());
                                }
                            }                            
                        }
                            
                    });
                        
                    animation[i].getChildren().add(pT);
                }
            }        
        }

    }
    
    public int[] getMaintenanceCount(){
        return maintenanceCount;
    }
    
    public SequentialTransition[] getAnimation(){
        return animation;
    }
    
    public void setAnimDuration(){
        for(int i = 0; i < 6; i++){
            for(Station s: stopPoints[i]){
                if(stopPoints[i].indexOf(s) != 0){
                    if(!stopPoints[i].get(stopPoints[i].indexOf(s) - 1).equals(s)){
                        WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(stopPoints[i].get(stopPoints[i].indexOf(s) - 1).getLabel()), 
                                Vertex.getVertex(s.getLabel()));
                        if(w != null)
                            //implies trainsets travel at 40pixels per sec        
                            animDurationInSecs[i] += (w.getWeight()/40.0);
                    }
                    else
                        animDurationInSecs[i] += 0;
                }
            }
            System.out.println("Duration of TCP "+TCPs.indexOf(this)+" is "+animDurationInSecs[i]);
        }
    }
    
    public double getAnimDuration(int trainsetIndex){
        return animDurationInSecs[trainsetIndex];
    }
    
    public double getTotalConnectingDistance(int trainsetIndex){
        if(totalConnectingDistance[trainsetIndex] == 0){
            for(Station s: stopPoints[trainsetIndex]){
                if(stopPoints[trainsetIndex].indexOf(s) != 0){
                    int index = stopPoints[trainsetIndex].indexOf(s);
                    double distance = new Point2D(Vertex.getVertex(s.getLabel()).getCenterX(), Vertex.getVertex(s.getLabel()).getCenterY())
                            .distance(Vertex.getVertex(stopPoints[trainsetIndex].get(index-1).getLabel()).getCenterX(), 
                                    Vertex.getVertex(stopPoints[trainsetIndex].get(index-1).getLabel()).getCenterX());
                    totalConnectingDistance[trainsetIndex] += distance;
                }
            }
//            System.out.println("Total Connecting Distance: "+totalConnectingDistance[trainsetIndex]);
            return totalConnectingDistance[trainsetIndex];
        }else{
//            System.out.println("Total Connecting Distance: "+totalConnectingDistance[trainsetIndex]);
            return totalConnectingDistance[trainsetIndex];
        }
            
    }
    
    public Path[] getAnimPath(){
        return animPath;
    }
    
    public double getDistTraveledBeyondThreshold(int trainsetIndex){
        return distTraveledBeyondThreshold[trainsetIndex];
    }
    
//    public void addUR_Value(boolean b, int columnIndex){
//        undergoingRepairs[columnIndex] = b;
//    }
//    
//    public boolean[] getUR(){
//        return undergoingRepairs;
//    }
//    
//    public static void generateTCPs(){
//        Station.STATIONSClassSequencer();
//        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
//        ProgressBar pBar = (ProgressBar) mainPageRoot.lookup("#pBar");
//        
//        Label pBarLabel = (Label) mainPageRoot.lookup("#pBarLabel");
//        Platform.runLater(() ->  {pBarLabel.setText("Generating "+INDIVIDUAL+" TCPs as Individuals...0%");});
//        
////        try{
//            loadTrainSets(INDIVIDUAL);
////        }catch(SQLException ex){}
//        
//        for(int m = 0; m < INDIVIDUAL; m++){
//            
//            for(int i = 0; i < 6; i++){
//                for(int k = 0; k < TRIPCOUNTPERDAY; k++){
//                    //if the slot on the TCP has not being assigned a value, assign a value
//                    //not null for preconfigured maintenance route
////                    if(TCPs.get(m).stopPoints[i].get(k) == null){
//                    if(TCPs.get(m).stopPoints[i].size() == k){
//                        //if cursor at the start of a new row i.e. new TCP, select a random station
//                        if(k == 0){
//                            System.out.println("k equals 0");
//                            int randStartTCPIndex = (int) (Math.random() * Station.getStations().size());
//                            Station s = Station.getStations().get(randStartTCPIndex);
//                            //check if the randomly selected station satisfies one trainset per station per time
//                            if(!isValidStationSlot(s, m, i, k))
//                                TCPs.get(m).stopPoints[i].add(k, resolveIsValidStationSlot(s, m, i, k));
//                            else
//                                TCPs.get(m).stopPoints[i].add(k, s);
//
//                            System.out.println("station "+TCPs.get(m).stopPoints[i].get(k)+" at Index "+k+" of TCP "+i);
//                        }else{
//                            System.out.println("k is not equals 0");
//                            Station s = getNextStation(TCPs.get(m).stopPoints[i].get(k-1));
//                            //check if the randomly selected station satisfies one trainset per station per time
//                            if(!isValidStationSlot(s, m, i, k))
//                                s = resolveIsValidStationSlot(s, m, i, k);
//
//                            if(shouldRepair(s, m, i, k))
//                                repairTrainSet_V2(s, m, i, k);
//                            else
//                                TCPs.get(m).stopPoints[i].add(k, s);
//
//                            System.out.println("station "+TCPs.get(m).stopPoints[i].get(k)+" at Index "+k+" of TCP "+i);
//                        }
//                    }
//                }
//
//                //build the stop points for all TCPs in the railway system
//                System.out.print("Building Stop points of TCP "+m+": ");
//                for(int j = 0; j < TRIPCOUNTPERDAY; j++){
////                    TCPs.get(tcpIndex).stopPoints.add(TCPsAsArray[m][i][j]);
//                    System.out.print(TCPs.get(m).stopPoints[i].get(j)+ ", ");
//                }
//                System.out.println("");
//                
//            }
//            TCPs.get(m).TCPToAnimPath();
//            //return every passenger to their start station for simulation
//            Passenger.resetPassengers(m);
//            for (int i = 0; i < 6; i++) {
//                System.out.println("Maintenance Count of TCP "+m+" trainset "+i+": "+TCPs.get(m).maintenanceCount[i]);
//            }
//
//            TCPs.get(m).setAnimation();
//            
//            //install passenger status listener once
//            if(m == 0){
//                //install listeners on stations
//                for(Station s: Station.getStations()){
//                    s.getStationPassengers().addListener((ListChangeListener.Change<? extends Passenger> listener) -> {
//                        while (listener.next()) {
//                            if (listener.wasAdded() || listener.wasRemoved()) {
//                                int arrivingPassengers = s.getStationPassengers().filtered((Passenger p) -> {
//                                    return !p.getTrip().getStartStation().equals(s);
//                                }).size();
//                                int departingPassengers = s.getStationPassengers().size() - arrivingPassengers;
//                                Label passengerStatus = Vertex.getVertex(s.getLabel()).getPassengerStatus();
//                                Platform.runLater(()-> { passengerStatus.setText(departingPassengers+"|"+arrivingPassengers);});
//                            } 
//                        }
//                    });
//                }
//                
//                AnchorPane container = (AnchorPane) mainPageRoot.lookup("#container");
//                TableView tbView = (TableView)container.lookup("#tbView");
//                TableColumn<TrainsetPassengers, String> tsColumn = new TableColumn<>("Trainsets");
//                tsColumn.setPrefWidth(57);
//                tsColumn.setCellValueFactory((TableColumn.CellDataFeatures<TrainsetPassengers, String> p) -> 
//                        new ReadOnlyStringWrapper(p.getValue().getTrainsetName()));
//                
//                TableColumn<TrainsetPassengers, String> psColumn = new TableColumn<>("Passengers");
//                psColumn.setPrefWidth(68);
//                psColumn.setCellValueFactory((TableColumn.CellDataFeatures<TrainsetPassengers, String> p) -> 
//                        new ReadOnlyStringWrapper(p.getValue().getPassengerCount()+""));
//
//                Platform.runLater(()-> tbView.getColumns().addAll(tsColumn, psColumn));
//                
//                Pane tbContainer = (Pane) container.lookup("#tbContainer");
//                tbContainer.getStyleClass().add("background");
//            }
//            
//            simulationTCP(m, true);//simulate TCP to generate fitness value
//            
//            float progress = (m + 1F) / INDIVIDUAL;
//            
//            Platform.runLater(() -> {
//                pBar.setProgress(progress);
//                if(progress != 1)
//                    pBarLabel.setText(pBarLabel.getText().substring(0, 35)+Math.round(progress*100)+"%");
//                else{
//                    pBarLabel.setText(pBarLabel.getText().substring(0, 35)+"DONE");
//                }
//            });
//        }
//
//    }
    
    public static class TrainsetPassengers{
        private SimpleStringProperty trainsetName;
        private SimpleStringProperty passengerCount;
        
        public SimpleStringProperty trainsetNameProperty(){
            if(trainsetName == null)
                trainsetName = new SimpleStringProperty(this, "trainsetName");
            
            return trainsetName;
        }
        
        public SimpleStringProperty passengerCountProperty(){
            if(passengerCount == null)
                passengerCount = new SimpleStringProperty(this, "passengerCount");
            return passengerCount;
        }   
        
        public TrainsetPassengers(String trainsetName, String passengerCount){
            this.trainsetName = new SimpleStringProperty(trainsetName);
            this.passengerCount = new SimpleStringProperty(passengerCount);
        }
        
        public String getTrainsetName(){
            return trainsetName.get();
        }
        
        public void setTrainsetName(String trainsetName){
            this.trainsetName.set(trainsetName);
        }
        
        public String getPassengerCount(){
            return passengerCount.get();
        }
        
        public void setPassengerCount(String passengerCount){
            this.passengerCount.set(passengerCount);
        }
    }
    
    public static void simulationTCP(int tcpIndex, boolean play){
        
        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
        AnchorPane container = (AnchorPane) mainPageRoot.lookup("#container");
        GraphInit gPanel = (GraphInit) container.getChildren().filtered((Node n1) -> { return n1 instanceof GraphInit; }).get(0);
        
        TCP simulatedTCP = TCPs.get(tcpIndex);

        simulatedTCP.clearCurrentAnimation(mainPageRoot, container);
        TableView tbView = (TableView) container.lookup("#tbView");
        ObservableList<TrainsetPassengers> passengers = FXCollections.observableArrayList();

        for (TrainSet t : simulatedTCP.getTrainSet()) {
            int trainsetIndex = Arrays.asList(simulatedTCP.getTrainSet()).indexOf(t);
            passengers.add(new TrainsetPassengers(t.getName(),0+""));
            t.getOnBoardPassenger().addListener((ListChangeListener.Change<? extends Passenger> listener) -> {
                while (listener.next()) {
                    if (listener.wasAdded() || listener.wasRemoved()) {
                        tbView.getItems().set(trainsetIndex, new TrainsetPassengers(t.getName(), t.getOnBoardPassenger().size() + ""));
                    }
                }
            });
        }
        
        tbView.setItems(passengers);
        
        for (int i = 0; i < simulatedTCP.getTrainSet().length; i++) {
            TrainSet t = simulatedTCP.getTrainSet()[i];
            t.setOpacity(0);
            Platform.runLater(()->{gPanel.getChildren().add(t);});

//            simulatedTCP.getAnimation()[i].setRate(50);
            simulatedTCP.pTs.setRate(50);
            simulatedTCP.pTs.getChildren().add(simulatedTCP.getAnimation()[i]);
            System.out.println("Animation TCP " + simulatedTCP.getTrainSet()[i] + " is set");
        }

        
        Platform.runLater(() -> {
            simulatedTCP.pTs.playFromStart();
            System.out.println("Simulating Animation for TCP " + tcpIndex);
            simulatedTCP.pTs.setOnFinished((e)->{
                synchronized(simulatedTCP){
                    simulatedTCP.setFitness(tcpIndex);
                    simulatedTCP.notifyAll();
                    if (tcpIndex == (INDIVIDUAL - 1) && simulatedTCP.pTs.getRate() == 50) {
                        Collections.sort(TCPs);
                        gBests.add((TCP)Collections.max(TCPs));
                        System.out.println("Added to GBest");
                        System.out.println("GBest size: "+gBests.size()+"\nlatest gBest fitness value: Passenger Satisfaction("
                                +gBests.get(gBests.size()-1).getFitness().get("passengerSatisfaction")+"); Train Utilization Efficiency: "
                                +gBests.get(gBests.size()-1).getFitness().get("tcpTrainUtilizationEfficiency"));
                        if(play){
                            playCurrentAnimation(mainPageRoot, container);
                            System.out.println("Animation Finished");
                        }
                    }
                }
            });
        });
            
    }
    
    public static double getMaxDuration(int tcpIndex){
        TCP tcp = TCPs.get(tcpIndex);
        if(tcp.maxAnimDuration == 0){
            double[] durationArray = new double[tcp.animDurationInSecs.length];
            for (int i = 0; i < durationArray.length; i++) {
                durationArray[i] = tcp.animDurationInSecs[i];
            }
            Arrays.sort(durationArray);
            tcp.maxAnimDuration = durationArray[durationArray.length - 1];
            return tcp.maxAnimDuration;
        }else
            return tcp.maxAnimDuration;
    }
    
    private double maxAnimDuration = 0;
    
    //synchronize updates to progress bar and label 
//    public static void playCurrentAnimation(AnchorPane mainPageRoot, AnchorPane container){
//        clearCurrentAnimation(mainPageRoot, container);
//        
//        TCP animatedTCP = gBests.get(gBests.size()-1);
////        TableView tbView = (TableView) container.lookup("#tbView");
//        
//        Passenger.resetPassengers(TCPs.indexOf(animatedTCP));
////        for(Object t: tbView.getItems()){
////            int trainsetIndex = tbView.getItems().indexOf(t);
////            tbView.getItems().set(trainsetIndex, new TrainsetPassengers(animatedTCP.getTrainSet()[trainsetIndex].getName(), 
////                    animatedTCP.getTrainSet()[trainsetIndex].getOnBoardPassenger().size() + ""));
////            
////        }
//        
//        for (Node n : container.getChildren().filtered((Node n1) -> {
//            return n1 instanceof GraphInit;
//        })) {
//            for (int i = 0; i < animatedTCP.getTrainSet().length; i++) {
//                TrainSet t = animatedTCP.getTrainSet()[i];
//                t.setOpacity(1);
//                Platform.runLater(()->{
//                    ((GraphInit) n).getChildren().add(t);
//                    System.out.println("Animation TCP " + t + " is set");
//                });
////                animatedTCP.getAnimation()[i].setRate(0.75);
//                pTs.setRate(1);
//                pTs.getChildren().add(animatedTCP.getAnimation()[i]);
//            }
//            break;
//        }
//
//        ProgressBar pBar = (ProgressBar) mainPageRoot.lookup("#pBar");
//        Label pBarLabel = (Label) mainPageRoot.lookup("#pBarLabel");
//
//        Platform.runLater(()->{
//            pBar.setProgress(0); //reset progress bar
//            pBarLabel.setText("Playing current animation...0%");
//        });
//
////        double progressFraction = 1 / (getMaxDuration(TCPs.indexOf(animatedTCP)) * 1.25);
//        double progressFraction = 1 / getMaxDuration(TCPs.indexOf(animatedTCP));
////        double progressFraction = 1 / (getMaxDuration(TCPs.indexOf(animatedTCP)) * 0.1);
//
//        timer1 = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent e) -> {
//            pBar.setProgress(pBar.getProgress() + progressFraction);
//            if (pBar.getProgress() < 1) {
//                pBarLabel.setText(pBarLabel.getText().substring(0, 28) + Math.round(pBar.getProgress() * 100) + "%");
//            } else {
//                pBarLabel.setText(pBarLabel.getText().substring(0, 28) + "DONE");
//                timer1.stop();
//            }
//        }));
//        timer1.setCycleCount(Timeline.INDEFINITE);
//        
//        pTs.playFromStart();
//        timer1.play();
//    }
    
    public static Timeline timer1;
    
    public void clearCurrentAnimation(AnchorPane mainPageRoot, AnchorPane container){
       
        for (Node n : container.getChildren().filtered((Node n1) -> {
            return n1 instanceof GraphInit;
        })) {
            for (Node t : ((GraphInit) n).lookupAll(".trainset")) {
                Platform.runLater(()->{
                    //removed all previous trainsets from simulation
                    if (((GraphInit) n).getChildren().remove(t)) 
                        System.out.println("Removed Trainset " + t);
                });
            }
        }
        
        pTs.getChildren().clear();
    }
    
    static ArrayList<TCP> gBests = new ArrayList<>();
    
    public static void optimize(float crossoverPossibility, float mutationPosibility, int iteration){
        //improve optimize method to stop when maxima does not increase after iteration count
        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
        ProgressBar pBar = (ProgressBar) mainPageRoot.lookup("#pBar");
        Label pBarLabel = (Label) mainPageRoot.lookup("#pBarLabel");
        
        Platform.runLater(()->{
            pBar.setProgress(0);
            pBarLabel.setText("Optimizing "+iteration+" generations of TCPs ..."+0+ "%");
        });

        mutationPosibility = Math.round(mutationPosibility * TRIPCOUNTPERDAY);
        //ensure mutation posibility is an even value
        mutationPosibility = (mutationPosibility % 2 == 0 ? mutationPosibility : mutationPosibility - 1); 
        crossoverPossibility = Math.round(INDIVIDUAL * crossoverPossibility);
        
        for(int counter = 0; counter < iteration; counter++){
//            if(counter > 0){
//                try{
//                    Thread.sleep(4000);
//                }catch(InterruptedException ex){}
//            }
            System.out.println("Iteration: "+counter);
            ArrayList<TCP> genePool = new ArrayList<>();
            for(TCP t: TCPs){
                System.out.println("TCP iteration "+TCPs.indexOf(t));
                try{
                    while(t.pTs.getStatus() == Animation.Status.RUNNING || t.getFitness().get("passengerSatisfaction") == null){
                        System.out.println("simulation still running");
                        Thread.sleep(200);
                    }
                        
//                    while(t.getFitness().get("passengerSatisfaction") == null){
//                        System.out.println("sleep thread");
                }catch(InterruptedException ex){}
                
                double fitness = t.getFitness().get("passengerSatisfaction") + t.getFitness().get("tcpTrainUtilizationEfficiency");
                for(int i = 0; i < Math.round(fitness); i++){
                    System.out.println("Adding to GenePool");
                    genePool.add(t);
                }
            }

            Collections.shuffle(genePool);//to improve the chance of selecting fitter genes
            System.out.println("Gene pool size: "+genePool.size());
            ArrayList<TCP> childrenTCP = new ArrayList<>();
            while(childrenTCP.size() < crossoverPossibility){
                System.out.println("Cross over possibility: "+crossoverPossibility);
                int selectedParentIndex1 = (int) (Math.random() * genePool.size());
                int selectedParentIndex2 = (int) (Math.random() * genePool.size());
                
                while(selectedParentIndex1 == selectedParentIndex2) //ensure parent1 is not same as parent2
                    selectedParentIndex2 = (int) (Math.random() * genePool.size());
                
                System.out.println("Parent Index 1: "+selectedParentIndex1+", Parent Index 2: "+selectedParentIndex2);
                System.out.println("crossing two parent TCPs");
                TCP childTCP = new TCP(genePool.get(selectedParentIndex1).getTrainSet(), "optimization");

                for(int j = 0; j < TCPs.get(0).stopPoints.length; j++){
                    ArrayList<Station> temp = new ArrayList<>();
                    System.out.println("generating tcp for trainset "+j);
                    int singleCrossPointIndex = 9; //9th element to last element
                    temp.addAll(genePool.get(selectedParentIndex1).stopPoints[j].subList(0, singleCrossPointIndex));
                    temp.addAll(genePool.get(selectedParentIndex2).stopPoints[j].subList(singleCrossPointIndex, TRIPCOUNTPERDAY));
                    while(!isValidTCP(temp)){
                        --singleCrossPointIndex;
                        if(singleCrossPointIndex < 0){
                            System.out.println("child chromosome from cross-over is invalid");
                            break;
                        }
                        else{
                            System.out.println("correcting child chromosome");
                            temp.clear();
                            temp.addAll(genePool.get(selectedParentIndex1).stopPoints[j].subList(0, singleCrossPointIndex));
                            temp.addAll(genePool.get(selectedParentIndex2).stopPoints[j].subList(singleCrossPointIndex, singleCrossPointIndex+9));
                            temp.addAll(genePool.get(selectedParentIndex1).stopPoints[j].subList(singleCrossPointIndex+9, TRIPCOUNTPERDAY));
//                            System.out.println("Size of corrected temp child trainset tcp: "+temp.size());
                        }
                    }
                    
                    childTCP.stopPoints[j].addAll(temp);

//                    if(isValidTCP(temp))
//                        break;
//                    else if(j == 5){ //if all stop points generated from cross-over are valid, add to childrenTCP
//                        //transfer valid cross-over from temp chromosomes to child TCP
//                        childTCP.stopPoints[j].addAll(temp);
//                        childrenTCP.add(childTCP);
//                    }
                }
                childrenTCP.add(childTCP);
            }

            //mutate child TCPs
            System.out.println("Mutation Possibility: "+mutationPosibility);
            for(TCP t: childrenTCP){
                
                //generate the random gene indices to be mutated
                Integer[] randomIndices = new Integer[(int)mutationPosibility];
                
                for (int j = 0; j < randomIndices.length; j++){
                    randomIndices[j] = 4 + (int) (Math.random() * (TRIPCOUNTPERDAY - 4));
                    //ensure generated random indices are greater than 4 to improve mutation effect on TCP fitness 
                    //and ensure no 2 generated random indices are the same
                    while(Collections.frequency(Arrays.asList(randomIndices), randomIndices[j]) > 1)
                        randomIndices[j] = (int) (Math.random() * TRIPCOUNTPERDAY);
                } 
               
                for(int i = 0; i < t.stopPoints.length; i++){
                    ArrayList<Station> mutatedChromosome = new ArrayList<>();
                    int mutationResolver = 1;
                    
                    mutatedChromosome.addAll(t.stopPoints[i]);
                    for (int j = 0; j < randomIndices.length/2; j += 2){
                        System.out.println("Mutating TCP "+childrenTCP.indexOf(t));
                        Collections.swap(mutatedChromosome, randomIndices[j], randomIndices[j+1]);

                        while(!isValidTCP(mutatedChromosome)){
                            Collections.swap(mutatedChromosome, randomIndices[j+1], randomIndices[j]);
                            if(randomIndices[j] == 0 || randomIndices[j+1] == 0)
                                break;
                            else{
                                randomIndices[j] = ((randomIndices[j] - mutationResolver) >= 0 ? (randomIndices[j] - mutationResolver) : 0);
                                randomIndices[j+1] = ((randomIndices[j+1] - mutationResolver) >= 0 ? (randomIndices[j+1] - mutationResolver) : 0);
                                Collections.swap(mutatedChromosome, randomIndices[j], randomIndices[j+1]);
                                System.out.println("correcting invalid mutated genes");
                            }
                        }
                    }

                    t.stopPoints[i] = mutatedChromosome;
                    System.out.println("mutated tcp for trainset "+i);
                }
            }

            //swap weakest individual for new generation
            TCPs.removeAll(TCPs.subList(0, (int)crossoverPossibility));
            System.out.println("removed weak tcps");
                
            TCPs.addAll(childrenTCP); //swap parent chromosomes with child chromosomes
            for(TCP t: childrenTCP){
                t.TCPToAnimPath();
                int tcpIndex = TCPs.indexOf(t);
                //return every passenger to their start station for simulation
//                Passenger.resetPassengers(t);
                
                buildTCPVariables(tcpIndex);
                t.setAnimation();
                System.out.println("Simulation TCP "+tcpIndex);
                
                while(t.pTs.getStatus() == Animation.Status.RUNNING)
                    System.out.println("simulation still running");
                
                simulationTCP(tcpIndex, false);
            }
            
            //add fittest individual to GBest array
//            gBests.add(getSortedTCPArray()[INDIVIDUAL-1]);


            float progress = (counter + 1F) / iteration;

            Platform.runLater(() -> {
                pBar.setProgress(progress);
                if (progress != 1) 
                    pBarLabel.setText("Optimizing "+iteration+" generations of TCPs ..."+(int)Math.round(progress * 100) + "%");
                else 
                    pBarLabel.setText(pBarLabel.getText().substring(0, 36) + "DONE");
            });
            
        }
        
        Platform.runLater(() -> {
            FitnessChartController.loadData();
            displaySubMenu(FitnessChart);
        });
        
    }
        
    public static void buildTCPVariables(int tcpIndex){
        System.out.println("rebuilding TCP Variables");
        TCP t = TCPs.get(tcpIndex);
        for(ArrayList<Station> sPoints: t.stopPoints){
            for(Station s: sPoints){
                if(sPoints.indexOf(s) != 0){
                    if(shouldRepair(s, tcpIndex, FXCollections.observableArrayList(t.stopPoints).indexOf(sPoints), sPoints.indexOf(s))){
                        if(Vertex.getVertex(s.getLabel()) instanceof DirPort){
                            t.maintenanceCount[FXCollections.observableArrayList(t.stopPoints).indexOf(sPoints)] += 1; 
                            t.runningDistance[FXCollections.observableArrayList(t.stopPoints).indexOf(sPoints)] = .0;
                        }else{
                            WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(sPoints.get(sPoints.indexOf(s) - 1).getLabel()),
                                    Vertex.getVertex(s.getLabel()));
                            if(w != null)
                                t.distTraveledBeyondThreshold[FXCollections.observableArrayList(t.stopPoints).indexOf(sPoints)] += w.getWeight();
                        }
                    }
//                    else{
//                        WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(sPoints.get(sPoints.indexOf(s) - 1).getLabel()),
//                                Vertex.getVertex(s.getLabel()));
//                        if (w != null) {
//                            t.runningDistance[FXCollections.observableArrayList(t.stopPoints).indexOf(sPoints)] += w.getWeight();
//                        }
//                    }

                }
            }
        }
    }
    
    //check station validity across columns
    public static boolean isValidTCP(ArrayList<Station> sPoints){
        boolean output = true; 
  
        for(Station s: sPoints){
            int stationIndex = sPoints.indexOf(s);
            if(stationIndex != 0 && output){
                output = Vertex.getVertex(s.getLabel()).getNeighbours().contains(Vertex.getVertex(sPoints.get(stationIndex - 1).getLabel()));
            }else
                break;
        }
        
       return output;
       //test
    }
    
    //fix this method
    public static void loadTrainSets(int i) {
//        String queryString = "Select Label from rros.train";;
//        PreparedStatement statement = DBConnection.connection.prepareStatement(queryString);
//        ResultSet rSet = statement.executeQuery();
       

        for(int j = 0; j < i; j++) {
            TrainSet[] tSArray = new TrainSet[6];
            for(int m = 1; m < 7; m++){ 
                tSArray[m - 1] = new TrainSet("Train"+m, 1, (m-1));
                System.out.println("Train"+m+" created");
            }
            new TCP(tSArray);
        }
    }
    
    
    public static boolean shouldRepair(Station s, int individualIndex, int rowIndex, int columnIndex){
        Station s1, s2;
             s1 = TCPs.get(individualIndex).stopPoints[rowIndex].get(columnIndex-1);
             s2 = s;

             //if the train-set is stationary for two slots on TCP
         if(s1.equals(s2))
             TCPs.get(individualIndex).runningDistance[rowIndex] += 0;
         else{
             WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(s1.getLabel()), Vertex.getVertex(s2.getLabel()));
             if(w != null)
                 TCPs.get(individualIndex).runningDistance[rowIndex] += w.getWeight();
         }
         
        return TCPs.get(individualIndex).runningDistance[rowIndex] >= MAINTENANCERUNNINGDISTANCE;

    }
    //improve to search another DIRPort if a vertex of currentPathToDIRPort is not a valid station slot on TCPsAsArray
    public static final void repairTrainSet(Station s, int individualIndex, int rowIndex, int columnIndex){
//        int tcpIndex = (individualIndex + 1) * (rowIndex + 1) - 1;
//        System.out.println("Repair Trainset variables: Individual Index -> "+(individualIndex + 1)+"; Row Index -> "+(rowIndex + 1));
        
        System.out.println("Undergoing repairs");
        if(Vertex.getVertex(s.getLabel()) instanceof DirPort){
            TCPs.get(individualIndex).stopPoints[rowIndex].add(columnIndex, s);
            System.out.println("current way station ("+s.getLabel()+") is a DIRPort");
            //if none of the stations devalidated the one trainset per station per time 
            TCPs.get(individualIndex).maintenanceCount[rowIndex] += 1;
            TCPs.get(individualIndex).distTraveledBeyondThreshold[rowIndex] += (TCPs.get(individualIndex).runningDistance[rowIndex] > MAINTENANCERUNNINGDISTANCE ? 
                    TCPs.get(individualIndex).runningDistance[rowIndex] - MAINTENANCERUNNINGDISTANCE : 0);
            TCPs.get(individualIndex).runningDistance[rowIndex] = .0;
            System.out.println("station "+TCPs.get(individualIndex).stopPoints[rowIndex].get(columnIndex)+" at Index "+columnIndex+" of TCP "+rowIndex);
        }
        else{
            
            ArrayList<Vertex> dPorts = new ArrayList<>();
            // get all DIRports in the railway system
            for(Vertex v: Vertex.vertices)
                if(v instanceof DirPort)
                    dPorts.add(v);

            PriorityQueue<UCSDIRPort> pathsToDIRPorts = new PriorityQueue<>();
            for(int i = 0; i < dPorts.size(); i++){
                /**
                 * Use Constructor UCSDIRPort(Vertex rootNode, Vertex goalNode) instead of Constructor UCSDIRPort(Vertex rootNode)
                 * to account for scenario where the path(array of vertices) of the nearest UCSDIRPort is false for isValidStationSlot
                 */
                System.out.println("searching shortest path from "+Vertex.getVertex(s.getLabel())+" to "+dPorts.get(i));
                pathsToDIRPorts.add(new UCSDIRPort(Vertex.getVertex(s.getLabel()), dPorts.get(i)));
            }

            boolean TCPValid = false; 
//            int size = pathsToDIRPorts.size();
            for(int i = 0; i < pathsToDIRPorts.size(); i++){
                if(!TCPValid){
//                    System.out.println("Size of paths to DIR Port "+pathsToDIRPorts.size());
                    ObservableList<Vertex> currentPath = pathsToDIRPorts.poll().getUCSPath().operator.peek().getPath();
                    int validRunningDistance = 0;
                    for(Vertex v: currentPath){
                        int newColIndex = columnIndex + currentPath.indexOf(v);
                        if(newColIndex < TRIPCOUNTPERDAY && isValidStationSlot(Station.getStation(v.getName()), individualIndex, rowIndex, newColIndex)){
                            if(TCPs.get(individualIndex).stopPoints[rowIndex].size() <= newColIndex+1)
                                TCPs.get(individualIndex).stopPoints[rowIndex].add(newColIndex, Station.getStation(v.getName()));
                            else
                                TCPs.get(individualIndex).stopPoints[rowIndex].set(newColIndex, Station.getStation(v.getName()));
                                
//                            TCPsAsArray[individualIndex][rowIndex][newColIndex] = Station.getStation(v.getName());
                            System.out.println("station "+TCPs.get(individualIndex).stopPoints[rowIndex].get(newColIndex)+" at Index "+newColIndex+" of TCP "+rowIndex);
                            TCPValid = true;
                        }
                        else{
                            TCPValid = false;
                            break;
                        }
                        Station s1 = TCPs.get(individualIndex).stopPoints[rowIndex].get(newColIndex-1);
                        Station s2 = TCPs.get(individualIndex).stopPoints[rowIndex].get(newColIndex);
                        
                        WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(s1.getLabel()), Vertex.getVertex(s2.getLabel()));
                        if (w != null) 
                            validRunningDistance += w.getWeight();
                        
                    }
                    //reset parameters when path to DIRPort is valid
                    if(TCPValid){
                        TCPs.get(individualIndex).runningDistance[rowIndex] += validRunningDistance;
                        TCPs.get(individualIndex).maintenanceCount[rowIndex] += 1;
                        TCPs.get(individualIndex).distTraveledBeyondThreshold[rowIndex] += (TCPs.get(individualIndex).runningDistance[rowIndex] > MAINTENANCERUNNINGDISTANCE  ? 
                                TCPs.get(individualIndex).runningDistance[rowIndex] - MAINTENANCERUNNINGDISTANCE : 0);
                        TCPs.get(individualIndex).runningDistance[rowIndex] = .0;
                    }
                }else
                    break;
            }
        }
    }
    
    public static final void repairTrainSet_V2(Station s, int individualIndex, int rowIndex, int columnIndex){
//        int tcpIndex = (individualIndex + 1) * (rowIndex + 1) - 1;
        System.out.println("Repair Trainset variables: Individual Index -> "+(individualIndex + 1)+"; Row Index -> "+(rowIndex + 1));
        
        System.out.println("Undergoing repairs");
        if(Vertex.getVertex(s.getLabel()) instanceof DirPort){
            TCPs.get(individualIndex).stopPoints[rowIndex].add(columnIndex, s);
            System.out.println("current way station ("+s.getLabel()+") is a DIRPort");

            //if none of the stations devalidated the one trainset per station per time 
            TCPs.get(individualIndex).maintenanceCount[rowIndex] += 1;
            TCPs.get(individualIndex).distTraveledBeyondThreshold[rowIndex] += (TCPs.get(individualIndex).runningDistance[rowIndex] > MAINTENANCERUNNINGDISTANCE ? 
                    TCPs.get(individualIndex).runningDistance[rowIndex] - MAINTENANCERUNNINGDISTANCE : 0);
            TCPs.get(individualIndex).runningDistance[rowIndex] = .0;
            System.out.println("station "+TCPs.get(individualIndex).stopPoints[rowIndex].get(columnIndex)+" at Index "+columnIndex+" of TCP "+rowIndex);
        }
        else{
            
            boolean TCPValid = false; 
            int dirPortSkip = 0;

            while(!TCPValid){
                ObservableList<Vertex> currentPath = new UCSDIRPort(Vertex.getVertex(s.getLabel()), dirPortSkip++).getUCSPath().operator.peek().getPath();
                int validRunningDistance = 0;
                for(Vertex v: currentPath){
                    int newColIndex = columnIndex + currentPath.indexOf(v);
                    if(newColIndex < TRIPCOUNTPERDAY){
                        if(isValidStationSlot(Station.getStation(v.getName()), individualIndex, rowIndex, newColIndex)){
                            if(TCPs.get(individualIndex).stopPoints[rowIndex].size() <= newColIndex+1)
                                TCPs.get(individualIndex).stopPoints[rowIndex].add(newColIndex, Station.getStation(v.getName()));
                            else
                                TCPs.get(individualIndex).stopPoints[rowIndex].set(newColIndex, Station.getStation(v.getName()));

                            System.out.println("station "+TCPs.get(individualIndex).stopPoints[rowIndex].get(newColIndex)+" at Index "+newColIndex+" of TCP "+rowIndex);
                            TCPValid = true;

                        }else{
                            TCPValid = false;
                            System.out.println("generated path to DIRPort invalid, rerouting...");
    //                            dirPortSkip = 0;
                            break;
                        }
                        
                        Station s1 = TCPs.get(individualIndex).stopPoints[rowIndex].get(newColIndex-1);
                        Station s2 = TCPs.get(individualIndex).stopPoints[rowIndex].get(newColIndex);

                        WeightedEdge w = WeightedEdge.getWeightedEdge(Vertex.getVertex(s1.getLabel()), Vertex.getVertex(s2.getLabel()));
                        if (w != null) 
                            validRunningDistance += w.getWeight();
                    }else
                        break;
                }
                //reset parameters when path to DIRPort is valid
                if(TCPValid){
                    TCPs.get(individualIndex).runningDistance[rowIndex] += validRunningDistance;
                    TCPs.get(individualIndex).maintenanceCount[rowIndex] += 1;
                    TCPs.get(individualIndex).distTraveledBeyondThreshold[rowIndex] += (TCPs.get(individualIndex).runningDistance[rowIndex] > MAINTENANCERUNNINGDISTANCE  ? 
                            TCPs.get(individualIndex).runningDistance[rowIndex] - MAINTENANCERUNNINGDISTANCE : 0);
                    TCPs.get(individualIndex).runningDistance[rowIndex] = .0;
                }
            }
        }
    }
    
    //check station validity across rows
    public static final boolean isValidStationSlot(Station station, int individualIndex, int rowIndex, int columnIndex){
        System.out.println("checking slot validation of "+station);

        for(int i = 0; i < rowIndex; i++){
            if(station.equals(TCPs.get(individualIndex).stopPoints[i].get(columnIndex)))
                return false;
        }
        
        return true;
    }
    
    //TCP should pick another neighbour or increase connectingTime between trips
    public static final Station resolveIsValidStationSlot(Station s, int individualIndex, int rowIndex, int columnIndex){
        System.out.println("resolving station slot validation for station "+s);
        //if s is the first station for a TCP
        if (columnIndex == 0) {
            //get another random station
            s = Station.getStations().get((int) (Math.random() * Station.getStations().size()));
            //if s is not valid recursively find another random station
            if (!isValidStationSlot(s, individualIndex, rowIndex, columnIndex)) 
                resolveIsValidStationSlot(s, individualIndex, rowIndex, columnIndex);
            
            return s;
        } else {
            //resolve for non start stations
            Station prevStation = TCPs.get(individualIndex).stopPoints[rowIndex].get(columnIndex - 1);
            
            ArrayList<Vertex> neighbours = Vertex.getVertex(prevStation.getLabel()).getNeighbours();
            
            for(Vertex v: neighbours){
                if(isValidStationSlot(Station.getStation(v.getName()), individualIndex, rowIndex, columnIndex) && !v.equals(Vertex.getVertex(s.getLabel()))){
                    System.out.println("returned neighbouring station "+Station.getStation(v.getName()));
                    return Station.getStation(v.getName());
                }
            }
            
            System.out.println("returned same station");
            return prevStation;
        }
        
    }
    
    public static final Station getNextStation(Station s){
        int neighboursLength = Vertex.getVertex(s.getLabel()).getNeighbours().size();
        Vertex v = Vertex.getVertex(s.getLabel()).getNeighbours().get((int)(Math.random() * neighboursLength));
        Station nextStation = Station.getStation(v.getName());
        System.out.println("Previous station "+s+" and next station is "+nextStation);
        return nextStation;
    }
    
    //determine corresponding station at a given time based on runningDistance given the distance between each station in a TCP is not uniform
    public Station getStationAtTimeSlot(double connectingTime, int trainsetIndex){
        int currentIndex = 0;
        for(Station s: stopPoints[trainsetIndex]){
            int index = stopPoints[trainsetIndex].indexOf(s);
            if(index != 0){
                double weight = WeightedEdge.getWeightedEdge(Vertex.getVertex(stopPoints[trainsetIndex].get(index-1).getLabel()), 
                        Vertex.getVertex(s.getLabel())).getWeight();
                if(connectingTime < weight)
                    currentIndex = index;
                else
                    connectingTime -= weight;
            }
        }
        
        return stopPoints[trainsetIndex].get(currentIndex);
    }
    
//    public Station getStationAtIndex(Station s, int rowIndex, int columnIndex){
//        
//    }
//    
//    public boolean isValidAnimSlot(){
//        
//    }
//    
//    public void resolveValidAnimSlot(){
//        
//    }
    
    //determine runningtime of a trainset based on location (station) on its TCP
    public final double getConnectingTime(int stationIndex, int trainsetIndex){
        double totalConnectingTime = 0;
        if(stationIndex != 0){
            for(int i=0; i < stationIndex; i++){
                Vertex startVertex = Vertex.getVertex(this.stopPoints[trainsetIndex].get(i).getLabel());
                Vertex endVertex = Vertex.getVertex(this.stopPoints[trainsetIndex].get(i+1).getLabel());

                //where 10px/sec is the velocity of a trainset
                //runningTime is determined by time taken to travel from station[i] to station[++i]
                //3secs is the minimum connecting time between two stations
                totalConnectingTime += ((double)(WeightedEdge.getWeightedEdge(startVertex, endVertex).getWeight()) / 30) + connectingTime;
            }
            return totalConnectingTime;
        }
        else
            return totalConnectingTime;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof TCP){
            double fitnessValue1 = this.getFitness().get("passengerSatisfaction") + this.getFitness().get("tcpTrainUtilizationEfficiency");
            double fitnessValue2 = ((TCP) o).getFitness().get("passengerSatisfaction") + ((TCP) o).getFitness().get("tcpTrainUtilizationEfficiency");
            if (fitnessValue1 < fitnessValue2) {
                return -1;
            } else if (fitnessValue1 > fitnessValue2) {
                return 1;
            } else {
                return 0;
            }
        }else
            return 0;
    }
    
}
