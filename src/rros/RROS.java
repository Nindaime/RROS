/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rros;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author PETER-PC
 */
public class RROS extends Application {
    public static String login = "LoginPage";
    public static String loginFile = "Login.fxml";
    public static String mainPage = "mainPage";
    public static String mainPageFile = "FXMLDocument.fxml";
    
    public static Stage window;
    
    @Override
    public void start(Stage stage) throws Exception {  
        window = stage;
        ScreenController mainContainer = new ScreenController();
        mainContainer.loadScreen(login, loginFile);
        mainContainer.loadScreen(mainPage, mainPageFile);

        mainContainer.setScreen(login);

        Scene scene = new Scene(mainContainer);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new DBConnection();
        launch(args);
    }
    
}
