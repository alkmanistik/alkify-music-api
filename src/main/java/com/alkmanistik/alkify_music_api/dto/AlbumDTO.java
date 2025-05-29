package com.alkmanistik.alkify_music_api.dto;

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
    private LocalDateTime releaseDate;
    private List<ArtistMinimalDTO> artists;
    private List<TrackMinimalDTO> tracks;
}
