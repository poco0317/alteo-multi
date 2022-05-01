package com.etterna.multi.services;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.etterna.multi.data.UserLogin;
import com.etterna.multi.repo.UserLoginRepository;

@Service
public class UserLoginService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(UserLoginService.class);
	
	@Autowired
	private UserLoginRepository repo;
	
	private UserLogin get(String username) {
		return repo.findById(username.toLowerCase()).orElse(null);
	}
	
	/**
	 * Creates a new account with the given username and password.
	 * Password is salted. Username is converted to lowercase.
	 */
	@Transactional
	public boolean newUser(String username, String password) {
		
		final String salt = PasswordUtil.getSalt();
		final String saltedPass = PasswordUtil.hashPassword(password, salt);
		
		UserLogin user = get(username);
		if (user == null) {
			m_logger.info("Created new user: {}", username.toLowerCase());
			
			user = new UserLogin();
			user.setUsername(username.toLowerCase());
			user.setPassword(saltedPass);
			user.setSalt(salt);
			repo.save(user);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Attempts to verify that a username and password combination is correct.
	 * Does not check that a session already exists, so a user may log in infinite times.
	 * All usernames are converted to lowercase.
	 */
	@Transactional
	public boolean login(String username, String password) {
		
		final UserLogin user = get(username);
		if (user == null) {
			// must create new user
			
			final boolean o = newUser(username, password);
			if (o) {
				m_logger.info("Logged in user: {}", username.toLowerCase());
			} else {
				m_logger.info("Failed user login (user already exists): {}", username.toLowerCase());
			}
			return o;
		} else {
			// user exists, verify that the password is correct
			
			final String salt = user.getSalt();
			final String saltedPass = PasswordUtil.hashPassword(password, salt);
			final String storedPass = user.getPassword();
			
			final boolean o = storedPass.equals(saltedPass); 
			if (o) {
				m_logger.info("Logged in user: {}", username.toLowerCase());
				return true;
			} else {
				m_logger.info("Failed user login (bad password): {}", username.toLowerCase());
				return false;
			}
		}
	}

}
