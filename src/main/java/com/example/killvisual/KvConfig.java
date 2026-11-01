package com.example.killvisual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class KvConfig {
	public static final class Item {
		public String name = "Новый пункт";
		public String note = "";
		public boolean done = false;
	}

	public static final class Mark {
		public String name = "Метка";
		public double x, y, z;
		public boolean on = true;
	}

	public static final int MAX_MARKS = 5;

	public static final String[] POSITIONS = {
			"По центру сверху", "Слева сверху", "Справа сверху",
			"Слева снизу", "Справа снизу", "По центру снизу"
	};

	public boolean showTitle = true;
	public boolean customTitle = true;
	public String nick = "";
	public float opacity = 0.85f;
	public float hudScale = 0.85f;
	public float menuScale = 1.0f;
	public boolean motionBlur = false;
	public int blurLevel = 2;
	public int position = 0;
	public boolean dragonfly = false;
	public float dragonSpeed = 1.0f;
	public List<Item> items = new ArrayList<>();
	public List<Mark> marks = new ArrayList<>();

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("killvisual.json");
	public static KvConfig INSTANCE = load();

	private static KvConfig load() {
		try {
			if (Files.exists(FILE)) {
				KvConfig c = GSON.fromJson(Files.readString(FILE), KvConfig.class);
				if (c != null) {
					if (c.items == null) c.items = new ArrayList<>();
					if (c.marks == null) c.marks = new ArrayList<>();
					return c;
				}
			}
		} catch (Exception ignored) { }
		return new KvConfig();
	}

	public static void save() {
		try { Files.writeString(FILE, GSON.toJson(INSTANCE)); } catch (Exception ignored) { }
	}
}
