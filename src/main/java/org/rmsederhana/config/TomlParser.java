package org.rmsederhana.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight TOML parser supporting sections, key-value pairs (int, float, boolean, string),
 * and comments. No external dependencies.
 */
public class TomlParser {

    /**
     * Parses a TOML file into a nested map: section -> (key -> value).
     * Keys outside any section go under the empty string "" key.
     * Values are parsed as Integer, Double, Boolean, or String.
     */
    public static Map<String, Map<String, Object>> parse(Path path) throws IOException {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        String currentSection = "";
        result.put(currentSection, new LinkedHashMap<>());

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Section header
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).trim();
                    result.putIfAbsent(currentSection, new LinkedHashMap<>());
                    continue;
                }

                // Key-value pair
                int eqIndex = line.indexOf('=');
                if (eqIndex > 0) {
                    String key = line.substring(0, eqIndex).trim();
                    
                    // Strip quotes from key if present
                    if ((key.startsWith("\"") && key.endsWith("\"")) ||
                        (key.startsWith("'") && key.endsWith("'"))) {
                        key = key.substring(1, key.length() - 1);
                    }

                    String rawValue = line.substring(eqIndex + 1).trim();

                    // Strip inline comments (only if not inside a quoted string)
                    if (!rawValue.startsWith("\"") && !rawValue.startsWith("'")) {
                        int commentIndex = rawValue.indexOf('#');
                        if (commentIndex > 0) {
                            rawValue = rawValue.substring(0, commentIndex).trim();
                        }
                    }

                    Object value = parseValue(rawValue);
                    result.computeIfAbsent(currentSection, k -> new LinkedHashMap<>()).put(key, value);
                }
            }
        }

        return result;
    }

    /**
     * Parses a raw TOML value string into the appropriate Java type.
     */
    private static Object parseValue(String raw) {
        if (raw.isEmpty()) {
            return "";
        }

        // Boolean
        if (raw.equalsIgnoreCase("true")) return true;
        if (raw.equalsIgnoreCase("false")) return false;

        // Quoted string
        if ((raw.startsWith("\"") && raw.endsWith("\"")) ||
            (raw.startsWith("'") && raw.endsWith("'"))) {
            return raw.substring(1, raw.length() - 1);
        }

        // Integer
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {}

        // Long (for large numbers)
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {}

        // Double
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {}

        // Fallback: treat as string
        return raw;
    }

    /**
     * Helper to get an int from the parsed map with a default value.
     */
    public static int getInt(Map<String, Object> section, String key, int defaultValue) {
        if (section == null) return defaultValue;
        Object val = section.get(key);
        if (val instanceof Number num) {
            return num.intValue();
        }
        return defaultValue;
    }

    /**
     * Helper to get a double from the parsed map with a default value.
     */
    public static double getDouble(Map<String, Object> section, String key, double defaultValue) {
        if (section == null) return defaultValue;
        Object val = section.get(key);
        if (val instanceof Number num) {
            return num.doubleValue();
        }
        return defaultValue;
    }

    /**
     * Helper to get a boolean from the parsed map with a default value.
     */
    public static boolean getBoolean(Map<String, Object> section, String key, boolean defaultValue) {
        if (section == null) return defaultValue;
        Object val = section.get(key);
        if (val instanceof Boolean b) {
            return b;
        }
        return defaultValue;
    }

    /**
     * Helper to get a string from the parsed map with a default value.
     */
    public static String getString(Map<String, Object> section, String key, String defaultValue) {
        if (section == null) return defaultValue;
        Object val = section.get(key);
        if (val instanceof String s) {
            return s;
        }
        return defaultValue;
    }
}
