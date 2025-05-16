package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.IConfig;

public class ConfigImpl implements IConfig {
    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void save() {

    }
}
