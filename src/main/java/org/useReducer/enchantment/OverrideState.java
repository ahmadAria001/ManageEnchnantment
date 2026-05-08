package org.useReducer.enchantment;

/**
 * Manages thread-local state for bypassing custom enchantment overrides.
 */
public class OverrideState {

    /**
     * If true, EnchantmentMixin will not apply the global multiplier or overrides,
     * effectively returning the original vanilla maximum level.
     */
    public static final ThreadLocal<Boolean> BYPASS_OVERRIDE = ThreadLocal.withInitial(() -> false);

}
