package net.fishinghacks.utils.mixin.client;

import net.fishinghacks.utils.mixin_misc.DisplayEntry;
import net.fishinghacks.utils.modules.misc.Scoreboard;
import net.fishinghacks.utils.modules.ui.PotionEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private static Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER;

    @Inject(method = "displayScoreboardSidebar", cancellable = true, at = @At("HEAD"))
    public void displayScoreboardSidebar(GuiGraphics guiGraphics, Objective objective, CallbackInfo ci) {
        var scoreboardMod = Scoreboard.instance;
        if (!scoreboardMod.enabled) return;
        ci.cancel();
        var font = ((Gui) (Object) this).getFont();

        net.minecraft.world.scores.Scoreboard scoreboard = objective.getScoreboard();
        NumberFormat numberformat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

        DisplayEntry[] displayEntries = scoreboard.listPlayerScores(objective).stream()
            .filter(entry -> !entry.isHidden()).sorted(SCORE_DISPLAY_ORDER).limit(15L).map(entry -> {
                PlayerTeam playerteam = scoreboard.getPlayersTeam(entry.owner());
                Component name = entry.ownerName();
                Component formattedName = PlayerTeam.formatNameForTeam(playerteam, name);
                Component score = entry.formatValue(numberformat);
                int scoreWidth = font.width(score);
                return new DisplayEntry(formattedName, score, scoreWidth);
            }).toArray(DisplayEntry[]::new);

        Component title = objective.getDisplayName();
        int titleWidth = scoreboardMod.TITLE.get() ? font.width(title) : 0;
        int scoreboardWidth = titleWidth;
        int colonWidth = font.width(": ");
        boolean numberWidth = scoreboardMod.NUMBER_WIDTH.get() || scoreboardMod.NUMBERS.get();

        for (DisplayEntry displayEntry : displayEntries) {
            scoreboardWidth = Math.max(scoreboardWidth, font.width(
                displayEntry.name()) + (displayEntry.scoreWidth() > 0 && numberWidth ?
                colonWidth + displayEntry.scoreWidth() : 0));
        }

        boolean textShadow = scoreboardMod.TEXT_SHADOW.get();
        int entryHeight = textShadow ? 10 : 9;
        int numDisplayEntries = displayEntries.length;
        int scoreboardHeight = numDisplayEntries * entryHeight;
        int scoreboardBottom = guiGraphics.guiHeight() / 2 + scoreboardHeight / 3;
        int padRight = 3;
        int scoreboardX = guiGraphics.guiWidth() - scoreboardWidth - padRight;
        int scoreboardRight = guiGraphics.guiWidth() - padRight + 2;
        int scoreboardY = scoreboardBottom - numDisplayEntries * entryHeight;
        guiGraphics.fill(scoreboardX - 2, scoreboardY - 1, scoreboardRight, scoreboardBottom,
            scoreboardMod.BACKGROUND.get().argb());
        if (scoreboardMod.TITLE.get()) {
            var titleTextShadow = scoreboardMod.TEXT_SHADOW_TITLE.get();
            int titleEntryHeight = titleTextShadow ? 10 : 9;
            guiGraphics.fill(scoreboardX - 2, scoreboardY - titleEntryHeight - 1, scoreboardRight, scoreboardY - 1,
                scoreboardMod.TITLE_BACKGROUND.get().argb());
            guiGraphics.drawString(font, title, scoreboardX + scoreboardWidth / 2 - titleWidth / 2,
                scoreboardY - titleEntryHeight, scoreboardMod.TITLE_TEXT_COLOR.get().argb(), titleTextShadow);
        }

        int textColor = scoreboardMod.TEXT_COLOR.get().argb();
        for (int i = 0; i < numDisplayEntries; i++) {
            DisplayEntry displayEntry = displayEntries[i];
            int entryY = scoreboardBottom - (numDisplayEntries - i) * entryHeight;
            guiGraphics.drawString(font, displayEntry.name(), scoreboardX, entryY, -1, textShadow);
            if (scoreboardMod.NUMBERS.get())
                guiGraphics.drawString(font, displayEntry.score(), scoreboardRight - displayEntry.scoreWidth(), entryY,
                    textColor, textShadow);
        }
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    public void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PotionEffects.instance.enabled && !PotionEffects.instance.displayVanilla.get()) ci.cancel();
    }
}
