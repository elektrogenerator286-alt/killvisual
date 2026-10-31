package com.example.killvisual.mixin;

import com.example.killvisual.KvConfig;
import com.example.killvisual.KvTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	/** Swap the vanilla title screen for ours. */
	@ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
	private Screen killvisual$replaceTitle(Screen screen) {
		if (screen != null && screen.getClass() == TitleScreen.class && KvConfig.INSTANCE.customTitle) {
			return new KvTitleScreen();
		}
		return screen;
	}
}
