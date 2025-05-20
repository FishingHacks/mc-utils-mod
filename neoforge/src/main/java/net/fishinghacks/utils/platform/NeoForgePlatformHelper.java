package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.config.ConfigBuilderImpl;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.fishinghacks.utils.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.HashSet;
import java.util.Set;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public boolean isModLoaded(String modid) {
        ModList modlist = ModList.get();
        if (modlist != null) return modlist.isLoaded(modid);
        for (var info : FMLLoader.getLoadingModList().getMods())
            if (info.getModId().equals(modid)) return true;
        return false;
    }

    @Override
    public boolean isReleaseEnvironment() {

        return FMLLoader.isProduction();
    }

    @Override
    public IConfigBuilder createConfigBuilder() {
        return new ConfigBuilderImpl();
    }

    @Override
    public Set<Class<?>> scanForAnnotation(Class<? extends Annotation> annotation) {
        Set<Class<?>> set = new HashSet<>();
        ModList.get().getModFiles().stream().map(IModFileInfo::getFile).map(IModFile::getScanResult)
            .flatMap(result -> result.getAnnotatedBy(annotation, ElementType.TYPE))
            .map(ModFileScanData.AnnotationData::clazz).map(Type::getClassName).forEach(v -> {
                try {
                    set.add(Class.forName(v));
                } catch (Exception ignored) {
                }
            });
        return set;
    }
}