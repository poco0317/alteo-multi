package com.etterna.multi.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.etterna.multi.data.UserLogin;
import com.etterna.multi.services.login.LoginProviderBase;

@Service
public class UserLoginService {
		
	@Autowired
	private ApplicationContext ctx;
	
	private LoginProviderBase loginProvider;
	
	@Value("${etterna.login.provider-class}")
	private String loginProviderClass;
	
	@EventListener
	public void handleApplicationReadyEvent(ApplicationReadyEvent evt) throws Exception {
		loginProvider = (LoginProviderBase) ctx.getBean(Class.forName(loginProviderClass));
	}
	
	public UserLogin get(String username) {
		return loginProvider.get(username);
	}
	
	/**
	 * Attempts to verify that a username and password combination is correct.
	 * Does not check that a session already exists, so a user may log in infinite times.
	 * All usernames are converted to lowercase.
	 */
	@Transactional
	public boolean login(String username, String password) {
		return loginProvider.login(username, password);
	}

}
