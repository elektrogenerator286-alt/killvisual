package com.example.killvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KillVisualClient implements ClientModInitializer {
	private static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of("killvisual", "controls"));
	private static KeyBinding menuKey;
	private static KeyBinding flyKey;

	@Override
	public void onInitializeClient() {
		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.killvisual.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
		flyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.killvisual.dragonfly", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			while (flyKey.wasPressed()) {
				if (c.currentScreen == null && c.player != null) {
					KvConfig.INSTANCE.dragonfly = !KvConfig.INSTANCE.dragonfly;
					KvConfig.save();
				}
			}
			KvDragonfly.tick(c);
			while (menuKey.wasPressed()) {
				if (c.currentScreen == null && c.player != null) c.setScreen(new KvScreen());
			}
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(c -> {
			String n = KvConfig.INSTANCE.nick;
			if (NickUtil.valid(n)) NickUtil.apply(n);
		});
		HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS,
				Identifier.of("killvisual", "motion_blur"), KvMotionBlur::render);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
				Identifier.of("killvisual", "fps_ping"), KvHud::render);
		HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
				Identifier.of("killvisual", "marks"), KvMarks::render);
	}
}
