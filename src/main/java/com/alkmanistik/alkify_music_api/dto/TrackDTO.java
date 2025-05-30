package com.alkmanistik.alkify_music_api.dto;

import com.alkmanistik.alkify_music_api.model.Track;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDTO {
    private Long id;
    private String title;
    private String genre;
    private int durationSeconds;
    private String audioUrl;
    @JsonIgnore
    private LocalDateTime releaseDate;
    private List<ArtistMinimalDTO> artists;
    private AlbumMinimalDTO album;
    private boolean isExplicit;
    private int likeCount;
}