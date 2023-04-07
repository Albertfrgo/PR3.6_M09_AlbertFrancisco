import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ControllerEndGame implements Initializable{

    @FXML
    private Label labelWinner;

    @FXML
    private Button buttonPlayAgain;

    CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");

    @FXML
    private void playAgain(){
        UtilsViews.setViewAnimating("ViewGame");

        /* Enviar un mensaje al ws de play Again 
         * Cuando ambos hayan mandado play again, el ws reinicia todo
         * y vuelve a empezar el juego
        */
        JSONObject objJson = new JSONObject("{}");
        String type = "playAgain";
        objJson.put("type", type);

        Main.socketClient.safeSend(objJson.toString());
    }


    public void setWinnerName(String name){
        labelWinner.setText("The winner is: "+name);
        ctrlGame.drawingStop();
    }

    public void initialize(URL arg0, ResourceBundle arg1) {
    }
    
}
