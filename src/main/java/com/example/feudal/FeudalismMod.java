// src/main/java/com/example/feudal/FeudalismMod.java
package com.example.feudal;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(FeudalismMod.MODID) // must match modId in neoforge.mods.toml
public class FeudalismMod {
    public static final String MODID = "feudalism";
    private static final Logger LOGGER = LogUtils.getLogger();

    // NeoForge 1.21.x entrypoint: constructor gets the mod event bus and container
    public FeudalismMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register lifecycle listeners
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        // Register this class on the global NeoForge event bus
        // (only needed because we have @SubscribeEvent methods below)
        NeoForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT);
        LOGGER.info("ITEM >> {}", Items.IRON_INGOT);
        LOGGER.info("The magic number is... {}", 42);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("HELLO FROM CLIENT SETUP");
        // If you want to access Minecraft here, do it in enqueueWork to be safe:
        /*
        event.enqueueWork(() -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            LOGGER.info("MINECRAFT NAME >> {}", mc.getUser().getName());
        });
        */
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Feudalism mod: server starting");
    }
}
