package org.djmahirnationtv.com.autoresponder_v3.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatResponderScreen extends Screen {
    private TextFieldWidget triggerField;
    private TextFieldWidget responseField;
    private ButtonWidget addButton;
    private ButtonWidget regexCheckbox;
    private boolean regexChecked = false;
    private static final Map<String, Boolean> triggerEnabledMap = new ConcurrentHashMap<>();
    private int scrollOffset = 0;
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
        triggerField.setPlaceholder(Text.of("Enter a word..."));

        responseField = new TextFieldWidget(textRenderer, centerX - 90, y + 25, 180, 20, Text.of(""));
        responseField.setEditable(true);
        responseField.setPlaceholder(Text.of("Enter a response..."));

        regexCheckbox = ButtonWidget.builder(Text.of((regexChecked ? "☑ " : "☐ ") + "Enable Regex"), btn -> {
            regexChecked = !regexChecked;
            init();
        }).position(centerX - 90, y + 50).size(180, 20).build();

        addButton = ButtonWidget.builder(Text.of("+ Add Trigger"), button -> {
            String trigger = triggerField.getText().trim();
            String response = responseField.getText().trim();
            if (!trigger.isEmpty() && !response.isEmpty()) {
                ChatResponder.getTriggers().put(trigger, response);
                triggerEnabledMap.put(trigger, true);
                triggerField.setText("");
                responseField.setText("");
                regexChecked = false;
                saveTriggers();
                init();
            }
        }).position(centerX - 90, y + 75).size(180, 20).build();

        addDrawableChild(triggerField);
        addDrawableChild(responseField);
        addDrawableChild(regexCheckbox);
        addDrawableChild(addButton);

        int listY = y + 110;
        int index = 0;
        for (Map.Entry<String, String> entry : ChatResponder.getTriggers().entrySet()) {
            if (index++ < scrollOffset) continue;
            if (index > scrollOffset + MAX_VISIBLE) break;

            String trigger = entry.getKey();
            String response = entry.getValue();
            boolean enabled = triggerEnabledMap.getOrDefault(trigger, true);
            String text = trigger + " -> " + response + (enabled ? "" : " (Disabled)");

            final ButtonWidget triggerButton = ButtonWidget.builder(Text.literal(text), btn -> {})
                    .position(centerX - 90, listY).size(180, 20).build();
            triggerButton.active = false;

            final ButtonWidget deleteButton = ButtonWidget.builder(Text.of("✕"), btn -> {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed) {
                        ChatResponder.getTriggers().remove(trigger);
                        triggerEnabledMap.remove(trigger);
                        saveTriggers();
                    }
                    MinecraftClient.getInstance().setScreen(this);
                    init();
                }, Text.of("Are you sure?"), Text.of("Do you really want to delete this trigger?")));
            }).position(centerX + 95, listY).size(20, 20).build();

            final ButtonWidget disableButton = ButtonWidget.builder(Text.of(enabled ? "❚❚" : "▶"), btn -> {
                triggerEnabledMap.put(trigger, !enabled);
                saveTriggers();
                init();
            }).position(centerX + 120, listY).size(20, 20).build();

            addDrawableChild(triggerButton);
            addDrawableChild(deleteButton);
            addDrawableChild(disableButton);

            listY += 25;
        }

        if (scrollOffset > 0) {
            addDrawableChild(ButtonWidget.builder(Text.of("↑"), b -> {
                scrollOffset = Math.max(0, scrollOffset - 1);
                init();
            }).position(centerX - 120, y + 110).size(20, 20).build());
        }

        if (ChatResponder.getTriggers().size() > scrollOffset + MAX_VISIBLE) {
            addDrawableChild(ButtonWidget.builder(Text.of("↓"), b -> {
                scrollOffset++;
                init();
            }).position(centerX + 170, y + 110 + MAX_VISIBLE * 25).size(20, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawCenteredText(context, textRenderer, title, width / 2, height / 4 - 20, 0xFFFFFF);
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
            Map<String, Object> data = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : ChatResponder.getTriggers().entrySet()) {
                String key = entry.getKey();
                data.put(key, new String[]{entry.getValue(), triggerEnabledMap.getOrDefault(key, true).toString()});
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
            Type type = new TypeToken<Map<String, String[]>>() {}.getType();
            Map<String, String[]> data = new Gson().fromJson(json, type);
            for (Map.Entry<String, String[]> entry : data.entrySet()) {
                String trigger = entry.getKey();
                String[] val = entry.getValue();
                ChatResponder.getTriggers().put(trigger, val[0]);
                triggerEnabledMap.put(trigger, Boolean.parseBoolean(val[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
