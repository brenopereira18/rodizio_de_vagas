package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/register")
    public ResponseEntity<UserEntity> createUser(@Valid @RequestBody RequestCreateUserDTO userDTO) {
        UserEntity user = this.userService.createUser(userDTO);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserEntity> getUser(Principal principal) {
        String registration = principal.getName();
        UserEntity user = this.userService.getUser(registration);
        return ResponseEntity.ok().body(user);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/managers")
    public ResponseEntity<List<ResponseUserDTO>> getAllManagers() {
        List<ResponseUserDTO> managers = this.userService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/tax")
    public ResponseEntity<List<ResponseUserDTO>> getAllTax() {
        List<ResponseUserDTO> tax = this.userService.getAllTax();
        return ResponseEntity.ok(tax);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserEntity> updateUser(Principal principal,@Valid  @RequestBody RequestUpdateUserDTO dto) {
        String registration = principal.getName();
        UserEntity user = this.userService.updateUser(registration, dto);
        return ResponseEntity.ok().body(user);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam String registration) {
        this.userService.deleteUser(registration);
        return ResponseEntity.noContent().build();
    }
}
