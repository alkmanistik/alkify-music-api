package com.alkmanistik.alkify_music_api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistRequest {

    @Size(min = 3, max = 50, message = "Artist name must be between 3 and 50 chars")
    @NotBlank(message = "Artist name is mandatory")
    @JsonProperty("artistName")
    private String artistName;
    @Size(max = 1000, message = "Description too long")
    @JsonProperty("description")
    private String description;
    private List<AlbumRequest> albums;

}
