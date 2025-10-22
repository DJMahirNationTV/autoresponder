package org.djmahirnationtv.com.autoresponder_v3.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.djmahirnationtv.com.autoresponder_v3.client.ChatResponder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class OnChatMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("TAIL"))
    private void onChatAdded(Text message, CallbackInfo ci) {
        String raw = message.getString();
        ChatResponder.onChat(raw, "UNKNOWN");
    }
}