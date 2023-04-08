import java.time.LocalTime;

import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CtrlGameCanvas {
  
    private Canvas cnv;
    private GraphicsContext gc;
    private AnimationTimer animationTimer;

    private double borderSize = 5;

    private String gameStatus = "playing";

    private int playerPoints = 0;
    private double playerX = Double.POSITIVE_INFINITY;
    private double playerY = Double.POSITIVE_INFINITY;
    private final double playerWidth = 5;
    private final double playerHeight = 200;
    private final double playerHalf = playerHeight / 2;
    private double playerSpeed = 250;
    private final double playerSpeedIncrement = 15;
    public String playerDirection = "none";

    private int playerPoints2 = 0;
    private double playerX2 = Double.POSITIVE_INFINITY;
    private double playerY2 = Double.POSITIVE_INFINITY;
    private final double playerWidth2 = 5;
    private final double playerHeight2 = 200;
    private final double playerHalf2 = playerHeight2 / 2;
    private double playerSpeed2 = 250;
    private final double playerSpeedIncrement2 = 15;
    public String playerDirection2 = "none";

    private double ballX = Double.POSITIVE_INFINITY;
    private double ballY = Double.POSITIVE_INFINITY;
    private final double ballSize = 15;
    private final double ballHalf = ballSize / 2;
    private double ballSpeed = 0;
    private final double ballSpeedIncrement = 25;
    private String ballDirection = "upRight";

    private long currentTimeMillis1;
    private long currentTimeMillis2;

    private int clientNumber;
    private String infoBroadcasted;

    private String winnerName = "none";
    public boolean winnerDecided = false;
    
    public CtrlGameCanvas () { }

    /* Nada mas se abre la app FX, al bola empieza a moverse aunque estemos en la pestaña login.
     * No he logrado hacer que el dibujo empieze a funcionar cuando se aprieta el boton de login me daba error
     * asi que he hecho que el dibujo empieze nada mas se inicia la app pero la pelota no
     * empieza a moverse hasta que hacemos login
     */
    public void startBallMovement(){
        currentTimeMillis1= System.currentTimeMillis();
        currentTimeMillis2= System.currentTimeMillis();
        ballSpeed = 200;
    }

    private void sendMessage(){
        JSONObject objJson = new JSONObject("{}");
        String type = "movementInfo";
        objJson.put("type", type);
        objJson.put("direction",  playerDirection);

        Main.socketClient.safeSend(objJson.toString());
        // System.out.println("Send WebSocket: " + objJson.toString());
    }

    // Iniciar el context i bucle de dibuix
    public void start (Canvas canvas) {

        cnv = canvas;

        // Define drawing context
        gc = canvas.getGraphicsContext2D();

        // Set initial positions
        ballX = cnv.getWidth() / 2;
        ballY = cnv.getHeight() / 2;
        playerY = 450 / 2;
        playerY2 = 450 / 2;

        // Init drawing bucle
        animationTimer = new UtilsFps(this::run, this::draw);
        animationTimer.start();
    }

    // Aturar el bucle de dibuix
    public void stop () {
        animationTimer.stop();
        // System.out.println("CtrlGameCanvas: stop");
    }

    /* Funcion para mostrar el num de cliente asignado por el server */
    public void setClientNumber(int clNumber){
        this.clientNumber =clNumber;
    }

    /* Funcion de prueba para ir recibiendo la informacion actualizada */
    public void updateParameters(JSONObject gameInfo){
        this.ballX = gameInfo.getDouble("ballX");
        this.ballY = gameInfo.getDouble("ballY");
        this.playerY = gameInfo.getDouble("player1_Y");
        this.playerX = gameInfo.getDouble("player1_X");
        this.playerX2 = gameInfo.getDouble("player2_X");
        this.playerY2 = gameInfo.getDouble("player2_Y");
        this.playerPoints = gameInfo.getInt("player1_Points");
        this.playerPoints2 = gameInfo.getInt("player2_Points");
        this.gameStatus = gameInfo.getString("gameStatus");
        if(clientNumber ==0){
        }else if(clientNumber ==1){
        } 
        if(gameInfo.getString("gameStatus").equals("gameOver")){
            this.winnerName = gameInfo.getString("winnerName");
            this.gameStatus = "gameOver2";
        }else{
            this.winnerDecided = false;
        }
    }

    public void showBroadcastedInfo(String info){
        this.infoBroadcasted = info;
    }

    /* Fin funciones de transferencia datos */

    // Animar
    private void run(double fps) {
        /* Trozo de testeo para visualizar datos, 
         * algunos datos se iran enviando constantemente por el socket
         */
        if(ballSpeed > 0){
            currentTimeMillis2 = System.currentTimeMillis();
        sendMessage();
        }
        /* Fin trozo testeo datos */

        if (fps < 1) return;

        final double boardWidth = cnv.getWidth();
        final double boardHeight = cnv.getHeight();

/*         // Keep player in bounds
        final double playerMinY = 5+borderSize;
        final double playerMaxY = boardHeight-playerHalf*2-5-borderSize;

        if (playerY < playerMinY) {

            playerY = playerMinY;

        } else if (playerY > playerMaxY) {

            playerY = playerMaxY;
        } */

        // Set player Y position
        playerX = cnv.getWidth() - playerWidth - 10 + 80;
        playerX2 = playerWidth2 + 130;

    }

    // Dibuixar
    private void draw() {
        // Clean drawing area
        gc.clearRect(0, 0, cnv.getWidth(), cnv.getHeight());

        // Draw board
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(borderSize);
        gc.strokeRect(0, 0, borderSize, cnv.getHeight());
        gc.strokeRect(0, 0, cnv.getWidth(), borderSize);
        gc.strokeRect(cnv.getWidth() - borderSize, 0, borderSize, cnv.getHeight());
        gc.strokeRect(0, cnv.getHeight()-borderSize, cnv.getWidth(), borderSize);

        // Draw player
        gc.setStroke(Color.PURPLE);
        gc.setLineWidth(playerWidth);
        gc.strokeRect(playerX - playerHalf, playerY, playerWidth, playerHeight);

        /* Dibujar el otro jugador */
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(playerWidth2);
        gc.strokeRect(playerX2 - playerHalf2, playerY2, playerWidth2, playerHeight2);

        // Draw ball
        gc.setFill(Color.BLACK);
        gc.fillArc(ballX - ballHalf, ballY - ballHalf, ballSize, ballSize, 0.0, 360, ArcType.ROUND);

        // Draw text with points
        gc.setFill(Color.GREEN);
        gc.setFont(new Font("Arial", 20));
        String pointsText2 = "" + playerPoints2;
        drawText(gc, pointsText2, 20, 20, "left");

        // Draw text with points
        gc.setFill(Color.PURPLE);
        gc.setFont(new Font("Arial", 20));
        String pointsText = "" + playerPoints;
        drawText(gc, pointsText, cnv.getWidth() - 20, 20, "right");

        // /* Dibujos de letras para visualizar parametros, 
        //  * FUNCION TESTEO
        //  */

        // // Texto que muestra el numero de cliente asignado por el server,
        // // la letra sera mas pequeña y translucida
        // gc.setGlobalAlpha(0.66);
        // gc.setFont(new Font("Arial", 14));
        // String numClientText = "Client Number: " + clientNumber;
        // drawText(gc, numClientText, cnv.getWidth()/2 - 200, 25, "left");

        // // Texto que nos muestra los parametros del juego en el cliente
        // String gameClientParameters = "in Client: " 
        //                            +"\nPlayerX:             "+playerX
        //                            +"\nPlayerY:             "+playerY
        //                            +"\nBallX:                 "+ballX
        //                            +"\nBallY:                 "+ballY
        //                            +"\nBallSpeed:         "+ballSpeed
        //                            +"\nPlayerSpeed:     "+playerSpeed
        //                            +"\nPlayerDirection: "+playerDirection;
        // drawText(gc, gameClientParameters, cnv.getWidth()/2 - 200, 75, "left");

        // // Texto que nos muestra los parametros del juego que envia el servidor, string enviado a pelo desde el server
        // drawText(gc, ("Broadcasted: "+infoBroadcasted), cnv.getWidth()/2 - 150, 200, "left");
        // gc.setGlobalAlpha(1); 



        // Draw game over text
        if (gameStatus.equals("gameOver2") && winnerDecided ==false) {
            /* Llamar a un metodo del main para cambiar de pantalla */
            /* UtilsViews.setViewAnimating("EndGame");
            ControllerEndGame ctrlEndGame = (ControllerEndGame) UtilsViews.getController("EndGame");
            ctrlEndGame.setWinnerName(winnerName); */
            CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
            ctrlGame.setWinnerName(winnerName);
            winnerDecided = true;
        }
    }

    public static void drawText(GraphicsContext gc, String text, double x, double y, String alignment) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        final double textWidth = tempText.getLayoutBounds().getWidth();
        final double textHeight = tempText.getLayoutBounds().getHeight();
        switch (alignment) {
            case "center":
                x = x - textWidth / 2;
                y = y + textHeight / 2;
                break;
            case "right":
                x = x - textWidth;
                y = y + textHeight / 2;
                break;
            case "left":
                y = y + textHeight / 2;
                break;
        }
        gc.fillText(text, x, y);
    }

    public static double[] findIntersection(double[][] lineA, double[][] lineB) {
        double[] result = new double[2];
    
        final double aX0 = lineA[0][0];
        final double aY0 = lineA[0][1];
        final double aX1 = lineA[1][0];
        final double aY1 = lineA[1][1];
    
        final double bX0 = lineB[0][0];
        final double bY0 = lineB[0][1];
        final double bX1 = lineB[1][0];
        final double bY1 = lineB[1][1];
    
        double x, y;
    
        if (aX1 == aX0) { // lineA is vertical
            if (bX1 == bX0) { // lineB is vertical too
                return null;
            }
            x = aX0;
            final double bM = (bY1 - bY0) / (bX1 - bX0);
            final double bB = bY0 - bM * bX0;
            y = bM * x + bB;
        } else if (bX1 == bX0) { // lineB is vertical
            x = bX0;
            final double aM = (aY1 - aY0) / (aX1 - aX0);
            final double aB = aY0 - aM * aX0;
            y = aM * x + aB;
        } else {
            final double aM = (aY1 - aY0) / (aX1 - aX0);
            final double aB = aY0 - aM * aX0;
    
            final double bM = (bY1 - bY0) / (bX1 - bX0);
            final double bB = bY0 - bM * bX0;

            final double tolerance = 1e-5;
            if (Math.abs(aM - bM) < tolerance) { 
                return null;
            }
    
            x = (bB - aB) / (aM - bM);
            y = aM * x + aB;
        }
    
        // Check if the intersection point is within the bounding boxes of both line segments
        final double boundingBoxTolerance = 1e-5;
        final boolean withinA = x >= Math.min(aX0, aX1) - boundingBoxTolerance &&
                                x <= Math.max(aX0, aX1) + boundingBoxTolerance &&
                                y >= Math.min(aY0, aY1) - boundingBoxTolerance &&
                                y <= Math.max(aY0, aY1) + boundingBoxTolerance;
        final boolean withinB = x >= Math.min(bX0, bX1) - boundingBoxTolerance &&
                                x <= Math.max(bX0, bX1) + boundingBoxTolerance &&
                                y >= Math.min(bY0, bY1) - boundingBoxTolerance &&
                                y <= Math.max(bY0, bY1) + boundingBoxTolerance;

        if (withinA && withinB) {
            result[0] = x;
            result[1] = y;
        } else {
            return null;
        }
    
        return result;
    }
}