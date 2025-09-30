package dev.ltocca.loanranger;

import dev.ltocca.loanranger.util.PasswordHasher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHasherTest {

    @Test
    void testHashAndCheck() {
        String plainPassword = "mySecurePassword123!";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(hashedPassword).isNotEqualTo(plainPassword);
        assertThat(PasswordHasher.check(plainPassword, hashedPassword)).isTrue();
    }

    @Test
    void testCheckWithWrongPassword() {
        String plainPassword = "mySecurePassword123!";
        String wrongPassword = "wrongPassword";
        String hashedPassword = PasswordHasher.hash(plainPassword);

        assertThat(PasswordHasher.check(wrongPassword, hashedPassword)).isFalse();
    }

    @Test
    void testCheckWithNullInputs() {
        String hashedPassword = PasswordHasher.hash("somePassword");

        assertThat(PasswordHasher.check(null, hashedPassword)).isFalse();

        assertThat(PasswordHasher.check("password", null)).isFalse();
    }
}