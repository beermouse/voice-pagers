package com.general.voicepagers;

import net.minecraftforge.common.ForgeConfigSpec;

public class VoicePagersConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue LOCAL_CHAT_RADIUS;
    public static final ForgeConfigSpec.IntValue PAGER_CHAT_RADIUS;

    static {
        BUILDER.push("Радиусы чатов мода Voice Pagers");

        LOCAL_CHAT_RADIUS = BUILDER
                .comment("Радиус действия локального чата (без пейджера) в блоках")
                .defineInRange("localChatRadius", 40, 1, 10000);

        PAGER_CHAT_RADIUS = BUILDER
                .comment("Радиус действия пейджера на одной частоте в блоках")
                .defineInRange("pagerChatRadius", 2000, 1, 100000);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
