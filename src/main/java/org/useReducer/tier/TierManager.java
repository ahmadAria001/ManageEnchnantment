package org.useReducer.tier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.useReducer.ManageEnchantment;
import org.useReducer.config.ConfigManager;

import java.util.*;

public class TierManager {

    private static final Map<Integer, List<ItemMatcher>> tierMatchers = new TreeMap<>();
    private static final Map<String, List<ItemMatcher>> categoryMatchers = new HashMap<>();

    public static void reload() {
        tierMatchers.clear();
        for (Map.Entry<Integer, List<String>> entry : ConfigManager.getMaterialTiers().entrySet()) {
            List<ItemMatcher> matchers = new ArrayList<>();
            for (String s : entry.getValue()) {
                matchers.add(parseMatcher(s));
            }
            tierMatchers.put(entry.getKey(), matchers);
        }

        categoryMatchers.clear();
        for (Map.Entry<String, List<String>> entry : ConfigManager.getAnvilCategories().entrySet()) {
            List<ItemMatcher> matchers = new ArrayList<>();
            for (String s : entry.getValue()) {
                matchers.add(parseMatcher(s));
            }
            categoryMatchers.put(entry.getKey(), matchers);
        }
    }

    public static int getMaxLevel(ItemStack stack, int defaultMax) {
        if (!ConfigManager.isMaterialTiersEnabled() || stack.isEmpty())
            return defaultMax;

        if (tierMatchers.isEmpty()) {
            reload();
        }

        int foundMax = -1;
        for (Map.Entry<Integer, List<ItemMatcher>> entry : tierMatchers.entrySet()) {
            for (ItemMatcher matcher : entry.getValue()) {
                if (matcher.matches(stack)) {
                    foundMax = Math.max(foundMax, entry.getKey());
                    break;
                }
            }
        }
        
        return foundMax != -1 ? foundMax : defaultMax;
    }

    public static boolean areSameCategory(ItemStack s1, ItemStack s2) {
        if (s1.isEmpty() || s2.isEmpty())
            return false;

        if (s1.isOf(s2.getItem()))
            return true;

        for (List<ItemMatcher> matchers : categoryMatchers.values()) {
            boolean m1 = false;
            boolean m2 = false;
            for (ItemMatcher matcher : matchers) {
                if (!m1 && matcher.matches(s1))
                    m1 = true;
                if (!m2 && matcher.matches(s2))
                    m2 = true;
                if (m1 && m2)
                    return true;
            }
        }

        return false;
    }

    private static ItemMatcher parseMatcher(String s) {
        if (s.startsWith("#")) {
            return new TagMatcher(TagKey.of(Registries.ITEM.getKey(), Identifier.of(s.substring(1))));
        } else {
            return new IdMatcher(Identifier.of(s));
        }
    }

    private interface ItemMatcher {
        boolean matches(ItemStack stack);
    }

    private record TagMatcher(TagKey<Item> tag) implements ItemMatcher {
        @Override
        public boolean matches(ItemStack stack) {
            return stack.isIn(tag);
        }
    }

    private record IdMatcher(Identifier id) implements ItemMatcher {
        @Override
        public boolean matches(ItemStack stack) {
            return Registries.ITEM.getId(stack.getItem()).equals(id);
        }
    }
}
