package org.rmsederhana.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.rmsederhana.config.ConfigManager;
import org.rmsederhana.tier.TierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class TooltipLevelMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void manageenchantment$appendEffectiveLevelToTooltip(Item.TooltipContext context, net.minecraft.entity.player.PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (!ConfigManager.isMaterialTiersEnabled()) return;

        ItemStack stack = (ItemStack) (Object) this;
        int cap = TierManager.getMaxLevel(stack, Integer.MAX_VALUE);
        
        ItemEnchantmentsComponent enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) return;

        List<Text> tooltip = cir.getReturnValue();
        
        for (int i = 0; i < tooltip.size(); i++) {
            Text line = tooltip.get(i);
            for (var entry : enchantments.getEnchantments()) {
                int level = enchantments.getLevel(entry);
                if (level > cap) {
                    Text enchantName = Enchantment.getName(entry, level);
                    // Match the beginning of the name to identify the line
                    if (line.getString().startsWith(enchantName.getString().split(" ")[0])) {
                        if (!line.getString().contains("Effective")) {
                            MutableText mutableLine = line.copy();
                            MutableText effective = Text.literal(" (")
                                    .append(Text.translatable("manageenchantment.effective"))
                                    .append(": " + cap + ")")
                                    .formatted(Formatting.DARK_GRAY);
                            tooltip.set(i, mutableLine.append(effective));
                        }
                    }
                }
            }
        }
    }
}
