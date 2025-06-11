package net.fishinghacks.utils.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DragUI extends Screen {
    private static final ResourceLocation CROSSHAIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair");
    private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace(
        "hud/hotbar_selection");
    private static final ResourceLocation HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/half");
    private static final ResourceLocation HEART_CONTAINER = ResourceLocation.withDefaultNamespace(
        "hud/heart/container");
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace(
        "hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace(
        "hud/experience_bar_progress");
    private static final Random random = new Random();
    private static final List<Item> SWORDS = List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
        Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
    private static final List<Item> AXES = List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE,
        Items.DIAMOND_AXE, Items.NETHERITE_AXE);
    private static final List<Item> PICKAXES = List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
        Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
    private static final List<Item> MISC_ITEMS = List.of(Items.COMPASS, Items.CLOCK, Items.RECOVERY_COMPASS,
        Items.FILLED_MAP);
    private static final List<Item> FOURTH_SLOT_ITEMS = List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL,
        Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL, Items.WOODEN_HOE,
        Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE, Items.TORCH,
        Items.BOW, Items.WHEAT_SEEDS, Items.SOUL_TORCH, Items.WATER_BUCKET);

    @Nullable
    private final Screen lastScreen;
    private final Screen clickUi;
    private List<ItemStack> items;
    private int health;
    private int xpLevel;
    private float xpProgress;

    public DragUI(@Nullable Screen lastScreen, @Nullable Screen clickUi) {
        super(Translation.ClickUITitle.get());
        this.lastScreen = lastScreen;
        this.clickUi = clickUi == null ? new ClickUi(lastScreen, this) : clickUi;
    }

    private Item randomItem(List<Item> items) {
        return items.get(random.nextInt(0, items.size()));
    }

    private ItemStack randomFood() {
        var items = BuiltInRegistries.ITEM.stream().map(Item::getDefaultInstance)
            .filter(v -> v.has(DataComponents.FOOD)).toList();
        var itemStack = items.get(random.nextInt(0, items.size()));
        itemStack.setCount(random.nextInt(1, 65));
        return itemStack;
    }

    private ItemStack randomBlock() {
        var blockItems = BuiltInRegistries.ITEM.stream().filter(v -> v instanceof BlockItem).toList();
        return new ItemStack(blockItems.get(random.nextInt(0, blockItems.size())), random.nextInt(1, 65));
    }

    @Override
    protected void init() {
        super.init();

        var sword = randomItem(SWORDS).getDefaultInstance();
        if (random.nextBoolean()) sword.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        sword.setDamageValue(random.nextInt(0, sword.getMaxDamage() + 1));
        var pickaxe = randomItem(PICKAXES).getDefaultInstance();
        if (random.nextBoolean()) pickaxe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        pickaxe.setDamageValue(random.nextInt(0, pickaxe.getMaxDamage() + 1));
        var axe = randomItem(AXES).getDefaultInstance();
        if (random.nextBoolean()) axe.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        axe.setDamageValue(random.nextInt(0, axe.getMaxDamage() + 1));
        var item = randomItem(FOURTH_SLOT_ITEMS);
        var fourthSlotItem = item.getDefaultInstance();
        fourthSlotItem.setCount(random.nextInt(1, fourthSlotItem.getMaxStackSize() + 1));
        if (fourthSlotItem.getMaxStackSize() == 1 && !fourthSlotItem.is(Items.WATER_BUCKET) && random.nextBoolean())
            fourthSlotItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        if (fourthSlotItem.is(Items.WATER_BUCKET) && random.nextInt(0, 5) == 0)
            fourthSlotItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        if (random.nextInt(0, 2000) == 0) {
            fourthSlotItem = new ItemStack(Items.REDSTONE, 420);
            fourthSlotItem.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        var misc = randomItem(MISC_ITEMS).getDefaultInstance();
        if (misc.is(Items.COMPASS) && random.nextBoolean()) misc.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        var food = randomFood();
        var blocks = randomBlock();

        items = List.of(sword, pickaxe, axe, fourthSlotItem, blocks, ItemStack.EMPTY, ItemStack.EMPTY, food, misc);
        health = random.nextInt(0, 21);
        xpLevel = random.nextInt(0, 101);
        xpProgress = random.nextFloat();


        ModuleManager.modules.forEach((name, module) -> {
            if (module instanceof RenderableModule renderableModule)
                this.addRenderableWidget(new RenderableModulePreview(renderableModule, width, height));
        });
        addRenderableWidget(
            Button.builder(Translation.ClickUITitle.get(), button -> Minecraft.getInstance().setScreen(clickUi))
                .pos((width - 50) / 2, 0).size(50, 20).build());
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        assert minecraft != null;
        if (minecraft.level == null) {
            CUBE_MAP.render(minecraft, 10f, 0f, 1f);
            renderCrosshair(guiGraphics);
            renderHotbar(guiGraphics);
            renderHearts(guiGraphics);
            renderExperienceBar(guiGraphics);
            renderExperienceLevel(guiGraphics);

            guiGraphics.flush();
            RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(
                Objects.requireNonNull(Minecraft.getInstance().getMainRenderTarget().getDepthTexture()), 1.0);
            renderTransparentBackground(guiGraphics);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (getFocused() instanceof RenderableModulePreview p) p.renderOutline(guiGraphics);
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(lastScreen);
    }

    // ┌─────────────────┐
    // │ Fake Gui Render │
    // └─────────────────┘
    private void renderCrosshair(GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SPRITE, (guiGraphics.guiWidth() - 15) / 2,
            (guiGraphics.guiHeight() - 15) / 2, 15, 15);
    }

    private void renderHotbar(GuiGraphics guiGraphics) {
        // items
        int centerX = guiGraphics.guiWidth() / 2;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
        guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SPRITE, centerX - 91, guiGraphics.guiHeight() - 22, 182,
            22);
        guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SELECTION_SPRITE, centerX - 91 - 1 + 3 * 20,
            guiGraphics.guiHeight() - 22 - 1, 24, 23);

        guiGraphics.pose().popPose();
        int seed = 1;

        for (int itemSlot = 0; itemSlot < 9; ++itemSlot) {
            int itemX = centerX - 90 + itemSlot * 20 + 2;
            int itemY = guiGraphics.guiHeight() - 16 - 3;
            this.renderSlot(guiGraphics, itemX, itemY, items.get(itemSlot), seed++);
        }
    }

    private void renderExperienceBar(GuiGraphics guiGraphics) {
        int xpTextureWidth = 182;
        int xpProgressWidth = (int) (xpProgress * 183.0F);
        int x = guiGraphics.guiWidth() / 2 - 91;
        int y = guiGraphics.guiHeight() - 32 + 3;
        guiGraphics.blitSprite(RenderType::guiTextured, EXPERIENCE_BAR_BACKGROUND_SPRITE, x, y, xpTextureWidth, 5);
        if (xpProgressWidth > 0) {
            guiGraphics.blitSprite(RenderType::guiTextured, EXPERIENCE_BAR_PROGRESS_SPRITE, xpTextureWidth, 5, 0, 0, x,
                y, xpProgressWidth, 5);
        }
    }

    private void renderExperienceLevel(GuiGraphics guiGraphics) {
        if (xpLevel <= 0) return;
        String s = "" + xpLevel;
        int j = (guiGraphics.guiWidth() - this.getFont().width(s)) / 2;
        int k = guiGraphics.guiHeight() - 31 - 4;
        guiGraphics.drawString(this.getFont(), s, j + 1, k, 0, false);
        guiGraphics.drawString(this.getFont(), s, j - 1, k, 0, false);
        guiGraphics.drawString(this.getFont(), s, j, k + 1, 0, false);
        guiGraphics.drawString(this.getFont(), s, j, k - 1, 0, false);
        guiGraphics.drawString(this.getFont(), s, j, k, 8453920, false);
    }

    private void renderSlot(GuiGraphics guiGraphics, int x, int y, ItemStack stack, int seed) {
        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, x, y, seed);
            assert minecraft != null;
            guiGraphics.renderItemDecorations(this.minecraft.font, stack, x, y);
        }
    }

    private void renderHearts(GuiGraphics guiGraphics) {
        int x = guiGraphics.guiWidth() / 2 - 91;
        int y = guiGraphics.guiHeight() - 39;
        int height = 10;
        int maxHearts = 10;
        for (int l = maxHearts - 1; l >= 0; --l) {
            int i1 = l / 10;
            int j1 = l % 10;
            int k1 = x + j1 * 8;
            int heartY = y - i1 * height;

            this.renderHeart(guiGraphics, HEART_CONTAINER, k1, heartY);
            int i2 = l * 2;

            if (i2 < health) {
                this.renderHeart(guiGraphics, i2 + 1 == health ? HEART_HALF : HEART_FULL, k1, heartY);
            }
        }
    }

    private void renderHeart(GuiGraphics guiGraphics, ResourceLocation heart, int x, int y) {
        guiGraphics.blitSprite(RenderType::guiTextured, heart, x, y, 9, 9);
    }


    private static final class RenderableModulePreview implements GuiEventListener, Renderable, NarratableEntry {
        private final RenderableModule module;
        private final Vector2i size;
        private Vector2i dragStart = new Vector2i(0);
        private boolean dragging;
        private final int screenWidth;
        private final int screenHeight;
        private boolean focused = false;

        private RenderableModulePreview(RenderableModule module, int screenWidth, int screenHeight) {
            this.module = module;
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            size = module.previewSize();
        }

        private void clamp() {
            if (module.x < 0) module.x = 0;
            if (module.y < 0) module.y = 0;

            if (module.x + size.x > screenWidth) module.x = screenWidth - size.x;
            if (module.y + size.y > screenHeight) module.y = screenHeight - size.y;
        }

        @Override
        public @NotNull ScreenRectangle getRectangle() {
            return new ScreenRectangle(module.x, module.y, size.x, size.y);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int i, int i1, float partialTick) {
            if (!module.isEnabled()) return;
            clamp();
            module.renderPreview(guiGraphics, partialTick);
        }

        public void renderOutline(@NotNull GuiGraphics guiGraphics) {
            if (focused) guiGraphics.renderOutline(module.x - 1, module.y - 1, size.x + 2, size.y + 2,
                Colors.CYAN.withAlpha(0x7f));
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            dragging = false;
            clamp();
            module.savePos();
            return GuiEventListener.super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (!isMouseOver(mouseX, mouseY) || button != 0)
                return GuiEventListener.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            dragging = true;
            module.x = (int) mouseX + dragStart.x;
            module.y = (int) mouseY + dragStart.y;
            return true;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOver(mouseX, mouseY) && button == 0) {
                dragStart = new Vector2i(module.x - (int) mouseX, module.y - (int) mouseY);
                return true;
            }
            return GuiEventListener.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            if (dragging) return true;
            if (mouseX >= module.x && mouseY >= module.y && mouseX < module.x + size.x && mouseY < module.y + size.y)
                return true;
            return GuiEventListener.super.isMouseOver(mouseX, mouseY);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int amount = (modifiers & GLFW.GLFW_MOD_SHIFT) > 0 ? 10 : 1;
            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT -> module.x -= amount;
                case GLFW.GLFW_KEY_RIGHT -> module.x += amount;
                case GLFW.GLFW_KEY_UP -> module.y -= amount;
                case GLFW.GLFW_KEY_DOWN -> module.y += amount;
                default -> {
                    return false;
                }
            }
            clamp();
            module.savePos();
            return true;
        }

        @Override
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }

        @Override
        public @NotNull NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public int hashCode() {
            return Objects.hash(module);
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent event) {
            return focused ? null : ComponentPath.leaf(this);
        }

        @Override
        public String toString() {
            return "RenderableModulePreview[" + "module=" + module + ']';
        }

    }
}
