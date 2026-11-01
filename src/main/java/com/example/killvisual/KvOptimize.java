package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.particle.ParticlesMode;

/** Switches for vanilla video settings that cost FPS. The old value is stored and restored on switch off. */
final class KvOptimize {
	enum Opt {
		PARTICLES("Меньше частиц"),
		CLOUDS("Без облаков"),
		SHADOWS("Без теней сущностей"),
		BLEND("Без сглаживания биомов"),
		ENTITIES("Дальность сущностей 50%");

		final String label;

		Opt(String label) { this.label = label; }
	}

	private KvOptimize() { }

	static boolean isOn(Opt o) { return KvConfig.INSTANCE.optOn.contains(o.name()); }

	static boolean anyOn() { return !KvConfig.INSTANCE.optOn.isEmpty(); }

	static void toggle(Opt o) {
		GameOptions g = MinecraftClient.getInstance().options;
		KvConfig cfg = KvConfig.INSTANCE;
		try {
			if (!isOn(o)) {
				cfg.optPrev.put(o.name(), current(g, o));
				cfg.optOn.add(o.name());
				set(g, o, optimized(o));
			} else {
				String prev = cfg.optPrev.get(o.name());
				cfg.optOn.remove(o.name());
				if (prev != null) set(g, o, prev);
			}
			g.write();
		} catch (RuntimeException ignored) { }
	}

	private static String optimized(Opt o) {
		return switch (o) {
			case PARTICLES -> ParticlesMode.MINIMAL.name();
			case CLOUDS -> CloudRenderMode.OFF.name();
			case SHADOWS -> "false";
			case BLEND -> "0";
			case ENTITIES -> "0.5";
		};
	}

	private static String current(GameOptions g, Opt o) {
		return switch (o) {
			case PARTICLES -> g.getParticles().getValue().name();
			case CLOUDS -> g.getCloudRenderMode().getValue().name();
			case SHADOWS -> String.valueOf(g.getEntityShadows().getValue());
			case BLEND -> String.valueOf(g.getBiomeBlendRadius().getValue());
			case ENTITIES -> String.valueOf(g.getEntityDistanceScaling().getValue());
		};
	}

	private static void set(GameOptions g, Opt o, String v) {
		switch (o) {
			case PARTICLES -> g.getParticles().setValue(ParticlesMode.valueOf(v));
			case CLOUDS -> g.getCloudRenderMode().setValue(CloudRenderMode.valueOf(v));
			case SHADOWS -> g.getEntityShadows().setValue(Boolean.parseBoolean(v));
			case BLEND -> g.getBiomeBlendRadius().setValue(Integer.parseInt(v));
			case ENTITIES -> g.getEntityDistanceScaling().setValue(Double.parseDouble(v));
		}
	}

	static void applyDistances(Integer view, Integer sim) {
		GameOptions g = MinecraftClient.getInstance().options;
		try {
			if (view != null) g.getViewDistance().setValue(view);
			if (sim != null) g.getSimulationDistance().setValue(sim);
			g.write();
		} catch (RuntimeException ignored) { }
	}
}
