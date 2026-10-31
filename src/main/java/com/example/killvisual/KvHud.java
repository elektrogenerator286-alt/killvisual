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
		String pingLabel = "Ping: ", gap = "      ";
		int textW = Ui.width(tr, fpsLabel + fpsVal + gap + pingLabel + ping, false);
		int panelW = textW + 34, panelH = 24;
		String title = "KillVisual";
		int titleW = Ui.width(tr, title, true) + 18, titleH = 14;

		int topPad = cfg.showTitle ? titleH / 2 : 0;
		int totalW = Math.max(panelW, cfg.showTitle ? titleW : 0);
		int totalH = topPad + panelH;
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int m = 6, bottomY = sh - totalH - m - 22;
		int x, y;
		switch (cfg.position) {
			case 1 -> { x = m; y = m; }
			case 2 -> { x = sw - totalW - m; y = m; }
			case 3 -> { x = m; y = bottomY; }
			case 4 -> { x = sw - totalW - m; y = bottomY; }
			case 5 -> { x = (sw - totalW) / 2; y = bottomY; }
			default -> { x = (sw - totalW) / 2; y = m; }
		}
		float a = cfg.opacity;
		int px = x + (totalW - panelW) / 2, py = y + topPad;
		// oval panel
		Ui.round(ctx, px - 1, py - 1, panelW + 2, panelH + 2, panelH / 2 + 1, Ui.withAlpha(Ui.argb(255, 60, 10, 18), a));
		Ui.roundGradient(ctx, px, py, panelW, panelH, panelH / 2,
				Ui.withAlpha(Ui.argb(255, 12, 12, 14), a), Ui.withAlpha(Ui.argb(255, 96, 10, 22), a));
		int tx = px + 17, ty = py + (cfg.showTitle ? 10 : 8);
		tx = draw(ctx, tr, fpsLabel, tx, ty, Ui.GRAY);
		tx = draw(ctx, tr, fpsVal, tx, ty, Ui.WHITE);
		tx = draw(ctx, tr, gap, tx, ty, Ui.GRAY);
		tx = draw(ctx, tr, pingLabel, tx, ty, Ui.GRAY);
		draw(ctx, tr, ping, tx, ty, Ui.WHITE);
		// title pill on the top edge
		if (cfg.showTitle) {
			int tpx = x + (totalW - titleW) / 2;
			Ui.outline(ctx, tpx, y, titleW, titleH, 6, Ui.ACCENT, Ui.argb(255, 12, 6, 8));
			Ui.text(ctx, tr, title, tpx + 9, y + 3, Ui.ACCENT, true);
		}
	}

	private static int draw(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color) {
		Ui.text(ctx, tr, s, x, y, color, false);
		return x + Ui.width(tr, s, false);
	}
}
