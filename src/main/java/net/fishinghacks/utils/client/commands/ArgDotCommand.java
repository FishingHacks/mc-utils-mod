package net.fishinghacks.utils.client.commands;

public abstract class ArgDotCommand extends DotCommand {
    public final void run(String args) {
        this.run(args.split(" "));
    }

    public abstract void run(String[] args);
}
