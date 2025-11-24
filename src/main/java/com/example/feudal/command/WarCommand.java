package com.example.feudal.command;

import com.example.feudal.manager.WarManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.StringArgumentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;

public class WarCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("war")
                .then(Commands.literal("declare")
                        .then(Commands.argument("faction", StringArgumentType.word())
                                .executes(ctx -> {
                                    String targetFaction = StringArgumentType.getString(ctx, "faction");
                                    ServerLevel world = ctx.getSource().getLevel();
                                    WarManager manager = WarManager.get(world);

                                    boolean success = manager.declareWar(ctx.getSource().getTextName(), targetFaction);
                                    if (success) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("War declared against " + targetFaction), false);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("Cannot declare war!"));
                                        return 0;
                                    }
                                })
                        )
                )
                .then(Commands.literal("end")
                        .then(Commands.argument("faction", StringArgumentType.word())
                                .executes(ctx -> {
                                    String targetFaction = StringArgumentType.getString(ctx, "faction");
                                    ServerLevel world = ctx.getSource().getLevel();
                                    WarManager manager = WarManager.get(world);

                                    boolean success = manager.endWar(ctx.getSource().getTextName(), targetFaction);
                                    if (success) {
                                        ctx.getSource().sendSuccess(() -> Component.literal("War ended with " + targetFaction), false);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("No active war with that faction!"));
                                        return 0;
                                    }
                                })
                        )
                )
        );
    }
}
