package com.example.feudal;

import com.example.feudal.command.FactionCommand;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod("feudalism")
public class FeudalismMod {

    public FeudalismMod() {
        System.out.println("Feudalism mod loaded.");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        FactionCommand.register(event.getDispatcher());
    }
}
