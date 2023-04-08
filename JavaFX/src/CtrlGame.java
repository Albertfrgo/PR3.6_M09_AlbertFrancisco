import java.net.URL;
import javafx.util.Duration;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;

public class CtrlGame implements Initializable {

    @FXML
    private AnchorPane anchor;

    @FXML
    private Canvas canvas;

    @FXML
    private Label labelWinner, labelGameOver;

    @FXML
    private Button buttonPlayAgain;

    @FXML
    private GridPane gridInfo;

    @FXML
    private Label countdownLabel, syncLabel;

    private static CtrlGameCanvas ctrlCanvas = new CtrlGameCanvas();

    private boolean showingGameOver = false;

    private Timeline timeline;
    private static int segundosRestantes = 3;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize canvas responsive size
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> {
            updateCanvasSize();
        });
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> {
            updateCanvasSize();
        });
    }

    @FXML
    private void playAgain(){
        /* Enviar un mensaje al ws de play Again 
         * Cuando ambos hayan mandado play again, el ws reinicia todo
         * y vuelve a empezar el juego
        */
        hideGameOver();

        JSONObject objJson = new JSONObject("{}");
        String type = "playAgain";
        objJson.put("type", type);

        Main.socketClient.safeSend(objJson.toString());
        ctrlCanvas.winnerDecided = false;
    }

    public void hideSyncText(){
        System.out.println("CtrlGame: hideSyncText");
        syncLabel.setVisible(false);
        // showCountDownLabel();
    }

    public void setNumberCountdown(int number){
        if(number == 0){
            hideCountdownLabel();
        }else{
            countdownLabel.setVisible(true);
            String numberText = String.valueOf(number);
            countdownLabel.setText(numberText);
        }
    }

/*     public void showCountDownLabel(){
        System.out.println("CtrlGame: showCountDownLabel");
        countdownLabel.setVisible(true);
        countdownLabel.setText("3");
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            segundosRestantes--;
            System.out.println("segundosRestantes: "+segundosRestantes);
            if (segundosRestantes <= 0) {
                timeline.stop();
                hideCountdownLabel();
                // iniciarJuego();
            } else {
                countdownLabel.setText(String.valueOf(segundosRestantes));
            }
        }));
        timeline.setCycleCount(3);
        timeline.setOnFinished(event -> {
            hideCountdownLabel();
            iniciarJuego();
        });
        timeline.play();
    } */

/*     private void iniciarJuego(){
        System.out.println("CtrlGame: iniciarJuego");
        countdownLabel.setVisible(false);
        JSONObject objJson = new JSONObject("{}");
        String type = "countEnded";
        objJson.put("type", type);
        Main.socketClient.safeSend(objJson.toString());
    } */

    public void hideCountdownLabel(){
        countdownLabel.setVisible(false);
    }

    public void setWinnerName(String name){
        System.out.println("CtrlGame: setWinnerName");
        labelWinner.setText("The winner is: "+name);
        showGameOver();
    }

    /* Funciones para pasar info desde el webSocket a la logica del juego
     * hacen este recorrido: Main -> CtrlGame -> CtrlGameCanvas
     */
    public void setClientNumber(int clNumber){
        ctrlCanvas.setClientNumber(clNumber);
    }

    public void updateParameters(JSONObject gameInfo){
        ctrlCanvas.updateParameters(gameInfo);
        if(gameInfo.getString("gameStatus").equals("playing") && showingGameOver== true){
            hideGameOver();
        }
    }

    public void showBroadcastedInfo(String info){
        ctrlCanvas.showBroadcastedInfo(info);
    }

    /* Fin funciones de transferencia datos */

    public void drawingStart () {
        ctrlCanvas.start(canvas);
    }

    public void drawingStop () {
        System.out.println("CtrlGame: drawingStop");
        ctrlCanvas.stop();
    }

    public void updateCanvasSize () {

        final double width = UtilsViews.parentContainer.getWidth();
        final double height = UtilsViews.parentContainer.getHeight();

        // Set Canvas size
        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    public void keyEvent (KeyEvent evt) {

        // Quan apretem una tecla
        if (evt.getEventType() == KeyEvent.KEY_PRESSED) {
            if (evt.getCode() == KeyCode.UP) {
                ctrlCanvas.playerDirection = "up";
            }
            if (evt.getCode() == KeyCode.DOWN) {
                ctrlCanvas.playerDirection = "down";
            }
        }

        // Quan deixem anar la tecla
        if (evt.getEventType() == KeyEvent.KEY_RELEASED) {
            if (evt.getCode() == KeyCode.UP) {
                if (ctrlCanvas.playerDirection.equals("up")) {
                    ctrlCanvas.playerDirection = "none";
                }
            }
            if (evt.getCode() == KeyCode.DOWN) {
                if (ctrlCanvas.playerDirection.equals("down")) {
                    ctrlCanvas.playerDirection = "none";
                }
            }
        }
    }

    public void startBallMovement(){
        ctrlCanvas.startBallMovement();
        hideGameOver();
    }

    private void hideGameOver(){
        showingGameOver = false;
        System.out.println("CtrlGame: hideGameOver");
        gridInfo.setVisible(false);
        gridInfo.setManaged(false);
        buttonPlayAgain.setVisible(false);
        labelGameOver.setVisible(false);
        labelWinner.setVisible(false);
        buttonPlayAgain.setDisable(true);
    }

    private void showGameOver(){
        showingGameOver = true;
        System.out.println("CtrlGame: showGameOver");
        gridInfo.setVisible(true);
        gridInfo.setManaged(true);
        buttonPlayAgain.setVisible(true);
        labelGameOver.setVisible(true);
        labelWinner.setVisible(true);
        buttonPlayAgain.setDisable(false);
    }
}