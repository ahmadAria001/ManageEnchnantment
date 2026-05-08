package org.useReducer.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.useReducer.ManageEnchantment;
import org.useReducer.config.ConfigManager;

import java.util.*;

public class ManageEnchantmentConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("ManageEnchantment Configuration"));

        builder.setSavingRunnable(() -> {
            ConfigManager.save();
        });

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder
                .startIntField(Text.literal("Global Max Level Multiplier"), ConfigManager.getGlobalMaxLevelMultiplier())
                .setDefaultValue(2)
                .setSaveConsumer(ConfigManager::setGlobalMaxLevelMultiplier)
                .build());

        general.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Bypass Incompatible Enchantments"),
                        ConfigManager.isIncompatibleBypassEnabled())
                .setDefaultValue(false)
                .setSaveConsumer(ConfigManager::setBypassIncompatibleEnchantments)
                .build());

        ConfigCategory anvil = builder.getOrCreateCategory(Text.literal("Anvil"));

        anvil.addEntry(
                entryBuilder.startBooleanToggle(Text.literal("Remove Anvil Cap"), ConfigManager.isAnvilCapRemoved())
                        .setDefaultValue(true)
                        .setSaveConsumer(ConfigManager::setRemoveAnvilCap)
                        .build());

        anvil.addEntry(entryBuilder.startIntField(Text.literal("Max Anvil Cost"), ConfigManager.getMaxAnvilCost())
                .setDefaultValue(0)
                .setSaveConsumer(ConfigManager::setMaxAnvilCost)
                .build());

        anvil.addEntry(entryBuilder
                .startDoubleField(Text.literal("Discount Multiplier"), ConfigManager.getDiscountMultiplier())
                .setDefaultValue(1.0)
                .setSaveConsumer(ConfigManager::setDiscountMultiplier)
                .build());

        anvil.addEntry(
                entryBuilder.startBooleanToggle(Text.literal("Cap Actual Cost"), ConfigManager.isActualCostCapped())
                        .setDefaultValue(false)
                        .setSaveConsumer(ConfigManager::setCapActualCost)
                        .build());

        anvil.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Disable Prior Work Penalty"),
                        ConfigManager.isPriorWorkPenaltyDisabled())
                .setDefaultValue(false)
                .setSaveConsumer(ConfigManager::setDisablePriorWorkPenalty)
                .build());

        ConfigCategory enchantingTable = builder.getOrCreateCategory(Text.literal("Misc"));
        enchantingTable.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Remove Bookshelf Cap"), ConfigManager.isBookshelfCapRemoved())
                .setDefaultValue(true)
                .setSaveConsumer(ConfigManager::setRemoveBookshelfCap)
                .build());

        ConfigCategory grindstone = builder.getOrCreateCategory(Text.literal("Grindstone"));
        grindstone.addEntry(
                entryBuilder.startDoubleField(Text.literal("XP Multiplier"), ConfigManager.getGrindstoneXpMultiplier())
                        .setDefaultValue(1.0)
                        .setTooltip(Text.literal("Multiplier applied to the XP dropped when disenchanting an item."))
                        .setSaveConsumer(ConfigManager::setGrindstoneXpMultiplier)
                        .build());
        grindstone.addEntry(entryBuilder.startIntField(Text.literal("XP Cap"), ConfigManager.getGrindstoneXpCap())
                .setDefaultValue(0)
                .setTooltip(
                        Text.literal("The maximum XP that a single grindstone operation can drop. 0 means unlimited."))
                .setSaveConsumer(ConfigManager::setGrindstoneXpCap)
                .build());

        ConfigCategory curses = builder.getOrCreateCategory(Text.literal("Curses"));
        curses.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Ban from Enchanting Table"),
                        ConfigManager.isBanCursesFromEnchantingTable())
                .setDefaultValue(false)
                .setTooltip(Text.literal("If true, curses will never appear in the enchanting table."))
                .setSaveConsumer(ConfigManager::setBanCursesFromEnchantingTable)
                .build());
        curses.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Ban from Villager Trades"),
                        ConfigManager.isBanCursesFromVillagerTrades())
                .setDefaultValue(false)
                .setTooltip(Text.literal("If true, villagers will never offer cursed enchanted books."))
                .setSaveConsumer(ConfigManager::setBanCursesFromVillagerTrades)
                .build());
        curses.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Ban from Chest Loot"), ConfigManager.isBanCursesFromChestLoot())
                .setDefaultValue(false)
                .setTooltip(Text.literal("If true, curses will never generate in chest loot."))
                .setSaveConsumer(ConfigManager::setBanCursesFromChestLoot)
                .build());

        ConfigCategory overrides = builder.getOrCreateCategory(Text.literal("Overrides"));

        List<String> newOverrides = new ArrayList<>();
        ConfigManager.getEnchantmentOverrides().forEach((k, v) -> newOverrides.add(k + "=" + v));

        overrides.addEntry(entryBuilder.startStrList(Text.literal("Enchantment Max Levels"), newOverrides)
                .setTooltip(Text.literal("Format: enchantment_id=level (e.g. sharpness=10). One entry per line."))
                .setSaveConsumer(list -> {
                    Map<String, Integer> map = new LinkedHashMap<>();
                    for (String s : list) {
                        String[] parts = s.split("=");
                        if (parts.length == 2) {
                            try {
                                map.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    ConfigManager.setEnchantmentOverrides(map);
                })
                .build());

        ConfigCategory tiers = builder.getOrCreateCategory(Text.literal("Material Tiers"));
        tiers.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enable Material Tiers"), ConfigManager.isMaterialTiersEnabled())
                .setDefaultValue(false)
                .setTooltip(
                        Text.literal("If enabled, enchantment levels will be capped based on the item material/tier."))
                .setSaveConsumer(ConfigManager::setMaterialTiersEnabled)
                .build());

        List<String> tierList = new ArrayList<>();
        ConfigManager.getMaterialTiers().forEach((level, items) -> {
            for (String item : items) {
                tierList.add(level + "=" + item);
            }
        });

        tiers.addEntry(entryBuilder.startStrList(Text.literal("Tiers"), tierList)
                .setTooltip(Text.literal("Format: level=item (e.g. 1=#minecraft:wooden_tools). One entry per line."))
                .setSaveConsumer(list -> {
                    Map<Integer, List<String>> map = new LinkedHashMap<>();
                    for (String s : list) {
                        String[] parts = s.split("=");
                        if (parts.length == 2) {
                            try {
                                int level = Integer.parseInt(parts[0].trim());
                                String item = parts[1].trim();
                                map.computeIfAbsent(level, k -> new ArrayList<>()).add(item);
                            } catch (
                            // NumberFormatException ignored
                            Exception e) {
                                ManageEnchantment.LOGGER.error("Failed to parse material tier: {}", s);
                            }
                        }
                    }
                    ManageEnchantment.LOGGER.info("Material tiers: {}", map);
                    ConfigManager.setMaterialTiers(map);
                })
                .setExpanded(true)
                .build());

        ConfigCategory categories = builder.getOrCreateCategory(Text.literal("Anvil Categories"));
        List<String> catList = new ArrayList<>();
        ConfigManager.getAnvilCategories().forEach((name, items) -> {
            for (String item : items) {
                catList.add(name + "=" + item);
            }
        });

        categories.addEntry(entryBuilder.startStrList(Text.literal("Categories"), catList)
                .setTooltip(Text.literal("Format: category=item (e.g. swords=#minecraft:swords). One entry per line."))
                .setSaveConsumer(list -> {
                    Map<String, List<String>> map = new LinkedHashMap<>();
                    for (String s : list) {
                        String[] parts = s.split("=");
                        if (parts.length == 2) {
                            String name = parts[0].trim();
                            String item = parts[1].trim();
                            map.computeIfAbsent(name, k -> new ArrayList<>()).add(item);
                        }
                    }
                    ConfigManager.setAnvilCategories(map);
                })
                .setExpanded(true)
                .build());

        builder.setSavingRunnable(ConfigManager::save);

        return builder.build();
    }
}
