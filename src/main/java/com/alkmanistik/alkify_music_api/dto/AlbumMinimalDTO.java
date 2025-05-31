package com.alkmanistik.alkify_music_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumMinimalDTO {
    private Long id;
    private String title;
    private int releaseDate;
    private String imageUrl;
    private int trackCount;
}
