package com.muguett.moonforge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muguett.moonforge.MoonforgeMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MoonforgeConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("moonforge.json");

    private static MoonforgeConfig config = new MoonforgeConfig();

    private MoonforgeConfigManager() {
    }

    public static MoonforgeConfig getConfig() {
        return config;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                    MoonforgeConfig loaded = GSON.fromJson(reader, MoonforgeConfig.class);
                    if (loaded != null) {
                        config = loaded;
                    }
                }
            } else {
                save();
            }
        } catch (Exception exception) {
            MoonforgeMod.LOGGER.warn("Failed to load Moonforge config, using defaults.", exception);
            config = new MoonforgeConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception exception) {
            MoonforgeMod.LOGGER.warn("Failed to save Moonforge config.", exception);
        }
    }
}