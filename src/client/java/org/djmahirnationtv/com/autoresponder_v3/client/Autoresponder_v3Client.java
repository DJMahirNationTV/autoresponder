package org.djmahirnationtv.com.autoresponder_v3.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.djmahirnationtv.com.autoresponder_v3.ChatResponderConfig;
import org.lwjgl.glfw.GLFW;

public class Autoresponder_v3Client implements ClientModInitializer {
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoresponder.toggle", GLFW.GLFW_KEY_R, "category.autoresponder"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ChatResponderScreen());
                }
            }
        });
    }
}