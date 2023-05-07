
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CtrlItemPlayer{
       
    @FXML
    private Label name;


    public void setName(String name){
        this.name.setText(name);
    }
    

    @FXML
    private void handleMenuAction() {
        CtrlStatistics ctrl=(CtrlStatistics) UtilsViews.getController("ViewStatistics");
        ctrl.charge(name.getText());
        UtilsViews.setViewAnimating("ViewStatistics");

        
    }

}