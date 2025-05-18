package net.fishinghacks.utils.config.spec;

public enum RestartType {
    None, Game, World;

    public RestartType with(RestartType other) {
        return switch (this) {
            case None -> other;
            case World -> other == Game ? Game : World;
            case Game -> Game;
        };
    }
}
