package org.useReducer.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffers;
import org.useReducer.config.ConfigManager;
import org.useReducer.enchantment.EnchantmentOverrideManager;
import org.useReducer.enchantment.OverrideState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mixin to intercept villager enchantment generation, enforcing caps and bans.
 */
@Mixin(TradeOffers.EnchantBookFactory.class)
public class TradeOffersEnchantBookFactoryMixin {

    /**
     * Intercepts the selection of a random enchantment from the registry.
     * We filter out any enchantments that have been explicitly banned (limit = 0).
     */
    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getRandomEntry(Lnet/minecraft/registry/tag/TagKey;Lnet/minecraft/util/math/random/Random;)Ljava/util/Optional;"))
    private Optional<RegistryEntry<Enchantment>> manageenchantment$filterBannedEnchantments(Registry<Enchantment> registry, TagKey<Enchantment> tag, Random random) {
        Iterable<RegistryEntry<Enchantment>> entries = registry.iterateEntries(tag);
        List<RegistryEntry<Enchantment>> validEntries = new ArrayList<>();
        
        for (RegistryEntry<Enchantment> entry : entries) {
            String path = EnchantmentOverrideManager.getPath(entry.value());
            if (path != null) {
                int limit = ConfigManager.getVillagerTradeLimit(path);
                if (limit != 0) { // 0 means banned
                    validEntries.add(entry);
                }
            } else {
                // If it's not mapped (e.g., dynamically generated), allow it
                validEntries.add(entry);
            }
        }
        
        if (validEntries.isEmpty()) {
            return Optional.empty(); // Falls back to standard book trade if everything is banned
        }
        
        return Optional.of(validEntries.get(random.nextInt(validEntries.size())));
    }

    /**
     * When the villager trade generation calls enchantment.getMaxLevel(), we intercept it.
     * We will check if the user has configured a specific limit for this enchantment.
     * If not, we will fallback to the default strategy (e.g., returning the vanilla max level).
     */
    @Redirect(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;getMaxLevel()I"))
    private int manageenchantment$capVillagerTradeLevel(Enchantment enchantment) {
        String path = EnchantmentOverrideManager.getPath(enchantment);
        if (path == null) {
            return enchantment.getMaxLevel();
        }

        // 1. Check if there's an explicit override for this enchantment in villager_trades.overrides
        int specificLimit = ConfigManager.getVillagerTradeLimit(path);
        if (specificLimit > 0) {
            return specificLimit;
        }

        // 2. Fallback to default strategy
        if ("vanilla".equalsIgnoreCase(ConfigManager.getDefaultCapStrategy())) {
            // Bypass our own global override mixin to ask Minecraft for the true original max level
            OverrideState.BYPASS_OVERRIDE.set(true);
            try {
                return enchantment.getMaxLevel();
            } finally {
                OverrideState.BYPASS_OVERRIDE.set(false);
            }
        }

        // "uncapped" or unknown strategy: just return the globally overridden max level
        return enchantment.getMaxLevel();
    }
}
