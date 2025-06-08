package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.TrackDTO;
import com.alkmanistik.alkify_music_api.exception.ForbiddenException;
import com.alkmanistik.alkify_music_api.mapper.GlobalMapper;
import com.alkmanistik.alkify_music_api.model.*;
import com.alkmanistik.alkify_music_api.repository.AlbumRepository;
import com.alkmanistik.alkify_music_api.repository.ArtistRepository;
import com.alkmanistik.alkify_music_api.repository.TrackRepository;
import com.alkmanistik.alkify_music_api.repository.UserRepository;
import com.alkmanistik.alkify_music_api.request.TrackRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final FileService fileService;
    private final GlobalMapper globalMapper;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;

    @Value("${project.audios}")
    private String audioPath;

    @Cacheable(value = "tracks.all", sync = true)
    public List<TrackDTO> getAllTracks() {
        return trackRepository.findAll().stream()
                .map(globalMapper::toTrackDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "track.byId", key = "#id", sync = true)
    public TrackDTO getById(Long id) {
        return trackRepository.findById(id)
                .map(globalMapper::toTrackDTO)
                .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + id));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "track.byId", key = "#result.id", condition = "#result != null"),
            @CacheEvict(value = "tracks.byAlbum", key = "#albumId"),
            @CacheEvict(value = "tracks.search", allEntries = true),
            @CacheEvict(value = "tracks.liked", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", key = "#albumId"),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", allEntries = true),
            @CacheEvict(value = "artists.byUserId", key = "#user.id"),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public TrackDTO createTrack(Long albumId, User user, TrackRequest trackRequest, MultipartFile file) throws IOException, ForbiddenException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));
        Artist artist = artistRepository.findById(album.getArtists().getFirst().getId())
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + album.getArtists().getFirst().getId()));

        checkArtistOwnership(artist, user);

        Track track = new Track();
        track.setTitle(trackRequest.getTitle());
        track.setGenre(trackRequest.getGenre());
        track.setExplicit(trackRequest.isExplicit());
        if (file != null && !file.isEmpty()) {
            String fileName = fileService.uploadFile(audioPath, file);
            track.setAudioFilePath(fileName);
        }
        track.setAlbum(album);
        track.setArtists(List.of(artist));
        var savedTrack = trackRepository.save(track);
        log.info("Saved track: {}", savedTrack);
        return globalMapper.toTrackDTO(savedTrack);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.byAlbum", allEntries = true),
            @CacheEvict(value = "tracks.search", allEntries = true),
            @CacheEvict(value = "tracks.liked", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", key = "#result.album.id"),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", allEntries = true),
            @CacheEvict(value = "artists.byUserId", key = "#user.id"),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public TrackDTO updateTrack(Long trackId, User user, TrackRequest trackRequest, MultipartFile file) throws IOException, ForbiddenException {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + trackId));

        checkTrackOwnership(track, user);

        if (trackRequest.getTitle() != null) {
            track.setTitle(trackRequest.getTitle());
        }
        if (trackRequest.getGenre() != null) {
            track.setGenre(trackRequest.getGenre());
        }
        track.setExplicit(trackRequest.isExplicit());
        if (file != null && !file.isEmpty()) {
            String fileName = fileService.uploadFile(audioPath, file);
            track.setAudioFilePath(fileName);
        }
        Track updatedTrack = trackRepository.save(track);
        log.info("Updated track: {}", updatedTrack);
        return globalMapper.toTrackDTO(updatedTrack);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.byAlbum", allEntries = true),
            @CacheEvict(value = "tracks.search", allEntries = true),
            @CacheEvict(value = "tracks.liked", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", allEntries = true),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", allEntries = true),
            @CacheEvict(value = "artists.byUserId", allEntries = true),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public void deleteTrack(Long trackId, User user) throws ForbiddenException {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        checkTrackOwnership(track, user);

        if (track.getAudioFilePath() != null) {
            fileService.deleteFile(audioPath, track.getAudioFilePath());
        }

        trackRepository.delete(track);
        log.info("Deleted track with id: {}", trackId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.byAlbum", allEntries = true),
            @CacheEvict(value = "tracks.search", allEntries = true),
            @CacheEvict(value = "tracks.liked", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", allEntries = true),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", allEntries = true),
            @CacheEvict(value = "artists.byUserId", allEntries = true),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", allEntries = true),
            @CacheEvict(value = "user.byEmail", allEntries = true)
    })
    public void delete(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        if (track.getAudioFilePath() != null) {
            fileService.deleteFile(audioPath, track.getAudioFilePath());
        }

        trackRepository.delete(track);
        log.info("Deleted track with id: {}", trackId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "track.byId", allEntries = true),
            @CacheEvict(value = "tracks.byAlbum", key = "#albumId"),
            @CacheEvict(value = "tracks.search", allEntries = true),
            @CacheEvict(value = "tracks.liked", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", allEntries = true),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", allEntries = true),
            @CacheEvict(value = "artists.byUserId", allEntries = true),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", allEntries = true),
            @CacheEvict(value = "user.byEmail", allEntries = true)
    })
    public void deleteTracksByAlbum(Long albumId) {
        trackRepository.findAllByAlbumId(albumId).forEach(track ->
                delete(track.getId())
        );
    }

    @Cacheable(value = "tracks.byAlbum", key = "#albumId", sync = true)
    public List<TrackDTO> getTracksByAlbumId(Long albumId) {
        return trackRepository.findAllByAlbumId(albumId).stream()
                .map(globalMapper::toTrackDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "tracks.search", key = "#title", sync = true)
    public List<TrackDTO> searchTracks(String title) {
        if (title == null || title.isBlank()) {
            return Collections.emptyList();
        }
        return trackRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(globalMapper::toTrackDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "tracks.byAlbum", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", key = "#result.album.id"),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#artistId"),
            @CacheEvict(value = "artists.byUserId", key = "#user.id"),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public TrackDTO addArtistToTrack(User user, Long trackId, Long artistId) throws ForbiddenException {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        checkTrackOwnership(track, user);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (track.getArtists().contains(artist)) {
            throw new IllegalArgumentException("Artist already exists in this track");
        }

        track.getArtists().add(artist);
        Track updatedTrack = trackRepository.save(track);
        return globalMapper.toTrackDTO(updatedTrack);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "tracks.byAlbum", allEntries = true),
            @CacheEvict(value = "albums.all", allEntries = true),
            @CacheEvict(value = "albums.byArtist", allEntries = true),
            @CacheEvict(value = "album.byId", key = "#result.album.id"),
            @CacheEvict(value = "albums.search", allEntries = true),
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#artistId"),
            @CacheEvict(value = "artists.byUserId", key = "#user.id"),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public void removeArtistFromTrack(User user, Long trackId, Long artistId) throws ForbiddenException {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        checkTrackOwnership(track, user);

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (!track.getArtists().contains(artist)) {
            throw new IllegalArgumentException("Artist not found in this track");
        }
        if (Objects.equals(track.getArtists().getFirst().getId(), artist.getId())) {
            throw new IllegalArgumentException("Artist, who created the track, can't be remove!");
        }

        track.getArtists().remove(artist);
        trackRepository.save(track);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.liked", key = "#user.id"),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public void likeTrack(Long trackId, User user) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        if (!track.getLikedUsers().contains(user)) {
            track.getLikedUsers().add(user);
            trackRepository.save(track);
            log.info("User {} liked track {}", user.getId(), trackId);
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.liked", key = "#user.id"),
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#user.id"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")
    })
    public void unlikeTrack(Long trackId, User user) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        if (track.getLikedUsers().contains(user)) {
            track.getLikedUsers().remove(user);
            user.getLikedTracks().remove(track);
            trackRepository.save(track);
            userRepository.save(user);
            log.info("User {} unliked track {}", user.getId(), trackId);
        }
    }

    @Cacheable(value = "track.likedStatus", key = "{#trackId, #userId}")
    public boolean isTrackLikedByUser(Long trackId, Long userId) {
        return trackRepository.existsByIdAndLikedUsersId(trackId, userId);
    }

    @Cacheable(value = "tracks.liked", key = "#user.id", sync = true)
    public List<TrackDTO> getLikedTracks(User user) {
        return trackRepository.findByLikedUsersId(user.getId()).stream()
                .map(globalMapper::toTrackDTO)
                .collect(Collectors.toList());
    }

    private void checkArtistOwnership(Artist artist, User user) throws ForbiddenException {
        if (!artist.getUser().getId().equals(user.getId())
                && !user.getRoles().contains(Role.ADMIN)) {
            throw new ForbiddenException("No permission to modify this artist");
        }
    }

    private void checkTrackOwnership(Track track, User user) throws ForbiddenException {
        checkArtistOwnership(track.getArtists().getFirst(), user);
    }

}