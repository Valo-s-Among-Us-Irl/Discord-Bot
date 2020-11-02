package valoeghese.amongusirl;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

// Actual game tasks implemented as js interactives
public class Task {
	private Task(String name, String displayName, int id) {
		this.name = name;
		this.hash = name.hashCode();
		this.displayName = displayName;
		this.id = id;
		TASKS.put(id, this);
	}

	final String name;
	final String displayName;
	final int hash;
	final int id;

	public static final Int2ObjectMap<Task> TASKS = new Int2ObjectArrayMap<>();
	
	public static final Task UNLOCK_MANIFOLDS = new Task("unlock_manifolds", "Unlock Manifolds", 0);
}
