package com.etterna.multi.services.login;

import javax.transaction.Transactional;

import org.springframework.stereotype.Component;

import com.etterna.multi.data.UserLogin;
import com.etterna.multi.services.PasswordUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PureDbLogin extends LoginProviderBase {

	@Override
	@Transactional
	protected boolean newUser(String username, String password) {
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

	@Override
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
