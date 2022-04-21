/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author PETER-PC
 */
public class DirPort extends Vertex{
    public Circle dirPortCircle;
    
    public DirPort(String name, double x, double y, Pane container) {
        super(name, x, y);
        dirPortCircle = new Circle(x, y, 6);
        dirPortCircle.setStroke(Color.BLACK);
        dirPortCircle.setStrokeWidth(2.5);
        container.getChildren().add(dirPortCircle);
    }
   
    
}
