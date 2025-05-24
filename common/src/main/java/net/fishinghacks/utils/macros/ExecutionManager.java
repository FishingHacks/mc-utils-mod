package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExecutionManager {
    private static final HashMap<Integer, RunningMacro> runningMacros = new HashMap<>();
    private static int currentIndex = 0;
    private static @Nullable Path macroDirectory;

    public static Path getMacroDirectory() {
        if (macroDirectory != null) return macroDirectory;
        return macroDirectory = Minecraft.getInstance().gameDirectory.toPath().resolve(".utils_data").resolve("macros")
            .toAbsolutePath();
    }

    public static Set<Map.Entry<Integer, RunningMacro>> getRunningMacros() {
        return runningMacros.entrySet();
    }

    public static void stopMacro(int index) {
        var macro = runningMacros.remove(index);
        macro.stop();
    }

    private static Optional<String> getFileContents(String file) {
        if (CommonUtil.isInvalidFilename(file)) return Optional.empty();
        try {
            return Optional.of(Files.readString(getMacroDirectory().resolve(file + ".macro")));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> startMacro(String path, Consumer<Component> outputError,
                                               Consumer<Integer> onBeforeRun,
                                               BiConsumer<Integer, RunningMacro> onExit) {
        var content = getFileContents(path);
        if (content.isEmpty()) return Optional.empty();
        int index = currentIndex++;
        onBeforeRun.accept(index);
        var macro = Executor.runInThread(content.get(), path, new HashMap<>(), outputError, v -> {
        }, ExecutionManager::getFileContents, () -> onExit.accept(index, runningMacros.remove(index)));
        runningMacros.put(index, macro);
        return Optional.of(index);
    }
}