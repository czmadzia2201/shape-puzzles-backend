CREATE TABLE IF NOT EXISTS game_type
(
    name VARCHAR(25) NOT NULL,
    display_name VARCHAR(25) NOT NULL,
    base_shape JSONB NOT NULL,
    unit_size DOUBLE PRECISION NOT NULL,
    CONSTRAINT game_type_pkey PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS piece
(
    id VARCHAR(25) NOT NULL,
    game_type_name VARCHAR(25) NOT NULL,
    start_point JSONB NOT NULL,
    vertices JSONB NOT NULL,
    CONSTRAINT piece_pkey PRIMARY KEY (id),
    CONSTRAINT game_type_fkey FOREIGN KEY (game_type_name) REFERENCES game_type (name)
);

CREATE TABLE IF NOT EXISTS task
(
    id VARCHAR(25) NOT NULL,
    game_type_name VARCHAR(25) NOT NULL,
    polygons JSONB NOT NULL,
    CONSTRAINT task_pkey PRIMARY KEY (id),
    CONSTRAINT game_type_fkey FOREIGN KEY (game_type_name) REFERENCES game_type (name)
);

CREATE TABLE IF NOT EXISTS user_data
(
    id BIGSERIAL NOT NULL,
    username VARCHAR(25) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT user_data_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_username
    ON user_data (username)
    WHERE active = true;

CREATE TABLE IF NOT EXISTS user_solved_task
(
    user_id BIGINT NOT NULL,
    task_id VARCHAR(25) NOT NULL,
    CONSTRAINT user_solved_task_pkey PRIMARY KEY (user_id, task_id),
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id) REFERENCES user_data (id),
    CONSTRAINT task_id_fkey FOREIGN KEY (task_id) REFERENCES task (id)
);