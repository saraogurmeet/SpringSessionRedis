package com.example.demo.config;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/**
 * Spring Security configuration.
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	ConcurrentSessionControlAuthenticationStrategy authenticationStrategy;

	@Autowired
	SessionRegistry sessionRegistry;

	@Autowired
	ConcurrentLoginFilter loginFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionAuthenticationStrategy(authenticationStrategy);
		http.authorizeRequests().antMatchers("/login").permitAll().and().authorizeRequests().antMatchers("/login.jsp")
				.permitAll().and().formLogin().loginPage("/login").defaultSuccessUrl("/index")
				.failureUrl("/login?error=true").permitAll().and().logout().logoutSuccessUrl("/login?logout=true")
				.invalidateHttpSession(true).deleteCookies("JESSIONID").permitAll().and().authorizeRequests()
				.antMatchers("/index").authenticated().and().csrf().disable();
		http.sessionManagement().invalidSessionUrl("/login").maximumSessions(1).maxSessionsPreventsLogin(true)
				.sessionRegistry(sessionRegistry).expiredSessionStrategy(new SessionInformationExpiredStrategy() {
					@Override
					public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
							throws IOException, ServletException {
						SessionInformation information = event.getSessionInformation();
						information.expireNow();
						sessionRegistry.removeSessionInformation(information.getSessionId());
					}
				});
		http.userDetailsService(userDetailsService());
		http.addFilterAfter(loginFilter, ConcurrentSessionFilter.class);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		UserDetails[] users = new UserDetails[20];
		for (int i = 0; i < 20; i++) {
			users[i] = User.withUsername("user" + (i + 1))
					.password("$2a$10$ABM/pZQgjvpHoO9nCz0SCub0i1V4WIUcRg3jjJ4mu4EH2yOcJpUra").authorities("ROLE_ADMIN").build();
		}

		InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> inMemoryUserDetailsManagerConfigurer = auth
				.inMemoryAuthentication();
		for (UserDetails userDetails : users) {
			inMemoryUserDetailsManagerConfigurer.withUser(userDetails);
		}
		inMemoryUserDetailsManagerConfigurer.passwordEncoder(passwordEncoder());
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
