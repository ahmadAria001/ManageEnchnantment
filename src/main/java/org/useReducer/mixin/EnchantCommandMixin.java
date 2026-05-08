package org.useReducer.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.EnchantCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.useReducer.ManageEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Debug mixin to capture and log the actual exception from the /enchant command.
 */
@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {

    @Inject(method = "execute", at = @At("HEAD"))
    private static void manageenchantment$logEnchantAttempt(
            ServerCommandSource source,
            Collection<? extends Entity> targets,
            RegistryEntry<Enchantment> enchantment,
            int level,
            CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ManageEnchantment.LOGGER.info("[ManageEnchantment] /enchant command: level={}, enchantment={}", level, enchantment);
    }

    @Inject(method = "execute", at = @At("RETURN"))
    private static void manageenchantment$logEnchantSuccess(
            ServerCommandSource source,
            Collection<? extends Entity> targets,
            RegistryEntry<Enchantment> enchantment,
            int level,
            CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ManageEnchantment.LOGGER.info("[ManageEnchantment] /enchant command SUCCESS: level={}, result={}", level, cir.getReturnValue());
    }
}
