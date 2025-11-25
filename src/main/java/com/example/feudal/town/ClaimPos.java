package com.example.feudal.town;

import java.util.Objects;

/**
 * Represents a claimed chunk in a specific dimension.
 * Dimension is stored as a string resource location, e.g. "minecraft:overworld".
 */
public class ClaimPos {

    private String dimension; // resource location string
    private int chunkX;
    private int chunkZ;

    // Required for Gson
    public ClaimPos() {
    }

    public ClaimPos(String dimension, int chunkX, int chunkZ) {
        this.dimension = dimension;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public String getDimension() {
        return dimension;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaimPos claimPos)) return false;
        return chunkX == claimPos.chunkX &&
                chunkZ == claimPos.chunkZ &&
                Objects.equals(dimension, claimPos.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return dimension + " [" + chunkX + ", " + chunkZ + "]";
    }
}
