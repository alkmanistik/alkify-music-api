package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.TrackDTO;
import com.alkmanistik.alkify_music_api.mapper.GlobalMapper;
import com.alkmanistik.alkify_music_api.model.Album;
import com.alkmanistik.alkify_music_api.model.Artist;
import com.alkmanistik.alkify_music_api.model.Track;
import com.alkmanistik.alkify_music_api.model.User;
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
                .toList();
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
            @CacheEvict(value = "tracks.liked", allEntries = true)
    })
    public TrackDTO createTrack(Long albumId, TrackRequest trackRequest, MultipartFile file) throws IOException {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + albumId));
        Artist artist = artistRepository.findById(album.getArtists().getFirst().getId())
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + album.getArtists().getFirst().getId()));
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
            @CacheEvict(value = "tracks.liked", allEntries = true)
    })
    public TrackDTO updateTrack(Long trackId, TrackRequest trackRequest, MultipartFile file) throws IOException {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + trackId));
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
            @CacheEvict(value = "tracks.liked", allEntries = true)
    })
    public void deleteTrack(Long trackId) {
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
            @CacheEvict(value = "tracks.liked", allEntries = true)
    })
    public void deleteTracksByAlbum(Long albumId) {
        trackRepository.findAllByAlbumId(albumId).forEach(track ->
                deleteTrack(track.getId())
        );
    }

    @Cacheable(value = "tracks.byAlbum", key = "#albumId", sync = true)
    public List<TrackDTO> getTracksByAlbumId(Long albumId) {
        return trackRepository.findAllByAlbumId(albumId).stream()
                .map(globalMapper::toTrackDTO)
                .toList();
    }

    @Cacheable(value = "tracks.search", key = "#title", sync = true)
    public List<TrackDTO> searchTracks(String title) {
        if (title == null || title.isBlank()) {
            return Collections.emptyList();
        }
        return trackRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(globalMapper::toTrackDTO)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.all", allEntries = true),
            @CacheEvict(value = "tracks.byAlbum", allEntries = true)
    })
    public TrackDTO addArtistToTrack(Long trackId, Long artistId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

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
            @CacheEvict(value = "tracks.byAlbum", allEntries = true)
    })
    public void removeArtistFromTrack(Long trackId, Long artistId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (!track.getArtists().contains(artist)) {
            throw new IllegalArgumentException("Artist not found in this track");
        }
        if (Objects.equals(track.getArtists().getFirst().getId(), artist.getId())) {
            throw new IllegalArgumentException("Artist, who created the track, can't be removed");
        }

        track.getArtists().remove(artist);
        trackRepository.save(track);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.liked", key = "#userId")
    })
    public void likeTrack(Long trackId, Long userId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!track.getLikedUsers().contains(user)) {
            track.getLikedUsers().add(user);
            trackRepository.save(track);
            log.info("User {} liked track {}", userId, trackId);
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "track.byId", key = "#trackId"),
            @CacheEvict(value = "tracks.liked", key = "#userId")
    })
    public void unlikeTrack(Long trackId, Long userId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (track.getLikedUsers().contains(user)) {
            track.getLikedUsers().remove(user);
            user.getLikedTracks().remove(track);
            trackRepository.save(track);
            userRepository.save(user);
            log.info("User {} unliked track {}", userId, trackId);
        }
    }

    @Cacheable(value = "track.likedStatus", key = "{#trackId, #userId}")
    public boolean isTrackLikedByUser(Long trackId, Long userId) {
        return trackRepository.existsByIdAndLikedUsersId(trackId, userId);
    }

    @Cacheable(value = "tracks.liked", key = "#userId", sync = true)
    public List<TrackDTO> getLikedTracks(Long userId) {
        return trackRepository.findByLikedUsersId(userId).stream()
                .map(globalMapper::toTrackDTO)
                .toList();
    }
}