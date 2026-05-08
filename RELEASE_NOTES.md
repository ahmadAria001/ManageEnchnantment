# ManageEnchantment v1.0.1-4

This release focuses heavily on internal stability, professional metadata cleanup, and preparing the mod for official Modrinth approval. We've squashed some typos, refactored our packages, and made sure our mixins are rock solid for production servers!

### ✨ Changes & Improvements
* **Mod ID & Metadata Overhaul:** Fixed a persistent typo across the entire project. The Mod ID is now correctly spelled as `manageenchantment` (previously `ManageEnchantment`). 
* **Author & Package Refactor:** Updated the author name and maven group to `useReducer`. All internal Java packages have been successfully migrated from `org.useReducer` to `org.useReducer`.
* **Documentation Rewrite:** A completely rewritten `README.md` that explicitly outlines all the features of the mod, including the global multiplier, anvil freedom, and tier-based limits, ensuring compliance with Modrinth's "Clear and Honest Function" rules.

### 🐛 Bug Fixes
* **Critical Mixin Stability:** Added the missing `refmap` (`manageenchantment-refmap.json` and `manageenchantment-client-refmap.json`) to both the main and client mixin configurations. This resolves potential "Mixin not found" crashes when running the mod in a production environment.
* **Asset Pathing:** Fixed the internal assets directory path to match the corrected Mod ID (`assets/manageenchantment`).

> **Note for Upgrading:** Because the Mod ID has changed, any existing configuration files named `ManageEnchantment.toml` will no longer be read. A new `manageenchantment.toml` will be generated on startup. You will need to manually copy your settings over if you had custom overrides.
