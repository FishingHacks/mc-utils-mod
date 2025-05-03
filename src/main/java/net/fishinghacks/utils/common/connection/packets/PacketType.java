package net.fishinghacks.utils.common.connection.packets;

import net.fishinghacks.utils.common.Utils;

import java.util.Objects;

public final class PacketType<T extends Packet<?>> {
    private final String name;

    private PacketType(String name) {
        this.name = name;
    }

    public static <T extends Packet<?>> PacketType<T> register(String name, Class<T> packetClass) {
        Utils.getLOGGER().info("Registering {} (packetType)", name);
        checkName(name);
        PacketType<T> type = new PacketType<>(name);
        Packets.register(type, packetClass);
        return type;
    }

    // valid characters: a-z_-A-Z0-9
    private static void checkName(String name) {
        for(int i = 0; i < name.length(); ++i) {
            int codePoint = name.codePointAt(i);
            if(codePoint == '-' || codePoint == '_') continue;
            if(codePoint >= 'a' && codePoint <= 'z') continue;
            if(codePoint >= 'A' && codePoint <= 'Z') continue;
            if(codePoint >= '0' && codePoint <= '9') continue;
            throw new IllegalArgumentException("Packet names can only be a-z, A-Z, 0-9, _ or -");
        }
    }

    public boolean needsLogin() {
        return !(this.equals(Packets.LOGIN) || this.equals(Packets.PING) || this.equals(Packets.PING_REQUEST));
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PacketType<?> p) return name.equals(p.name);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "PacketType[" +
            "name=" + name + ']';
    }
}
