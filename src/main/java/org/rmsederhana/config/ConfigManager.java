package org.rmsederhana.config;

import net.fabricmc.loader.api.FabricLoader;
import org.rmsederhana.ManageEnchnantment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages the mod configuration loaded from a TOML file.
 * 
 * Configuration priority for max levels:
 *   1. Per-enchantment override (if not -1)
 *   2. Vanilla max level × global multiplier
 */
public class ConfigManager {

    private static final String CONFIG_FILE_NAME = "manageenchantment.toml";

    // General settings
    private static int globalMaxLevelMultiplier = 2;

    // Anvil settings
    private static boolean removeAnvilCap = true;
    private static int maxAnvilCost = 0;
    private static double discountMultiplier = 1.0;
    private static boolean capActualCost = false;
    private static boolean disablePriorWorkPenalty = false;

    // Per-enchantment max level overrides (key = enchantment path, e.g. "sharpness")
    private static final Map<String, Integer> enchantmentOverrides = new LinkedHashMap<>();

    // All vanilla enchantments with their vanilla max levels (for default config generation)
    private static final Map<String, Integer> VANILLA_DEFAULTS = new LinkedHashMap<>();

    static {
        // Max Level V
        VANILLA_DEFAULTS.put("sharpness", 5);
        VANILLA_DEFAULTS.put("smite", 5);
        VANILLA_DEFAULTS.put("bane_of_arthropods", 5);
        VANILLA_DEFAULTS.put("efficiency", 5);
        VANILLA_DEFAULTS.put("power", 5);
        VANILLA_DEFAULTS.put("impaling", 5);
        VANILLA_DEFAULTS.put("density", 5);

        // Max Level IV
        VANILLA_DEFAULTS.put("protection", 4);
        VANILLA_DEFAULTS.put("fire_protection", 4);
        VANILLA_DEFAULTS.put("blast_protection", 4);
        VANILLA_DEFAULTS.put("projectile_protection", 4);
        VANILLA_DEFAULTS.put("feather_falling", 4);
        VANILLA_DEFAULTS.put("piercing", 4);
        VANILLA_DEFAULTS.put("breach", 4);

        // Max Level III
        VANILLA_DEFAULTS.put("unbreaking", 3);
        VANILLA_DEFAULTS.put("fortune", 3);
        VANILLA_DEFAULTS.put("looting", 3);
        VANILLA_DEFAULTS.put("depth_strider", 3);
        VANILLA_DEFAULTS.put("respiration", 3);
        VANILLA_DEFAULTS.put("loyalty", 3);
        VANILLA_DEFAULTS.put("riptide", 3);
        VANILLA_DEFAULTS.put("luck_of_the_sea", 3);
        VANILLA_DEFAULTS.put("lure", 3);
        VANILLA_DEFAULTS.put("quick_charge", 3);
        VANILLA_DEFAULTS.put("soul_speed", 3);
        VANILLA_DEFAULTS.put("swift_sneak", 3);
        VANILLA_DEFAULTS.put("thorns", 3);
        VANILLA_DEFAULTS.put("sweeping_edge", 3);
        VANILLA_DEFAULTS.put("wind_burst", 3);

        // Max Level II
        VANILLA_DEFAULTS.put("knockback", 2);
        VANILLA_DEFAULTS.put("fire_aspect", 2);
        VANILLA_DEFAULTS.put("punch", 2);
        VANILLA_DEFAULTS.put("frost_walker", 2);

        // Max Level I
        VANILLA_DEFAULTS.put("silk_touch", 1);
        VANILLA_DEFAULTS.put("aqua_affinity", 1);
        VANILLA_DEFAULTS.put("flame", 1);
        VANILLA_DEFAULTS.put("infinity", 1);
        VANILLA_DEFAULTS.put("channeling", 1);
        VANILLA_DEFAULTS.put("multishot", 1);
        VANILLA_DEFAULTS.put("mending", 1);
        VANILLA_DEFAULTS.put("binding_curse", 1);
        VANILLA_DEFAULTS.put("vanishing_curse", 1);
    }

    /**
     * Load configuration from file, or generate defaults if not present.
     */
    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configPath)) {
            generateDefaultConfig(configPath);
            ManageEnchnantment.LOGGER.info("Generated default config at {}", configPath);
        }

        try {
            Map<String, Map<String, Object>> toml = TomlParser.parse(configPath);

            // Parse [general]
            Map<String, Object> general = toml.get("general");
            globalMaxLevelMultiplier = TomlParser.getInt(general, "global_max_level_multiplier", 2);

            if (globalMaxLevelMultiplier < 1) {
                ManageEnchnantment.LOGGER.warn("global_max_level_multiplier must be >= 1, defaulting to 1");
                globalMaxLevelMultiplier = 1;
            }

            // Parse [anvil]
            Map<String, Object> anvil = toml.get("anvil");
            
            // Backwards compatibility: check general if not in anvil
            removeAnvilCap = TomlParser.getBoolean(anvil != null ? anvil : general, "remove_anvil_cap", true);
            maxAnvilCost = TomlParser.getInt(anvil != null ? anvil : general, "max_anvil_cost", 0);
            
            discountMultiplier = TomlParser.getDouble(anvil, "discount_multiplier", 1.0);
            capActualCost = TomlParser.getBoolean(anvil, "cap_actual_cost", false);
            disablePriorWorkPenalty = TomlParser.getBoolean(anvil, "disable_prior_work_penalty", false);

            // Parse [enchantments]
            Map<String, Object> enchantments = toml.get("enchantments");
            enchantmentOverrides.clear();
            if (enchantments != null) {
                for (Map.Entry<String, Object> entry : enchantments.entrySet()) {
                    String key = entry.getKey().trim();
                    if (entry.getValue() instanceof Number num) {
                        enchantmentOverrides.put(key, num.intValue());
                    }
                }
            }

            ManageEnchnantment.LOGGER.info("Config loaded: multiplier={}, anvil_cap_removed={}, {} enchantment overrides",
                    globalMaxLevelMultiplier, removeAnvilCap, enchantmentOverrides.size());

        } catch (IOException e) {
            ManageEnchnantment.LOGGER.error("Failed to load config, using defaults", e);
        }
    }

    /**
     * Get the configured max level for an enchantment.
     * 
     * @param enchantmentId The full enchantment ID (e.g., "minecraft:sharpness" or "enchantplus:mace/striker")
     * @param enchantmentPath The enchantment's path (e.g., "sharpness" or "mace/striker")
     * @param vanillaMaxLevel The vanilla default max level
     * @return The configured max level, or -1 if no override is set
     */
    public static int getConfiguredMaxLevel(String enchantmentId, String enchantmentPath, int vanillaMaxLevel) {
        // Priority 1: Per-enchantment override using full ID
        Integer override = enchantmentOverrides.get(enchantmentId);
        if (override != null && override != -1) {
            return override;
        }

        // Priority 2: Per-enchantment override using just path (for vanilla convenience)
        override = enchantmentOverrides.get(enchantmentPath);
        if (override != null && override != -1) {
            return override;
        }

        // Priority 3: Global multiplier
        if (globalMaxLevelMultiplier > 1) {
            return vanillaMaxLevel * globalMaxLevelMultiplier;
        }

        // No override - return vanilla default
        return vanillaMaxLevel;
    }

    /**
     * Check if an enchantment path has any override configured (either individual or global).
     */
    public static boolean hasOverride(String enchantmentPath) {
        Integer override = enchantmentOverrides.get(enchantmentPath);
        if (override != null && override != -1) {
            return true;
        }
        return globalMaxLevelMultiplier > 1;
    }

    public static boolean isAnvilCapRemoved() {
        return removeAnvilCap;
    }

    public static int getMaxAnvilCost() {
        return maxAnvilCost;
    }

    public static double getDiscountMultiplier() {
        return discountMultiplier;
    }

    public static boolean isActualCostCapped() {
        return capActualCost;
    }

    public static boolean isPriorWorkPenaltyDisabled() {
        return disablePriorWorkPenalty;
    }

    public static int getGlobalMaxLevelMultiplier() {
        return globalMaxLevelMultiplier;
    }

    public static Map<String, Integer> getEnchantmentOverrides() {
        return Map.copyOf(enchantmentOverrides);
    }

    public static Map<String, Integer> getVanillaDefaults() {
        return Map.copyOf(VANILLA_DEFAULTS);
    }

    /**
     * Generates the default configuration file with all vanilla enchantments documented.
     */
    private static void generateDefaultConfig(Path configPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ============================================================\n");
        sb.append("# ManageEnchantment Configuration\n");
        sb.append("# ============================================================\n");
        sb.append("# This mod allows all enchantments to exceed their vanilla\n");
        sb.append("# maximum level. Configure levels below.\n");
        sb.append("# Server restart required after changes.\n");
        sb.append("# ============================================================\n");
        sb.append("\n");
        sb.append("[general]\n");
        sb.append("# Global multiplier for ALL enchantment max levels.\n");
        sb.append("# Applied when an enchantment has no individual override (or is set to -1).\n");
        sb.append("# Example: multiplier=2 means Sharpness max becomes 10 (5 x 2)\n");
        sb.append("# Set to 1 to disable global scaling.\n");
        sb.append("global_max_level_multiplier = 2\n");
        sb.append("\n");
        sb.append("[anvil]\n");
        sb.append("# Remove the \"Too Expensive!\" anvil cap (vanilla: 40 levels)\n");
        sb.append("remove_anvil_cap = true\n");
        sb.append("\n");
        sb.append("# Maximum anvil XP cost allowed. Used as the cap threshold (0 = unlimited)\n");
        sb.append("max_anvil_cost = 0\n");
        sb.append("\n");
        sb.append("# Multiplier applied to the final anvil XP cost (e.g. 0.5 = half cost)\n");
        sb.append("discount_multiplier = 1.0\n");
        sb.append("\n");
        sb.append("# If true, the actual XP deducted from the player will never exceed max_anvil_cost.\n");
        sb.append("cap_actual_cost = false\n");
        sb.append("\n");
        sb.append("# If true, disables the vanilla mechanic where items get twice as expensive every time they are repaired or combined.\n");
        sb.append("disable_prior_work_penalty = false\n");
        sb.append("\n");
        sb.append("[enchantments]\n");
        sb.append("# Per-enchantment max level overrides.\n");
        sb.append("# Set to -1 to use the global multiplier instead.\n");
        sb.append("# Individual values take priority over the global multiplier.\n");
        sb.append("#\n");
        sb.append("# Format: enchantment_name = max_level\n");
        sb.append("# Vanilla max levels shown in comments for reference.\n");
        sb.append("\n");

        sb.append("# --- Damage Enchantments (Vanilla Max: V) ---\n");
        sb.append("sharpness = -1\n");
        sb.append("smite = -1\n");
        sb.append("bane_of_arthropods = -1\n");
        sb.append("density = -1\n");
        sb.append("impaling = -1\n");
        sb.append("\n");

        sb.append("# --- Tool Enchantments (Vanilla Max: V) ---\n");
        sb.append("efficiency = -1\n");
        sb.append("power = -1\n");
        sb.append("\n");

        sb.append("# --- Protection Enchantments (Vanilla Max: IV) ---\n");
        sb.append("protection = -1\n");
        sb.append("fire_protection = -1\n");
        sb.append("blast_protection = -1\n");
        sb.append("projectile_protection = -1\n");
        sb.append("feather_falling = -1\n");
        sb.append("\n");

        sb.append("# --- Crossbow Enchantments (Vanilla Max: IV) ---\n");
        sb.append("piercing = -1\n");
        sb.append("\n");

        sb.append("# --- Mace Enchantments (Vanilla Max: IV) ---\n");
        sb.append("breach = -1\n");
        sb.append("\n");

        sb.append("# --- Level III Enchantments ---\n");
        sb.append("unbreaking = -1\n");
        sb.append("fortune = -1\n");
        sb.append("looting = -1\n");
        sb.append("depth_strider = -1\n");
        sb.append("respiration = -1\n");
        sb.append("loyalty = -1\n");
        sb.append("riptide = -1\n");
        sb.append("luck_of_the_sea = -1\n");
        sb.append("lure = -1\n");
        sb.append("quick_charge = -1\n");
        sb.append("soul_speed = -1\n");
        sb.append("swift_sneak = -1\n");
        sb.append("thorns = -1\n");
        sb.append("sweeping_edge = -1\n");
        sb.append("wind_burst = -1\n");
        sb.append("\n");

        sb.append("# --- Level II Enchantments ---\n");
        sb.append("knockback = -1\n");
        sb.append("fire_aspect = -1\n");
        sb.append("punch = -1\n");
        sb.append("frost_walker = -1\n");
        sb.append("\n");

        sb.append("# --- Level I Enchantments ---\n");
        sb.append("# These are typically toggle enchantments. Raising their level\n");
        sb.append("# may not add meaningful effect, but is supported.\n");
        sb.append("silk_touch = -1\n");
        sb.append("aqua_affinity = -1\n");
        sb.append("flame = -1\n");
        sb.append("infinity = -1\n");
        sb.append("channeling = -1\n");
        sb.append("multishot = -1\n");
        sb.append("mending = -1\n");
        sb.append("binding_curse = -1\n");
        sb.append("vanishing_curse = -1\n");

        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, sb.toString());
        } catch (IOException e) {
            ManageEnchnantment.LOGGER.error("Failed to generate default config", e);
        }
    }
}
