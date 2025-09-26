classDiagram
direction LR
class book_copies {
varchar(13) isbn
bigint library_id
book_status status
bigint copy_id
}
class books {
varchar(255) title
varchar(255) author
integer publication_year
varchar(100) genre
varchar(13) isbn
}
class libraries {
varchar(255) name
varchar(500) address
varchar(50) phone
varchar(255) email
bigint library_id
}
class reservations {
bigint copy_id
bigint member_id
date reservation_date
varchar(50) status
bigint reservation_id
}
class users {
varchar(100) username
varchar(200) name
varchar(255) email
varchar(512) password
user_role role
bigint library_id
bigint user_id
}

book_copies  -->  books : isbn
book_copies  -->  libraries : library_id
reservations  -->  book_copies : copy_id
reservations  -->  users : member_id
users  -->  libraries : library_id