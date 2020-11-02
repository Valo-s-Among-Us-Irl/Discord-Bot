package valoeghese.amongusirl;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class Room {
	private Room(String name, String displayName, int id) {
		this.name = name;
		this.displayName = displayName;
		this.id = id;
		ROOMS.put(id, this);
	}

	final String name;
	final String displayName;
	final int id;

	public static final Int2ObjectMap<Room> ROOMS = new Int2ObjectArrayMap<>();

	public static final Room REACTOR = new Room("reactor", "Reactor", 0);
	public static final Room O2 = new Room("o2", "O2", 1);
	public static final Room STORAGE = new Room("storage", "Storage", 2);
	public static final Room HALLWAY = new Room("hallway", "Hallway", 3);
	public static final Room ELECTRICAL = new Room("electrical", "Electrical", 4);
	public static final Room ADMIN = new Room("admin", "Admin", 5);
	public static final Room CAFETERIA = new Room("cafeteria", "Cafeteria", 6);
	public static final Room SHIELDS = new Room("shields", "Shields", 7);
}
