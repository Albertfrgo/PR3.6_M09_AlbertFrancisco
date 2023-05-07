import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class CtrlListPlayers {
    
    @FXML
    private VBox yPane = new VBox();
    
    @FXML
    private void setViewStartScreen() {
        UtilsViews.setViewAnimating("ViewStartScreen");
    }

    public void loadUsers(){
        yPane.getChildren().clear();
        
        
        JSONObject obj = new JSONObject("{}");
        UtilsHTTP.sendPOST(Main.protocol + "://" + Main.host + ":" + Main.port + "/get_all_rankings", obj.toString(), (response) -> {

            JSONObject objResponse = new JSONObject(response);
            if (objResponse.getString("status").equals("OK")) {

                JSONArray JSONlist = objResponse.getJSONArray("result");
                URL resource = this.getClass().getResource("./assets/itemPlayer.fxml");

                for(int i = 0; i < JSONlist.length(); i++){ 
                    
                    try{
                        // Load template and set controller
                    FXMLLoader loader = new FXMLLoader(resource);
                    Parent itemTemplate = loader.load();
                    CtrlItemPlayer ItemPlayerController = loader.getController();
                        
                        // Fill user item 
                        ItemPlayerController.setName(JSONlist.get(i).toString());

                        yPane.getChildren().add(itemTemplate);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

