package com.example.feudal.town;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Town data container.
 *
 * IMPORTANT:
 *  - DO NOT access config in constructors (Gson loads before configs)
 *  - Default values here get overwritten by JSON when loading
 *  - Config defaults are applied in TownManager.createTown()
 */
public class Town {

    private UUID id;
    private String name;
    private UUID leader;

    private Set<UUID> citizens = new HashSet<>();
    private Set<ClaimPos> claims = new HashSet<>();

    private long bankGold = 0L;

    // DO NOT READ CONFIG HERE!
    // Gson calls this BEFORE NeoForge loads configs.
    private String description = "";
    private String motd = "";
    private boolean publicJoin = false;  // overwritten in TownManager after creation

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * No-args constructor required for Gson.
     * MUST NOT reference config or Minecraft classes.
     */
    public Town() {
        // leave blank
    }

    /**
     * Constructor used when creating a new town in-game.
     * DO NOT reference config here. TownManager handles that safely.
     */
    public Town(UUID id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;

        this.citizens.add(leader);

        // Public join is set later by TownManager
        this.publicJoin = false;
    }

    // ========================================================================
    // BASIC GETTERS / SETTERS
    // ========================================================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getCitizens() {
        return citizens;
    }

    public void setCitizens(Set<UUID> citizens) {
        this.citizens = citizens;
    }

    public Set<ClaimPos> getClaims() {
        return claims;
    }

    public void setClaims(Set<ClaimPos> claims) {
        this.claims = claims;
    }

    public long getBankGold() {
        return bankGold;
    }

    public void setBankGold(long bankGold) {
        this.bankGold = bankGold;
    }

    // ========================================================================
    // MEMBERSHIP
    // ========================================================================

    public void addCitizen(UUID id) {
        citizens.add(id);
    }

    public void removeCitizen(UUID id) {
        citizens.remove(id);
    }

    public boolean isMember(UUID id) {
        return citizens.contains(id);
    }

    // ========================================================================
    // CLAIMS
    // ========================================================================

    public void addClaim(ClaimPos pos) {
        claims.add(pos);
    }

    public void removeClaim(ClaimPos pos) {
        claims.remove(pos);
    }

    // ========================================================================
    // DESCRIPTION / MOTD
    // ========================================================================

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        if (description == null) {
            this.description = "";
            return;
        }
        this.description = description;
    }

    public String getMotd() {
        return motd == null ? "" : motd;
    }

    public void setMotd(String motd) {
        if (motd == null) {
            this.motd = "";
            return;
        }
        this.motd = motd;
    }

    // ========================================================================
    // PUBLIC / PRIVATE JOIN
    // ========================================================================

    public boolean isPublicJoin() {
        return publicJoin;
    }

    /**
     * Set default join mode. Called by TownManager AFTER config loads.
     */
    public void setPublicJoin(boolean publicJoin) {
        this.publicJoin = publicJoin;
    }
}
