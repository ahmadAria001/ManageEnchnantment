package org.useReducer.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.registry.entry.RegistryEntry;
import org.useReducer.ManageEnchantment;
import org.useReducer.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Mixin to remove the vanilla 15-bookshelf hard cap for enchanting tables.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    /**
     * Minecraft caps the maximum number of bookshelves at 15.
     * We modify this constant to Integer.MAX_VALUE to allow infinite bookshelves.
     */
    @ModifyConstant(method = "calculateRequiredExperienceLevel", constant = @Constant(intValue = 15))
    private static int manageenchantment$removeBookshelfCap(int original) {
        if (ConfigManager.isBookshelfCapRemoved()) {
            // Using a high number instead of MAX_VALUE to prevent potential integer
            // overflows
            // when it calculates base requirements later.
            return 10000;
        }
        return original;
    }

    /**
     * Minecraft 1.21 checks compatibility using EnchantmentHelper.isCompatible.
     * We override this to return true if the config option is enabled.
     */
    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private static void manageenchantment$bypassIncompatible(Collection<RegistryEntry<Enchantment>> candidates,
            RegistryEntry<Enchantment> enchantment, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigManager.isIncompatibleBypassEnabled()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Filter out curses from the possible entries stream if configured.
     */
    @ModifyVariable(method = "generateEnchantments", at = @At("HEAD"), argsOnly = true)
    private static java.util.stream.Stream<RegistryEntry<Enchantment>> manageenchantment$filterCurses(
            java.util.stream.Stream<RegistryEntry<Enchantment>> possibleEntries) {
        if (ConfigManager.isBanCursesFromEnchantingTable()) {
            return possibleEntries.filter(entry -> !entry.isIn(net.minecraft.registry.tag.EnchantmentTags.CURSE));
        }
        return possibleEntries;
    }

    /**
     * Cap the generated enchantment levels based on the item's material tier.
     */
    @Inject(method = "generateEnchantments", at = @At("RETURN"))
    private static void manageenchantment$capGeneratedLevels(net.minecraft.util.math.random.Random random,
            net.minecraft.item.ItemStack stack, int level,
            java.util.stream.Stream<RegistryEntry<Enchantment>> possibleEntries,
            CallbackInfoReturnable<java.util.List<net.minecraft.enchantment.EnchantmentLevelEntry>> cir) {
        java.util.List<net.minecraft.enchantment.EnchantmentLevelEntry> result = cir.getReturnValue();
        if (result == null || result.isEmpty())
            return;

        ManageEnchantment.LOGGER.info("{} has max level {}", stack.getItem(),
                org.useReducer.tier.TierManager.getMaxLevel(stack, Integer.MAX_VALUE));
        int tierCap = org.useReducer.tier.TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
        boolean modified = false;
        java.util.List<net.minecraft.enchantment.EnchantmentLevelEntry> newResult = new java.util.ArrayList<>();

        for (net.minecraft.enchantment.EnchantmentLevelEntry entry : result) {
            if (entry.level() > tierCap) {
                newResult.add(new net.minecraft.enchantment.EnchantmentLevelEntry(entry.enchantment(), tierCap));
                modified = true;
            } else {
                newResult.add(entry);
            }
        }

        if (modified) {
            cir.setReturnValue(newResult);
        }
    }
}
