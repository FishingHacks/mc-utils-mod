package net.fishinghacks.utils;

import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public enum Translation implements Supplier<Component> {
    ClickUITitle("utils.gui.clickui.title"), DragUITitle("utils.gui.dragui.title"), CmdInviteNotConnected(
        "utils.cmd.invite.not_connected"), CmdInviteNotShared("utils.cmd.invite.not_shared"), CmdInviteE4MCMissing(
        "utils.cmd.invite.e4mc_missing"), CmdInviteInvited("utils.cmd.invite.invited"), CmdCalcResult(
        "utils.cmd.calc.result"), CmdCalcInvalidSubcommand("utils.cmd.calc.invalid_subcommand"), CmdCalcInvalidBlockid(
        "utils.cmd.calc.invalid_blockid"), CmdCalcOverworld("utils.cmd.calc.overworld"), CmdCalcNether(
        "utils.cmd.calc.nether"), CmdCalcNetherOverworldUsage(
        "utils.cmd.calc.nether_overworld_usage"), CmdCalcParseIntFailed(
        "utils.cmd.calc.failed_to_parse_int"), CmdCalcParseCustomFuncRemoved(
        "utils.cmd.calc.remove_custom_func"), CmdCalcCustomAddUsage(
        "utils.cmd.calc.add_custom_usage"), CmdCalcCustomAdded("utils.cmd.calc.added_custom"), CmdCalcCraftResult(
        "utils.cmd.calc.craft.result"), CmdCalcCraftOneOf("utils.cmd.calc.craft.one_of"), CmdCalcCraftNotFound(
        "utils.cmd.calc.craft.no_recipe_found"), CmdCalcCraftMultiple(
        "utils.cmd.calc.craft.multiple"), CmdCalcCraftUsage("utils.cmd.calc.craft.usage"), CmdCalcCraftEntry(
        "utils.cmd.calc.craft.entry"), Error("utils.common.error"), ClickToCopy(
        "utils.common.click_to_copy"), CosmeticReloadAll(
        "utils.cmd.reload_cosmetics.reloaded.all"), CosmeticReloadPlayer(
        "utils.cmd.reload_cosmetics.reloaded.player"), CosmeticReloadUUID(
        "utils.cmd.reload_cosmetics.reloaded.uuid"), NoPlayerFound(
        "utils.cmd.could_not_find_player"), GuiConfigCmdPrefix("utils.gui.config.cmd_prefix"), ModuleCategoryUi(
        "utils.module_category.ui"), ModuleCategoryMisc("utils.module_category.misc"), ClickUIOpenKey(
        "key.clickui.open"), FpsConfigColored("config.module.fps.colored"), InviteFailed("utils.invite.failed"), Invite(
        "utils.invite"), InviteTrusted("utils.invite.trusted"), InviteGuiTitle(
        "utils.invite.gui_title"), InvitePlayerName("utils.invite.player_name"), InviteAccept(
        "utils.invite.accept"), ServerDisconnected("utils.server.disconnected"), ServerUnconnected(
        "utils.server.unconnected"), ServerConnecting("utils.server.connecting"), ServerConnected(
        "utils.server.connected"), ServerConnection("utils.server.connection"), ServerConnect(
        "utils.server.connect"), ServerDisconnect("utils.server.disconnect"), CosmeticGuiTitle(
        "utils.gui.cosmetics.title"), CosmeticGuiCape("utils.gui.cosmetics.cape"), CosmeticGuiElytra(
        "utils.gui.cosmetics.elytra"), CosmeticGuiClear("utils.gui.cosmetics.clear"), CosmeticGuiTypeMCCapes(
        "utils.gui.cosmetics.type.mccapes"), CosmeticGuiTypeServerCapes(
        "utils.gui.cosmetics.type.server_capes"), CosmeticGuiTypeServerModels(
        "utils.gui.cosmetics.type.server_models"), CosmeticGuiLoading(
        "utils.gui.cosmetics.loading"), CosmeticGuiErrored("utils.gui.cosmetics.errored"), Lmb("utils.common.lmb"), Rmb(
        "utils.common.rmb"), Facing("utils.common.facing"), North("utils.common.facing.north"), South(
        "utils.common.facing.south"), West("utils.common.facing.west"), East(
        "utils.common.facing.east"), ServerDisplayPrefix("utils.configuration.server_display.prefix"), SuffixDev(
        "utils.suffix.dev"), SuffixTranslator("utils.suffix.translator"), SuffixBetaTester(
        "utils.suffix.beta_tester"), KeyAlt("utils.key.alt"), KeyShift("utils.key.shift"), KeyControl(
        "utils.key.control"), KeyMacControl("utils.key.control.mac"), Mods("utils.common.mods"),
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

    public IConfigBuilder config(IConfigBuilder builder) {
        return builder.translation(this.key);
    }
}