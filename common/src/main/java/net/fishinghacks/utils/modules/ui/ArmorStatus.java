package net.fishinghacks.utils.modules.ui;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.TranslatableEnum;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.values.CachedValue;
import net.fishinghacks.utils.modules.Module;
import net.fishinghacks.utils.modules.ModuleCategory;
import net.fishinghacks.utils.modules.RenderableModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

@Module(name = "armor_status", category = ModuleCategory.UI)
public class ArmorStatus extends RenderableModule {
    private final ItemStack previewHead = new ItemStack(Items.DIAMOND_HELMET, 1);
    private final ItemStack previewBody = new ItemStack(Items.DIAMOND_CHESTPLATE, 1);
    private final ItemStack previewLegs = new ItemStack(Items.DIAMOND_LEGGINGS, 1);
    private final ItemStack previewFoot = new ItemStack(Items.DIAMOND_BOOTS, 1);
    private final ItemStack previewMainhand = new ItemStack(Items.DIAMOND_SWORD, 1);
    private final ItemStack previewOffhand = new ItemStack(Items.SHIELD, 1);

    private CachedValue<Boolean> vertical;
    private CachedValue<Boolean> mainhand;
    private CachedValue<Boolean> offhand;
    private CachedValue<Boolean> showAmount;
    private CachedValue<DurabilityDisplay> durabilityDisplay;

    @Override
    public void buildConfig(Config cfg, ConfigBuilder builder) {
        super.buildConfig(cfg, builder);

        vertical = CachedValue.wrap(cfg, builder, "vertical", false);
        mainhand = CachedValue.wrap(cfg, builder, "mainhand", false);
        offhand = CachedValue.wrap(cfg, builder, "offhand", false);
        showAmount = CachedValue.wrap(cfg, builder, "display_amount", true);
        durabilityDisplay = CachedValue.wrapEnum(cfg, builder, "durability_display_type", DurabilityDisplay.Bar);
    }

    private void renderItem(GuiGraphics graphics, Vector2i pos, ItemStack item) {
        graphics.renderItem(item, pos.x + 2, pos.y + 2);

        graphics.pose().pushPose();
        var font = Minecraft.getInstance().font;
        var display = durabilityDisplay.get();
        var text = display == DurabilityDisplay.Text || display == DurabilityDisplay.Both;
        var bar = display == DurabilityDisplay.Bar || display == DurabilityDisplay.Both;
        if (bar && item.isBarVisible()) {
            graphics.fill(pos.x + 4, pos.y + 15, pos.x + 17, pos.y + 17, 200, Colors.BLACK.get());
            int width = item.getBarWidth();
            graphics.fill(pos.x + 4, pos.y + 15, pos.x + 4 + width, pos.y + 16, 200, ARGB.opaque(item.getBarColor()));
        }

        if (text && item.isDamageableItem()) {
            String percentageDurability =
                (int) (100f - ((float) item.getDamageValue() / item.getMaxDamage()) * 100f) + "%";
            int x = pos.x + (bar ? 5 : 2);
            int y = pos.y + (bar ? 11 : 13);
            graphics.pose().pushPose();
            graphics.pose().scale(.5f, .5f, 1f);
            graphics.pose().translate(0f, 0f, 200f);
            graphics.drawString(font, percentageDurability, x * 2, y * 2, ARGB.opaque(item.getBarColor()));
            graphics.pose().popPose();
        }

        if (showAmount.get() && item.getCount() != 1) {
            String s = String.valueOf(item.getCount());
            graphics.pose().translate(0f, 0f, 200f);
            graphics.drawString(Minecraft.getInstance().font, s, pos.x + 18 - font.width(s), pos.y + 10,
                Colors.WHITE.get());
        }
        graphics.pose().popPose();
    }

    private void render(GuiGraphics graphics, ItemStack head, ItemStack body, ItemStack legs, ItemStack feet,
                        ItemStack mainHand, ItemStack offHand) {
        int itemCount = 4;
        if (this.mainhand.get()) itemCount++;
        if (this.offhand.get()) itemCount++;
        var vertical = this.vertical.get();
        Vector2i size = vertical ? new Vector2i(20, itemCount * 20) : new Vector2i(itemCount * 20, 20);
        Vector2i pos = getPosition(size.x, size.y);
        graphics.fill(pos.x, pos.y, pos.x + size.x, pos.y + size.y, Colors.BLACK.withAlpha(0x7f));
        if (!head.isEmpty()) renderItem(graphics, pos, head);
        if (vertical) pos.y += 20;
        else pos.x += 20;
        if (!body.isEmpty()) renderItem(graphics, pos, body);
        if (vertical) pos.y += 20;
        else pos.x += 20;
        if (!legs.isEmpty()) renderItem(graphics, pos, legs);
        if (vertical) pos.y += 20;
        else pos.x += 20;
        if (!feet.isEmpty()) renderItem(graphics, pos, feet);
        if (vertical) pos.y += 20;
        else pos.x += 20;
        if (!mainHand.isEmpty()) renderItem(graphics, pos, mainHand);
        if (vertical) pos.y += 20;
        else pos.x += 20;
        if (!offHand.isEmpty()) renderItem(graphics, pos, offHand);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        render(guiGraphics, player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST),
            player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET),
            mainhand.get() ? player.getMainHandItem() : ItemStack.EMPTY,
            offhand.get() ? player.getOffhandItem() : ItemStack.EMPTY);
    }

    @Override
    public void renderPreview(GuiGraphics guiGraphics, float partialTick) {
        render(guiGraphics, previewHead, previewBody, previewLegs, previewFoot,
            mainhand.get() ? previewMainhand : ItemStack.EMPTY, offhand.get() ? previewOffhand : ItemStack.EMPTY);
    }

    @Override
    public Vector2i previewSize() {
        int itemCount = 4;
        if (mainhand.get()) itemCount++;
        if (offhand.get()) itemCount++;
        return vertical.get() ? new Vector2i(20, itemCount * 20) : new Vector2i(itemCount * 20, 20);
    }

    public enum DurabilityDisplay implements TranslatableEnum {
        Bar, Text, Both, None;

        @Override
        public @NotNull Component getTranslatedName() {
            return switch (this) {
                case Bar -> Translation.DurabilityDisplayTypeBar.get();
                case Text -> Translation.DurabilityDisplayTypeText.get();
                case Both -> Translation.DurabilityDisplayTypeBoth.get();
                case None -> Translation.DurabilityDisplayTypeNone.get();
            };
        }
    }
}
