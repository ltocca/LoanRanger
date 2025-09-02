# General

Find how to have an admin, separated from the librarian.


### ConnectionManager.java

- Instead of using the simple ConnectionManager implementation, maybe use Hikari to create a connection pool;
- Use a .env file for the credentials;