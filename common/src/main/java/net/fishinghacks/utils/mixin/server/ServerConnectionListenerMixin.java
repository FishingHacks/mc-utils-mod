package net.fishinghacks.utils.mixin.server;

import net.fishinghacks.utils.server.Server;
import net.minecraft.server.network.ServerConnectionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConnectionListener.class)
public class ServerConnectionListenerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ignored) {
        if (Server.getInstance() != null) Server.getInstance().tick();
    }
}
