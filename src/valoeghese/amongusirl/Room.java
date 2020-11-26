package valoeghese.amongusirl;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class Room {
	private Room(String name, String displayName, int id) {
		this.name = name;
		ROOM_BY_NAME.put(name, this);

		this.displayName = displayName;

		this.id = id;
		ROOMS.put(id, this);
	}

	final String name;
	final String displayName;
	public final int id;

	@Override
	public String toString() {
		return this.displayName;
	}

	public static Map<String, Room> ROOM_BY_NAME = new HashMap<>();
	public static final Int2ObjectMap<Room> ROOMS = new Int2ObjectArrayMap<>();

	public static final Room REACTOR = new Room("reactor", "Reactor", 0);
	public static final Room O2 = new Room("o2", "O2", 1);
	public static final Room STORAGE = new Room("storage", "Storage", 2);
	public static final Room HALLWAY = new Room("hallway", "Hallway", 3);
	public static final Room ELECTRICAL = new Room("electrical", "Electrical", 4);
	public static final Room ADMIN = new Room("admin", "Admin", 5);
	public static final Room CAFETERIA = new Room("cafeteria", "Cafeteria", 6);
	public static final Room SHIELDS = new Room("shields", "Shields", 7);
	public static final Room OUTSIDE = new Room("outside", "Outside", 8);
}
