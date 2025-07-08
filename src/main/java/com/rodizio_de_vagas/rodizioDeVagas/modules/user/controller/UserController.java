package com.rodizio_de_vagas.rodizioDeVagas.modules.user.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.ResponseManagerDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.service.UserService;
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
    public ResponseEntity<List<ResponseManagerDTO>> getAllManagers() {
        List<ResponseManagerDTO> managers = this.userService.getAllManagers();
        return ResponseEntity.ok(managers);
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
