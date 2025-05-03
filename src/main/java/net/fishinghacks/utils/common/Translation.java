package net.fishinghacks.utils.common;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

public enum Translation implements Supplier<Component> {
    ClickUITitle("utils.gui.clickui.title"),
    DragUITitle("utils.gui.dragui.title"),
    CmdSayHelp("utils.cmd.say.help"),
    CosmeticReloadAll("utils.cmd.reload_cosmetics.reloaded.all"),
    CosmeticReloadPlayer("utils.cmd.reload_cosmetics.reloaded.player"),
    CosmeticReloadUUID("utils.cmd.reload_cosmetics.reloaded.uuid"),
    NoPlayerFound("utils.cmd.could_not_find_player"),
    GuiConfigCmdPrefix("utils.gui.config.cmd_prefix"),
    ModuleCategoryUi("utils.module_category.ui"),
    ModuleCategoryMisc("utils.module_category.misc"),
    ClickUIOpenKey("key.clickui.open"),
    FpsConfigColored("config.module.fps.colored"),
    InviteFailed("utils.invite.failed"),
    Invite("utils.invite"),
    InviteTrusted("utils.invite.trusted"),
    InviteGuiTitle("utils.invite.gui_title"),
    InvitePlayerName("utils.invite.player_name"),
    InviteAccept("utils.invite.accept"),
    ServerDisconnected("utils.server.disconnected"),
    ServerUnconnected("utils.server.unconnected"),
    ServerConnecting("utils.server.connecting"),
    ServerConnected("utils.server.connected"),
    ServerConnection("utils.server.connection"),
    ServerConnect("utils.server.connect"),
    ServerDisconnect("utils.server.disconnect"),
    CosmeticGuiTitle("utils.gui.cosmetics.title"),
    CosmeticGuiCape("utils.gui.cosmetics.cape"),
    CosmeticGuiElytra("utils.gui.cosmetics.elytra"),
    ;

    private final String key;
    private final Component component;

    Translation(String key) {
        this.key = key;
        component = Component.translatable(key);
    }

    public String key() {
        return key;
    }

    public MutableComponent with(Object... args) {
        return Component.translatable(this.key, args);
    }

    public Component get() {
        return component;
    }

    public ModConfigSpec.Builder config(ModConfigSpec.Builder builder) {
        return builder.translation(this.key);
    }
}