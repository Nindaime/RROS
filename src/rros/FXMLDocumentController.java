/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author PETER-PC
 */
public class FXMLDocumentController implements Initializable, ControlledScreen {
    ScreenController myController;

    @Override
    public void setScreenParent(ScreenController screenParent) {
        myController = screenParent;
    }
    
    @FXML
    private ProgressBar pBar;
    
    @FXML
    private Button btnClose;
    
    @FXML
    private AnchorPane topPane;
        
    @FXML
    private void closeButtonAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void hover_in(MouseEvent event) {
        ((Button)event.getSource()).setEffect(new ColorAdjust(0, -1, 0.31, 0));
    }

    @FXML
    private void hover_out(MouseEvent event) {
        ((Button)event.getSource()).setEffect(new ColorAdjust(0, -1, 0, 0));
    }

    @FXML
    private void requestfocus(MouseEvent event) {
        topPane.requestFocus();
    }

    double yOffset = 0;
    double xOffset = 0;

    @FXML
    private void determine(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
    }

    @FXML
    private void pick(MouseEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();
        ((Node) event.getSource()).setCursor(Cursor.CLOSED_HAND);
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }

    @FXML
    private void drop(MouseEvent event) {
        ((Node) event.getSource()).setCursor(Cursor.OPEN_HAND);
    }
    
    @FXML
    private void reveal(MouseEvent event) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(250), e-> ((Node) event.getSource()).setCursor(Cursor.OPEN_HAND), 
                new KeyValue(((Node) event.getSource()).opacityProperty(), 1, Interpolator.EASE_OUT)));
        tl.setDelay(Duration.millis(100));
        tl.play();
    }

    @FXML
    private void hide(MouseEvent event) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(250), e-> ((Node) event.getSource()).setCursor(Cursor.OPEN_HAND), 
                new KeyValue(((Node) event.getSource()).opacityProperty(), .1, Interpolator.EASE_OUT)));
        tl.setDelay(Duration.millis(100));
        tl.play();
    }
    
    @FXML
    private void getCoordinate(MouseEvent event){
        System.out.println ("x: "+event.getSceneX()+"; y: "+event.getSceneY());
    }
    
    @FXML
    private AnchorPane container;
            
    @FXML
    private void logout(ActionEvent event){
        for(Node n: container.getChildren()){
            System.out.println("Pane: "+n);
            if(n instanceof GraphInit){
                myController.getChildren().remove(n);
                System.out.println("graph init removed");
            }
        }
        myController.setScreen(RROS.login);
    }
    
    public static void displaySubMenu(String subWindow){
        subController.setScreen(subWindow);
        Stage subMenu = new Stage();
        subMenu.setWidth(600);
        subMenu.setHeight(400);

        subMenu.initOwner(RROS.window);
        subMenu.initModality(Modality.WINDOW_MODAL);
        subMenu.initStyle(StageStyle.UNDECORATED);

        //display popupWindow at Center of Modal Window
        subMenu.setX((((Stage) subMenu.getOwner()).getX() + (((Stage) subMenu.getOwner()).getWidth() / 2) - 300));
        subMenu.setY((((Stage) subMenu.getOwner()).getY() + (((Stage) subMenu.getOwner()).getHeight() / 2) - 200));

        Group root = new Group();
        root.getChildren().addAll(subController);
        Scene scene = new Scene(root);
        subMenu.setScene(scene);
        subMenu.show();
    }
    
    public static ScreenController subController;
    public static String FitnessChart = "FitnessChart";
    public static String FitnessChartWindow = "FitnessChart.fxml";
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        subController = new ScreenController();
        subController.loadScreen(FitnessChart, FitnessChartWindow);
        
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Generate TCPs");
        item1.setOnAction((ActionEvent e) -> {
            contextMenu.hide();
            new Thread(() -> {   GraphInit g = new GraphInit();  }).start();
        });
        
        MenuItem item2 = new MenuItem("Optimize TCP");
        item2.setOnAction((ActionEvent e) -> {
            new Thread(()->{
                TCP1.optimize(0.3F, 0.1F, 4);
                //parameters -> (cross-over possibilty, mututation-possibility, number of generations)
            }).start();
        });
        
        MenuItem item3 = new MenuItem("Pause Animation");
        item3.setOnAction((ActionEvent e) -> {
            TCP1.currentAnimation.pause();
            TCP1.timer1.pause();
        });
        
        MenuItem item4 = new MenuItem("Play Animation");
        item4.setOnAction((ActionEvent e) -> {
            TCP1.currentAnimation.play();
            TCP1.timer1.play();
        });
        
        contextMenu.getItems().addAll(item1, item2, item3, item4);

        container.setOnMouseClicked(e ->{
            if(e.getButton() == MouseButton.SECONDARY)
                contextMenu.show(container, e.getScreenX(), e.getScreenY());
            else if(e.getButton() == MouseButton.PRIMARY)
                contextMenu.hide();
        });
        
    }    
    
}
