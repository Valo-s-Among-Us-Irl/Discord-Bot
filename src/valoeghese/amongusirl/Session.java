package valoeghese.amongusirl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import tk.valoeghese.zoesteriaconfig.api.ZoesteriaConfig;
import tk.valoeghese.zoesteriaconfig.api.container.Container;

public class Session {
	public Session(int taskCount, int impostors) {
		this.taskCount = taskCount;
		this.impostors = impostors;
		
		List<Object> iroles = AmongUsIRL.config.getList("ImpostorRoles");
		createRoles(iroles, this.impostorCappedRoles, this.impostorWeightedRoles);
		this.impostorWeighted = this.impostorWeightedRoles.size() == 1 ?
				() -> this.impostorWeightedRoles.get(0) :
				() -> this.impostorWeightedRoles.get(AmongUsIRL.RANDOM.nextInt(this.impostorWeightedRoles.size()));

		List<Object> croles = AmongUsIRL.config.getList("CrewmateRoles");
		createRoles(croles, this.crewCappedRoles, this.crewWeightedRoles);
		
		this.crewWeighted = this.crewWeightedRoles.size() == 1 ?
				() -> this.crewWeightedRoles.get(0) :
				() -> this.crewWeightedRoles.get(AmongUsIRL.RANDOM.nextInt(this.crewWeightedRoles.size()));
	}

	private List<User> users = new ArrayList<>();
	private Object2BooleanMap<User> isImpostor = new Object2BooleanArrayMap<>();
	private final List<String> impostorCappedRoles = new ArrayList<>();
	private final List<String> impostorWeightedRoles = new ArrayList<>();
	private final Supplier<String> impostorWeighted;
	private final List<String> crewWeightedRoles = new ArrayList<>();
	private final List<String> crewCappedRoles = new ArrayList<>();
	private final Supplier<String> crewWeighted;
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

		Collections.shuffle(this.users);

		for (User user : this.users) {
			String role = "missingno";

			if (this.isImpostor.getBoolean(user)) {
				if (!this.impostorCappedRoles.isEmpty()) {
					role = this.impostorCappedRoles.remove(0);
				} else {
					role = this.impostorWeighted.get();
				}
			} else {
				if (!this.crewCappedRoles.isEmpty()) {
					role = this.crewCappedRoles.remove(0);
				} else {
					role = this.crewWeighted.get();
				}
			}
			
			this.message(user, "You are " + role).queue();
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

	@SuppressWarnings("unchecked")
	private static void createRoles(List<Object> roles, List<String> cappedRoles, List<String> weightedRoles) {
		for (Object o : roles) {
			Container data = ZoesteriaConfig.createWritableConfig((Map<String, Object>) o);

			final int count = data.getIntegerValue("Entries");
			final List<String> map = data.getBooleanValue("Capped") ? cappedRoles : weightedRoles;
			final String name = data.getStringValue("Name");

			for (int i = 0; i < count; ++i) {
				map.add(name);
			}
		}

		// for edge case scenarios where not all are used
		Collections.shuffle(cappedRoles);
	}
}
