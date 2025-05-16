package net.fishinghacks.utils.connection.packets;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;

public class Packets {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<String, Class<? extends Packet<?>>> packets = new HashMap<>();

    public static <T extends Packet<?>> void register(PacketType<T> ty, Class<T> packetClass) {
        LOGGER.info("Registering {} (Packets)", ty.name());
        if (packets.containsKey(ty.name()))
            throw new RuntimeException("Packet Class " + ty.name() + " was registered multiple times");
        packets.put(ty.name(), packetClass);
    }

    public static <T extends Packet<?>> PacketType<T> register(String name, Class<T> packetClass) {
        return PacketType.register(name, packetClass);
    }

    @Nullable
    public static Class<? extends Packet<?>> getPacket(String name) {
        return packets.get(name);
    }

    public static final PacketType<DisconnectPacket> DISCONNECT = register("disconnect", DisconnectPacket.class);
    public static final PacketType<PingPacket> PING = register("ping", PingPacket.class);
    public static final PacketType<PingRequestPacket> PING_REQUEST = register("ping_request", PingRequestPacket.class);
    public static final PacketType<LoginPacket> LOGIN = register("login", LoginPacket.class);
    public static final PacketType<InvitePacket> INVITE = register("invite", InvitePacket.class);
    public static final PacketType<InviteNotificationPacket> INVITE_NOTIFICATION = register("invite_notification",
        InviteNotificationPacket.class);
    public static final PacketType<InviteFailedPacket> INVITE_FAILED = register("invite_failed",
        InviteFailedPacket.class);
    public static final PacketType<NotLoggedInPacket> NOT_LOGGED_IN = register("not_logged_in",
        NotLoggedInPacket.class);
    public static final PacketType<GetNamePacket> GET_NAME = register("get_name", GetNamePacket.class);
    public static final PacketType<GetNameReplyPacket> GET_NAME_REPLY = register("get_name_reply",
        GetNameReplyPacket.class);
    public static final PacketType<CosmeticsListRequestPacket> LIST_REQUEST = register("list_cosmetics_request",
        CosmeticsListRequestPacket.class);
    public static final PacketType<CapesListPacket> LIST_CAPES = register("capes_list", CapesListPacket.class);
    public static final PacketType<ModelsListPacket> LIST_MODELS = register("models_list", ModelsListPacket.class);
    public static final PacketType<CosmeticRequestPacket> COSMETIC_REQUEST = register("cosmetic_request",
        CosmeticRequestPacket.class);
    public static final PacketType<CosmeticReplyPacket> COSMETIC_REPLY = register("cosmetic_reply",
        CosmeticReplyPacket.class);
    public static final PacketType<GetCosmeticForPlayer> GET_PLAYER_COSMETIC = register("get_player_cosmetic",
        GetCosmeticForPlayer.class);
    public static final PacketType<GetCosmeticForPlayerReply> GET_PLAYER_COSMETIC_REPLY = register(
        "get_player_cosmetic_reply", GetCosmeticForPlayerReply.class);
    public static final PacketType<SetCapePacket> SET_CAPE = register("set_cape", SetCapePacket.class);
    public static final PacketType<SetModelsPacket> SET_MODELS = register("set_models", SetModelsPacket.class);
    public static final PacketType<AddModelPacket> ADD_MODEL = register("add_model", AddModelPacket.class);
    public static final PacketType<RemoveModelPacket> REMOVE_MODEL = register("remove_model", RemoveModelPacket.class);
    public static final PacketType<ReloadCosmeticForPlayer> RELOAD_COSMETIC = register("reload_cosmetic",
        ReloadCosmeticForPlayer.class);
}
