package net.fishinghacks.utils.gui.mcsettings;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.gui.components.Button;
import net.fishinghacks.utils.gui.components.Spacer;
import net.fishinghacks.utils.Colors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ResourcePacks implements OptionSubscreen {
    public static final ResourceLocation DEFAULT_ICON = ResourceLocation.withDefaultNamespace(
        "textures/misc/unknown_pack.png");
    private static final int DIVIDER_WIDTH = 20;
    private static final int PADDING = 10;

    @Nullable
    private Watcher watcher;
    private final PackSelectionModel model;
    private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();
    private long lastChangePoll = Util.getMillis();
    @Nullable
    private Long reloadAt = null;
    private boolean reload = false;

    public ResourcePacks(Options options, Minecraft mc) {
        watcher = Watcher.create(mc.getResourcePackDirectory());
        model = new PackSelectionModel(() -> reload = false, this::getPackIcon, mc.getResourcePackRepository(),
            options::updateResourcePacks);
    }

    private ResourceLocation getPackIcon(Pack pack) {
        return this.packIcons.computeIfAbsent(pack.getId(),
            (p_280879_) -> this.loadPackIcon(Minecraft.getInstance().getTextureManager(), pack));
    }

    private ResourceLocation loadPackIcon(TextureManager textureManager, Pack pack) {
        try {
            try (PackResources resources = pack.open()) {
                IoSupplier<InputStream> iconSupplier = resources.getRootResource("pack.png");
                if (iconSupplier == null) return DEFAULT_ICON;

                String packId = pack.getId();
                String sanitizedId = Util.sanitizeName(packId, ResourceLocation::validPathChar);
                ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace(
                    "pack/" + sanitizedId + "/" + Hashing.sha1().hashUnencodedChars(packId) + "/icon");

                try (InputStream inputstream = iconSupplier.get()) {
                    NativeImage nativeimage = NativeImage.read(inputstream);
                    Objects.requireNonNull(resourcelocation);
                    textureManager.register(resourcelocation,
                        new DynamicTexture(resourcelocation::toString, nativeimage));
                    return resourcelocation;
                }
            }
        } catch (Exception exception) {
            Constants.LOG.warn("Failed to load icon from pack {}", pack.getId(), exception);
        }
        return DEFAULT_ICON;
    }


    @Override
    public void addElements(LinearLayout layout, Options options, int configWidth, McSettingsScreen parent) {
        reload = false;
        int elementWidth = (configWidth - DIVIDER_WIDTH - 2 * PADDING) / 2;
        LinearLayout topLevelLayout = LinearLayout.horizontal();
        layout.addChild(topLevelLayout);

        LinearLayout unselectedPacks = LinearLayout.vertical();
        LinearLayout selectedPacks = LinearLayout.vertical();
        unselectedPacks.addChild(new Spacer(elementWidth, PADDING));
        selectedPacks.addChild(new Spacer(elementWidth, PADDING));
        topLevelLayout.addChild(new Spacer(PADDING, 0));
        topLevelLayout.addChild(unselectedPacks);
        topLevelLayout.addChild(new Spacer(DIVIDER_WIDTH, 0));
        topLevelLayout.addChild(selectedPacks);
        topLevelLayout.addChild(new Spacer(PADDING, 0));

        this.model.getUnselected().forEach(entry -> unselectedPacks.addChild(new PackEntry(entry, elementWidth)));
        this.model.getSelected().forEach(entry -> selectedPacks.addChild(new PackEntry(entry, elementWidth)));

        unselectedPacks.addChild(new Spacer(elementWidth, PADDING));
        selectedPacks.addChild(new Spacer(elementWidth, PADDING));
    }

    private void reload() {
        model.findNewPacks();
        packIcons.clear();
        reload = true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, McSettingsScreen parent) {
        if (reload) {
            parent.rebuildList();
            reload = false;
        }
        if (watcher == null) return;
        if (Util.getMillis() - lastChangePoll > 1000L) {
            lastChangePoll = Util.getMillis();
            try {
                if (watcher.pollForChanges()) this.reloadAt = lastChangePoll + 500L;
            } catch (Exception ignored) {
            }
        }
        if (reloadAt != null && reloadAt - Util.getMillis() <= 0) {
            reload();
            reloadAt = null;
        }
    }

    @Override
    public void onClose(Options options, McSettingsScreen parent) {
        this.model.commit();
        if (this.watcher != null) try {
            watcher.close();
            watcher = null;
        } catch (Exception ignored) {
        }
    }

    static class Watcher implements AutoCloseable {
        private final WatchService watcher;
        private final Path packPath;

        public Watcher(Path packPath) throws IOException {
            this.packPath = packPath;
            this.watcher = packPath.getFileSystem().newWatchService();

            try {
                this.watchDir(packPath);

                try (DirectoryStream<Path> directorystream = Files.newDirectoryStream(packPath)) {
                    for (Path path : directorystream) {
                        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDir(path);
                        }
                    }
                }

            } catch (Exception exception) {
                this.watcher.close();
                throw exception;
            }
        }

        @Nullable
        public static Watcher create(Path packPath) {
            try {
                return new Watcher(packPath);
            } catch (IOException ioexception) {
                Constants.LOG.warn("Failed to initialize pack directory {} monitoring", packPath, ioexception);
                return null;
            }
        }

        private void watchDir(Path path) throws IOException {
            path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChanges() throws IOException {
            boolean flag = false;

            WatchKey watchkey;
            while ((watchkey = this.watcher.poll()) != null) {
                for (WatchEvent<?> watchevent : watchkey.pollEvents()) {
                    flag = true;
                    if (watchkey.watchable() == this.packPath && watchevent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path path = this.packPath.resolve((Path) watchevent.context());
                        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                            this.watchDir(path);
                        }
                    }
                }

                watchkey.reset();
            }

            return flag;
        }

        public void close() throws IOException {
            this.watcher.close();
        }
    }

    class PackEntry extends AbstractWidget {
        private static final int HEIGHT = 32;
        private static final int PADDING = 4;
        private static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
        public static final String MOVE_DOWN = "⏷";
        public static final String MOVE_UP = "⏶";
        public static final String MOVE_RIGHT = ">";
        public static final String MOVE_LEFT = "<";

        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private final FormattedCharSequence incompatibleNameDisplayCache;
        private final MultiLineLabel incompatibleDescriptionDisplayCache;
        private final Button moveUp;
        private final Button moveDown;
        private final Button selectUnselect;
        private final int maxStringWidth;

        public PackEntry(PackSelectionModel.Entry pack, int width) {
            super(0, 0, width, HEIGHT + 2 * PADDING, Component.empty());
            maxStringWidth = width - 2 * Button.CUBE_WIDTH - 36;
            Minecraft minecraft = Minecraft.getInstance();
            this.pack = pack;
            this.nameDisplayCache = cacheName(minecraft, pack.getTitle());
            this.descriptionDisplayCache = cacheDescription(minecraft, pack.getExtendedDescription());
            this.incompatibleNameDisplayCache = cacheName(minecraft, INCOMPATIBLE_TITLE);
            this.incompatibleDescriptionDisplayCache = cacheDescription(minecraft,
                pack.getCompatibility().getDescription());
            moveUp = Button.Builder.cube(MOVE_UP).onPress(this::moveUp).active(pack.canMoveUp()).build();
            moveDown = Button.Builder.cube(MOVE_DOWN).onPress(this::moveDown).active(pack.canMoveDown())
                .build();
            selectUnselect = Button.Builder.cube(pack.isSelected() ? MOVE_LEFT : MOVE_RIGHT).onPress(btn -> {
                if (pack.isSelected()) pack.unselect();
                else pack.select();
                ResourcePacks.this.reload = true;
            }).active(pack.canSelect() || pack.canUnselect()).height(HEIGHT).build();
        }

        private void moveUp(Button ignored) {
            if(pack.canMoveUp()) pack.moveUp();
            ResourcePacks.this.reload = true;
        }

        private void moveDown(Button ignored) {
            if(pack.canMoveDown()) pack.moveDown();
            ResourcePacks.this.reload = true;
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            if(pack.isSelected()) {
                selectUnselect.setX(x);
                x += Button.CUBE_WIDTH;
            } else selectUnselect.setX(x + width - selectUnselect.getWidth());
            moveDown.setX(x);
            moveUp.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            selectUnselect.setY(y + PADDING);
            moveUp.setY(y + PADDING);
            moveDown.setY(y + PADDING + HEIGHT / 2);
        }

        private FormattedCharSequence cacheName(Minecraft minecraft, Component name) {
            int i = minecraft.font.width(name);
            if (i > maxStringWidth) {
                FormattedText formattedtext = FormattedText.composite(
                    minecraft.font.substrByWidth(name, maxStringWidth - minecraft.font.width("...")),
                    FormattedText.of("..."));
                return Language.getInstance().getVisualOrder(formattedtext);
            } else {
                return name.getVisualOrderText();
            }
        }

        private MultiLineLabel cacheDescription(Minecraft minecraft, Component text) {
            return MultiLineLabel.create(minecraft.font, maxStringWidth, 2, text);
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float v) {
            int offsetY = getY() + PADDING;
            int offsetX = getX() + Button.CUBE_WIDTH;
            if(pack.isSelected()) offsetX += Button.CUBE_WIDTH;
            guiGraphics.blit(RenderType::guiTextured, pack.getIconTexture(), offsetX, offsetY, 0f, 0f, 32, 32, 32, 32);
            offsetX += 36;
            var name = nameDisplayCache;
            var description = descriptionDisplayCache;
            if(getRectangle().containsPoint(mouseX, mouseY) && !pack.getCompatibility().isCompatible()) {
                name = incompatibleNameDisplayCache;
                description = incompatibleDescriptionDisplayCache;
            }

            guiGraphics.drawString(Minecraft.getInstance().font, name, offsetX, offsetY + 1, Colors.WHITE.get());
            description.renderLeftAligned(guiGraphics, offsetX, offsetY + 12, 10, Colors.WHITE.get());
        }

        @Override
        public @Nullable ComponentPath nextFocusPath(@NotNull FocusNavigationEvent p_265640_) {
            return null;
        }

        @Override
        public @Nullable ComponentPath getCurrentFocusPath() {
            return null;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return false;
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return false;
        }

        @Override
        public void visitWidgets(@NotNull Consumer<AbstractWidget> consumer) {
            super.visitWidgets(consumer);
            consumer.accept(moveUp);
            consumer.accept(moveDown);
            consumer.accept(selectUnselect);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }
    }
}
