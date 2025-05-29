package net.fishinghacks.utils.actions;

import com.google.common.collect.Lists;
import net.fishinghacks.utils.commands.commands.MacroCommand;
import net.fishinghacks.utils.macros.ExecutionManager;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.List;

public class ToggleMacroAction extends Action {
    private String value = "";

    @Override
    public ActionType type() {
        return ActionType.ToggleMacro;
    }

    @Override
    protected void enable() {
        if (value.isEmpty()) return;
        int id = -1;
        for (var entry : ExecutionManager.getRunningMacros())
            if (entry.getValue().name().equals(value)) {
                id = entry.getKey();
                break;
            }
        if (id == -1) MacroCommand.runMacro(value);
        else ExecutionManager.stopMacro(id);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public @Nullable List<String> validValues() {
        var macros = Lists.newArrayList("");
        try (var files = Files.list(ExecutionManager.getMacroDirectory())) {
            files.forEach(v -> {
                var name = v.getFileName().toString();
                if (!name.endsWith(".macro") || name.length() < 7) return;
                macros.add(name.substring(0, name.length() - 6));
            });
        } catch (Exception ignored) {
            return null;
        }
        return macros;
    }

    @Override
    protected void disable() {

    }
}
