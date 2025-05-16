package net.fishinghacks.utils.mixin;

import link.e4mc.E4mcClient;
import link.e4mc.QuiclimeSession;
import net.fishinghacks.utils.WrappingLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

// this is very hacky, but we will look for the info log of `Domain assigned: <subdomain>.<region>.e4mc.link` of the
// `e4mc_minecraft` logger.

@Mixin(QuiclimeSession.class)
public class E4MCQuiclimeSessionMixin {
    @Shadow
    @Mutable
    private static final Logger LOGGER = new WrappingLogger(LoggerFactory.getLogger(E4mcClient.MOD_ID));
}