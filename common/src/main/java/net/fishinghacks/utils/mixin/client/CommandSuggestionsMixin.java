package net.fishinghacks.utils.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fishinghacks.utils.commands.CommandManager;
import net.fishinghacks.utils.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
    @Shadow
    @Nullable
    private ParseResults<SharedSuggestionProvider> currentParse;
    @Shadow
    @Final
    EditBox input;
    @Shadow
    @Nullable
    private CommandSuggestions.SuggestionsList suggestions;
    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected abstract void updateUsageInfo();

    @Inject(method = "updateCommandInfo", cancellable = true, at = @At(value = "INVOKE", target = "Lcom/mojang" +
        "/brigadier/StringReader;canRead()Z", remap = false))
    public void updateCommandInfo(CallbackInfo ci, @Local StringReader reader) {
        String prefix = Configs.clientConfig.CMD_PREFIX.get();
        if (!reader.canRead(prefix.length()) || !reader.getString().startsWith(prefix, reader.getCursor())) return;
        reader.setCursor(reader.getCursor() + prefix.length());
        if (currentParse == null) {
            assert Minecraft.getInstance().player != null;
            currentParse = CommandManager.getDispatcher()
                .parse(reader, Minecraft.getInstance().player.connection.getSuggestionsProvider());
        }

        int cursor = input.getCursorPosition();
        if (cursor >= prefix.length() && suggestions == null) {
            pendingSuggestions = CommandManager.getDispatcher().getCompletionSuggestions(this.currentParse, cursor);
            pendingSuggestions.thenRun(() -> {
                if (pendingSuggestions.isDone()) this.updateUsageInfo();
            });
        }

        ci.cancel();
    }
}
