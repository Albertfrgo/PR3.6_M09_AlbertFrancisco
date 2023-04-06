const express = require('express')
const fs = require('fs/promises')
const url = require('url')
const post = require('./post.js')
const { v4: uuidv4 } = require('uuid')

/* De momento el servidor lo estoy haciendo para que funcione dos jugadores
En el servidor se calcula el movimiento de la bola, si hay choque entre la bola y uno de los jugadores,
el movimiento de os jugadores (las palas) es informacion que ira recibiendo de los clientes, 
para calcular el servidor internamente todo el modelo */

/* Variables para tener un modelo completo del juego */
/* Tendremos una bola y Player1, el primero que se conecte, y Player2, segundo */

let borderSize =5;
let gameRunning = false;

let player1_Points = 0;
let player1_X;
let player1_Y;
const player1_Width = 5;
const player1_Height = 200;
const player1_Half = player1_Height / 2;
let player1_Speed = 250;
const player1_SpeedIncrement = 15;
let player1_Direction = "none";

let player2_Points = 0;
let player2_X;
let player2_Y;
const player2_Width = 5;
const player2_Height = 200;
const player2_Half = player2_Height / 2;
let player2_Speed = 250;
const player2_SpeedIncrement = 15;
let player2_Direction = "none";

let ballX;
let ballY;
const ballSize =15;
const ballHalf = ballSize / 2;
let ballSpeed = 0;
const ballSpeedIncrement = 25;
let ballDirection = "upRight";

const widthGame = 800;
const heightGame = 600;
const cnvHeight=450;
let fps =60;

let currentFPS =60;
let TARGET_MS = 1000 / fps;
let frameCount;
let fpsStartTime;

let clientNumber = 0;

/* Funcion que se ejecuta cada cierto tiempo en donde se calculara toda la logica del juego
movimiento de la pelota y colision pelota-jugador, la posicion del jugador la ira recibiendo de
la info que se envia por el websocket */
function gameLoop() {
  try{
    const startTime = Date.now();

    if (currentFPS >= 1) {
        // Cridar aquí la funció que actualitza el joc (segons currentFPS)

        try{
          // console.log("Game Execution, calculating ball and players positions...");
    
          if(fps < 1){
            return;
          }
          let boardWidth = widthGame;
          let boardHeight = heightGame;

          /* 
          ¡Parte que podemos dejar a cada cliente o al server!
          Movimiento de los jugadores, del websocket solo recibiria si playerDirection es 
          none, no se mueve, left o right, y en funcion de eso moveria el jugador 
          */

          // Move player 1
          switch (player1_Direction) {
            case "up":
              player1_Y = player1_Y + player1_Speed / fps; 
              break;
            case "down":
              player1_Y = player1_Y - player1_Speed / fps; 
              break;
          }

          // Move player 2
          switch (player2_Direction) {
            case "right":
              player2_Y = player2_Y + player2_Speed / fps;
              break;
            case "left":
              player2_Y = player2_Y - player2_Speed / fps;
              break;
          }

          // Keep player 1 in bounds
          const player1_MinY = 5+borderSize;
          const player1_MaxY = 561-player1_Half*2-5-borderSize;
          if (player1_Y < player1_MinY) {

            player1_Y = player1_MinY;

          } else if (player1_Y > player1_MaxY) {

            player1_Y = player1_MaxY;
          }

          // // Keep player 2 in bounds
          const player2_MinY = 5+borderSize;
          const player2_MaxY = 561-player2_Half*2-5-borderSize;

          if (player2_Y < player2_MinY) {

            player2_Y = player2_MinY;

          } else if (player2_Y > player2_MaxY) {

            player2_Y = player2_MaxY;
          }

          /* 
          Fin movimiento jugadores, el resto
          lo calculara el servidor obligatoriamente
           */
              
          // Move ball
          let ballNextX = ballX;
          let ballNextY = ballY;
          switch (ballDirection) {
            case "upRight": 
            ballNextX = ballX + ballSpeed / fps;
            ballNextY = ballY - ballSpeed / fps;
            break;
          case "upLeft": 
              ballNextX = ballX - ballSpeed / fps;
              ballNextY = ballY - ballSpeed / fps;
              break;
          case "downRight": 
              ballNextX = ballX + ballSpeed / fps;
              ballNextY = ballY + ballSpeed / fps;
              break;
          case "downLeft": 
              ballNextX = ballX - ballSpeed / fps;
              ballNextY = ballY + ballSpeed / fps;
              break;
          }
    
          // Check ball collision with board sides
          const lineBall = [ [ballX, ballY], [ballNextX, ballNextY] ];
    
          const lineBoardLeft = [ [borderSize, 0], [borderSize, boardHeight] ];
          const intersectionLeft = findIntersection(lineBall, lineBoardLeft);
    
          const boardMaxX = boardWidth - borderSize;
          const lineBoardRight = [ [boardMaxX, 0], [boardMaxX, boardHeight] ];
          const intersectionRight = findIntersection(lineBall, lineBoardRight);
    
          const lineBoardTop = [ [0, borderSize], [boardWidth, borderSize] ];
          const intersectionTop = findIntersection(lineBall, lineBoardTop);
    
          if (intersectionLeft != null){
            switch (ballDirection) {
              case "upLeft": 
                  ballDirection = "upRight";
                  break;
              case "downLeft": 
                  ballDirection = "downRight";
                  break;
            }
            ballX = intersectionLeft[0] +1;
            ballY = intersectionLeft[1];
          } else if (intersectionRight != null){
            switch (ballDirection) {
              case "upRight": 
                  ballDirection = "upLeft";
                  break;
              case "downRight": 
                  ballDirection = "downLeft";
                  break;
            }
            ballX = intersectionRight[0] -1;
            ballY = intersectionRight[1];
          } else if (intersectionTop != null){
            switch (ballDirection) {
              case "upRight": 
                  ballDirection = "downRight"; 
                  break;
              case "upLeft": 
                  ballDirection = "downLeft"; 
                  break;
            }
            ballX = intersectionTop[0];
            ballY = intersectionTop[1] + 1;
          } else {
            if (ballNextY > boardHeight){
              gameStatus = "gameOver";
            } else {
              ballX = ballNextX;
              ballY = ballNextY;
            }
          }
    
          // Check ball collision with player

          /* Colision con el jugador 1 */
          const line_Player1 = [[player1_X - player1_Half, player1_Y], [player1_X + player1_Half, player1_Y]];
          const intersection_Player1 = findIntersection(lineBall, line_Player1);
    
          if(intersection_Player1 != null){
            switch (ballDirection){
              case "downRight":
                ballDirection = "upRight";
                break;
              case "downLeft":
                ballDirection = "upLeft";
                break;
            }
            ballX = intersection_Player1[0];
            ballY = intersection_Player1[1] - 1;
            player1_Points = player1_Points + 1;
            ballSpeed = ballSpeed + ballSpeedIncrement;
            player1_Speed = player1_Speed + player1_SpeedIncrement;
          }

          player1_X = widthGame - player1_Width - 10 + 80;
    
          /* Colision con el jugador 2 */
          const line_Player2 = [[player2_X - player2_Half, player2_Y], [player2_X + player2_Half, player2_Y]];
          const intersection_Player2 = findIntersection(lineBall, line_Player2);
  
          if(intersection_Player2 != null){
            switch (ballDirection){
              case "downRight":
                ballDirection = "upRight";
                break;
              case "downLeft":
                ballDirection = "upLeft";
                break;
            }
            ballX = intersection_Player2[0];
            ballY = intersection_Player2[1] - 1;
            player2_Points = player2_Points + 1;
            ballSpeed = ballSpeed + ballSpeedIncrement;
            player2_Speed = player2_Speed + player2_SpeedIncrement;
            player2_X = player2_Width + 130;
          }
    
        }catch(err){
          console.log(err);
        }

        // Cridar aquí la funció que fa un broadcast amb les dades del joc a tots els clients
        var gameInfo = { ballX: ballX, ballY: ballY, ballSize: ballSize,
         player1_X: player1_X, player1_Y: player1_Y, player1_Height: player1_Height, player1_Points: player1_Points,
         player2_X: player2_X, player2_Y: player2_Y, player2_Height: player2_Height, player2_Points: player2_Points,};
        var rst = { type: "gameInfoBroadcast", message: gameInfo };
        broadcast(rst)
    }

    const endTime = Date.now();
    const elapsedTime = endTime - startTime;
    const remainingTime = Math.max(1, TARGET_MS - elapsedTime);

    frameCount++;
    const fpsElapsedTime = endTime - fpsStartTime;
    if (fpsElapsedTime >= 500) {
        currentFPS = (frameCount / fpsElapsedTime) * 1000;
        frameCount = 0;
        fpsStartTime = endTime;
    }
    if(gameRunning){
      setTimeout(() => { setImmediate(gameLoop); }, remainingTime);
    }
  }catch(err){
    console.log(err);
  }
}

function stopLoop(){
  gameRunning = false;
  console.log("Stopping game execution");

  // Resetting ball and players positions
  ballX =widthGame/2;
  ballY =heightGame/2;
  player1_Y = cnvHeight / 2;
  player2_Y = heightGame / 2;
  ballSpeed = 20;
}

/* Funcion que es llamada cuando se recibe un post de iniciar el juego, un jugador se ha logueado */
function startGame(){
  gameRunning = true;
  console.log("Starting game execution");
      // Set initial positions
      ballX =widthGame/2;
      ballY =heightGame/2;
      player1_Y = cnvHeight / 2;
      player2_Y = cnvHeight / 2;
      ballSpeed =20;

      gameLoop();
}

function findIntersection(lineA, lineB){
  const result = new Array(2);

  const aX0 = lineA[0][0];
  const aY0 = lineA[0][1];
  const aX1 = lineA[1][0];
  const aY1 = lineA[1][1];

  const bX0 = lineB[0][0];
  const bY0 = lineB[0][1];
  const bX1 = lineB[1][0];
  const bY1 = lineB[1][1];

  let x, y;

  if(aX1 == aX0){ //lineA is vertical
    if(bX1 == bX0){ //lineB is vertical too
      return null;
    }
    x = aX0;
    const bM = (bY1 - bY0) / (bX1 - bX0);
    const bB = bY0 - bM * bX0;
    y = bM * x + bB;
  }else if(bX1 == bX0){ //lineB is vertical
    x = bX0;
    const aM = (aY1 - aY0) / (aX1 - aX0);
    const aB = aY0 - aM * aX0;
    y = aM * x + aB;
  }else{
    const aM = (aY1 - aY0) / (aX1 - aX0);
    const aB = aY0 - aM * aX0;

    const bM = (bY1 - bY0) / (bX1 - bX0);
    const bB = bY0 - bM * bX0;

    const tolerance = 1e-5;
    if(Math.abs(aM - bM) < tolerance){
      return null;
    }

    x = (bB - aB) / (aM - bM);
    y = aM * x + aB;

  }

  // Check if the intersetion point is within the bounding boxes of both line segments
  const boundingBoxTolerance = 1e-5;
  const withinA = x >= Math.min(aX0, aX1) - boundingBoxTolerance &&
                  x <= Math.max(aX0, aX1) + boundingBoxTolerance &&
                  y >= Math.min(aY0, aY1) - boundingBoxTolerance &&
                  y <= Math.max(aY0, aY1) + boundingBoxTolerance;
  const withinB = x >= Math.min(bX0, bX1) - boundingBoxTolerance &&
                  x <= Math.max(bX0, bX1) + boundingBoxTolerance &&
                  y >= Math.min(bY0, bY1) - boundingBoxTolerance &&
                  y <= Math.max(bY0, bY1) + boundingBoxTolerance;

  if (withinA && withinB) {
    result[0] = x;
    result[1] = y;
  }else{
    return null;
  }

  return result;
}

function returnBallInfo(){
  
}

// Wait 'ms' milliseconds
function wait (ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// Start HTTP server
const app = express()

// Set port number
const port = process.env.PORT || 3000

// Publish static files from 'public' folder
app.use(express.static('public'))

// Activate HTTP server
const httpServer = app.listen(port, appListen)
function appListen () {
  console.log(`Listening for HTTP queries on: http://localhost:${port}`)
}

// Run WebSocket server
const WebSocket = require('ws')
const wss = new WebSocket.Server({ server: httpServer })
const socketsClients = new Map()
console.log(`Listening for WebSocket queries on ${port}`)

// What to do when a websocket client connects
wss.on('connection', (ws) => {

  console.log("Client connected")
  console.log('socketsClients Map:');
  for (const [key, value] of socketsClients.entries()) {
    console.log(`  ${key} => ${value}`);
  }

  // Add client to the clients list

  /* Con client number podemos contar cuantos clientes se han identificado */
  const id = uuidv4()
  const color = Math.floor(Math.random() * 360)
  const metadata = { id, color, clientNumber}
  clientNumber++;
  socketsClients.set(ws, metadata)

  // What to do when a client is disconnected
  ws.on("close", () => {
    socketsClients.delete(ws)
    clientNumber--;
  })

  // What to do when a client message is received
  ws.on('message', (bufferedMessage) => {
    console.log("Message received from client: " + bufferedMessage)

    var messageAsString = bufferedMessage.toString()
    var messageAsObject = {}
    
    try { messageAsObject = JSON.parse(messageAsString) } 
    catch (e) { console.log("Could not parse bufferedMessage from WS message") }

    /* El mensaje start game lo envia un cliente cuando se ha logeado y esta listo para empezar,
    para que empieze el juego hacen falta dos clientes listos */
    if(messageAsObject.type == "startGame"){
      // startGame();
      var rst = { type: "answer", message: "client "+metadata.clientNumber + " is ready to start" }
      if(clientNumber == 2){
        console.log("Start game *****");
        startGame();
      }
      var rst = { type: "answer", message: "Game started" }
    }

    /* El mensaje stopGame seria el mensaje que aparece por ejemplo cuando un cliente cierra
    la ventana, se va del juego, y el bucle deberia detenerse, aparte de detener el bucle,
    podemos manejar la logica para atorgar la victoria */
    if(messageAsObject.type == "stopGame"){
      if(gameRunning == true && (metadata.clientNumber == 1 || metadata.clientNumber == 2)){
        stopLoop();
      }
      var rst = { type: "answer", message: "Game stopped" }
    }

    /* Una vez haya empezado el juego, los clientes iran enviando info de su movimiento,
    podemos hacer que solo indiquen hacia que direccion mueven las palas o que tambien manejen ellos el movimiento
    indicando la pos X e Y */
    if(messageAsObject.type == "movementInfo"){
      if((gameRunning == true)){
        console.log(metadata.clientNumber)
        if(metadata.clientNumber == 0){
          // sacamos del webSocket la informacion, actualizamos variables de player1
          if(messageAsObject.direction == "up"){
            player1_Direction = "up";
          }else if(messageAsObject.direction == "down"){
            player1_Direction = "down";
          }else if(messageAsObject.direction == "none"){
            player1_Direction = "none";
          }
        }else if(metadata.clientNumber == 1){
          // sacamos del webSocket la informacion, actualizamos variables de player2
          if(messageAsObject.direction == "up"){
            player2_Direction = "up";
          }else if(messageAsObject.direction == "down"){
            player2_Direction = "down";
          }else if(messageAsObject.direction == "none"){
            player2_Direction = "none";
          }
          }
        }
        rst = { type: "answer", message: "Movement info received", clientNumber: metadata.clientNumber }
      }

    // console.log("Will respond " +JSON.stringify(rst));
    ws.send(JSON.stringify(rst));
  })
})

// Send a message to all websocket clients
async function broadcast (obj) {
  // console.log("Broadcasting message to all clients: " + JSON.stringify(obj))
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      var messageAsString = JSON.stringify(obj)
      client.send(messageAsString)
    }
  })
}


