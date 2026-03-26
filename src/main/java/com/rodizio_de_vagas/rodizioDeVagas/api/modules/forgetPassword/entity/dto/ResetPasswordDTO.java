package com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
    @NotBlank(message = "Token é obrigatório.")
    String token,

    @NotBlank(message = "Nova senha é obrigatória.")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres.")
    String newPassword,

    @NotBlank(message = "Confirmação de senha é obrigatória.")
    String confirmPassword

) {
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
