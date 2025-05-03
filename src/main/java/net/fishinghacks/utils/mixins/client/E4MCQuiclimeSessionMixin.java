package net.fishinghacks.utils.mixins.client;

import link.e4mc.E4mcClient;
import link.e4mc.QuiclimeSession;
import net.fishinghacks.utils.client.E4MCStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

// this is very hacky, but we will look for the info log of `Domain assigned: <subdomain>.<region>.e4mc.link` of the
// `e4mc_minecraft` logger.

@Mixin(QuiclimeSession.class)
public class E4MCQuiclimeSessionMixin {
    @Shadow
    @Final
    @Mutable
    private static final Logger LOGGER = new WrappingLogger(LoggerFactory.getLogger(E4mcClient.MOD_ID));

    private record WrappingLogger(Logger parent) implements Logger {
        @Override
        public void info(String format, Object arg) {
            if (format.equals("Domain assigned: {}") && arg instanceof String s) E4MCStore.domainAssigned(s);
            parent.info(format, arg);
        }

        @Override
        public void info(String format, Object... arguments) {
            if (format.equals("Domain assigned: {}") && arguments.length == 1 && arguments[0] instanceof String s)
                E4MCStore.domainAssigned(s);
            parent.info(format, arguments);
        }


        @Override
        public String getName() {
            return parent.getName();
        }

        @Override
        public boolean isTraceEnabled() {
            return parent.isTraceEnabled();
        }

        @Override
        public void trace(String msg) {
            parent.trace(msg);
        }

        @Override
        public void trace(String format, Object arg) {
            parent.trace(format, arg);
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            parent.trace(format, arg1, arg2);
        }

        @Override
        public void trace(String format, Object... arguments) {
            parent.trace(format, arguments);
        }

        @Override
        public void trace(String msg, Throwable t) {
            parent.trace(msg, t);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return parent.isTraceEnabled();
        }

        @Override
        public void trace(Marker marker, String msg) {
            parent.trace(marker, msg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg) {
            parent.trace(marker, format, arg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2) {
            parent.trace(marker, format, arg1, arg2);
        }

        @Override
        public void trace(Marker marker, String format, Object... argArray) {
            parent.trace(marker, format, argArray);
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
            parent.trace(marker, msg, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return parent.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            parent.debug(msg);
        }

        @Override
        public void debug(String format, Object arg) {
            parent.debug(format, arg);
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            parent.debug(format, arg1, arg2);
        }

        @Override
        public void debug(String format, Object... arguments) {
            parent.debug(format, arguments);
        }

        @Override
        public void debug(String msg, Throwable t) {
            parent.debug(msg, t);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return parent.isDebugEnabled(marker);
        }

        @Override
        public void debug(Marker marker, String msg) {
            parent.debug(marker, msg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg) {
            parent.debug(marker, format, arg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2) {
            parent.debug(marker, format, arg1, arg2);
        }

        @Override
        public void debug(Marker marker, String format, Object... arguments) {
            parent.debug(marker, format, arguments);
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            parent.debug(marker, msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return parent.isInfoEnabled();
        }

        @Override
        public void info(String msg) {
            parent.info(msg);
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            parent.info(format, arg1, arg2);
        }

        @Override
        public void info(String msg, Throwable t) {
            parent.info(msg, t);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return parent.isInfoEnabled(marker);
        }

        @Override
        public void info(Marker marker, String msg) {
            parent.info(marker, msg);
        }

        @Override
        public void info(Marker marker, String format, Object arg) {
            parent.info(marker, format, arg);
        }

        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2) {
            parent.info(marker, format, arg1, arg2);
        }

        @Override
        public void info(Marker marker, String format, Object... arguments) {
            parent.info(marker, format, arguments);
        }

        @Override
        public void info(Marker marker, String msg, Throwable t) {
            parent.info(marker, msg, t);
        }

        @Override
        public boolean isWarnEnabled() {
            return parent.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            parent.warn(msg);
        }

        @Override
        public void warn(String format, Object arg) {
            parent.warn(format, arg);
        }

        @Override
        public void warn(String format, Object... arguments) {
            parent.warn(format, arguments);
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            parent.warn(format, arg1, arg2);
        }

        @Override
        public void warn(String msg, Throwable t) {
            parent.warn(msg, t);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return parent.isWarnEnabled(marker);
        }

        @Override
        public void warn(Marker marker, String msg) {
            parent.warn(marker, msg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg) {
            parent.warn(marker, format, arg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2) {
            parent.warn(marker, format, arg1, arg2);
        }

        @Override
        public void warn(Marker marker, String format, Object... arguments) {
            parent.warn(marker, format, arguments);
        }

        @Override
        public void warn(Marker marker, String msg, Throwable t) {
            parent.warn(marker, msg, t);
        }

        @Override
        public boolean isErrorEnabled() {
            return parent.isErrorEnabled();
        }

        @Override
        public void error(String msg) {
            parent.error(msg);
        }

        @Override
        public void error(String format, Object arg) {
            parent.error(format, arg);
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            parent.error(format, arg1, arg2);
        }

        @Override
        public void error(String format, Object... arguments) {
            parent.error(format, arguments);
        }

        @Override
        public void error(String msg, Throwable t) {
            parent.error(msg, t);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return parent.isErrorEnabled(marker);
        }

        @Override
        public void error(Marker marker, String msg) {
            parent.error(marker, msg);
        }

        @Override
        public void error(Marker marker, String format, Object arg) {
            parent.error(marker, format, arg);
        }

        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2) {
            parent.error(marker, format, arg1, arg2);
        }

        @Override
        public void error(Marker marker, String format, Object... arguments) {
            parent.error(marker, format, arguments);
        }

        @Override
        public void error(Marker marker, String msg, Throwable t) {
            parent.error(marker, msg, t);
        }
    }
}
