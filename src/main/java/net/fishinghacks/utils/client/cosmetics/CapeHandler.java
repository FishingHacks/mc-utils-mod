package net.fishinghacks.utils.client.cosmetics;

import java.util.HashMap;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fishinghacks.utils.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class CapeHandler {
    final UUID uuid;
    int lastFrame = 0;
    int maxFrames = 0;
    long lastFrameTime = 0;
    boolean hasCape = false;
    boolean isAnimated = false;
    @Nullable
    public String serviceProviderCapeId = null;
    public boolean isServiceProviderCape = false;

    private static final HashMap<UUID, CapeHandler> instances = new HashMap<>();

    public static @Nullable CapeHandler fromId(UUID id) {
        return instances.get(id);
    }

    public static CapeHandler fromProfile(GameProfile profile) {
        CapeHandler inst = instances.get(profile.getId());
        if (inst != null) return inst;
        return new CapeHandler(profile);
    }

    static void removeProfile(UUID uuid) {
        CapeHandler handler = CapeHandler.instances.remove(uuid);
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        if (handler.isAnimated) {
            for (int i = 0; i < handler.maxFrames; ++i)
                manager.release(Utils.id("capes/" + uuid + "/" + i));
        } else {
            manager.release(Utils.id("capes/" + uuid));
        }
    }

    static void removeAllProfiles() {
        CapeHandler.instances.clear();
    }

    CapeHandler(GameProfile profile) {
        this.uuid = profile.getId();
        instances.put(this.uuid, this);

        tryGetForProvider(profile, CapeProvider.first);
    }

    private void tryGetForProvider(GameProfile profile, @Nullable CapeProvider provider) {
        if (provider == null) return;
        provider.get(profile).exceptionally(ignored -> {
            tryGetForProvider(profile, provider.next());
            return null;
        }).thenAccept(image -> {
            if (image == null || (image.getWidth() == 0 && image.getHeight() == 0)) {
                if (image != null) image.close();
                tryGetForProvider(profile, provider.next());
                return;
            }
            boolean animated = image.getHeight() > image.getWidth() / 2 * 3;
            Minecraft.getInstance().schedule(() -> setCape(image, animated));
        });
    }

    public PlayerSkin getSkin(PlayerSkin skin) {
        if (!hasCape) return skin;
        ResourceLocation cape = getCape();
        if (cape == null) return skin;
        return new PlayerSkin(skin.texture(), skin.textureUrl(), cape, cape, skin.model(), skin.secure());
    }

    @Nullable
    public ResourceLocation getCape() {
        if (!hasCape) return null;
        if (!this.isAnimated) return Utils.id("capes/" + this.uuid);
        final long time = System.currentTimeMillis();
        if (time > this.lastFrameTime + 100L) {
            this.lastFrame = (this.lastFrame + 1) % this.maxFrames;
            this.lastFrameTime = time;
        }
        return Utils.id("capes/" + this.uuid + "/" + this.lastFrame);
    }

    private void setCape(NativeImage img, boolean isAnimated) {
        if (isAnimated) {
            Int2ObjectOpenHashMap<NativeImage> cape = parseAnimatedCape(img);
            TextureManager manager = Minecraft.getInstance().getTextureManager();
            cape.forEach((frame, texture) -> manager.register(Utils.id(this.nameForFrame(frame)),
                new DynamicTexture(() -> this.nameForFrame(frame), texture)));
            this.maxFrames = cape.size();
            this.hasCape = true;
            this.isAnimated = true;
        } else {
            NativeImage cape = parseCape(img);
            this.hasCape = true;
            Minecraft.getInstance().getTextureManager()
                .register(Utils.id("capes/" + this.uuid), new DynamicTexture(() -> "capes/" + this.uuid, cape));
        }
    }

    private NativeImage parseCape(NativeImage img) {
        int imgWidth = 64;
        int imgHeight = 32;
        int srcWidth = img.getWidth();
        int srcHeight = img.getHeight();

        while (imgWidth < srcWidth || imgHeight < srcHeight) {
            imgWidth *= 2;
            imgHeight *= 2;
        }

        NativeImage newImg = new NativeImage(imgWidth, imgHeight, true);
        img.copyRect(newImg, 0, 0, 0, 0, srcWidth, srcHeight, false, false);

        return newImg;
    }

    private Int2ObjectOpenHashMap<NativeImage> parseAnimatedCape(NativeImage img) {
        Int2ObjectOpenHashMap<NativeImage> cape = new Int2ObjectOpenHashMap<>();

        int totalFrames = img.getHeight() / (img.getWidth() / 2);
        for (int currentFrame = 0; currentFrame < totalFrames; ++currentFrame) {
            NativeImage frame = new NativeImage(img.getWidth(), img.getWidth() / 2, true);
            img.copyRect(frame, 0, currentFrame * img.getWidth() / 2, 0, 0, img.getWidth(), img.getWidth() / 2, false,
                false);
            cape.put(currentFrame, frame);
        }

        return cape;
    }

    private String nameForFrame(int frame) {
        return "capes/" + this.uuid + "/" + frame;
    }
}
