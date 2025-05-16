package net.fishinghacks.utils.cosmetics;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.caching.DownloadTextureCache;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.connection.packets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CosmeticModelHandler {
    public final UUID id;
    public final ArrayList<CosmeticModel> models;
    private boolean isClosed = false;

    private static final HashMap<UUID, CosmeticModelHandler> instances = new HashMap<>();

    public static CosmeticModelHandler fromProfile(GameProfile profile) {
        CosmeticModelHandler inst = instances.get(profile.getId());
        if (inst != null) return inst;
        return new CosmeticModelHandler(profile);
    }

    static void removeProfile(UUID uuid) {
        CosmeticModelHandler handler = CosmeticModelHandler.instances.remove(uuid);
        handler.isClosed = true;
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        handler.models.forEach(model -> manager.release(model.texture()));
    }

    static void removeAllProfiles() {
        CosmeticModelHandler.instances.clear();
    }

    public CosmeticModelHandler(GameProfile profile) {
        this.id = profile.getId();
        instances.put(id, this);
        models = new ArrayList<>();
        var conn = ClientConnectionHandler.getInstance().getConnection();
        if (conn == null || !ClientConnectionHandler.getInstance().isConnected()) return;
        ClientConnectionHandler.getInstance().waitForPacket(Packets.GET_PLAYER_COSMETIC_REPLY, cosmetics -> {
            if (cosmetics.isEmpty()) return;
            cosmetics.get().models().forEach(name -> {
                ClientConnectionHandler.getInstance().waitForPacket(Packets.COSMETIC_REPLY, v -> {
                    if (v.isEmpty()) return;
                    onCosmeticDataReceived(name, Base64.decodeBase64(v.get().b64Data()));
                }, v -> v.cosmeticType() == CosmeticType.ModelData && name.equals(v.name()));
                conn.send(new CosmeticRequestPacket(CosmeticType.ModelData, name));
            });
        });
        conn.send(new GetCosmeticForPlayer(profile.getId()));
    }

    private void onCosmeticDataReceived(String name, byte[] data) {
        try {
            var model = CosmeticModelLoader.loadModel(data);
            DownloadTextureCache.serviceServerModels.getOrLoad(name).thenAccept(image -> {
                var location = Constants.id("models_textures/" + id + "/" + Hashing.sha256().hashBytes(data));
                Minecraft.getInstance().schedule(() -> {
                    if (isClosed) return;
                    NativeImage newImage = new NativeImage(image.getWidth(), image.getHeight(), true);
                    newImage.copyFrom(image);
                    Minecraft.getInstance().getTextureManager()
                        .register(location, new DynamicTexture(location::toString, newImage));
                    models.add(new CosmeticModel(model, location, name));
                });
            });

        } catch (Exception e) {
            Constants.LOG.info("Failed to load model {}", name, e);
        }
    }
}
