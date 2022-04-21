/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author PETER-PC
 */
public class FitnessChartController implements Initializable, ControlledScreen{

    ScreenController myController;
    //visualize optimization using scatter chart
    @FXML
    private LineChart chart;
    
    @FXML 
    private Button btnClose;
    
    @FXML
    private void closeButtonAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
        
        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
        AnchorPane container = (AnchorPane) mainPageRoot.lookup("#container");
        TCP1.playCurrentAnimation(mainPageRoot, container);

    }
    
    static XYChart.Series fitness = new XYChart.Series<>();
    /**
     * Initializes the controller class.
     */
    
    public static void loadData(){
        fitness.setName("Fitness Values");
        System.out.println("Loading XYChart data");
        for(TCP t: TCP.gBests){
            System.out.println("Passenger Staisfaction: "+t.getFitness().get("passengerSatisfaction")+
                    ", Train utilization efficiency: "+t.getFitness().get("tcpTrainUtilizationEfficiency"));

            fitness.getData().add(new XYChart.Data<>(+t.getFitness().get("passengerSatisfaction")+"", 
                    t.getFitness().get("tcpTrainUtilizationEfficiency")));
            System.out.println("loaded data"+TCP.gBests.indexOf(t));
        }
    }
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        chart.getData().add(fitness);
        chart.getXAxis().setLabel("Passenger Satisfaction");
        chart.getYAxis().setLabel("Train Utilization Efficiency");
    }   
    //build another FXML to visualize individual fitness in a generation

    @Override
    public void setScreenParent(ScreenController pageParent) {
        myController = pageParent;
    }
    
}
