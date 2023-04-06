import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class CtrlGame implements Initializable {

    @FXML
    private AnchorPane anchor;

    @FXML
    private Canvas canvas;

    private static CtrlGameCanvas ctrlCanvas = new CtrlGameCanvas();

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

    /* Funciones para pasar info desde el webSocket a la logica del juego
     * hacen este recorrido: Main -> CtrlGame -> CtrlGameCanvas
     */
    public void setClientNumber(int clNumber){
        ctrlCanvas.setClientNumber(clNumber);
    }

    public void updateParameters(JSONObject gameInfo){
        ctrlCanvas.updateParameters(gameInfo);
    }

    public void showBroadcastedInfo(String info){
        ctrlCanvas.showBroadcastedInfo(info);
    }

    /* Fin funciones de transferencia datos */

    public void drawingStart () {
        ctrlCanvas.start(canvas);
    }

    public void drawingStop () {
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
    }
}