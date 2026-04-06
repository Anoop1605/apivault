package com.apivault.eventstore;

import java.security.MessageDigest;

public class HashUtil {

    public static String generateHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());

            StringBuilder hex = new StringBuilder();

            for (byte b : hashBytes) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}