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

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUser(@PathVariable Long id) {
        UserEntity user = this.userService.getUser(id);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping
    public ResponseEntity<List<ResponseManagerDTO>> getAllManagers() {
        List<ResponseManagerDTO> managers = this.userService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserEntity> updateUser(@PathVariable Long id,@Valid  @RequestBody RequestUpdateUserDTO dto) {
        UserEntity user = this.userService.updateUser(id, dto);
        return ResponseEntity.ok().body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
