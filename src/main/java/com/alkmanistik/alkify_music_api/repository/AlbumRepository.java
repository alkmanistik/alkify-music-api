package com.alkmanistik.alkify_music_api.repository;

import com.alkmanistik.alkify_music_api.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findByArtistsId(Long artistId);

    List<Album> findByTitleContainingIgnoreCase(String title);
}
