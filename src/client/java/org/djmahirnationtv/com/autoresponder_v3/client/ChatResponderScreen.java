package org.djmahirnationtv.com.autoresponder_v3.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatResponderScreen extends Screen {
    private TextFieldWidget triggerField;
    private TextFieldWidget responseField;
    private ButtonWidget addButton;
    private ClickableWidget regexCheckbox;
    private boolean regexChecked = false;
    private static final Map<String, Boolean> triggerEnabledMap = new ConcurrentHashMap<>();
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 6;

    public ChatResponderScreen() {
        super(Text.of("Chat Responder Config"));
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
        responseField.setPlaceholder(Text.of("Enter the response..."));

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

            final ButtonWidget[] triggerButton = new ButtonWidget[1];
            final ButtonWidget[] deleteButton = new ButtonWidget[1];
            final ButtonWidget[] disableButton = new ButtonWidget[1];
            final ButtonWidget[] editButton = new ButtonWidget[1];

            triggerButton[0] = ButtonWidget.builder(Text.literal(text), btn -> {}).position(centerX - 90, listY).size(180, 20).build();
            triggerButton[0].active = false;

            deleteButton[0] = ButtonWidget.builder(Text.of("✕"), btn -> {
                MinecraftClient.getInstance().setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed) {
                        ChatResponder.getTriggers().remove(trigger);
                        triggerEnabledMap.remove(trigger);
                    }
                    MinecraftClient.getInstance().setScreen(this);
                    init();
                }, Text.of("Are you sure?"), Text.of("Do you really want to delete this trigger?")));
            }).position(centerX + 95, listY).size(20, 20).build();

            disableButton[0] = ButtonWidget.builder(Text.of(enabled ? "❚❚" : "▶"), btn -> {
                triggerEnabledMap.put(trigger, !enabled);
                init();
            }).position(centerX + 120, listY).size(20, 20).build();

            editButton[0] = ButtonWidget.builder(Text.of("✎"), btn -> {
                triggerField.setText(trigger);
                responseField.setText(response);
                ChatResponder.getTriggers().remove(trigger);
                init();
            }).position(centerX + 145, listY).size(20, 20).build();

            addDrawableChild(triggerButton[0]);
            addDrawableChild(deleteButton[0]);
            addDrawableChild(disableButton[0]);
            addDrawableChild(editButton[0]);

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
        renderBackground(context, mouseX, mouseY, delta);
        int panelWidth = 300;
        int panelHeight = 300;
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;
        context.fill(x, y, x + panelWidth, y + panelHeight, 0xCC000000);
        super.render(context, mouseX, mouseY, delta);
        drawCenteredText(context, textRenderer, title, width / 2, y + 8, 0xFFFFFF);
    }

    private void drawCenteredText(DrawContext context, TextRenderer renderer, Text text, int x, int y, int color) {
        int textWidth = renderer.getWidth(text);
        context.drawText(renderer, text, x - textWidth / 2, y, color, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
