package rros;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import java.sql.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Login Controller.
 */
public class LoginController extends AnchorPane implements Initializable, ControlledScreen {
    ScreenController myController;
    
    @Override
    public void setScreenParent(ScreenController screenParent){
        myController = screenParent;
    }

    @FXML
    private TextField userId;
    @FXML
    private PasswordField password;
    @FXML
    public Label errorMessage;
    @FXML
    private Button btnClose;     
    @FXML
    private AnchorPane container;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorMessage.setText("");
    }
    
    @FXML
    private void closeButtonAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
        
    @FXML
    private void hover_in(MouseEvent event){
        ImageView closeImage = (ImageView) btnClose.getGraphic();
        closeImage.setEffect(new ColorAdjust(0, -1, 0.71, 0));
    }
    
    @FXML
    private void hover_out(MouseEvent event){
        ImageView closeImage = (ImageView) btnClose.getGraphic();
        closeImage.setEffect(new ColorAdjust(0, -1, 0, 0));
    }
        
    @FXML
    private void requestfocus(MouseEvent event) {
        container.requestFocus();
    }
    
    double yOffset = 0;
    double xOffset = 0;
    
    @FXML
    private void determine(MouseEvent event){
        Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
    }
    
    @FXML
    private void pick(MouseEvent event){
        Scene scene = ((Node)event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();
        scene.setCursor(Cursor.CLOSED_HAND);
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }
    
    @FXML
    private void drop(MouseEvent event){
        Scene scene = ((Node)event.getSource()).getScene();
        scene.setCursor(Cursor.HAND);
    }    

    @FXML
    public void processLogin(ActionEvent event) throws SQLException, ClassNotFoundException, IOException {
        String queryString;
        PreparedStatement statement;
        ResultSet result;

        queryString = "Select username, usertype, profileImage from user where username = ? and password = ?";
        statement = DBConnection.connection.prepareStatement(queryString);
        statement.setString(1, userId.getText());
        statement.setString(2, password.getText());
        AnchorPane mainPageRoot = (AnchorPane) ScreenController.screens.get(RROS.mainPage);
       
        System.out.println("Main page root: "+mainPageRoot);

        result = statement.executeQuery();
        if(result.next()){
            myController.setScreen(RROS.mainPage);
            ((Label) mainPageRoot.lookup("#username")).setText(userId.getText());
            ((ImageView) mainPageRoot.lookup("#profileImage")).setImage(new Image(result.getBlob("profileimage").getBinaryStream()));
                    
            userId.setText("");
            password.setText("");
            errorMessage.setText("");
        } 
        else
            errorMessage.setText("Username/Password is incorrect");
    }

}
