package org.djmahirnationtv.com.autoresponder_v3.client;

import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ChatResponder {
    private static final Map<String, String> triggers = new ConcurrentHashMap<>();

    static {
        triggers.put("-> to you |", "Hi there!");
    }

    public static void onChat(String msg, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String myName = client.player.getName().getString();
        if (name.equals(myName)) return;

        String cleanMsg = ChatResponderScreen.stripColorCodes(msg);

        for (Map.Entry<String, String> entry : triggers.entrySet()) {
            String trigger = entry.getKey();
            boolean enabled = ChatResponderScreen.getTriggerEnabled(trigger);
            boolean regex = ChatResponderScreen.getTriggerRegex(trigger);
            if (!enabled) continue;

            if (regex) {
                if (Pattern.compile(trigger).matcher(cleanMsg).find()) {
                    reply(entry.getValue());
                    return;
                }
            } else {
                if (cleanMsg.contains(trigger)) {
                    reply(entry.getValue());
                    return;
                }
            }
        }
    }

    public static void reply(String resp) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatMessage(resp);
        }
    }

    public static Map<String, String> getTriggers() {
        return triggers;
    }
}