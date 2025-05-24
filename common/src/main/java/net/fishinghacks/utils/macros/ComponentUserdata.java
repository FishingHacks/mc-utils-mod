package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.exprs.LiteralValue;
import net.minecraft.network.chat.Component;

public record ComponentUserdata(Component child) implements UserData {
    @Override
    public LiteralValue index(LiteralValue value) {
        return LiteralValue.NULL;
    }

    @Override
    public String asString() {
        return "<chat component>";
    }
}
