package com.example.bankcards.util;

import java.security.SecureRandom;

public class PanGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generate() {
        StringBuilder pan = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            pan.append(random.nextInt(10)); // 0..9
        }

        return pan.toString();
    }
}
