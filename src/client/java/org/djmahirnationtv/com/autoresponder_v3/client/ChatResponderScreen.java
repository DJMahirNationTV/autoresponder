// File: java/org/djmahirnationtv/com/autoresponder_v3/client/ChatResponderScreen.java

package org.djmahirnationtv.com.autoresponder_v3.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class ChatResponderScreen extends Screen {
    private TextFieldWidget triggerField;
    private TextFieldWidget responseField;
    private TextFieldWidget delayField;
    private ButtonWidget regexCheckbox;
    private ButtonWidget mentionCheckbox;
    private ButtonWidget soundCheckbox;
    private ButtonWidget addButton;
    private boolean regexChecked = false;
    private boolean mentionChecked = false;
    private boolean soundChecked = false;
    private int currentPage = 0;
    private static final int MAX_VISIBLE = 6;
    private static final Path SAVE_FILE = Path.of("config/autoresponder_triggers.json");

    public ChatResponderScreen() {
        super(Text.of("Chat Responder Config"));
        loadTriggers();
    }

    @Override
    protected void init() {
        clearChildren();

        int centerX = width / 2;
        int y = height / 4;

        triggerField = new TextFieldWidget(textRenderer, centerX - 90, y, 180, 20, Text.of(""));
        triggerField.setEditable(true);
        triggerField.setPlaceholder(Text.literal("Enter a word...").setStyle(Style.EMPTY.withColor(0x555555)));

        responseField = new TextFieldWidget(textRenderer, centerX - 90, y + 25, 180, 20, Text.of(""));
        responseField.setEditable(true);
        responseField.setPlaceholder(Text.literal("Enter a response...").setStyle(Style.EMPTY.withColor(0x555555)));
        responseField.setMaxLength(500);

        delayField = new TextFieldWidget(textRenderer, centerX - 90, y + 50, 180, 20, Text.of("0"));
        delayField.setEditable(true);
        delayField.setPlaceholder(Text.literal("Delay in seconds...").setStyle(Style.EMPTY.withColor(0x555555)));

        regexCheckbox = ButtonWidget.builder(Text.of((regexChecked ? "☑ " : "☐ ") + "Enable Regex"), btn -> {
            regexChecked = !regexChecked;
            init();
        }).position(centerX - 90, y + 75).size(180, 20).build();

        mentionCheckbox = ButtonWidget.builder(Text.of((mentionChecked ? "☑ " : "☐ ") + "Mention Only"), b -> {
            mentionChecked = !mentionChecked;
            init();
        }).position(centerX - 90, y + 100).size(180, 20).build();

        soundCheckbox = ButtonWidget.builder(Text.of((soundChecked ? "☑ " : "☐ ") + "Sound Feedback"), b -> {
            soundChecked = !soundChecked;
            init();
        }).position(centerX - 90, y + 125).size(180, 20).build();

        addButton = ButtonWidget.builder(Text.of("+ Add Trigger"), button -> {
            String trigger = triggerField.getText().trim();
            String response = responseField.getText().trim();
            int delay = 0;
            try {
                delay = (int) Double.parseDouble(delayField.getText().trim());
            } catch (NumberFormatException ignored) {}
            if (!trigger.isEmpty() && !response.isEmpty()) {
                ChatResponder.getTriggers().put(trigger, response);
                ChatResponder.setTriggerEnabled(trigger, true);
                ChatResponder.setTriggerRegex(trigger, regexChecked);
                ChatResponder.setTriggerMentionOnly(trigger, mentionChecked);
                ChatResponder.setTriggerSound(trigger, soundChecked);
                ChatResponder.setTriggerDelay(trigger, delay);
                triggerField.setText("");
                responseField.setText("");
                delayField.setText("0");
                regexChecked = false;
                mentionChecked = false;
                soundChecked = false;
                saveTriggers();
                init();
            }
        }).position(centerX - 90, y + 150).size(180, 20).build();

        addDrawableChild(triggerField);
        addDrawableChild(responseField);
        addDrawableChild(delayField);
        addDrawableChild(regexCheckbox);
        addDrawableChild(mentionCheckbox);
        addDrawableChild(soundCheckbox);
        addDrawableChild(addButton);

        List<Map.Entry<String, String>> entries = new ArrayList<>(ChatResponder.getTriggers().entrySet());
        int start = currentPage * MAX_VISIBLE;
        int end = Math.min(start + MAX_VISIBLE, entries.size());

        int listY = y + 185;
        for (int i = start; i < end; i++) {
            Map.Entry<String, String> entry = entries.get(i);
            String trigger = entry.getKey();
            String response = entry.getValue();
            boolean enabled = ChatResponder.getTriggerEnabled(trigger);
            boolean regex = ChatResponder.getTriggerRegex(trigger);
            String text = (regex ? "/" : "") + trigger + (regex ? "/" : "") + " -> " + response + (enabled ? "" : " (Disabled)");

            final ButtonWidget triggerButton = ButtonWidget.builder(Text.literal(text), btn -> {})
                    .position(centerX - 90, listY).size(180, 20)
                    .tooltip(Tooltip.of(Text.of(trigger + " -> " + response)))
                    .build();
            triggerButton.active = false;

            final ButtonWidget deleteButton = ButtonWidget.builder(Text.of("✕"), btn -> {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed) {
                        ChatResponder.getTriggers().remove(trigger);
                        ChatResponder.setTriggerEnabled(trigger, false);
                        saveTriggers();
                    }
                    MinecraftClient.getInstance().setScreen(this);
                    init();
                }, Text.of("Are you sure?"), Text.of("Do you really want to delete this trigger?")));
            }).position(centerX + 95, listY).size(20, 20).build();

            final ButtonWidget disableButton = ButtonWidget.builder(Text.of(enabled ? "❚❚" : "▶"), btn -> {
                ChatResponder.setTriggerEnabled(trigger, !enabled);
                saveTriggers();
                init();
            }).position(centerX + 120, listY).size(20, 20).build();

            addDrawableChild(triggerButton);
            addDrawableChild(deleteButton);
            addDrawableChild(disableButton);

            listY += 25;
        }

        int totalPages = (int) Math.ceil((double) entries.size() / MAX_VISIBLE);

        if (currentPage > 0) {
            addDrawableChild(ButtonWidget.builder(Text.of("← Prev"), b -> {
                currentPage--;
                init();
            }).position(centerX - 90, listY).size(80, 20).build());
        }

        if (currentPage < totalPages - 1) {
            addDrawableChild(ButtonWidget.builder(Text.of("Next →"), b -> {
                currentPage++;
                init();
            }).position(centerX + 10, listY).size(80, 20).build());
        }

        String pageText = "Page " + (currentPage + 1) + " / " + Math.max(1, totalPages);
        addDrawableChild(ButtonWidget.builder(Text.literal(pageText), b -> {}).position(centerX - 40, listY + 25).size(80, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawCenteredText(context, textRenderer, title, width / 2, height / 4 - 20, 0xFFFFFF);
        context.getMatrices().push();
        context.getMatrices().scale(0.75f, 0.75f, 1f);
        int scaledX = (int)((width / 2) / 0.75f);
        int offsetYvar = 7;
        int scaledY = (int)((height / 4 - 2 - offsetYvar) / 0.75f);
        Text clickable = Text.literal("Developed by DJMahirNationTV")
                .setStyle(Style.EMPTY.withColor(0xAAAAAA).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://djmahirnationtv.com")));
        drawCenteredText(context, textRenderer, clickable, scaledX, scaledY, 0xAAAAAA);
        context.getMatrices().pop();
    }

    private void drawCenteredText(DrawContext context, TextRenderer renderer, Text text, int x, int y, int color) {
        int textWidth = renderer.getWidth(text);
        context.drawText(renderer, text, x - textWidth / 2, y, color, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void saveTriggers() {
        try {
            Files.createDirectories(SAVE_FILE.getParent());
            Map<String, Object[]> data = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : ChatResponder.getTriggers().entrySet()) {
                String key = entry.getKey();
                data.put(key, new Object[]{
                        entry.getValue(),
                        ChatResponder.getTriggerEnabled(key),
                        ChatResponder.getTriggerRegex(key),
                        ChatResponder.getTriggerMentionOnly(key),
                        ChatResponder.getTriggerSound(key),
                        ChatResponder.getTriggerDelays().getOrDefault(key, 0)
                });
            }
            String json = new Gson().toJson(data);
            Files.writeString(SAVE_FILE, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTriggers() {
        if (!Files.exists(SAVE_FILE)) return;
        try {
            String json = Files.readString(SAVE_FILE);
            Type mapType = new TypeToken<Map<String, Object[]>>() {}.getType();
            Map<String, Object[]> parsed = new Gson().fromJson(json, mapType);
            if (parsed != null) {
                for (Map.Entry<String, Object[]> entry : parsed.entrySet()) {
                    String trigger = entry.getKey();
                    Object[] val = entry.getValue();
                    if (val.length < 2) continue;
                    ChatResponder.getTriggers().put(trigger, (String) val[0]);
                    ChatResponder.setTriggerEnabled(trigger, Boolean.parseBoolean(val[1].toString()));
                    boolean regex = val.length >= 3 && Boolean.parseBoolean(val[2].toString());
                    boolean mentionOnly = val.length >= 4 && Boolean.parseBoolean(val[3].toString());
                    boolean sound = val.length >= 5 && Boolean.parseBoolean(val[4].toString());
                    int delay = val.length >= 6 ? (int) Double.parseDouble(val[5].toString()) : 0;
                    ChatResponder.setTriggerRegex(trigger, regex);
                    ChatResponder.setTriggerMentionOnly(trigger, mentionOnly);
                    ChatResponder.setTriggerSound(trigger, sound);
                    ChatResponder.setTriggerDelay(trigger, delay);
                }
            }
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String stripColorCodes(String message) {
        return message.replaceAll("§[0-9a-fk-or]", "");
    }

    public static boolean shouldTriggerRespond(String rawMessage) {
        String cleanMsg = stripColorCodes(rawMessage);
        for (Map.Entry<String, String> entry : ChatResponder.getTriggers().entrySet()) {
            String trigger = entry.getKey();
            boolean enabled = ChatResponder.getTriggerEnabled(trigger);
            boolean regex = ChatResponder.getTriggerRegex(trigger);
            if (!enabled) continue;
            if (regex) {
                if (Pattern.compile(trigger).matcher(cleanMsg).find()) return true;
            } else {
                if (cleanMsg.contains(trigger)) return true;
            }
        }
        return false;
    }
}
