USE railway;

-- Insert 3 players
INSERT INTO Players (Nickname, CodePlayer, Color) VALUES 
('player1', 'abc123', 'pink'),
('player2', 'def456', 'purple'),
('player3', 'ghi789', 'red');

-- Insert 6 matches
INSERT INTO Matches (Time_Stamp, Duration, Player1Id, Player2Id, Player1Points, Player2Points, Player1Touches, Player2Touches) VALUES 
('2023-05-05 14:30:00', '00:02:00', 1, 2, 5, 2, 12, 13),
('2023-05-06 15:00:00', '00:01:30', 1, 3, 5, 3, 15, 17),
('2023-05-07 16:00:00', '00:03:00', 2, 3, 4, 5, 11, 14),
('2023-05-08 17:30:00', '00:01:45', 1, 2, 5, 3, 14, 12),
('2023-05-09 18:00:00', '00:02:30', 2, 3, 5, 4, 12, 11),
('2023-05-10 19:00:00', '00:01:15', 1, 3, 4, 5, 16, 13);
