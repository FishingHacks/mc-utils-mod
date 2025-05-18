package net.fishinghacks.utils.gui.configuration;

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

    private final List<Step<?>> undos = new ArrayList<>();
    private final List<Step<?>> redos = new ArrayList<>();


    public void undo() {
        if (canUndo()) {
            Step<?> step = undos.removeLast();
            step.runUndo();
            redos.add(step);
        }
    }

    public void redo() {
        if (canRedo()) {
            Step<?> step = redos.removeLast();
            step.runRedo();
            undos.add(step);
        }
    }

    private void add(Step<?> step) {
        undos.add(step);
        redos.clear();
        step.runRedo();
    }

    public <T> Step<T> step(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
        return new Step<>(run, newValue, undo, oldValue);
    }

    public <T> void add(Consumer<T> run, T newValue, Consumer<T> undo, T oldValue) {
        add(step(run, newValue, undo, oldValue));
    }
    public <T> void add(Consumer<T> apply, T newValue, T oldValue) {
        add(step(apply, newValue, apply, oldValue));
    }

    public void add(Step<?>... steps) {
        add(ImmutableList.copyOf(steps));
    }

    public void add(final List<Step<?>> steps) {
        add(new Step<>(n -> steps.forEach(Step::runRedo), null, n -> steps.forEach(
            Step::runUndo), null));
    }

    public boolean canUndo() {
        return !undos.isEmpty();
    }

    public boolean canRedo() {
        return !redos.isEmpty();
    }
}
