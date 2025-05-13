package net.fishinghacks.utils.client.cosmetics;

import java.util.UUID;

public class CosmeticHandler {
    public static void reloadCosmetics() {
        CapeHandler.removeAllProfiles();
        CosmeticModelHandler.removeAllProfiles();
    }

    public static void reloadCosmeticsForPlayer(UUID player) {
        CapeHandler.removeProfile(player);
        CosmeticModelHandler.removeProfile(player);
    }
}
