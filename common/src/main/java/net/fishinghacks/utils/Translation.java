package net.fishinghacks.utils;

import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public enum Translation implements Supplier<Component> {
    ClickUITitle("utils.gui.clickui.title"), DragUITitle("utils.gui.dragui.title"), CmdInvalid(
        "utils.cmd.invalid"), CmdInviteNotConnected("utils.cmd.invite.not_connected"), CmdInviteNotShared(
        "utils.cmd.invite.not_shared"), CmdInviteE4MCMissing("utils.cmd.invite.e4mc_missing"), CmdInviteInvited(
        "utils.cmd.invite.invited"), CmdCalcRunning("utils.cmd.calc.running"), CmdCalcResult(
        "utils.cmd.calc.result"), CmdCalcResultResultLong("utils.cmd.calc.result_long"), CmdCalcInvalidSubcommand(
        "utils.cmd.calc.invalid_subcommand"), CmdCalcInvalidBlockid("utils.cmd.calc.invalid_blockid"), CmdCalcOverworld(
        "utils.cmd.calc.overworld"), CmdCalcNether("utils.cmd.calc.nether"), CmdCalcNetherOverworldUsage(
        "utils.cmd.calc.nether_overworld_usage"), CmdCalcParseIntFailed(
        "utils.cmd.calc.failed_to_parse_int"), CmdCalcParseCustomFuncRemoved(
        "utils.cmd.calc.remove_custom_func"), CmdCalcCustomAddUsage(
        "utils.cmd.calc.add_custom_usage"), CmdCalcCustomAdded("utils.cmd.calc.added_custom"), CmdCalcCraftResult(
        "utils.cmd.calc.craft.result"), CmdCalcCraftOneOf("utils.cmd.calc.craft.one_of"), CmdCalcCraftNotFound(
        "utils.cmd.calc.craft.no_recipe_found"), CmdCalcCraftMultiple(
        "utils.cmd.calc.craft.multiple"), CmdCalcCraftUsage("utils.cmd.calc.craft.usage"), CmdCalcCraftEntry(
        "utils.cmd.calc.craft.entry"), CmdCalcStoppedCalculation("cmd.calc.stopped_calculation"), Error(
        "utils.common.error"), ClickToCopy("utils.common.click_to_copy"), CosmeticReloadAll(
        "utils.cmd.reload_cosmetics.reloaded.all"), CosmeticReloadPlayer(
        "utils.cmd.reload_cosmetics.reloaded.player"), CosmeticReloadUUID(
        "utils.cmd.reload_cosmetics.reloaded.uuid"), NoPlayerFound("utils.cmd.could_not_find_player"), ModuleCategoryUi(
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
        "utils.key.control"), KeyMacControl("utils.key.control.mac"), Mods("utils.common.mods"), Config(
        "utils.common.config"), ClickHere("utils.common.click_here"), GuiConfigTooLong(
        "utils.gui.config.value_too_long"), GuiConfigUnsupported(
        "utils.gui.config.value_unsupported"), GuiConfigSectionButton(
        "utils.gui.config.section_button"), GuiConfigCrumbElement(
        "utils.gui.config.crumb_element"), GuiConfigCrumbSeperator(
        "utils.gui.config.crumb_seperator"), GuiConfigRestartGameTitle(
        "utils.gui.config.restart.game.title"), GuiConfigRestartGameDescription(
        "utils.gui.config.restart.game.text"), GuiConfigRestartWorldTitle(
        "utils.gui.config.restart.world.title"), GuiConfigRestartWorldDescription(
        "utils.gui.config.restart.world.text"), GuiConfigRestartIgnore(
        "utils.gui.config.restart.return"), GuiConfigRestartIgnoreTooltip(
        "utils.gui.config.restart.return.tooltip"), GuiConfigTitle(
        "utils.gui.config.title"), ConfigChatTimeformatDisabled(
        "utils.configuration.chat.time_format.disabled"), ConfigChatTimeformatMinuteSecond(
        "utils.configuration.chat.time_format.minute_second"), ConfigChatTimeformatTwentyFourHours(
        "utils.configuration.chat.time_format.24_hours"), ConfigChatTimeformatTwelveHours(
        "utils.configuration.chat.time_format.12_hours"), ActionTypeToggleModule(
        "utils.action_type.toggle_module"), ActionTypeEnableModule(
        "utils.action_type.enable_module"), ActionTypeDisableModule(
        "utils.action_type.disable_module"), ActionTypeHoldModule("utils.action_type.hold_module"), GuiActionsTitle(
        "utils.gui.actions.title"), GuiActionsRemove("utils.gui.actions.remove"), GuiActionsType(
        "utils.gui.actions.type"), GuiActionsKey("utils.gui.actions.key"), GuiActionsValue("utils.gui.actions.value"),
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

    public IConfigBuilder config(ConfigBuilder builder) {
        return builder.inner().translation(this.key);
    }
}