-- Drop tables in reverse order of dependency to avoid foreign key constraints
DROP TABLE IF EXISTS attendance, loans, reservations, book_copies, books, events, users, libraries CASCADE;
DROP TYPE IF EXISTS user_role, book_status, event_type;


CREATE TYPE user_role AS ENUM ('MEMBER', 'LIBRARIAN', 'ADMIN');
CREATE TYPE book_status AS ENUM ('AVAILABLE', 'LOANED', 'RESERVED', 'UNDER_MAINTENANCE');
CREATE TYPE event_type AS ENUM (
    'BOOK_PRESENTATION',
    'POETRY_READING',
    'AUTHOR_TALK',
    'WORKSHOP',
    'BOOK_CLUB',
    'STORYTELLING',
    'LECTURE',
    'EXHIBITION'
);

CREATE TABLE libraries
(
    library_id SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    address    VARCHAR(500) NOT NULL phone      VARCHAR(50),
    email      VARCHAR(255),
);

CREATE TABLE users
(
    user_id    SERIAL PRIMARY KEY,
    first_name VARCHAR(100)        NOT NULL,
    last_name  VARCHAR(100)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(512)        NOT NULL, -- Should be saved with a hash
    role       user_role           NOT NULL,
    library_id INT,                          -- Nullable, right now only relevant for Librarians;
    FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE SET NULL
);

CREATE TABLE books
(
    isbn             VARCHAR(13) PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    publication_year INT,
    genre            VARCHAR(100)
);

CREATE TABLE book_copies
(
    copy_id    SERIAL PRIMARY KEY,
    isbn       VARCHAR(13) NOT NULL,
    library_id INT         NOT NULL,
    status     book_status NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (isbn) REFERENCES books (isbn) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE CASCADE
);

CREATE TABLE loans
(
    loan_id     SERIAL PRIMARY KEY,
    copy_id     INT  NOT NULL UNIQUE, -- A copy can only be on one loan at a time
    member_id   INT  NOT NULL,
    loan_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date    DATE NOT NULL,
    return_date DATE,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE reservations
(
    reservation_id   SERIAL PRIMARY KEY,
    isbn             VARCHAR(13) NOT NULL,
    member_id        INT         NOT NULL,
    reservation_date DATE        NOT NULL DEFAULT CURRENT_DATE,
    status           VARCHAR(50)          DEFAULT 'PENDING', -- PENDING, FULFILLED, CANCELED
    library_id       INT,                                    -- Library where the member wants to pick up the book
    FOREIGN KEY (isbn) REFERENCES books (isbn) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE SET NULL,
    UNIQUE (isbn, member_id)                                 -- A member can only have one reservation per book title
);

CREATE TABLE events
(
    event_id     SERIAL PRIMARY KEY,
    library_id   INT          NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    event_date   DATETIME    NOT NULL,
    max_capacity INT CHECK (max_capacity > 0),
    event_type   event_type   NOT NULL,
    is_active    BOOLEAN   DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE CASCADE,

);

CREATE TABLE attendance
(
    attendance_id     SERIAL PRIMARY KEY,
    event_id          INT NOT NULL,
    member_id         INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events (event_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE,
    UNIQUE (event_id, member_id) -- A member can only register once for an event
);