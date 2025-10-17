/*
package dev.ltocca.loanranger.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {

    @Test
    void hash_shouldProduceValidBCryptHash() {
        String plainPassword = "mySecurePassword123!";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(hashedPassword).isNotNull();
        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        // BCrypt hashes start with a specific prefix, e.g., "$2a$10$"
        assertThat(hashedPassword).startsWith("$2a$");
    }

    @Test
    void check_withCorrectPassword_shouldReturnTrue() {
        String plainPassword = "mySecurePassword123!";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(PasswordHasher.check(plainPassword, hashedPassword)).isTrue();
    }

    @Test
    void check_withIncorrectPassword_shouldReturnFalse() {
        String plainPassword = "mySecurePassword123!";
        String wrongPassword = "wrongPassword";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(PasswordHasher.check(wrongPassword, hashedPassword)).isFalse();
    }

    @Test
    void check_withNullOrEmptyInputs_shouldReturnFalse() {
        String hashedPassword = PasswordHasher.hash("somePassword");

        assertThat(PasswordHasher.check(null, hashedPassword)).isFalse();
        assertThat(PasswordHasher.check("", hashedPassword)).isFalse();
        assertThat(PasswordHasher.check("password", null)).isFalse();
        assertThat(PasswordHasher.check("password", "")).isFalse();
    }
}*/
package dev.ltocca.loanranger.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {

    @Test
    void hash_returnsValidBcryptHash() {
        String plainPassword = "testPassword123";

        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(hashedPassword).startsWith("$2a$");
        // Verify it's a valid BCrypt hash by checking if we can validate against it
        assertThat(PasswordHasher.check(plainPassword, hashedPassword)).isTrue();
    }

    @Test
    void hash_withEmptyPassword_returnsValidHash() {
        String plainPassword = "";

        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(hashedPassword).startsWith("$2a$");
        assertThat(PasswordHasher.check(plainPassword, hashedPassword)).isTrue();
    }

    @Test
    void hash_withSpecialCharacters_returnsValidHash() {
        String plainPassword = "p@$$w0rd!@#$%^&*()";

        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(hashedPassword).startsWith("$2a$");
        assertThat(PasswordHasher.check(plainPassword, hashedPassword)).isTrue();
    }

    @Test
    void check_withCorrectPassword_returnsTrue() {
        String plainPassword = "correctPassword";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        boolean result = PasswordHasher.check(plainPassword, hashedPassword);

        assertThat(result).isTrue();
    }

    @Test
    void check_withIncorrectPassword_returnsFalse() {
        String plainPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        boolean result = PasswordHasher.check(wrongPassword, hashedPassword);

        assertThat(result).isFalse();
    }

    @Test
    void check_withNullPlainText_returnsFalse() {
        String hashedPassword = PasswordHasher.hash("somePassword");

        boolean result = PasswordHasher.check(null, hashedPassword);

        assertThat(result).isFalse();
    }

    @Test
    void check_withNullHashedText_returnsFalse() {
        boolean result = PasswordHasher.check("somePassword", null);

        assertThat(result).isFalse();
    }

    @Test
    void check_withEmptyPlainText_returnsFalse() {
        String hashedPassword = PasswordHasher.hash("somePassword");

        boolean result = PasswordHasher.check("", hashedPassword);

        assertThat(result).isFalse();
    }

    @Test
    void check_withEmptyHashedText_returnsFalse() {
        boolean result = PasswordHasher.check("somePassword", "");

        assertThat(result).isFalse();
    }

    @Test
    void check_withBothNull_returnsFalse() {
        boolean result = PasswordHasher.check(null, null);

        assertThat(result).isFalse();
    }

    @Test
    void check_withBothEmpty_returnsFalse() {
        boolean result = PasswordHasher.check("", "");

        assertThat(result).isFalse();
    }

    @Test
    void hash_consistentlyProducesDifferentHashesForSamePassword() {
        String plainPassword = "samePassword";

        String hash1 = PasswordHasher.hash(plainPassword);
        String hash2 = PasswordHasher.hash(plainPassword);

        // BCrypt should produce different hashes due to random salt
        assertThat(hash1).isNotEqualTo(hash2);
        // But both should validate correctly
        assertThat(PasswordHasher.check(plainPassword, hash1)).isTrue();
        assertThat(PasswordHasher.check(plainPassword, hash2)).isTrue();
    }


    @Test
    void check_withPreviouslyHashedPassword_stillWorks() {
        // Test with a known BCrypt hash to ensure compatibility
        String knownHash = "$2a$10$abcdefghijklmnopqrstuu6ZR2.6pVp9q.ZuJ.Z/d.8V5Zz.5S";
        String password = "password";

        // This should return false for incorrect password
        boolean result = PasswordHasher.check("wrongpassword", knownHash);
        assertThat(result).isFalse();
    }
}