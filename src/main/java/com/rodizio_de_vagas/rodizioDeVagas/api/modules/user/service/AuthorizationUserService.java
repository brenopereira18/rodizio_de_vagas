package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String registration) throws UsernameNotFoundException {
        return userRepository.findByRegistration(registration)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
