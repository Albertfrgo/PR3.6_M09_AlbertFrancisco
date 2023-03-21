const express = require('express')
const fs = require('fs/promises')
const url = require('url')
const post = require('./post.js')
const { v4: uuidv4 } = require('uuid')

/* Variables para tener un modelo completo del juego */

let borderSize =5;
let gameRunning = false;

let playerPoints = 0;
let playerX;
let playerY;
const playerWidth = 200;
const playerHalf = playerWidth / 2;
const playerHeight = 5;
let playerSpeed = 250;
const playerSpeedIncrement = 15;
let playerDirection = "none";

let ballX;
let ballY;
const ballSize =15;
const ballHalf = ballSize / 2;
let ballSpeed = 0;
const ballSpeedIncrement = 25;
let ballDirection = "upRight";

const widthGame = 800;
const heightGame = 600;
let fps =60;

/* loopInterval establece cada cuanto el servidor hara calculos de la posicion de la pelota */
const loopInterval = 10;
let gameLoop;;

/* Funcion que se ejecuta cada cierto tiempo en donde se calculara toda la logica del juego
movimiento de la pelota y colision pelota-jugador, la posicion del jugador la ira recibiendo de
la info que envia el websocket */

function startGame(){
  gameRunning = true;
  console.log("Starting game execution");
      // Set initial positions
      ballX =widthGame/2;
      ballY =heightGame/2;
      playerX =widthGame/2;

      ballSpeed =200;
  
  gameLoop = setInterval(() => {
    try{
      // console.log("Game Execution, calculating ball movement...");

      if(fps < 1){
        return;
      }
      let boardWidth = widthGame;
      let boardHeight = heightGame;

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
      const linePlayer = [[playerX - playerHalf, playerY], [playerX + playerHalf, playerY]];
      const intersectionPlayer = findIntersection(lineBall, linePlayer);

      if(intersectionPlayer != null){
        switch (ballDirection){
          case "downRight":
            ballDirection = "upRight";
            break;
          case "downLeft":
            ballDirection = "upLeft";
            break;
        }
        ballX = intersectionPlayer[0];
        ballY = intersectionPlayer[1] - 1;
        playerPoints = playerPoints + 1;
        ballSpeed = ballSpeed + ballSpeedIncrement;
        playerSpeed = playerSpeed + playerSpeedIncrement;
      }

      playerY = heightGame - playerHeight - borderSize *2;
    }catch(err){
      console.log(err);
    }


  }, loopInterval);
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

function stopLoop(){
  gameRunning = false;
  console.log("Stopping game execution");
  clearInterval(gameLoop);
}

// What to do when a websocket client connects
wss.on('connection', (ws) => {

  console.log("Client connected")

  // Add client to the clients list
  const id = uuidv4()
  const color = Math.floor(Math.random() * 360)
  const metadata = { id, color }
  socketsClients.set(ws, metadata)

  // What to do when a client is disconnected
  ws.on("close", () => {
    socketsClients.delete(ws)
  })

  // What to do when a client message is received
  ws.on('message', (bufferedMessage) => {
    console.log("Message received from client: " + bufferedMessage)
    let numberRandomAnswer = Math.floor((Math.random() * 10) + 1);

    var messageAsString = bufferedMessage.toString()
    var messageAsObject = {}
    
    try { messageAsObject = JSON.parse(messageAsString) } 
    catch (e) { console.log("Could not parse bufferedMessage from WS message") }

    /* Empieza el juego y llamara a la funcion correspondiente */
    if(messageAsObject.type == "startGame"){
      startGame();
    }
    if(messageAsObject.type == "stopGame"){
      stopLoop();
    }

    var rst = { type: "answer", message: numberRandomAnswer }
    console.log("Will respond " +JSON.stringify(rst));
    ws.send(JSON.stringify(rst));
  })
})


