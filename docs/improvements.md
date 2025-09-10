# General

# ORM package

## all

when the method returns optional print a 'System.err.println()' with more info.

### ConnectionManager.java

- Instead of using the simple ConnectionManager implementation, maybe use Hikari to create a connection pool;
- Use a .env file for the credentials;

~~Password Hashing~~, done!

## Domain model 

~~Edit Reservation class to modify the Book member to BookCopy~~

## DAO Classes
- TODO: CLOSE EVERY STATEMENT IN ORDER TO PREVENT RUNS EVEN IF THROWN EXCEPTION
to be done (usually) before the return statement
    ```java
    finally{
        if (pstmt != null){
            pstmt.close();
        }
        if (rs != null){
            rs.close();
        }
    }
    ```

- add trim() method to the string input to "sanitize" the strings 