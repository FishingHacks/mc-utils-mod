package net.fishinghacks.utils.mixin;

import net.fishinghacks.utils.commands.CommandManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "sendChat", cancellable = true, at = @At("HEAD"))
    public void sendChat(String message, CallbackInfo ci) {
        if (CommandManager.onChat(message)) ci.cancel();
    }
}
