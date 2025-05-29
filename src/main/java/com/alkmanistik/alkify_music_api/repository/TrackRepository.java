package com.alkmanistik.alkify_music_api.repository;

import com.alkmanistik.alkify_music_api.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findAllByAlbumId(Long albumId);

    List<Track> findByTitleContainingIgnoreCase(String title);

    boolean existsByIdAndLikedUsersId(Long trackId, Long userId);

    List<Track> findByLikedUsersId(Long userId);
}
