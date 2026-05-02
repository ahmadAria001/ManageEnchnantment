package org.rmsederhana.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.rmsederhana.tier.TierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Enchantment.class)
public class EnchantmentTierMixin {

    @ModifyVariable(method = "modifyDamage", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capDamageLevel(int level, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }

    @ModifyVariable(method = "modifyDamageProtection", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capProtectionLevel(int level, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }

    @ModifyVariable(method = "modifyKnockback", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capKnockbackLevel(int level, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }

    @ModifyVariable(method = "modifyArmorEffectiveness", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capArmorEffectLevel(int level, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }

    @ModifyVariable(method = "modifyBlockExperience", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capBlockXpLevel(int level, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }

    @ModifyVariable(method = "modifyMobExperience", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capMobXpLevel(int level, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }

    @ModifyVariable(method = "modifyValue(Lnet/minecraft/component/ComponentType;Lnet/minecraft/server/world/ServerWorld;ILnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lorg/apache/commons/lang3/mutable/MutableFloat;)V", at = @At("HEAD"), argsOnly = true)
    private int manageenchantment$capValueLevel(int level, net.minecraft.component.ComponentType<?> type, ServerWorld world, int level_dup, ItemStack stack) {
        return TierManager.getMaxLevel(stack, level);
    }
}
