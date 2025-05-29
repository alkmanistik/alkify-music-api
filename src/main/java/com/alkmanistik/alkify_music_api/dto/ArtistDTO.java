package com.alkmanistik.alkify_music_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDTO {
    private Long id;
    private String artistName;
    private String imageUrl;
    private String description;
    private int subscriberCount;
    private List<AlbumMinimalDTO> albums;
    private List<TrackMinimalDTO> tracks;
}