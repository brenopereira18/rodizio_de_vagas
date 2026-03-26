package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/register")
    public ResponseEntity<ResponseUserDTO> createUser(@Valid @RequestBody RequestCreateUserDTO userDTO) {
        ResponseUserDTO user = this.userService.createUser(userDTO);
        return ResponseEntity.status(201).body(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseUserDTO> getUser(Principal principal) {
        ResponseUserDTO user = this.userService.getUser(principal.getName());
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/managers")
    public ResponseEntity<List<ResponseUserDTO>> getAllManagers() {
        return ResponseEntity.ok(this.userService.getAllManagers());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/tax")
    public ResponseEntity<List<ResponseUserDTO>> getAllTax() {
        return ResponseEntity.ok(this.userService.getAllTax());
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseUserDTO> updateUser(Principal principal,@Valid  @RequestBody RequestUpdateUserDTO dto) {
        ResponseUserDTO user = this.userService.updateUser(principal.getName(), dto);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam String registration) {
        this.userService.deleteUser(registration);
        return ResponseEntity.noContent().build();
    }
}
