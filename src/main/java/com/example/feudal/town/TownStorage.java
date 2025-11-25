package com.example.feudal.town;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles saving/loading town + player data as JSON.
 */
public class TownStorage {

    private static final Logger LOGGER = LogManager.getLogger("Feudalism-TownStorage");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("feudalism");
    private static final Path TOWNS_FILE = CONFIG_DIR.resolve("towns.json");
    private static final Path PLAYERS_FILE = CONFIG_DIR.resolve("players.json");

    private static final Type TOWN_MAP_TYPE = new TypeToken<Map<String, Town>>() {}.getType();
    private static final Type PLAYER_MAP_TYPE = new TypeToken<Map<String, TownPlayerData>>() {}.getType();

    private TownStorage() {
    }

    public static void loadAll() {
        createConfigDir();
        loadTowns();
        loadPlayers();
    }

    public static void saveAll() {
        createConfigDir();
        saveTowns();
        savePlayers();
    }

    private static void createConfigDir() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create Feudalism config directory: {}", CONFIG_DIR, e);
        }
    }

    private static void saveTowns() {
        Map<String, Town> data = new HashMap<>();
        for (Map.Entry<UUID, Town> entry : TownManager.getTownMap().entrySet()) {
            data.put(entry.getKey().toString(), entry.getValue());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                TOWNS_FILE,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save towns to {}", TOWNS_FILE, e);
        }
    }

    private static void loadTowns() {
        if (!Files.exists(TOWNS_FILE)) {
            LOGGER.info("No towns.json found, starting with empty town data.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(TOWNS_FILE, StandardCharsets.UTF_8)) {
            Map<String, Town> data = GSON.fromJson(reader, TOWN_MAP_TYPE);
            if (data == null) data = new HashMap<>();

            Map<UUID, Town> towns = new HashMap<>();
            for (Map.Entry<String, Town> entry : data.entrySet()) {
                try {
                    UUID id = UUID.fromString(entry.getKey());
                    towns.put(id, entry.getValue());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Invalid town UUID key '{}' in towns.json – skipping.", entry.getKey());
                }
            }

            TownManager.setTownMap(towns);
            LOGGER.info("Loaded {} towns.", towns.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load towns from {}", TOWNS_FILE, e);
        }
    }

    private static void savePlayers() {
        Map<String, TownPlayerData> data = new HashMap<>();
        for (Map.Entry<UUID, TownPlayerData> entry : TownPlayerManager.getPlayerMap().entrySet()) {
            data.put(entry.getKey().toString(), entry.getValue());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                PLAYERS_FILE,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save player town data to {}", PLAYERS_FILE, e);
        }
    }

    private static void loadPlayers() {
        if (!Files.exists(PLAYERS_FILE)) {
            LOGGER.info("No players.json found, starting with empty player town data.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(PLAYERS_FILE, StandardCharsets.UTF_8)) {
            Map<String, TownPlayerData> data = GSON.fromJson(reader, PLAYER_MAP_TYPE);
            if (data == null) data = new HashMap<>();

            Map<UUID, TownPlayerData> players = new HashMap<>();
            for (Map.Entry<String, TownPlayerData> entry : data.entrySet()) {
                try {
                    UUID id = UUID.fromString(entry.getKey());
                    players.put(id, entry.getValue());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Invalid player UUID key '{}' in players.json – skipping.", entry.getKey());
                }
            }

            TownPlayerManager.setPlayerMap(players);
            LOGGER.info("Loaded {} player town records.", players.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load player town data from {}", PLAYERS_FILE, e);
        }
    }
}
