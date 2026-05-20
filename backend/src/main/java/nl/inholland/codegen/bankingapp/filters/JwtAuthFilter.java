package nl.inholland.codegen.bankingapp.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final String AUTH_HEADER_KEY = "Authorization";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
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
                System.out.println("So we got to this point where the user might have auth headers");
                String email = jwtUtil.extractEmail(authHeaderValue);
                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException | AuthenticationException ignored) {
                // Invalid/expired token: proceed without authentication
            }
        }
	}
}
