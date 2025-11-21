-- This script will be run automatically by Spring Boot on startup
-- because of 'spring.sql.init.mode=always' in application-dev.properties.

-- Clear existing data to ensure a clean slate on each restart
TRUNCATE libraries, users, books, book_copies, loans, reservations RESTART IDENTITY CASCADE;

-- Create Libraries
INSERT INTO libraries (name, address, phone, email) VALUES
                                                        ('Central City Library', '123 Knowledge Ave, Central City', '555-0101', 'contact@ccl.com'),
                                                        ('Maplewood Branch', '456 Oak St, Maplewood', '555-0102', 'maplewood@ccl.com');

-- Create Users (Password for all is "password")
-- Hashed using BCrypt: $2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr
INSERT INTO users (username, name, email, password, role, library_id) VALUES
                                                                          ('admin', 'Admin User', 'admin@loanranger.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr', 'ADMIN', NULL),
                                                                          ('librarian_jane', 'Jane Doe', 'jane.doe@ccl.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr', 'LIBRARIAN', 1),
                                                                          ('member_john', 'John Smith', 'john.smith@email.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr', 'MEMBER', NULL);

-- Create Books
INSERT INTO books (isbn, title, author, publication_year, genre) VALUES
                                                                     ('978-0321765723', 'The C++ Programming Language', 'Bjarne Stroustrup', 2013, 'Programming'),
                                                                     ('978-0132350884', 'Clean Code', 'Robert C. Martin', 2008, 'Software Craftsmanship'),
                                                                     ('978-1491904244', 'Designing Data-Intensive Applications', 'Martin Kleppmann', 2017, 'Databases');

-- Create Book Copies
INSERT INTO book_copies (isbn, library_id, status) VALUES
                                                       ('978-0321765723', 1, 'AVAILABLE'),
                                                       ('978-0132350884', 1, 'AVAILABLE'),
                                                       ('978-0132350884', 2, 'LOANED'),
                                                       ('978-1491904244', 1, 'RESERVED');

-- Create a Loan for the "LOANED" book
INSERT INTO loans (copy_id, member_id, loan_date, due_date) VALUES
    (3, 3, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days');

-- Create a Reservation for the "RESERVED" book
INSERT INTO reservations (copy_id, member_id, reservation_date, status) VALUES
    (4, 3, CURRENT_DATE - INTERVAL '1 day', 'PENDING');