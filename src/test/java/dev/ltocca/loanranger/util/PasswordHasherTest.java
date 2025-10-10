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
}