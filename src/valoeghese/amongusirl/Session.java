package valoeghese.amongusirl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import tk.valoeghese.zoesteriaconfig.api.ZoesteriaConfig;
import tk.valoeghese.zoesteriaconfig.api.container.Container;
import valoeghese.amongusirl.util.DelayedTask;
import valoeghese.amongusirl.util.Util;

public class Session {
	public Session(int impostors) {
		// Set up task lists
		List<Object> rooms = AmongUsIRL.config.getList("EnabledRooms");
		Container enabledTasks = AmongUsIRL.config.getContainer("EnabledTasks");

		for (Object o : rooms) {
			String room = (String) o;
			List<Object> tasksInRoom = enabledTasks.getList(room);

			if (tasksInRoom != null) {
				for (Object oo : tasksInRoom) {
					Task task = Task.TASKS_BY_NAME.get((String) oo);

					switch (task.type) {
					case COMMON:
						addTask(this.commonTaskIndex, this.commonTaskList, task, room);
						break;
					case LONG:
						addTask(this.longTaskIndex, this.longTaskList, task, room);
						break;
					case SHORT:
						addTask(this.shortTaskIndex, this.shortTaskList, task, room);
						break;
					case EMERGENCY:
						// not a normal task
						break;
					}
				}
			}
		}

		this.impostors = impostors;

		// Make Sub Roles
		List<Object> iroles = AmongUsIRL.config.getList("ImpostorRoles");
		createRoles(iroles, this.impostorCappedRoles, this.impostorWeightedRoles);
		this.impostorWeighted = (this.impostorWeightedRoles.size() == 1 ?
				() -> this.impostorWeightedRoles.get(0) :
					() -> this.impostorWeightedRoles.get(AmongUsIRL.RANDOM.nextInt(this.impostorWeightedRoles.size()))
				);

		List<Object> croles = AmongUsIRL.config.getList("CrewmateRoles");
		createRoles(croles, this.crewCappedRoles, this.crewWeightedRoles);

		this.crewWeighted = (this.crewWeightedRoles.size() == 1 ?
				() -> this.crewWeightedRoles.get(0) :
					() -> this.crewWeightedRoles.get(AmongUsIRL.RANDOM.nextInt(this.crewWeightedRoles.size()))
				);
	}

	private final Map<Task, List<ConfiguredTask>> commonTaskIndex = new HashMap<>();
	private final List<Task> commonTaskList = new ArrayList<>();
	private final Map<Task, List<ConfiguredTask>> longTaskIndex = new HashMap<>();
	private final List<Task> longTaskList = new ArrayList<>();
	private final Map<Task, List<ConfiguredTask>> shortTaskIndex = new HashMap<>();
	private final List<Task> shortTaskList = new ArrayList<>();

	private List<User> users = new ArrayList<>();
	private Object2BooleanMap<User> isImpostor = new Object2BooleanArrayMap<>();
	private Map<User, List<ConfiguredTask>> tasks = new LinkedHashMap<>();

	private final List<String> impostorCappedRoles = new ArrayList<>();
	private final List<String> impostorWeightedRoles = new ArrayList<>();
	private final Supplier<String> impostorWeighted;
	private final List<String> crewWeightedRoles = new ArrayList<>();
	private final List<String> crewCappedRoles = new ArrayList<>();
	private final Supplier<String> crewWeighted;
	private final List<Message> sabotagePrompts = new ArrayList<>();

	private boolean started = false;
	private int tasksComplete = 0;
	private int taskCount = 0;
	private final int impostors;
	private Sabotage currentSabotage = null;

	// Interface

	public void start() {
		this.started = true;

		if (this.users.isEmpty()) {
			return;
		}

		// Shuffle users for random impostors
		Collections.shuffle(this.users);

		for (int i = 0; i < this.impostors; ++i) {
			this.isImpostor.put(this.users.get(i), true);
		}

		// Shuffle users for random sub roles
		Collections.shuffle(this.users);

		final int commonTasks = AmongUsIRL.config.getIntegerValue("Tasks.Common");
		final int longTasks = AmongUsIRL.config.getIntegerValue("Tasks.Long");
		final int shortTasks = AmongUsIRL.config.getIntegerValue("Tasks.Short");

		List<ConfiguredTask> commonDistributedTasks = new ArrayList<>();
		// common tasks are distributed commonly
		this.delegateTasks(commonDistributedTasks, commonTasks, this.commonTaskIndex, this.commonTaskList);

		// Give sub roles and delegate tasks
		for (User user : this.users) {
			String role = "missingno";
			boolean impostor = this.isImpostor.getBoolean(user);

			if (impostor) {
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

			List<ConfiguredTask> userTasks = new ArrayList<>();

			for (ConfiguredTask task : commonDistributedTasks) {
				userTasks.add(task);
			}

			this.delegateTasks(userTasks, longTasks, this.longTaskIndex, this.longTaskList);
			this.delegateTasks(userTasks, shortTasks, this.shortTaskIndex, this.shortTaskList);

			this.tasks.put(user, userTasks);

			StringBuilder sb = new StringBuilder();

			for (ConfiguredTask t : userTasks) {
				sb.append('\n').append(t.toString());
			}

			if (impostor) {
				this.message(user, "Fake Tasks:" + sb.toString()).queue();
				this.sabotagePrompts.add(this.message(user, "Sabotages:\n:zero: - Start an oxygen crisis (delay: 2 minutes)").complete());
			} else {
				this.message(user, "Tasks:" + sb.toString()).queue();
				this.taskCount += (commonTasks + longTasks + shortTasks);
			}
		}
		
		for (Message message : this.sabotagePrompts) {
			message.addReaction("\u0030\uFE0F\u20E3").queue();
		}
	}

	private void delegateTasks(List<ConfiguredTask> userTasks, int tasks, Map<Task, List<ConfiguredTask>> index, List<Task> list) {
		if (tasks > 0) {
			Collections.shuffle(list);

			// delegate tasks or fake tasks
			for (int i = 0; i < tasks; ++i) {
				Task task = list.get(i);
				List<ConfiguredTask> variants = index.get(task);
				userTasks.add(variants.get(AmongUsIRL.RANDOM.nextInt(variants.size())));
			}
		}
	}

	public void acceptReaction(User user, String reaction) {
		if (reaction.equals("RE:U+30U+fe0fU+20e3")) {
			final Sabotage sabotage = new Sabotage(Sabotage.Type.OXYGEN);
			this.currentSabotage = sabotage;
			this.broadcast("**SABOTAGE!** The __oxygen__ has been sabotaged.\nEmergency Task: [O2] Fix O2. You have 30 seconds.");
			
			DelayedTask oxygenEndEvent = new DelayedTask(System.currentTimeMillis() + 30 * 1000, () -> {
				if (this.currentSabotage == sabotage && !this.currentSabotage.fixed) {
					this.win("Impostors win: Oxygen Depleted!");
				}
			});

			synchronized (AmongUsIRL.delayedTasks) {
				AmongUsIRL.delayedTasks.add(oxygenEndEvent);
			}
		}
	}

	public String acceptMessage(User user, String message) {
		if (!isImpostor.getBoolean(user)) {
			try {
				int code = Integer.parseInt(message);
				final ConfiguredTask tsk = new ConfiguredTask(Util.getTask(code), Util.getRoom(code));

				if (tsk.task == null) {
					return "Invalid task encoded in given code.";
				}

				//System.out.println(tsk); // DEBUG
				if (tsk.room == null) {
					return "Invalid room encoded in given code.";
				}

				// check validity of code
				int[] shouldCodes = Util.getIdCodes(tsk.task, tsk.room);
				if (!(shouldCodes[0] == code || shouldCodes[1] == code)) {
					return "Outdated code!";
				}

				// reboot wifi 2 treated as reboot wifi outside of code
				ConfiguredTask t = new ConfiguredTask(tsk.task == Task.REBOOT_WIFI_2 ? Task.REBOOT_WIFI : tsk.task, tsk.room);

				AtomicReference<ConfiguredTask> t1 = new AtomicReference<>();

				// see if the user actually has the task
				if (tasks.get(user).stream().anyMatch(task -> {
					if (task.equals(t)) {
						t1.set(task); // set specific instance
						return true;
					}
					return false;
				})) {
					ConfiguredTask ct = t1.get(); // use specific instance

					if (++ct.part == ct.task.stages) {
						long now = System.currentTimeMillis();

						if (now < ct.target) {
							return "This task is not ready to be performed! Remaining time: " + (int) Math.floor(ct.target - now) + " seconds.";
						}

						if (ct.task == Task.REBOOT_WIFI) {
							if (tsk.task != Task.REBOOT_WIFI_2) {
								return "You must reactivate the wifi, not turn it off again, silly!";
							}
						}

						tasks.get(user).remove(t);
						this.tasksComplete++;
						
						if (this.tasks.isEmpty()) {
							if (this.tasksComplete == this.taskCount) {
								this.win("Crewmates win: All Tasks Completed!");
								return null;
							} else {
								return "Completed Task! No remaining tasks.";
							}
						} else {
							StringBuilder sb = new StringBuilder("Completed Task! Remaining Tasks:");

							List<ConfiguredTask> userTasks = this.tasks.get(user);

							for (ConfiguredTask usertask : userTasks) {
								sb.append('\n').append(usertask.toString());
							}

							return sb.toString();
						}
					} else {
						if (ct.task == Task.TRANSFER_DATA) {
							ct.room = AmongUsIRL.uploadRoom;
						} else if (ct.task == Task.FUEL_ENGINES) {
							switch (ct.part) {
							case 1:
								ct.room = AmongUsIRL.engineRoom1;
								break;
							case 2:
								ct.room = ct.origin;
								break;
							case 3:
								ct.room = AmongUsIRL.engineRoom2;
								break;
							}
						} else if (ct.task == Task.EMPTY_GARBAGE) {
							ct.room = AmongUsIRL.garbageRoom;
						} else if (ct.task == Task.REBOOT_WIFI) {
							ct.target = System.currentTimeMillis() + 1000 * 60; // 60 seconds

							DelayedTask reMessage = new DelayedTask(ct.target, () -> {
								if (AmongUsIRL.session == this) { // check session still working
									message(user, "The WiFi is ready to be turned on!");
								}
							});

							synchronized (AmongUsIRL.delayedTasks) {
								AmongUsIRL.delayedTasks.add(reMessage);
							}
						}

						StringBuilder sb = new StringBuilder("Completed Part " + ct.part + "/" + ct.task.stages + "! Remaining Tasks:");

						List<ConfiguredTask> userTasks = this.tasks.get(user);

						for (ConfiguredTask usertask : userTasks) {
							sb.append('\n').append(usertask.toString());
						}

						return sb.toString();
					}
				}

				return "You do not have this task, silly!";
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private void win(String string) {
		this.broadcast(string);
		AmongUsIRL.session = null;
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

	public boolean hasUser(User user) {
		return this.users.contains(user);
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

	private static void addTask(Map<Task, List<ConfiguredTask>> map, List<Task> list, Task task, String room) {
		ConfiguredTask ct = new ConfiguredTask(task, Room.ROOM_BY_NAME.get(room));
		map.computeIfAbsent(task, l -> new ArrayList<>()).add(ct);

		if (!list.contains(task)) {
			list.add(task);
		}
	}
}
