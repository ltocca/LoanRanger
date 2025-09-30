package dev.ltocca.loanranger.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    public static boolean check(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}