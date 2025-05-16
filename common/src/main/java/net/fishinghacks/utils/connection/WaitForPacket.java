package net.fishinghacks.utils.connection;

import net.fishinghacks.utils.connection.packets.Packet;
import net.fishinghacks.utils.connection.packets.PacketType;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WaitForPacket<T extends Packet<?>> {
    public final PacketType<?> type;
    private final Consumer<Optional<T>> run;
    @Nullable
    private final Predicate<T> filter;
    boolean didRun = false;

    public WaitForPacket(PacketType<?> type, Consumer<Optional<T>> run, @Nullable Predicate<T> filter) {
        this.type = type;
        this.run = run;
        this.filter = filter;
        ClientConnectionHandler.getInstance().registerPacketWaiter(this);
    }

    public WaitForPacket(PacketType<?> type, Consumer<Optional<T>> run) {
        this(type, run, null);
    }

    public void remove() {
        if (didRun) return;
        ClientConnectionHandler.getInstance().cancelWaitForPacket(this);
    }

    @SuppressWarnings("unchecked")
    public boolean run(@Nullable Packet<?> packet) {
        if (didRun) return true;
        if (packet == null) {
            didRun = true;
            run.accept(Optional.empty());
            return true;
        }
        if (packet.type() != type) return false;
        if (filter != null && !filter.test((T) packet)) return false;
        didRun = true;
        run.accept(Optional.of((T) packet));
        return true;
    }
}
