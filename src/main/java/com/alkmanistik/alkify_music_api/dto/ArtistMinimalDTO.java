package com.alkmanistik.alkify_music_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistMinimalDTO {
    private Long id;
    private String artistName;
    private String imageUrl;
}
