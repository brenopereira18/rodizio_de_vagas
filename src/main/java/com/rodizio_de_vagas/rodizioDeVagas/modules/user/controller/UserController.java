package com.rodizio_de_vagas.rodizioDeVagas.modules.user.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.ResponseManagerDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> createUser(@Valid @RequestBody RequestCreateUserDTO userDTO) {
        UserEntity user = this.userService.createUser(userDTO);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/{registration}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String registration) {
        UserEntity user = this.userService.getUser(registration);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping
    public ResponseEntity<List<ResponseManagerDTO>> getAllManagers() {
        List<ResponseManagerDTO> managers = this.userService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    @PutMapping("/{registration}/profile")
    public ResponseEntity<UserEntity> updateUser(@PathVariable String registration,@Valid  @RequestBody RequestUpdateUserDTO dto) {
        UserEntity user = this.userService.updateUser(registration, dto);
        return ResponseEntity.ok().body(user);
    }

    @DeleteMapping("/{registration}")
    public ResponseEntity<Void> deleteUser(@PathVariable String registration) {
        this.userService.deleteUser(registration);
        return ResponseEntity.noContent().build();
    }
}
