package com.example.feudal.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class FeudalSavedData extends SavedData {

    public static final String DATA_NAME = "feudalism_data";

    private final Map<String, String> factionOwners = new HashMap<>();

    public static FeudalSavedData load(CompoundTag tag) {
        FeudalSavedData data = new FeudalSavedData();
        CompoundTag factionsTag = tag.getCompound("factions");

        for (String key : factionsTag.getAllKeys()) {
            data.factionOwners.put(key, factionsTag.getString(key));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag factionsTag = new CompoundTag();

        for (Map.Entry<String, String> e : factionOwners.entrySet()) {
            factionsTag.putString(e.getKey(), e.getValue());
        }

        tag.put("factions", factionsTag);
        return tag;
    }

    public void setOwner(String faction, String owner) {
        factionOwners.put(faction, owner);
        setDirty();
    }

    public String getOwner(String faction) {
        return factionOwners.getOrDefault(faction, "");
    }
}
