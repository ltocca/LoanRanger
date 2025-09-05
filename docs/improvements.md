# General

TODO: CLOSE EVERY STATEMENT IN ORDER TO PREVENT RUNS EVEN IF THROWN EXCEPTION
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

### ConnectionManager.java

- Instead of using the simple ConnectionManager implementation, maybe use Hikari to create a connection pool;
- Use a .env file for the credentials;

Password Hashing?