package org.rmsederhana.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import org.rmsederhana.config.ConfigManager;
import org.rmsederhana.tier.TierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getEnchantments()Lnet/minecraft/component/type/ItemEnchantmentsComponent;", at = @At("RETURN"), cancellable = true)
    private void manageenchantment$capEnchantments(CallbackInfoReturnable<ItemEnchantmentsComponent> cir) {
        if (!ConfigManager.isMaterialTiersEnabled()) return;

        ItemEnchantmentsComponent original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;

        ItemStack stack = (ItemStack) (Object) this;
        int cap = TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
        if (cap == Integer.MAX_VALUE) return;

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(original);
        boolean changed = false;

        for (RegistryEntry<Enchantment> entry : original.getEnchantments()) {
            int level = original.getLevel(entry);
            if (level > cap) {
                builder.set(entry, cap);
                changed = true;
            }
        }

        if (changed) {
            cir.setReturnValue(builder.build());
        }
    }
}
