package com.alkmanistik.alkify_music_api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackRequest {
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    private String genre;
    @NotNull
    private boolean isExplicit;
}
