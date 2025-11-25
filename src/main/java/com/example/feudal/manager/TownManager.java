package com.example.feudal.town;

import com.example.feudal.config.FeudalConfig;
import java.util.*;

public class TownManager {

    private static final Map<UUID, Town> TOWNS = new HashMap<>();
    private static final Map<String, UUID> NAME_TO_ID = new HashMap<>();

    public static Town createTown(String name, UUID leader) {
        UUID id = UUID.randomUUID();
        Town town = new Town(id, name, leader);

        TOWNS.put(id, town);
        NAME_TO_ID.put(name.toLowerCase(), id);

        TownPlayerData d = TownPlayerManager.get(leader);
        d.setTownId(id);
        d.setRank(TownRank.LEADER);

        return town;
    }

    public static Town getTown(UUID id) { return TOWNS.get(id); }

    public static Town getTownByName(String name) {
        UUID id = NAME_TO_ID.get(name.toLowerCase());
        return id == null ? null : TOWNS.get(id);
    }

    public static Map<UUID, Town> getTownMap() { return TOWNS; }

    // === RESTORED METHOD ===
    public static void setTownMap(Map<UUID, Town> towns) {
        TOWNS.clear();
        NAME_TO_ID.clear();

        TOWNS.putAll(towns);

        for (Map.Entry<UUID, Town> entry : towns.entrySet()) {
            Town town = entry.getValue();
            NAME_TO_ID.put(town.getName().toLowerCase(), entry.getKey());
        }
    }

    public static int getClaimLimit(Town town) {
        return FeudalConfig.BASE_CLAIMS.get()
                + (town.getCitizens().size() * FeudalConfig.CLAIMS_PER_MEMBER.get());
    }

    public static Town getTownOfPlayer(UUID uuid) {
        TownPlayerData d = TownPlayerManager.get(uuid);
        if (!d.hasTown()) return null;
        return getTown(d.getTownId());
    }

    public static Town getTownByClaim(String dimension, int x, int z) {
        ClaimPos pos = new ClaimPos(dimension, x, z);
        for (Town t : TOWNS.values()) {
            if (t.getClaims().contains(pos))
                return t;
        }
        return null;
    }

    public static boolean isChunkClaimed(String dimension, int x, int z) {
        return getTownByClaim(dimension, x, z) != null;
    }
}
