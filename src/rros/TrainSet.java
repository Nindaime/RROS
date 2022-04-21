/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author PETER-PC
 */
public class TrainSet extends HBox{
    private int cCount;
    private final String trainSetName;
    private final ObservableList<Passenger> onBoardPassengers = FXCollections.observableArrayList();
    private final Label label;
    public static final int CAPACITYPERCAR = 20;
    private final String[] trainsetColors = {"black", "grey", "orange", "cadetblue","orangered", "goldenrod"};
        
//    private double runningDistance = 0, runningTime = 0;
    
    public TrainSet(String name, int cCount, int index){
        super(1);
        label = new Label(" "+name.charAt(name.length()-1)+" ");
        label.setFont(Font.font("System", FontWeight.LIGHT, 11.5));
        this.cCount = cCount;
        trainSetName = name;
        getStyleClass().add("trainset");
        getChildren().add(label);
        label.setTextFill(Color.WHITE);
        setMargin(label, new Insets(0,1,0,0));
        label.setStyle("-fx-background-color: "+trainsetColors[index]);
        
        //build carraige
        for(int i = 0; i < cCount; i++){
            ImageView body = new ImageView(new Image(getClass().getResourceAsStream("body/body"+index+".png")));
            setMargin(body, new Insets(2,0,0,0));
            getChildren().add(body);
        }
        
        ImageView head = new ImageView(new Image(getClass().getResourceAsStream("head/head"+index+".png")));
        setMargin(head, new Insets(2,0,0,0));
        getChildren().add(head);
        
        //color train
        for(Node n: getChildren()){
            if(n instanceof ImageView){
                ((ImageView) n).setPreserveRatio(true);
                ((ImageView) n).setFitWidth(40);
            }
        }
        
    }
    
    public void setPassengerCount(String pCount){
        
    }
    
    public ObservableList<Passenger> getOnBoardPassenger(){
        return onBoardPassengers;
    }
    
    public String getName(){
        return trainSetName;
    }
    
    public int getCarCount(){
        return cCount;
    }
    
    public void setCapacity(int cCount){
        this.cCount = cCount;
    }
    
    @Override
    public String toString(){
        return trainSetName;
    }
}