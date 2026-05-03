package org.rmsederhana.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.rmsederhana.config.ConfigManager;
import org.rmsederhana.tier.TierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public abstract class EffectiveLevelMixin {

    @Unique
    private static final ThreadLocal<Boolean> manageenchantment$isInternal = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getLevel", at = @At("RETURN"), cancellable = true)
    private static void manageenchantment$capLevel(RegistryEntry<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (manageenchantment$isInternal.get() || !ConfigManager.isMaterialTiersEnabled() || stack == null || stack.isEmpty()) return;
        
        int level = cir.getReturnValue();
        if (level <= 0) return;

        manageenchantment$isInternal.set(true);
        try {
            int cap = TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
            if (level > cap) {
                cir.setReturnValue(cap);
            }
        } finally {
            manageenchantment$isInternal.set(false);
        }
    }

    @Inject(method = "getEquipmentLevel", at = @At("HEAD"), cancellable = true)
    private static void manageenchantment$capEquipmentLevel(RegistryEntry<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (manageenchantment$isInternal.get() || !ConfigManager.isMaterialTiersEnabled() || entity == null) return;

        manageenchantment$isInternal.set(true);
        try {
            int totalLevel = 0;
            // Get all equipment that can have this enchantment
            Map<?, ItemStack> equipment = enchantment.value().getEquipment(entity);
            
            for (ItemStack stack : equipment.values()) {
                if (stack != null && !stack.isEmpty()) {
                    int level = EnchantmentHelper.getLevel(enchantment, stack);
                    if (level > 0) {
                        int cap = TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
                        totalLevel += Math.min(level, cap);
                    }
                }
            }
            cir.setReturnValue(totalLevel);
        } finally {
            manageenchantment$isInternal.set(false);
        }
    }
}
