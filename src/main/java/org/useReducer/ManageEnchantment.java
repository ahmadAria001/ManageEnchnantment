package org.useReducer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.useReducer.command.TestTradesCommand;
import org.useReducer.config.ConfigManager;
import org.useReducer.enchantment.EnchantmentOverrideManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageEnchantment implements ModInitializer {
	public static final String MOD_ID = "manageenchantment";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[ManageEnchantment] Initializing...");

		// Load configuration from TOML file (generates defaults on first run)
		ConfigManager.load();

		// Register commands
		CommandRegistrationCallback.EVENT.register(TestTradesCommand::register);

		// Apply enchantment overrides when the server starts
		// At this point the dynamic registry (including enchantments) is loaded
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			org.useReducer.tier.TierManager.reload();
			EnchantmentOverrideManager.applyOverrides(server);
			LOGGER.info("[ManageEnchantment] Enchantment overrides and tiers applied.");
		});

		// Clean up overrides when the server stops
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			EnchantmentOverrideManager.clearOverrides();
			LOGGER.info("[ManageEnchantment] Enchantment overrides cleared.");
		});

		LOGGER.info("[ManageEnchantment] Initialized successfully.");
	}
}
