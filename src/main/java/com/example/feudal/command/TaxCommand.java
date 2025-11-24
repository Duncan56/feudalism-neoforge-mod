package com.example.feudal.command;

import com.example.feudal.manager.VassalManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class TaxCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tax")
                .then(Commands.literal("collect")
                        .executes(ctx -> {
                            ServerLevel world = ctx.getSource().getLevel();
                            VassalManager manager = VassalManager.get(world);

                            int collected = manager.collectTaxes(ctx.getSource().getTextName());
                            ctx.getSource().sendSuccess(() -> Component.literal("Collected " + collected + " coins in taxes."), false);
                            return 1;
                        })
                )
        );
    }
}
