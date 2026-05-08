package org.useReducer.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.useReducer.config.ConfigManager;
import org.useReducer.tier.TierManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to remove or raise the anvil's "Too Expensive!" level cap.
 * 
 * In vanilla, if the anvil cost exceeds 40 levels, the output becomes
 * unavailable ("Too Expensive!"). This mixin modifies that constant
 * based on the mod configuration.
 */
@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @Shadow @Final private Property levelCost;

    /**
     * Modify the "Too Expensive!" threshold (vanilla: 40) in updateResult().
     * When remove_anvil_cap is true and max_anvil_cost is 0, effectively removes the cap.
     * When max_anvil_cost > 0, uses that as the new cap.
     */
    @ModifyConstant(method = "updateResult", constant = @Constant(intValue = 40))
    private int manageenchantment$modifyAnvilCap(int original) {
        if (ConfigManager.isAnvilCapRemoved()) {
            int maxCost = ConfigManager.getMaxAnvilCost();
            return maxCost <= 0 ? Integer.MAX_VALUE : maxCost;
        }
        return original;
    }

    /**
     * Applies the discount multiplier and hard cap to the actual XP cost
     * computed at the end of updateResult().
     */
    @Inject(method = "updateResult", at = @At("RETURN"))
    private void manageenchantment$applyCostScaling(CallbackInfo ci) {
        int currentCost = this.levelCost.get();
        if (currentCost <= 0) return;

        // Apply discount
        double discount = ConfigManager.getDiscountMultiplier();
        int newCost = (int) Math.max(1, Math.round(currentCost * discount));

        // Apply hard cap if enabled
        if (ConfigManager.isActualCostCapped()) {
            int maxCost = ConfigManager.getMaxAnvilCost();
            if (maxCost > 0 && newCost > maxCost) {
                newCost = maxCost;
            }
        }

        // Apply new cost
        if (newCost != currentCost) {
            this.levelCost.set(newCost);
        }
    }

    /**
     * Redirects the isOf check in updateResult to allow combining items that share a category.
     */
    @org.spongepowered.asm.mixin.injection.Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean manageenchantment$allowCrossMaterialCombine(ItemStack stack, Item item) {
        if (!ConfigManager.isMaterialTiersEnabled()) return stack.isOf(item);
        
        // 'stack' is the result copy of input[0]
        // 'item' is input[1].getItem()
        return stack.isOf(item) || TierManager.areSameCategory(stack, new ItemStack(item));
    }

    /**
     * Vanilla doubles the item's repair cost each time it goes through the anvil (Prior Work Penalty).
     * If disabled in config, we force the next repair cost to always be 0.
     */
    @Inject(method = "getNextCost", at = @At("HEAD"), cancellable = true)
    private static void manageenchantment$disablePriorWorkPenalty(int cost, CallbackInfoReturnable<Integer> cir) {
        if (ConfigManager.isPriorWorkPenaltyDisabled()) {
            cir.setReturnValue(0);
        }
    }
}
