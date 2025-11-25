package com.example.feudal;

import com.example.feudal.config.FeudalConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(FeudalismMod.MODID)
public class FeudalismMod {

    public static final String MODID = "feudalism";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FeudalismMod(IEventBus modEventBus, ModContainer modContainer) {

        // Register config ONCE (correct)
        modContainer.registerConfig(ModConfig.Type.COMMON, FeudalConfig.COMMON_CONFIG);

        LOGGER.info("[Feudalism] Config registered successfully");
    }
}
