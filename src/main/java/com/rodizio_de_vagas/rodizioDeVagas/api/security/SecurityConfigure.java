package com.rodizio_de_vagas.rodizioDeVagas.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfigure {

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(Customizer.withDefaults())
                .xssProtection(Customizer.withDefaults())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/gerenciador_de_servico/fiscal/login",
                    "/gerenciador_de_servico/recuperar-senha",
                    "/gerenciador_de_servico/resetar-senha/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .anyRequest().authenticated()
            ).formLogin(form -> form
                .loginPage("/gerenciador_de_servico/fiscal/login")
                .loginProcessingUrl("/gerenciador_de_servico/fiscal/login")
                .usernameParameter("matricula")
                .passwordParameter("senha")
                .defaultSuccessUrl("/gerenciador_de_servico/servicos", true)
                .failureUrl("/gerenciador_de_servico/fiscal/login?error=true")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
            )
            //.logout(logout -> logout
              //  .logoutUrl("/gerenciador_de_servico/logout")
              //  .logoutSuccessUrl("/gerenciador_de_servico/fiscal/login?logout=true")
              //  .invalidateHttpSession(true)
              //  .deleteCookies("JSESSIONID")
            //)
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
