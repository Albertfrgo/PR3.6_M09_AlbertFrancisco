const fs = require('fs/promises')
const url = require('url')
const mysql = require('mysql2')
const post = require('./post.js')
const { v4: uuidv4 } = require('uuid')
const { response } = require('express')
const { stat } = require('fs')

/* Funcion para pedir informacion de estadisticas de un usuario
recibira el nombre del post y nos mandara varias claves con los valores,
sobre las partidas, habra un json con toda la info de dicha partida */
async function getStatisticsPlayer(req, res) {
  try {
    const playerName = req.body.playerName;

    const matchesWonQuery = `SELECT COUNT(*) AS MatchesWon FROM Matches WHERE Player1Points=5 AND Player1Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}') OR Player2Points=5 AND Player2Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}');`;
    const matchesLostQuery = `SELECT COUNT(*) AS MatchesLost FROM Matches WHERE (Player1Points=5 AND Player2Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}')) OR (Player2Points=5 AND Player1Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}'));`;
    const longestMatchQuery = `SELECT Id, Duration FROM Matches WHERE Player1Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}') OR Player2Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}') ORDER BY Duration DESC LIMIT 1;`;
    const matchWithMoreTouchesQuery = `SELECT Id FROM Matches WHERE Player1Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}') OR Player2Id IN (SELECT Id FROM Players WHERE Nickname='${playerName}') ORDER BY Player1Touches+Player2Touches DESC LIMIT 1;`;

    const matchesWonResult = await queryDatabase(matchesWonQuery);
    const matchesLostResult = await queryDatabase(matchesLostQuery);
    const longestMatchResult = await queryDatabase(longestMatchQuery);
    const matchWithMoreTouchesResult = await queryDatabase(matchWithMoreTouchesQuery);

    const matchesWon = matchesWonResult[0].MatchesWon;
    const matchesLost = matchesLostResult[0].MatchesLost;
    const longestMatch = await getMatchInfo(longestMatchResult[0].Id);
    const matchWithMoreTouches = await getMatchInfo(matchWithMoreTouchesResult[0].Id);

    const response = {
      MatchesWon: matchesWon,
      MatchesLost: matchesLost,
      LongestMatch: longestMatch,
      MatchWithMoreTouches: matchWithMoreTouches,
    };

    res.status(200).json(response);
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Failed to get player statistics" });
  }
}

  /* Funcion que nos devuelve una lista de usuarios, es para mostrarlos al
  principio cuando le damos a estadisticas */
async function getUsersList(req, res){
  console.log("getUsersList");
    let receivedPOST = await post.getPostObject(req)
    try {
      let query = 'SELECT Id, Nickname FROM Players;'
      let result = await queryDatabase(query)
  
      let usersList = {}
      for (let row of result) {
        usersList[row.Id] = row.Nickname
      }
  
      res.writeHead(200, { 'Content-Type': 'application/json' });
      console.log("getUsersList will return: "+JSON.stringify(usersList))
      res.end(JSON.stringify({"status":"OK","message":usersList}));
    } catch (error) {
      console.log(error)
      res.end(JSON.stringify({"status":"ERROR","message":"Failed to get user list"}));
    }
  }


  /* Funcion de TESTEO llamada al iniciar el servidor para comprobar en consola 
  que la conexion a BBDD funciona */
  async function printUsersTest(){
    let query = 'SELECT Id, Nickname FROM Players;'
    let result = JSON.stringify(await queryDatabase(query));
    console.log("The users are: "+ result)
  }

  /* Funcion para guardar una partida, timeStamp es timeStartMatch, 
  se guardara como variable timeStamp y se usara para calcular duracion de la partida 
  las partidas las guarda el server, asi que este metodo no tiene logica de posts*/
  async function saveMatch(timeStamp, timeEndMatch, player1Id, player2Id, player1Points, player2Points, player1Touches, player2Touches) {
    console.log("saveMatch");
    try {
      const duration = Math.floor((timeEndMatch - timeStamp) / 1000); // in seconds
      const query = `INSERT INTO Matches (Time_Stamp, Duration, Player1Id, Player2Id, Player1Points, Player2Points, Player1Touches, Player2Touches)
        VALUES ('${new Date(timeStamp).toISOString().slice(0, 19).replace('T', ' ')}', ${duration}, ${player1Id}, ${player2Id}, ${player1Points}, ${player2Points}, ${player1Touches}, ${player2Touches})`;
      await queryDatabase(query);
    } catch (error) {
      console.log(error);
      throw new Error('Failed to save the match');
    }
  }
  

  /* Funcion para registrar un usuario, una vez registrado,
  el post le pasar los datos. Una vez registrado solo se consultara informacion */
  async function savePlayer(req, res){
    console.log("savePlayer");
    let receivedPOST = await post.getPostObject(req);
    let nickname = receivedPOST.nickname;
    let codePlayer = receivedPOST.codePlayer;
    let color = receivedPOST.color;
    try {
      const query = `INSERT INTO Players (Nickname, CodePlayer, Color)
                     '${nickname}', '${codePlayer}', '${color}')`;
      await queryDatabase(query);
      res.end(JSON.stringify({"status":"OK","message":"Player saved correctly"}));
    } catch (error) {
      console.log(error);
      res.end(JSON.stringify({"status":"ERROR","message":"Failed to save the player"}));
    }
  }

  /* Funcion para que un jugador se logee, el login es solo decirle al cliente si ese nombre y codigo que ha pasado son correctos
  y darle el OK, con el OK el cliente se logueara y podra pasar a la siguiente pantalla y empezar partida si quiere
  
  Podemos implementar que la app de escritorio guarde la id del jugador una vez logueado y al empezar la partida,
  se establezca como id del jugador 1 o 2, segun orden de la partida. La idea es que una cosa es que el jugador haga login
  y otra es que juegue, pero no por hacer login pasaria automaticamente a ser el jugador 1 o 2 de la partida*/
  async function logPlayer(req, res) {
    console.log("logPlayer");
    try {
      const receivedPOST = await post.getPostObject(req);
      const { nickname, code } = receivedPOST;
      console.log("nickname: "+nickname+" code:"+code);
  
      const queryLog = `SELECT * FROM Players WHERE Nickname = '${nickname}' AND CodePlayer = '${code}'`;
      const player = await queryDatabase(queryLog);
      
      if (player) {
        res.end(JSON.stringify({ "status": "OK", "message": "Player logged correctly", "Logged": true }));
      } else {
        res.end(JSON.stringify({ "status": "ERROR", "message": "Invalid nickname or code", "Logged": false }));
      }
    } catch (error) {
      console.log(error);
      res.end(JSON.stringify({ "status": "ERROR", "message": "Failed to log player", "Logged": false }));
    }
  }

  /* Como en el juego se identificaran los jugadores por el nombre, que tb es unico
  esta funcion nos devuelve el id si le pasamos el nombre del jugador */
  async function getId(nickname) {
    console.log("getId: "+nickname);
    try {
      let query = `SELECT Id FROM Players WHERE Nickname = '${nickname}'`;
      const result = await queryDatabase(query);
      if (result.length === 0) {
        throw new Error(`Player with nickname '${nickname}' not found`);
      }
      return result[0].Id;
    } catch (error) {
      console.log(error);
      throw new Error('Failed to get player id');
    }
  }

  /* Funcion para obtener en un json la info de una partida, la llama
  la funcion getStatistics players para tener disponible toda la informacion de las partidas (aunque no se usara toda), 
  por ejemplo partida mas larga, o partida con mas toques */
  async function getMatchInfo(idMatch) {
    try {
      const query = `SELECT * FROM Matches WHERE Id=${idMatch}`;
      const result = await queryDatabase(query);
      const matchInfo = {
        "Id": result[0].Id,
        "Time_Stamp": result[0].Time_Stamp,
        "Duration": result[0].Duration,
        "Player1Id": result[0].Player1Id,
        "Player2Id": result[0].Player2Id,
        "Player1Points": result[0].Player1Points,
        "Player2Points": result[0].Player2Points,
        "Player1Touches": result[0].Player1Touches,
        "Player2Touches": result[0].Player2Touches
      };
      return matchInfo;
    } catch (error) {
      console.error(error);
      return null;
    }
  }
  

// Perform a query to the database
function queryDatabase (query) {

    return new Promise((resolve, reject) => {
      var connection = mysql.createConnection({
        host: process.env.MYSQLHOST || "localhost",
        port: process.env.MYSQLPORT || 3306,
        user: process.env.MYSQLUSER || "root",
        password: process.env.MYSQLPASSWORD || "localhost",
        database: process.env.MYSQLDATABASE || "pr31_m6"
      });
  
      connection.query(query, (error, results) => { 
        if (error) reject(error);
        resolve(results)
      });
       
      connection.end();
    })
  }

  module.exports = {
    queryDatabase,
    getStatisticsPlayer,
    getUsersList,
    saveMatch, 
    savePlayer,
    logPlayer,
    getId,
    printUsersTest
  };