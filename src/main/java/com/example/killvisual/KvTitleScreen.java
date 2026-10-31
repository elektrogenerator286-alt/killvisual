package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;

import java.util.function.Supplier;

/** Custom main menu: three square buttons, a nick bar and a flowing background. */
public final class KvTitleScreen extends Screen {
	private static final String[] LABELS = {"PLAY", "SETTINGS", "MULTIPLAYER"};
	private final float[] anim = new float[3];
	private final int[] tx = new int[3], ty = new int[3];
	private int size;
	private long lastNs = System.nanoTime();
	private Supplier<SkinTextures> skin;

	public KvTitleScreen() { super(Text.literal("KillVisuals")); }

	@Override
	protected void init() {
		size = Math.max(46, Math.min(80, (int) (Math.min(width, height) * 0.17)));
		int gap = 14;
		tx[0] = Math.max(24, (int) (width * 0.07));
		ty[0] = (int) (height * 0.36);
		tx[1] = tx[0];
		ty[1] = ty[0] + size + gap;
		tx[2] = (int) (width * 0.58);
		ty[2] = (int) (height * 0.58);
		MinecraftClient mc = MinecraftClient.getInstance();
		try {
			skin = mc.getSkinProvider().supplySkinTextures(mc.getGameProfile(), false);
		} catch (Exception e) {
			skin = null;
		}
	}

	// ---------- background ----------

	private static int hsv(float h, float s, float v) {
		h = h - (float) Math.floor(h);
		float r, g, b;
		int i = (int) (h * 6);
		float f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
		switch (i % 6) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return Ui.argb(255, (int) (r * 255), (int) (g * 255), (int) (b * 255));
	}

	private void paintBackground(DrawContext ctx) {
		double t = System.nanoTime() / 1e9;
		int step = 3;
		for (int x = 0; x < width; x += step) {
			double px = x * 0.004;
			float h1 = (float) (0.88 + 0.13 * Math.sin(px * 1.3 + t * 0.35));
			float h2 = (float) (0.80 + 0.15 * Math.sin(px * 0.9 - t * 0.28 + 2.0));
			float v1 = (float) (0.22 + 0.10 * Math.sin(px * 2.1 + t * 0.5));
			float v2 = (float) (0.14 + 0.08 * Math.sin(px * 1.7 - t * 0.4 + 1.0));
			ctx.fillGradient(x, 0, x + step, height, hsv(h1, 0.85f, v1), hsv(h2, 0.9f, v2));
		}
	}

	// ---------- icons ----------

	private static int u(float v, float k) { return Math.round(v * k); }

	private void gamepad(DrawContext c, int cx, int cy, float k, int bg) {
		int w = Ui.WHITE;
		Ui.round(c, cx - u(18, k), cy - u(9, k), u(36, k), u(20, k), u(9, k), w);
		Ui.round(c, cx - u(18, k), cy + u(3, k), u(12, k), u(14, k), u(6, k), w);
		Ui.round(c, cx + u(6, k), cy + u(3, k), u(12, k), u(14, k), u(6, k), w);
		c.fill(cx - u(12, k), cy - u(1, k), cx - u(4, k), cy + u(2, k), bg);
		c.fill(cx - u(9.5f, k), cy - u(4, k), cx - u(6.5f, k), cy + u(5, k), bg);
		Ui.circle(c, cx + u(6, k), cy - u(4, k), u(4, k), bg);
		Ui.circle(c, cx + u(11, k), cy, u(4, k), bg);
	}

	private void gear(DrawContext c, int cx, int cy, float k, int bg) {
		int w = Ui.WHITE;
		for (int i = 0; i < 8; i++) {
			double a = i * Math.PI / 4;
			int tx0 = cx + (int) Math.round(Math.cos(a) * 14 * k), ty0 = cy + (int) Math.round(Math.sin(a) * 14 * k);
			Ui.circle(c, tx0 - u(3.5f, k), ty0 - u(3.5f, k), u(7, k), w);
		}
		Ui.circle(c, cx - u(15, k), cy - u(15, k), u(30, k), w);
		Ui.circle(c, cx - u(7, k), cy - u(7, k), u(14, k), bg);
	}

	private void globe(DrawContext c, int cx, int cy, float k, int bg) {
		int w = Ui.WHITE;
		Ui.circle(c, cx - u(16, k), cy - u(16, k), u(32, k), w);
		Ui.circle(c, cx - u(14, k), cy - u(14, k), u(28, k), bg);
		Ui.outline(c, cx - u(7, k), cy - u(14, k), u(14, k), u(28, k), u(7, k), w, bg);
		c.fill(cx - u(14, k), cy, cx + u(14, k), cy + Math.max(1, u(1.5f, k)), w);
		c.fill(cx - u(11, k), cy - u(7, k), cx + u(11, k), cy - u(7, k) + Math.max(1, u(1.2f, k)), w);
		c.fill(cx - u(11, k), cy + u(7, k), cx + u(11, k), cy + u(7, k) + Math.max(1, u(1.2f, k)), w);
		c.fill(cx, cy - u(14, k), cx + Math.max(1, u(1.2f, k)), cy + u(14, k), w);
	}

	// ---------- screen ----------

	@Override
	public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
		paintBackground(ctx);
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1e9f);
		lastNs = now;

		// title
		String title = "KILLVISUALS";
		int tw = Ui.widthTitle(textRenderer, title);
		Ui.textTitle(ctx, textRenderer, title, width - tw - 30, 26, Ui.WHITE);

		// square buttons
		float k = size / 64f;
		for (int i = 0; i < 3; i++) {
			boolean hover = Ui.in(mx, my, tx[i], ty[i], size, size);
			anim[i] += ((hover ? 1f : 0f) - anim[i]) * Math.min(1f, dt * 12f);
			float a = anim[i];
			int bg = hover ? Ui.argb(235, 96, 10, 22) : Ui.argb(200, 12, 8, 12);
			Ui.round(ctx, tx[i] - 1, ty[i] - 1, size + 2, size + 2, 16,
					hover ? Ui.ACCENT : Ui.argb(200, 90, 40, 50));
			Ui.round(ctx, tx[i], ty[i], size, size, 15, bg);
			int cx = tx[i] + size / 2, cy = ty[i] + size / 2;
			int hole = hover ? Ui.argb(255, 96, 10, 22) : Ui.argb(255, 18, 12, 16);
			switch (i) {
				case 0 -> gamepad(ctx, cx, cy, k, hole);
				case 1 -> gear(ctx, cx, cy, k, hole);
				default -> globe(ctx, cx, cy, k, hole);
			}
			if (a > 0.03f) {
				int col = Ui.withAlpha(Ui.WHITE, a);
				int lw = Ui.widthBig(textRenderer, LABELS[i]);
				if (i < 2) {
					Ui.textBig(ctx, textRenderer, LABELS[i], tx[i] + size + 14 + (int) ((1 - a) * -8), ty[i] + size / 2 - 5, col);
				} else {
					Ui.textBig(ctx, textRenderer, LABELS[i], tx[i] + size / 2 - lw / 2, ty[i] + size + 8 + (int) ((1 - a) * -6), col);
				}
			}
		}

		// nick bar
		MinecraftClient mc = MinecraftClient.getInstance();
		int bh = 52, bw = Math.min((int) (width * 0.45), 360), bx = 24, by = height - bh - 24;
		Ui.round(ctx, bx - 1, by - 1, bw + 2, bh + 2, 20, Ui.argb(200, 90, 40, 50));
		Ui.roundGradient(ctx, bx, by, bw, bh, 19, Ui.argb(230, 12, 8, 12), Ui.argb(230, 70, 10, 22));
		int head = bh - 14;
		SkinTextures tex = null;
		try { if (skin != null) tex = skin.get(); } catch (Exception ignored) { }
		if (tex != null) PlayerSkinDrawer.draw(ctx, tex, bx + 10, by + 7, head);
		String nick = mc.getSession().getUsername();
		Ui.textBig(ctx, textRenderer, nick, bx + 10 + head + 14, by + bh / 2 - 5, Ui.WHITE);

		// small quit link
		String q = "Выход";
		int qw = Ui.width(textRenderer, q, false);
		boolean qh = Ui.in(mx, my, width - qw - 28, height - 30, qw + 8, 16);
		Ui.text(ctx, textRenderer, q, width - qw - 24, height - 26, qh ? Ui.WHITE : Ui.GRAY, false);

		super.render(ctx, mx, my, delta);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (click.button() == 0) {
			double mx = click.x(), my = click.y();
			MinecraftClient mc = MinecraftClient.getInstance();
			if (Ui.in(mx, my, tx[0], ty[0], size, size)) { mc.setScreen(new SelectWorldScreen(this)); return true; }
			if (Ui.in(mx, my, tx[1], ty[1], size, size)) { mc.setScreen(new OptionsScreen(this, mc.options)); return true; }
			if (Ui.in(mx, my, tx[2], ty[2], size, size)) { mc.setScreen(new MultiplayerScreen(this)); return true; }
			String q = "Выход";
			int qw = Ui.width(textRenderer, q, false);
			if (Ui.in(mx, my, width - qw - 28, height - 30, qw + 8, 16)) { mc.scheduleStop(); return true; }
		}
		return super.mouseClicked(click, doubled);
	}

	@Override public boolean shouldCloseOnEsc() { return false; }
}
