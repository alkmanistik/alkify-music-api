package com.alkmanistik.alkify_music_api.mapper;

import com.alkmanistik.alkify_music_api.dto.*;
import com.alkmanistik.alkify_music_api.model.Album;
import com.alkmanistik.alkify_music_api.model.Artist;
import com.alkmanistik.alkify_music_api.model.Track;
import com.alkmanistik.alkify_music_api.model.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GlobalMapper {

    private <T, R> List<R> safeMap(Collection<T> collection, Function<T, R> mapper) {
        return Optional.ofNullable(collection)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    private int safeSize(Collection<?> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::size)
                .orElse(0);
    }

    public UserDTO toUserDTO(User user) {
        if (user == null) return null;

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setManagedArtists(safeMap(user.getManagedArtists(), this::toArtistDTO));
        return userDTO;
    }

    public ArtistDTO toArtistDTO(Artist artist) {
        if (artist == null) return null;

        ArtistDTO artistDTO = new ArtistDTO();
        artistDTO.setId(artist.getId());
        artistDTO.setArtistName(artist.getArtistName());
        artistDTO.setImageUrl(artist.getImageFilePath());
        artistDTO.setDescription(artist.getDescription());
        artistDTO.setSubscriberCount(safeSize(artist.getSubscribers()));
        artistDTO.setAlbums(safeMap(artist.getAlbums(), this::toAlbumMinimalDTO));
        artistDTO.setTracks(safeMap(artist.getTracks(), this::toTrackMinimalDTO));
        return artistDTO;
    }

    public ArtistMinimalDTO toArtistMinimalDTO(Artist artist) {
        if (artist == null) return null;

        ArtistMinimalDTO dto = new ArtistMinimalDTO();
        dto.setId(artist.getId());
        dto.setArtistName(artist.getArtistName());
        dto.setImageUrl(artist.getImageFilePath());
        return dto;
    }

    public AlbumDTO toAlbumDTO(Album album) {
        if (album == null) return null;

        AlbumDTO dto = new AlbumDTO();
        dto.setId(album.getId());
        dto.setTitle(album.getTitle());
        dto.setDescription(album.getDescription());
        dto.setImageUrl(album.getImageFilePath());
        dto.setReleaseDate(album.getCreatedAt());
        dto.setArtists(safeMap(album.getArtists(), this::toArtistMinimalDTO));
        dto.setTracks(safeMap(album.getTracks(), this::toTrackMinimalDTO));
        return dto;
    }

    public AlbumMinimalDTO toAlbumMinimalDTO(Album album) {
        if (album == null) return null;

        AlbumMinimalDTO dto = new AlbumMinimalDTO();
        dto.setId(album.getId());
        dto.setTitle(album.getTitle());
        dto.setReleaseDate(album.getCreatedAt());
        dto.setImageUrl(album.getImageFilePath());
        dto.setTrackCount(safeSize(album.getTracks()));
        return dto;
    }

    public TrackDTO toTrackDTO(Track track) {
        if (track == null) return null;

        TrackDTO dto = new TrackDTO();
        dto.setId(track.getId());
        dto.setTitle(track.getTitle());
        dto.setGenre(track.getGenre());
        dto.setDurationSeconds(track.getDurationSeconds());
        dto.setAudioUrl(track.getAudioFilePath());
        dto.setReleaseDate(track.getReleaseDate());
        dto.setArtists(safeMap(track.getArtists(), this::toArtistMinimalDTO));
        dto.setAlbum(toAlbumMinimalDTO(track.getAlbum()));
        dto.setExplicit(track.isExplicit());
        dto.setLikeCount(safeSize(track.getLikedUsers()));
        return dto;
    }

    public TrackMinimalDTO toTrackMinimalDTO(Track track) {
        if (track == null) return null;

        TrackMinimalDTO dto = new TrackMinimalDTO();
        dto.setId(track.getId());
        dto.setTitle(track.getTitle());
        dto.setDurationSeconds(track.getDurationSeconds());
        dto.setAudioUrl(track.getAudioFilePath());
        dto.setExplicit(track.isExplicit());
        return dto;
    }
}