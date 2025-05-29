package com.alkmanistik.alkify_music_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackMinimalDTO {
    private Long id;
    private String title;
    private int durationSeconds;
    private String audioUrl;
    private boolean isExplicit;
}
