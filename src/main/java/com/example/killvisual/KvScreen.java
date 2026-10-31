package com.example.killvisual;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class KvScreen extends net.minecraft.client.gui.screen.Screen {
	private int px, py, pw, ph;           // panel
	private int sx, sy, sw, sh;           // sidebar
	private int cx, cy, cw, ch;           // content
	private int sel = 0;                  // 0 = Оформление, 1.. = custom items
	private int scroll = 0;
	private boolean dragging;
	private TextFieldWidget nameField, noteField;

	private static final int ROW = 24;

	public KvScreen() { super(Text.literal("KillVisual")); }

	private KvConfig cfg() { return KvConfig.INSTANCE; }
	private List<KvConfig.Item> items() { return cfg().items; }

	@Override
	protected void init() {
		pw = Math.min(width - 20, 480);
		ph = Math.min(height - 20, 300);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
		sx = px + 14; sy = py + 44; sw = (int) (pw * 0.34); sh = ph - 58;
		cx = sx + sw + 10; cy = sy; cw = px + pw - 14 - cx; ch = sh;
		rebuild();
	}

	private void rebuild() {
		clearChildren();
		nameField = null;
		noteField = null;
		if (sel > 0 && sel <= items().size()) {
			KvConfig.Item it = items().get(sel - 1);
			nameField = new TextFieldWidget(textRenderer, cx + 16, cy + 62, cw - 32, 20, Text.empty());
			nameField.setMaxLength(24);
			nameField.setText(it.name);
			nameField.setChangedListener(s -> it.name = s);
			addDrawableChild(nameField);
			noteField = new TextFieldWidget(textRenderer, cx + 16, cy + 106, cw - 32, 20, Text.empty());
			noteField.setMaxLength(200);
			noteField.setText(it.note);
			noteField.setChangedListener(s -> it.note = s);
			addDrawableChild(noteField);
		} else {
			sel = 0;
		}
	}

	// ---------------- painting ----------------

	@Override
	public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
		super.renderBackground(ctx, mx, my, delta);
		// panel
		Ui.round(ctx, px - 1, py - 1, pw + 2, ph + 2, 14, Ui.argb(255, 40, 8, 14));
		Ui.roundGradient(ctx, px, py, pw, ph, 14, Ui.argb(255, 10, 10, 12), Ui.argb(255, 96, 10, 22));
		ctx.drawText(textRenderer, "KillVisual", px + 18, py + 16, Ui.ACCENT, true);
		// close X
		int xx = px + pw - 30, xy = py + 14;
		for (int i = 0; i < 10; i++) {
			ctx.fill(xx + i, xy + i, xx + i + 2, xy + i + 1, Ui.GRAY);
			ctx.fill(xx + 9 - i, xy + i, xx + 11 - i, xy + i + 1, Ui.GRAY);
		}
		// sidebar
		Ui.round(ctx, sx, sy, sw, sh, 12, Ui.argb(255, 4, 4, 5));
		ctx.enableScissor(sx, sy, sx + sw, sy + sh);
		int total = items().size() + 2;
		for (int i = 0; i < total; i++) {
			int ry = sy + 6 + i * ROW - scroll;
			if (ry + ROW < sy || ry > sy + sh) continue;
			boolean hover = Ui.in(mx, my, sx + 6, ry, sw - 12, ROW - 2);
			if (i == total - 1) {
				ctx.drawText(textRenderer, "+ Добавить пункт", sx + 20, ry + 8, hover ? Ui.WHITE : Ui.GRAY, true);
				continue;
			}
			boolean selected = i == sel;
			if (selected) {
				Ui.round(ctx, sx + 6, ry, sw - 12, ROW - 2, 10, Ui.argb(255, 86, 20, 28));
				ctx.fill(sx + 12, ry + 5, sx + 15, ry + ROW - 7, Ui.ACCENT);
			} else if (hover) {
				Ui.round(ctx, sx + 6, ry, sw - 12, ROW - 2, 10, Ui.argb(255, 24, 10, 12));
			}
			String name = i == 0 ? "Оформление" : items().get(i - 1).name;
			while (textRenderer.getWidth(name) > sw - 54 && name.length() > 1) name = name.substring(0, name.length() - 1);
			ctx.drawText(textRenderer, name, sx + 22, ry + 8, selected ? Ui.WHITE : Ui.GRAY, true);
			boolean on = i == 0 || items().get(i - 1).done;
			Ui.circle(ctx, sx + sw - 24, ry + 7, 8, on ? Ui.ACCENT : Ui.DIM);
		}
		ctx.disableScissor();
		// content
		Ui.round(ctx, cx, cy, cw, ch, 12, Ui.argb(255, 38, 4, 10));
		if (sel == 0) paintAppearance(ctx, mx, my); else paintItem(ctx, mx, my);
	}

	private void paintAppearance(DrawContext ctx, int mx, int my) {
		Ui.circle(ctx, cx + 16, cy + 14, 14, Ui.ACCENT);
		ctx.drawText(textRenderer, "Оформление", cx + 38, cy + 17, Ui.WHITE, true);
		ctx.drawText(textRenderer, "настройки функции", cx + 16, cy + 36, Ui.GRAY, true);
		int cardY = cy + 52, cardH = Math.min(ch - 62, 104);
		Ui.round(ctx, cx + 8, cardY, cw - 16, cardH, 10, Ui.argb(255, 26, 2, 6));
		// row 0: title toggle
		int r0 = cardY + 14;
		Ui.circle(ctx, cx + 20, r0, 12, cfg().showTitle ? Ui.ACCENT : Ui.DIM);
		ctx.drawText(textRenderer, "Название на панели", cx + 40, r0 + 2, Ui.WHITE, true);
		// row 1: opacity slider
		int r1 = cardY + 44;
		ctx.drawText(textRenderer, "Прозрачность", cx + 20, r1 + 2, Ui.WHITE, true);
		int tx = sliderX(), tw = sliderW();
		Ui.round(ctx, tx, r1 + 5, tw, 4, 2, Ui.argb(255, 70, 30, 36));
		float t = (cfg().opacity - 0.1f) / 0.9f;
		int fillW = (int) (tw * t);
		Ui.round(ctx, tx, r1 + 5, Math.max(4, fillW), 4, 2, Ui.ACCENT);
		Ui.circle(ctx, tx + fillW - 5, r1 + 2, 10, Ui.WHITE);
		// row 2: position
		int r2 = cardY + 74;
		ctx.drawText(textRenderer, "Положение", cx + 20, r2 + 2, Ui.WHITE, true);
		String pos = KvConfig.POSITIONS[cfg().position];
		ctx.drawText(textRenderer, pos, cx + cw - 24 - textRenderer.getWidth(pos), r2 + 2, Ui.ACCENT, true);
	}

	private int sliderX() { return cx + cw - 24 - Math.min(120, cw / 3); }
	private int sliderW() { return Math.min(120, cw / 3); }

	private void paintItem(DrawContext ctx, int mx, int my) {
		KvConfig.Item it = items().get(sel - 1);
		Ui.circle(ctx, cx + 16, cy + 14, 14, it.done ? Ui.ACCENT : Ui.DIM);
		ctx.drawText(textRenderer, "Свой пункт", cx + 38, cy + 17, Ui.WHITE, true);
		ctx.drawText(textRenderer, "вспомогатель: идеи и заметки", cx + 16, cy + 36, Ui.GRAY, true);
		ctx.drawText(textRenderer, "Название", cx + 16, cy + 52, Ui.GRAY, true);
		ctx.drawText(textRenderer, "Заметки", cx + 16, cy + 96, Ui.GRAY, true);
		int ry = cy + 140;
		Ui.circle(ctx, cx + 18, ry, 12, it.done ? Ui.ACCENT : Ui.DIM);
		ctx.drawText(textRenderer, "Сделано", cx + 38, ry + 2, Ui.WHITE, true);
		boolean hover = Ui.in(mx, my, cx + 16, ry + 24, 120, 14);
		ctx.drawText(textRenderer, "Удалить пункт", cx + 18, ry + 28, hover ? Ui.WHITE : Ui.ACCENT, true);
	}

	// ---------------- input ----------------

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (super.mouseClicked(click, doubled)) return true;
		if (click.button() != 0) return false;
		double mx = click.x(), my = click.y();
		// close
		if (Ui.in(mx, my, px + pw - 34, py + 10, 22, 22)) { close(); return true; }
		// sidebar
		if (Ui.in(mx, my, sx, sy, sw, sh)) {
			int idx = (int) ((my - sy - 6 + scroll) / ROW);
			int total = items().size() + 2;
			if (idx >= 0 && idx < total - 1) {
				sel = idx;
				rebuild();
			} else if (idx == total - 1) {
				items().add(new KvConfig.Item());
				sel = items().size();
				rebuild();
				if (nameField != null) setFocused(nameField);
			}
			return true;
		}
		if (sel == 0) {
			int cardY = cy + 52;
			int r0 = cardY + 14, r1 = cardY + 44, r2 = cardY + 74;
			if (Ui.in(mx, my, cx + 14, r0 - 2, cw - 28, 16)) { cfg().showTitle = !cfg().showTitle; return true; }
			if (Ui.in(mx, my, sliderX() - 6, r1 - 2, sliderW() + 12, 16)) { dragging = true; setOpacity(mx); return true; }
			if (Ui.in(mx, my, cx + 14, r2 - 2, cw - 28, 16)) {
				cfg().position = (cfg().position + 1) % KvConfig.POSITIONS.length;
				return true;
			}
		} else {
			KvConfig.Item it = items().get(sel - 1);
			int ry = cy + 140;
			if (Ui.in(mx, my, cx + 14, ry - 2, 140, 16)) { it.done = !it.done; return true; }
			if (Ui.in(mx, my, cx + 16, ry + 24, 120, 14)) {
				items().remove(sel - 1);
				sel = 0;
				rebuild();
				return true;
			}
		}
		return false;
	}

	private void setOpacity(double mx) {
		float t = (float) ((mx - sliderX()) / sliderW());
		t = Math.max(0f, Math.min(1f, t));
		cfg().opacity = 0.1f + 0.9f * t;
	}

	@Override
	public boolean mouseDragged(Click click, double dx, double dy) {
		if (dragging) { setOpacity(click.x()); return true; }
		return super.mouseDragged(click, dx, dy);
	}

	@Override
	public boolean mouseReleased(Click click) {
		dragging = false;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double h, double v) {
		if (Ui.in(mx, my, sx, sy, sw, sh)) {
			int max = Math.max(0, (items().size() + 2) * ROW + 12 - sh);
			scroll = Math.max(0, Math.min(max, scroll - (int) (v * 12)));
			return true;
		}
		return super.mouseScrolled(mx, my, h, v);
	}

	@Override public void removed() { KvConfig.save(); }
	@Override public boolean shouldPause() { return false; }
}
