package net.fishinghacks.utils.macros.parsing;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Location(String file, int row, int column) {
    public static final Location ZERO = new Location("", 0, 0);

    public static Location builtin(String file) {
        return new Location("@builtins/" + file, 0, 0);
    }

    public void print(Function<String, Optional<String>> getFileContents, String file, String contents, Component error,
                      Consumer<Component> output) {
        if (this.file.isEmpty()) {
            output.accept(error.copy().withStyle(ChatFormatting.RED));
            return;
        }
        //    .──[ my_macro:5:7 ]
        //  4 |     if (error != null)
        //  5 |         throw_error(error);
        //    |         ^- Error: Tried to divide by zero
        //  6 |     else exit();
        output.accept(Component.literal("   .──[ ").append(this.file).append(":").append(String.valueOf(row)).append(":")
            .append(String.valueOf(column)).append(" ]"));
        String fileContents = this.file.equals(file) ? contents : getFileContents.apply(this.file).orElse("");
        int startingLine = Math.max(row - 1, 1);
        boolean didAnything = false;
        for (var line : fileContents.lines().skip(startingLine - 1).limit(3).toList()) {
            didAnything = true;
            var lineString = String.valueOf(startingLine);
            var fullLineString = (lineString.length() == 1 ? " " + lineString : lineString) + " | ";
            output.accept(Component.literal(fullLineString).append(line));
            if (startingLine == this.row) {
                int width = Minecraft.getInstance().font.width(fullLineString + line.substring(0, column - 1));
                int spaceWidth = Minecraft.getInstance().font.width(" ");
                output.accept(
                    Component.literal(" ".repeat((width + spaceWidth / 2) / spaceWidth)).append("^- ").append(error)
                        .withStyle(ChatFormatting.RED));
            }
            startingLine++;
        }
        if (!didAnything) output.accept(error.copy().withStyle(ChatFormatting.RED));
    }
}
