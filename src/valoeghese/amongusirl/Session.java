package valoeghese.amongusirl;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class Session {
	public Session(int taskCount, int impostors) {
		this.taskCount = taskCount;
		this.impostors = impostors;
		
		List<Object> iroles = AmongUsIRL.config.getList("ImpostorRoles");
		createRoles(iroles, this.impostorCappedRoles, this.impostorWeightedRoles);

		List<Object> croles = AmongUsIRL.config.getList("CrewmateRoles");
	}

	private List<User> users = new ArrayList<>();
	private Object2BooleanMap<User> isImpostor = new Object2BooleanArrayMap<>();
	private List<String> impostorCappedRoles = new ArrayList<>();
	private List<String> impostorWeightedRoles = new ArrayList<>();
	private List<String> crewWeightedRoles = new ArrayList<>();
	private List<String> crewCappedRoles = new ArrayList<>();
	private boolean started = false;
	private int tasksComplete = 0;
	private final int taskCount;
	private final int impostors;

	// Interface

	public void start() {
		this.started = true;
		Collections.shuffle(this.users);

		for (int i = 0; i < this.impostors; ++i) {
			this.isImpostor.put(this.users.get(i), true);
		}

		for (User user : this.users) {
			
		}
	}

	public boolean joinUser(User user) {
		if (this.started) {
			return false;
		} else {
			this.users.add(user);
			return true;
		}
	} // alternatively the 2 liner "if (!this.started) addUser; return !started;"

	// Getters

	public boolean hasStarted() {
		return this.started;
	}

	// Get task progress as percentage
	public int getTaskProgress() {
		return (int) (100 * ((float) this.tasksComplete / (float) this.taskCount));
	}

	// Utils

	private MessageAction message(User user, String message) {
		return user.openPrivateChannel().complete().sendMessage(message);
	}

	private void broadcast(String message) {
		this.broadcastExcept(null, message);
	}

	private void broadcastExcept(User exempt, String message) {
		for (User user : this.users) {
			if (user != exempt) {
				try {
					message(user, message).queue();
				} catch (Exception e) {
					System.err.println("Error sending broadcast PM");
					e.printStackTrace();
				}
			}
		}
	}
	
	// static utils

	private static void createRoles(List<Object> roles, List<String> cappedRoles, List<String> weightedRoles) {
		for (Object o : roles) {
			
		}
	}

	private static int getIdCode(Task task) {
		ZonedDateTime ima = Instant.now().atZone(ZoneOffset.UTC);
		return random(0xFFFF, task.hash, ima.getHour(), ima.getDayOfMonth());
	}

	private static int random(int mask, int seed, int...modifiers) {
		seed = 375462423 * seed + 672456235;

		for (int mod : modifiers) {
			seed += mod;
			seed = 375462423 * seed + 672456235;
		}

		return seed & mask;
	}
}
