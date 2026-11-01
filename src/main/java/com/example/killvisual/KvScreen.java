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
	private int sel = 0;
	private int scroll = 0;
	private int dragId;   // which slider is being dragged: 1 opacity, 2 HUD size, 3 menu size, 4 blur, 5 Dragonfly speed, 6 view distance, 7 simulation distance
	private TextFieldWidget nameField, noteField;
	private TextFieldWidget mName, mX, mY, mZ;
	private String markErr = "";
	private Integer viewTmp, simTmp;

	private static final int ROW = 22;
	private static final int BASE = 5;       // 0 = Оформление, 1 = Dragonfly, 2 = Motion Blur, 3 = Метки, 4 = Оптимизация, BASE.. = custom items
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
		placeMarkFields();
	}

	private void rebuild() {
		clearChildren();
		nameField = null;
		noteField = null;
		mName = mX = mY = mZ = null;
		if (sel == 3) buildMarkFields();
		if (sel >= BASE && sel - BASE < items().size()) {
			KvConfig.Item it = items().get(sel - BASE);
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
		} else if (sel > 4) {
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
		int total = items().size() + BASE + 1;
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
			String name = i == 0 ? "Оформление" : i == 1 ? "Dragonfly" : i == 2 ? "Motion Blur" : i == 3 ? "Метки" : i == 4 ? "Оптимизация" : items().get(i - BASE).name;
			while (Ui.width(textRenderer, name, false) > sw - 54 && name.length() > 1) name = name.substring(0, name.length() - 1);
			Ui.text(ctx, textRenderer, name, sx + 22, ry + 8, selected ? Ui.WHITE : Ui.GRAY, false);
			boolean on = i == 0 || (i == 1 ? cfg().dragonfly : i == 2 ? cfg().motionBlur : i == 3 ? !cfg().marks.isEmpty() : i == 4 ? KvOptimize.anyOn() : items().get(i - BASE).done);
			Ui.circle(ctx, sx + sw - 24, ry + 7, 8, on ? Ui.ACCENT : Ui.DIM);
		}
		ctx.disableScissor();
		// content
		Ui.round(ctx, cx, cy, cw, ch, 12, Ui.argb(255, 38, 4, 10));
		if (sel == 0) paintAppearance(ctx, mx, my);
		else if (sel == 1) paintDragonfly(ctx);
		else if (sel == 2) paintMotionBlur(ctx);
		else if (sel == 3) paintMarks(ctx, mx, my);
		else if (sel == 4) paintOptimize(ctx);
		else paintItem(ctx, mx, my);
	}

	private void paintAppearance(DrawContext ctx, int mx, int my) {
		Ui.circle(ctx, cx + 16, cy + 14, 14, Ui.ACCENT);
		Ui.text(ctx, textRenderer, "Оформление", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "настройки функции", cx + 16, cy + 36, Ui.GRAY, false);
		int cardY = cardY(), cardH = Math.min(ch - 52, 6 * 21 + 14);
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
	}

	private void paintMotionBlur(DrawContext ctx) {
		Ui.circle(ctx, cx + 16, cy + 14, 14, cfg().motionBlur ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Motion Blur", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "размытие при движении", cx + 16, cy + 36, Ui.GRAY, false);
		Ui.round(ctx, cx + 8, cardY(), cw - 16, 2 * 21 + 14, 10, Ui.argb(255, 26, 2, 6));
		Ui.circle(ctx, cx + 20, ry(0), 12, cfg().motionBlur ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, KvMotionBlur.failed ? "Motion Blur (ошибка)" : "Motion Blur включён", cx + 40, ry(0) + 2, Ui.WHITE, false);
		slider(ctx, ry(1), "Сила блюра", cfg().blurLevel / 4f, String.valueOf(cfg().blurLevel + 1));
	}

	private void paintDragonfly(DrawContext ctx) {
		Ui.circle(ctx, cx + 16, cy + 14, 14, cfg().dragonfly ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Dragonfly", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "скорость полёта, сам полёт не включается", cx + 16, cy + 36, Ui.GRAY, false);
		Ui.round(ctx, cx + 8, cardY(), cw - 16, 3 * 21 + 14, 10, Ui.argb(255, 26, 2, 6));
		Ui.circle(ctx, cx + 20, ry(0), 12, cfg().dragonfly ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Dragonfly включён", cx + 40, ry(0) + 2, Ui.WHITE, false);
		slider(ctx, ry(1), "Скорость полёта", (cfg().dragonSpeed - 0.2f) / 4.8f, String.format("x%.1f", cfg().dragonSpeed));
		Ui.text(ctx, textRenderer, "Работает, когда вы уже летите", cx + 20, ry(2) + 2, Ui.GRAY, false);
	}

	private void paintOptimize(DrawContext ctx) {
		Ui.circle(ctx, cx + 16, cy + 14, 14, KvOptimize.anyOn() ? Ui.ACCENT : Ui.DIM);
		Ui.text(ctx, textRenderer, "Оптимизация", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "настройки, которые повышают FPS", cx + 16, cy + 36, Ui.GRAY, false);
		int n = KvOptimize.Opt.values().length;
		Ui.round(ctx, cx + 8, cardY(), cw - 16, Math.min(ch - 52, (n + 2) * 21 + 14), 10, Ui.argb(255, 26, 2, 6));
		for (int i = 0; i < n; i++) {
			KvOptimize.Opt o = KvOptimize.Opt.values()[i];
			Ui.circle(ctx, cx + 20, ry(i), 12, KvOptimize.isOn(o) ? Ui.ACCENT : Ui.DIM);
			Ui.text(ctx, textRenderer, o.label, cx + 40, ry(i) + 2, Ui.WHITE, false);
		}
		var o = client.options;
		int view = viewTmp != null ? viewTmp : o.getViewDistance().getValue();
		int sim = simTmp != null ? simTmp : o.getSimulationDistance().getValue();
		slider(ctx, ry(n), "Дальность прорисовки", (view - 2) / 30f, String.valueOf(view));
		slider(ctx, ry(n + 1), "Дальность симуляции", (sim - 5) / 27f, String.valueOf(sim));
	}

	private void buildMarkFields() {
		mName = new TextFieldWidget(textRenderer, 0, 0, 10, 18, Text.empty());
		mName.setMaxLength(16);
		mName.setPlaceholder(Text.literal("Название метки"));
		mX = coordField("X");
		mY = coordField("Y (можно пусто)");
		mZ = coordField("Z");
		addDrawableChild(mName);
		addDrawableChild(mX);
		addDrawableChild(mY);
		addDrawableChild(mZ);
		placeMarkFields();
	}

	private TextFieldWidget coordField(String hint) {
		TextFieldWidget f = new TextFieldWidget(textRenderer, 0, 0, 10, 18, Text.empty());
		f.setMaxLength(12);
		f.setPlaceholder(Text.literal(hint));
		return f;
	}

	private void placeMarkFields() {
		if (mName == null) return;
		mName.setX(cx + 16); mName.setY(cy + 50); mName.setWidth(cw - 32);
		int fw = (cw - 32 - 12) / 3;
		mX.setX(cx + 16); mX.setY(cy + 74); mX.setWidth(fw);
		mY.setX(cx + 16 + fw + 6); mY.setY(cy + 74); mY.setWidth(fw);
		mZ.setX(cx + 16 + 2 * (fw + 6)); mZ.setY(cy + 74); mZ.setWidth(fw);
	}

	private int markRowY(int i) { return cy + 130 + i * 18; }

	private void paintMarks(DrawContext ctx, int mx, int my) {
		Ui.circle(ctx, cx + 16, cy + 14, 14, cfg().marks.isEmpty() ? Ui.DIM : Ui.ACCENT);
		Ui.text(ctx, textRenderer, "Метки", cx + 38, cy + 17, Ui.WHITE, true);
		Ui.text(ctx, textRenderer, "координаты и расстояние до метки", cx + 16, cy + 36, Ui.GRAY, false);
		boolean hAdd = Ui.in(mx, my, cx + 16, cy + 98, 108, 16);
		Ui.round(ctx, cx + 16, cy + 98, 108, 16, 8, hAdd ? Ui.argb(255, 120, 24, 34) : Ui.argb(255, 86, 20, 28));
		Ui.text(ctx, textRenderer, "Добавить метку", cx + 26, cy + 102, Ui.WHITE, false);
		boolean hPos = Ui.in(mx, my, cx + 130, cy + 98, 100, 16);
		Ui.round(ctx, cx + 130, cy + 98, 100, 16, 8, hPos ? Ui.argb(255, 60, 20, 26) : Ui.argb(255, 40, 12, 16));
		Ui.text(ctx, textRenderer, "Моя позиция", cx + 142, cy + 102, Ui.WHITE, false);
		if (!markErr.isEmpty()) Ui.text(ctx, textRenderer, markErr, cx + 16, cy + 118, Ui.ACCENT, false);
		ctx.enableScissor(cx, cy, cx + cw, cy + ch);
		var player = client == null ? null : client.player;
		for (int i = 0; i < cfg().marks.size(); i++) {
			KvConfig.Mark m = cfg().marks.get(i);
			int y = markRowY(i);
			Ui.circle(ctx, cx + 18, y + 2, 10, m.on ? Ui.ACCENT : Ui.DIM);
			String dist = player == null ? "" : Math.round(KvMarks.distance(player, m)) + " бл.";
			String line = m.name + "  " + Math.round(m.x) + " " + Math.round(m.y) + " " + Math.round(m.z);
			int maxW = cw - 80 - Ui.width(textRenderer, dist, false);
			while (Ui.width(textRenderer, line, false) > maxW && line.length() > 1) line = line.substring(0, line.length() - 1);
			Ui.text(ctx, textRenderer, line, cx + 34, y + 3, Ui.WHITE, false);
			Ui.text(ctx, textRenderer, dist, cx + cw - 30 - Ui.width(textRenderer, dist, false), y + 3, Ui.GRAY, false);
			boolean hx = Ui.in(mx, my, cx + cw - 24, y, 16, 16);
			Ui.text(ctx, textRenderer, "x", cx + cw - 20, y + 3, hx ? Ui.WHITE : Ui.ACCENT, true);
		}
		ctx.disableScissor();
	}

	private static double num(String t) {
		return Double.parseDouble(t.trim().replace(',', '.'));
	}

	private void addMark() {
		if (cfg().marks.size() >= KvConfig.MAX_MARKS) { markErr = "Максимум меток: " + KvConfig.MAX_MARKS; return; }
		try {
			KvConfig.Mark m = new KvConfig.Mark();
			m.x = num(mX.getText());
			m.z = num(mZ.getText());
			if (mY.getText().isBlank()) m.y = client != null && client.player != null ? client.player.getY() : 64;
			else m.y = num(mY.getText());
			String n = mName.getText().trim();
			m.name = n.isEmpty() ? "Метка " + (cfg().marks.size() + 1) : n;
			cfg().marks.add(m);
			mName.setText("");
			mX.setText("");
			mY.setText("");
			mZ.setText("");
			markErr = "";
		} catch (NumberFormatException e) {
			markErr = "Впишите числа в X и Z";
		}
	}

	private void fillMyPosition() {
		if (client == null || client.player == null) return;
		mX.setText(String.valueOf(client.player.getBlockX()));
		mY.setText(String.valueOf(client.player.getBlockY()));
		mZ.setText(String.valueOf(client.player.getBlockZ()));
	}

	private int ry(int i) { return cardY() + 8 + 21 * i; }

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
		KvConfig.Item it = items().get(sel - BASE);
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
			int total = items().size() + BASE + 1;
			if (idx >= 0 && idx < total - 1) {
				sel = idx;
				rebuild();
			} else if (idx == total - 1) {
				items().add(new KvConfig.Item());
				sel = items().size() - 1 + BASE;
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
		} else if (sel == 4) {
			int n = KvOptimize.Opt.values().length;
			for (int i = 0; i < n; i++) {
				if (Ui.in(mx, my, cx + 14, ry(i) - 2, cw - 28, 18)) { KvOptimize.toggle(KvOptimize.Opt.values()[i]); return true; }
			}
			if (Ui.in(mx, my, sliderX() - 6, ry(n) - 2, sliderW() + 12, 18)) { dragId = 6; setSlider(6, mx); return true; }
			if (Ui.in(mx, my, sliderX() - 6, ry(n + 1) - 2, sliderW() + 12, 18)) { dragId = 7; setSlider(7, mx); return true; }
		} else if (sel == 3) {
			if (Ui.in(mx, my, cx + 16, cy + 98, 108, 16)) { addMark(); return true; }
			if (Ui.in(mx, my, cx + 130, cy + 98, 100, 16)) { fillMyPosition(); return true; }
			for (int i = 0; i < cfg().marks.size(); i++) {
				int y = markRowY(i);
				if (Ui.in(mx, my, cx + cw - 24, y, 16, 16)) { cfg().marks.remove(i); return true; }
				if (Ui.in(mx, my, cx + 14, y - 2, cw - 44, 18)) { cfg().marks.get(i).on = !cfg().marks.get(i).on; return true; }
			}
		} else if (sel == 2) {
			if (Ui.in(mx, my, cx + 14, ry(0) - 2, cw - 28, 18)) {
				cfg().motionBlur = !cfg().motionBlur;
				KvMotionBlur.failed = false;
				return true;
			}
			if (Ui.in(mx, my, sliderX() - 6, ry(1) - 2, sliderW() + 12, 18)) {
				dragId = 4;
				setSlider(4, mx);
				return true;
			}
		} else if (sel == 1) {
			if (Ui.in(mx, my, cx + 14, ry(0) - 2, cw - 28, 18)) { cfg().dragonfly = !cfg().dragonfly; return true; }
			if (Ui.in(mx, my, sliderX() - 6, ry(1) - 2, sliderW() + 12, 18)) {
				dragId = 5;
				setSlider(5, mx);
				return true;
			}
		} else {
			KvConfig.Item it = items().get(sel - BASE);
			int ry = cy + 140;
			if (Ui.in(mx, my, cx + 14, ry - 2, 140, 16)) { it.done = !it.done; return true; }
			if (Ui.in(mx, my, cx + 16, ry + 24, 120, 14)) {
				items().remove(sel - BASE);
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
			case 6 -> viewTmp = 2 + Math.round(t * 30);
			case 7 -> simTmp = 5 + Math.round(t * 27);
			case 5 -> cfg().dragonSpeed = Math.round((0.2f + 4.8f * t) * 10f) / 10f;
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
		if (dragId == 6 || dragId == 7) {
			KvOptimize.applyDistances(viewTmp, simTmp);
			viewTmp = null;
			simTmp = null;
		}
		if (dragId == 3) { dragId = 0; init(); } // apply the new menu size
		dragId = 0;
		moving = false;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double h, double v) {
		if (Ui.in(mx, my, sx, sy, sw, sh)) {
			int max = Math.max(0, (items().size() + BASE + 1) * ROW + 12 - sh);
			scroll = Math.max(0, Math.min(max, scroll - (int) (v * 12)));
			return true;
		}
		return super.mouseScrolled(mx, my, h, v);
	}

	@Override public void removed() { KvConfig.save(); }
	@Override public boolean shouldPause() { return false; }
}
