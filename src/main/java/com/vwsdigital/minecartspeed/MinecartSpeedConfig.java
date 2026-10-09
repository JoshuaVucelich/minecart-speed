package com.vwsdigital.minecartspeed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MinecartSpeedConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public double boostSpeed = 120.0;
    public List<String> triggerBlocks = List.of("minecraft:redstone_block");
    public boolean requirePowered = true;
    public boolean preserveDirection = true;
    public boolean ejectPassengersOnBoost = false;
    public double accelerationBlocks = 3.0;
    public boolean keepOnRails = true;

    private transient Set<Identifier> triggerBlockIds = Set.of();

    public static MinecartSpeedConfig loadOrCreate() {
        Path path = configPath();
        MinecartSpeedConfig config;
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                config = GSON.fromJson(json, MinecartSpeedConfig.class);
                if (config == null) {
                    config = new MinecartSpeedConfig();
                }
            } else {
                config = new MinecartSpeedConfig();
                save(config);
            }
        } catch (IOException e) {
            MinecartSpeed.LOGGER.error("Failed to read minecart-speed config, using defaults", e);
            config = new MinecartSpeedConfig();
        }
        config.rebuildCache();
        return config;
    }

    public static void save(MinecartSpeedConfig config) {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException e) {
            MinecartSpeed.LOGGER.error("Failed to write minecart-speed config", e);
        }
    }

    public boolean isTriggerBlock(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return triggerBlockIds.contains(id);
    }

    private void rebuildCache() {
        Set<Identifier> ids = new HashSet<>();
        if (triggerBlocks != null) {
            for (String raw : triggerBlocks) {
                if (raw == null || raw.isBlank()) continue;
                try {
                    ids.add(Identifier.parse(raw));
                } catch (Exception e) {
                    MinecartSpeed.LOGGER.warn("Invalid block id in trigger_blocks: {}", raw);
                }
            }
        }
        this.triggerBlockIds = Set.copyOf(ids);
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve("minecart-speed.json");
    }
}
