package com.etterna.multi.services.login;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import com.etterna.multi.data.UserLogin;
import com.etterna.multi.repo.UserLoginRepository;

public abstract class LoginProviderBase {
	
	@Autowired
	protected UserLoginRepository repo;
	
	@Transactional
	public UserLogin get(String username) {
		return repo.findById(username.toLowerCase()).orElse(null);
	}
	
	/**
	 * Creates a new account with the given username and password.
	 * Password is salted. Username is converted to lowercase.
	 */
	protected abstract boolean newUser(String username, String password);
	public abstract  boolean login(String username, String password);

}
