// src/main/java/com/example/feudal/events/ModEvents.java
package com.example.feudal.events;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Handles command registration via the NeoForge event bus.
 */
@EventBusSubscriber // defaults to the NeoForge EVENT_BUS, which fires RegisterCommandsEvent
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("feudaltest2")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Feudalism events command works!"),
                                    false
                            );
                            return 1;
                        })
        );
    }
}
