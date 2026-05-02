package org.rmsederhana.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ItemEnchantmentsComponent.Builder.class)
public class ItemEnchantmentsComponentBuilderMixin {

    @ModifyConstant(method = "set", constant = @Constant(intValue = 255))
    private int manageenchantment$uncapSet(int original) {
        return Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "add", constant = @Constant(intValue = 255))
    private int manageenchantment$uncapAdd(int original) {
        return Integer.MAX_VALUE;
    }
}
