package com.example.feudal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.literal;

/**
 * Global Feudalism debug toggle.
 *
 * Commands:
 *   /feudal debug on
 *   /feudal debug off
 *   /feudal debug status
 *
 * Only players/console with permission level 2+ can use it.
 */
public class FeudalDebugCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("Feudalism-Debug");

    private static boolean DEBUG_ENABLED = false;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("feudal")
                        .requires(src -> src.hasPermission(2)) // OPs / console only
                        .then(literal("debug")
                                .then(literal("on").executes(FeudalDebugCommand::debugOn))
                                .then(literal("off").executes(FeudalDebugCommand::debugOff))
                                .then(literal("status").executes(FeudalDebugCommand::debugStatus))
                        )
        );
    }

    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED;
    }

    public static void setDebugEnabled(boolean enabled) {
        DEBUG_ENABLED = enabled;
        LOGGER.info("Feudalism debug mode is now: {}", enabled ? "ON" : "OFF");
    }

    private static int debugOn(CommandContext<CommandSourceStack> ctx) {
        setDebugEnabled(true);
        ctx.getSource().sendSuccess(
                () -> Component.literal("Feudalism debug mode: ON"), true);
        return 1;
    }

    private static int debugOff(CommandContext<CommandSourceStack> ctx) {
        setDebugEnabled(false);
        ctx.getSource().sendSuccess(
                () -> Component.literal("Feudalism debug mode: OFF"), true);
        return 1;
    }

    private static int debugStatus(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(
                () -> Component.literal("Feudalism debug mode is currently: "
                        + (DEBUG_ENABLED ? "ON" : "OFF")), false);
        return 1;
    }
}
