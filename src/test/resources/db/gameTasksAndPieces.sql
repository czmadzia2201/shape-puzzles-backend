-- Game types
INSERT INTO game_type (name, display_name)
VALUES
    ('tangram', 'Tangram'),
    ('house', 'House'),
    ('t', 'T');


-- Pieces: tangram
INSERT INTO piece (id, game_type_name, start_point, vertices)
VALUES
    ('tangram_piece_1', 'tangram', '{"x": 0, "y": 0}', '[]'),
    ('tangram_piece_2', 'tangram', '{"x": 2, "y": 0}', '[]'),
    ('tangram_piece_3', 'tangram', '{"x": 4, "y": 0}', '[]');


-- Pieces: house
INSERT INTO piece (id, game_type_name, start_point, vertices)
VALUES
    ('house_piece_1', 'house', '{"x": 0, "y": 0}', '[]'),
    ('house_piece_2', 'house', '{"x": 2, "y": 0}', '[]');


-- Pieces: T
INSERT INTO piece (id, game_type_name, start_point, vertices)
VALUES
    ('t_piece_1', 't', '{"x": 0, "y": 0}', '[]');


-- Tasks: tangram
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('tangram_001', 'tangram', '[]'),
    ('tangram_002', 'tangram', '[]'),
    ('tangram_003', 'tangram', '[]');


-- Tasks: house
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('house_001', 'house', '[]'),
    ('house_002', 'house', '[]');


-- Tasks: T
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('t_001', 't', '[]');