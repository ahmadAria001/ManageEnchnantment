package org.rmsederhana.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.rmsederhana.config.ConfigManager;
import org.rmsederhana.enchantment.EnchantmentOverrideManager;
import org.rmsederhana.enchantment.OverrideState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override the max level of enchantments based on configuration.
 * 
 * Since Enchantment is a Record in 1.21, getMaxLevel() delegates to
 * definition().maxLevel(). We intercept the return value and replace it
 * with the configured value from EnchantmentOverrideManager.
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void manageenchantment$overrideMaxLevel(CallbackInfoReturnable<Integer> cir) {
        if (OverrideState.BYPASS_OVERRIDE.get()) {
            return;
        }

        Enchantment self = (Enchantment) (Object) this;
        Integer configuredLevel = EnchantmentOverrideManager.getMaxLevel(self);
        if (configuredLevel != null) {
            cir.setReturnValue(configuredLevel);
        }
    }

    /**
     * Minecraft 1.21 Anvil checks compatibility using Enchantment.canBeCombined.
     */
    @Inject(method = "canBeCombined", at = @At("HEAD"), cancellable = true)
    private static void manageenchantment$bypassIncompatible(RegistryEntry<Enchantment> first, RegistryEntry<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
        if (ConfigManager.isIncompatibleBypassEnabled()) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Minecraft only has Roman numeral translation keys up to 10 (enchantment.level.10).
     * For levels > 10, the game tries to translate "enchantment.level.11" which doesn't exist,
     * resulting in ugly text like "Striker enchantment.level.11".
     * This mixin intercepts the name generation for high levels and appends the raw number instead.
     */
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private static void manageenchantment$getCustomName(RegistryEntry<Enchantment> enchantmentRegistryEntry, int level, CallbackInfoReturnable<Text> cir) {
        if (level > 10) {
            Enchantment enchantment = enchantmentRegistryEntry.value();
            MutableText name = enchantment.description().copy();
            
            // Apply standard vanilla formatting (Red for curses, Gray for everything else)
            if (enchantmentRegistryEntry.isIn(EnchantmentTags.CURSE)) {
                Texts.setStyleIfAbsent(name, Style.EMPTY.withColor(Formatting.RED));
            } else {
                Texts.setStyleIfAbsent(name, Style.EMPTY.withColor(Formatting.GRAY));
            }
            
            // Append the level in "Roman (Arabic)" format: e.g. "XX (20)"
            String levelString = toRoman(level) + " (" + level + ")";
            name.append(ScreenTexts.SPACE).append(Text.literal(levelString));
            cir.setReturnValue(name);
        }
    }

    /**
     * Converts an integer to a Roman numeral string.
     */
    private static String toRoman(int num) {
        if (num <= 0) return String.valueOf(num);
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] romanLetters = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                num -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }
}
