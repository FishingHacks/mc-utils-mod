package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExecutionManager {
    private static final HashMap<Integer, RunningMacro> runningMacros = new HashMap<>();
    private static int currentIndex = 0;
    private static @Nullable Path macroDirectory;
    private static List<String> macros = new ArrayList<>();
    private static @Nullable WatchKey watcherKey = null;
    private static final @Nullable WatchService watcher;

    static {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getMacros() {
        if (watcher == null) return macros;
        if (watcherKey == null) startWatcher();

        WatchKey res;
        while ((res = watcher.poll()) != null) {
            if (!res.pollEvents().isEmpty()) {
                try (var files = Files.list(getMacroDirectory())) {
                    macros = files.map(Path::getFileName).map(Path::toString)
                        .filter(name -> name.length() > 6 && name.endsWith(".macro"))
                        .map(name -> name.substring(0, name.length() - 6)).toList();
                } catch (IOException ignored) {
                }
                break;
            }
        }
        return macros;
    }

    public static void startWatcher() {
        if (watcher == null) return;
        if (watcherKey != null && watcherKey.isValid()) return;
        if (macroDirectory == null) {
            // starts a watcher
            getMacroDirectory();
            return;
        }
        try {
            watcherKey = macroDirectory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE);
        } catch (IOException ignored) {
        }
    }

    public static Path getMacroDirectory() {
        if (macroDirectory == null)
            macroDirectory = Minecraft.getInstance().gameDirectory.toPath().resolve(".utils_data").resolve("macros")
                .toAbsolutePath();
        startWatcher();
        return macroDirectory;
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