package valoeghese.amongusirl;

public class ConfiguredTask {
	public ConfiguredTask(Task task, Room room) {
		this.task = task;
		this.room = room;
	}

	final Task task;
	Room room; // can change
	int part = 0; // what part of the task you are in

	@Override
	public String toString() {
		return this.room.toString() + ": " + this.task.toString();
	}
}
