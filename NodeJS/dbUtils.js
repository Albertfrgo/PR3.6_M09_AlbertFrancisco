const fs = require('fs/promises')
const url = require('url')
const mysql = require('mysql2')
const post = require('./post.js')
const { v4: uuidv4 } = require('uuid')
const { response } = require('express')
const { stat } = require('fs')

/* Funcion para pedir informacion de estadisticas de un usuario
recibira el nombre del post y nos mandara varias claves con texto describiendo estadisticas */
async function getStatisticsPlayer(req, res) {
    let receivedPost = await post.getPost(req, res);
    console.log(receivedPost);
  
    try {
      let nickname = receivedPost.nickname;
  
      let winsQuery = `SELECT COUNT(*) AS wins FROM Matches WHERE Player1_Id = (
        SELECT Id FROM Players WHERE Nickname = '${nickname}'
      )`;
  
      let lossesQuery = `SELECT COUNT(*) AS losses FROM Matches WHERE Player0_Id = (
        SELECT Id FROM Players WHERE Nickname = '${nickname}'
      )`;
  
      let longestMatchQuery = `SELECT CONCAT(Duration DIV 60, ' minutes and ', Duration MOD 60, ' seconds') AS duration FROM Matches WHERE (Player0_Id = (
        SELECT Id FROM Players WHERE Nickname = '${nickname}'
      ) OR Player1_Id = (
        SELECT Id FROM Players WHERE Nickname = '${nickname}'
      )) ORDER BY Duration DESC LIMIT 1`;
  
      let mostTouchesQuery = `SELECT CONCAT(
        (SELECT Nickname FROM Players WHERE Id = M.Player0_Id),
        ' ',
        M.Player0_Touches,
        ' vs ',
        (SELECT Nickname FROM Players WHERE Id = M.Player1_Id),
        ' ',
        M.Player1_Touches,
        ' - ',
        DATE_FORMAT(Time_Stamp, '%d/%m/%Y')
      ) AS match
      FROM Matches M
      WHERE (Player0_Id = (
        SELECT Id FROM Players WHERE Nickname = '${nickname}'
      ) OR Player1_Id = (
        SELECT Id FROM Players WHERE Nickname = '${nickname}'
      ))
      ORDER BY GREATEST(Player0_Touches, Player1_Touches) DESC LIMIT 1`;
  
      let results = await Promise.all([
        queryDatabase(winsQuery),
        queryDatabase(lossesQuery),
        queryDatabase(longestMatchQuery),
        queryDatabase(mostTouchesQuery)
      ]);
  
      let stats = {
        "Statistics NickPlayer": nickname,
        "Number of matches won": results[0][0].wins,
        "Number of matches lost": results[1][0].losses,
        "Longest Match": results[2][0].duration,
        "Match with more touches": results[3][0].match
      };
  
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ "status": "OK", "message": stats }));
    } catch (error) {
      console.log(error);
      res.end(JSON.stringify({ "status": "ERROR", "message": "Failed to get the statistics of player" }));
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

  /* Funcion de testeo llamada al iniciar el servidor para comprobar en consola 
  que la conexion a BBDD funciona */
  async function printUsersTest(){
    let query = 'SELECT Id, Nickname FROM Players;'
    let result = JSON.stringify(await queryDatabase(query));
    console.log("The users are: "+ result)
  }

  /* Funcion para guardar una partida, timeStamp es timeStartMatch, 
  se guardara como variable timeStamp y se usara para calcular duracion de la partida */
  async function saveMatch(timeStamp, timeEndMatch, player1Id, player2Id, player1Touches, player2Touches) {
    try {
      const duration = Math.floor((timeEndMatch - timeStamp) / 1000); // in seconds
      const query = `
        INSERT INTO Matches (Time_Stamp, Duration, Player0_Id, Player1_Id, Player0_Touches, Player1_Touches)
        VALUES ('${new Date(timeStamp).toISOString().slice(0, 19).replace('T', ' ')}', ${duration}, ${player1Id}, ${player2Id}, ${player1Touches}, ${player2Touches})
      `;
      await queryDatabase(query);
    } catch (error) {
      console.log(error);
      throw new Error('Failed to save the match');
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
    printUsersTest
  };