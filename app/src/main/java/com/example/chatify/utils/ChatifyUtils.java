package com.example.chatify.utils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ChatifyUtils {
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private static final String AES_KEY = "AAAAB3NzaC1yc2EAAAADAAAAAB3NzaC1yc2EAAAADAAAAAB3NzaC1yc2EAAAADA";
    public static String censorOffensiveWords(String message, ArrayList<String> offensiveWords) {
        String censoredMessage = message;
        for (String word : offensiveWords) {
            Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(censoredMessage);
            StringBuilder replacement = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                replacement.append("*");
            }
            censoredMessage = matcher.replaceAll(replacement.toString());
        }
        return censoredMessage;
    }

    public static String encryptMessage(String input) throws Exception {
        byte[] paddedKey = Arrays.copyOf(AES_KEY.getBytes(), 16); // Adjust to 16 bytes (128 bits)
        Key secretKeySpec = new SecretKeySpec(paddedKey, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decryptMessage(String input) throws Exception {
        byte[] paddedKey = Arrays.copyOf(AES_KEY.getBytes(), 16); // Adjust to 16 bytes (128 bits)
        Key secretKeySpec = new SecretKeySpec(paddedKey, AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}
