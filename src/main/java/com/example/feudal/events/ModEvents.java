package com.example.feudal.events;

import com.example.feudal.commands.FeudalDebugCommand;
import com.example.feudal.commands.TownCommand;
import com.example.feudal.town.TownStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * COMMAND-ONLY VERSION
 *
 * Loads & saves towns + registers commands.
 * No protections, no territory checks.
 */
@EventBusSubscriber(modid = "feudalism")
public class ModEvents {

    // -------------------------------------------------------------------------
    // LOAD / SAVE
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        TownStorage.loadAll();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        TownStorage.saveAll();
    }

    // -------------------------------------------------------------------------
    // COMMAND REGISTRATION
    // -------------------------------------------------------------------------

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TownCommand.register(event.getDispatcher());
        FeudalDebugCommand.register(event.getDispatcher());
    }
}
