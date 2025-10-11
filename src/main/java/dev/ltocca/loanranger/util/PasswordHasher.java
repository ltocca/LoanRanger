package dev.ltocca.loanranger.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    // Use the standard Spring Security encoder. It is thread-safe.
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hash(String plainPassword) {
        // The encoder's encode method handles null by throwing an exception,
        // which is reasonable for a hashing operation.
        return encoder.encode(plainPassword);
    }

    public static boolean check(String plainPassword, String hashedPassword) {
        // Add a guard clause to handle all invalid inputs before calling the encoder.
        // This prevents exceptions from being thrown during a simple check.
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        // The matches method is safe against malformed hashes and will return false.
        return encoder.matches(plainPassword, hashedPassword);
    }
}