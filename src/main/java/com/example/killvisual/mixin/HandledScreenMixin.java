package com.example.killvisual.mixin;

import com.example.killvisual.KvBg;
import com.example.killvisual.KvConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
	@Shadow protected int backgroundWidth;
	@Shadow protected int backgroundHeight;

	/** drawSlots runs with the matrix already moved to the panel corner, so we draw at 0,0. */
	@Inject(method = "drawSlots", at = @At("HEAD"))
	private void killvisual$shimmer(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
		if (!KvConfig.INSTANCE.invFx) return;
		KvBg.paintPanel(context, 0, 0, backgroundWidth, backgroundHeight);
	}
}
