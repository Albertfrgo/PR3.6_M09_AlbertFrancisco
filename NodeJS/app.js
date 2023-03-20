const express = require('express')
const fs = require('fs/promises')
const url = require('url')
const post = require('./post.js')
const { v4: uuidv4 } = require('uuid')

/* Variables para tener un modelo completo del juego */

/* estado del juego y si se han conectado los jugadores */
let gameRunning = false;
let player1 = false;
let player2 = false;

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

const loopInterval = 500;
let gameLoop;;

function startGame(){
  gameRunning = true;
  console.log("Starting game execution");
  gameLoop = setInterval(() => {
    console.log("Game Execution...");
  }, loopInterval);
}

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


