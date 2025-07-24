package org.djmahirnationtv.com.autoresponder_v3.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.client.network.message.MessageHandler;
import org.djmahirnationtv.com.autoresponder_v3.client.ChatResponder;
import org.djmahirnationtv.com.autoresponder_v3.ChatResponderConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class OnChatMixin {
    @Inject(method = "onChatMessage", at = @At("TAIL"))
    private void onGameMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        if (!ChatResponderConfig.enabled) return;
        String msg = message.getContent().getString().toLowerCase();
        ChatResponder.onChat(msg, sender.getName());
    }
}