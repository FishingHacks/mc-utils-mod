package net.fishinghacks.utils.modules;

public interface ModuleManagerLike {
    boolean isEnabled(String module);
    void setEnabled(String module, boolean value);
}
