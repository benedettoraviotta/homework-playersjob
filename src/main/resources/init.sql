CREATE TABLE job_state (
    id SERIAL PRIMARY KEY,
    status VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP
);

CREATE TABLE players (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    date_of_birth VARCHAR(255),
    age INTEGER,
    height INTEGER,
    foot VARCHAR(255),
    joined_on VARCHAR(255),
    signed_from VARCHAR(255),
    contract VARCHAR(255),
    market_value INTEGER,
    status VARCHAR(255)
);

CREATE TABLE player_nationalities (
    player_id VARCHAR(255) NOT NULL,
    nationality VARCHAR(255) NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);

CREATE TABLE processed_player (
    id SERIAL PRIMARY KEY,
    player_id VARCHAR(255) NOT NULL,
    job_id INTEGER NOT NULL,
    processed_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (job_id) REFERENCES job_state(id)
);