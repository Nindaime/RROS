/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

/**
 *
 * @author PETER-PC
 */
public class WeightedEdge extends Line {
    static ArrayList<WeightedEdge> edges = new ArrayList<>();
    Vertex startV, endV;
    double weight;
    Label weightDisplay;


    public WeightedEdge(Vertex startV, Vertex endV) {
        super(startV.getCenterX(), startV.getCenterY(), endV.getCenterX(), endV.getCenterY());
        setStrokeWidth(1.25);
//        setOpacity(1);
        setBlendMode(BlendMode.SRC_OVER);
        setStroke(Color.web("#E7D19F"));
        setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 3, 0, 0, 0));
        this.startV = startV;
        this.endV = endV;
        weight = new Point2D(startV.getCenterX(), startV.getCenterY())
                .distance(new Point2D(endV.getCenterX(), endV.getCenterY()));
        setWeight();
    }
    
    public static WeightedEdge getWeightedEdge(Vertex startV, Vertex endV){
        for(WeightedEdge w: edges){
            if(w.equals(new WeightedEdge(startV, endV)))
                return w;
        }
        return null;
    }

    public final void setWeight() {
        weight = new Point2D(startV.getCenterX(), startV.getCenterY())
                .distance(new Point2D(endV.getCenterX(), endV.getCenterY()));
        weightDisplay = new Label(String.format("%.2f", weight));
        weightDisplay.setLabelFor(this);
        Point2D midPoint = startV.vertCoord.midpoint(endV.vertCoord);
        weightDisplay.setLayoutX(midPoint.getX());
        weightDisplay.setLayoutY(midPoint.getY());
        weightDisplay.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 5, 0, 0, 0));
        weightDisplay.setTextFill(Color.WHITE);
        weightDisplay.setFont(Font.font(14));
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        return weight == ((WeightedEdge) o).weight;
    }
}
