package org.rmsederhana.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.rmsederhana.config.ConfigManager;
import org.rmsederhana.tier.TierManager;
import org.rmsederhana.util.EnchantmentContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEnchantmentsComponent.class)
public abstract class ComponentLevelMixin {

    @Inject(method = "getLevel", at = @At("RETURN"), cancellable = true)
    private void manageenchantment$capComponentLevel(RegistryEntry<Enchantment> enchantment, CallbackInfoReturnable<Integer> cir) {
        if (!ConfigManager.isMaterialTiersEnabled()) return;

        // Read the item context from our utility class
        ItemStack contextStack = EnchantmentContext.get();
        if (contextStack == null || contextStack.isEmpty()) return;

        int level = cir.getReturnValue();
        if (level <= 0) return;

        int cap = TierManager.getMaxLevel(contextStack, Integer.MAX_VALUE);
        if (level > cap) {
            cir.setReturnValue(cap);
        }
    }
}
