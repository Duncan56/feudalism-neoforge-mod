package com.example.feudal.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FeudalConfig {

    public static final ModConfigSpec COMMON_CONFIG;

    public static final ModConfigSpec.BooleanValue DEBUG_MODE;

    public static final ModConfigSpec.IntValue BASE_CLAIMS;
    public static final ModConfigSpec.IntValue CLAIMS_PER_MEMBER;
    public static final ModConfigSpec.IntValue MAX_DESC_LENGTH;
    public static final ModConfigSpec.IntValue MAX_MOTD_LENGTH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("debug");
        DEBUG_MODE = builder.comment("Enable verbose debug logging for Feudalism commands")
                .define("DEBUG_MODE", false);
        builder.pop();

        builder.push("town_claims");
        BASE_CLAIMS = builder.defineInRange("BASE_CLAIMS", 8, 1, 5000);
        CLAIMS_PER_MEMBER = builder.defineInRange("CLAIMS_PER_MEMBER", 2, 0, 5000);
        builder.pop();

        builder.push("metadata_limits");
        MAX_DESC_LENGTH = builder.defineInRange("MAX_DESC_LENGTH", 200, 1, 2000);
        MAX_MOTD_LENGTH = builder.defineInRange("MAX_MOTD_LENGTH", 200, 1, 2000);
        builder.pop();

        COMMON_CONFIG = builder.build();
    }
}
