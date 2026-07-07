package com.fts.tenantbasededuportal.security;

import com.fts.tenantbasededuportal.exception.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {

            final String userId = this.jwtService.extractUserId(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                final UserPrincipal principal = this.customUserDetailsService.loadUserById(userId);

                if (this.jwtService.validateToken(token, principal)) {

                    final UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(principal,
                                    null,
                                    principal.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);
        }
        catch (final JwtException | IllegalArgumentException exception){

            final ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(Instant.now())
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .message("Invalid or expired JWT token.")
                    .path(request.getRequestURI())
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            this.objectMapper.writeValue(response.getWriter(), errorResponse);

            return;
        }
    }
}

