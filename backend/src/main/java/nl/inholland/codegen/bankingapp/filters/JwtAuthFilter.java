package nl.inholland.codegen.bankingapp.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final String AUTH_HEADER_KEY = "Authorization";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

	@Override
	protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeaderValue = request.getHeader(AUTH_HEADER_KEY);

        // we return early, and skip the logic which authenticates the user completely
        if (authHeaderValue == null || !authHeaderValue.startsWith(JwtUtil.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extractUsername(authHeaderValue);
                Optional<User> user;

                // TODO: set authentication state, depends on User Repository
                // so for now, this code will not actually authenticate anything
                // which is fine, we only need the stubs for now

            } catch (JwtException | IllegalArgumentException ignored) {
                // Invalid/expired token: proceed without authentication
            }
        }
	}
}
