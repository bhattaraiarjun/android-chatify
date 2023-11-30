package com.example.chatify.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatifyUtils {
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
}
