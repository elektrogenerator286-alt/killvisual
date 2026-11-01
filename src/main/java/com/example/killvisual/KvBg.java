package com.example.killvisual;

import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/** Shared flowing background with falling stars, used by the title and list screens. */
public final class KvBg {
	private KvBg() { }

	private static final class Star {
		float x, y, vx, vy, size, phase;
		boolean streak;
		int color;
	}

	private static final Random RND = new Random();
	private static Star[] stars = new Star[0];
	private static int lastW, lastH;
	private static long lastNs = System.nanoTime();

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

	private static Star newStar(int w, int h, boolean anywhere) {
		Star st = new Star();
		st.streak = RND.nextFloat() < 0.14f;
		st.x = RND.nextFloat() * (w + 60) - 30;
		st.y = anywhere ? RND.nextFloat() * h : -10 - RND.nextFloat() * 40;
		if (st.streak) {
			st.vy = 90 + RND.nextFloat() * 110;
			st.vx = 30 + RND.nextFloat() * 50;
			st.size = 1.5f;
		} else {
			st.vy = 8 + RND.nextFloat() * 28;
			st.vx = 2 + RND.nextFloat() * 8;
			st.size = RND.nextFloat() < 0.25f ? 2f : 1f;
		}
		st.phase = 1.5f + RND.nextFloat() * 3f;
		int pick = RND.nextInt(3);
		st.color = pick == 0 ? 0xFFFFFF : pick == 1 ? 0xFFC8DC : 0xD8C0FF;
		return st;
	}

	public static void paint(DrawContext ctx, int width, int height) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1e9f);
		lastNs = now;
		double t = now / 1e9;
		if (width != lastW || height != lastH || stars.length == 0) {
			lastW = width; lastH = height;
			int n = Math.max(30, Math.min(90, width * height / 7000));
			stars = new Star[n];
			for (int i = 0; i < n; i++) stars[i] = newStar(width, height, true);
		}
		int step = 3;
		for (int x = 0; x < width; x += step) {
			double px = x * 0.004;
			float h1 = (float) (0.88 + 0.13 * Math.sin(px * 1.3 + t * 0.35));
			float h2 = (float) (0.80 + 0.15 * Math.sin(px * 0.9 - t * 0.28 + 2.0));
			float v1 = (float) (0.22 + 0.10 * Math.sin(px * 2.1 + t * 0.5));
			float v2 = (float) (0.14 + 0.08 * Math.sin(px * 1.7 - t * 0.4 + 1.0));
			ctx.fillGradient(x, 0, x + step, height, hsv(h1, 0.85f, v1), hsv(h2, 0.9f, v2));
		}
		ctx.fillGradient(0, 0, width, height / 3, Ui.argb(110, 0, 0, 0), Ui.argb(0, 0, 0, 0));
		ctx.fillGradient(0, height * 2 / 3, width, height, Ui.argb(0, 0, 0, 0), Ui.argb(150, 0, 0, 0));
		for (int i = 0; i < stars.length; i++) {
			Star s = stars[i];
			s.x += s.vx * dt;
			s.y += s.vy * dt;
			if (s.y > height + 8 || s.x > width + 20) { stars[i] = newStar(width, height, false); continue; }
			float tw = 0.55f + 0.45f * (float) Math.sin(t * s.phase + i);
			int xi = Math.round(s.x), yi = Math.round(s.y);
			if (s.streak) {
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
				ctx.fill(xi, yi, xi + sz, yi + sz, Ui.withAlpha(0xFF000000 | s.color, tw));
				if (sz >= 2) {
					int arm = (Math.round(tw * 255 * 0.5f) << 24) | s.color;
					ctx.fill(xi - 2, yi, xi, yi + sz, arm);
					ctx.fill(xi + sz, yi, xi + sz + 2, yi + sz, arm);
					ctx.fill(xi, yi - 2, xi + sz, yi, arm);
					ctx.fill(xi, yi + sz, xi + sz, yi + sz + 2, arm);
				}
			}
		}
	}
}
