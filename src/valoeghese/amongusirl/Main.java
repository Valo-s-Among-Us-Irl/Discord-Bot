package valoeghese.amongusirl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {
	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		System.out.println(event.getAuthor().getName());
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
	}
	
	@Override
	public void onGenericGuildMessageReaction(GenericGuildMessageReactionEvent event) {
	}

	// Booter
	public static void main(String[] args) {
		try (FileInputStream fis = new FileInputStream(new File("./properties.txt"))) {
			Properties p = new Properties();
			p.load(fis);
			master = p.getProperty("master");
			new JDABuilder(p.getProperty("key")).addEventListeners(new Main()).build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception running bot!", e);
		}
	}

	static String master;
}
