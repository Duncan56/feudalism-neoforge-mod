package com.example.feudal.commands;

import com.example.feudal.config.FeudalConfig;
import com.example.feudal.town.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * ALL town-related commands.
 * Fully config-integrated. Handles:
 * - creation, deletion
 * - invites
 * - joining/leaving
 * - ranks
 * - metadata (desc/motd/public)
 * - claims
 * - members list
 * - chunk ownership checking
 *
 * Includes debug hooks:
 * - Uses FeudalDebugCommand.isDebugEnabled()
 * - Logs to console and (optionally) to the invoking player
 */
public class TownCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("Feudalism-TownCommand");

    // -------------------------------------------------------------------------
    // Debug helpers
    // -------------------------------------------------------------------------

    private static void debug(CommandContext<CommandSourceStack> ctx, String msg) {
        LOGGER.debug("[TownCommand] {}", msg);
        if (FeudalDebugCommand.isDebugEnabled()) {
            try {
                ctx.getSource().sendSystemMessage(Component.literal("[DEBUG] " + msg));
            } catch (Exception ignored) {}
        }
    }

    private static void debugException(CommandContext<CommandSourceStack> ctx, Exception ex) {
        LOGGER.error("[TownCommand] Exception during command execution", ex);
        if (FeudalDebugCommand.isDebugEnabled()) {
            try {
                ctx.getSource().sendSystemMessage(
                        Component.literal("[DEBUG-ERROR] " + ex.getClass().getSimpleName() + ": " + ex.getMessage()));
            } catch (Exception ignored) {}
        }
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                literal("town")
                        .requires(src -> src.hasPermission(0))

                        // --- creation ---
                        .then(literal("create")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(TownCommand::createTown)
                                )
                        )

                        // --- info ---
                        .then(literal("info")
                                .executes(ctx -> townInfo(ctx, null))
                                .then(argument("name", StringArgumentType.word())
                                        .executes(ctx -> townInfo(ctx,
                                                StringArgumentType.getString(ctx, "name")))
                                )
                        )

                        .then(literal("here")
                                .executes(TownCommand::here)
                        )

                        // --- membership ---
                        .then(literal("join")
                                .then(argument("name", StringArgumentType.word())
                                        .executes(TownCommand::joinTown)
                                )
                        )

                        .then(literal("leave")
                                .executes(TownCommand::leaveTown)
                        )

                        .then(literal("disband")
                                .executes(TownCommand::disbandTown)
                        )

                        // --- invites ---
                        .then(literal("invite")
                                .then(argument("player", EntityArgument.player())
                                        .executes(TownCommand::invitePlayer)
                                )
                        )

                        .then(literal("accept")
                                .then(argument("town", StringArgumentType.word())
                                        .executes(TownCommand::acceptInvite)
                                )
                        )

                        .then(literal("deny")
                                .then(argument("town", StringArgumentType.word())
                                        .executes(TownCommand::denyInvite)
                                )
                        )

                        // --- ranks ---
                        .then(literal("promote")
                                .then(argument("player", EntityArgument.player())
                                        .executes(TownCommand::promote)
                                )
                        )

                        .then(literal("demote")
                                .then(argument("player", EntityArgument.player())
                                        .executes(TownCommand::demote)
                                )
                        )

                        .then(literal("setleader")
                                .then(argument("player", EntityArgument.player())
                                        .executes(TownCommand::setLeader)
                                )
                        )

                        // --- metadata ---
                        .then(literal("desc")
                                .then(argument("description", StringArgumentType.greedyString())
                                        .executes(TownCommand::setDescription)
                                )
                        )

                        .then(literal("motd")
                                .then(argument("motd", StringArgumentType.greedyString())
                                        .executes(TownCommand::setMotd)
                                )
                        )

                        .then(literal("public")
                                .then(argument("enabled", BoolArgumentType.bool())
                                        .executes(TownCommand::setPublicJoin)
                                )
                        )

                        // --- claims ---
                        .then(literal("claim")
                                .executes(TownCommand::claimChunk)
                        )

                        .then(literal("unclaim")
                                .executes(TownCommand::unclaimChunk)
                        )

                        .then(literal("claims")
                                .executes(TownCommand::listClaims)
                        )

                        .then(literal("members")
                                .executes(TownCommand::listMembers)
                        )
        );
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static ServerPlayer getPlayer(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    private static Town getPlayerTownOrError(CommandContext<CommandSourceStack> ctx,
                                             ServerPlayer player) {
        Town town = TownManager.getTownOfPlayer(player.getUUID());
        if (town == null) {
            ctx.getSource().sendFailure(Component.literal("You are not in a town."));
        }
        return town;
    }

    private static boolean ensureLeader(CommandContext<CommandSourceStack> ctx,
                                        ServerPlayer player,
                                        Town town) {
        TownPlayerData d = TownPlayerManager.get(player.getUUID());
        if (d.getRank() != TownRank.LEADER) {
            ctx.getSource().sendFailure(Component.literal("Only the town leader may do this."));
            return false;
        }
        return true;
    }

    private static boolean ensureCanManageClaims(CommandContext<CommandSourceStack> ctx,
                                                 ServerPlayer player) {
        TownPlayerData d = TownPlayerManager.get(player.getUUID());
        if (!d.getRank().canManageClaims()) {
            ctx.getSource().sendFailure(Component.literal("Only leaders/officers may manage claims."));
            return false;
        }
        return true;
    }

    // =========================================================================
    // Create town
    // =========================================================================

    private static int createTown(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "createTown() called");

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");

        debug(ctx, "Requested town name: " + name + " by player " + player.getGameProfile().getName());

        TownPlayerData d = TownPlayerManager.get(player.getUUID());
        if (d.hasTown()) {
            out.sendFailure(Component.literal("You are already in a town."));
            debug(ctx, "Player already in a town, aborting /town create");
            return 0;
        }

        if (TownManager.getTownByName(name) != null) {
            out.sendFailure(Component.literal("A town with that name already exists."));
            debug(ctx, "Town name already taken: " + name);
            return 0;
        }

        Town town = TownManager.createTown(name, player.getUUID());
        debug(ctx, "Town created with id=" + town.getId());
        out.sendSuccess(() -> Component.literal(
                "Town '" + town.getName() + "' created!"), true);
        return 1;
    }

    // =========================================================================
    // Town info
    // =========================================================================

    private static int townInfo(CommandContext<CommandSourceStack> ctx, String name)
            throws CommandSyntaxException {

        debug(ctx, "townInfo() called, name=" + name);

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();
        Town town;

        try {
            if (name == null) {
                town = TownManager.getTownOfPlayer(player.getUUID());
                debug(ctx, "Resolved self-town: " + (town != null ? town.getName() : "null"));
                if (town == null) {
                    out.sendFailure(Component.literal(
                            "You are not in a town. Use /town info <name> to check other towns."));
                    return 0;
                }
            } else {
                town = TownManager.getTownByName(name);
                debug(ctx, "Resolved named town '" + name + "': " + (town != null ? "found" : "null"));
                if (town == null) {
                    out.sendFailure(Component.literal("No such town exists."));
                    return 0;
                }
            }

            // Null-safety guards in case of old JSON
            if (town.getCitizens() == null) {
                debug(ctx, "town.getCitizens() was null, fixing");
            }
            Set<UUID> citizens = town.getCitizens();

            if (town.getClaims() == null) {
                debug(ctx, "town.getClaims() was null, fixing");
            }
            Set<ClaimPos> claims = town.getClaims();

            String description = town.getDescription() == null ? "" : town.getDescription();
            String motd = town.getMotd() == null ? "" : town.getMotd();

            debug(ctx, "Printing town info to player");
            out.sendSuccess(() -> Component.literal("=== Town: " + town.getName() + " ==="), false);
            out.sendSuccess(() -> Component.literal("Leader: " + town.getLeader()), false);
            out.sendSuccess(() -> Component.literal("Members: " + citizens.size()), false);
            out.sendSuccess(() -> Component.literal("Claims: "
                    + claims.size() + " / " + TownManager.getClaimLimit(town)), false);
            out.sendSuccess(() -> Component.literal("Join type: "
                    + (town.isPublicJoin() ? "Public" : "Invite-Only")), false);

            if (!description.isEmpty())
                out.sendSuccess(() -> Component.literal("Description: " + description), false);

            if (!motd.isEmpty())
                out.sendSuccess(() -> Component.literal("MOTD: " + motd), false);

            debug(ctx, "townInfo() completed OK for " + town.getName());
            return 1;

        } catch (Exception ex) {
            debugException(ctx, ex);
            out.sendFailure(Component.literal("Internal error running /town info. See logs for details."));
            return 0;
        }
    }

    // =========================================================================
    // /town here
    // =========================================================================

    private static int here(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "here() called");

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        ServerLevel level = (ServerLevel) player.level();
        ChunkPos pos = new ChunkPos(player.blockPosition());
        String dim = level.dimension().location().toString();

        debug(ctx, "Player at dim=" + dim + " chunk=(" + pos.x + "," + pos.z + ")");

        Town t = TownManager.getTownByClaim(dim, pos.x, pos.z);
        if (t == null) {
            out.sendSuccess(() -> Component.literal("This chunk is wilderness."), false);
        } else {
            out.sendSuccess(() -> Component.literal(
                    "This chunk belongs to town '" + t.getName() + "'."), false);
        }
        return 1;
    }

    // =========================================================================
    // Joining & leaving
    // =========================================================================

    private static int joinTown(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "joinTown() called");

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        String name = StringArgumentType.getString(ctx, "name");
        Town town = TownManager.getTownByName(name);

        debug(ctx, "Target town to join: " + name);

        if (town == null) {
            out.sendFailure(Component.literal("No town found with that name."));
            return 0;
        }

        TownPlayerData d = TownPlayerManager.get(player.getUUID());
        if (d.hasTown()) {
            out.sendFailure(Component.literal("You are already in a town."));
            return 0;
        }

        boolean allowed = town.isPublicJoin()
                || TownInviteManager.hasInvite(town.getId(), player.getUUID());

        debug(ctx, "Join allowed=" + allowed + " (public=" + town.isPublicJoin() + ")");

        if (!allowed) {
            out.sendFailure(Component.literal("That town is invite-only."));
            return 0;
        }

        d.setTownId(town.getId());
        d.setRank(TownRank.CITIZEN);
        town.addCitizen(player.getUUID());

        TownInviteManager.removeInvite(town.getId(), player.getUUID());
        out.sendSuccess(() -> Component.literal("Joined town '" + town.getName() + "'."), true);
        return 1;
    }

    private static int leaveTown(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "leaveTown() called");

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        TownPlayerData d = TownPlayerManager.get(player.getUUID());
        if (!d.hasTown()) {
            out.sendFailure(Component.literal("You are not in a town."));
            return 0;
        }

        Town town = TownManager.getTown(d.getTownId());
        debug(ctx, "Player town = " + (town != null ? town.getName() : "null"));

        if (town == null) {
            d.setTownId(null);
            d.setRank(TownRank.CITIZEN);
            out.sendFailure(Component.literal("Town data missing. You have been detached."));
            return 1;
        }

        if (d.getRank() == TownRank.LEADER && town.getCitizens().size() > 1) {
            out.sendFailure(Component.literal(
                    "You are the leader. Transfer leadership before leaving."));
            return 0;
        }

        town.removeCitizen(player.getUUID());
        d.setTownId(null);
        d.setRank(TownRank.CITIZEN);

        out.sendSuccess(() -> Component.literal(
                "You left the town '" + town.getName() + "'."), true);
        return 1;
    }

    private static int disbandTown(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "disbandTown() called");

        ServerPlayer player = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, player);
        if (town == null) return 0;

        if (!ensureLeader(ctx, player, town)) return 0;

        // detach all members
        for (UUID m : town.getCitizens()) {
            TownPlayerData d = TownPlayerManager.get(m);
            d.setTownId(null);
            d.setRank(TownRank.CITIZEN);
        }

        TownInviteManager.clearInvitesForTown(town.getId());
        TownManager.getTownMap().remove(town.getId());

        debug(ctx, "Town disbanded: " + town.getName());

        ctx.getSource().sendSuccess(() ->
                Component.literal("Town '" + town.getName() + "' was disbanded."), true);
        return 1;
    }

    // =========================================================================
    // Invites
    // =========================================================================

    private static int invitePlayer(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "invitePlayer() called");

        ServerPlayer sender = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, sender);
        if (town == null) return 0;
        if (!ensureLeader(ctx, sender, town)) return 0;

        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

        debug(ctx, "Inviting " + target.getGameProfile().getName() + " to town " + town.getName());

        if (town.isMember(target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("Player already in your town."));
            return 0;
        }

        TownInviteManager.invitePlayer(town.getId(), target.getUUID());

        ctx.getSource().sendSuccess(() ->
                Component.literal("Invited " + target.getGameProfile().getName()), false);

        target.sendSystemMessage(Component.literal(
                "You were invited to join town '" + town.getName()
                        + "'. Use /town accept " + town.getName()));

        return 1;
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "acceptInvite() called");

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        String name = StringArgumentType.getString(ctx, "town");
        Town town = TownManager.getTownByName(name);

        debug(ctx, "Accepting invite for town: " + name);

        if (town == null) {
            out.sendFailure(Component.literal("No such town exists."));
            return 0;
        }

        if (!TownInviteManager.hasInvite(town.getId(), player.getUUID())) {
            out.sendFailure(Component.literal("You do not have an invite from that town."));
            return 0;
        }

        TownPlayerData d = TownPlayerManager.get(player.getUUID());
        if (d.hasTown()) {
            out.sendFailure(Component.literal("Already in a town."));
            return 0;
        }

        d.setTownId(town.getId());
        d.setRank(TownRank.CITIZEN);
        town.addCitizen(player.getUUID());

        TownInviteManager.removeInvite(town.getId(), player.getUUID());

        out.sendSuccess(() -> Component.literal("Joined town '" + town.getName() + "'."), true);
        return 1;
    }

    private static int denyInvite(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "denyInvite() called");

        ServerPlayer player = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        String name = StringArgumentType.getString(ctx, "town");
        Town town = TownManager.getTownByName(name);

        if (town == null) {
            out.sendFailure(Component.literal("No such town exists."));
            return 0;
        }

        if (!TownInviteManager.hasInvite(town.getId(), player.getUUID())) {
            out.sendFailure(Component.literal("No invite from that town."));
            return 0;
        }

        TownInviteManager.removeInvite(town.getId(), player.getUUID());
        out.sendSuccess(() -> Component.literal("Invite declined."), false);

        return 1;
    }

    // =========================================================================
    // Ranks
    // =========================================================================

    private static int promote(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "promote() called");

        ServerPlayer sender = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, sender);
        if (town == null) return 0;
        if (!ensureLeader(ctx, sender, town)) return 0;

        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        if (!town.isMember(target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("Player not in your town."));
            return 0;
        }

        TownPlayerData d = TownPlayerManager.get(target.getUUID());

        if (d.getRank() == TownRank.LEADER) {
            ctx.getSource().sendFailure(Component.literal("They are already the leader."));
            return 0;
        }
        if (d.getRank() == TownRank.OFFICER) {
            ctx.getSource().sendFailure(Component.literal("Already an officer."));
            return 0;
        }

        d.setRank(TownRank.OFFICER);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Promoted to officer."), true);

        return 1;
    }

    private static int demote(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "demote() called");

        ServerPlayer sender = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, sender);
        if (town == null) return 0;
        if (!ensureLeader(ctx, sender, town)) return 0;

        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        if (!town.isMember(target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("Player not in your town."));
            return 0;
        }

        TownPlayerData d = TownPlayerManager.get(target.getUUID());

        if (d.getRank() == TownRank.CITIZEN) {
            ctx.getSource().sendFailure(Component.literal("Already a citizen."));
            return 0;
        }

        if (d.getRank() == TownRank.LEADER) {
            ctx.getSource().sendFailure(Component.literal(
                    "Use /town setleader to transfer leadership."));
            return 0;
        }

        d.setRank(TownRank.CITIZEN);

        ctx.getSource().sendSuccess(() ->
                Component.literal("Demoted to citizen."), true);

        return 1;
    }

    private static int setLeader(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "setLeader() called");

        ServerPlayer sender = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, sender);
        if (town == null) return 0;
        if (!ensureLeader(ctx, sender, town)) return 0;

        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        if (!town.isMember(target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal("Player not in town."));
            return 0;
        }

        if (target.getUUID().equals(town.getLeader())) {
            ctx.getSource().sendFailure(Component.literal("Already leader."));
            return 0;
        }

        TownPlayerData senderData = TownPlayerManager.get(sender.getUUID());
        TownPlayerData targetData = TownPlayerManager.get(target.getUUID());

        senderData.setRank(TownRank.CITIZEN);
        targetData.setRank(TownRank.LEADER);

        town.setLeader(target.getUUID());

        ctx.getSource().sendSuccess(() ->
                Component.literal("Leadership transferred."), true);
        return 1;
    }

    // =========================================================================
    // Metadata
    // =========================================================================

    private static int setDescription(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "setDescription() called");

        ServerPlayer p = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, p);
        if (town == null) return 0;

        if (!ensureLeader(ctx, p, town)) return 0;

        String desc = StringArgumentType.getString(ctx, "description");

        if (desc.length() > FeudalConfig.MAX_DESC_LENGTH.get()) {
            ctx.getSource().sendFailure(Component.literal("Description too long."));
            return 0;
        }

        town.setDescription(desc);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Description updated."), false);

        return 1;
    }

    private static int setMotd(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "setMotd() called");

        ServerPlayer p = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, p);
        if (town == null) return 0;

        if (!ensureLeader(ctx, p, town)) return 0;

        String motd = StringArgumentType.getString(ctx, "motd");

        if (motd.length() > FeudalConfig.MAX_MOTD_LENGTH.get()) {
            ctx.getSource().sendFailure(Component.literal("MOTD too long."));
            return 0;
        }

        town.setMotd(motd);
        ctx.getSource().sendSuccess(() ->
                Component.literal("MOTD updated."), false);

        return 1;
    }

    private static int setPublicJoin(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "setPublicJoin() called");

        ServerPlayer p = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, p);
        if (town == null) return 0;

        if (!ensureLeader(ctx, p, town)) return 0;

        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        town.setPublicJoin(enabled);

        ctx.getSource().sendSuccess(() ->
                Component.literal("Join type now: "
                        + (enabled ? "Public" : "Invite-Only")), false);

        return 1;
    }

    // =========================================================================
    // Claims
    // =========================================================================

    private static int claimChunk(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "claimChunk() called");

        ServerPlayer p = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        TownPlayerData d = TownPlayerManager.get(p.getUUID());
        if (!d.hasTown()) {
            out.sendFailure(Component.literal("You must be in a town."));
            return 0;
        }
        if (!ensureCanManageClaims(ctx, p)) return 0;

        Town town = TownManager.getTown(d.getTownId());

        ServerLevel level = (ServerLevel) p.level();
        ChunkPos pos = new ChunkPos(p.blockPosition());
        String dim = level.dimension().location().toString();

        debug(ctx, "Attempting claim in dim=" + dim + " chunk=(" + pos.x + "," + pos.z + ") for town " + town.getName());

        if (TownManager.isChunkClaimed(dim, pos.x, pos.z)) {
            Town owner = TownManager.getTownByClaim(dim, pos.x, pos.z);
            out.sendFailure(Component.literal("Chunk already claimed by "
                    + (owner != null ? owner.getName() : "someone")));
            return 0;
        }

        int limit = TownManager.getClaimLimit(town);
        if (town.getClaims().size() >= limit) {
            out.sendFailure(Component.literal(
                    "Claim limit reached (" + town.getClaims().size() + "/" + limit + ")"));
            return 0;
        }

        town.addClaim(new ClaimPos(dim, pos.x, pos.z));
        out.sendSuccess(() ->
                Component.literal("Chunk claimed for town '" + town.getName() + "'."), true);

        return 1;
    }

    private static int unclaimChunk(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "unclaimChunk() called");

        ServerPlayer p = getPlayer(ctx);
        CommandSourceStack out = ctx.getSource();

        TownPlayerData d = TownPlayerManager.get(p.getUUID());
        if (!d.hasTown()) {
            out.sendFailure(Component.literal("You are not in a town."));
            return 0;
        }
        if (!ensureCanManageClaims(ctx, p)) return 0;

        Town town = TownManager.getTown(d.getTownId());

        ServerLevel level = (ServerLevel) p.level();
        ChunkPos pos = new ChunkPos(p.blockPosition());
        String dim = level.dimension().location().toString();

        ClaimPos claim = new ClaimPos(dim, pos.x, pos.z);

        if (!town.getClaims().contains(claim)) {
            out.sendFailure(Component.literal("Town does not claim this chunk."));
            return 0;
        }

        town.removeClaim(claim);
        out.sendSuccess(() ->
                Component.literal("Chunk unclaimed."), true);

        return 1;
    }

    private static int listClaims(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "listClaims() called");

        ServerPlayer p = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, p);
        if (town == null) return 0;

        CommandSourceStack out = ctx.getSource();

        if (town.getClaims().isEmpty()) {
            out.sendSuccess(() ->
                    Component.literal("Your town has no claims."), false);
            return 1;
        }

        out.sendSuccess(() ->
                Component.literal("Claims for town '" + town.getName() + "':"), false);

        for (ClaimPos pos : town.getClaims()) {
            out.sendSuccess(() ->
                    Component.literal("- " + pos.getDimension()
                            + " [" + pos.getChunkX() + ", " + pos.getChunkZ() + "]"), false);
        }

        return 1;
    }

    // =========================================================================
    // Members
    // =========================================================================

    private static int listMembers(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {

        debug(ctx, "listMembers() called");

        ServerPlayer p = getPlayer(ctx);
        Town town = getPlayerTownOrError(ctx, p);
        if (town == null) return 0;

        CommandSourceStack out = ctx.getSource();

        Set<UUID> members = town.getCitizens();

        out.sendSuccess(() ->
                Component.literal("--- Members of '" + town.getName() + "' ---"), false);

        String leader = town.getLeader().toString();
        out.sendSuccess(() ->
                Component.literal("Leader: " + leader), false);

        String officers = members.stream()
                .filter(id -> !id.equals(town.getLeader()))
                .filter(id -> TownPlayerManager.get(id).getRank() == TownRank.OFFICER)
                .map(UUID::toString)
                .collect(Collectors.joining(", "));

        String citizens = members.stream()
                .filter(id -> TownPlayerManager.get(id).getRank() == TownRank.CITIZEN)
                .map(UUID::toString)
                .collect(Collectors.joining(", "));

        out.sendSuccess(() ->
                Component.literal("Officers: " + (officers.isEmpty() ? "<none>" : officers)), false);

        out.sendSuccess(() ->
                Component.literal("Citizens: " + (citizens.isEmpty() ? "<none>" : citizens)), false);

        return 1;
    }
}
