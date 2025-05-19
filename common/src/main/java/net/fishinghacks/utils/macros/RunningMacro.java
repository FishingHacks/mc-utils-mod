package net.fishinghacks.utils.macros;

import java.util.concurrent.atomic.AtomicBoolean;

public class RunningMacro {
    private final Thread thread;
    private final AtomicBoolean shouldStop;

    RunningMacro(Thread thread, AtomicBoolean shouldStop) {
        this.thread = thread;
        this.shouldStop = shouldStop;
    }

    public void stop() {
        shouldStop.set(true);
        thread.interrupt();
    }
}
