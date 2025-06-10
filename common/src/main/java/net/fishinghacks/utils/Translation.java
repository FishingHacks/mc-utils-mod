package net.fishinghacks.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

public enum Translation implements Supplier<Component> {
    ClickUITitle("gui.clickui.title"), DragUITitle("gui.dragui.title"), CmdInvalid(
        "cmd.invalid"), CmdInviteNotConnected("cmd.invite.not_connected"), CmdInviteNotShared(
        "cmd.invite.not_shared"), CmdInviteE4MCMissing("cmd.invite.e4mc_missing"), CmdInviteInvited(
        "cmd.invite.invited"), CmdCalcRunning("cmd.calc.running"), CmdCalcResult(
        "cmd.calc.result"), CmdCalcResultResultLong("cmd.calc.result_long"), CmdCalcInvalidSubcommand(
        "cmd.calc.invalid_subcommand"), CmdCalcInvalidBlockid("cmd.calc.invalid_blockid"), CmdCalcOverworld(
        "cmd.calc.overworld"), CmdCalcNether("cmd.calc.nether"), CmdCalcNetherOverworldUsage(
        "cmd.calc.nether_overworld_usage"), CmdCalcParseIntFailed(
        "cmd.calc.failed_to_parse_int"), CmdCalcParseCustomFuncRemoved(
        "cmd.calc.remove_custom_func"), CmdCalcCustomAddUsage("cmd.calc.add_custom_usage"), CmdCalcCustomAdded(
        "cmd.calc.added_custom"), CmdCalcCraftResult("cmd.calc.craft.result"), CmdCalcCraftOneOf(
        "cmd.calc.craft.one_of"), CmdCalcCraftNotFound("cmd.calc.craft.no_recipe_found"), CmdCalcCraftMultiple(
        "cmd.calc.craft.multiple"), CmdCalcCraftUsage("cmd.calc.craft.usage"), CmdCalcCraftEntry(
        "cmd.calc.craft.entry"), CmdCalcStoppedCalculation("cmd.calc.stopped_calculation"), Error(
        "common.error"), ClickToCopy("common.click_to_copy"), CosmeticReloadAll(
        "cmd.reload_cosmetics.reloaded.all"), CosmeticReloadPlayer(
        "cmd.reload_cosmetics.reloaded.player"), CosmeticReloadUUID(
        "cmd.reload_cosmetics.reloaded.uuid"), NoPlayerFound("cmd.could_not_find_player"), ModuleCategoryUi(
        "module_category.ui"), ModuleCategoryMisc("module_category.misc"), ClickUIOpenKey("key.clickui.open"), Invite(
        "invite"), InviteFailed("invite.failed"), InviteNotification("invite.notification"), InviteTrusted(
        "invite.trusted"), InviteGuiTitle("invite.gui_title"), InvitePlayerName("invite.player_name"), InviteAccept(
        "invite.accept"), InviteNotOnWhitelist("invite.not_on_whitelist"), ServerDisconnected(
        "server.disconnected"), ServerUnconnected("server.unconnected"), ServerConnecting(
        "server.connecting"), ServerConnected("server.connected"), ServerConnection("server.connection"), ServerConnect(
        "server.connect"), ServerDisconnect("server.disconnect"), CosmeticGuiTitle(
        "gui.cosmetics.title"), CosmeticGuiCape("gui.cosmetics.cape"), CosmeticGuiElytra(
        "gui.cosmetics.elytra"), CosmeticGuiClear("gui.cosmetics.clear"), CosmeticGuiTypeMCCapes(
        "gui.cosmetics.type.mccapes"), CosmeticGuiTypeServerCapes(
        "gui.cosmetics.type.server_capes"), CosmeticGuiTypeServerModels(
        "gui.cosmetics.type.server_models"), CosmeticGuiLoading("gui.cosmetics.loading"), CosmeticGuiErrored(
        "gui.cosmetics.errored"), Lmb("common.lmb"), Rmb("common.rmb"), Facing("common.facing"), North(
        "common.facing.north"), South("common.facing.south"), West("common.facing.west"), East(
        "common.facing.east"), ServerDisplayPrefix("configuration.server_display.prefix"), SuffixDev(
        "suffix.dev"), SuffixTranslator("suffix.translator"), SuffixBetaTester("suffix.beta_tester"), KeyAlt(
        "key.alt"), KeyShift("key.shift"), KeyControl("key.control"), KeyMacControl("key.control.mac"), Mods(
        "common.mods"), ClickHere("common.click_here"), GuiConfigTooLong(
        "gui.config.value_too_long"), GuiConfigUnsupported("gui.config.value_unsupported"), GuiConfigSectionButton(
        "gui.config.section_button"), GuiConfigCrumbElement("gui.config.crumb_element"), GuiConfigCrumbSeperator(
        "gui.config.crumb_seperator"), GuiConfigRestartGameTitle(
        "gui.config.restart.game.title"), GuiConfigRestartGameDescription(
        "gui.config.restart.game.text"), GuiConfigRestartWorldTitle(
        "gui.config.restart.world.title"), GuiConfigRestartWorldDescription(
        "gui.config.restart.world.text"), GuiConfigRestartIgnore(
        "gui.config.restart.return"), GuiConfigRestartIgnoreTooltip(
        "gui.config.restart.return.tooltip"), GuiConfigTitle("gui.config.title"), ModuleMufflerTitle(
        "configuration.muffler"), ConfigChatTimeformatDisabled(
        "configuration.chat.time_format.disabled"), ConfigChatTimeformatMinuteSecond(
        "configuration.chat.time_format.minute_second"), ConfigChatTimeformatTwentyFourHours(
        "configuration.chat.time_format.24_hours"), ConfigChatTimeformatTwelveHours(
        "configuration.chat.time_format.12_hours"), ActionTypeToggleModule(
        "action_type.toggle_module"), ActionTypeEnableModule("action_type.enable_module"), ActionTypeDisableModule(
        "action_type.disable_module"), ActionTypeHoldModule("action_type.hold_module"), ActionTypeStartMacro(
        "action_type.start_macro"), ActionTypeToggleMacro("action_type.toggle_macro"), GuiActionsTitle(
        "gui.actions.title"), GuiActionsRemove("gui.actions.remove"), GuiActionsType("gui.actions.type"), GuiActionsKey(
        "gui.actions.key"), GuiActionsValue("gui.actions.value"), Open("common.open"), ScreenshotGuiTitle(
        "gui.screenshots.title"), ScreenshotGuiConfirmDelete("gui.screenshots.confirm_delete"), MainGuiButtonMacros(
        "gui.main.macros"), MainGuiButtonSettings("gui.main.settings"), MainGuiButtonModules(
        "gui.main.modules"), CmdMacrosNoneRunning("cmd.macros.no_running"), CmdMacrosRunning(
        "cmd.macros.running"), CmdMacrosExited("cmd.macros.exited"), CmdMacrosStartedRunning(
        "cmd.macros.started_running"), CmdMacrosStopped("cmd.macros.stopped"), CmdMacrosStopUsage(
        "cmd.macros.stop.usage"), CmdMacrosStartUsage("cmd.macros.start.usage"), CmdMacrosFailedToStart(
        "cmd.macros.failed_to_start"), CmdMacrosHelp("cmd.macros.help"), DurabilityDisplayTypeBar(
        "durability_display_type.bar"), DurabilityDisplayTypeText(
        "durability_display_type.text"), DurabilityDisplayTypeBoth(
        "durability_display_type.both"), DurabilityDisplayTypeNone("durability_display_type.none"), CmdWhitelistAdded(
        "cmd.whitelist.added"), CmdWhitelistRemoved("cmd.whitelist.removed"), CmdWhitelistEnabled(
        "cmd.whitelist.enabled"), CmdWhitelistDisabled("cmd.whitelist.disabled"), CmdMufflerSetVolume(
        "cmd.muffler.set_volume"), CmdMufflerGetVolume("cmd.muffler.get_volume"), CmdMufflerGetVolumeMuted(
        "cmd.muffler.get_volume.muted"), CmdMufflerMuted("cmd.muffler.muted"), CmdMufflerUnmuted(
        "cmd.muffler.unmuted"), Search("common.search"), NoResults("common.no_results"),
    ;

    private final String key;
    private final Component component;

    Translation(String key) {
        this.key = "utils." + key;
        component = Component.translatable(this.key);
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
}