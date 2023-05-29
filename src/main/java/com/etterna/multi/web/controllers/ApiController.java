package com.etterna.multi.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.services.LobbyService;
import com.etterna.multi.services.SessionService;
import com.etterna.multi.web.dto.PlayerDTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
		
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private LobbyService lobbyService;
	
	@GetMapping("/online")
	public List<PlayerDTO> getOnline() {
		m_logger.info("Got request online players");
		
		List<PlayerDTO> o = sessionService.getLoggedInSessions().stream().map(player -> {
			PlayerDTO p = new PlayerDTO();
			p.setName(player.getUsername());
			p.setState(player.getState().name());
			if (player.getLobby() != null)
				p.setLobby(player.getLobby().getName());
			return p;
		}).collect(Collectors.toList());
		
		return o;
	}
	
	
	@GetMapping("/online/{lobbyName}")
	public List<PlayerDTO> getOnlinePlayersInLobby(@PathVariable("lobbyName") String lobbyName) {
		m_logger.info("Got request for players in '{}'", lobbyName);
		
		Lobby lobby = lobbyService.getLobbyByName(lobbyName);
		if (lobby == null) {
			return new ArrayList<>();
		}
		
		return lobby.getPlayers().stream().map(player -> {
			PlayerDTO p = new PlayerDTO();
			p.setName(player.getUsername());
			p.setState(player.getState().name());
			p.setLobby(lobby.getName());
			return p;
		}).collect(Collectors.toList());
	}

}
