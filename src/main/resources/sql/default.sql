INSERT INTO libraries (name, address, phone, email)
VALUES
    ('Central Library', '123 Main St, City', '555-0101', 'central@library.org'),
    ('West Branch', '456 West Ave, City', '555-0102', 'west@library.org'),
    ('Eastside Community Library', '789 East Blvd, City', '555-0103', 'east@library.org'),
    ('North Hills Library', '321 North Rd, City', '555-0104', 'north@library.org'),
    ('South District Library', '654 South St, City', '555-0105', 'south@library.org');


-- into the password value there is the 'password' string after bcrypt
INSERT INTO users (role, username, name, email, password, library_id)
VALUES
    ('MEMBER', 'john_doe', 'John Doe', 'john@example.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL),
    ('LIBRARIAN', 'jane_smith', 'Jane Smith', 'jane@library.org', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', 1),
    ('ADMIN', 'admin1', 'Admin One', 'admin@library.org', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL),
    ('MEMBER', 'alice_brown', 'Alice Brown', 'alice@example.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL),
    ('LIBRARIAN', 'mike_jones', 'Mike Jones', 'mike@westbranch.org', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', 2),
    ('LIBRARIAN', 'sarah_lee', 'Sarah Lee', 'sarah@eastside.org', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', 3),
    ('MEMBER', 'tom_wilson', 'Tom Wilson', 'tom@example.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL),
    ('MEMBER', 'lisa_ray', 'Lisa Ray', 'lisa@example.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL),
    ('LIBRARIAN', 'david_kim', 'David Kim', 'david@north.org', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', 4),
    ('MEMBER', 'emma_white', 'Emma White', 'emma@example.com', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL),
    ('MEMBER', 'chris_evans', 'Chris Evans', 'chris@example.com',
     '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL);
--('ADMIN', 'superadmin', 'Super Admin', 'super@library.org', '$2a$10$Iz3kYRcmqPy45Dokr58hLug5GdIAxfWE2LwOocfLO0k5E/8sqLsr6', NULL);

INSERT INTO books (isbn, title, author, genre, publication_year)
VALUES
    ('9781234567890', 'The Great Gatsby', 'F. Scott Fitzgerald', 'Fiction', 1925),
    ('9780987654321', '1984', 'George Orwell', 'Dystopian', 1949),
    ('9780141187761', 'To Kill a Mockingbird', 'Harper Lee', 'Fiction', 1960),
    ('9780061120084', 'The Catcher in the Rye', 'J.D. Salinger', 'Fiction', 1951),
    ('9780143105428', 'Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'Non-Fiction', 2011),
    ('9780307277671', 'The Road', 'Cormac McCarthy', 'Post-Apocalyptic', 2006),
    ('9780060935467', 'One Hundred Years of Solitude', 'Gabriel García Márquez', 'Magical Realism', 1967),
    ('9780385333849', 'A Thousand Splendid Suns', 'Khaled Hosseini', 'Historical Fiction', 2007),
    ('9780316769488', 'The Bell Jar', 'Sylvia Plath', 'Fiction', 1963),
    ('9780062315007', 'Dune', 'Frank Herbert', 'Science Fiction', 1965);

INSERT INTO book_copies (isbn, library_id, status)
VALUES
    ('9781234567890', 1, 'AVAILABLE'),
    ('9781234567890', 2, 'LOANED'),
    ('9780987654321', 1, 'RESERVED'),
    ('9780987654321', 2, 'AVAILABLE'),
    ('9780141187761', 1, 'AVAILABLE'),
    ('9780141187761', 3, 'AVAILABLE'),
    ('9780061120084', 2, 'AVAILABLE'),
    ('9780061120084', 4, 'AVAILABLE'),
    ('9780143105428', 1, 'AVAILABLE'),
    ('9780143105428', 5, 'AVAILABLE'),
    ('9780307277671', 3, 'RESERVED'),
    ('9780307277671', 4, 'LOANED'),
    ('9780060935467', 5, 'AVAILABLE'),
    ('9780385333849', 1, 'AVAILABLE'),
    ('9780316769488', 2, 'AVAILABLE'),
    ('9780062315007', 3, 'AVAILABLE');

INSERT INTO loans (copy_id, member_id, loan_date, due_date, return_date)
VALUES
    (2, 1, '2025-09-01', '2025-09-15', NULL),
    (7, 7, '2025-09-02', '2025-09-16', NULL),
    (12, 8, '2025-09-03', '2025-09-17', '2025-09-14'),
    (4, 10, '2025-09-05', '2025-09-19', NULL),
    (10, 11, '2025-09-07', '2025-09-21', NULL);

INSERT INTO reservations (copy_id, member_id, reservation_date, status)
VALUES
    (3, 4, '2025-09-10', 'PENDING'),
    (11, 7, '2025-09-11', 'PENDING'),
    (6, 10, '2025-09-12', 'FULFILLED'),
    (14, 1, '2025-09-13', 'PENDING');

