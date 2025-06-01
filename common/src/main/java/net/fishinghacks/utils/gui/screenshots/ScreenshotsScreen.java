package net.fishinghacks.utils.gui.screenshots;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.caching.FutureState;
import net.fishinghacks.utils.caching.ScreenshotCache;
import net.fishinghacks.utils.gui.BlackScreen;
import net.fishinghacks.utils.gui.ConfirmPopupScreen;
import net.fishinghacks.utils.gui.Icons;
import net.fishinghacks.utils.gui.components.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotsScreen extends BlackScreen {
    private static final int boxX = 30;
    private static final int boxY = 50;
    private static final int headerHeight = 20 + Box.DEFAULT_BORDER_SIZE;
    private static final int headerY = boxY - 20;
    public static final List<String> screenshots = new ArrayList<>();

    private int boxWidth;
    private int boxHeight;
    private int page = 1;
    private Button nextButton = null;
    private Button prevButton = null;
    private StringWidget pageWidget = null;
    private final List<ScreenshotsBox> boxes = new ArrayList<>();
    private final List<ScreenshotEntry> loadedScreenshots = new ArrayList<>();
    private ScrollableList list;
    private int previewWidth;
    private int previewHeight;
    private int headerWidth;
    private @Nullable Integer focused = null;
    private Button focusedImageCloseButton;

    public ScreenshotsScreen(@Nullable Screen parent) {
        super(Component.empty(), parent);
        var screenshotsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");
        try (var files = Files.list(screenshotsDir)) {
            screenshots.clear();
            files.forEach(file -> {
                var filename = file.getFileName().toString();
                if (filename.length() < 5) return;
                screenshots.add(filename.substring(0, filename.length() - 4));
            });
        } catch (IOException ignored) {
        }
        loadScreenshots(1);
    }

    private int imageHeight(int width) {
        return width * this.height / this.width;
    }

    @Override
    protected void init() {
        super.init();
        boxWidth = width - 60 - Box.DEFAULT_BORDER_SIZE * 2;
        boxHeight = height - 60 - 20;
        headerWidth = width - 60;

        addRenderableOnly(new Box(new Spacer(headerWidth, headerHeight), Box.Borders.NONE, 0, 0,
            Box.DEFAULT_BORDER_COLOR)).setPosition(boxX, headerY);
        addRenderableOnly(new Box(new Spacer(boxWidth, boxHeight))).setPosition(boxX, boxY);
        addRenderableOnly(
            new StringWidget(boxX + 10, headerY, headerWidth, headerHeight, Translation.ScreenshotGuiTitle.get(),
                font).alignLeft());
        addRenderableWidget(
            Button.Builder.cube("<").pos(boxX - 4 - Button.CUBE_WIDTH, headerY).onPress(ignored -> onClose()).build());
        focusedImageCloseButton = Button.Builder.cube('×').pos(width - Button.CUBE_WIDTH - 4, 4).build();

        addRenderableWidget(
            new IconButton.Builder(Icons.FOLDER).pos(boxX - 4 - Button.CUBE_WIDTH, headerY + Button.CUBE_WIDTH + 4)
                .onPress(ignored -> Util.getPlatform()
                    .openPath(Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots"))).build());

        addPageSelector();
        addCosmeticBoxes();
    }

    private void addPageSelector() {
        LinearLayout pageSelector = new LinearLayout(boxX + (headerWidth - 2 * Button.CUBE_WIDTH - 8 - 20) / 2,
            headerY + (headerHeight - Button.DEFAULT_HEIGHT) / 2, LinearLayout.Orientation.HORIZONTAL).spacing(4);
        this.prevButton = pageSelector.addChild(
            Button.Builder.cube("<").active(page > 1).onPress(ignored -> loadScreenshots(page - 1)).build());
        this.pageWidget = pageSelector.addChild(
            new StringWidget(20, Button.DEFAULT_HEIGHT, Component.literal("" + page), font).alignCenter());
        this.nextButton = pageSelector.addChild(Button.Builder.cube(">").active((page * 9) < screenshots.size())
            .onPress(ignored -> loadScreenshots(page + 1)).build());
        pageSelector.arrangeElements();
        pageSelector.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return super.keyPressed(keyCode, scanCode, modifiers);
        if (focused != null) return false;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void addCosmeticBoxes() {
        boxes.clear();
        previewWidth = (boxWidth - 40 /* list padding */ - 30 /* 3 * 10px padding */ - 12 /* 3 * 2 * 2px box
        border
            padding */) / 3;
        previewHeight = imageHeight(previewWidth);
        LinearLayout row = LinearLayout.vertical().spacing(10);
        int boxHeight = previewHeight + 7 + IconButton.DEFAULT_HEIGHT;
        int normalTextWidth = font.width("0000-01-01_00.00.00");
        if (previewWidth - normalTextWidth < ScreenshotsBox.BUTTON_WIDTH) boxHeight += font.lineHeight + 3;

        int id = 0;
        for (int i = 0; i < 3; ++i) {
            LinearLayout column = LinearLayout.horizontal().spacing(10);
            for (int j = 0; j < 3; ++j) {
                var box = new ScreenshotsBox(0, 0, previewWidth + 6, boxHeight, this, id);
                column.addChild(box);
                boxes.add(box);
                id++;
            }
            row.addChild(column);
        }
        list = new ScrollableList(boxX + 20, boxY + 20, boxWidth - 40, this.boxHeight - 40, row,
            list == null ? 0 : list.getScrollOffset());
        list.getChildren(this::addWidget);
        addRenderableWidget(list);
    }

    public void openScreenshot(int id) {
        if (id >= loadedScreenshots.size()) return;
        Util.getPlatform().openPath(Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots")
            .resolve(loadedScreenshots.get(id).name + ".png").toAbsolutePath());
    }

    public void refreshScreenshots() {
        var screenshotsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots");
        try (var files = Files.list(screenshotsDir)) {
            screenshots.clear();
            files.forEach(file -> {
                var filename = file.getFileName().toString();
                if (filename.length() < 5 || !filename.endsWith(".png")) return;
                screenshots.add(filename.substring(0, filename.length() - 4));
            });
        } catch (IOException ignored) {
        }
        if (page > 1 && (page - 1) * 9 >= screenshots.size()) loadScreenshots(page - 1);
        else loadScreenshots(page);
    }

    public void deleteScreenshot(int id) {
        if (id >= loadedScreenshots.size()) return;
        ConfirmPopupScreen.open(Translation.ScreenshotGuiConfirmDelete.with(loadedScreenshots.get(id).name), () -> {
            try {
                Files.delete(Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots")
                    .resolve(loadedScreenshots.get(id).name + ".png"));
                refreshScreenshots();
            } catch (IOException ignored) {
            }
        });
    }

    public void copyScreenshot(int id) {
        if (id >= loadedScreenshots.size()) return;
        if (ScreenshotCopier.onMac()) {
            Path path = Minecraft.getInstance().gameDirectory.toPath().resolve("screenshots")
                .resolve(loadedScreenshots.get(id).name + ".png");
            ScreenshotCopier.doCopyMacOS(path.toAbsolutePath().toString());
        } else ScreenshotCache.instance.getOrLoad(loadedScreenshots.get(id).name)
            .thenAccept(ScreenshotCopier::handleScreenshotAWT);
    }

    public void uploadScreenshot(int ignoredid) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (focused != null) return false;
        if (list.isMouseOver(mouseX, mouseY) && list.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void loadScreenshots(int page) {
        if (page < 1) return;
        this.page = page;
        if (pageWidget != null) pageWidget.setMessage(Component.literal("" + page));
        int offset = (page - 1) * 9;
        if (offset >= screenshots.size() && offset > 0) {
            loadScreenshots((screenshots.size() / 9) + 1);
            return;
        }
        loadedScreenshots.forEach(ScreenshotEntry::close);
        loadedScreenshots.clear();
        for (int i = offset; i < offset + 9; ++i)
            if (i >= screenshots.size()) break;
            else loadedScreenshots.add(new ScreenshotEntry(screenshots.get(i)));
    }

    private void update() {
        loadedScreenshots.forEach(ScreenshotEntry::update);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (focused != null) {
            focused = null;
            return true;
        }
        int i = 0;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        assert loadedScreenshots.size() <= boxes.size();
        for (var ignored : loadedScreenshots) {
            int x = boxes.get(i).getX() + 2;
            int y = boxes.get(i).getY() + 2;
            if (mx >= x && my >= y && mx < x + previewWidth && my < y + previewHeight && mx >= list.getX() && my >= list.getY() && mx < list.getRight() && my < list.getBottom()) {
                focused = i;
                return true;
            }
            ++i;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        if (focused != null) {
            focused = null;
            return;
        }
        super.onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        update();
        if (focused != null) if (focused < loadedScreenshots.size()) {
            renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            loadedScreenshots.get(focused).blit(guiGraphics, 0, 0, width, height);
            focusedImageCloseButton.render(guiGraphics, mouseX, mouseY, partialTick);
            return;
        } else focused = null;

        for (int i = 0; i < boxes.size(); ++i) {
            boolean isActive = i < loadedScreenshots.size();
            boxes.get(i).visitWidgets(widget -> widget.active = widget.visible = isActive);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        prevButton.active = page > 1;
        nextButton.active = (page * 9) < screenshots.size();

        int i = 0;
        assert loadedScreenshots.size() <= boxes.size();
        guiGraphics.enableScissor(list.getX(), list.getY(), list.getRight(), list.getBottom());
        for (var entry : loadedScreenshots) {
            int x = boxes.get(i).getX() + 2;
            int y = boxes.get(i).getY() + 2;
            ++i;
            if (!guiGraphics.containsPointInScissor(x, y) && !guiGraphics.containsPointInScissor(x + previewWidth,
                y + previewHeight)) continue;
            entry.blit(guiGraphics, x, y, previewWidth, previewHeight);
            var text = font.split(Component.literal(entry.name), previewWidth).getFirst();
            guiGraphics.drawString(font, text, x + 2, y + previewHeight + 3, Colors.WHITE.get());
        }
        guiGraphics.disableScissor();
    }

    @Override
    public void removed() {
        super.removed();
    }

    private static class ScreenshotEntry {
        @Nullable ResourceLocation location = null;
        final String name;
        int width = -1;
        int height = -1;

        ScreenshotEntry(String name) {
            this.name = name;
        }

        void update() {
            if (location != null) return;
            var state = FutureState.from(ScreenshotCache.instance.getOrLoad(name));
            if (state.didError()) return;
            var value = state.getValue();
            if (state.isProcessing() || value.isEmpty()) return;
            location = Constants.id("screenshots_menu/" + Hashing.sha256().hashBytes(name.getBytes()));
            NativeImage original = value.get();
            width = original.getWidth();
            height = original.getHeight();
            NativeImage copied = new NativeImage(width, height, true);
            copied.copyFrom(original);
            Minecraft.getInstance().getTextureManager()
                .register(location, new DynamicTexture(location::toString, copied));
        }

        int scaleWidth(int height) {
            return height * this.width / this.height;
        }

        int scaleHeight(int width) {
            return width * this.height / this.width;
        }

        void blit(GuiGraphics graphics, int x, int y, int width, int height) {
            if (width < 1 || height < 1 || location == null) return;
            int scaledWidth = width;
            int scaledHeight = height;
            if (scaleWidth(height) > width) scaledHeight = scaleHeight(width);
            else scaledWidth = scaleWidth(height);
            graphics.blit(RenderType::guiTextured, location, x + (width - scaledWidth) / 2,
                y + (height - scaledHeight) / 2, 0, 0, scaledWidth, scaledHeight, this.width, this.height, this.width,
                this.height, -1);
        }

        void close() {
            if (location != null) Minecraft.getInstance().getTextureManager().release(location);
        }
    }
}
