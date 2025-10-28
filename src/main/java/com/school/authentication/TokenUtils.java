package com.school.authentication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    public static String generateToken(int length) {
        final var tokenCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        final var builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(tokenCharacters.charAt(RANDOM.nextInt(tokenCharacters.length())));
        }
        return builder.toString();
    }
}
