package org.useReducer.config;

import net.fabricmc.loader.api.FabricLoader;
import org.useReducer.ManageEnchantment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages the mod configuration loaded from a TOML file.
 * 
 * Configuration priority for max levels:
 *   1. Per-enchantment override (if not -1)
 *   2. Vanilla max level × global multiplier
 */
public class ConfigManager {

    private static final String CONFIG_FILE_NAME = "manageenchantment.toml";
    
    private static int globalMaxLevelMultiplier = 2;
    private static boolean bypassIncompatibleEnchantments = false;
    private static boolean removeAnvilCap = true;
    private static int maxAnvilCost = 0;
    private static double discountMultiplier = 1.0;
    private static boolean capActualCost = false;
    private static boolean disablePriorWorkPenalty = false;
    private static boolean removeBookshelfCap = true;
    private static double grindstoneXpMultiplier = 1.0;
    private static int grindstoneXpCap = 0;
    private static boolean banCursesFromEnchantingTable = false;
    private static boolean banCursesFromVillagerTrades = false;
    private static boolean banCursesFromChestLoot = false;
    private static String defaultVillagerCapStrategy = "vanilla";
    private static String defaultChestLootCapStrategy = "vanilla";
    private static boolean enableMaterialTiers = true;

    private static final Map<String, Integer> villagerOverrides = new HashMap<>();
    private static final Map<String, Integer> chestLootOverrides = new HashMap<>();
    private static final Map<Integer, List<String>> materialTiers = new TreeMap<>();
    private static final Map<String, List<String>> anvilCategories = new HashMap<>();
    private static final Map<String, Integer> enchantmentOverrides = new HashMap<>();

    // --- Getters ---
    public static int getGlobalMaxLevelMultiplier() { return globalMaxLevelMultiplier; }
    public static boolean isBypassIncompatibleEnchantments() { return bypassIncompatibleEnchantments; }
    public static boolean isAnvilCapRemoved() { return removeAnvilCap; }
    public static int getMaxAnvilCost() { return maxAnvilCost; }
    public static double getDiscountMultiplier() { return discountMultiplier; }
    public static boolean isActualCostCapped() { return capActualCost; }
    public static boolean isPriorWorkPenaltyDisabled() { return disablePriorWorkPenalty; }
    public static boolean isBookshelfCapRemoved() { return removeBookshelfCap; }
    public static double getGrindstoneXpMultiplier() { return grindstoneXpMultiplier; }
    public static int getGrindstoneXpCap() { return grindstoneXpCap; }
    public static boolean isBanCursesFromEnchantingTable() { return banCursesFromEnchantingTable; }
    public static boolean isBanCursesFromVillagerTrades() { return banCursesFromVillagerTrades; }
    public static boolean isBanCursesFromChestLoot() { return banCursesFromChestLoot; }
    public static String getDefaultCapStrategy() { return defaultVillagerCapStrategy; }
    public static String getChestLootDefaultCapStrategy() { return defaultChestLootCapStrategy; }
    public static boolean isMaterialTiersEnabled() { return enableMaterialTiers; }
    public static boolean isIncompatibleBypassEnabled() { return bypassIncompatibleEnchantments; }

    public static Map<Integer, List<String>> getMaterialTiers() { return materialTiers; }
    public static Map<String, List<String>> getAnvilCategories() { return anvilCategories; }
    public static Map<String, Integer> getEnchantmentOverrides() { return enchantmentOverrides; }

    // --- Setters (Required by YACL Config Screen) ---
    public static void setGlobalMaxLevelMultiplier(int val) { globalMaxLevelMultiplier = val; }
    public static void setBypassIncompatibleEnchantments(boolean val) { bypassIncompatibleEnchantments = val; }
    public static void setRemoveAnvilCap(boolean val) { removeAnvilCap = val; }
    public static void setMaxAnvilCost(int val) { maxAnvilCost = val; }
    public static void setDiscountMultiplier(double val) { discountMultiplier = val; }
    public static void setCapActualCost(boolean val) { capActualCost = val; }
    public static void setDisablePriorWorkPenalty(boolean val) { disablePriorWorkPenalty = val; }
    public static void setRemoveBookshelfCap(boolean val) { removeBookshelfCap = val; }
    public static void setGrindstoneXpMultiplier(double val) { grindstoneXpMultiplier = val; }
    public static void setGrindstoneXpCap(int val) { grindstoneXpCap = val; }
    public static void setBanCursesFromEnchantingTable(boolean val) { banCursesFromEnchantingTable = val; }
    public static void setBanCursesFromVillagerTrades(boolean val) { banCursesFromVillagerTrades = val; }
    public static void setBanCursesFromChestLoot(boolean val) { banCursesFromChestLoot = val; }
    public static void setMaterialTiersEnabled(boolean val) { enableMaterialTiers = val; }
    
    public static void setEnchantmentOverrides(Map<String, Integer> map) {
        enchantmentOverrides.clear();
        enchantmentOverrides.putAll(map);
    }

    public static void setMaterialTiers(Map<Integer, List<String>> map) {
        materialTiers.clear();
        materialTiers.putAll(map);
    }

    public static void setAnvilCategories(Map<String, List<String>> map) {
        anvilCategories.clear();
        anvilCategories.putAll(map);
    }

    // --- Business Logic ---
    public static int getVillagerTradeLimit(String enchantId) {
        return villagerOverrides.getOrDefault(enchantId, -1);
    }

    public static int getChestLootLimit(String enchantId) {
        return chestLootOverrides.getOrDefault(enchantId, -1);
    }

    public static int getConfiguredMaxLevel(String fullId, String path, int vanillaMax) {
        int override = enchantmentOverrides.getOrDefault(path, -1);
        if (override != -1) return override;
        return vanillaMax * globalMaxLevelMultiplier;
    }

    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);
        generateConfig(configPath);
        org.useReducer.tier.TierManager.reload();
    }

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configPath = configDir.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configPath)) {
            generateConfig(configPath);
            ManageEnchantment.LOGGER.info("Generated default config at {}", configPath);
        }

        try {
            Map<String, Map<String, Object>> toml = TomlParser.parse(configPath);

            // Parse [general]
            Map<String, Object> general = toml.get("general");
            if (general == null) general = toml.get(""); // fallback for old configs
            
            globalMaxLevelMultiplier = TomlParser.getInt(general, "global_max_level_multiplier", 2);
            bypassIncompatibleEnchantments = TomlParser.getBoolean(general, "bypass_incompatible_enchantments", false);

            Map<String, Object> anvil = toml.get("anvil");
            removeAnvilCap = TomlParser.getBoolean(anvil != null ? anvil : general, "remove_anvil_cap", true);
            maxAnvilCost = TomlParser.getInt(anvil != null ? anvil : general, "max_anvil_cost", 0);
            discountMultiplier = TomlParser.getDouble(anvil != null ? anvil : general, "discount_multiplier", 1.0);
            capActualCost = TomlParser.getBoolean(anvil != null ? anvil : general, "cap_actual_cost", false);
            disablePriorWorkPenalty = TomlParser.getBoolean(anvil != null ? anvil : general, "disable_prior_work_penalty", false);

            Map<String, Object> table = toml.get("enchanting_table");
            removeBookshelfCap = TomlParser.getBoolean(table != null ? table : general, "remove_bookshelf_cap", true);

            Map<String, Object> grindstone = toml.get("grindstone");
            grindstoneXpMultiplier = TomlParser.getDouble(grindstone != null ? grindstone : general, "grindstone_xp_multiplier", 1.0);
            grindstoneXpCap = TomlParser.getInt(grindstone != null ? grindstone : general, "grindstone_xp_cap", 0);

            Map<String, Object> curses = toml.get("curses");
            banCursesFromEnchantingTable = TomlParser.getBoolean(curses != null ? curses : general, "ban_from_enchanting_table", false);
            banCursesFromVillagerTrades = TomlParser.getBoolean(curses != null ? curses : general, "ban_from_villager_trades", false);
            banCursesFromChestLoot = TomlParser.getBoolean(curses != null ? curses : general, "ban_from_chest_loot", false);

            Map<String, Object> villager = toml.get("villager_trades");
            defaultVillagerCapStrategy = TomlParser.getString(villager != null ? villager : general, "default_cap_strategy", "vanilla");
            villagerOverrides.clear();
            Map<String, Object> vOverrides = toml.get("villager_trades.overrides");
            if (vOverrides != null) {
                for (Map.Entry<String, Object> entry : vOverrides.entrySet()) {
                    if (entry.getValue() instanceof Number) villagerOverrides.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }

            Map<String, Object> chest = toml.get("chest_loot");
            defaultChestLootCapStrategy = TomlParser.getString(chest != null ? chest : general, "default_cap_strategy", "vanilla");
            chestLootOverrides.clear();
            Map<String, Object> cOverrides = toml.get("chest_loot.overrides");
            if (cOverrides != null) {
                for (Map.Entry<String, Object> entry : cOverrides.entrySet()) {
                    if (entry.getValue() instanceof Number) chestLootOverrides.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }

            Map<String, Object> tiers = toml.get("material_tiers");
            enableMaterialTiers = TomlParser.getBoolean(tiers != null ? tiers : general, "enable_material_tiers", true);
            materialTiers.clear();
            if (tiers != null) {
                for (Map.Entry<String, Object> entry : tiers.entrySet()) {
                    try {
                        int level = Integer.parseInt(entry.getKey());
                        if (entry.getValue() instanceof List) {
                            List<String> list = new ArrayList<>();
                            for (Object o : (List<?>) entry.getValue()) list.add(o.toString());
                            materialTiers.put(level, list);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            Map<String, Object> categories = toml.get("anvil_categories");
            anvilCategories.clear();
            if (categories != null) {
                for (Map.Entry<String, Object> entry : categories.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        List<String> list = new ArrayList<>();
                        for (Object o : (List<?>) entry.getValue()) list.add(o.toString());
                        anvilCategories.put(entry.getKey(), list);
                    }
                }
            }

            Map<String, Object> enchants = toml.get("enchantments");
            enchantmentOverrides.clear();
            if (enchants != null) {
                for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                    if (entry.getValue() instanceof Number) enchantmentOverrides.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }

            org.useReducer.tier.TierManager.reload();
            ManageEnchantment.LOGGER.info("Config loaded: multiplier={}, anvil_cap_removed={}, {} enchantment overrides",
                    globalMaxLevelMultiplier, removeAnvilCap, enchantmentOverrides.size());

        } catch (Exception e) {
            ManageEnchantment.LOGGER.error("Failed to load config: {}", e.getMessage());
        }
    }

    private static void generateConfig(Path path) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ManageEnchantment Configuration\n\n");

        sb.append("[general]\n");
        sb.append("global_max_level_multiplier = ").append(globalMaxLevelMultiplier).append("\n");
        sb.append("bypass_incompatible_enchantments = ").append(bypassIncompatibleEnchantments).append("\n\n");

        sb.append("[anvil]\n");
        sb.append("remove_anvil_cap = ").append(removeAnvilCap).append("\n");
        sb.append("max_anvil_cost = ").append(maxAnvilCost).append("\n");
        sb.append("discount_multiplier = ").append(discountMultiplier).append("\n");
        sb.append("cap_actual_cost = ").append(capActualCost).append("\n");
        sb.append("disable_prior_work_penalty = ").append(disablePriorWorkPenalty).append("\n\n");

        sb.append("[enchanting_table]\n");
        sb.append("remove_bookshelf_cap = ").append(removeBookshelfCap).append("\n\n");

        sb.append("[grindstone]\n");
        sb.append("grindstone_xp_multiplier = ").append(grindstoneXpMultiplier).append("\n");
        sb.append("grindstone_xp_cap = ").append(grindstoneXpCap).append("\n\n");

        sb.append("[curses]\n");
        sb.append("ban_from_enchanting_table = ").append(banCursesFromEnchantingTable).append("\n");
        sb.append("ban_from_villager_trades = ").append(banCursesFromVillagerTrades).append("\n");
        sb.append("ban_from_chest_loot = ").append(banCursesFromChestLoot).append("\n\n");

        sb.append("[material_tiers]\n");
        sb.append("enable_material_tiers = ").append(enableMaterialTiers).append("\n\n");
        
        if (materialTiers.isEmpty()) {
            sb.append("1 = [\"minecraft:wooden_sword\", \"minecraft:wooden_pickaxe\", \"minecraft:wooden_axe\", \"minecraft:wooden_shovel\", \"minecraft:wooden_hoe\", ");
            sb.append("\"minecraft:stone_sword\", \"minecraft:stone_pickaxe\", \"minecraft:stone_axe\", \"minecraft:stone_shovel\", \"minecraft:stone_hoe\", ");
            sb.append("\"#minecraft:leather_armor\"]\n");
            sb.append("2 = [\"minecraft:iron_sword\", \"minecraft:iron_pickaxe\", \"minecraft:iron_axe\", \"minecraft:iron_shovel\", \"minecraft:iron_hoe\", ");
            sb.append("\"minecraft:golden_sword\", \"minecraft:golden_pickaxe\", \"minecraft:golden_axe\", \"minecraft:golden_shovel\", \"minecraft:golden_hoe\", ");
            sb.append("\"#minecraft:iron_armor\", \"#minecraft:golden_armor\", \"#minecraft:chainmail_armor\"]\n");
            sb.append("3 = [\"minecraft:diamond_sword\", \"minecraft:diamond_pickaxe\", \"minecraft:diamond_axe\", \"minecraft:diamond_shovel\", \"minecraft:diamond_hoe\", ");
            sb.append("\"#minecraft:diamond_armor\"]\n");
            sb.append("4 = [\"minecraft:netherite_sword\", \"minecraft:netherite_pickaxe\", \"minecraft:netherite_axe\", \"minecraft:netherite_shovel\", \"minecraft:netherite_hoe\", ");
            sb.append("\"#minecraft:netherite_armor\", \"minecraft:elytra\", \"minecraft:trident\"]\n\n");
        } else {
            for (Map.Entry<Integer, List<String>> entry : materialTiers.entrySet()) {
                sb.append(entry.getKey()).append(" = [");
                for (int i = 0; i < entry.getValue().size(); i++) {
                    sb.append("\"").append(entry.getValue().get(i)).append("\"");
                    if (i < entry.getValue().size() - 1) sb.append(", ");
                }
                sb.append("]\n");
            }
            sb.append("\n");
        }

        sb.append("[anvil_categories]\n");
        if (anvilCategories.isEmpty()) {
            sb.append("swords = [\"#minecraft:swords\"]\n");
            sb.append("pickaxes = [\"#minecraft:pickaxes\"]\n");
            sb.append("axes = [\"#minecraft:axes\"]\n");
            sb.append("shovels = [\"#minecraft:shovels\"]\n");
            sb.append("hoes = [\"#minecraft:hoes\"]\n");
            sb.append("helmets = [\"#minecraft:helmets\"]\n");
            sb.append("chestplates = [\"#minecraft:chestplates\"]\n");
            sb.append("leggings = [\"#minecraft:leggings\"]\n");
            sb.append("boots = [\"#minecraft:boots\"]\n\n");
        } else {
            for (Map.Entry<String, List<String>> entry : anvilCategories.entrySet()) {
                sb.append(entry.getKey()).append(" = [");
                for (int i = 0; i < entry.getValue().size(); i++) {
                    sb.append("\"").append(entry.getValue().get(i)).append("\"");
                    if (i < entry.getValue().size() - 1) sb.append(", ");
                }
                sb.append("]\n");
            }
            sb.append("\n");
        }

        sb.append("[enchantments]\n");
        if (enchantmentOverrides.isEmpty()) {
            sb.append("sharpness = -1\n");
            sb.append("efficiency = -1\n");
        } else {
            for (Map.Entry<String, Integer> entry : enchantmentOverrides.entrySet()) {
                sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
            }
        }
        
        try {
            Files.writeString(path, sb.toString());
        } catch (IOException e) {
            ManageEnchantment.LOGGER.error("Failed to generate default config: {}", e.getMessage());
        }
    }
}
