package com.example.feudal.town;

import java.util.UUID;

public class TownPlayerData {

    private UUID townId;
    private TownRank rank = TownRank.CITIZEN;

    public TownPlayerData() {}

    public UUID getTownId() { return townId; }
    public void setTownId(UUID id) { this.townId = id; }

    public TownRank getRank() { return rank; }
    public void setRank(TownRank rank) { this.rank = rank; }

    public boolean hasTown() { return townId != null; }
}
