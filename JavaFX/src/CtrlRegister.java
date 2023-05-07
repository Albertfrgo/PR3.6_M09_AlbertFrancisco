import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class CtrlRegister implements Initializable {

    @FXML
    private Label error;

    @FXML
    private TextField user;

    @FXML
    private PasswordField password, repeatPassword;

    @FXML
    private ComboBox colors;

    ObservableList<String> colorsComboboxOptions = 
    FXCollections.observableArrayList(
        "pink", "purple", "red", "orange", "yellow", "lime", "green", "cyan", "blue", "navy", "black"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colors.setItems(colorsComboboxOptions);
        colors.getSelectionModel().selectFirst();
    }

    @FXML
    private void setViewLogin() {
        UtilsViews.setViewAnimating("ViewLogin");
    }

    @FXML
    private void register(){
        if (password.getText().equals(repeatPassword.getText()) && !password.getText().isEmpty() && !user.getText().isEmpty()){
            JSONObject obj = new JSONObject("{}");
            obj.put("nickname", user.getText());
            obj.put("codePlayer", password.getText());
            obj.put("color", colors.getValue());


            UtilsHTTP.sendPOST(Main.protocol + "://" + Main.host + ":" + Main.port + "/savePlayer", obj.toString(), (response) -> {
                JSONObject objResponse = new JSONObject(response);
                if (objResponse.getString("status").equals("OK")) {
                    setViewLogin();
                } else {
                    showError();
                }
            });
        }else{
            showError();
        }
    }

    private void showError () {
        // Show the error
        error.setVisible(true);

        // Hide the error after 3 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), ae -> error.setVisible(false)));
        timeline.play();
    }


}
