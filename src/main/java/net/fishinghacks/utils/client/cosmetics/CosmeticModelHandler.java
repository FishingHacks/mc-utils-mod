package net.fishinghacks.utils.client.cosmetics;

import com.mojang.authlib.GameProfile;
import net.fishinghacks.utils.common.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CosmeticModelHandler {
    public final UUID id;
    public List<CosmeticModel> models;

    private static final HashMap<UUID, CosmeticModelHandler> instances = new HashMap<>();

    public static CosmeticModelHandler fromProfile(GameProfile profile) {
        CosmeticModelHandler inst = instances.get(profile.getId());
        if (inst != null) return inst;
        return new CosmeticModelHandler(profile);
    }

    public static void removeProfile(UUID uuid) {
        CosmeticModelHandler handler = CosmeticModelHandler.instances.remove(uuid);
        // todo: actually remove textures (don't have to yet cuz its part of the texture pack)
        TextureManager manager = Minecraft.getInstance().getTextureManager();
    }

    public static void removeAllProfiles() {
        CosmeticModelHandler.instances.clear();
    }

    public CosmeticModelHandler(GameProfile profile) {
        this.id = profile.getId();
        instances.put(id, this);

        try {
            var root = CosmeticModelLoader.loadModel(Utils.id("cosmetics/axolotl.jpm"));
            models = root.map(modelPart -> List.of(new CosmeticModel(modelPart, ResourceLocation.withDefaultNamespace("textures/entity/axolotl/axolotl_blue.png"))))
                .orElseGet(List::of);
        } catch (Exception ignored) {
            models = List.of();
        }
    }
}
