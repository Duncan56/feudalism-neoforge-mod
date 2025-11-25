package com.example.feudal.events;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber(modid = "feudalism", bus = Mod.EventBusSubscriber.Bus.GAME)
public class ModEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("feudaltest2")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Feudalism event bus command works!"),
                                    false
                            );
                            return 1;
                        })
        );
    }
}
