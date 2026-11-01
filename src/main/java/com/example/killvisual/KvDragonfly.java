package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/** Dragonfly: changes only the flight speed. It never turns flight on by itself. */
final class KvDragonfly {
	private static final float VANILLA_FLY_SPEED = 0.05f;
	private static boolean active;

	private KvDragonfly() { }

	static void tick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		if (p == null) {
			active = false;
			return;
		}
		KvConfig cfg = KvConfig.INSTANCE;
		if (cfg.dragonfly) {
			active = true;
			p.getAbilities().setFlySpeed(VANILLA_FLY_SPEED * cfg.dragonSpeed);
		} else if (active) {
			active = false;
			p.getAbilities().setFlySpeed(VANILLA_FLY_SPEED);
		}
	}
}
