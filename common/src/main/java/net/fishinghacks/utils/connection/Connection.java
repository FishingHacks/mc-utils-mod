package net.fishinghacks.utils.connection;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import io.netty.channel.*;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import net.fishinghacks.utils.connection.packets.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Connection extends SimpleChannelInboundHandler<Packet<?>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Channel channel;
    private SocketAddress address;
    private final Queue<Consumer<Connection>> pendingActions = Queues.newConcurrentLinkedQueue();
    @Nullable
    private final Consumer<String> onDisconnect;
    @Nullable
    private final Consumer<Packet<?>> onReceive;
    @Nullable
    private String disconnectReason = null;
    private boolean didDisconnect = false;
    private boolean handlingFault = false;
    private final boolean isServer;
    @Nullable
    private String playerName = null;
    private UUID playerId = null;
    private final PacketHandler handler;

    private Instant lastPing = Instant.now();
    private boolean pingSent = true;

    public Connection(@Nullable Consumer<String> onDisconnect, @Nullable Consumer<Packet<?>> onReceive,
                      boolean isServer, PacketHandler handler) {
        this.onDisconnect = onDisconnect;
        this.onReceive = onReceive;
        this.isServer = isServer;
        this.handler = handler;
    }

    public void setPlayer(String playerName, UUID id) {
        if (playerName == null || (this.playerName != null && this.playerId != null) || id == null) return;
        this.playerName = playerName;
        this.playerId = id;
    }

    @Nullable
    public String getPlayerName() {
        return playerName;
    }

    @Nullable
    public UUID getPlayerId() {
        return playerId;
    }

    public void setupPipeline(ChannelPipeline pipeline) {
        pipeline.addLast("splitter", new LineBasedFrameDecoder(0xffffff /* 16 MiB is the max size */))
            .addLast(new FlowControlHandler()).addLast("decoder", new PacketDecoder())
            .addLast("encoder", new PacketEncoder()).addLast("packet_handler", this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    public boolean isConnecting() {
        return channel == null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable exception) {
        boolean doubleFault = handlingFault;
        handlingFault = true;
        if (!channel.isOpen()) return;
        if (exception instanceof ReadTimeoutException e) {
            LOGGER.debug("Timeout", e);
            disconnect("timeout");
            return;
        }
        String reason = "Exception: " + exception;
        if (doubleFault) {
            LOGGER.debug("Double fault", exception);
            disconnect(reason);
            return;
        }
        LOGGER.debug("failed to send packet", exception);
        disconnectAndNotify(reason);

        setReadOnly();
    }

    public String getLoggableAddress() {
        return address.toString();
    }

    public String getAddress() {
        if (address instanceof InetSocketAddress a)
            return a.getHostName() + (a.getPort() == 25560 ? "" : ":" + a.getPort());
        return address.toString();
    }

    public void send(Packet<?> packet) {
        send(packet, null);
    }

    public void send(Packet<?> packet, @Nullable SendListener listener) {
        send(packet, listener, true);
    }

    public void send(Packet<?> packet, @Nullable SendListener listener, boolean flush) {
        if (isConnected()) {
            flushQueue();
            sendPacket(packet, listener, flush);
        } else pendingActions.add(conn -> conn.sendPacket(packet, listener, flush));
    }

    @SuppressWarnings("resource")
    public void sendPacket(Packet<?> packet, @Nullable SendListener listener, boolean flush) {
        if (channel.eventLoop().inEventLoop()) this.doSendPacket(packet, listener, flush);
        else channel.eventLoop().execute(() -> doSendPacket(packet, listener, flush));
    }

    public void doSendPacket(Packet<?> packet, @Nullable SendListener listener, boolean flush) {
        LOGGER.debug("Sending packet {} ({})", packet.type(), packet);
        ChannelFuture channelFuture = flush ? channel.writeAndFlush(packet) : channel.write(packet);
        if (listener != null) {
            channelFuture.addListener(future -> {
                if (future.isSuccess()) listener.onSuccess();
                else {
                    Packet<?> failurePacket = listener.onFailure();
                    if (failurePacket == null) return;
                    channel.writeAndFlush(failurePacket).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                }
            });
        }
        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void setReadOnly() {
        if (channel != null) channel.config().setAutoRead(false);
    }

    public void tick() {
        flushQueue();

        if (Duration.between(lastPing, Instant.now()).getSeconds() > 20 && !pingSent) {
            pingSent = true;
            send(new PingRequestPacket());

        }
        if (!isConnected() && !didDisconnect) handleDisconnect();

        if (channel != null) channel.flush();

    }

    public void handlePing() {
        lastPing = Instant.now();
        pingSent = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        this.address = channel.remoteAddress();
        if (disconnectReason != null) disconnect(disconnectReason);
        this.pingSent = false;
        this.lastPing = Instant.now();
    }

    @SuppressWarnings("resource")
    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }

    }

    public void runOnceConnected(Consumer<Connection> action) {
        if (isConnected()) {
            flushQueue();
            action.accept(this);
        } else pendingActions.add(action);
    }

    private void flushQueue() {
        if (this.channel != null && this.channel.isOpen()) {
            Consumer<Connection> consumer;
            synchronized (this.pendingActions) {
                while ((consumer = this.pendingActions.poll()) != null) {
                    consumer.accept(this);
                }
            }
        }

    }

    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    public void disconnectAndNotify(String reason) {
        if (channel == null) return;
        if (isServer && channel.isOpen())
            send(new DisconnectPacket(reason), SendListener.thenRun(() -> disconnect(reason)));
        else disconnect(reason);
    }

    public void disconnect(String reason) {
        disconnectReason = reason;
        if (channel == null) return;
        channel.close().awaitUninterruptibly();
        handleDisconnect();
    }

    public void handleDisconnect() {
        if (channel == null || channel.isOpen()) return;
        if (didDisconnect) LOGGER.warn("handleDisconnect called twice");
        if (disconnectReason == null) disconnectReason = "Disconnected";
        LOGGER.info("Disconnecting: {}", disconnectReason);
        didDisconnect = true;
        if (onDisconnect != null) onDisconnect.accept(disconnectReason);
        handler.onDisconnect(disconnectReason, this);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) {
        LOGGER.debug("Received packet {} ({})", packet.type(), packet);
        if (!channel.isOpen()) return;
        if (isServer && (playerName == null || playerId == null) && packet.type().needsLogin())
            send(new NotLoggedInPacket());
        else {
            if (onReceive != null) onReceive.accept(packet);
            genericsFtw(packet, handler);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PacketHandler> void genericsFtw(Packet<T> packet, PacketHandler handler) {
        packet.handle(this, (T) handler);
    }

    public interface SendListener {
        static SendListener thenRun(final Runnable runnable) {
            return new SendListener() {
                public void onSuccess() {
                    runnable.run();
                }

                public @Nullable Packet<?> onFailure() {
                    runnable.run();
                    return null;
                }
            };
        }

        static SendListener onFailure(Supplier<Packet<?>> supplier) {
            return new SendListener() {
                @Override
                public @Nullable Packet<?> onFailure() {
                    return supplier.get();
                }
            };
        }

        default void onSuccess() {
        }

        default @Nullable Packet<?> onFailure() {
            return null;
        }
    }
}
