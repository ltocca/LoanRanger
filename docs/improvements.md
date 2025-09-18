# General

- when development of the packages is complete rename them to lowercase, following Google Java style
guide [5.2.1](https://google.github.io/styleguide/javaguide.html#s5.2.1-package-names)

- NEED TO UNDERSTAND WHERE DO I NEED TO CHECK THE INPUT AND TRIM IT
    
   - for example when a user updates the email right now I have two checks

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
~~- TODO: CLOSE EVERY STATEMENT IN ORDER TO PREVENT RUNS EVEN IF THROWN EXCEPTION
to be done (usually) before the return statement~~
    
```
    finally{
        if (pstmt != null){
            pstmt.close();
        }
        if (rs != null){
            rs.close();
        }
    }
```
    
~~also modify ALL THE DAO CLASSES to create and then close each connection to the db~~
[1](https://jenkov.com/tutorials/jdbc/connection.html)
[2](https://stackoverflow.com/questions/2225221/closing-database-connections-in-java)
[3](https://blog.jooq.org/how-to-prevent-jdbc-resource-leaks-with-jdbc-and-with-jooq/ )

~~but [here](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) it is written that from Java 8 the JDBC classes are AutoCloseable, and its `close()` method is called~~ 

IMPORTANT
maybe it is necessary to refactor the database code in order to improve transactions implementation


- add trim() method to the string input to "sanitize" the strings 

## Presentation package

Simple cli to use the program

## BusinessLogic

[//]: # (TODO: find more appropriate names for the controller classes, and make a separate facade for the events) 


