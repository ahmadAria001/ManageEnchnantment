package org.rmsederhana.util;

import net.minecraft.item.ItemStack;

/**
 * Utility to share the current ItemStack context across enchantment checks.
 * This is not a mixin, so it can have public methods.
 */
public class EnchantmentContext {
    private static final ThreadLocal<ItemStack> CURRENT_STACK = new ThreadLocal<>();

    public static void set(ItemStack stack) {
        CURRENT_STACK.set(stack);
    }

    public static ItemStack get() {
        return CURRENT_STACK.get();
    }

    public static void clear() {
        CURRENT_STACK.remove();
    }
}
