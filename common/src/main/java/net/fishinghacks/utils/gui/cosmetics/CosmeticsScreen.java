package net.fishinghacks.utils.gui.cosmetics;

import com.mojang.blaze3d.platform.Lighting;
import net.fishinghacks.utils.caching.FutureStateHolder;
import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.cosmetics.CapeHandler;
import net.fishinghacks.utils.cosmetics.CosmeticModelHandler;
import net.fishinghacks.utils.gui.BlackScreen;
import net.fishinghacks.utils.gui.DisplayPlayerEntityRenderer;
import net.fishinghacks.utils.gui.PlaceholderEntity;
import net.fishinghacks.utils.gui.components.*;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.connection.packets.AddModelPacket;
import net.fishinghacks.utils.connection.packets.RemoveModelPacket;
import net.fishinghacks.utils.connection.packets.SetCapePacket;
import net.fishinghacks.utils.connection.packets.SetModelsPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsScreen extends BlackScreen {
    private static final int playerBoxWidth = 150 - 2 * Box.DEFAULT_BORDER_SIZE;
    private static final int playerBoxWidthBorder = 150;
    private static final int playerWidth = 90;
    private static final int playerHeight = (int) ((float) playerWidth * 1.8f);
    private static final int playerOffsetX = (playerBoxWidthBorder - playerWidth) / 2;
    private static final int boxX = 100;
    private static final int boxY = 70;
    private static final int headerHeight = 20 + Box.DEFAULT_BORDER_SIZE;
    private static final int headerY = boxY - 20;
    private final PlaceholderEntity entity = new PlaceholderEntity(Minecraft.getInstance().getGameProfile(),
        this::setSlim);
    private final DisplayPlayerEntityRenderer renderer = new DisplayPlayerEntityRenderer(
        new EntityRendererProvider.Context(Minecraft.getInstance().getEntityRenderDispatcher(),
            Minecraft.getInstance().getItemModelResolver(), Minecraft.getInstance().getMapRenderer(),
            Minecraft.getInstance().getBlockRenderer(), Minecraft.getInstance().getResourceManager(),
            Minecraft.getInstance().getEntityModels(), new EquipmentAssetManager(), Minecraft.getInstance().font),
        entity.slim);
    private int cosmeticBoxWidth;
    private int boxHeight;
    private int playerBoxX;
    private double xOff = 0;
    private double yOff = (double) playerHeight / 2;
    private boolean isDragging = false;
    private List<CosmeticsEntry> fetchedList = List.of();
    @Nullable
    private FutureStateHolder<Fetcher> fetcher = null;
    private int page = 1;
    private boolean hasPrev = false;
    private boolean hasNext = true;
    private Button nextButton = null;
    private Button prevButton = null;
    private StringWidget pageWidget = null;
    private final List<CosmeticsBox> boxes = new ArrayList<>();
    private ScrollableList cosmeticsList;
    private int previewWidth;
    private int previewHeight;
    private int headerWidth;
    private CosmeticScreenType type = CosmeticScreenType.ServerCapes;
    private StringWidget title;

    public CosmeticsScreen(@Nullable Screen parent) {
        super(Component.empty(), parent);
        fetch(1);
    }

    private Component getTitleComponent() {
        if (fetcher == null || fetcher.getState().isDone()) return Translation.CosmeticGuiTitle.get();
        var comp = Translation.CosmeticGuiTitle.get().copy().append("    ");
        if (fetcher.getState().didError())
            comp = comp.append(Translation.CosmeticGuiErrored.get().copy().withStyle(ChatFormatting.RED));
        else comp = comp.append(Translation.CosmeticGuiLoading.get());
        return comp;
    }

    @Override
    protected void init() {
        super.init();
        cosmeticBoxWidth = width - 200 - playerBoxWidthBorder - Box.DEFAULT_BORDER_SIZE;
        boxHeight = height - 100 - 20;
        playerBoxX = width - 100 - playerBoxWidth - 2 * Box.DEFAULT_BORDER_SIZE;
        headerWidth = width - 200;

        addRenderableOnly(new Box(new Spacer(headerWidth, headerHeight), Box.Borders.NONE, 0, 0,
            Box.DEFAULT_BORDER_COLOR)).setPosition(boxX, headerY);
        addRenderableOnly(
            new Box(new Spacer(cosmeticBoxWidth, boxHeight), new Box.Borders().setRight(false))).setPosition(boxX,
            boxY);
        addRenderableOnly(new Box(new Spacer(playerBoxWidth, boxHeight), new Box.Borders())).setPosition(playerBoxX,
            boxY);
        title = addRenderableOnly(
            new StringWidget(boxX + 10, headerY, headerWidth, headerHeight, getTitleComponent(), font).alignLeft());
        addRenderableWidget(
            Button.Builder.cube("<").pos(boxX - 4 - Button.CUBE_WIDTH, headerY).onPress(ignored -> onClose()).build());
        addRenderableWidget(Button.Builder.big(Translation.CosmeticGuiClear.get()).pos(playerBoxX + 4, boxY + 4)
            .width(playerBoxWidth - 4).onPress(ignored -> applyCosmetic(null, true)).build());
        var dropdown = addRenderableWidget(GuiDropdown.fromTranslatableEnum(type, CosmeticScreenType.values()));
        dropdown.setSize(Button.BIG_WIDTH, GuiDropdown.DEFAULT_HEIGHT);
        dropdown.setPosition(playerBoxX - 6 - Button.BIG_WIDTH,
            headerY + (headerHeight - GuiDropdown.DEFAULT_HEIGHT) / 2);
        dropdown.onValueChange((i, type) -> this.setType(type));

        addPageSelector();
        addElytraSwitch();
        addCosmeticBoxes();
    }

    private void addPageSelector() {
        LinearLayout pageSelector = new LinearLayout(boxX + (headerWidth - 2 * Button.CUBE_WIDTH - 8 - 20) / 2,
            headerY + (headerHeight - Button.DEFAULT_HEIGHT) / 2, LinearLayout.Orientation.HORIZONTAL).spacing(4);
        this.prevButton = pageSelector.addChild(
            Button.Builder.cube("<").active(fetcher == null).onPress(ignored -> fetch(page - 1)).build());
        this.pageWidget = pageSelector.addChild(
            new StringWidget(20, Button.DEFAULT_HEIGHT, Component.literal("" + page), font).alignCenter());
        this.nextButton = pageSelector.addChild(
            Button.Builder.cube(">").active(fetcher == null).onPress(ignored -> fetch(page + 1)).build());
        pageSelector.arrangeElements();
        pageSelector.visitWidgets(this::addRenderableWidget);
    }

    private void addElytraSwitch() {
        LinearLayout elytraSwitch = new LinearLayout(playerBoxX + playerBoxWidthBorder / 8,
            headerY + (headerHeight - Toggle.DEFAULT_HEIGHT) / 2, LinearLayout.Orientation.HORIZONTAL).spacing(4);
        int stringWidth = (playerBoxWidthBorder / 4 * 3 - 8 - Toggle.DEFAULT_WIDTH) / 2;
        elytraSwitch.addChild(
            new StringWidget(stringWidth, Toggle.DEFAULT_HEIGHT, Translation.CosmeticGuiCape.get(), font).alignRight());
        elytraSwitch.addChild(
            new Toggle.Builder().checked(entity.showElytra).onChange((i0, v) -> entity.showElytra = v).build());
        elytraSwitch.addChild(new StringWidget(stringWidth, Toggle.DEFAULT_HEIGHT, Translation.CosmeticGuiElytra.get(),
            font).alignLeft());
        elytraSwitch.arrangeElements();
        elytraSwitch.visitWidgets(this::addRenderableWidget);
    }

    private void addCosmeticBoxes() {
        boxes.clear();
        previewWidth = (cosmeticBoxWidth - 40 - 60) / 4;
        previewHeight = previewWidth / 2;
        LinearLayout row = LinearLayout.vertical().spacing(10);
        for (int i = 0; i < 5; ++i) {
            LinearLayout column = LinearLayout.horizontal().spacing(10);
            for (int j = 0; j < 4; ++j) {
                final int id = i * 4 + j;
                var box = new CosmeticsBox((setActive) -> applyCosmetic(id, setActive), 0, 0, previewWidth + 6,
                    previewHeight + 20);
                column.addChild(box);
                boxes.add(box);
            }
            row.addChild(column);
        }
        cosmeticsList = new ScrollableList(boxX + 20, boxY + 20, cosmeticBoxWidth - 40, boxHeight - 40, row,
            cosmeticsList == null ? 0 : cosmeticsList.getScrollOffset());
        cosmeticsList.getChildren(this::addWidget);
        addRenderableWidget(cosmeticsList);
    }

    private void setType(CosmeticScreenType newType) {
        this.type = newType;
        nextButton.active = false;
        prevButton.active = false;
        fetch(1);
    }

    protected boolean isSelected(CosmeticsEntry e) {
        switch (type) {
            case MinecraftCapes -> {
                var profile = CapeHandler.fromProfile(Minecraft.getInstance().getGameProfile());
                if (!profile.isServiceProviderCape && profile.serviceProviderCapeId != null)
                    return profile.serviceProviderCapeId.equals(e.hash);
            }
            case ServerCapes -> {
                var profile = CapeHandler.fromProfile(Minecraft.getInstance().getGameProfile());
                if (profile.isServiceProviderCape && profile.serviceProviderCapeId != null)
                    return profile.serviceProviderCapeId.equals(e.hash);
            }
            case ServerModels -> {
                var profile = CosmeticModelHandler.fromProfile(Minecraft.getInstance().getGameProfile());
                for (var model : profile.models) if (model.id().equals(e.hash)) return true;
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (cosmeticsList.isMouseOver(mouseX, mouseY) && cosmeticsList.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void updateFetchedList(List<CosmeticsEntry> list) {
        fetchedList.forEach(CosmeticsEntry::close);
        fetchedList = list;
    }

    private void fetch(int page) {
        if (page < 1) return;
        if (fetcher != null) fetcher.future().cancel(true);
        if (nextButton != null) nextButton.active = false;
        if (prevButton != null) prevButton.active = false;
        updateFetchedList(List.of());
        fetcher = new FutureStateHolder<>(switch (type) {
            case MinecraftCapes -> MinecraftCapesGallery.fetchPage(page);
            case ServerCapes -> ServerCosmetics.fetchCapes(page);
            case ServerModels -> ServerCosmetics.fetchModels(page);
        });
        if (title != null) title.setMessage(getTitleComponent());
    }

    private void update() {
        if (fetcher == null) return;
        if (fetcher.getState().isProcessing()) return;
        if (title != null) title.setMessage(getTitleComponent());
        if (fetcher.getState().didError()) {
            fetcher = null;
            updateFetchedList(List.of());
            return;
        }
        var value = fetcher.getState().getValue();
        if (value.isEmpty()) return;
        cosmeticsList.setScrollOffset(0);
        Fetcher gallery = value.get();
        pageWidget.setMessage(Component.literal("" + gallery.currentPage()));
        page = gallery.currentPage();
        hasNext = gallery.hasNext();
        hasPrev = gallery.hasPrev();
        updateFetchedList(gallery.getFetched());
        fetcher = null;
    }

    private void setSlim(boolean slim) {
        this.renderer.setSlim(slim);
    }

    private void applyCosmetic(@Nullable Integer id, boolean setActive) {
        var conn = ClientConnectionHandler.getInstance().getConnection();
        if (conn == null) return;
        CosmeticsEntry entry;
        try {
            if (id != null) entry = fetchedList.get(id);
            else entry = null;
        } catch (IndexOutOfBoundsException ignored) {
            return;
        }
        switch (type) {
            case MinecraftCapes -> conn.send(new SetCapePacket(entry != null && setActive ? entry.hash : null, true));
            case ServerCapes -> conn.send(new SetCapePacket(entry != null && setActive ? entry.hash : null, false));
            case ServerModels -> {
                if (entry == null) conn.send(new SetModelsPacket(List.of()));
                else {
                    boolean isSet = CosmeticModelHandler.fromProfile(
                            Minecraft.getInstance().getGameProfile()).models.stream()
                        .anyMatch(model -> model.id().equals(entry.hash));
                    if (isSet) conn.send(new RemoveModelPacket(entry.hash));
                    else conn.send(new AddModelPacket(entry.hash));
                }
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        update();
        prevButton.active = hasPrev;
        nextButton.active = hasNext;

        int actualY = boxY + (boxHeight - playerHeight) / 2;
        int actualX = playerBoxX + playerOffsetX;

        renderEntityInInventoryFollowsMouse(guiGraphics, actualX, actualY, actualX + playerWidth,
            actualY + playerHeight, 60, this.entity);

        assert fetchedList.isEmpty() || fetchedList.size() == 20;
        int i = 0;
        guiGraphics.enableScissor(cosmeticsList.getX(), cosmeticsList.getY(), cosmeticsList.getRight(),
            cosmeticsList.getBottom());
        for (var entry : fetchedList) {
            boolean selected = isSelected(entry);
            boxes.get(i).setSelected(selected);
            int x = boxes.get(i).getX() + 3;
            int y = boxes.get(i).getY() + 3;
            ++i;
            if (!guiGraphics.containsPointInScissor(x, y) && !guiGraphics.containsPointInScissor(x + previewWidth,
                y + previewHeight)) continue;
            entry.blit(guiGraphics, x, y, previewWidth, previewHeight);
            if (selected) guiGraphics.drawString(font, Component.literal(entry.title).withStyle(ChatFormatting.BOLD), x,
                y + previewHeight + 4, Colors.WHITE.get());
            else guiGraphics.drawString(font, entry.title, x, y + previewHeight + 4, Colors.WHITE.get());
        }
        guiGraphics.disableScissor();
    }

    @Override
    public void removed() {
        super.removed();
        fetchedList.forEach(CosmeticsEntry::close);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging || (mouseX >= playerBoxX && mouseX < playerBoxX + playerBoxWidthBorder && mouseY >= boxY && mouseY < boxY + boxHeight)) {
            isDragging = true;
            xOff -= dragX / 4;
            yOff -= dragY / 2;
            xOff = Mth.positiveModulo(xOff, playerWidth);
            yOff = Mth.clamp(yOff, 0, playerHeight);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }


    public void renderEntityInInventoryFollowsMouse(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int scale,
                                                    PlaceholderEntity entity) {
        float mouseX = (float) x1 + (float) xOff;
        float mouseY = (float) y1 + (float) yOff;
        float centerX = (float) (x1 + x2) / 2.0F;
        float centerY = (float) (y1 + y2) / 2.0F;
        float angleX = (float) Math.atan((centerX - mouseX) / (float) ((x2 - x1) / 2));
        float angleY = (float) Math.atan((centerY - mouseY) / (float) (y2 - y1));

        float x = (float) (x1 + x2) / 2.0F;
        float y = (float) (y1 + y2) / 2.0F;
        Quaternionf rotation = new Quaternionf().rotateX(angleY).rotateY((float) Math.PI - angleX * 4)
            .rotateZ((float) Math.PI);
        Vector3f translation = new Vector3f(0.0F, 1.8f / 2f, 0f);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 50);
        guiGraphics.pose().scale(scale, scale, -scale);
        guiGraphics.pose().translate(translation.x, translation.y, translation.z);
        guiGraphics.pose().mulPose(rotation);

        guiGraphics.flush();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityrenderdispatcher.setRenderShadow(false);

        guiGraphics.drawSpecial(bufferSource -> renderer.render(entity, guiGraphics.pose(), bufferSource, 0xf000f0));
        guiGraphics.flush();
        entityrenderdispatcher.setRenderShadow(true);
        guiGraphics.pose().popPose();
        Lighting.setupFor3DItems();
    }

}
