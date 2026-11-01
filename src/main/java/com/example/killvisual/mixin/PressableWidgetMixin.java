package com.example.killvisual.mixin;

import com.example.killvisual.Ui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin extends ClickableWidget {
	protected PressableWidgetMixin(int x, int y, int w, int h, Text message) {
		super(x, y, w, h, message);
	}

	/** Rounded red-black buttons on the single-player and multiplayer screens. */
	@Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
	private void killvisual$button(DrawContext ctx, CallbackInfo ci) {
		Screen screen = MinecraftClient.getInstance().currentScreen;
		if (!(screen instanceof SelectWorldScreen || screen instanceof MultiplayerScreen)) return;
		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		boolean hover = isHovered() || isFocused();
		boolean on = this.active;
		int r = Math.max(4, h / 3);
		int border = !on ? Ui.argb(255, 60, 50, 54) : hover ? Ui.ACCENT : Ui.argb(255, 110, 50, 62);
		int left = !on ? Ui.argb(255, 26, 22, 24) : hover ? Ui.argb(255, 120, 14, 30) : Ui.argb(255, 16, 10, 16);
		int right = !on ? Ui.argb(255, 34, 28, 30) : hover ? Ui.argb(255, 70, 10, 40) : Ui.argb(255, 72, 10, 28);
		Ui.round(ctx, x, y, w, h, r + 1, border);
		Ui.roundGradient(ctx, x + 1, y + 1, w - 2, h - 2, r, left, right);
		ci.cancel();
	}
}
