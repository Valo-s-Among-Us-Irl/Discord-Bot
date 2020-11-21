package valoeghese.amongusirl;

public class ConfiguredTask {
	public ConfiguredTask(Task task, Room room) {
		this.task = task;
		this.room = room;
		this.origin = room;
	}

	final Task task;
	final Room origin;
	Room room; // can change
	int part = 0; // what part of the task you are in
	long target = 0;

	@Override
	public String toString() {
		return this.room.toString() + ": " + (((this.task == Task.TRANSFER_DATA) && (this.room == AmongUsIRL.uploadRoom)) ? "Upload Data" : this.task.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof ConfiguredTask) {
			ConfiguredTask other = (ConfiguredTask) obj;
			return this.room == other.room && this.task == other.task;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 31 * this.room.hashCode() + this.task.hashCode();
	}
}
