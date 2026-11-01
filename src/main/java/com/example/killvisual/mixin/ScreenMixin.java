package com.example.killvisual.mixin;

import com.example.killvisual.KvBg;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
	@Shadow public int width;
	@Shadow public int height;

	/** Our flowing background on the single-player and multiplayer screens. */
	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void killvisual$background(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		Object self = this;
		if (self instanceof SelectWorldScreen || self instanceof MultiplayerScreen) {
			KvBg.paint(context, width, height);
			ci.cancel();
		} else if (self instanceof net.minecraft.client.gui.screen.ingame.HandledScreen
				&& com.example.killvisual.KvConfig.INSTANCE.invFx) {
			KvBg.paintSoft(context, width, height);
			ci.cancel();
		}
	}
}
