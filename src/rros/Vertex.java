/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author PETER-PC
 */
public class Vertex extends Circle{
    public TrainSet trainSet;
    public int tripNumber;
    public Vertex departureStation, arrivalStation;
    public int travelerPopulation;
    
    static ArrayList<Vertex> vertices = new ArrayList<>();
    private final ArrayList<Vertex> neighbours = new ArrayList<>();
    public Point2D vertCoord;
    public Label name, passengerStatus;

    public Vertex(String name, double x, double y) {
        this.name = new Label(name);
        passengerStatus = new Label("0|0");
        passengerStatus.setFont(Font.font("System", FontWeight.LIGHT, 10));
        passengerStatus.setTextFill(Color.WHITE);
        passengerStatus.setStyle("-fx-background-color: cadetblue");
        if(name.matches("Abuja")){
            passengerStatus.setLayoutX(x-7);
            passengerStatus.setLayoutY(y-25);
        }else{
            passengerStatus.setLayoutX(x-7);
            passengerStatus.setLayoutY(y+8);
        }
        setRadius(5);
        vertices.add(this);
        RadialGradient colorFill = new RadialGradient(0, 0, 0.2, 0.3, 1, true, CycleMethod.NO_CYCLE, new Stop[]{
            new Stop(0, Color.rgb(250, 250, 255)),
            new Stop(1, Color.CADETBLUE)
        });
        setFill(colorFill);
        setEffect(new InnerShadow(10, Color.CADETBLUE.darker()));
        setCursor(Cursor.HAND);
        this.setCenterX(x);
        this.setCenterY(y);
        vertCoord = new Point2D(x, y);
        Tooltip.install(this, new Tooltip(name + ": " + x + " " + y));
        this.name.setLabelFor(this);
    }

    public Label getPassengerStatus(){
        return passengerStatus;
    }
    
    public ArrayList<Vertex> getNeighbours() {
        sortNeighbours(neighbours);
        return neighbours;
    }
    
    public static Vertex getVertex(String s){
        for(Vertex v: vertices){
            if(v.getName().matches(s))
                return v;
        }
        return null;
    }
   
    //sort neighbors from closest to farthest
    public void sortNeighbours(ArrayList<Vertex> neighbours){
        double[] weights = new double[neighbours.size()];
        
        for(Vertex v: neighbours){
            for(WeightedEdge w: WeightedEdge.edges){
                if(w.equals(new WeightedEdge(this, v)))
                    weights[neighbours.indexOf(v)] = w.getWeight();
            }
        }
        
        //perform insertion sort on neighbours based on weights
        for(int i = 1; i < weights.length; i++){
            double currentElement = weights[i];
            Vertex currentVertex = neighbours.get(i);
            
            int k;
            for(k = i-1; k >= 0 && weights[k] > currentElement; k--){
                weights[k+1] = weights[k];
                neighbours.set(k+1, neighbours.get(k));
            }
            
            weights[k+1] = currentElement;
            neighbours.set(k+1, currentVertex);
            
        }
    }

    public Point2D getCoordinates() {        return new Point2D(this.getCenterX(), this.getCenterY());    }

    public String getName() {        return name.getText();    }

    @Override
    public String toString() {        return getName();    }

    @Override
    public boolean equals(Object o) {        return this.getName().equals(((Vertex) o).getName());    }    
}