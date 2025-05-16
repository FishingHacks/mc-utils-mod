package net.fishinghacks.utils.mixin;

import com.mojang.brigadier.ParseResults;
import net.fishinghacks.utils.E4MCStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(method = "performCommand", at = @At("HEAD"))
    public void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        E4MCStore.onCommandExecuted(parseResults);
    }
}
