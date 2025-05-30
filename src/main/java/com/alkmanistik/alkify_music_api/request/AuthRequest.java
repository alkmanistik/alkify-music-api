package com.alkmanistik.alkify_music_api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    @Email
    @NotBlank(message = "Email is mandatory")
    private String email;
    @Size(min = 8, message = "Min 8 chars for password")
    @NotBlank(message = "Password is mandatory")
    private String password;

}
