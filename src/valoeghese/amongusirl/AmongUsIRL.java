package valoeghese.amongusirl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tk.valoeghese.zoesteriaconfig.api.ZoesteriaConfig;
import tk.valoeghese.zoesteriaconfig.api.container.WritableConfig;
import tk.valoeghese.zoesteriaconfig.api.template.ConfigTemplate;
import valoeghese.amongusirl.util.DelayedTask;
import valoeghese.amongusirl.util.OrderedList;

public class AmongUsIRL extends ListenerAdapter {
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		User author = event.getAuthor();

		if (session != null) {
			if (session.hasUser(author) && session.hasStarted()) {
				String result = session.acceptMessage(author, event.getMessage().getContentRaw());

				if (result != null) {
					event.getChannel().sendMessage(result).queue();
				}
			}
		}
	}

	@Override
	public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
		User user = event.getUser();

		if (session != null) {	
			if (session.hasUser(user) && session.hasStarted()) {
				session.acceptReaction(event.getMessageIdLong(), user, event.getReactionEmote().toString());
			}
		}
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		if (event.getMessageId().equals(sessionMsg)) {
			if (event.getReactionEmote().toString().equals("RE:U+1f50e")) {
				if (session != null) {
					session.joinUser(event.getUser());
				}
			}
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if (master.equals(event.getAuthor().getAsTag())) {
			try {
				String[] content = event.getMessage().getContentRaw().split(" ");

				if (content.length > 0) {
					switch(content[0]) {
					case "a.new":
						sessionMsg = null;
						session = new Session(Math.max(1, Integer.parseInt(content[1])));
						sessionMsg = event.getChannel().sendMessage("Started new session! React with :mag_right: to join.").complete().getId();
						break;
					case "a.start":
						if (session == null) {
							event.getChannel().sendMessage("Session not yet created!").queue();
						} else if (session.hasStarted()) {
							event.getChannel().sendMessage("Session has already started!").queue();
						} else {
							event.getChannel().sendMessage("Started session!").queue();
							session.start();
						}
						break;
					default:
						break;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				event.getChannel().sendMessage("Syntax: `a.new <impostors>`").queue();
			} catch (NumberFormatException e) {
				event.getChannel().sendMessage(e.getLocalizedMessage()).queue();
			} catch (Exception e) {
				e.printStackTrace(System.out); // probably not important
			}
		}
	}

	// Booter
	public static void main(String[] args) throws IOException {
		// Load, Update or Create config.
		File file = new File("settings.zfg");
		file.createNewFile();

		config = ZoesteriaConfig.loadConfigWithDefaults(file, ConfigTemplate.builder()
				.addList("ImpostorRoles", l -> {
					WritableConfig impostor = ZoesteriaConfig.createWritableConfig(new LinkedHashMap<>());
					impostor.putStringValue("Name", "an Impostor");
					impostor.putIntegerValue("Entries", 1);
					impostor.putBooleanValue("Capped", false);
					l.add(impostor.asMap());
				})
				.addList("CrewmateRoles", l -> {
					WritableConfig impostor = ZoesteriaConfig.createWritableConfig(new LinkedHashMap<>());
					impostor.putStringValue("Name", "a Crewmate");
					impostor.putIntegerValue("Entries", 1);
					impostor.putBooleanValue("Capped", false);
					l.add(impostor.asMap());
				})
				.addList("EnabledRooms", l -> {
					l.add(Room.REACTOR.name);
					l.add(Room.O2.name);
					l.add(Room.STORAGE.name);
					l.add(Room.HALLWAY.name);
					l.add(Room.ELECTRICAL.name);
					l.add(Room.ADMIN.name);
					l.add(Room.CAFETERIA.name);
					l.add(Room.SHIELDS.name);
				})
				.addContainer("EnabledTasks", c -> c
						.addList(Room.REACTOR.name, l -> {
							l.add(Task.UNLOCK_MANIFOLDS.name);
							l.add(Task.TRANSFER_DATA.name);					
						})
						.addList(Room.O2.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
							l.add(Task.EMPTY_GARBAGE.name);
						})
						.addList(Room.STORAGE.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
							l.add(Task.FUEL_ENGINES.name);
						})
						.addList(Room.HALLWAY.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
						})
						.addList(Room.ELECTRICAL.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
							l.add(Task.REBOOT_WIFI.name);
						})
						.addList(Room.ADMIN.name, l -> {
							l.add(Task.SWIPE_CARD.name);
						})
						.addList(Room.CAFETERIA.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
							l.add(Task.EMPTY_GARBAGE.name);
						})
						.addList(Room.SHIELDS.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
						}))
				.addContainer("Tasks", c -> c
						.addDataEntry("Common", 1)
						.addDataEntry("Long", 1)
						.addDataEntry("Short", 2))
				.addDataEntry("UploadRoom", Room.ADMIN.name)
				.addDataEntry("EngineRoom1", Room.OUTSIDE.name)
				.addDataEntry("EngineRoom2", Room.OUTSIDE.name)
				.addDataEntry("GarbageRoom", Room.STORAGE.name)
				.addContainer("Sabotages", c -> c
						.addDataEntry("O2", true))
				.build());

		config.writeToFile(file);

		uploadRoom = Room.ROOM_BY_NAME.get(config.getStringValue("UploadRoom"));
		engineRoom1 = Room.ROOM_BY_NAME.get(config.getStringValue("EngineRoom1"));
		engineRoom2 = Room.ROOM_BY_NAME.get(config.getStringValue("EngineRoom2"));
		garbageRoom = Room.ROOM_BY_NAME.get(config.getStringValue("EngineRoom2"));

		delayedTaskWorker = new Thread(() -> {
			long nextExec = System.currentTimeMillis() + 500;
			long thisExec = 0;

			while (true) {
				while (thisExec < nextExec) { // yeah I know I can do a one-liner with blank body but that's messy
					thisExec = System.currentTimeMillis();
				}

				nextExec = thisExec + 500;

				synchronized (delayedTasks) {
					List<DelayedTask> toRemove = new ArrayList<>();

					for (DelayedTask task : delayedTasks) {
						if (task.target > thisExec) { // this works because tasks are ordered by their target time to execute
							break; // therefore if the target is greater than thisExec, it applies to all subsequent tasks.
						}

						task.runnable.run();
						toRemove.add(task);
					}

					for (DelayedTask task : toRemove) {
						delayedTasks.remove(task);
					}
				}
			}
		});

		delayedTaskWorker.setDaemon(true);
		delayedTaskWorker.start();

		// bootstrap JDA
		try (FileInputStream fis = new FileInputStream(new File("./properties.txt"))) {
			Properties p = new Properties();
			p.load(fis);
			master = p.getProperty("master");
			JDABuilder.createDefault(p.getProperty("key")).addEventListeners(new AmongUsIRL()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception running bot!", e);
		}
	}

	static Thread delayedTaskWorker;
	static List<DelayedTask> delayedTasks = new OrderedList<>(dTask -> dTask.target);
	static String sessionMsg = null;
	static String master;
	static Session session = null;
	static Room uploadRoom, engineRoom1, engineRoom2, garbageRoom;
	static WritableConfig config;
	static final Random RANDOM = new Random();
}
