package valoeghese.amongusirl;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class Util {
	private Util() {
	}

	public static int getIdCode(Task task, Room room) {
		ZonedDateTime ima = Instant.now().atZone(ZoneOffset.UTC);
		int shiftedTimeKey = random(0x3FF, task.hash, ima.getHour(), ima.getDayOfMonth(), ima.getMinute() / 2) << 3;
		return ((shiftedTimeKey | room.id) << 6) | (0b111111 & task.id);
	}

	public static int random(int mask, int seed, int...modifiers) {
		seed = 375462423 * seed + 672456235;

		for (int mod : modifiers) {
			seed += mod;
			seed = 375462423 * seed + 672456235;
		}

		return seed & mask;
	}

	public static Task getTask(int code) {
		return Task.TASKS.get(0b111111 & code);
	}

	public static Room getRoom(int code) {
		return Room.ROOMS.get((code >> 6) & 0b111);
	}
}
