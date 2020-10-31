package valoeghese.amongusirl;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

// Actual game tasks implemented as js interactives
public class Task {
	private Task(String name, int id) {
		this.name = name;
		this.hash = name.hashCode();
		this.id = id;
	}

	final String name;
	final int hash;
	final int id;

	public static final Int2ObjectMap<String> TASKS = new Int2ObjectArrayMap<>();
}
