package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.parsing.Location;
import net.minecraft.network.chat.Component;

public class MacroException extends Throwable {
    public final Component message;
    public final Location location;

    public MacroException(Component message, Location location) {
        super();
        this.message = message;
        this.location = location;
    }

    public MacroException(Component message, Location location, Throwable cause) {
        this(message, location);
        initCause(cause);
    }
}
