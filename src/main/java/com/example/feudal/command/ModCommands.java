package com.feudalism;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("feudaltest")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal("Feudalism command works!"), false);
                    return 1;
                })
        );
    }
}
