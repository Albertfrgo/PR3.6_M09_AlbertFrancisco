import java.io.IOError;
import java.io.IOException;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    private CtrlGame ctrlGame;

    public static UtilsWS socketClient = null;

    /* Datos que habra que adaptar a Railway */
    public static int port = 443;
    public static String protocol = "https";
    public static String host = "prpong-production.up.railway.app";
    public static String protocolWS = "ws";

    public static String server = "localhost";

    public static void main(String[] args) {

        // Iniciar app JavaFX   
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        try{
            final int windowWidth = 800;
            final int windowHeight = 600;

            UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
            UtilsViews.addView(getClass(), "Login", "./assets/viewConnect.fxml");
            UtilsViews.addView(getClass(), "ViewGame", "./assets/viewGame.fxml");
            // UtilsViews.addView(getClass(), "EndGame", "./assets/endGameScreen.fxml");
            ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
            
            Scene scene = new Scene(UtilsViews.parentContainer);
            scene.addEventFilter(KeyEvent.ANY, keyEvent -> { ctrlGame.keyEvent(keyEvent); });
            
            stage.setScene(scene);
            stage.onCloseRequestProperty(); // Call close method when closing window
            stage.setTitle("JavaFX - Pong");
            stage.setMinWidth(windowWidth);
            stage.setMinHeight(windowHeight);
            stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> { ctrlGame.drawingStart(); });
            stage.show();

            // Add icon only if not Mac
            if (!System.getProperty("os.name").contains("Mac")) {
                Image icon = new Image("file:./assets/icon.png");
                stage.getIcons().add(icon);
            }

            
            // Iniciar WebSockets
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void stop() { 
        ctrlGame.drawingStop();

        if(Main.socketClient != null){
            JSONObject objJson = new JSONObject("{}");
            String type = "stopGame";
            objJson.put("type", type);
            Main.socketClient.safeSend(objJson.toString());
            // System.out.println("Send WebSocket: " + objJson.toString());
        }

        System.exit(1); // Kill all executor services
    }

}
