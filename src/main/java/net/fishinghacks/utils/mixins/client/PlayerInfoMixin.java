package net.fishinghacks.utils.mixins.client;

import com.mojang.authlib.GameProfile;
import net.fishinghacks.utils.client.cosmetics.CapeHandler;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "createSkinLookup", at = @At("RETURN"), cancellable = true)
    private static void modifySkinLookup(GameProfile profile, CallbackInfoReturnable<Supplier<PlayerSkin>> ci) {
        var skinSupplier = ci.getReturnValue();
        ci.setReturnValue(() -> CapeHandler.fromProfile(profile).getSkin(skinSupplier.get()));
    }
}