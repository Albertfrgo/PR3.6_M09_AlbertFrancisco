import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;

public class CtrlStartScreen {
    private int clientNumber;
    private String name,color;
    private static CtrlGameCanvas ctrlCanvas = new CtrlGameCanvas();

    @FXML
    private void setViewGame() {
        CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");

        Main.socketClient.onMessage((response) -> {
            // JavaFX necessita que els canvis es facin des de el thread principal
            Platform.runLater(()->{ 
                // Fer aquí els canvis a la interficie
    
                /* Recibimos msgObj del webSocket donde tendremos la info recibida, a partir de aqui
                * sacar la info y hacer cambios sobre el controlador de la logica de juego ctrlGame
                */
                JSONObject msgObj = new JSONObject(response);
                // System.out.println("The answer is" +msgObj.toString());
                if (msgObj.getString("type").equals("gameInfoBroadcast")){
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
                }else if(msgObj.getString("type").equals("playersInfo")){
                    CtrlGame.ctrlCanvas.setColor1(msgObj.getString("player1_Color"));
                    CtrlGame.ctrlCanvas.setName1(msgObj.getString("player1_Name"));
                    CtrlGame.ctrlCanvas.setColor2(msgObj.getString("player2_Color"));
                    CtrlGame.ctrlCanvas.setName2(msgObj.getString("player2_Name"));

                } 
            });
        });

        if (clientNumber==0){
            CtrlGame.ctrlCanvas.setColor1(color);
            CtrlGame.ctrlCanvas.setName1(name);
        }else if (clientNumber==1){
            CtrlGame.ctrlCanvas.setColor2(color);
            CtrlGame.ctrlCanvas.setName2(name);
        }
        ctrlGame.setClientNumber(clientNumber);
        UtilsViews.setViewAnimating("ViewGame");

        ctrlGame.startBallMovement();

        JSONObject objJson = new JSONObject("{}");
        String type = "startGame";
        objJson.put("type", type);
        objJson.put("playerName", name);
        objJson.put("playerColor", color);

        Main.socketClient.safeSend(objJson.toString());
    }

    @FXML
    private void setViewListPlayers() {
        CtrlListPlayers ctrl=(CtrlListPlayers) UtilsViews.getController("ViewListPlayers");
        ctrl.loadUsers();
        UtilsViews.setViewAnimating("ViewListPlayers");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setClientNumber(int clientNumber) {
        this.clientNumber = clientNumber;
    }
    

    

    
}
