// File: java/org/djmahirnationtv/com/autoresponder_v3/client/ChatResponder.java

package org.djmahirnationtv.com.autoresponder_v3.client;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class ChatResponder {
    private static final Map<String, String> triggers = new ConcurrentHashMap<>();
    private static final Map<String, Integer> triggerDelays = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> triggerEnabledMap = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> triggerRegexMap = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static boolean mentionOnly = false;
    public static boolean soundFeedbackEnabled = false;
    public static String webhookUrl = "";

    private static final Path SETTINGS_FILE = Path.of("config/autoresponder_settings.txt");

    public static void onChat(String msg, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String myName = client.player.getName().getString();
        if (name.equals(myName)) return;

        String cleanMsg = stripColorCodes(msg);

        if (mentionOnly && !cleanMsg.toLowerCase().contains(myName.toLowerCase())) return;

        for (Map.Entry<String, String> entry : triggers.entrySet()) {
            String trigger = entry.getKey();
            boolean enabled = getTriggerEnabled(trigger);
            boolean regex = getTriggerRegex(trigger);
            if (!enabled) continue;

            boolean match = regex ? Pattern.compile(trigger).matcher(cleanMsg).find() : cleanMsg.contains(trigger);

            if (match) {
                int delay = triggerDelays.getOrDefault(trigger, 0);
                String response = replacePlaceholders(entry.getValue(), name);
                scheduler.schedule(() -> {
                    reply(response);
                    playSound();
                    sendWebhook(trigger, cleanMsg);
                }, delay, TimeUnit.SECONDS);
                return;
            }
        }
    }

    private static String replacePlaceholders(String response, String name) {
        String player = MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getName().getString() : "Player";
        String time = LocalTime.now().withNano(0).toString();
        return response
                .replace("%player%", player)
                .replace("%sender%", name)
                .replace("%time%", time);
    }

    public static void reply(String resp) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.networkHandler.sendChatMessage(resp);
        }
    }

    private static void playSound() {
        if (!soundFeedbackEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        }
    }

    private static void sendWebhook(String trigger, String message) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        try {
            Map<String, Object> embed = new LinkedHashMap<>();
            embed.put("title", "Triggered Response");
            embed.put("description", "Trigger: " + trigger + "\nMessage: " + message);
            embed.put("color", 15277667);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("embeds", List.of(embed));
            payload.put("attachments", new ArrayList<>());

            String json = new Gson().toJson(payload);

            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code != 200 && code != 204) {
                Scanner scanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                if (scanner.hasNext()) {
                    System.err.println("[Webhook Error] " + scanner.next());
                }
            } else {
                System.out.println("[Webhook Success] Status Code: " + code);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[Webhook Exception] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveGlobalSettings() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            StringBuilder content = new StringBuilder();
            content.append("mentionOnly=").append(mentionOnly).append("\n");
            content.append("soundFeedbackEnabled=").append(soundFeedbackEnabled).append("\n");
            content.append("webhookUrl=").append(webhookUrl).append("\n");
            Files.writeString(SETTINGS_FILE, content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadGlobalSettings() {
        if (!Files.exists(SETTINGS_FILE)) return;
        try {
            for (String line : Files.readAllLines(SETTINGS_FILE)) {
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue;
                switch (parts[0]) {
                    case "mentionOnly" -> mentionOnly = Boolean.parseBoolean(parts[1]);
                    case "soundFeedbackEnabled" -> soundFeedbackEnabled = Boolean.parseBoolean(parts[1]);
                    case "webhookUrl" -> webhookUrl = parts[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getTriggers() {
        return triggers;
    }

    public static Map<String, Integer> getTriggerDelays() {
        return triggerDelays;
    }

    public static boolean getTriggerEnabled(String trigger) {
        return triggerEnabledMap.getOrDefault(trigger, true);
    }

    public static boolean getTriggerRegex(String trigger) {
        return triggerRegexMap.getOrDefault(trigger, false);
    }

    public static void setTriggerEnabled(String trigger, boolean enabled) {
        triggerEnabledMap.put(trigger, enabled);
    }

    public static void setTriggerRegex(String trigger, boolean regex) {
        triggerRegexMap.put(trigger, regex);
    }

    public static String stripColorCodes(String message) {
        return message.replaceAll("\u00a7[0-9a-fk-or]", "");
    }
}
