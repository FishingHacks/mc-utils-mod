package net.fishinghacks.utils.config.spec;

public enum ConfigType {
    Client, Server;

    public String extension() {
        return switch (this) {
            case Client -> "client";
            case Server -> "server";
        };
    }

    public boolean isClient() {
        return this == ConfigType.Client;
    }
    public boolean isServer() {
        return this == ConfigType.Server;
    }
}
