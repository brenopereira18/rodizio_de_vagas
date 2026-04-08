package com.rodizio_de_vagas.rodizioDeVagas.api.security;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = recoverToken(request);

        if (token != null) {
            try {
                var login = tokenProvider.validationToken(token);
                if (login != null) {
                    userRepository.findByRegistration(login)
                        .ifPresent(user -> {

                            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                                var authentication = new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                                );
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        });
                }
            } catch (Exception ex) {
                logger.warn("Token inválido: {}", ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/servico_extraordinario") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/images/") ||
            path.equals("/favicon.ico");
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
