package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/** Screen-space atmosphere: haze, sunset, night and neon looks. */
final class KvAtmo {
	private KvAtmo() { }

	static void render(DrawContext ctx, RenderTickCounter tick) {
		KvConfig cfg = KvConfig.INSTANCE;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (cfg.atmo == 0 || mc.player == null) return;
		int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
		float p = cfg.atmoPower;
		double t = System.nanoTime() / 1e9;
		switch (cfg.atmo) {
			case 1 -> {
				ctx.fill(0, 0, w, h, Ui.argb((int) (70 * p), 205, 215, 225));
				ctx.fillGradient(0, h / 3, w, h, Ui.argb(0, 215, 222, 230), Ui.argb((int) (120 * p), 215, 222, 230));
			}
			case 2 -> {
				ctx.fill(0, 0, w, h, Ui.argb((int) (55 * p), 255, 120, 40));
				ctx.fillGradient(0, 0, w, h, Ui.argb((int) (90 * p), 120, 30, 90), Ui.argb((int) (90 * p), 255, 150, 50));
			}
			case 3 -> {
				ctx.fill(0, 0, w, h, Ui.argb((int) (110 * p), 6, 14, 50));
				ctx.fillGradient(0, 0, w, h, Ui.argb((int) (60 * p), 0, 0, 20), Ui.argb(0, 0, 0, 0));
			}
			default -> {
				int g = (int) (110 + 110 * Math.sin(t * 0.8)), b = (int) (180 + 70 * Math.sin(t * 0.5 + 1.5));
				ctx.fillGradient(0, 0, w, h, Ui.argb((int) (80 * p), 255, Math.max(0, g / 3), b),
						Ui.argb((int) (80 * p), 40, g, 255));
			}
		}
	}
}
