package com.alkmanistik.alkify_music_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    @JsonIgnore
    private LocalDateTime releaseDate;
    private List<ArtistMinimalDTO> artists;
    private List<TrackMinimalDTO> tracks;
}
