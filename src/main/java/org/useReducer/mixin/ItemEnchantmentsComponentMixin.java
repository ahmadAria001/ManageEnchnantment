package org.useReducer.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import org.useReducer.ManageEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantmentsComponent.class)
public class ItemEnchantmentsComponentMixin {

    /**
     * Uncaps the ENCHANTMENT_LEVEL_CODEC from Codec.intRange(1, 255) 
     * to Codec.intRange(1, Integer.MAX_VALUE).
     */
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 255))
    private static int manageenchantment$uncapCodec(int original) {
        ManageEnchantment.LOGGER.info("[ManageEnchantment] Uncapped ENCHANTMENT_LEVEL_CODEC from {} to {}", original, Integer.MAX_VALUE);
        return Integer.MAX_VALUE;
    }

    /**
     * Uncaps the validation check in the constructor that throws
     * IllegalArgumentException when level > 255.
     */
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 255))
    private int manageenchantment$uncapConstructorValidation(int original) {
        return Integer.MAX_VALUE;
    }
}
