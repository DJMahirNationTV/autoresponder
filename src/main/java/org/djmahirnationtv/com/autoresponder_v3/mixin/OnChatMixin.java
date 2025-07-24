/*
package org.djmahirnationtv.com.autoresponder_v3.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.message.MessageType;
import net.minecraft.text.Text;
import org.djmahirnationtv.com.autoresponder_v3.ChatResponder;
import org.djmahirnationtv.com.autoresponder_v3.ChatResponderConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class OnChatMixin {
    @Inject(method = "onGameMessage", at = @At("TAIL"))
    private void onGameMessage(Text message, boolean overlay, MessageType.Parameters params, CallbackInfo ci) {
        if (!ChatResponderConfig.enabled) return;
        String msg = message.getString().toLowerCase();
        ChatResponder.onChat(msg);
    }
}
*/