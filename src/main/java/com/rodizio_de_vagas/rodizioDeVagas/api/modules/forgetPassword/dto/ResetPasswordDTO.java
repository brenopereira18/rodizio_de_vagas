package com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordDTO {

    @NotBlank(message = "Token é obrigatório.")
    private String token;

    @NotBlank(message = "Nova senha é obrigatória.")
    private String newPassword;

    @NotBlank(message = "Confirmação de senha é obrigatória.")
    private String confirmPassword;

    public boolean passwordsMatch() {
        // Garante que ambos os campos não são nulos antes de comparar
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
