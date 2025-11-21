-- Drop existing tables to ensure a clean state
DROP TABLE IF EXISTS reservations, loans, book_copies, books, users, libraries CASCADE;
DROP TYPE IF EXISTS user_role, book_status, reservation_status CASCADE;


-- Create ENUM types for status fields (This now works perfectly)
CREATE TYPE user_role AS ENUM ('MEMBER', 'LIBRARIAN', 'ADMIN');
CREATE TYPE book_status AS ENUM ('AVAILABLE', 'LOANED', 'RESERVED', 'UNDER_MAINTENANCE');
CREATE TYPE reservation_status AS ENUM ('PENDING', 'FULFILLED', 'CANCELLED', 'WAITING');

-- Create tables (These are your original, unmodified table definitions)
CREATE TABLE libraries
(
    library_id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    address    VARCHAR(255),
    phone      VARCHAR(50),
    email      VARCHAR(255) UNIQUE
);

CREATE TABLE users
(
    user_id    BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    name       VARCHAR(255),
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       user_role    NOT NULL,
    library_id BIGINT,
    FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE SET NULL
);

CREATE TABLE books
(
    isbn             VARCHAR(20) PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    publication_year INT,
    genre            VARCHAR(100)
);

CREATE TABLE book_copies
(
    copy_id    BIGSERIAL PRIMARY KEY,
    isbn       VARCHAR(20) NOT NULL,
    library_id BIGINT      NOT NULL,
    status     book_status NOT NULL,
    FOREIGN KEY (isbn) REFERENCES books (isbn) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries (library_id) ON DELETE CASCADE
);

CREATE TABLE loans
(
    loan_id     BIGSERIAL PRIMARY KEY,
    copy_id     BIGINT NOT NULL,
    member_id   BIGINT NOT NULL,
    loan_date   DATE   NOT NULL,
    due_date    DATE   NOT NULL,
    return_date DATE,
    FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE reservations
(
    reservation_id   BIGSERIAL PRIMARY KEY,
    copy_id          BIGINT             NOT NULL,
    member_id        BIGINT             NOT NULL,
    reservation_date DATE               NOT NULL,
    status           reservation_status NOT NULL,
    FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES users (user_id) ON DELETE CASCADE
);