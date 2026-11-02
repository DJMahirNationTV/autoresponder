package org.djmahirnationtv.com.autoresponder_v3.client;

public class ChatUtils {
    public static String stripColorCodes(String message) {
        return message.replaceAll("§[0-9a-fk-or]", "");
    }
}