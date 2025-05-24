package net.fishinghacks.utils.macros;

import java.util.concurrent.atomic.AtomicBoolean;

public class RunningMacro {
    private final Thread thread;
    private final AtomicBoolean shouldStop;
    private final String name;

    RunningMacro(Thread thread, AtomicBoolean shouldStop, String name) {
        this.thread = thread;
        this.shouldStop = shouldStop;
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void stop() {
        shouldStop.set(true);
        thread.interrupt();
    }
}
