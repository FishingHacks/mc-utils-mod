package net.fishinghacks.utils.mixin;

import net.fishinghacks.utils.config.ConfigsImpl;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "reloadResources", at = @At("HEAD"))
    public void reloadResources(Collection<String> ignored1, CallbackInfoReturnable<CompletableFuture<Void>> ignored2) {
        ConfigsImpl.reload();
    }
}
