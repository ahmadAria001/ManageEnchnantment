# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1-5] - 2026-05-08

### Added
- **Enchantment Override System:** Added the ability to globally multiply enchantment max levels or set per-enchantment overrides via the configuration file.
- **Anvil Freedom:** Introduced features to remove the 40-level anvil cap, disable prior work penalties, and apply hard caps to anvil costs.
- **Enchanting Table Scaling:** Removed the vanilla 15-bookshelf limit, allowing for infinite level scaling.
- **Automated Publishing:** Added GitHub Actions workflows for automated project building and publishing to Modrinth and GitHub Releases.
- **Documentation:** Created a comprehensive `README.md` to comply with Modrinth's "Clear and Honest Function" rules.

### Changed
- **Metadata Overhaul:** Fixed a pervasive typo in the Mod ID, renaming it from `manageenchnantment` to `manageenchantment` across the entire project.
- **Author Refactor:** Refactored all internal Java packages and maven groups from `org.rmsederhana` to `org.useReducer` to accurately reflect project authorship.
- **Asset Paths:** Updated all asset paths and configuration file names to match the corrected Mod ID.

### Fixed
- **Mixin Stability:** Added the missing `refmap` entries to both main and client mixin configurations (`manageenchantment.mixins.json` and `manageenchantment.client.mixins.json`) to prevent potential "Mixin not found" crashes in production environments.
