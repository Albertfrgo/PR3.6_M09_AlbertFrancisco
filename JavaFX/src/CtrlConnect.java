import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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

public class CtrlConnect implements Initializable {
    /* 
     * Haura de treballar amb dues variables, el port i el servidor, estatica
     * 
     * Quan s'entrin el nom i cognom, s'apreta el boto i ens porta automaticament
     * a la seguent vista
     */
    @FXML
    private Label labelServidor, labelPort;

    @FXML
    private TextField txtServidor, txtPort;

    @FXML
    private Button buttonLogin;

    private CtrlGame ctrlGame;

    @FXML
    private void setNextView() {
        // System.out.println("Opening filter view");
        UtilsViews.setViewAnimating("ViewLogin");

        /* Envio de post hardcodeado para probar como se comportan
         * HTTPS y WSS a la vez*/
        // JSONObject obj = new JSONObject("{}");
        // // obj.put("type", "usersInfo");
        // System.out.println("We will send this post "+Main.protocol + "://" + Main.host + ":" + Main.port + "/getUsers");
        // UtilsHTTP.sendPOST(Main.protocol + "://" + Main.host + ":" + Main.port + "/getUsers", obj.toString(), (response) -> {
        //     callBackGetusers(response);
        // });
    }

    private static void callBackGetusers(String response) {
        String printedJson = response;
        System.out.println("The json received by the post /getUsers:" + printedJson);
    }

    @FXML
    private void login(){
        boolean loginOK =false;
        String inputServidor, inputPort;
        inputServidor = txtServidor.getText();
        inputPort = txtPort.getText();

        try{
            Main.port = Integer.parseInt(inputPort);
            loginOK = true;
        }catch(Exception e){
            UtilsAlerts.alertError("Error en login", "El port no esta en un format correcte");
        }
        Main.host = inputServidor;

        if(loginOK){
            setNextView();
        }
    }

    @FXML
    private void loginNoPort(){
        String urlString = txtServidor.getText();

        Main.socketClient = UtilsWS.getSharedInstance(urlString);
        setNextView();

    }



    @FXML
    private void fastLogin(){
        //Funcio provisional per fer proves i no haver d'entrar la contrasenya cada cop
        setNextView();
        ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
        ctrlGame.startBallMovement();

        JSONObject objJson = new JSONObject("{}");
        String type = "startGame";
        objJson.put("type", type);
        // Main.socketClient.safeSend(objJson.toString());
        // System.out.println("Send WebSocket: " + objJson.toString());
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
    }
}
