package com.example.demo.config;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class ConcurrentLoginFilter extends GenericFilterBean {

	@Autowired
	SessionRegistry sessionRegistry;
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		HttpSession session = request.getSession(false);

		if (session != null) {
			System.out.println("session id:  "+session.getId());
			SessionInformation info = sessionRegistry.getSessionInformation(session.getId());

			if (info != null) {
				if (info.isExpired()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Requested session ID " + request.getRequestedSessionId() + " has expired.");
					}
					return;
				} else {
					// Non-expired - update last request date/time
					sessionRegistry.refreshLastRequest(info.getSessionId());
					Authentication auth = SecurityContextHolder.getContext().getAuthentication();
					if (null != auth) {
						System.out.println("auth.isAuthenticated(): " + auth.isAuthenticated());
						System.out.println(auth);

						if (!(auth instanceof AnonymousAuthenticationToken)) {

							User user = (User) auth.getPrincipal();
							System.out.println(auth.getPrincipal() + " " + info.getSessionId());
							List<SessionInformation> list = sessionRegistry.getAllSessions(user, false);
							System.out.println("sessionregistry size: " + list.size());
							if (list.size() > 1) {
								info.expireNow();
								sessionRegistry.removeSessionInformation(session.getId());
							}
						}
					}
				}
			}
		}

		chain.doFilter(request, response);
	}

}
