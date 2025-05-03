package net.fishinghacks.utils.client.gui.configuration;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UndoManager {
    public record Step<T>(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
        private void runUndo() {
            undo.accept(oldValue);
        }
        private void runRedo() {
            run.accept(newValue);
        }
    }

    private final List<UndoManager.Step<?>> undos = new ArrayList<>();
    private final List<UndoManager.Step<?>> redos = new ArrayList<>();


    public void undo() {
        if (canUndo()) {
            UndoManager.Step<?> step = undos.removeLast();
            step.runUndo();
            redos.add(step);
        }
    }

    public void redo() {
        if (canRedo()) {
            UndoManager.Step<?> step = redos.removeLast();
            step.runRedo();
            undos.add(step);
        }
    }

    private void add(UndoManager.Step<?> step, boolean execute) {
        undos.add(step);
        redos.clear();
        if (execute) {
            step.runRedo();
        }
    }

    public <T> UndoManager.Step<T> step(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
        return new UndoManager.Step<>(run, newValue, undo, oldValue);
    }

    public <T> void add(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
        add(step(run, newValue, undo, oldValue), true);
    }
    public <T> void add(Consumer<T> apply, T newValue, T oldValue) {
        add(step(apply, newValue, apply, oldValue), true);
    }

    public <T> void addNoExecute(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
        add(step(run, newValue, undo, oldValue), false);
    }

    public void add(UndoManager.Step<?>... steps) {
        add(ImmutableList.copyOf(steps));
    }

    public void add(final List<UndoManager.Step<?>> steps) {
        add(new UndoManager.Step<>(n -> steps.forEach(UndoManager.Step::runRedo), null, n -> steps.forEach(
            UndoManager.Step::runUndo), null), true);
    }

    public boolean canUndo() {
        return !undos.isEmpty();
    }

    public boolean canRedo() {
        return !redos.isEmpty();
    }
}
