import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
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
        wins.setText("");
        lose.setText("");
        duration.setText("");
        game1.setText("");
        touchs.setText("");
        game2.setText("");
        this.nom.setText('"'+nom+'"');
        JSONObject obj = new JSONObject("{}");
        obj.put("playerName", nom);
        UtilsHTTP.sendPOST(Main.protocol + "://" + Main.host + ":" + Main.port + "/getStatisticsPlayer", obj.toString(), (response) -> {
            JSONObject objResponse = new JSONObject(response);
            wins.setText(""+objResponse.getInt("MatchesWon"));
            lose.setText(""+objResponse.getInt("MatchesLost"));
            if (!objResponse.get("LongestMatch").equals(null)){
                JSONObject JSONlist = objResponse.getJSONObject("LongestMatch");
                String tiempo = JSONlist.getString("Duration");
                String[] partes = tiempo.split(":");
                int horas = Integer.parseInt(partes[0]);
                int minutos = Integer.parseInt(partes[1]);
                int segundos = Integer.parseInt(partes[2]);
                minutos += horas * 60;
                duration.setText(minutos+" minuts " +segundos+" segons");

                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(JSONlist.getString("Time_Stamp"), formatter);
                DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String fecha = offsetDateTime.format(formatterFecha);
                
                game1.setText(JSONlist.getInt("Player2Points")+" - "+JSONlist.getString("Player2Name")+" vs "+JSONlist.getString("Player1Name")+" - "+JSONlist.getInt("Player1Points")+" - "+fecha);
            }
            if (!objResponse.get("MatchWithMoreTouches").equals(null)){
                JSONObject JSONlist = objResponse.getJSONObject("MatchWithMoreTouches");

                touchs.setText(""+(JSONlist.getInt("Player1Touches")+JSONlist.getInt("Player2Touches")));
                
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(JSONlist.getString("Time_Stamp"), formatter);
                DateTimeFormatter formatterFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String fecha = offsetDateTime.format(formatterFecha);
                
                game2.setText(JSONlist.getInt("Player2Points")+" - "+JSONlist.getString("Player2Name")+" vs "+JSONlist.getString("Player1Name")+" - "+JSONlist.getInt("Player1Points")+" - "+fecha);
            }
        });
    }

}