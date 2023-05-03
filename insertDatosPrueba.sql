USE pr31_m6;

-- Insert 3 players
INSERT INTO Players (Nickname, CodePlayer, Color) VALUES 
    ('Player1', '123456', 'red'),
    ('Player2', 'abcde', 'green'),
    ('Player3', 'qwerty', 'purple');

-- Insert 6 matches
INSERT INTO Matches (Time_Stamp, Duration, Player0_Id, Player1_Id, Player0_Touches, Player1_Touches) VALUES
    ('2023-05-01 15:30:00', '00:05:00', 1, 2, 10, 9), -- Player1 won against Player2
    ('2023-05-01 16:00:00', '00:10:00', 2, 3, 23, 21), -- Player2 won against Player3
    ('2023-05-01 17:00:00', '00:08:00', 3, 1, 5, 3),  -- Player3 won against Player1
    ('2023-05-02 09:30:00', '00:03:00', 1, 3, 5, 4),  -- Player1 won against Player3
    ('2023-05-02 10:00:00', '00:06:00', 3, 2, 11, 10), -- Player3 won against Player2
    ('2023-05-02 11:00:00', '00:04:00', 2, 1, 8, 6);  -- Player2 won against Player1
