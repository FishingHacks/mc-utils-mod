package net.fishinghacks.utils.platform.services;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface IPlatformHelper {

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isReleaseEnvironment();
    IConfigBuilder createConfigBuilder();
    Set<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation);
}
