package org.rmsederhana.mixin;

import net.minecraft.world.World;
import org.rmsederhana.config.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerMixin {
    @Inject(method = "getExperience(Lnet/minecraft/world/World;)I", at = @At("RETURN"), cancellable = true)
    private void manageenchantment$modifyGrindstoneXp(World world, CallbackInfoReturnable<Integer> cir) {
        int original = cir.getReturnValue();
        if (original <= 0) return;
        
        double multiplier = ConfigManager.getGrindstoneXpMultiplier();
        int cap = ConfigManager.getGrindstoneXpCap();
        
        int modified = (int) Math.round(original * multiplier);
        if (cap > 0 && modified > cap) {
            modified = cap;
        }
        
        cir.setReturnValue(modified);
    }
}
