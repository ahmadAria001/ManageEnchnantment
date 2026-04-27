package org.rmsederhana.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import org.rmsederhana.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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
            // Using a high number instead of MAX_VALUE to prevent potential integer overflows
            // when it calculates base requirements later.
            return 10000;
        }
        return original;
    }
}
