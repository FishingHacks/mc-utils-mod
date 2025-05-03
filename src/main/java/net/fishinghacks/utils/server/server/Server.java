package net.fishinghacks.utils.server.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.fishinghacks.utils.common.config.Configs;
import net.fishinghacks.utils.common.connection.Connection;
import net.fishinghacks.utils.common.connection.packets.DisconnectPacket;
import net.fishinghacks.utils.common.connection.packets.InviteNotificationPacket;
import net.fishinghacks.utils.common.connection.packets.Packet;
import net.fishinghacks.utils.common.connection.packets.PacketHandler;
import net.minecraft.FileUtil;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.network.ServerConnectionListener;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.*;

public class Server {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DedicatedServer server;
    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());
    private final Map<String, Connection> playerMap = new HashMap<>();
    private ChannelFuture channel = null;
    private final PacketHandler handler;

    public Server(DedicatedServer server) {
        this.server = server;
        Path cosmeticsDirectory = server.getServerDirectory().resolve("cosmetics");
        try {
            FileUtil.createDirectoriesSafe(cosmeticsDirectory);
        } catch (IOException ignored) {
        }
        handler = new ServerPacketHandlerImpl(this, cosmeticsDirectory);
    }

    public void run() {
        if (!Configs.serverConfig.serverEnabled.get()) return;
        LOGGER.info("Running utils service server");
        channel = startTCPListener(Configs.serverConfig.serverPort.get());
    }

    public void close() {
        if (channel == null) return;
        for (Connection conn : connections)
            try {
                conn.disconnect("exit");
            } catch (Exception ignored) {
            }
        try {
            channel.channel().close().sync();
        } catch (InterruptedException ignored) {
            LOGGER.error("Interruption while closing channel");
        }
        LOGGER.info("Shutting down...");
    }

    public void tick() {
        synchronized (this.connections) {
            Iterator<Connection> connectionIterator = this.connections.iterator();

            while (connectionIterator.hasNext()) {
                Connection conn = connectionIterator.next();
                if (conn.isConnecting()) continue;
                if (!conn.isConnected()) {
                    connectionIterator.remove();
                    String name = conn.getPlayerName();
                    if (name != null) playerMap.remove(conn.getPlayerName());
                    conn.handleDisconnect();
                }
                try {
                    conn.tick();
                } catch (Exception e) {
                    LOGGER.warn("Failed to handle packet for {}",
                        server.logIPs() ? conn.getLoggableAddress() : "IP hidden", e);
                    conn.send(new DisconnectPacket("Internal server error"),
                        Connection.SendListener.thenRun(() -> conn.disconnect("Internal server error")));
                    conn.setReadOnly();
                }
            }
        }
    }

    public void broadcast(Packet<?> packet) {
        for (var connection : connections) connection.send(packet);
    }

    public void registerConnectionToPlayer(String name, Connection conn) {
        Connection old = playerMap.put(name, conn);
        if (old != null) old.disconnectAndNotify("New connection registered");
    }

    @Nullable
    public Connection forPlayer(String name) {
        return playerMap.get(name);
    }

    public void schedule(Runnable runnable) {
        server.schedule(server.wrapRunnable(runnable));
    }

    private ChannelFuture startTCPListener(int port) {
        InetAddress address = (new InetSocketAddress(port)).getAddress();

        Class<? extends ServerSocketChannel> channelClass;
        EventLoopGroup eventLoopGroup;
        if (Epoll.isAvailable()) {
            channelClass = EpollServerSocketChannel.class;
            eventLoopGroup = ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP.get();
            LOGGER.info("Using epoll channel type");
        } else {
            channelClass = NioServerSocketChannel.class;
            eventLoopGroup = ServerConnectionListener.SERVER_EVENT_GROUP.get();
            LOGGER.info("Using default channel type");
        }


        return new ServerBootstrap().channel(channelClass).childHandler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.config().setOption(ChannelOption.TCP_NODELAY, true);

                ChannelPipeline pipeline = ch.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                Connection conn = new Connection(null, null, true, Server.this.handler);
                Server.this.connections.add(conn);
                conn.setupPipeline(pipeline);
                if (Configs.serverConfig.sendServerInvite.get()) conn.runOnceConnected(connection -> connection.send(
                    new InviteNotificationPacket(Configs.serverConfig.serverInviteUrl.get(),
                        Configs.serverConfig.serverInviteName.get(), true)));
            }
        }).group(eventLoopGroup).localAddress(address, port).bind().syncUninterruptibly();
    }
}
