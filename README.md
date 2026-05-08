# ManageEnchantment

**ManageEnchantment** is a Fabric utility mod that fundamentally improves the vanilla enchantment system by allowing you to break through artificial level caps, configure universal enchantment power scaling, and completely remove anvil limitations.

## Features

### Global Maximum Level Multiplier
By default, this mod automatically multiplies the maximum allowed level of *every* enchantment by a configurable amount (default is 2x). For example, `Sharpness V` can become `Sharpness X`, and `Efficiency V` becomes `Efficiency X`.

### Per-Enchantment Overrides
If you don't want a global multiplier, you can configure the exact maximum level for any specific enchantment individually in the config file.

### Complete Anvil Freedom
- **"Too Expensive!" Removed:** The vanilla 40-level cap is gone. You can repair and combine items forever.
- **Prior Work Penalty Removed:** Items no longer double in cost every time they pass through the anvil.
- **Cost Capping:** You can configure a hard maximum level cost for any anvil operation so it never gets out of hand.

### Enchanting Table Enhancements
- Removes the vanilla hard cap of 15 bookshelves, allowing for infinite level scaling if you build larger libraries.
- Optionally ban curses from appearing in the enchanting table, villager trades, or chest loot.

## Configuration
The mod generates a `manageenchantment.toml` file in your `config` folder. You can tweak almost every aspect of the mod, including turning on a bypass that allows mutually exclusive enchantments (like Mending and Infinity) to be combined.

## License
This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
