-- Game types
INSERT INTO game_type (name, display_name, base_shape, unit_size)
VALUES
    ('tangram', 'Tangram', '[]', 50.0),
    ('house', 'House', '[]', 50.0),
    ('t', 'T', '[]', 50.0);


-- Pieces: tangram
INSERT INTO piece (id, game_type_name, start_point, vertices)
VALUES
    ('tangram_piece_1', 'tangram', '{"x": {"constant": 0}, "y": {"constant": 0}}', '[]'),
    ('tangram_piece_2', 'tangram', '{"x": {"constant": 0}, "y": {"constant": 0}}', '[]'),
    ('tangram_piece_3', 'tangram', '{"x": {"constant": 0}, "y": {"constant": 0}}', '[]');


-- Pieces: house
INSERT INTO piece (id, game_type_name, start_point, vertices)
VALUES
    ('house_piece_1', 'house', '{"x": {"constant": 0}, "y": {"constant": 0}}', '[]'),
    ('house_piece_2', 'house', '{"x": {"constant": 0}, "y": {"constant": 0}}', '[]');


-- Pieces: T
INSERT INTO piece (id, game_type_name, start_point, vertices)
VALUES
    ('t_piece_1', 't', '{"x": {"constant": 0}, "y": {"constant": 0}}', '[]');


-- Tasks: tangram
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('tangram_001', 'tangram', '[]'),
    ('tangram_002', 'tangram', '[]'),
    ('tangram_003', 'tangram', '[
          [
            {
              "x": { "constant": 0 },
              "y": { "constant": 0 }
            },
            {
              "x": { "constant": 4 },
              "y": { "constant": 0 }
            },
            {
              "x": { "constant": 4 },
              "y": { "constant": 4 }
            },
            {
              "x": { "constant": 0 },
              "y": { "constant": 4 }
            }
          ]
        ]');


-- Tasks: house
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('house_001', 'house', '[]'),
    ('house_002', 'house', '[]');


-- Tasks: T
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('t_001', 't', '[]');