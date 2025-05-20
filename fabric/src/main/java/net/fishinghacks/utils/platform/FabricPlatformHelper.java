package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.config.ConfigBuilderImpl;
import net.fishinghacks.utils.modules.ModuleManager;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.fishinghacks.utils.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class FabricPlatformHelper implements IPlatformHelper {
    private static void loadAllClasses(ClassLoader loader, String parentPath, String parentName, String name,
                                       Set<Class<?>> classSet, Class<? extends Annotation> annotation) {
        if (name.endsWith(".class")) {
            try {
                var clazz = Class.forName(parentName + "." + name.substring(0, name.lastIndexOf('.')));
                if (clazz.getAnnotation(annotation) != null) classSet.add(clazz);
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            var newPath = parentPath + "/" + name;
            var newName = parentName + "." + name;
            try (var stream = loader.getResourceAsStream(newPath)) {
                if (stream == null) return;
                var reader = new BufferedReader(new InputStreamReader(stream));
                reader.lines().forEach(v -> loadAllClasses(loader, newPath, newName, v, classSet, annotation));
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Set<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation) {
        Set<Class<?>> classSet = new HashSet<>();

        var loader = ModuleManager.class.getClassLoader();
        for (var pkg : loader.getDefinedPackages()) {
            var name = pkg.getName().replaceAll("[.]", "/");
            try (var stream = loader.getResourceAsStream(name)) {
                if (stream == null) continue;
                var reader = new BufferedReader(new InputStreamReader(stream));
                reader.lines().forEach(v -> loadAllClasses(loader, name, pkg.getName(), v, classSet, annotation));
            } catch (Exception ignored) {
            }
        }

        return classSet;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isReleaseEnvironment() {

        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public IConfigBuilder createConfigBuilder() {
        return new ConfigBuilderImpl();
    }
}
