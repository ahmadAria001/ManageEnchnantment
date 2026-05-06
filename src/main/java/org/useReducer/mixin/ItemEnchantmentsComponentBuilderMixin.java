package org.useReducer.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import org.useReducer.ManageEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantmentsComponent.Builder.class)
public class ItemEnchantmentsComponentBuilderMixin {

    /**
     * Uncaps the Math.min(level, 255) inside the set() method.
     */
    @ModifyConstant(method = "set", constant = @Constant(intValue = 255))
    private int manageenchantment$uncapSet(int original) {
        ManageEnchantment.LOGGER.info("[ManageEnchantment] Builder.set uncap fired: {} -> {}", original, Integer.MAX_VALUE);
        return Integer.MAX_VALUE;
    }

    /**
     * Uncaps the Math.min(level, 255) inside the add() method.
     */
    @ModifyConstant(method = "add", constant = @Constant(intValue = 255))
    private int manageenchantment$uncapAdd(int original) {
        ManageEnchantment.LOGGER.info("[ManageEnchantment] Builder.add uncap fired: {} -> {}", original, Integer.MAX_VALUE);
        return Integer.MAX_VALUE;
    }
}
