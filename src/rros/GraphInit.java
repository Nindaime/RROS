/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 *
 * @author PETER-PC
 */
public class GraphInit extends Pane{
        
    public GraphInit(){     
        
        setStyle("-fx-background: gainsboro");
        
        String[] vertices = {
            "Sokoto",
            "Kano",
            "Zaria",
            "Bauchi",
            "Jos",
            "Abuja",
            "Benin-City",
            "Warri",
            "Port-Harcourt",
            "Calabar",
            "Ibadan",
            "Lagos",
            "Ogbomosho",
            "Maiduguri",
            "Biu",
            "Kaduna",
            "Gombe",
            "Onitsha"};
        
        double[][] vertexCoordinates = {
            {187.0, 95.0},
            {385.0, 155.0},
            {333.0, 211.0},
            {459.0, 259.0},
            {401.0, 285.0},
            {314.0, 334.0},
            {207.0, 497.0},
            {219.0, 544.0},
            {294.0, 588.0},
            {371.0, 578.0},
            {109.0, 437.0},
            {75.0,  485.0},
            {127.0, 389.0},
            {657.0, 169.0},
            {600.0, 242.0},
            {315.0, 246.0},
            {539.0, 260.0},
            {278.0, 509.0}};

        String[][] Edges = {
            {"Zaria", "Ogbomosho"},
            {"Zaria", "Bauchi", "Maiduguri"},
            {"Kaduna", "Jos"},
            {"Jos", "Maiduguri"},
            {"Abuja", "Kaduna"},
            {"Benin-City"},
            {"Warri", "Ibadan", "Onitsha"},
            {"Onitsha", "Port-Harcourt"},
            {"Calabar","Onitsha"},
            {"Biu"},
            {"Lagos", "Ogbomosho"},
            {"Benin-City"},
            {"Abuja"},
            {"Biu"},
            {"Gombe"},
            {"Abuja"},
            {"Bauchi"},
            {"Abuja"}};

        //initializing vertices into graph
        Vertex vertex;
        for (int i = 0; i < vertices.length; i++) {
            if(vertices[i].matches("Lagos") || vertices[i].matches("Kano") || 
                    vertices[i].matches("Abuja") || vertices[i].matches("Maiduguri") || 
                    vertices[i].matches("Sokoto") || vertices[i].matches("Calabar"))
                vertex = new DirPort(vertices[i], vertexCoordinates[i][0], vertexCoordinates[i][1], this);
            else
                vertex = new Vertex(vertices[i], vertexCoordinates[i][0], vertexCoordinates[i][1]);
            
            getChildren().addAll(vertex, vertex.getPassengerStatus());
        }
        
        //create neighbours
        for (int i = 0; i < vertices.length; i++) {
            System.out.println("Vertex "+i+": "+Vertex.vertices.get(i));
            
            for (String s : Edges[i]) {
                int j = Vertex.vertices.indexOf(Vertex.getVertex(s));
                //add end vertex[j] to start vertex[i] neighbor list
                Vertex.vertices.get(i).getNeighbours().add(Vertex.vertices.get(j));
                //add start vertex[i] to end vertex[j] neighbour list
                if (!(Vertex.vertices.get(j).getNeighbours().contains(Vertex.vertices.get(i)))) {
                    Vertex.vertices.get(j).getNeighbours().add(Vertex.vertices.get(i));
                }
                
                WeightedEdge e = new WeightedEdge(Vertex.vertices.get(i), Vertex.vertices.get(j));

                //can only add e to edge list if it doesn't exist
                if (!WeightedEdge.edges.contains(e)) {
                    WeightedEdge.edges.add(e);
                }
            }
        }
        
        //dispay edges and weights on the scenegraph
        for (WeightedEdge e : WeightedEdge.edges) {
            getChildren().add(0, e);
            getChildren().add(1, e.weightDisplay);
        }

//      Print Graph in Console
        for (Vertex e : Vertex.vertices) {

            System.out.print(e + "\t\t->");
            for (Vertex n : e.getNeighbours()) {
                System.out.print(n + " : ");
            }
            System.out.println();
        }
        
        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
        AnchorPane container = (AnchorPane) mainPageRoot.lookup("#container");
        Platform.runLater(() -> { container.getChildren().add(this); });
        TCP1.generateTCPs();
//        initSimulation();
    }
    
    
    public final void initSimulation(){
        TCP1.generateTCPs();
        
//        for(TCP t: TCP.TCPs){
//            int index = TCP.TCPs.indexOf(t);
//            Passenger.resetPassengers(index);
//            for (int i = 0; i < t.getTrainSet().length; i++) {
//                t.getTrainSet()[i].setVisible(false);
//                getChildren().add(t.getTrainSet()[i]);
//                t.getAnimation()[i].setRate(20);
//                pTs.getChildren().add(t.getAnimation()[i]);
//                System.out.println("Animation TCP "+t.getTrainSet()[i]+" is set");
//            }
//            System.out.println("Simulating Animation for TCP "+index);
//            Platform.runLater(() -> {pTs.playFromStart();});
//            t.setFitness(index);
//        }
//
//        int currentGBestIndex = TCP.TCPs.indexOf(TCP.gBests.get(0));
//        Passenger.resetPassengers(currentGBestIndex);
        
//        //animate only the first 6 TCPs
//        TCP animatedTCP = TCP.TCPs.get(0);
////        animatedTCP.setAnimation();
//
//        for (int i = 0; i < animatedTCP.getTrainSet().length; i++) {
//            getChildren().add(animatedTCP.getTrainSet()[i]);
//            pTs.getChildren().add(animatedTCP.getAnimation()[i]);
//            System.out.println("Animation TCP "+animatedTCP.getTrainSet()[i]+" is set");
//        }
    }
    
    //print neighbours according to their proximity
    public final void printNeighbours(){
        for(Vertex v: Vertex.vertices){
            System.out.println("For the Vertex "+v.getName()+": ");
            for(Vertex n: v.getNeighbours()){
                System.out.println("Neighbour "+v.getNeighbours().indexOf(n)+" ("+n.getName()+"):Weight is "+WeightedEdge.getWeightedEdge(v, n).getWeight());
            }
        }
    }
    
    
}