package org.djmahirnationtv.com.autoresponder_v3.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.djmahirnationtv.com.autoresponder_v3.client.ChatResponder;
import org.djmahirnationtv.com.autoresponder_v3.ChatResponderConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class OnChatMixin {
    @Inject(method = "addMessage", at = @At("TAIL"))
    private void onChatAdded(Text message, CallbackInfo ci) {
        if (!ChatResponderConfig.enabled) return;

        String raw = message.getString(); // gets visible text
        ChatResponder.onChat(raw, "UNKNOWN"); // sender name is not available here
    }
}