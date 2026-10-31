package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;

/** FPS and ping decoration drawn on the screen. */
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
		String pingLabel = "Ping: ";
		String gap = "     ";
		int textW = tr.getWidth(fpsLabel + fpsVal + gap + pingLabel + ping);
		int plateW = textW + 16, plateH = 15;
		String title = "KillVisual";
		int titleW = tr.getWidth(title) + 16, titleH = 14;

		int stackH = (cfg.showTitle ? titleH + 3 : 0) + plateH;
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int pw = Math.max(plateW, cfg.showTitle ? titleW : 0);
		int m = 6;
		int x, y;
		switch (cfg.position) {
			case 1 -> { x = m; y = m; }
			case 2 -> { x = sw - pw - m; y = m; }
			case 3 -> { x = m; y = sh - stackH - m - 22; }
			case 4 -> { x = sw - pw - m; y = sh - stackH - m - 22; }
			case 5 -> { x = (sw - pw) / 2; y = sh - stackH - m - 22; }
			default -> { x = (sw - pw) / 2; y = m; }
		}
		float a = cfg.opacity;
		int cy = y;
		if (cfg.showTitle) {
			int tx = x + (pw - titleW) / 2;
			Ui.outline(ctx, tx, cy, titleW, titleH, 4, Ui.withAlpha(Ui.ACCENT, a), Ui.withAlpha(Ui.argb(255, 12, 6, 8), a));
			ctx.drawText(tr, title, tx + 8, cy + 3, Ui.withAlpha(Ui.ACCENT, Math.max(a, 0.4f)), true);
			cy += titleH + 3;
		}
		int px = x + (pw - plateW) / 2;
		Ui.round(ctx, px, cy, plateW, plateH, 4, Ui.withAlpha(Ui.argb(255, 12, 6, 8), a * 0.7f));
		int tx = px + 8, ty = cy + 4;
		tx = draw(ctx, tr, fpsLabel, tx, ty, Ui.GRAY);
		tx = draw(ctx, tr, fpsVal, tx, ty, Ui.WHITE);
		tx = draw(ctx, tr, gap, tx, ty, Ui.GRAY);
		tx = draw(ctx, tr, pingLabel, tx, ty, Ui.GRAY);
		draw(ctx, tr, ping, tx, ty, Ui.WHITE);
	}

	private static int draw(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color) {
		ctx.drawText(tr, s, x, y, color, true);
		return x + tr.getWidth(s);
	}
}
