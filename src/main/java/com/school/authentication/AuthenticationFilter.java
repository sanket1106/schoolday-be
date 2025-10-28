package com.school.authentication;

import com.school.exceptions.AuthorizationException;
import com.school.feature.users.dao.IUserRepository;
import com.school.feature.users.dao.IUserSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends GenericFilterBean {

    private final IUserSessionRepository userSessionRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        final var path = request.getRequestURI();
        if (path.contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final var token = authHeader.replace("Bearer ", "");

        final var userSession = userSessionRepository.findById(token).orElse(null);

        if (userSession == null || !userSession.isActive()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final var httpSession = request.getSession();
        httpSession.setAttribute("userSession", userSession);
        filterChain.doFilter(request, response);
    }
}
