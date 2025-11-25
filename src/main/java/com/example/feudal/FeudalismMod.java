package com.feudalism;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@Mod(FeudalismMod.MODID)
public class FeudalismMod {
    public static final String MODID = "feudalism";

    public FeudalismMod(IEventBus modEventBus) {
        // Register mod-level event listeners
        modEventBus.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("feudaltest")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Feudalism command works in 1.21.1!"),
                                    false
                            );
                            return 1;
                        })
        );
    }
}
