package org.useReducer.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.EnchantWithLevelsLootFunction;
import net.minecraft.registry.entry.RegistryEntry;
import org.useReducer.config.ConfigManager;
import org.useReducer.enchantment.EnchantmentOverrideManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EnchantRandomlyLootFunction.class, EnchantWithLevelsLootFunction.class})
public class ChestLootEnchantmentMixin {

    @Inject(method = "process", at = @At("RETURN"), cancellable = true)
    private void manageenchantment$capChestLootEnchantments(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty()) {
            return;
        }

        ItemEnchantmentsComponent enchantments = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(result);
        if (enchantments.isEmpty()) {
            return;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
        boolean modified = false;

        for (var entry : enchantments.getEnchantmentEntries()) {
            RegistryEntry<Enchantment> registryEntry = entry.getKey();
            Enchantment enchantment = registryEntry.value();
            int currentLevel = entry.getIntValue();

            boolean isCurse = registryEntry.isIn(net.minecraft.registry.tag.EnchantmentTags.CURSE);
            if (isCurse && ConfigManager.isBanCursesFromChestLoot()) {
                builder.remove(i -> i.equals(registryEntry));
                modified = true;
                continue;
            }

            String path = EnchantmentOverrideManager.getPath(enchantment);
            if (path == null) {
                continue; // Unmapped enchantment
            }

            int specificLimit = ConfigManager.getChestLootLimit(path);

            if (specificLimit == 0) {
                // Banned entirely from chest loot
                builder.remove(i -> i.equals(registryEntry));
                modified = true;
            } else if (specificLimit > 0) {
                // Cap to specific limit
                if (currentLevel > specificLimit) {
                    builder.set(registryEntry, specificLimit);
                    modified = true;
                }
            } else {
                // No specific limit, fallback to default strategy
                if ("vanilla".equalsIgnoreCase(ConfigManager.getChestLootDefaultCapStrategy())) {
                    // Try to cap at original vanilla max level
                    int vanillaMax = enchantment.definition().maxLevel();
                    if (currentLevel > vanillaMax) {
                        builder.set(registryEntry, vanillaMax);
                        modified = true;
                    }
                }
                // If "uncapped", we leave it alone (it can be up to the global override limit)
            }
        }

        if (modified) {
            net.minecraft.enchantment.EnchantmentHelper.set(result, builder.build());
            cir.setReturnValue(result);
        }
    }
}
