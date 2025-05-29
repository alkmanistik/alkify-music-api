package com.alkmanistik.alkify_music_api.repository;

import com.alkmanistik.alkify_music_api.model.Artist;
import com.alkmanistik.alkify_music_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByArtistNameContainingIgnoreCase(String name);

    List<Artist> findByUser(User user);

    List<Artist> findByUserId(Long userId);

    void deleteAllByUser(User user);

    void deleteAllByUserId(Long userId);

    int countSubscribersById(Long artistId);

    boolean existsByIdAndSubscribersId(Long artistId, Long userId);
}
