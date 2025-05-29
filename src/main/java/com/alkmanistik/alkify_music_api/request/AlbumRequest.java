package com.alkmanistik.alkify_music_api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    private String title;

    @Size(max = 1000)
    private String description;
    private List<TrackRequest> tracks;
}
