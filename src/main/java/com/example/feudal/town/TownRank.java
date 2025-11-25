package com.example.feudal.town;

public enum TownRank {
    LEADER,
    OFFICER,
    CITIZEN;

    public boolean canManageClaims() {
        return this == LEADER || this == OFFICER;
    }

    public boolean isLeader() {
        return this == LEADER;
    }
}
