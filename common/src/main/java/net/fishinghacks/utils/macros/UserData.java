package net.fishinghacks.utils.macros;

import net.fishinghacks.utils.macros.exprs.LiteralValue;

public interface UserData {
    default String asString() {
        return "<user data>";
    }
    LiteralValue index(LiteralValue value);
}
