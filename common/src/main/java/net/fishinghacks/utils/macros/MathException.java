package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.parsing.Location;
import net.minecraft.network.chat.Component;

public class MathException extends Throwable {
    public final Component message;
    public Location location;

    public MathException(Component message, Location location) {
        super();
        this.message = message;
        this.location = location;
    }

    public MathException(Component message, Location location, Throwable cause) {
        this(message, location);
        initCause(cause);
    }
}
