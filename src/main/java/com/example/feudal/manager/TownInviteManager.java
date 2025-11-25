package com.example.feudal.town;

import java.util.*;

public class TownInviteManager {

    private static final Map<UUID, Set<UUID>> INVITES = new HashMap<>();

    public static void invitePlayer(UUID town, UUID player) {
        INVITES.computeIfAbsent(player, k -> new HashSet<>()).add(town);
    }

    public static boolean hasInvite(UUID town, UUID player) {
        return INVITES.getOrDefault(player, Collections.emptySet()).contains(town);
    }

    public static void removeInvite(UUID town, UUID player) {
        Set<UUID> set = INVITES.get(player);
        if (set == null) return;
        set.remove(town);
        if (set.isEmpty()) INVITES.remove(player);
    }

    public static void clearInvitesForTown(UUID town) {
        for (Set<UUID> s : INVITES.values()) s.remove(town);
        INVITES.entrySet().removeIf(e -> e.getValue().isEmpty());
    }
}
