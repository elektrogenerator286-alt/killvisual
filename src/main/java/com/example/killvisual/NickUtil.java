package com.example.killvisual;

import com.example.killvisual.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Changes the in-game nickname. Works for offline (non-licensed) launchers only. */
public final class NickUtil {
	private NickUtil() { }

	public static boolean valid(String name) {
		return name != null && name.matches("[A-Za-z0-9_]{3,16}");
	}

	public static void apply(String name) {
		MinecraftClient mc = MinecraftClient.getInstance();
		Session old = mc.getSession();
		UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		Session fresh = new Session(name, id, old.getAccessToken(), old.getXuid(), old.getClientId());
		((MinecraftClientAccessor) mc).killvisual$setSession(fresh);
	}
}
