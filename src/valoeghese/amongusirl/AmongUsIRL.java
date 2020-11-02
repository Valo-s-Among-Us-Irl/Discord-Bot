package valoeghese.amongusirl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Random;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tk.valoeghese.zoesteriaconfig.api.ZoesteriaConfig;
import tk.valoeghese.zoesteriaconfig.api.container.WritableConfig;
import tk.valoeghese.zoesteriaconfig.api.template.ConfigTemplate;

public class AmongUsIRL extends ListenerAdapter {
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		if (event.getMessageId().equals(sessionMsg)) {
			if (event.getReactionEmote().toString().equals("RE:U+1f50e")) {
				session.joinUser(event.getUser());
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
							l.add(Task.EMPTY_GARBAGE.name);
						})
						.addList(Room.HALLWAY.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
						})
						.addList(Room.ELECTRICAL.name, l -> {
							l.add(Task.TRANSFER_DATA.name);
							l.add(Task.REBOOT_WIFI.name);
						})
						.addList(Room.ADMIN.name, l -> {
							l.add(Task.ENTER_ID_CODE.name);
						})
						.addList(Room.CAFETERIA.name, l -> {
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
				.build());

		config.writeToFile(file);

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

	static String sessionMsg = null;
	static String master;
	static Session session = null;
	static WritableConfig config;
	static final Random RANDOM = new Random();
}
