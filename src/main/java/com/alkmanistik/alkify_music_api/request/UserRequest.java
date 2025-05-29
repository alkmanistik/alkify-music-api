package com.alkmanistik.alkify_music_api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @Size(min = 3, message = "Min 3 chars for username")
    @NotBlank(message = "Name is mandatory")
    private String username;
    @Email
    @NotBlank(message = "Email is mandatory")
    private String email;
    @Size(min = 8, message = "Min 8 chars for password")
    @NotBlank(message = "Password is mandatory")
    private String password;
    private List<ArtistRequest> managedArtists;

}
