package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {

	@Autowired
	private RedisOperationsSessionRepository sessionRepository;

	@Bean
	@SuppressWarnings("unchecked")
	public SpringSessionBackedSessionRegistry sessionRegistry1() {
		RedisOperationsSessionRepository redisOperationsSessionRepository = (RedisOperationsSessionRepository) this.sessionRepository;
		redisOperationsSessionRepository.setDefaultMaxInactiveInterval(300);
		return new SpringSessionBackedSessionRegistry(redisOperationsSessionRepository);
	}

	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("JSESSIONID");
		serializer.setCookiePath("/");
		serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); // <3>
		return serializer;
	}

	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
	}

	@Bean
	public ConcurrentSessionFilter sessionFilter() {
		return new ConcurrentSessionFilter(sessionRegistry1());
	}

	@Bean
	public ConcurrentSessionControlAuthenticationStrategy authenticationStrategy() {
		return new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry1());
	}

	@Bean
	public ConcurrentLoginFilter loginFilter() {
		return new ConcurrentLoginFilter();
	}

}
