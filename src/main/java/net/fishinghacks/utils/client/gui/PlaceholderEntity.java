package net.fishinghacks.utils.client.gui;

import com.mojang.authlib.GameProfile;
import net.fishinghacks.utils.client.cosmetics.CapeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class PlaceholderEntity {
    public final GameProfile gameProfile;
    public PlayerSkin skin;
    public boolean slim;

    public final boolean showBody = true;
    public boolean showElytra = false;
    public final float yaw = 0f;
    public double x = 0.0;
    @Nullable
    private final Consumer<Boolean> onSlimChange;

    public PlaceholderEntity(GameProfile gameProfile, @Nullable Consumer<Boolean> onSlimChange) {
        this.gameProfile = gameProfile;
        this.onSlimChange = onSlimChange;

        skin = DefaultPlayerSkin.get(gameProfile);
        slim = skin.model() == PlayerSkin.Model.SLIM;
        Minecraft.getInstance().getSkinManager().getOrLoad(gameProfile).thenAccept(skin -> {
            if (skin.isEmpty()) return;
            this.skin = skin.get();
            this.slim = this.skin.model() == PlayerSkin.Model.SLIM;
            if(this.onSlimChange != null) this.onSlimChange.accept(this.slim);
        });
    }

    public ResourceLocation getSkinTexture() {
        return skin.texture();
    }

    @Nullable
    public ResourceLocation getCapeTexture() {
        var capeTexture = CapeHandler.fromProfile(gameProfile).getCape();
        return capeTexture != null ? capeTexture : skin.capeTexture();
    }

    public ResourceLocation getElytraTexture() {
        var capeTexture = CapeHandler.fromProfile(gameProfile).getCape();
        return Optional.ofNullable(capeTexture != null ? capeTexture : skin.elytraTexture())
            .orElse(ResourceLocation.withDefaultNamespace("textures/entity/equipment/wings/elytra.png"));
    }
}
