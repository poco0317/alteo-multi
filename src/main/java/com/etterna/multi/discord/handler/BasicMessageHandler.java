package com.etterna.multi.discord.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.etterna.multi.services.LobbyService;
import com.etterna.multi.services.MultiplayerService;
import com.etterna.multi.services.SessionService;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
@Scope("prototype")
public class BasicMessageHandler extends ListenerAdapter {
	
	@Value("${discord.channelid}")
	private String channelId;
	
	private static final String CMD_PLAYERCOUNT = "m!status";
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private LobbyService lobbyService;
	
	@Autowired
	private MultiplayerService multiplayerService;
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		String msg = event.getMessage().getContentDisplay();
		
		if (event.getChannel() == null || !event.getChannel().getId().equals(channelId)) {
			return;
		}
		
		if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
			return;
		}
		
		if (event.isWebhookMessage()) {
			return;
		}
		
		
		if (msg.equals(CMD_PLAYERCOUNT)) {
			int connected = sessionService.getSessionCount();
			int loggedIn = sessionService.getLoggedInSessions().size();
			int lobbies = lobbyService.getAllLobbies(false).size();
			
			event.getChannel().sendMessage(
					String.format(
							"Users Connected: %d\nUsers Logged In: %d\nLobbies Open: %d",
							connected,
							loggedIn,
							lobbies)
					)
				.queue();
		}
		else {
			String username = event.getAuthor().getEffectiveName() + " (Discord)";
			multiplayerService.chatToMainLobby(username, msg);
		}
	}
}
