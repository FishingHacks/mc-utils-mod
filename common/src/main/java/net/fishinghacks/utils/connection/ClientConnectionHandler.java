package net.fishinghacks.utils.connection;

import com.google.common.net.HostAndPort;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.fishinghacks.utils.cosmetics.CosmeticHandler;
import net.fishinghacks.utils.gui.PopupScreen;
import net.fishinghacks.utils.gui.ServiceServerSettingsPopup;
import net.fishinghacks.utils.Translation;
import net.fishinghacks.utils.connection.packets.*;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientConnectionHandler {
    public static final Component UNKNOWN_HOST_MESSAGE = Component.translatable("disconnect.genericReason",
        Component.translatable("disconnect.unknownHost"));
    public static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private ChannelFuture future;
    @Nullable
    private Connection connection;
    @Nullable
    private String name;
    private boolean aborted;
    private boolean connecting;
    private final PacketHandler handler = new ClientPacketHandlerImpl();
    private static final ClientConnectionHandler instance = new ClientConnectionHandler();
    private final List<WaitForPacket<?>> packetWaiters = new ArrayList<>();

    public <T extends Packet<?>> WaitForPacket<T> waitForPacket(PacketType<T> type, Consumer<Optional<T>> consumer,
                                                                Predicate<T> filter) {
        return new WaitForPacket<>(type, consumer, filter);
    }

    public <T extends Packet<?>> WaitForPacket<T> waitForPacket(PacketType<T> type, Consumer<Optional<T>> consumer) {
        return new WaitForPacket<>(type, consumer);
    }

    public void cancelWaitForPacket(WaitForPacket<?> waiter) {
        if (waiter.didRun) return;
        for (int i = packetWaiters.size() - 1; i >= 0; --i) {
            if (packetWaiters.get(i) == waiter) packetWaiters.remove(i);
        }
    }

    void registerPacketWaiter(WaitForPacket<?> waiter) {
        packetWaiters.add(waiter);
    }

    public static ClientConnectionHandler getInstance() {
        return instance;
    }

    public String getIp() {
        if (connection == null) return "";
        return connection.getAddress();
    }

    @NotNull
    public String getName() {
        return name != null ? name : "unknown name";
    }

    public void setName(@NotNull String name) {
        this.name = name + " (" + getIp() + ")";
    }

    public Component getFormattedStatus() {
        if (isConnected()) return Translation.ServerConnected.with(
            Component.literal(name == null ? "" : name).withStyle(ChatFormatting.GREEN));
        else if (isConnecting()) return Translation.ServerConnecting.get();
        return Translation.ServerUnconnected.get();
    }

    public boolean isConnecting() {
        return connecting;
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void abort() {
        aborted = true;
        if (future != null) future.cancel(true);
        if (isConnected()) disconnect("abort");
    }

    public void tick() {
        if (connection == null) return;
        if (!connection.isConnected() && !connection.isConnecting()) {
            connection.handleDisconnect();
            connection = null;
            return;
        }

        try {
            connection.tick();
        } catch (Exception e) {
            LOGGER.warn("Failed to tick connection", e);
            this.disconnect("Failed to tick connection");
        }
    }

    @Nullable
    public Connection getConnection() {
        return connection;
    }

    public void connect(ServerAddress address) {
        Thread thread = new Thread(null, () -> connectWrapper(address), "Client Utilsmod Connection");
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    private void connectWrapper(ServerAddress address) {
        if (connecting) return;
        name = address.toString();
        if (connection != null) connection.disconnect("new connection");
        connection = null;
        connecting = true;
        try {
            doConnect(address);
        } catch (Exception e) {
            if (connection != null) connection.disconnect("error");
            connection = null;
            PopupScreen.popup(Component.translatable("disconnect.genericReason", e.getMessage()));
            throw e;
        } finally {
            connecting = false;
            aborted = false;
            future = null;
        }
    }

    public void disconnect(String reason) {
        if (connection == null) return;
        connection.disconnect(reason);
        connection = null;
        name = "";
    }

    private void doConnect(ServerAddress address) {
        connecting = true;
        aborted = false;

        var optionalAddress = ServerNameResolver.DEFAULT.resolveAddress(address)
            .map(ResolvedServerAddress::asInetSocketAddress);
        if (aborted) {
            connecting = false;
            return;
        }
        if (optionalAddress.isEmpty()) {
            LOGGER.error("Couldn't connect to server: Unknown host \"{}\"", address.getHost());
            PopupScreen.popup(UNKNOWN_HOST_MESSAGE);
            return;
        }
        InetSocketAddress addr = optionalAddress.get();
        Connection conn;
        synchronized (ClientConnectionHandler.this) {
            if (aborted) return;
            conn = new Connection(ClientConnectionHandler::onDisconnect, this::onReceive, false, handler);

            Class<? extends SocketChannel> channelClass;
            EventLoopGroup eventLoopGroup;
            if (Epoll.isAvailable()) {
                channelClass = EpollSocketChannel.class;
                eventLoopGroup = net.minecraft.network.Connection.NETWORK_EPOLL_WORKER_GROUP.get();
            } else {
                channelClass = NioSocketChannel.class;
                eventLoopGroup = net.minecraft.network.Connection.NETWORK_WORKER_GROUP.get();
            }

            this.future = new Bootstrap().group(eventLoopGroup).handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel channel) {
                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                    } catch (ChannelException ignored) {
                    }

                    ChannelPipeline pipeline = channel.pipeline();
                    conn.setupPipeline(pipeline);
                }
            }).channel(channelClass).connect(addr.getAddress(), addr.getPort());
        }

        future.syncUninterruptibly();

        synchronized (ClientConnectionHandler.this) {
            future = null;
            if (aborted) {
                conn.disconnect("aborted");
                return;
            }

            this.connection = conn;
            conn.send(new LoginPacket(Minecraft.getInstance().getGameProfile().getName(),
                Minecraft.getInstance().getGameProfile().getId()));
            conn.send(new GetNamePacket());
            conn.runOnceConnected(ignored -> {
                synchronized (this) {
                    name = getIp();
                }
                CosmeticHandler.reloadCosmetics();
            });
        }
    }

    private void onReceive(Packet<?> packet) {
        for (int i = packetWaiters.size() - 1; i >= 0; --i) {
            if (packetWaiters.get(i).run(packet)) packetWaiters.remove(i);
        }
    }

    private static void onDisconnect(String reason) {
        instance.future = null;
        instance.connection = null;
        for(var waiter : getInstance().packetWaiters) waiter.run(null);
        getInstance().packetWaiters.clear();
        CosmeticHandler.reloadCosmetics();
    }

    public static void openSettingsScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ServiceServerSettingsPopup) mc.screen.onClose();
        else Minecraft.getInstance().setScreen(new ServiceServerSettingsPopup(Minecraft.getInstance().screen));
    }

    public static ServerAddress parseAddress(String address) {
        try {
            HostAndPort parsed = HostAndPort.fromString(address).withDefaultPort(25560);
            return parsed.getHost().isEmpty() ? null : new ServerAddress(parsed.getHost(), parsed.getPort());
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }
}
