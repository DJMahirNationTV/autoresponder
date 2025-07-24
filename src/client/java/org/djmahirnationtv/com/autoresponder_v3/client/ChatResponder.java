package org.djmahirnationtv.com.autoresponder_v3.client;

import net.minecraft.client.MinecraftClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatResponder {
    private static final Map<String, String> triggers = new ConcurrentHashMap<>();
    static {
        assert MinecraftClient.getInstance().player != null;
        triggers.put("hello", "Hi there !");
    }

    public static void onChat(String msg, String name) {
        assert MinecraftClient.getInstance().player != null;
        String myName = MinecraftClient.getInstance().player.getName().getString();
        if (name.equals(myName)) return; // Ignore own messages

        triggers.forEach((t, r) -> {
            if (msg.contains(t.toLowerCase())) reply(r);
        });
    }

    public static void reply(String resp) {
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(resp);
    }

    public static Map<String, String> getTriggers() {
        return triggers;
    }

    // Additional methods to register/remove triggers programmatically
}
