package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

/** Own armor on the HUD, and gear tags above other players. */
final class KvGear {
	private static final EquipmentSlot[] ARMOR = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

	private KvGear() { }

	static void render(DrawContext ctx, RenderTickCounter tick) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) return;
		KvConfig cfg = KvConfig.INSTANCE;
		if (cfg.armorHud) armorHud(ctx, mc);
		if (cfg.enemyTags && mc.currentScreen == null) tags(ctx, mc, tick);
	}

	private static void armorHud(DrawContext ctx, MinecraftClient mc) {
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		int x = sw / 2 + 96, y = sh - 22;
		Ui.round(ctx, x - 3, y - 3, 4 * 18 + 4, 22, 6, Ui.withAlpha(Ui.argb(255, 12, 6, 8), KvConfig.INSTANCE.opacity));
		for (int i = 0; i < 4; i++) {
			ItemStack st = mc.player.getEquippedStack(ARMOR[i]);
			if (st.isEmpty()) continue;
			ctx.drawItem(st, x + i * 18, y);
			ctx.drawStackOverlay(mc.textRenderer, st, x + i * 18, y);
		}
	}

	private static void tags(DrawContext ctx, MinecraftClient mc, RenderTickCounter tick) {
		if (mc.world == null) return;
		float td = tick.getTickProgress(false);
		Camera cam = mc.gameRenderer.getCamera();
		Vec3d cp = mc.player.getCameraPosVec(td);
		double yaw = Math.toRadians(cam.getYaw()), pit = Math.toRadians(cam.getPitch());
		Vec3d f = new Vec3d(-Math.sin(yaw) * Math.cos(pit), -Math.sin(pit), Math.cos(yaw) * Math.cos(pit));
		Vec3d r = new Vec3d(-Math.cos(yaw), 0, -Math.sin(yaw));
		Vec3d u = r.crossProduct(f);
		int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
		double k = (sh / 2.0) / Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
		TextRenderer tr = mc.textRenderer;
		for (PlayerEntity p : mc.world.getPlayers()) {
			if (p == mc.player || p.isInvisible() && false) continue;
			Vec3d d = p.getLerpedPos(td).add(0, p.getHeight() + 0.65, 0).subtract(cp);
			double z = d.dotProduct(f);
			if (z < 0.5 || z > 64) continue;
			double sx = sw / 2.0 + d.dotProduct(r) / z * k;
			double sy = sh / 2.0 - d.dotProduct(u) / z * k;
			if (sx < -60 || sx > sw + 60 || sy < -40 || sy > sh + 40) continue;
			float s = (float) Math.max(0.55, Math.min(1.0, 7.0 / z + 0.35));
			ctx.getMatrices().pushMatrix();
			ctx.getMatrices().translate((float) sx, (float) sy);
			ctx.getMatrices().scale(s, s);
			// 4 armor + 2 hands = 6 slots
			int w = 6 * 18 + 10, h = 36;
			Ui.round(ctx, -w / 2, -h, w, h, 6, Ui.withAlpha(Ui.argb(255, 12, 6, 8), 0.8f));
			Ui.ring(ctx, -w / 2, -h, w, h, 6, 1, Ui.ACCENT);
			String name = p.getName().getString();
			Ui.text(ctx, tr, name, -Ui.width(tr, name, false) / 2, -h + 3, Ui.WHITE, false);
			ItemStack[] all = { p.getEquippedStack(EquipmentSlot.HEAD), p.getEquippedStack(EquipmentSlot.CHEST),
					p.getEquippedStack(EquipmentSlot.LEGS), p.getEquippedStack(EquipmentSlot.FEET),
					p.getMainHandStack(), p.getOffHandStack() };
			for (int i = 0; i < 6; i++) {
				int ix = -w / 2 + 5 + i * 18, iy = -h + 16;
				if (i == 4) Ui.round(ctx, ix - 1, iy - 1, 1, 18, 0, Ui.GRAY);
				if (all[i].isEmpty()) continue;
				ctx.drawItem(all[i], ix, iy);
				ctx.drawStackOverlay(tr, all[i], ix, iy);
			}
			ctx.getMatrices().popMatrix();
		}
	}
}
