-- Game types
INSERT INTO game_type (name, display_name, base_shape, unit_size)
VALUES
    ('tangram', 'Tangram', '[]', 50.0),
    ('house', 'House', '[]', 50.0),
    ('t', 'T', '[]', 50.0);

-- Tasks
INSERT INTO task (id, game_type_name, polygons)
VALUES
    ('tangram_001', 'tangram', '[]'),
    ('tangram_002', 'tangram', '[
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
        ]'),
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
        ]'),
    ('house_001', 'house', '[]'),
    ('house_002', 'house', '[]'),
    ('t_001', 't', '[]');

-- Users
INSERT INTO user_data (username, password_hash, active)
VALUES
    ('user1', 'hash1', true),
    ('user2', 'hash2', true);

-- Solved tasks for user1
INSERT INTO user_solved_task (user_id, task_id)
SELECT id, 'tangram_001'
FROM user_data
WHERE username = 'user1';

INSERT INTO user_solved_task (user_id, task_id)
SELECT id, 'tangram_002'
FROM user_data
WHERE username = 'user1';

INSERT INTO user_solved_task (user_id, task_id)
SELECT id, 'house_001'
FROM user_data
WHERE username = 'user1';

-- Solved tasks for user2
INSERT INTO user_solved_task (user_id, task_id)
SELECT id, 'tangram_003'
FROM user_data
WHERE username = 'user2';

INSERT INTO user_solved_task (user_id, task_id)
SELECT id, 'house_002'
FROM user_data
WHERE username = 'user2';