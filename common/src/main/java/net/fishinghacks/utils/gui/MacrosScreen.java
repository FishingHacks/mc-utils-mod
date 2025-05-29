package net.fishinghacks.utils.gui;

import net.fishinghacks.utils.Colors;
import net.fishinghacks.utils.CommonUtil;
import net.fishinghacks.utils.gui.components.*;
import net.fishinghacks.utils.macros.ExecutionManager;
import net.fishinghacks.utils.macros.Highlighter;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MacrosScreen extends BlackScreen {
    private static final int boxX = 30;
    private static final int boxY = 50;
    private static final int headerHeight = 20 + Box.DEFAULT_BORDER_SIZE;
    private static final int headerY = boxY - 20;
    public static final List<String> screenshots = new ArrayList<>();

    private Component parsed = Component.empty();
    private MacroScreenInput input;
    private final Path file;
    private String content;
    private boolean didChange = false;

    private static String makeTitle(Path path) {
        String filename = path.getFileName().toString();
        return filename.substring(0, filename.length() - 6);
    }

    public MacrosScreen(@Nullable Screen parent, Path file) {
        super(Component.literal(makeTitle(file)), parent);
        this.file = file;
        try {
            content = Files.readString(file);
        } catch (IOException ignored) {
            content = "";
        }
    }

    public static void open() {
        var directory = ExecutionManager.getMacroDirectory();
        List<Path> validValues = new ArrayList<>();
        validValues.add(Path.of(""));
        try {
            FileUtil.createDirectoriesSafe(directory);
            try (var files = Files.list(directory)) {
                files.forEach(file -> {
                    String name = file.getFileName().toString();
                    // "x.macro".length
                    if (name.length() < 7) return;
                    if (!name.endsWith(".macro")) return;
                    validValues.add(file);
                });
            }
        } catch (IOException ignored) {
        }
        var parent = Minecraft.getInstance().screen;
        DropdownPopupScreen.open(Component.literal("Select Macro"), path -> {
            if (path != validValues.getFirst()) Minecraft.getInstance().setScreen(new MacrosScreen(parent, path));
            else openCreateFilePopup();
        }, validValues.getFirst(), validValues, path -> {
            if (path == validValues.getFirst()) return Component.literal("+ Create File");
            String filename = path.getFileName().toString();
            return Component.literal(filename.substring(0, filename.length() - 6));
        });
    }

    private static void openCreateFilePopup() {
        var parent = Minecraft.getInstance().screen;
        InputPopupScreen.open(Component.literal("File name"), name -> Minecraft.getInstance()
                .setScreen(new MacrosScreen(parent, ExecutionManager.getMacroDirectory().resolve(name + ".macro"))),
            s -> !CommonUtil.isInvalidFilename(s));
    }

    @Override
    protected void init() {
        super.init();
        int boxWidth = width - 60 - Box.DEFAULT_BORDER_SIZE * 2;
        int boxHeight = height - 60 - 20;
        int headerWidth = width - 60;

        addRenderableOnly(new Box(new Spacer(headerWidth, headerHeight), Box.Borders.NONE, 0, 0,
            Box.DEFAULT_BORDER_COLOR)).setPosition(boxX, headerY);
        addRenderableOnly(new Box(new Spacer(boxWidth, boxHeight))).setPosition(boxX, boxY);
        addRenderableOnly(new StringWidget(boxX + 4, headerY, headerWidth, headerHeight, getTitle(), font).alignLeft());
        addRenderableWidget(
            Button.Builder.cube("<").pos(boxX - 4 - Button.CUBE_WIDTH, headerY).onPress(ignored -> onClose()).build());
        addRenderableWidget(new IconButton.Builder(Icons.OPEN).pos(boxX + headerWidth - IconButton.DEFAULT_WIDTH - 4,
                headerY + (headerHeight - IconButton.DEFAULT_HEIGHT) / 2)
            .onPress(ignored -> Util.getPlatform().openPath(file.toAbsolutePath())).build());
        addRenderableWidget(
            new IconButton.Builder(Icons.SAVE).pos(boxX + headerWidth - IconButton.DEFAULT_WIDTH * 2 - 8,
                headerY + (headerHeight - IconButton.DEFAULT_HEIGHT) / 2).onPress(ignored -> save()).build());
        addRenderableWidget(
            new IconButton.Builder(Icons.FOLDER).pos(boxX + headerWidth - IconButton.DEFAULT_WIDTH * 3 - 12,
                    headerY + (headerHeight - IconButton.DEFAULT_HEIGHT) / 2)
                .onPress(ignored -> Util.getPlatform().openPath(file.toAbsolutePath().getParent())).build());

        input = addRenderableWidget(
            new MacroScreenInput(font, boxX + 2, boxY + 2, boxWidth, boxHeight, Component.empty(), Component.empty(),
                graphics -> {
                    int y = input.getY() + 4;
                    int x = input.getX() + 4;
                    graphics.drawWordWrap(font, parsed, x, y, input.getWidth() - 8, Colors.WHITE.get());
                }));
        input.setValueListener(this::parse);
        input.setValue(content);
        parse(content);
    }

    private void parse(String value) {
        var parsed = Component.empty();
        didChange = true;
        content = value;
        for (var line : value.split("\n")) parsed.append(Highlighter.highlightLine(line)).append("\n");
        this.parsed = parsed;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            input.insertText("    ");
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_S && (modifiers & GLFW.GLFW_MOD_CONTROL) > 0) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void save() {
        try {
            Files.writeString(file, content);
            didChange = false;
        } catch (IOException e) {
            PopupScreen.popup(Component.literal("Failed to save " + file + ". Do you want to continue anyway?"));
        }
    }

    @Override
    public void onClose() {
        if (didChange) ConfirmPopupScreen.open(Component.literal("Are you sure you want to continue without saving?"),
            super::onClose);
        else super.onClose();
    }
}
