CREATE DATABASE IF NOT EXISTS pr31_m6;
USE pr31_m6;

DROP TABLE IF EXISTS Matches;
DROP TABLE IF EXISTS Players;

CREATE TABLE IF NOT EXISTS Players (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Nickname VARCHAR(255) NOT NULL,
    CodePlayer VARCHAR(255) NOT NULL,
    Color ENUM('pink', 'purple', 'red', 'orange', 'yellow', 'lime', 'green', 'cyan', 'blue', 'navy', 'black')
);

CREATE TABLE IF NOT EXISTS Matches (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Time_Stamp TIMESTAMP NOT NULL,
    Duration TIME NOT NULL,
    Player0_Id INT NOT NULL,
    Player1_Id INT NOT NULL,
    Player0_Touches INT NOT NULL,
    Player1_Touches INT NOT NULL,
    FOREIGN KEY (Player0_Id) REFERENCES Players(Id),
    FOREIGN KEY (Player1_Id) REFERENCES Players(Id)
);

