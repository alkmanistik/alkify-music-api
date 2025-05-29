package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.ArtistDTO;
import com.alkmanistik.alkify_music_api.dto.UserDTO;
import com.alkmanistik.alkify_music_api.mapper.GlobalMapper;
import com.alkmanistik.alkify_music_api.model.Artist;
import com.alkmanistik.alkify_music_api.model.User;
import com.alkmanistik.alkify_music_api.repository.ArtistRepository;
import com.alkmanistik.alkify_music_api.repository.UserRepository;
import com.alkmanistik.alkify_music_api.request.ArtistRequest;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistService {

    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final GlobalMapper globalMapper;
    private final AlbumService albumService;
    private final FileService fileService;

    @Value("${project.images}")
    private String imagePath;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#result.id"),
            @CacheEvict(value = "artist.byUserId", key = "#userId")
    })
    public ArtistDTO createArtist(Long userId, ArtistRequest artistRequest, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new EntityNotFoundException("User not found with id: " + userId);
                });
        Artist artist = new Artist();
        artist.setArtistName(artistRequest.getArtistName());
        artist.setDescription(artistRequest.getDescription());
        if (file != null && !file.isEmpty()) {
            String fileName = fileService.uploadFile(imagePath, file);
            artist.setImageFilePath(fileName);
        }
        artist.setUser(user);
        var savedArtist = artistRepository.save(artist);
        log.info("Created new artist with id: {}", savedArtist.getId());
        if (artistRequest.getAlbums() != null) {
            artistRequest.getAlbums().forEach(albumRequest -> {
                try {
                    albumService.createAlbum(savedArtist.getId(), albumRequest, null);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create album: " + albumRequest.getTitle(), e);
                }
            });
        }
        return globalMapper.toArtistDTO(savedArtist);
    }

    @Cacheable(value = "artist.byUserId", key="#userId",
        unless = "#result == null")
    public List<ArtistDTO> getUserArtists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        return artistRepository.findByUserId(userId).stream()
                .map(globalMapper::toArtistDTO)
                .toList();
    }

    @Cacheable(value = "artist.all",
            unless = "#result == null")
    public List<ArtistDTO> getAllArtists() {
        var artists = artistRepository.findAll();
        return artists.stream()
                .map(globalMapper::toArtistDTO)
                .toList();
    }

    @Cacheable(value = "artist.byId", key="#id",
            unless = "#result == null")
    public ArtistDTO getArtistById(Long id) {
        var artist = artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));
        return globalMapper.toArtistDTO(artist);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#id"),
            @CacheEvict(value = "artist.byUserId", allEntries = true)
    })
    public void deleteArtist(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (artist.getImageFilePath() != null) {
            fileService.deleteFile(imagePath, artist.getImageFilePath());
        }

        albumService.deleteAlbumsByArtist(artist.getId());

        artistRepository.delete(artist);
        log.info("Deleted artist with id: {}", id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#id"),
            @CacheEvict(value = "artist.byUserId", allEntries = true)
    })
    public ArtistDTO updateArtistById(Long id, ArtistRequest artistRequest, MultipartFile file) throws IOException {
        var artistForUpdate = artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));
        if (artistRequest.getArtistName() != null) {
            artistForUpdate.setArtistName(artistRequest.getArtistName());
        }
        artistRequest.setDescription(artistRequest.getDescription());
        if (file != null && !file.isEmpty()) {
            String newFileName = fileService.uploadFile(imagePath, file);
            artistForUpdate.setImageFilePath(newFileName);
        }
        var updatedArtist = artistRepository.save(artistForUpdate);
        return globalMapper.toArtistDTO(updatedArtist);
    }

    @Cacheable(value = "artist.search", key = "#name")
    public List<ArtistDTO> searchArtistsByName(String name) {
        var artists = artistRepository.findByArtistNameContainingIgnoreCase(name);
        return artists.stream()
                .map(globalMapper::toArtistDTO)
                .toList();
    }

    @Transactional
    public void deleteAllArtistsByUser(Long userId) {
        artistRepository.findByUserId(userId).forEach(artist -> {
            deleteArtist(artist.getId());
        });
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#id"),
            @CacheEvict(value = "artist.byUserId", allEntries = true)
    })
    public void subscribeToArtist(Long userId, Long artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (!user.getSubscribedArtists().contains(artist)) {
            user.getSubscribedArtists().add(artist);
            artist.getSubscribers().add(user);
            userRepository.save(user);
            artistRepository.save(artist);
            log.info("User {} subscribed to artist {}", userId, artistId);
        }
    }

    // Удаляем подписку пользователя на артиста
    @Transactional
    public void unsubscribeFromArtist(Long userId, Long artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (user.getSubscribedArtists().contains(artist)) {
            user.getSubscribedArtists().remove(artist);
            artist.getSubscribers().remove(user);
            userRepository.save(user);
            artistRepository.save(artist);
            log.info("User {} unsubscribed from artist {}", userId, artistId);
        }
    }

    public boolean isUserSubscribed(Long userId, Long artistId) {
        return artistRepository.existsByIdAndSubscribersId(artistId, userId);
    }

    public int getSubscriberCount(Long artistId) {
        return artistRepository.countSubscribersById(artistId);
    }

    public List<UserDTO> getArtistSubscribers(Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        return artist.getSubscribers().stream()
                .map(globalMapper::toUserDTO)
                .toList();
    }

    public List<ArtistDTO> getUserSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return user.getSubscribedArtists().stream()
                .map(globalMapper::toArtistDTO)
                .toList();
    }


}