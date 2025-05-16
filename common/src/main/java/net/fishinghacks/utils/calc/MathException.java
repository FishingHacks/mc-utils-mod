package net.fishinghacks.utils.calc;

import net.minecraft.network.chat.Component;

public class MathException extends Throwable {
    public final Component message;
    public int characterPos;
    public String source;

    public MathException(Component message, int characterPos, String source) {
        super();
        this.message = message;
        this.characterPos = characterPos;
        this.source = source;
    }

    public MathException(Component message) {
        this(message, 0, "");
    }

    public MathException withCharPos(int pos, String source) {
        this.characterPos = pos;
        this.source = source;
        return this;
    }
}
