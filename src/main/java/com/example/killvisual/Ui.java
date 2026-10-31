package com.example.killvisual;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Small drawing helpers: pixel-rounded rectangles and colours. */
final class Ui {
	private Ui() { }

	private static final Style REG = Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("killvisual", "ui")));
	private static final Style BOLD = Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of("killvisual", "ui_bold")));

	static Text t(String s, boolean bold) { return Text.literal(s).setStyle(bold ? BOLD : REG); }
	static int width(TextRenderer tr, String s, boolean bold) { return tr.getWidth(t(s, bold)); }
	static void text(DrawContext c, TextRenderer tr, String s, int x, int y, int color, boolean bold) {
		c.drawText(tr, t(s, bold), x, y, color, true);
	}

	static final int ACCENT = 0xFFE42838;
	static final int WHITE = 0xFFFFFFFF;
	static final int GRAY = 0xFFBFAAAF;
	static final int DIM = 0xFF4A2A30;

	static int argb(int a, int r, int g, int b) {
		return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}

	static int withAlpha(int color, float alpha) {
		int a = (int) (((color >>> 24) & 255) * Math.max(0f, Math.min(1f, alpha)));
		return (a << 24) | (color & 0xFFFFFF);
	}

	/** Distance (in pixels, fractional) the rounded edge is pulled in at this column. */
	private static float edge(int d, int r) {
		if (d >= r) return 0f;
		double dx = r - d - 0.5;
		return (float) (r - Math.sqrt(Math.max(0, r * r - dx * dx)));
	}

	static void round(DrawContext c, int x, int y, int w, int h, int r, int color) {
		roundGradient(c, x, y, w, h, r, color, color);
	}

	static void outline(DrawContext c, int x, int y, int w, int h, int r, int border, int fill) {
		round(c, x, y, w, h, r, border);
		round(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
	}

	/** Horizontal gradient inside a rounded rectangle, with anti-aliased (smooth) corners. */
	static void roundGradient(DrawContext c, int x, int y, int w, int h, int r, int left, int right) {
		r = Math.min(r, Math.min(w, h) / 2);
		for (int col = 0; col < w; col++) {
			float e = edge(Math.min(col, w - 1 - col), r);
			float t = w <= 1 ? 0 : col / (float) (w - 1);
			int a = lerp(left >>> 24, right >>> 24, t);
			int rr = lerp((left >> 16) & 255, (right >> 16) & 255, t);
			int g = lerp((left >> 8) & 255, (right >> 8) & 255, t);
			int b = lerp(left & 255, right & 255, t);
			int full = argb(a, rr, g, b);
			int fl = (int) Math.floor(e);
			float frac = e - fl;
			if (frac < 0.02f) {
				c.fill(x + col, y + fl, x + col + 1, y + h - fl, full);
			} else {
				float cov = 1f - frac;
				int part = argb(Math.round(a * cov), rr, g, b);
				c.fill(x + col, y + fl, x + col + 1, y + fl + 1, part);
				c.fill(x + col, y + h - fl - 1, x + col + 1, y + h - fl, part);
				if (h - 2 * (fl + 1) > 0) c.fill(x + col, y + fl + 1, x + col + 1, y + h - fl - 1, full);
			}
		}
	}

	private static int lerp(int a, int b, float t) { return Math.round(a + (b - a) * t); }

	static void circle(DrawContext c, int x, int y, int size, int color) {
		round(c, x, y, size, size, size / 2, color);
	}

	static boolean in(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
