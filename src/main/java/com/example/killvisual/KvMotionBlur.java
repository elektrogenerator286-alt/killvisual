package com.example.killvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.memory.ObjectAllocator;
import net.minecraft.util.Identifier;

import java.util.Set;

/** Motion blur: blends the previous frame (kept in a persistent target) into the current one. */
final class KvMotionBlur {
	static boolean failed;
	private static PostEffectProcessor proc;
	private static int level = -1;

	private KvMotionBlur() { }

	static void render(DrawContext ctx, RenderTickCounter tick) {
		KvConfig cfg = KvConfig.INSTANCE;
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!cfg.motionBlur || failed || mc.player == null) return;
		try {
			int lv = Math.max(0, Math.min(4, cfg.blurLevel));
			if (proc == null || lv != level) {
				if (proc != null) proc.close();
				proc = mc.getShaderLoader().loadPostEffect(Identifier.of("killvisual", "mb_" + lv),
						Set.of(PostEffectProcessor.MAIN));
				level = lv;
				if (proc == null) { failed = true; return; }
			}
			proc.render(mc.getFramebuffer(), ObjectAllocator.TRIVIAL);
		} catch (Throwable e) {
			failed = true;
			proc = null;
		}
	}
}
