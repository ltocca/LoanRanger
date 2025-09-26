-- Drop tables in reverse order of dependency to avoid foreign key constraints
DROP TABLE IF EXISTS loans, reservations, book_copies, books, users, libraries CASCADE;
DROP TYPE IF EXISTS user_role, book_status, reservation_status;

CREATE TYPE user_role AS ENUM ('MEMBER', 'LIBRARIAN', 'ADMIN');
CREATE TYPE book_status AS ENUM ('AVAILABLE', 'LOANED', 'RESERVED', 'UNDER_MAINTENANCE');
CREATE TYPE reservation_status AS ENUM ('PENDING', 'FULFILLED', 'CANCELLED', 'WAITING');

CREATE TABLE libraries
(
    library_id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    address    VARCHAR(500) NOT NULL,
    phone      VARCHAR(50),
    email      VARCHAR(255)
);

CREATE TABLE users
(
    user_id    BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) UNIQUE NOT NULL,
    name       VARCHAR(200)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(512)        NOT NULL, -- Should be saved with a hash
    role       user_role           NOT NULL,
    library_id BIGINT,                       -- Nullable, right now only relevant for Librarians;
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
    copy_id    BIGSERIAL PRIMARY KEY,
    isbn       VARCHAR(13) NOT NULL,
    library_id BIGINT      NOT NULL,
    status     book_status NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (isbn) REFERENCES books (isbn) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE CASCADE
);

CREATE TABLE loans
(
    loan_id     BIGSERIAL PRIMARY KEY,
    copy_id     BIGINT NOT NULL,
    member_id   BIGINT NOT NULL,
    loan_date   DATE   NOT NULL DEFAULT CURRENT_DATE,
    due_date    DATE   NOT NULL,
    return_date DATE,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE reservations
(
    reservation_id   BIGSERIAL PRIMARY KEY,
    copy_id          BIGINT NOT NULL,
    member_id        BIGINT NOT NULL,
    reservation_date DATE   NOT NULL DEFAULT CURRENT_DATE,
    status           VARCHAR(50)     DEFAULT 'PENDING', -- PENDING, FULFILLED, CANCELED
    FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE
);

INSERT INTO users (role, username, name, email, password, library_id)
VALUES ('ADMIN', 'superadmin', 'Super Admin', 'super@library.org',
        '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL);