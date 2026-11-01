package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;

/** Dragonfly: creative-style flight with an adjustable speed. */
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
		PlayerAbilities ab = p.getAbilities();
		if (cfg.dragonfly) {
			active = true;
			ab.allowFlying = true;
			ab.flying = true;
			ab.setFlySpeed(VANILLA_FLY_SPEED * cfg.dragonSpeed);
		} else if (active) {
			active = false;
			boolean keep = p.isCreative() || p.isSpectator();
			ab.allowFlying = keep;
			if (!keep) ab.flying = false;
			ab.setFlySpeed(VANILLA_FLY_SPEED);
		}
	}
}
