package com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationUser implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String registration) throws ResourceNotFoundException {
        return userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
