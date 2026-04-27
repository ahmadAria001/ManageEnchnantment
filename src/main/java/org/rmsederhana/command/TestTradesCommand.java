package org.rmsederhana.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.rmsederhana.enchantment.EnchantmentOverrideManager;

import java.util.HashMap;
import java.util.Map;

public class TestTradesCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("manageenchantment")
                .then(CommandManager.literal("testtrades")
                        .executes(context -> execute(context.getSource(), 100))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 100000))
                                .executes(context -> execute(context.getSource(), IntegerArgumentType.getInteger(context, "amount")))
                        )
                )
        );
    }

    private static int execute(ServerCommandSource source, int amount) {
        ServerWorld world = source.getWorld();
        Random random = world.getRandom();
        
        // This is exactly how Librarians create their enchanted book trades.
        // It passes '1' for experience, and uses the TRADEABLE tag.
        TradeOffers.EnchantBookFactory factory = new TradeOffers.EnchantBookFactory(1, EnchantmentTags.TRADEABLE);
        
        Map<String, Integer> highestLevels = new HashMap<>();
        Map<String, Integer> highestPrices = new HashMap<>();
        int totalEnchantments = 0;
        
        for (int i = 0; i < amount; i++) {
            TradeOffer offer = factory.create(world, null, random);
            if (offer != null) {
                ItemStack result = offer.getSellItem();
                if (result.isOf(Items.ENCHANTED_BOOK)) {
                    var enchantments = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(result);
                    for (var entry : enchantments.getEnchantmentEntries()) {
                        RegistryEntry<Enchantment> registryEntry = entry.getKey();
                        int level = entry.getIntValue();
                        
                        String path = EnchantmentOverrideManager.getPath(registryEntry.value());
                        if (path == null) path = "unknown";
                        
                        highestLevels.put(path, Math.max(highestLevels.getOrDefault(path, 0), level));
                        
                        // Original first buy item is always the Emerald cost
                        int cost = offer.getOriginalFirstBuyItem().getCount();
                        highestPrices.put(path, Math.max(highestPrices.getOrDefault(path, 0), cost));
                        
                        totalEnchantments++;
                    }
                }
            }
        }
        
        final int finalTotal = totalEnchantments;
        source.sendFeedback(() -> Text.literal("§aGenerated " + amount + " Librarian trades. Total enchanted books: " + finalTotal), false);
        for (Map.Entry<String, Integer> entry : highestLevels.entrySet()) {
            final String path = entry.getKey();
            final int maxLevel = entry.getValue();
            final int maxPrice = highestPrices.getOrDefault(path, 0);
            source.sendFeedback(() -> Text.literal("§7- " + path + ": Max Level Seen: " + maxLevel + " (Highest Cost: " + maxPrice + " Emeralds)"), false);
        }
        
        return 1;
    }
}
