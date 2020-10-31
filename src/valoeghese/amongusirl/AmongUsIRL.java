package valoeghese.amongusirl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Properties;

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
		System.out.println("e");
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
						session = new Session(Math.max(1, Integer.parseInt(content[1])), Math.max(1, Integer.parseInt(content[2])));
						sessionMsg = event.getChannel().sendMessage("Started new session! React with :mag_right: to join.").complete().getId();
						break;
					case "a.start":
						if (session == null) {
							event.getChannel().sendMessage("Session not yet created!");
						} else if (session.hasStarted()) {
							event.getChannel().sendMessage("Session has already started!");
						} else {
							event.getChannel().sendMessage("Started session!");
							session.start();
						}
						break;
					default:
						break;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				event.getChannel().sendMessage("Syntax: `a.new <taskCount> <impostors>`").queue();
			} catch (NumberFormatException e) {
				event.getChannel().sendMessage(e.getLocalizedMessage()).queue();
			} catch (Exception e) {
				e.printStackTrace(System.out); // probably not important
			}
		}
	}

	// Booter
	public static void main(String[] args) throws IOException {
		// Load, Update, or Create config.
		File file = new File("settings.zfg");
		file.createNewFile();

		config = ZoesteriaConfig.loadConfigWithDefaults(file, ConfigTemplate.builder()
				.addList("Impostor Roles", l -> {
					WritableConfig impostor = ZoesteriaConfig.createWritableConfig(new LinkedHashMap<>());
					impostor.putStringValue("Name", "Impostor");
					impostor.putIntegerValue("Entries", 1);
					impostor.putBooleanValue("Capped", false);
				})
				.addList("Crewmate Roles", l -> {
					WritableConfig impostor = ZoesteriaConfig.createWritableConfig(new LinkedHashMap<>());
					impostor.putStringValue("Name", "Crewmate");
					impostor.putIntegerValue("Entries", 1);
					impostor.putBooleanValue("Capped", false);
				})
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
}
