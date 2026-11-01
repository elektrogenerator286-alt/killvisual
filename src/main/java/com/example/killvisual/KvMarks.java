package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.Vec3d;

/** Draws the saved marks on the screen with the distance in blocks. */
final class KvMarks {
	private KvMarks() { }

	static double distance(ClientPlayerEntity p, KvConfig.Mark m) {
		Vec3d eye = p.getEyePos();
		double dx = m.x - eye.x, dy = m.y - eye.y, dz = m.z - eye.z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	static void render(DrawContext ctx, RenderTickCounter tick) {
		MinecraftClient mc = MinecraftClient.getInstance();
		ClientPlayerEntity p = mc.player;
		if (p == null || mc.options.hudHidden) return;
		KvConfig cfg = KvConfig.INSTANCE;
		if (cfg.marks.isEmpty()) return;
		TextRenderer tr = mc.textRenderer;

		int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
		double yaw = Math.toRadians(p.getYaw()), pitch = Math.toRadians(p.getPitch());
		double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
		double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
		double ux = -rz * fy, uy = rz * fx - rx * fz, uz = rx * fy;
		double tanHalf = Math.tan(Math.toRadians(mc.options.getFov().getValue()) / 2.0);
		double aspect = (double) w / h;
		Vec3d eye = p.getEyePos();

		for (KvConfig.Mark m : cfg.marks) {
			if (!m.on) continue;
			double dx = m.x - eye.x, dy = m.y - eye.y, dz = m.z - eye.z;
			double cz = dx * fx + dy * fy + dz * fz;
			double cxv = dx * rx + dz * rz;
			double cyv = dx * ux + dy * uy + dz * uz;
			int dist = (int) Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));

			int margin = 14;
			double sx, sy;
			boolean edge = true;
			sx = w / 2.0;
			sy = h / 2.0;
			if (cz > 0.05) {
				double nx = cxv / cz / (tanHalf * aspect), ny = cyv / cz / tanHalf;
				double px = w / 2.0 + nx * w / 2.0, py = h / 2.0 - ny * h / 2.0;
				if (px >= margin && px <= w - margin && py >= margin && py <= h - margin) {
					sx = px;
					sy = py;
					edge = false;
				}
			}
			if (edge) {
				double dirx = cxv, diry = -cyv;
				double len = Math.sqrt(dirx * dirx + diry * diry);
				if (len < 1e-6) { dirx = 1; diry = 0; len = 1; }
				dirx /= len;
				diry /= len;
				double tx = Math.abs(dirx) < 1e-6 ? 1e9 : (w / 2.0 - margin) / Math.abs(dirx);
				double ty = Math.abs(diry) < 1e-6 ? 1e9 : (h / 2.0 - margin) / Math.abs(diry);
				double t = Math.min(tx, ty);
				sx = w / 2.0 + dirx * t;
				sy = h / 2.0 + diry * t;
			}

			int ix = (int) Math.round(sx), iy = (int) Math.round(sy);
			Ui.circle(ctx, ix - 5, iy - 5, 10, Ui.WHITE);
			Ui.circle(ctx, ix - 4, iy - 4, 8, edge ? Ui.DIM : Ui.ACCENT);
			String label = m.name + "  " + dist + " бл.";
			int lw = Ui.width(tr, label, false);
			int lx = Math.max(2, Math.min(w - lw - 2, ix - lw / 2));
			int ly = iy + 8 > h - 12 ? iy - 18 : iy + 8;
			Ui.text(ctx, tr, label, lx, ly, Ui.WHITE, false);
		}
	}
}
