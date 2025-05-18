package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableTextModule;
import net.fishinghacks.utils.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PositionModule extends RenderableTextModule {
    private CachedValue<Boolean> showDirection;
    private CachedValue<Boolean> showBiome;

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);
        showDirection = CachedValue.wrap(cfg, builder, "show_direction", false);
        showBiome = CachedValue.wrap(cfg, builder, "show_biome", false);
    }

    private Component fromDirection(double rotY) {
        rotY = Mth.wrapDegrees(rotY);
        var directionTranslation = switch (Direction.fromYRot(rotY)) {
            case NORTH -> Translation.North.get();
            case SOUTH -> Translation.South.get();
            case WEST -> Translation.West.get();
            case EAST -> Translation.East.get();
            default -> throw new IllegalStateException("unreachable");
        };
        boolean isXIncreasing = rotY >= -180.0 && rotY <= 0.0;
        boolean isZIncreasing = rotY >= -90.0 && rotY <= 90.0;
        return Component.empty().withStyle(ChatFormatting.AQUA)
            .append(Translation.Facing.get().copy().withStyle(ChatFormatting.GRAY)).append(directionTranslation)
            .append(" (" + (isXIncreasing ? "+" : "-") + (isZIncreasing ? "+" : "-") + ")");
    }

    @Override
    public List<Component> getText() {
        assert Minecraft.getInstance().player != null;
        var player = Minecraft.getInstance().player;
        Vec3 pos = player.position();
        String posText = (int) pos.x + " " + (int) pos.y + " " + (int) pos.z;
        List<Component> entries = new ArrayList<>();
        entries.add(Component.literal("XYZ: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(posText).withStyle(ChatFormatting.AQUA)));
        if (showDirection.get()) entries.add(fromDirection(player.getYRot()));
        if (showBiome.get()) {
            @SuppressWarnings("resource") var biome = player.level().getBiome(player.blockPosition()).unwrapKey();
            if (biome.isEmpty()) return entries;
            var location = biome.get().location();
            entries.add(Component.translatableWithFallback("biome." + location.toLanguageKey(), location.toString())
                .withStyle(ChatFormatting.AQUA));
        }
        return entries;
    }

    @Override
    public List<Component> getPreviewText() {
        List<Component> entries = new ArrayList<>();
        entries.add(Component.literal("XYZ: ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("0 0 0").withStyle(ChatFormatting.AQUA)));
        if (showDirection.get())
            entries.add(Translation.Facing.get().copy().append(Translation.West.get()).append("(--)"));
        if (showBiome.get()) entries.add(Component.translatable("biome.minecraft.beach"));
        return entries;
    }

    @Override
    public String name() {
        return "position";
    }

    @Override
    public ModuleCategory category() {
        return ModuleCategory.UI;
    }
}
