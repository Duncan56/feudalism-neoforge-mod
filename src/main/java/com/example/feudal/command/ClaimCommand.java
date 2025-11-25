package com.example.feudal.command;

import com.example.feudal.manager.ChunkOwnershipManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
                .then(Commands.literal("chunk")
                        .executes(ctx -> {
                            ServerLevel world = ctx.getSource().getLevel();
                            BlockPos pos = new BlockPos(ctx.getSource().getPosition());
                            ChunkOwnershipManager manager = ChunkOwnershipManager.get(world);

                            boolean success = manager.claimChunk(pos, ctx.getSource().getTextName());
                            if (success) {
                                ctx.getSource().sendSuccess(() -> Component.literal("Chunk claimed!"), false);
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(Component.literal("Chunk already claimed!"));
                                return 0;
                            }
                        })
                )
        );
    }
}
