package net.fishinghacks.utils.client;

import net.fishinghacks.utils.common.Translation;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Contributors {
    public static final Map<UUID, Type> contributors = new HashMap<>();

    static {
        // 2a312138-2b30-4a6c-b43d-784d0d755e44
        contributors.put(new UUID(0x2a3121382b304a6cL, 0xb43d784d0d755e44L), Type.Dev);
        // f1902924-505a-401a-b62c-15c7ca1906f2
        contributors.put(new UUID(0xf1902924505a401aL, 0xb62c15c7ca1906f2L), Type.BetaTester);
    }

    public enum Type {
        Dev(Translation.SuffixDev), Translator(Translation.SuffixTranslator), BetaTester(Translation.SuffixBetaTester);

        public final Component display;

        Type(Translation translation) {
            display = translation.get();
        }
    }
}
