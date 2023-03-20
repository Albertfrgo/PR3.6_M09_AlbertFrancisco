public class MsgWebSocket {
    String type = "bounce";
    String text = "";

    MsgWebSocket (String type, String text) {
        this.type = type;
        this.text = text;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }
}
