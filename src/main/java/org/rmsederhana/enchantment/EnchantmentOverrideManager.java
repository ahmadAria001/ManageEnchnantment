package org.rmsederhana.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.rmsederhana.ManageEnchnantment;
import org.rmsederhana.config.ConfigManager;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Manages runtime enchantment max level overrides.
 * 
 * Uses an IdentityHashMap keyed by Enchantment instances to efficiently
 * look up configured max levels from within the Enchantment Mixin.
 */
public class EnchantmentOverrideManager {

    // Maps Enchantment instances to their configured max levels
    private static final Map<Enchantment, Integer> MAX_LEVEL_OVERRIDES = new IdentityHashMap<>();

    /**
     * Apply overrides from the config to all enchantments in the registry.
     * Called during server start after the dynamic registry is loaded.
     */
    public static void applyOverrides(MinecraftServer server) {
        MAX_LEVEL_OVERRIDES.clear();

        Registry<Enchantment> registry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        int overrideCount = 0;

        for (RegistryEntry<Enchantment> entry : registry.getIndexedEntries()) {
            // Get the enchantment's identifier
            Identifier id = entry.getKey()
                    .map(key -> key.getValue())
                    .orElse(null);

            if (id == null) continue;

            String enchantmentIdStr = id.toString();
            String enchantmentPath = id.getPath();
            Enchantment enchantment = entry.value();
            int vanillaMaxLevel = enchantment.definition().maxLevel();

            // Check if config has an override for this enchantment
            int configuredLevel = ConfigManager.getConfiguredMaxLevel(enchantmentIdStr, enchantmentPath, vanillaMaxLevel);

            if (configuredLevel != vanillaMaxLevel) {
                MAX_LEVEL_OVERRIDES.put(enchantment, configuredLevel);
                overrideCount++;
                ManageEnchnantment.LOGGER.debug("Override {}: {} -> {}", id, vanillaMaxLevel, configuredLevel);
            }
        }

        ManageEnchnantment.LOGGER.info("Applied {} enchantment max level overrides", overrideCount);
    }

    /**
     * Get the configured max level for an Enchantment instance.
     * 
     * @param enchantment The enchantment instance
     * @return The configured max level, or null if no override exists
     */
    public static Integer getMaxLevel(Enchantment enchantment) {
        return MAX_LEVEL_OVERRIDES.get(enchantment);
    }

    /**
     * Clear all overrides (called on server stop).
     */
    public static void clearOverrides() {
        MAX_LEVEL_OVERRIDES.clear();
    }
}
