import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class CtrlLogin {

    @FXML
    private Label error;

    @FXML
    private TextField user;

    @FXML
    private PasswordField password;


    @FXML
    private void setViewStart() {
        JSONObject obj = new JSONObject("{}");
            obj.put("nickname", user.getText());
            obj.put("code", password.getText());


            UtilsHTTP.sendPOST(Main.protocol + "://" + Main.host + ":" + Main.port + "/logPlayer", obj.toString(), (response) -> {
                JSONObject objResponse = new JSONObject(response);
                if (objResponse.getString("status").equals("OK")) {
                    CtrlStartScreen ctrl=(CtrlStartScreen) UtilsViews.getController("ViewStartScreen");
                    ctrl.setName(user.getText());
                    ctrl.setColor(objResponse.getString("Color"));
                    ctrl.setId(objResponse.getInt("Id"));
                    UtilsViews.setViewAnimating("ViewStartScreen");
                } else {
                    showError();
                }
            });
    }

    @FXML
    private void setViewRegister() {
        UtilsViews.setViewAnimating("ViewRegister");
    }

    private void showError () {
        // Show the error
        error.setVisible(true);

        // Hide the error after 3 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), ae -> error.setVisible(false)));
        timeline.play();
    }

    
}