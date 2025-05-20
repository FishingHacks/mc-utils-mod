package net.fishinghacks.utils;

import net.fishinghacks.utils.platform.Services;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Telemetry {
    private static final HashMap<String, Entry> entries = new HashMap<>();

    @NotNull
    private static Entry getEntry(String name) {
        if (Services.PLATFORM.isReleaseEnvironment())
            throw new IllegalStateException("all places should check for development env.");
        return entries.computeIfAbsent(name, Entry::new);
    }

    public static void start(String name) {
        if (Services.PLATFORM.isReleaseEnvironment()) return;
        getEntry(name).start();
    }

    public static void stop(String name) {
        if (Services.PLATFORM.isReleaseEnvironment()) return;
        getEntry(name).stop();
    }

    public static void shutdown() {
        if (Services.PLATFORM.isReleaseEnvironment()) return;
        entries.values().forEach(e -> {
            if (e.isRunning) e.stop();
            e.dumpTelemetry();
        });
    }

    public static void registerRender(String name, int time) {
        if (Services.PLATFORM.isReleaseEnvironment()) return;
        getEntry(name).add(time);
    }

    private static class Entry {
        private double renderMean = 0;
        private double M2 = 0;
        private int amount = 0;
        private final List<Integer> top10PercentRenders = new ArrayList<>();
        private long totalTimeInScreen = 0L;
        private long lastEnter = 0L;
        private final String name;
        public boolean isRunning = false;

        public Entry(String name) {
            this.name = name;
        }

        public void start() {
            if (isRunning) return;
            isRunning = true;
            lastEnter = Util.getMillis();
        }

        public void stop() {
            if (!isRunning) return;
            isRunning = false;
            totalTimeInScreen += Util.getMillis() - lastEnter;
            dumpTelemetry(Util.getMillis() - lastEnter);
        }

        public void dumpTelemetry() {
            dumpTelemetry(null);
        }

        public void dumpTelemetry(@Nullable Long lastTimeOnScreen) {
            double meanFps = ((double) amount / totalTimeInScreen) * 1000.0;
            Constants.LOG.info("=== Telemetry for {}: ===", name);
            if (lastTimeOnScreen != null)
                Constants.LOG.info("Time spent on the screen: {}ms, Total time: {}ms. Mean FPS: {}", lastTimeOnScreen,
                    totalTimeInScreen, meanFps);
            else Constants.LOG.info("Total time spent on screen: {}ms, Mean FPS: {}", totalTimeInScreen, meanFps);
            Constants.LOG.info("Mean render time: {}ms, Standard deviation: {}ms", mean(), stddev());
            Constants.LOG.info("For the longest 10% of all render times: mean: {}ms, standard deviation: {}ms",
                top10Mean(), top10StdDev());
        }

        public void add(int renderTime) {
            amount++;
            int top10PEntries = Math.max(1, amount / 10);
            double newMean = renderMean + ((double) renderTime - renderMean) / amount;
            M2 += ((double) renderTime - newMean) * ((double) renderTime - renderMean);
            renderMean = newMean;
            if (top10PEntries > top10PercentRenders.size()) top10PercentRenders.add(renderTime);
            else if (top10PercentRenders.getFirst() < renderTime) top10PercentRenders.add(renderTime);
            else return;
            top10PercentRenders.sort(Integer::compareTo);
        }

        public double mean() {
            return renderMean;
        }

        public double stddev() {
            return amount <= 1 ? 0 : Math.sqrt(M2 / (amount - 1));
        }

        public double top10Mean() {
            if (top10PercentRenders.isEmpty()) return 0.0;
            return (double) top10PercentRenders.stream().reduce(0, Integer::sum) / top10PercentRenders.size();
        }

        public double top10StdDev() {
            if (top10PercentRenders.isEmpty()) return 0.0;
            var mean = top10Mean();
            return Math.sqrt(top10PercentRenders.stream().map(v -> (double) v - mean).map(v -> Math.pow(v, 2))
                .reduce(0.0, Double::sum) / top10PercentRenders.size());
        }
    }
}
