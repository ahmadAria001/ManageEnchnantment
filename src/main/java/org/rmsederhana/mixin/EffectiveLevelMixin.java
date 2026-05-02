package org.rmsederhana.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.rmsederhana.config.ConfigManager;
import org.rmsederhana.tier.TierManager;
import org.rmsederhana.util.EnchantmentContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(EnchantmentHelper.class)
public abstract class EffectiveLevelMixin {

    @Unique
    private static final ThreadLocal<Boolean> manageenchantment$isInternal = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getLevel", at = @At("HEAD"))
    private static void manageenchantment$captureStack(RegistryEntry<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        EnchantmentContext.set(stack);
    }

    @Inject(method = "getLevel", at = @At("RETURN"), cancellable = true)
    private static void manageenchantment$capLevel(RegistryEntry<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (manageenchantment$isInternal.get() || !ConfigManager.isMaterialTiersEnabled() || stack == null || stack.isEmpty()) return;
        
        int level = cir.getReturnValue();
        if (level <= 0) return;

        manageenchantment$isInternal.set(true);
        try {
            int cap = TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
            if (level > cap) cir.setReturnValue(cap);
        } finally {
            manageenchantment$isInternal.set(false);
            EnchantmentContext.clear();
        }
    }

    /**
     * Fix for Mining Speed (Efficiency) and other entity-wide checks.
     * We manually iterate and cap each item to avoid the "Internal Bypass" bug.
     */
    @Inject(method = "getEquipmentLevel", at = @At("HEAD"), cancellable = true)
    private static void manageenchantment$capEquipmentLevel(RegistryEntry<Enchantment> enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (manageenchantment$isInternal.get() || !ConfigManager.isMaterialTiersEnabled()) return;

        manageenchantment$isInternal.set(true);
        try {
            int total = 0;
            for (ItemStack stack : enchantment.value().getEquipment(entity).values()) {
                if (stack == null || stack.isEmpty()) continue;
                
                ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
                if (enchantments != null) {
                    int rawLevel = enchantments.getLevel(enchantment);
                    if (rawLevel > 0) {
                        int cap = TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
                        total += Math.min(rawLevel, cap);
                    }
                }
            }
            cir.setReturnValue(total);
        } finally {
            manageenchantment$isInternal.set(false);
        }
    }

    @Inject(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V", at = @At("HEAD"))
    private static void manageenchantment$captureStackForEach(ItemStack stack, EnchantmentHelper.Consumer consumer, CallbackInfo ci) {
        EnchantmentContext.set(stack);
    }

    @Inject(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V", at = @At("RETURN"))
    private static void manageenchantment$releaseStackForEach(ItemStack stack, EnchantmentHelper.Consumer consumer, CallbackInfo ci) {
        EnchantmentContext.clear();
    }

    @Inject(method = "applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"))
    private static void manageenchantment$captureStackAttributes(ItemStack stack, EquipmentSlot slot, BiConsumer consumer, CallbackInfo ci) {
        EnchantmentContext.set(stack);
    }

    @Inject(method = "applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("RETURN"))
    private static void manageenchantment$releaseStackAttributes(ItemStack stack, EquipmentSlot slot, BiConsumer consumer, CallbackInfo ci) {
        EnchantmentContext.clear();
    }

    @Inject(method = "applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"))
    private static void manageenchantment$captureStackAttributeSlot(ItemStack stack, AttributeModifierSlot slot, BiConsumer consumer, CallbackInfo ci) {
        EnchantmentContext.set(stack);
    }

    @Inject(method = "applyAttributeModifiers(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V", at = @At("RETURN"))
    private static void manageenchantment$releaseStackAttributeSlot(ItemStack stack, AttributeModifierSlot slot, BiConsumer consumer, CallbackInfo ci) {
        EnchantmentContext.clear();
    }
}
