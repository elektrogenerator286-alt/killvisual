package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;

/** FPS and ping in an oval panel, with the title pill on its top edge. */
final class KvHud {
	private KvHud() { }

	static void render(DrawContext ctx, RenderTickCounter tick) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) return;
		KvConfig cfg = KvConfig.INSTANCE;
		TextRenderer tr = mc.textRenderer;

		String ping = "-";
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (e != null) ping = e.getLatency() + " мс";
		}
		String fpsLabel = "FPS: ", fpsVal = String.valueOf(mc.getCurrentFps());
		String pingLabel = "Ping: ", gap = "    ";
		int textW = Ui.width(tr, fpsLabel + fpsVal + gap + pingLabel + ping, false);
		int panelW = textW + 28, panelH = 22;
		String title = "KillVisual";
		int titleW = Ui.width(tr, title, true) + 16, titleH = 13;

		int topPad = cfg.showTitle ? titleH / 2 : 0;
		int totalW = Math.max(panelW, cfg.showTitle ? titleW : 0);
		int totalH = topPad + panelH;
		float s = cfg.hudScale;
		int scaledW = Math.round(totalW * s), scaledH = Math.round(totalH * s);
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int m = 6, bottomY = sh - scaledH - m - 22;
		int x, y;
		switch (cfg.position) {
			case 1 -> { x = m; y = m; }
			case 2 -> { x = sw - scaledW - m; y = m; }
			case 3 -> { x = m; y = bottomY; }
			case 4 -> { x = sw - scaledW - m; y = bottomY; }
			case 5 -> { x = (sw - scaledW) / 2; y = bottomY; }
			default -> { x = (sw - scaledW) / 2; y = m; }
		}
		float a = cfg.opacity;

		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().translate((float) x, (float) y);
		ctx.getMatrices().scale(s, s);

		int px = (totalW - panelW) / 2, py = topPad;
		Ui.roundGradient(ctx, px, py, panelW, panelH, panelH / 2,
				Ui.withAlpha(Ui.argb(255, 12, 12, 14), a), Ui.withAlpha(Ui.argb(255, 96, 10, 22), a));
		Ui.ring(ctx, px, py, panelW, panelH, panelH / 2, 1, Ui.withAlpha(Ui.argb(255, 120, 40, 56), a));
		int tx = px + 14, ty = py + (cfg.showTitle ? 9 : 7);
		tx = draw(ctx, tr, fpsLabel, tx, ty, Ui.withAlpha(Ui.GRAY, a));
		tx = draw(ctx, tr, fpsVal, tx, ty, Ui.withAlpha(Ui.WHITE, a));
		tx = draw(ctx, tr, gap, tx, ty, Ui.withAlpha(Ui.GRAY, a));
		tx = draw(ctx, tr, pingLabel, tx, ty, Ui.withAlpha(Ui.GRAY, a));
		draw(ctx, tr, ping, tx, ty, Ui.withAlpha(Ui.WHITE, a));
		if (cfg.showTitle) {
			int tpx = (totalW - titleW) / 2;
			Ui.roundGradient(ctx, tpx, 0, titleW, titleH, 6,
					Ui.withAlpha(Ui.argb(255, 12, 6, 8), a), Ui.withAlpha(Ui.argb(255, 12, 6, 8), a));
			Ui.ring(ctx, tpx, 0, titleW, titleH, 6, 1, Ui.withAlpha(Ui.ACCENT, a));
			Ui.text(ctx, tr, title, tpx + 8, 2, Ui.withAlpha(Ui.ACCENT, a), true);
		}

		ctx.getMatrices().popMatrix();
	}

	private static int draw(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color) {
		Ui.text(ctx, tr, s, x, y, color, false);
		return x + Ui.width(tr, s, false);
	}
}
