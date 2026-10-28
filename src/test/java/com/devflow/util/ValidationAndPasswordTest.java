package com.devflow.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationAndPasswordTest {
    @Test void emailValidation() {
        assertTrue(ValidationUtil.isValidEmail("a.b+c@devflow.dev"));
        assertFalse(ValidationUtil.isValidEmail("not-an-email"));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test void projectKeyValidation() {
        assertTrue(ValidationUtil.isValidProjectKey("ECOM"));
        assertTrue(ValidationUtil.isValidProjectKey("A1"));
        assertFalse(ValidationUtil.isValidProjectKey("ecom"));
        assertFalse(ValidationUtil.isValidProjectKey("1ABC"));
        assertFalse(ValidationUtil.isValidProjectKey("TOOLONGKEY1"));
    }

    @Test void passwordStrength() {
        assertTrue(ValidationUtil.isStrongPassword("Passw0rd1"));
        assertFalse(ValidationUtil.isStrongPassword("short1"));
        assertFalse(ValidationUtil.isStrongPassword("onlyletters"));
        assertFalse(ValidationUtil.isStrongPassword("12345678"));
    }

    @Test void parsingHelpers() {
        assertEquals(5, ValidationUtil.parseInt(" 5 "));
        assertNull(ValidationUtil.parseInt("abc"));
        assertNull(ValidationUtil.parseDate(""));
        assertThrows(ServiceException.class, () -> ValidationUtil.parseDate("31/12/2030"));
    }

    @Test void bcryptNeverStoresPlainTextAndVerifies() {
        String hash = PasswordUtil.hash("Secret123");
        assertNotEquals("Secret123", hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(PasswordUtil.verify("Secret123", hash));
        assertFalse(PasswordUtil.verify("secret123", hash));
        assertFalse(PasswordUtil.verify("Secret123", "not-a-hash"));
    }
}
