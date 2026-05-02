package org.rmsederhana.client;

import net.fabricmc.api.ClientModInitializer;

public class ManageEnchnantmentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			org.rmsederhana.tier.TierManager.reload();
		});
	}
}