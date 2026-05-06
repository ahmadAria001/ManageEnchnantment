package org.useReducer.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.useReducer.ManageEnchantment;
import org.useReducer.config.ConfigManager;

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
    
    // Maps Enchantment instances to their identifier paths (e.g. "sharpness")
    private static final Map<Enchantment, String> ENCHANTMENT_PATHS = new IdentityHashMap<>();

    /**
     * Apply overrides from the config to all enchantments in the registry.
     * Called during server start after the dynamic registry is loaded.
     */
    public static void applyOverrides(MinecraftServer server) {
        MAX_LEVEL_OVERRIDES.clear();
        ENCHANTMENT_PATHS.clear();

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
            
            ENCHANTMENT_PATHS.put(enchantment, enchantmentPath);

            // Check if config has an override for this enchantment
            int configuredLevel = ConfigManager.getConfiguredMaxLevel(enchantmentIdStr, enchantmentPath, vanillaMaxLevel);

            if (configuredLevel != vanillaMaxLevel) {
                MAX_LEVEL_OVERRIDES.put(enchantment, configuredLevel);
                overrideCount++;
                ManageEnchantment.LOGGER.debug("Override {}: {} -> {}", id, vanillaMaxLevel, configuredLevel);
            }
        }

        ManageEnchantment.LOGGER.info("Applied {} enchantment max level overrides", overrideCount);
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
     * Get the identifier path for an Enchantment instance.
     * 
     * @param enchantment The enchantment instance
     * @return The identifier path, or null if not registered
     */
    public static String getPath(Enchantment enchantment) {
        return ENCHANTMENT_PATHS.get(enchantment);
    }

    /**
     * Clear all overrides (called on server stop).
     */
    public static void clearOverrides() {
        MAX_LEVEL_OVERRIDES.clear();
        ENCHANTMENT_PATHS.clear();
    }
}
