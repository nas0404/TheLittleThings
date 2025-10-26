-- Contributor: @Naseem Win

-- Reset existing tables (order matters due to FKs)
DROP TABLE IF EXISTS challenges CASCADE;
DROP TABLE IF EXISTS wins CASCADE;
DROP TABLE IF EXISTS journaling CASCADE;
DROP TABLE IF EXISTS goals CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friendships CASCADE;
DROP TABLE IF EXISTS friend_challenges CASCADE;

-- Core user dimension
CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50) NOT NULL,
    dob         DATE NOT NULL,
    age         INT,                              -- Calculated from dob
    gender      VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female', 'Other')),
    streaks     INT DEFAULT 0,
    region      VARCHAR(100),
    created_at  TIMESTAMP DEFAULT NOW(),
    last_login  TIMESTAMPTZ,
    trophies    INT DEFAULT 0
);

-- Category taxonomy scoped per user
CREATE TABLE categories (
    category_id   SERIAL PRIMARY KEY,
    user_id       INT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW(),
    CONSTRAINT uq_categories_user_name UNIQUE (user_id, name),
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- Goal tracking with optional category linkage
CREATE TABLE goals (
    goal_id     SERIAL PRIMARY KEY,
    user_id     INT NOT NULL,
    category_id INT,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    priority    VARCHAR(10) NOT NULL
                 CHECK (priority IN ('HIGH','MEDIUM','LOW')),
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_goals_category FOREIGN KEY (category_id)
        REFERENCES categories(category_id) ON DELETE SET NULL
);

-- Maintain goal.updated_at automatically
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_goals_set_updated_at
BEFORE UPDATE ON goals
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Journaling entries aligned to users
CREATE TABLE journaling (
    journal_id  SERIAL PRIMARY KEY,
    user_id     INT NOT NULL,
    title       VARCHAR(255) NOT NULL,
    content     TEXT NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_j_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- Win ledger linking goals and journals
CREATE TABLE wins (
    win_id          SERIAL PRIMARY KEY,
    user_id         INT NOT NULL,
    goal_id         INT NOT NULL,
    journal_id      INT UNIQUE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    num_trophies    INT DEFAULT 0,
    completion_date TIMESTAMP,
    CONSTRAINT fk_wins_user    FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_wins_goal    FOREIGN KEY (goal_id)
        REFERENCES goals(goal_id) ON DELETE CASCADE,
    CONSTRAINT fk_wins_journal FOREIGN KEY (journal_id)
        REFERENCES journaling(journal_id) ON DELETE SET NULL
);

-- Solo challenges for personal goal bundles
CREATE TABLE challenges (
    challenge_id SERIAL PRIMARY KEY,
    user_id      INT NOT NULL,
    goal_list    TEXT NOT NULL,
    target_date  DATE,
    trophies     INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_challenges_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- Friend relationships
CREATE TABLE IF NOT EXISTS friendships (
    id SERIAL PRIMARY KEY,
    userA_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    userB_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    requested_by INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    responded_by INT REFERENCES users(user_id) ON DELETE SET NULL,
    requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    responded_at TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_friend_pair UNIQUE (userA_id, userB_id),
    CONSTRAINT chk_not_self CHECK (userA_id <> userB_id)
);

-- Competitive friend challenges with escrow mechanics
CREATE TABLE IF NOT EXISTS friend_challenges (
    id SERIAL PRIMARY KEY,
    challenger_id   INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    opponent_id     INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    goal_list       TEXT NOT NULL,
    start_date      DATE,
    end_date        DATE,
    trophies_stake  INT DEFAULT 0,
    status          VARCHAR(32) NOT NULL DEFAULT 'PROPOSED',
    winner_user_id  INT REFERENCES users(user_id) ON DELETE SET NULL,
    escrowed                    BOOLEAN NOT NULL DEFAULT false,
    completion_requested_by_id  INT REFERENCES users(user_id) ON DELETE SET NULL,
    completion_requested_at     TIMESTAMPTZ,
    completion_confirmed_by_id  INT REFERENCES users(user_id) ON DELETE SET NULL,
    completion_confirmed_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_fc_not_self CHECK (challenger_id <> opponent_id)
);

-- Seed demo accounts (password hashes are placeholders)
INSERT INTO users (username, email, password, first_name, last_name, dob, age, gender, streaks, region, trophies, created_at)
VALUES
    ('alice', 'alice@email', '4e40e8ffe0ee32fa53e139147ed559229a5930f89c2204706fc174beb36210b3', 'Alice', 'Anderson', '2000-05-15', 24, 'Female', 3, 'EU', 50, NOW()),
    ('bob', 'bob@email', '8d059c3640b97180dd2ee453e20d34ab0cb0f2eccbe87d01915a8e578a202b11', 'Bob', 'Brown', '1998-07-20', 26, 'Male', 5, 'EU', 120, NOW()),
    ('charlie', 'charlie@email', '1f94d69d790d97505f9405bf85fdde90eeb6503b7d8aef5e499637d7c1ea2660', 'Charlie', 'Chaplin', '2002-09-10', 22, 'Male', 1, 'US', 10, NOW()),
    ('diana', 'diana@email', 'fbc7a4a6124b5cd5f80305e073f4a8dc4a98772586eb7520c3af422856c77a15', 'Diana', 'Doe', '1999-03-01', 25, 'Female', 2, 'US', 75, NOW()),
    ('eve', 'eve@email', '2efbfc598c9b2486f6d2c2d46686ba91c07a41de84ca8aeb621f40d927c2b283', 'Eve', 'Evans', '2001-12-25', 23, 'Other', 0, 'AU', 30, NOW());
