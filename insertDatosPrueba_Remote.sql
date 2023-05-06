USE railway;

-- Insert 3 players
INSERT INTO Players (Nickname, CodePlayer, Color) VALUES 
('player1', 'abc123', 'pink'),
('player2', 'def456', 'purple'),
('player3', 'ghi789', 'red');

-- Insert 6 matches
INSERT INTO Matches (Time_Stamp, Duration, Player0_Id, Player1_Id, Player0_Points, Player1_Points) VALUES 
('2023-05-05 14:30:00', '00:02:00', 1, 2, 5, 2),
('2023-05-06 15:00:00', '00:01:30', 1, 3, 5, 3),
('2023-05-07 16:00:00', '00:03:00', 2, 3, 4, 5),
('2023-05-08 17:30:00', '00:01:45', 1, 2, 5, 3),
('2023-05-09 18:00:00', '00:02:30', 2, 3, 5, 4),
('2023-05-10 19:00:00', '00:01:15', 1, 3, 4, 5);
