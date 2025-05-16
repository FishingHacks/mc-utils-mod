package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.Contributors;
import net.fishinghacks.utils.modules.misc.Tablist;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;
import java.util.List;

@Mixin(PlayerTabOverlay.class)
public class TabOverlayMixin {
    @Shadow
    @Nullable
    private Component header;
    @Shadow
    @Nullable
    private Component footer;

    @Inject(method = "decorateName", at = @At("HEAD"))
    public void decorateName(PlayerInfo playerInfo, MutableComponent name, CallbackInfoReturnable<Component> ignored) {
        if (!Tablist.isEnabled || !Tablist.showSuffix) return;
        var role = Contributors.contributors.get(playerInfo.getProfile().getId());
        if (role != null) name.append(" ").append(role.display);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;split(Lnet/minecraft/network/chat/FormattedText;I)Ljava/util/List;"))
    public List<FormattedCharSequence> split(Font font, FormattedText component, int maxWidth) {
        if (!Tablist.isEnabled) return font.split(component, maxWidth);
        if (component == header && !Tablist.showHeader) return List.of();
        else if (component == footer && !Tablist.showFooter) return List.of();
        else return font.split(component, maxWidth);
    }
}
