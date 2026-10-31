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

	private static int inset(int d, int r) {
		if (d >= r) return 0;
		double dy = r - d - 0.5;
		return (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
	}

	static void round(DrawContext c, int x, int y, int w, int h, int r, int color) {
		r = Math.min(r, Math.min(w, h) / 2);
		for (int row = 0; row < h; row++) {
			int in = inset(Math.min(row, h - 1 - row), r);
			c.fill(x + in, y + row, x + w - in, y + row + 1, color);
		}
	}

	static void outline(DrawContext c, int x, int y, int w, int h, int r, int border, int fill) {
		round(c, x, y, w, h, r, border);
		round(c, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
	}

	/** Horizontal gradient inside a rounded rectangle. */
	static void roundGradient(DrawContext c, int x, int y, int w, int h, int r, int left, int right) {
		r = Math.min(r, Math.min(w, h) / 2);
		for (int col = 0; col < w; col++) {
			int in = inset(Math.min(col, w - 1 - col), r);
			float t = w <= 1 ? 0 : col / (float) (w - 1);
			int a = lerp(left >>> 24, right >>> 24, t);
			int rr = lerp((left >> 16) & 255, (right >> 16) & 255, t);
			int g = lerp((left >> 8) & 255, (right >> 8) & 255, t);
			int b = lerp(left & 255, right & 255, t);
			c.fill(x + col, y + in, x + col + 1, y + h - in, argb(a, rr, g, b));
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
