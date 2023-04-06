import java.time.LocalTime;

import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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

    private double ballX = Double.POSITIVE_INFINITY;
    private double ballY = Double.POSITIVE_INFINITY;
    private final double ballSize = 15;
    private final double ballHalf = ballSize / 2;
    private double ballSpeed = 0;
    private final double ballSpeedIncrement = 25;
    private String ballDirection = "upRight";

    private long currentTimeMillis1;
    private long currentTimeMillis2;
    
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
        System.out.println("Send WebSocket: " + objJson.toString());
    }

    private void requestInfoBall(){
        JSONObject objJson = new JSONObject("{}");
        String message = "requestingBallInfo";
        String type = "requestingBallInfo";
        objJson.put("type", type);
        objJson.put("message", message);
    }

    // Iniciar el context i bucle de dibuix
    public void start (Canvas canvas) {

        cnv = canvas;

        // Define drawing context
        gc = canvas.getGraphicsContext2D();

        // Set initial positions
        ballX = cnv.getWidth() / 2;
        ballY = cnv.getHeight() / 2;
        playerY = cnv.getHeight() / 2;

        // Init drawing bucle
        animationTimer = new UtilsFps(this::run, this::draw);
        animationTimer.start();
    }

    // Aturar el bucle de dibuix
    public void stop () {
        animationTimer.stop();
    }

    // Animar
    private void run(double fps) {
        /* Trozo de testeo para visualizar datos, 
         * algunos datos se iran enviando constantemente por el socket
         */
        if(ballSpeed > 0){
            currentTimeMillis2 = System.currentTimeMillis();
            /* De momento tiene dos variable que cuentan tiempo en ms para que cada 2000ms aprox, 
             * printe un mensaje */
            if ((currentTimeMillis2 -currentTimeMillis1) >1000) {
                currentTimeMillis1 = currentTimeMillis2;
                System.out.println("Running " + LocalTime.now().toString());
                sendMessage();
            }
        }
        /* Fin trozo testeo datos */

        if (fps < 1) return;

        final double boardWidth = cnv.getWidth();
        final double boardHeight = cnv.getHeight();
        // final double boardWidth = 400;
        // final double boardHeight = 300;

        // // Move player
        // switch (playerDirection) {
        //     case "up":
        //         playerY = playerY + playerSpeed / fps; 
        //         break;
        //     case "down":
        //         playerY = playerY - playerSpeed / fps;
        //         break;
        // }

        // Keep player in bounds
        final double playerMinY = 5+borderSize;
        final double playerMaxY = boardHeight-playerHalf*2-5-borderSize;

        if (playerY < playerMinY) {

            playerY = playerMinY;

        } else if (playerY > playerMaxY) {

            playerY = playerMaxY;
        }
        // Set player Y position
        playerX = cnv.getWidth() - playerWidth - 10 + 80;
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

        // Draw player
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(playerWidth);
        gc.strokeRect(playerX - playerHalf, playerY, playerWidth, playerHeight);
        // gc.setLineWidth(playerHeight);
        // gc.strokeRect(400 - playerHalf, 400, playerWidth, playerHeight);

        // Draw ball
        gc.setFill(Color.BLACK);
        gc.fillArc(ballX - ballHalf, ballY - ballHalf, ballSize, ballSize, 0.0, 360, ArcType.ROUND);

        // Draw text with points
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 20));
        String pointsText = "Points: " + playerPoints;
        drawText(gc, pointsText, cnv.getWidth()/2 - 20, 20, "right");

        // Draw game over text
        if (gameStatus.equals("gameOver")) {
            final double boardCenterX = cnv.getWidth() / 2;
            final double boardCenterY = cnv.getHeight() / 2;

            gc.setFont(new Font("Arial", 40));
            drawText(gc, "GAME OVER", boardCenterX, boardCenterY - 20, "center");

            gc.setFont(new Font("Arial", 20));
            drawText(gc, "You are a loser!", boardCenterX, boardCenterY + 20, "center");
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