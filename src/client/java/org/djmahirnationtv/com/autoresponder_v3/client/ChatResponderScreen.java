package org.djmahirnationtv.com.autoresponder_v3.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatResponderScreen extends Screen {
    private TextFieldWidget triggerField;
    private TextFieldWidget responseField;
    private ButtonWidget addButton;

    public ChatResponderScreen() {
        super(Text.of("Chat Responder Config"));
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int y = height / 4;

        triggerField = new TextFieldWidget(textRenderer, centerX - 100, y, 200, 20, Text.of("Trigger"));
        responseField = new TextFieldWidget(textRenderer, centerX - 100, y + 30, 200, 20, Text.of("Response"));
        addButton = ButtonWidget.builder(Text.of("Add Trigger"), button -> {
            String trigger = triggerField.getText().toLowerCase();
            String response = responseField.getText();
            if (!trigger.isEmpty() && !response.isEmpty()) {
                ChatResponder.getTriggers().put(trigger, response);
                triggerField.setText("");
                responseField.setText("");
            }
        }).position(centerX - 100, y + 60).size(200, 20).build();

        addDrawableChild(triggerField);
        addDrawableChild(responseField);
        addDrawableChild(addButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        drawCenteredText(context, textRenderer, title, width / 2, 20, 0xFFFFFF);
    }

    private void drawCenteredText(DrawContext context, net.minecraft.client.font.TextRenderer renderer, Text text, int x, int y, int color) {
        int textWidth = renderer.getWidth(text);
        context.drawText(renderer, text, x - textWidth / 2, y, color, false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
