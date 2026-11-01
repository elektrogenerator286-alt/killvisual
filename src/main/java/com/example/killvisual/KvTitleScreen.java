package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
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
	private final java.util.Random rnd = new java.util.Random();
	private Star[] stars = new Star[0];
	private boolean editing;
	private TextFieldWidget nickField;
	private String nickError = "";

	private static final class Star {
		float x, y, vx, vy, size, phase;
		boolean streak;
		int color;
	}

	public KvTitleScreen() { super(Text.literal("KillVisuals")); }

	@Override
	protected void init() {
		int bh = 30, barTop = height - bh - 24;
		int titleBottom = 112, gap = 12;
		int avail = barTop - 14 - titleBottom;
		size = Math.max(32, Math.min(Math.min((avail - 2 * gap) / 3, (int) (Math.min(width, height) * 0.16)), 70));
		int totalH = 3 * size + 2 * gap;
		int top = titleBottom + Math.max(0, (avail - totalH) / 2);
		tx[0] = Math.max(24, (int) (width * 0.07));
		ty[0] = top;
		tx[1] = tx[0];
		ty[1] = top + size + gap;
		tx[2] = tx[0];
		ty[2] = ty[1] + size + gap;
		MinecraftClient mc = MinecraftClient.getInstance();
		try {
			skin = mc.getSkinProvider().supplySkinTextures(mc.getGameProfile(), false);
		} catch (Exception e) {
			skin = null;
		}
		int n = Math.max(30, Math.min(90, width * height / 7000));
		stars = new Star[n];
		for (int i = 0; i < n; i++) {
			stars[i] = newStar(true);
		}
	}

	private Star newStar(boolean anywhere) {
		Star st = new Star();
		st.streak = rnd.nextFloat() < 0.14f;
		st.x = rnd.nextFloat() * (width + 60) - 30;
		st.y = anywhere ? rnd.nextFloat() * height : -10 - rnd.nextFloat() * 40;
		if (st.streak) {
			st.vy = 90 + rnd.nextFloat() * 110;
			st.vx = 30 + rnd.nextFloat() * 50;
			st.size = 1.5f;
		} else {
			st.vy = 8 + rnd.nextFloat() * 28;
			st.vx = 2 + rnd.nextFloat() * 8;
			st.size = rnd.nextFloat() < 0.25f ? 2f : 1f;
		}
		st.phase = 1.5f + rnd.nextFloat() * 3f;
		int pick = rnd.nextInt(3);
		st.color = pick == 0 ? 0xFFFFFF : pick == 1 ? 0xFFC8DC : 0xD8C0FF;
		return st;
	}

	// ---------- nick editing ----------

	private int barX() { return 24; }
	private int barH() { return 30; }
	private int barW() { return Math.min((int) (width * 0.22), 200); }
	private int barY() { return height - barH() - 24; }

	private void startEditing() {
		editing = true;
		nickError = "";
		int head = barH() - 8;
		int nx = barX() + 8 + head + 10;
		nickField = new TextFieldWidget(textRenderer, nx, barY() + 5, barW() - (nx - barX()) - 10, 20, Text.empty());
		nickField.setMaxLength(16);
		nickField.setText(MinecraftClient.getInstance().getSession().getUsername());
		addDrawableChild(nickField);
		setFocused(nickField);
		nickField.setFocused(true);
	}

	private void stopEditing() {
		editing = false;
		nickError = "";
		clearChildren();
		nickField = null;
	}

	private void applyNick() {
		String n = nickField == null ? "" : nickField.getText().trim();
		if (!NickUtil.valid(n)) {
			nickError = "3-16 символов: латиница, цифры, _";
			return;
		}
		NickUtil.apply(n);
		KvConfig.INSTANCE.nick = n;
		KvConfig.save();
		MinecraftClient mc = MinecraftClient.getInstance();
		try {
			skin = mc.getSkinProvider().supplySkinTextures(mc.getGameProfile(), false);
		} catch (Exception ignored) { }
		stopEditing();
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (editing) {
			int key = input.key();
			if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { applyNick(); return true; }
			if (key == GLFW.GLFW_KEY_ESCAPE) { stopEditing(); return true; }
		}
		return super.keyPressed(input);
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
		int px = Math.max(1, Math.round(1.6f * k)); // line thickness
		int r = u(16, k);
		// outer ring
		Ui.circle(c, cx - r, cy - r, 2 * r, w);
		Ui.circle(c, cx - r + px + 1, cy - r + px + 1, 2 * r - 2 * (px + 1), bg);
		// meridian
		int mw = u(8, k);
		Ui.outline(c, cx - mw, cy - r + px + 1, 2 * mw, 2 * r - 2 * (px + 1), mw, w, bg);
		Ui.round(c, cx - mw + px, cy - r + 2 * (px + 1), 2 * mw - 2 * px, 2 * r - 4 * (px + 1), Math.max(0, mw - px), bg);
		// centre line and equator
		c.fill(cx - px / 2, cy - r + px, cx - px / 2 + px, cy + r - px, w);
		c.fill(cx - r + px, cy - px / 2, cx + r - px, cy - px / 2 + px, w);
	}

	// ---------- screen ----------

	@Override
	public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
		paintBackground(ctx);
		// soft vignette
		ctx.fillGradient(0, 0, width, height / 3, Ui.argb(110, 0, 0, 0), Ui.argb(0, 0, 0, 0));
		ctx.fillGradient(0, height * 2 / 3, width, height, Ui.argb(0, 0, 0, 0), Ui.argb(150, 0, 0, 0));
	}

	private void paintStars(DrawContext ctx, float dt, double t) {
		for (int i = 0; i < stars.length; i++) {
			Star s = stars[i];
			s.x += s.vx * dt;
			s.y += s.vy * dt;
			if (s.y > height + 8 || s.x > width + 20) { stars[i] = newStar(false); continue; }
			float tw = 0.55f + 0.45f * (float) Math.sin(t * s.phase + i);
			int xi = Math.round(s.x), yi = Math.round(s.y);
			if (s.streak) {
				float len = 10;
				float nx = -s.vx, ny = -s.vy, l = (float) Math.sqrt(nx * nx + ny * ny);
				nx /= l; ny /= l;
				for (int k = 0; k < 10; k++) {
					float f = 1f - k / 10f;
					int px = Math.round(s.x + nx * k * 2.2f), py = Math.round(s.y + ny * k * 2.2f);
					ctx.fill(px, py, px + 2, py + 2, Ui.withAlpha(0xFF000000 | s.color, f * 0.8f));
				}
				ctx.fill(xi - 1, yi - 1, xi + 2, yi + 2, Ui.withAlpha(0xFFFFFFFF, 0.95f));
			} else {
				int sz = (int) s.size;
				int col = 0xFF000000 | s.color;
				ctx.fill(xi, yi, xi + sz, yi + sz, Ui.withAlpha(col, tw));
				if (sz >= 2) {
					int a2 = Math.round(tw * 255 * 0.5f);
					int arm = (a2 << 24) | s.color;
					ctx.fill(xi - 2, yi, xi, yi + sz, arm);
					ctx.fill(xi + sz, yi, xi + sz + 2, yi + sz, arm);
					ctx.fill(xi, yi - 2, xi + sz, yi, arm);
					ctx.fill(xi, yi + sz, xi + sz, yi + sz + 2, arm);
				}
			}
		}
	}

	private void paintTitle(DrawContext ctx, double t) {
		String title = "KILLVISUALS";
		int n = title.length(), spacing = 3;
		int[] cw = new int[n];
		int total = 0;
		for (int i = 0; i < n; i++) {
			cw[i] = Ui.widthTitle(textRenderer, String.valueOf(title.charAt(i)));
			total += cw[i] + (i < n - 1 ? spacing : 0);
		}
		int x0 = width - total - 30, y0 = 56;
		// glow behind
		int x = x0;
		for (int i = 0; i < n; i++) {
			String ch = String.valueOf(title.charAt(i));
			int glow = Ui.withAlpha(Ui.ACCENT, 0.10f);
			Ui.textTitle(ctx, textRenderer, ch, x - 2, y0, glow);
			Ui.textTitle(ctx, textRenderer, ch, x + 2, y0, glow);
			Ui.textTitle(ctx, textRenderer, ch, x, y0 - 2, glow);
			Ui.textTitle(ctx, textRenderer, ch, x, y0 + 2, glow);
			x += cw[i] + spacing;
		}
		// letters with a moving shimmer: white -> pink -> red
		x = x0;
		for (int i = 0; i < n; i++) {
			float p = i / (float) (n - 1);
			float shimmer = 0.5f + 0.5f * (float) Math.sin(t * 1.6 - i * 0.55);
			int r = 255;
			int g = Math.round(255 - (255 - 120) * (p * 0.7f + shimmer * 0.3f));
			int b = Math.round(255 - (255 - 150) * (p * 0.7f + shimmer * 0.3f));
			Ui.textTitle(ctx, textRenderer, String.valueOf(title.charAt(i)), x, y0, Ui.argb(255, r, g, b));
			x += cw[i] + spacing;
		}
		// underline
		Ui.roundGradient(ctx, x0, y0 + 38, total, 3, 1, Ui.argb(0, 228, 40, 56), Ui.ACCENT);
		Ui.roundGradient(ctx, x0, y0 + 38, total / 2, 3, 1, Ui.argb(0, 255, 255, 255), Ui.argb(160, 255, 200, 210));
	}

	private void glow(DrawContext c, int x, int y, int w, int h, int r, int color, float strength) {
		for (int i = 5; i >= 1; i--) {
			Ui.round(c, x - i * 2, y - i * 2, w + i * 4, h + i * 4, r + i * 2, Ui.withAlpha(color, strength * 0.05f));
		}
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1e9f);
		lastNs = now;
		double t = now / 1e9;

		paintStars(ctx, dt, t);
		paintTitle(ctx, t);

		// square buttons
		float k = size / 64f;
		for (int i = 0; i < 3; i++) {
			boolean hover = Ui.in(mx, my, tx[i], ty[i], size, size);
			anim[i] += ((hover ? 1f : 0f) - anim[i]) * Math.min(1f, dt * 12f);
			float a = anim[i];
			int x = tx[i], y = ty[i] - Math.round(3 * a);
			if (a > 0.02f) glow(ctx, x, y, size, size, 16, Ui.ACCENT, a);
			Ui.round(ctx, x - 1, y - 1, size + 2, size + 2, 17,
					hover ? Ui.ACCENT : Ui.argb(210, 110, 50, 62));
			Ui.roundGradient(ctx, x, y, size, size, 16,
					hover ? Ui.argb(245, 120, 14, 30) : Ui.argb(225, 16, 10, 16),
					hover ? Ui.argb(245, 70, 10, 40) : Ui.argb(225, 40, 12, 24));
			Ui.round(ctx, x + 10, y + 2, size - 20, 1, 0, Ui.withAlpha(Ui.WHITE, hover ? 0.35f : 0.16f));
			int cx = x + size / 2, cy = y + size / 2;
			int hole = hover ? Ui.argb(255, 100, 12, 34) : Ui.argb(255, 26, 11, 20);
			switch (i) {
				case 0 -> gamepad(ctx, cx, cy, k, hole);
				case 1 -> gear(ctx, cx, cy, k, hole);
				default -> globe(ctx, cx, cy, k, hole);
			}
			if (a > 0.03f) {
				int lw = Ui.widthBig(textRenderer, LABELS[i]);
				int pw2 = lw + 24, ph2 = 24;
				int lx, ly;
				if (true) {
					lx = x + size + 12 + (int) ((1 - a) * -10);
					ly = y + size / 2 - ph2 / 2;
				} else {
					lx = x + size / 2 - pw2 / 2;
					ly = y + size + 10 + (int) ((1 - a) * -8);
				}
				Ui.roundGradient(ctx, lx, ly, pw2, ph2, 12,
						Ui.withAlpha(Ui.argb(255, 120, 14, 30), a * 0.9f), Ui.withAlpha(Ui.argb(255, 70, 10, 40), a * 0.9f));
				Ui.textBig(ctx, textRenderer, LABELS[i], lx + 12, ly + 7, Ui.withAlpha(Ui.WHITE, a));
			}
		}

		// nick bar
		MinecraftClient mc = MinecraftClient.getInstance();
		int bh = barH(), bw = barW(), bx = barX(), by = barY();
		boolean barHover = Ui.in(mx, my, bx, by, bw, bh);
		glow(ctx, bx, by, bw, bh, 16, Ui.ACCENT, barHover ? 0.9f : 0.5f);
		Ui.round(ctx, bx - 1, by - 1, bw + 2, bh + 2, 17, barHover ? Ui.ACCENT : Ui.argb(210, 110, 50, 62));
		Ui.roundGradient(ctx, bx, by, bw, bh, 16, Ui.argb(240, 14, 9, 14), Ui.argb(240, 84, 12, 30));
		int head = bh - 8;
		Ui.round(ctx, bx + 7, by + 3, head + 2, head + 2, 4, Ui.withAlpha(Ui.ACCENT, 0.9f));
		SkinTextures tex = null;
		try { if (skin != null) tex = skin.get(); } catch (Exception ignored) { }
		if (tex != null) PlayerSkinDrawer.draw(ctx, tex, bx + 8, by + 4, head);
		int nx = bx + 8 + head + 10;
		if (editing) {
			String hint = nickError.isEmpty() ? "Enter - сохранить, Esc - отмена" : nickError;
			Ui.text(ctx, textRenderer, hint, bx + 8, by - 13, nickError.isEmpty() ? Ui.GRAY : Ui.ACCENT, false);
		} else {
			String nick = mc.getSession().getUsername();
			Ui.text(ctx, textRenderer, nick, nx, by + 10, Ui.WHITE, true);
			String edit = "изменить";
			int ew = Ui.width(textRenderer, edit, false);
			Ui.text(ctx, textRenderer, edit, bx + bw - ew - 12, by + bh / 2 - 4, barHover ? Ui.WHITE : Ui.GRAY, false);
		}

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
			boolean inBar = Ui.in(mx, my, barX(), barY(), barW(), barH());
			if (editing) {
				if (inBar) return super.mouseClicked(click, doubled);
				stopEditing();
			} else if (inBar) {
				startEditing();
				return true;
			}
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
