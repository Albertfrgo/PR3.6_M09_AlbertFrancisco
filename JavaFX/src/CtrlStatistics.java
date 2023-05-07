import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class CtrlStatistics {

    @FXML
    private Label nom, wins, lose, duration, game1, touchs, game2;


    @FXML
    private void setViewListPlayers() {
        UtilsViews.setViewAnimating("ViewListPlayers");
    }

    public void charge(String nom){
        this.nom.setText('"'+nom+'"');
        JSONObject obj = new JSONObject("{}");
        obj.put("playerName", nom);
        UtilsHTTP.sendPOST(Main.protocol + "://" + Main.host + ":" + Main.port + "/getStatisticsPlayer", obj.toString(), (response) -> {
            System.out.println(response);
            
        });
    }

}