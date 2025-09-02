CREATE TABLE libraries
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    address    VARCHAR(500) NOT NULL,
    phone      VARCHAR(50),
    email      VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(512)        NOT NULL,
    role       VARCHAR(50)         NOT NULL CHECK (role IN ('CLIENT', 'LIBRARIAN')), -- maybe add 'ADMIN'
    library_id BIGINT REFERENCES libraries (id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE books
(
    id           SERIAL PRIMARY KEY,
    isbn         VARCHAR(13)  NOT NULL, -- updated to the last standard
    title        VARCHAR(500) NOT NULL,
    author       VARCHAR(255) NOT NULL,
    is_available BOOLEAN   DEFAULT TRUE,
    library_id   BIGINT REFERENCES libraries (id) ON DELETE CASCADE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loans
(
    id          SERIAL PRIMARY KEY,
    book_id     BIGINT NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    library_id  BIGINT NOT NULL REFERENCES libraries (id) ON DELETE CASCADE,
    loan_date   DATE   NOT NULL DEFAULT CURRENT_DATE,
    due_date    DATE   NOT NULL,
    return_date DATE,
    is_returned BOOLEAN         DEFAULT FALSE,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reservations
(
    id               SERIAL PRIMARY KEY,
    book_id          BIGINT NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    user_id          BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    library_id       BIGINT REFERENCES libraries (id) ON DELETE SET NULL,
    reservation_date DATE   NOT NULL DEFAULT CURRENT_DATE,
    is_active        BOOLEAN         DEFAULT TRUE,
    created_at       TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events
(
    id                SERIAL PRIMARY KEY,
    library_id        BIGINT       NOT NULL REFERENCES libraries (id) ON DELETE CASCADE,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    event_type        VARCHAR(50)  NOT NULL CHECK (event_type IN
                                                   ('BOOK_PRESENTATION', 'POETRY_READING', 'AUTHOR_TALK', 'WORKSHOP',
                                                    'BOOK_CLUB', 'STORYTELLING', 'LECTURE', 'EXHIBITION')),
    event_datetime    TIMESTAMP    NOT NULL,
    location          VARCHAR(255),
    max_capacity      INTEGER      NOT NULL CHECK (max_capacity > 0),
    current_attendees INTEGER   DEFAULT 0 CHECK (current_attendees >= 0),
    is_active         BOOLEAN   DEFAULT TRUE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_attendance
(
    id                SERIAL PRIMARY KEY,
    event_id          BIGINT NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    user_id           BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attended          BOOLEAN   DEFAULT FALSE,
    UNIQUE (event_id, user_id) -- Added in order to prevent duplicate registrations
);


-- TODO: Connect to db file