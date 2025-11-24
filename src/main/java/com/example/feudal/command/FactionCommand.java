package com.example.feudal.command;

import com.example.feudal.data.FeudalSavedData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.StringArgumentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

public class FactionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("faction")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String factionName = StringArgumentType.getString(ctx, "name");

                                    ServerLevel world = ctx.getSource().getLevel();
                                    FeudalSavedData data = world.getDataStorage()
                                            .computeIfAbsent(FeudalSavedData::load, FeudalSavedData::new, FeudalSavedData.DATA_NAME);

                                    if (data.getOwner(factionName) != null && !data.getOwner(factionName).isEmpty()) {
                                        ctx.getSource().sendFailure(Component.literal("Faction already exists!"));
                                        return 0;
                                    }

                                    data.setOwner(factionName, ctx.getSource().getTextName());
                                    ctx.getSource().sendSuccess(() -> Component.literal("Faction created: " + factionName), false);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            ServerLevel world = ctx.getSource().getLevel();
                            FeudalSavedData data = world.getDataStorage()
                                    .computeIfAbsent(FeudalSavedData::load, FeudalSavedData::new, FeudalSavedData.DATA_NAME);

                            StringBuilder sb = new StringBuilder("Factions:\n");
                            data.getFactionMap().forEach((name, owner) -> sb.append(name).append(" (").append(owner).append(")\n"));

                            ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                            return 1;
                        })
                )
        );
    }
}
