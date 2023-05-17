package com.etterna.multi.web.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etterna.multi.services.SessionService;
import com.etterna.multi.web.dto.PlayerDTO;

@RestController
@RequestMapping("/api")
public class ApiController {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ApiController.class);
	
	@Autowired
	private SessionService sessionService;
	
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
	
	

}
