package valoeghese.amongusirl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

// Actual game tasks implemented as js interactives
public class Task {
	private Task(String name, String displayName, int id, int stages, Type type) {
		this.name = name;
		TASKS_BY_NAME.put(name, this);

		this.hash = name.hashCode();
		this.displayName = displayName;

		this.id = id;
		TASKS.put(id, this);

		this.type = type;
		TASKS_BY_TYPE.computeIfAbsent(type, t -> new ArrayList<>()).add(this);
		
		this.stages = stages;
	}

	final String name;
	final String displayName;
	public final int hash;
	public final int id;
	final int stages;
	final Type type;

	@Override
	public String toString() {
		return this.displayName;
	}

	public static final Int2ObjectMap<Task> TASKS = new Int2ObjectArrayMap<>();
	public static final Map<String, Task> TASKS_BY_NAME = new HashMap<>();
	public static final Map<Type, List<Task>> TASKS_BY_TYPE = new EnumMap<>(Type.class);

	public static final Task UNLOCK_MANIFOLDS = new Task("unlock_manifolds", "Unlock Manifolds", 0, 1, Type.SHORT);
	public static final Task TRANSFER_DATA = new Task("transfer_data", "Download Data", 1, 2, Type.SHORT); // also [UploadRoom]:Upload Data
	public static final Task REBOOT_WIFI = new Task("reboot_wifi", "Reboot Wifi", 2, 2, Type.LONG);
	public static final Task FUEL_ENGINES = new Task("fuel_engines", "Fuel Engines", 3, 4, Type.LONG);
	public static final Task SWIPE_CARD = new Task("swipe_card", "Swipe Card", 4, 1, Type.COMMON);
	public static final Task EMPTY_GARBAGE = new Task("empty_garbage", "Empty Garbage", 5, 2, Type.SHORT);
	public static final Task O2_1 = new Task("o2_1", "Fix Oxygen: 1", 6, 1, Type.EMERGENCY);
	public static final Task O2_2 = new Task("o2_2", "Fix Oxygen: 2", 7, 1, Type.EMERGENCY);
	// second half of reboot wifi
	public static final Task REBOOT_WIFI_2 = new Task("reboot_wifi_2", "Reboot Wifi", 8, 2, Type.LONG);

	public static enum Type {
		SHORT,
		LONG,
		COMMON,
		EMERGENCY
	}
}
