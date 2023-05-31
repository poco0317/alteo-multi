package com.etterna.multi.services.login;

import java.util.Arrays;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.etterna.multi.data.UserLogin;
import com.etterna.multi.services.PasswordUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Login users using site API. If the password is correct, create a matching account in the db.
 * If site API is down, use db to log in.
 */
@Component
@Slf4j
public class ApiSupportedByDbLogin extends LoginProviderBase {

	@Value("${etterna.login.api-url}")
	private String apiUrl;

	@Value("${etterna.login.api-key}")
	private String apiKey;

	private static final int CONNECTION_TIMEOUT = 10 * 1000; // 10 secs

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

	private static RestTemplate restTemplate() {
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectionRequestTimeout(CONNECTION_TIMEOUT);
		httpRequestFactory.setConnectTimeout(CONNECTION_TIMEOUT);
		httpRequestFactory.setReadTimeout(CONNECTION_TIMEOUT);

		return new RestTemplate(httpRequestFactory);
	}

	/**
	 * Send a POST containing LoginRequest JSON and expecting LoginResponse JSON
	 */
	private boolean apiLogin(String username, String password) throws Throwable {
		try {
			RestTemplate restCall = restTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			headers.setAccept(Arrays.asList(new MediaType[] {MediaType.APPLICATION_JSON}));
			MultiValueMap<String, String> reqBody = new LinkedMultiValueMap<String, String>();
			reqBody.add("username", username);
			reqBody.add("password", password);
			reqBody.add("clientData", apiKey);
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(reqBody, headers);
			ResponseEntity<LoginResponse> response = null;
			try {
				response = restCall.postForEntity(apiUrl, request, LoginResponse.class);
			} catch (HttpClientErrorException ee) {
				if (ee.getStatusCode() == HttpStatus.NOT_FOUND && ee.getResponseBodyAsString() != null && ee.getResponseBodyAsString().contains("errors")) {
					// site returns a "404" with errors when username is not found or password is wrong
					m_logger.info(ee.getMessage());
					return false;
				}
				throw ee;
			}
			LoginResponse body = response.getBody();
			return body != null && body.getData() != null && body.getData().getAttributes() != null && body.getData().getAttributes().getAccessToken() != null;
		} catch (Throwable e) {
			m_logger.warn("Exception while making API call");
			m_logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Getter @Setter
	public class LoginRequest {
		private String username;
		private String password;
		private String clientData = apiKey;
		private LoginRequest(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

	@Getter @Setter
	public static class LoginResponse {
		private Object[] errors;
		public Data data;
		@Getter @Setter
		public static class Data {
			private Token attributes;
			@Getter @Setter
			public static class Token {
				private String accessToken;
			}
		}
	}

	@Override
	@Transactional
	public boolean login(String username, String password) {
		try {
			final boolean validCredentials = apiLogin(username, password);
			
			if (!validCredentials) {
				m_logger.info("Failed user login (bad password to API): {}", username.toLowerCase());
				return false;
			}
		} catch (Throwable e) {
			// connection issue, go off of db completely
			return dbLogin(username, password, false);
		}
		
		return dbLogin(username, password, true);
	}

	private boolean dbLogin(String username, String password, boolean replaceExisting) {
		final UserLogin user = get(username);
		if (user == null) {
			// must create new user

			if (!replaceExisting) {
				m_logger.info("Failed user login (user does not exist): {}", username.toLowerCase());
				return false;
			}

			final boolean o = newUser(username, password);
			if (o) {
				m_logger.info("Logged in user: {}", username.toLowerCase());
			} else {
				m_logger.info("Failed user login (user already exists): {}", username.toLowerCase());
			}
			return o;
		} else {
			// if user exists, verify that the currently saved password is correct
			// if not, replace it

			final String salt = user.getSalt();
			final String saltedPass = PasswordUtil.hashPassword(password, salt);
			final String storedPass = user.getPassword();

			final boolean eq = storedPass.equals(saltedPass);

			if (eq) {
				m_logger.info("Logged in user: {}", username.toLowerCase());
				return true;
			} else if (!eq && replaceExisting) {
				user.setPassword(saltedPass);
				repo.save(user);
				m_logger.info("Logged in user (replaced db credentials): {}", username.toLowerCase());
				return true;
			} else {
				m_logger.info("Failed user login (bad password): {}", username.toLowerCase());
				return false;
			}
		}
	}

}
