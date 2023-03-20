const express = require('express')
const fs = require('fs/promises')
const url = require('url')
const post = require('./post.js')
const { v4: uuidv4 } = require('uuid')
const mysql = require('mysql2')

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

// Set URL rout for POST queries
app.post('/dades', getDades)
async function getDades (req, res) {
  try{

 
  let receivedPOST = await post.getPostObject(req)
  let result = {};

  var textFile = await fs.readFile("./public/consoles/consoles-list.json", { encoding: 'utf8'})
  var objConsolesList = JSON.parse(textFile)

  if (receivedPOST) {
    console.log("Peticio rebuda: " + receivedPOST.type + " " + receivedPOST.name )
    if (receivedPOST.type == "consola") {
      // console.log("\npeticio d'informacio d'una consola'");
      var objFilteredList = objConsolesList.filter((obj) => { return obj.name == receivedPOST.name })
      await wait(1500)
      if (objFilteredList.length > 0) {
        result = { status: "OK", result: objFilteredList[0] }
      }
    }
    if (receivedPOST.type == "consoles") {
      // console.log("codi marca")
      var objBrandConsolesList = objConsolesList
      await wait(1500)
      // Ordena les consoles per nom de model
      objBrandConsolesList.sort((a,b) => { 
          var textA = a.name.toUpperCase();
          var textB = b.name.toUpperCase();
          return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
      })
      result = { status: "OK", result: objBrandConsolesList } 
    }
    if (receivedPOST.type == "marques") {
      // console.log("\npeticio de marques");
      var objBrandsList = objConsolesList.map((obj) => { return obj.brand })
      await wait(1500)
      let senseDuplicats = [...new Set(objBrandsList)]
      result = { status: "OK", result: senseDuplicats.sort() } 
    }
    if (receivedPOST.type == "colors") {
        // console.log("\npeticio de colors");
        var objLlistaColors = objConsolesList.map(function (obj){ return obj.color})
        await wait(1500)
        let senseDuplicats = [...new Set(objLlistaColors)]
        result = { status: "OK",result : senseDuplicats}
    }
    if (receivedPOST.type == "processadors") {
      // console.log("\npeticio de processadors");
      var objLlistaProcessadors = objConsolesList.map(function (obj){ return obj.processor})
      await wait(1500)
      let senseDuplicats = [...new Set(objLlistaProcessadors)]
      result = {status: "OK", result : senseDuplicats}
    }
    if (receivedPOST.type == "consolesMarca") {
      // console.log("\npeticio de les consoles d'una marca determinada");
      var objBrandConsolesList = objConsolesList.filter ((obj) => { return obj.brand == receivedPOST.name })
      await wait(1500)
      // Ordena les consoles per nom de model
      objBrandConsolesList.sort((a,b) => { 
          var textA = a.name.toUpperCase();
          var textB = b.name.toUpperCase();
          return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
      })
      result = { status: "OK", result: objBrandConsolesList } 
    }
    if (receivedPOST.type == "consolesColor") {
      // console.log("\npeticio de les consoles d'un color determinat");
      var objBrandConsolesList = objConsolesList.filter ((obj) => { return obj.color == receivedPOST.name })
      await wait(1500)
      // Ordena les consoles per nom de model
      objBrandConsolesList.sort((a,b) => { 
          var textA = a.name.toUpperCase();
          var textB = b.name.toUpperCase();
          return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
      })
      result = { status: "OK", result: objBrandConsolesList } 
    }
    //Cerca modificada perque enlloc de buscar que coincideixi, buscar que contingui la paraula de cerca
    if (receivedPOST.type == "consolesProcessador") {
      // console.log("\npeticio de les consoles d'un processador determinat");
      var objBrandConsolesList = objConsolesList.filter ((obj) => { return obj.processor == receivedPOST.name })
      await wait(1500)
      // Ordena les consoles per nom de model
      objBrandConsolesList.sort((a,b) => { 
          var textA = a.name.toUpperCase();
          var textB = b.name.toUpperCase();
          return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
      })
      result = { status: "OK", result: objBrandConsolesList } 
    }
    if (receivedPOST.type == "login") {
      // console.log("codi login")
      let port =receivedPOST.port
      let server =receivedPOST.server
      if (port == 3000 && server == "localhost") {
        result = { status: "OK", result: "ACCEPTED" }
      }else{
        result = { status: "OK", result: "DENIED" }
      }
    }
  }

  // console.log(JSON.stringify(result))
  res.writeHead(200, { 'Content-Type': 'application/json' })
  res.end(JSON.stringify(result))

  }catch(e){
    console.log("ERROR: " + e.stack)
  }
}

// Run WebSocket server
const WebSocket = require('ws')
const wss = new WebSocket.Server({ server: httpServer })
const socketsClients = new Map()
console.log(`Listening for WebSocket queries on ${port}`)

// What to do when a websocket client connects
wss.on('connection', (ws) => {

  console.log("Client connected")

  // Add client to the clients list
  const id = uuidv4()
  const color = Math.floor(Math.random() * 360)
  const metadata = { id, color }
  socketsClients.set(ws, metadata)

  // Send clients list to everyone
  sendClients()

  // What to do when a client is disconnected
  ws.on("close", () => {
    socketsClients.delete(ws)
  })

  // What to do when a client message is received
  ws.on('message', (bufferedMessage) => {
    var messageAsString = bufferedMessage.toString()
    var messageAsObject = {}
    
    try { messageAsObject = JSON.parse(messageAsString) } 
    catch (e) { console.log("Could not parse bufferedMessage from WS message") }

    if (messageAsObject.type == "bounce") {
      var rst = { type: "bounce", message: messageAsObject.message }
      ws.send(JSON.stringify(rst))
    } else if (messageAsObject.type == "broadcast") {
      var rst = { type: "broadcast", origin: id, message: messageAsObject.message }
      broadcast(rst)
    } else if (messageAsObject.type == "private") {
      var rst = { type: "private", origin: id, destination: messageAsObject.destination, message: messageAsObject.message }
      private(rst)
    }
  })
})

// Send clientsIds to everyone
function sendClients () {
  var clients = []
  socketsClients.forEach((value, key) => {
    clients.push(value.id)
  })
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      var id = socketsClients.get(client).id
      var messageAsString = JSON.stringify({ type: "clients", id: id, list: clients })
      client.send(messageAsString)
    }
  })
}

// Send a message to all websocket clients
async function broadcast (obj) {
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      var messageAsString = JSON.stringify(obj)
      client.send(messageAsString)
    }
  })
}

// Send a private message to a specific client
async function private (obj) {
  wss.clients.forEach((client) => {
    if (socketsClients.get(client).id == obj.destination && client.readyState === WebSocket.OPEN) {
      var messageAsString = JSON.stringify(obj)
      client.send(messageAsString)
      return
    }
  })
}

// Perform a query to the database
function queryDatabase (query) {

  return new Promise((resolve, reject) => {
    var connection = mysql.createConnection({
      host: process.env.MYSQLHOST || "localhost",
      port: process.env.MYSQLPORT || 3306,
      user: process.env.MYSQLUSER || "root",
      password: process.env.MYSQLPASSWORD || "",
      database: process.env.MYSQLDATABASE || "test"
    });

    connection.query(query, (error, results) => { 
      if (error) reject(error);
      resolve(results)
    });
     
    connection.end();
  })
}