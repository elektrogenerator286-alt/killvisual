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
	private int dragId;   // which slider is being dragged: 1 opacity, 2 HUD size, 3 menu size
	private TextFieldWidget nameField, noteField;

	private static final int ROW = 22;
	private boolean placed, moving;
	private int offX, offY;

	public KvScreen() { super(Text.literal("KillVisual")); }

	private KvConfig cfg() { return KvConfig.INSTANCE; }
	private List<KvConfig.Item> items() { return cfg().items; }

	@Override
	protected void init() {
		pw = Math.min(width - 20, (int) (400 * cfg().menuScale));
		ph = Math.min(height - 20, (int) (320 * cfg().menuScale));
		if (!placed) { px = (width - pw) / 2; py = (height - ph) / 2; placed = true; }
		px = Math.max(0, Math.min(width - pw, px));
		py = Math.max(0, Math.min(height - ph, py));
		layout();
		rebuild();
	}

	private void layout() {
		sx = px + 12; sy = py + 38; sw = (int) (pw * 0.34); sh = ph - 50;
		cx = sx + sw + 8; cy = sy; cw = px + pw - 12 - cx; ch = sh;
	}

	private void moveFields() {
		if (nameField != null) { nameField.setX(cx + 16); nameField.setY(cy + 62); nameField.setWidth(cw - 32); }
		if (noteField != null) { noteField.setX(cx + 16); noteField.setY(cy + 106); noteField.setWidth(cw - 32); }
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
		double tm = (System.currentTimeMillis() % 6000000L) / 1000.0;
		float w1 = (float) (Math.sin(tm * 0.9) * 0.5 + 0.5), w2 = (float) (Math.sin(tm * 0.9 + 2.1) * 0.5 + 0.5);
		Ui.round(ctx, px - 1, py - 1, pw + 2, ph + 2, 14,
				Ui.argb(255, 40 + (int) (80 * w1), 8 + (int) (20 * w2), 14 + (int) (40 * w2)));
		Ui.roundGradient(ctx, px, py, pw, ph, 14,
				Ui.argb(255, 10 + (int) (60 * w2), 8 + (int) (6 * w1), 12 + (int) (28 * w1)),
				Ui.argb(255, 60 + (int) (80 * w1), 8 + (int) (12 * w2), 20 + (int) (50 * w2)));
		Ui.text(ctx, textRenderer, "KillVisual", px + 18, py + 14, Ui.ACCENT, true);
		// close X
		int xx = px + pw - 28, xy = py + 12;
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
				Ui.text(ctx, textRenderer, "+ Добавить пункт", sx + 20, ry + 8, hover ? Ui.WHITE : Ui.GRAY, false);
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
			while (Ui.width(textRenderer, name, false) > sw - 54 && name.length() > 1) name = name.substring(0, name.length() - 1);
			Ui.text(ctx, textRenderer, name, sx + 22, ry + 8, selected ? Ui.WHITE : Ui.GRAY, false);
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
		Ui.text(ctx, textRenderer, "Оформление", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "настройки функции", cx + 16, cy + 36, Ui.GRAY, false);
		int cardY = cardY(), cardH = Math.min(ch - 52, 11 * 19 + 14);
		Ui.round(ctx, cx + 8, cardY, cw - 16, cardH, 10, Ui.argb(255, 26, 2, 6));
		Ui.circle(ctx, cx + 20, ry(0), 12, cfg().showTitle ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Название на панели", cx + 40, ry(0) + 2, Ui.WHITE, false);
		slider(ctx, ry(1), "Прозрачность", (cfg().opacity - 0.1f) / 0.9f, Math.round(cfg().opacity * 100) + "%");
		slider(ctx, ry(2), "Размер плашки", (cfg().hudScale - 0.6f) / 0.9f, String.format("x%.2f", cfg().hudScale));
		slider(ctx, ry(3), "Размер меню", (cfg().menuScale - 0.9f) / 0.5f, String.format("x%.2f", cfg().menuScale));
		Ui.text(ctx, textRenderer, "Положение", cx + 20, ry(4) + 2, Ui.WHITE, false);
		String pos = KvConfig.POSITIONS[cfg().position];
		Ui.text(ctx, textRenderer, pos, cx + cw - 24 - Ui.width(textRenderer, pos, false), ry(4) + 2, Ui.ACCENT, false);
		Ui.circle(ctx, cx + 20, ry(5), 12, cfg().customTitle ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Свое главное меню", cx + 40, ry(5) + 2, Ui.WHITE, false);
		Ui.circle(ctx, cx + 20, ry(6), 12, cfg().motionBlur ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, KvMotionBlur.failed ? "Motion Blur (ошибка)" : "Motion Blur", cx + 40, ry(6) + 2, Ui.WHITE, false);
		slider(ctx, ry(7), "Сила блюра", cfg().blurLevel / 4f, String.valueOf(cfg().blurLevel + 1));
		int half = (cw - 16) / 2;
		Ui.circle(ctx, cx + 20, ry(8), 12, cfg().armorHud ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "ArmorHud", cx + 40, ry(8) + 2, Ui.WHITE, false);
		Ui.circle(ctx, cx + 20 + half, ry(8), 12, cfg().enemyTags ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Таблички", cx + 40 + half, ry(8) + 2, Ui.WHITE, false);
		Ui.text(ctx, textRenderer, "Atmosphere", cx + 20, ry(9) + 2, Ui.WHITE, false);
		String at = KvConfig.ATMOS[cfg().atmo];
		Ui.text(ctx, textRenderer, at, cx + 20 + half - 12 - Ui.width(textRenderer, at, false), ry(9) + 2, Ui.ACCENT, false);
		Ui.circle(ctx, cx + 20 + half, ry(9), 12, cfg().invFx ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Фон инвентаря", cx + 40 + half, ry(9) + 2, Ui.WHITE, false);
		slider(ctx, ry(10), "Сила атмосферы", cfg().atmoPower, Math.round(cfg().atmoPower * 100) + "%");
	}

	private int ry(int i) { return cardY() + 6 + 19 * i; }

	private int cardY() { return cy + 46; }

	private void slider(DrawContext ctx, int y, String label, float t, String value) {
		t = Math.max(0f, Math.min(1f, t));
		Ui.text(ctx, textRenderer, label, cx + 20, y + 2, Ui.WHITE, false);
		int tx = sliderX(), tw = sliderW();
		Ui.round(ctx, tx, y + 5, tw, 4, 2, Ui.argb(255, 70, 30, 36));
		int fillW = (int) (tw * t);
		Ui.round(ctx, tx, y + 5, Math.max(4, fillW), 4, 2, Ui.ACCENT);
		Ui.circle(ctx, tx + fillW - 5, y + 2, 10, Ui.WHITE);
		Ui.text(ctx, textRenderer, value, tx - 8 - Ui.width(textRenderer, value, false), y + 2, Ui.GRAY, false);
	}

	private int sliderX() { return cx + cw - 24 - Math.min(110, cw / 3); }
	private int sliderW() { return Math.min(110, cw / 3); }

	private void paintItem(DrawContext ctx, int mx, int my) {
		KvConfig.Item it = items().get(sel - 1);
		Ui.circle(ctx, cx + 16, cy + 14, 14, it.done ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Свой пункт", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "вспомогатель: идеи и заметки", cx + 16, cy + 36, Ui.GRAY, false);
		Ui.text(ctx, textRenderer, "Название", cx + 16, cy + 52, Ui.GRAY, false);
		Ui.text(ctx, textRenderer, "Заметки", cx + 16, cy + 96, Ui.GRAY, false);
		int ry = cy + 140;
		Ui.circle(ctx, cx + 18, ry, 12, it.done ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Сделано", cx + 38, ry + 2, Ui.WHITE, false);
		boolean hover = Ui.in(mx, my, cx + 16, ry + 24, 120, 14);
		Ui.text(ctx, textRenderer, "Удалить пункт", cx + 18, ry + 28, hover ? Ui.WHITE : Ui.ACCENT, false);
	}

	// ---------------- input ----------------

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (super.mouseClicked(click, doubled)) return true;
		if (click.button() != 0) return false;
		double mx = click.x(), my = click.y();
		// close
		if (Ui.in(mx, my, px + pw - 32, py + 8, 22, 22)) { close(); return true; }
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
			int cardY = cardY();
			if (Ui.in(mx, my, cx + 14, ry(0) - 2, cw - 28, 18)) { cfg().showTitle = !cfg().showTitle; return true; }
			for (int id = 1; id <= 3; id++) {
				if (Ui.in(mx, my, sliderX() - 6, ry(id) - 2, sliderW() + 12, 18)) {
					dragId = id;
					setSlider(id, mx);
					return true;
				}
			}
			if (Ui.in(mx, my, cx + 14, ry(4) - 2, cw - 28, 18)) {
				cfg().position = (cfg().position + 1) % KvConfig.POSITIONS.length;
				return true;
			}
			if (Ui.in(mx, my, cx + 14, ry(5) - 2, cw - 28, 18)) { cfg().customTitle = !cfg().customTitle; return true; }
			if (Ui.in(mx, my, cx + 14, ry(6) - 2, cw - 28, 18)) {
				cfg().motionBlur = !cfg().motionBlur;
				KvMotionBlur.failed = false;
				return true;
			}
			int half = (cw - 16) / 2;
			if (Ui.in(mx, my, cx + 14, ry(8) - 2, half - 4, 18)) { cfg().armorHud = !cfg().armorHud; return true; }
			if (Ui.in(mx, my, cx + 14 + half, ry(8) - 2, half - 4, 18)) { cfg().enemyTags = !cfg().enemyTags; return true; }
			if (Ui.in(mx, my, cx + 14, ry(9) - 2, half - 4, 18)) { cfg().atmo = (cfg().atmo + 1) % KvConfig.ATMOS.length; return true; }
			if (Ui.in(mx, my, cx + 14 + half, ry(9) - 2, half - 4, 18)) { cfg().invFx = !cfg().invFx; return true; }
			if (Ui.in(mx, my, sliderX() - 6, ry(10) - 2, sliderW() + 12, 18)) {
				dragId = 5;
				setSlider(5, mx);
				return true;
			}
			if (Ui.in(mx, my, sliderX() - 6, ry(7) - 2, sliderW() + 12, 18)) {
				dragId = 4;
				setSlider(4, mx);
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
		// grab the frame / title area to move the window
		if (Ui.in(mx, my, px, py, pw, ph) && !Ui.in(mx, my, cx, cy, cw, ch)) {
			moving = true;
			offX = (int) (mx - px);
			offY = (int) (my - py);
			return true;
		}
		return false;
	}

	private void setSlider(int id, double mx) {
		float t = (float) ((mx - sliderX()) / sliderW());
		t = Math.max(0f, Math.min(1f, t));
		switch (id) {
			case 1 -> cfg().opacity = 0.1f + 0.9f * t;
			case 2 -> cfg().hudScale = 0.6f + 0.9f * t;
			case 4 -> cfg().blurLevel = Math.round(t * 4);
			case 5 -> cfg().atmoPower = t;
			default -> cfg().menuScale = 0.9f + 0.5f * t;
		}
	}

	@Override
	public boolean mouseDragged(Click click, double dx, double dy) {
		if (dragId != 0) { setSlider(dragId, click.x()); return true; }
		if (moving) {
			px = Math.max(0, Math.min(width - pw, (int) (click.x() - offX)));
			py = Math.max(0, Math.min(height - ph, (int) (click.y() - offY)));
			layout();
			moveFields();
			return true;
		}
		return super.mouseDragged(click, dx, dy);
	}

	@Override
	public boolean mouseReleased(Click click) {
		if (dragId == 3) { dragId = 0; init(); } // apply the new menu size
		dragId = 0;
		moving = false;
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
