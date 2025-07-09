package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationUserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String registration) throws ResourceNotFoundException {
        return userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
