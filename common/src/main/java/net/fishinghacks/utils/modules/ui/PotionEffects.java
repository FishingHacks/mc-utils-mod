package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.config.spec.AbstractConfig;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import org.joml.Vector2i;

@Module(name = "potion_effects", category = ModuleCategory.UI)
public class PotionEffects extends RenderableModule {
    public static final int WIDTH = 150;
    public static final int SINGLE_LINE_HEIGHT = 22;
    public static PotionEffects instance;

    private CachedValue<Boolean> showIcon;
    private CachedValue<Boolean> durationNewLine;
    private CachedValue<Boolean> displayDuration;
    public CachedValue<Boolean> displayVanilla;
    private final MobEffectInstance dummyEffect = new MobEffectInstance(MobEffects.STRENGTH, 2000);

    public PotionEffects() {
        instance = this;
    }

    @Override
    public void buildConfig(AbstractConfig cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);

        showIcon = CachedValue.wrap(cfg, builder, "show_icon", true);
        durationNewLine = CachedValue.wrap(cfg, builder, "duration_newline", true);
        displayDuration = CachedValue.wrap(cfg, builder, "show_duration", true);
        displayVanilla = CachedValue.wrap(cfg, builder, "show_vanilla_effects", false);
    }

    private void render(GuiGraphics guiGraphics, int x, int y, MobEffectInstance effect,
                        MobEffectTextureManager textureManager) {
        guiGraphics.fill(x, y, x + WIDTH, y + SINGLE_LINE_HEIGHT, Colors.BLACK.withAlpha(0x7f));
        int textX = x + 3;
        if (showIcon.get()) {
            var sprite = textureManager.get(effect.getEffect());
            guiGraphics.blitSprite(RenderType::guiTextured, sprite, x + 3, y + 2, 18, 18);
            textX += 22;
        }
        Component duration = MobEffectUtil.formatDuration(effect, 1.0F,
            Minecraft.getInstance().level == null ? 20 : Minecraft.getInstance().level.tickRateManager().tickrate());
        if (durationNewLine.get() && displayDuration.get()) {
            int textY = y + (SINGLE_LINE_HEIGHT - 9 * 2) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, getEffectName(effect), textX, textY,
                Colors.WHITE.get());
            textY += 9;
            guiGraphics.drawString(Minecraft.getInstance().font, duration.copy().withStyle(ChatFormatting.DARK_GRAY),
                textX, textY, Colors.WHITE.get());
        } else {
            var text = getEffectName(effect);
            if (displayDuration.get()) text.append(" - ").append(duration);
            int textY = y + (SINGLE_LINE_HEIGHT - Minecraft.getInstance().font.lineHeight) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, text, textX, textY, Colors.WHITE.get());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        assert Minecraft.getInstance().player != null;
        var effects = Minecraft.getInstance().player.getActiveEffects();
        Vector2i pos = getPosition(WIDTH, SINGLE_LINE_HEIGHT * effects.size());
        var textureManager = Minecraft.getInstance().getMobEffectTextures();
        for (var effect : effects) {
            render(guiGraphics, pos.x, pos.y, effect, textureManager);
            pos.y += SINGLE_LINE_HEIGHT;
        }
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, float partialTick) {
        render(guiGraphics, x, y, dummyEffect, Minecraft.getInstance().getMobEffectTextures());
    }

    @Override
    public Vector2i previewSize() {
        return new Vector2i(WIDTH, SINGLE_LINE_HEIGHT);
    }

    private MutableComponent getEffectName(MobEffectInstance effect) {
        MutableComponent mutablecomponent = effect.getEffect().value().getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            mutablecomponent.append(CommonComponents.SPACE)
                .append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
        }

        return mutablecomponent;
    }
}
