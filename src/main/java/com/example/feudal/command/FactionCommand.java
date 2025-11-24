package com.example.feudal.command;

import com.example.feudal.data.FeudalSavedData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

public class FactionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction")
            .then(Commands.literal("create")
                .then(Commands.argument("name", net.minecraft.commands.arguments.StringArgumentType.word())
                    .executes(ctx -> {
                        String name = net.minecraft.commands.arguments.StringArgumentType.getString(ctx, "name");

                        ServerLevel world = ctx.getSource().getLevel();
                        FeudalSavedData data = world.getDataStorage()
                                .computeIfAbsent(FeudalSavedData::load, FeudalSavedData::new, FeudalSavedData.DATA_NAME);

                        data.setOwner(name, ctx.getSource().getTextName());
                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Faction created: " + name), false);

                        return 1;
                    })
                )
            )
        );
    }
}
