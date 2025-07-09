package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestAuthenticationDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseLoginDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.provider.TokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid RequestAuthenticationDTO authenticationDTO) {
        var registrationPassword = new UsernamePasswordAuthenticationToken(authenticationDTO.register(), authenticationDTO.password());
        var auth = this.authenticationManager.authenticate(registrationPassword);

        var token = this.tokenProvider.generateToken((UserEntity) auth.getPrincipal());
        return ResponseEntity.ok(new ResponseLoginDTO(token));
    }
}
