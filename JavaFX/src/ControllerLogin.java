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

public class ControllerLogin implements Initializable {
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
        UtilsViews.setViewAnimating("ViewGame");
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

        ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
        Main.socketClient = UtilsWS.getSharedInstance(urlString);

        Main.socketClient.onMessage((response) -> {
            // JavaFX necessita que els canvis es facin des de el thread principal
            Platform.runLater(()->{ 
                // Fer aquí els canvis a la interficie
    
                /* Recibimos msgObj del webSocket donde tendremos la info recibida, a partir de aqui
                * sacar la info y hacer cambios sobre el controlador de la logica de juego ctrlGame
                */
                JSONObject msgObj = new JSONObject(response);
                // System.out.println("The answer is" +msgObj.toString());
                if(msgObj.getString("type").equals("infoConnection")){
                    // System.out.println("1 infoConnection received");

                    ctrlGame.setClientNumber(msgObj.getInt("clientNumber"));
                }else if (msgObj.getString("type").equals("gameInfoBroadcast")){
                    ctrlGame.updateParameters(msgObj.getJSONObject("gameInfo"));
                    String jsonString = msgObj.getJSONObject("gameInfo").toString();
                    String formattedJsonString = jsonString.replace(",", ",\n");
                    ctrlGame.showBroadcastedInfo(formattedJsonString);
                }else if (msgObj.getString("type").equals("countdown")){
                    // System.out.println("3 countdown received");

                    int numCountReceived = msgObj.getInt("message");
                    // System.out.println("count number received");
                    ctrlGame.setNumberCountdown(numCountReceived);
                    ctrlGame.hideSyncText();
                }    
            });
        });

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setNextView();

        ctrlGame.startBallMovement();

        JSONObject objJson = new JSONObject("{}");
        String type = "startGame";
        objJson.put("type", type);
        Main.socketClient.safeSend(objJson.toString());
    
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
