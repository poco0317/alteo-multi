package com.etterna.multi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.etterna.multi.socket.SocketTextHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	
	private static final Logger m_logger = LoggerFactory.getLogger(WebSocketConfig.class);
	
	@Autowired
	private ApplicationContext ctx;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		m_logger.info("Registering WebSocket Handlers");
		
		registry.addHandler(ctx.getBean(SocketTextHandler.class), "/");
		
		m_logger.info("Registered WebSocket Handlers");
	}
	
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(8192 * 8192);
		container.setAsyncSendTimeout(1000L * 10L);
		container.setMaxSessionIdleTimeout(1000L * 45L);
		return container;
	}
	
	
	
}
