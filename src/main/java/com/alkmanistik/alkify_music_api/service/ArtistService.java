package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.ArtistDTO;
import com.alkmanistik.alkify_music_api.dto.UserDTO;
import com.alkmanistik.alkify_music_api.exception.ForbiddenException;
import com.alkmanistik.alkify_music_api.mapper.GlobalMapper;
import com.alkmanistik.alkify_music_api.model.Artist;
import com.alkmanistik.alkify_music_api.model.Role;
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
import java.util.stream.Collectors;

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
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#result.id", condition = "#result != null"),
            @CacheEvict(value = "artists.byUserId", key = "#user.id"),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "artist.subscriptions", key = "#user.id")
    })
    public ArtistDTO createArtist(User user, ArtistRequest artistRequest, MultipartFile file) throws IOException {
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
                    albumService.createAlbum(savedArtist.getId(), user, albumRequest, null);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create album: " + albumRequest.getTitle(), e);
                } catch (ForbiddenException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return globalMapper.toArtistDTO(savedArtist);
    }

    @Cacheable(value = "artists.byUserId", key = "#userId", sync = true)
    public List<ArtistDTO> getUserArtists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        return artistRepository.findByUserId(userId).stream()
                .map(globalMapper::toArtistDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "artists.all", sync = true)
    public List<ArtistDTO> getAllArtists() {
        return artistRepository.findAll().stream()
                .map(globalMapper::toArtistDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "artist.byId", key = "#id", sync = true)
    public ArtistDTO getArtistById(Long id) {
        return artistRepository.findById(id)
                .map(globalMapper::toArtistDTO)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#id"),
            @CacheEvict(value = "artists.byUserId", key = "#user.id"),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "artist.subscriptions", allEntries = true),
            @CacheEvict(value = "artist.subscribers", key = "#id")
    })
    public void deleteArtist(Long id, User user) throws ForbiddenException {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        checkArtistOwnership(artist, user);

        if (artist.getImageFilePath() != null) {
            fileService.deleteFile(imagePath, artist.getImageFilePath());
        }

        albumService.deleteAlbumsByArtist(artist.getId());

        artistRepository.delete(artist);
        log.info("Deleted artist with id: {} by userId {}", id, user.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#id"),
            @CacheEvict(value = "artists.byUserId", allEntries = true),
            @CacheEvict(value = "artist.search", allEntries = true),
            @CacheEvict(value = "artist.subscriptions", allEntries = true),
            @CacheEvict(value = "artist.subscribers", key = "#id")
    })
    protected void delete(Long id) {
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
            @CacheEvict(value = "artists.all", allEntries = true),
            @CacheEvict(value = "artist.byId", key = "#id"),
            @CacheEvict(value = "artists.byUserId", allEntries = true),
            @CacheEvict(value = "artist.search", allEntries = true)
    })
    public ArtistDTO updateArtistById(Long id, User user, ArtistRequest artistRequest, MultipartFile file) throws IOException, ForbiddenException {
        var artistForUpdate = artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        checkArtistOwnership(artistForUpdate, user);

        if (artistRequest.getArtistName() != null) {
            artistForUpdate.setArtistName(artistRequest.getArtistName());
        }
        artistForUpdate.setDescription(artistRequest.getDescription());
        if (file != null && !file.isEmpty()) {
            String newFileName = fileService.uploadFile(imagePath, file);
            artistForUpdate.setImageFilePath(newFileName);
        }
        var updatedArtist = artistRepository.save(artistForUpdate);
        return globalMapper.toArtistDTO(updatedArtist);
    }

    @Cacheable(value = "artist.search", key = "#name", sync = true)
    public List<ArtistDTO> searchArtistsByName(String name) {
        return artistRepository.findByArtistNameContainingIgnoreCase(name).stream()
                .map(globalMapper::toArtistDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllArtistsByUser(Long userId) {
        artistRepository.findByUserId(userId).forEach(artist ->
                delete(artist.getId()));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist.subscribers", key = "#artistId"),
            @CacheEvict(value = "artist.subscriptions", key = "#user.id"),
            @CacheEvict(value = "artist.byId", key = "#artistId")
    })
    public void subscribeToArtist(User user, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (!user.getSubscribedArtists().contains(artist)) {
            user.getSubscribedArtists().add(artist);
            artist.getSubscribers().add(user);
            userRepository.save(user);
            artistRepository.save(artist);
            log.info("User {} subscribed to artist {}", user.getId(), artistId);
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "artist.subscribers", key = "#artistId"),
            @CacheEvict(value = "artist.subscriptions", key = "#user.id"),
            @CacheEvict(value = "artist.byId", key = "#artistId")
    })
    public void unsubscribeFromArtist(User user, Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (user.getSubscribedArtists().contains(artist)) {
            user.getSubscribedArtists().remove(artist);
            artist.getSubscribers().remove(user);
            userRepository.save(user);
            artistRepository.save(artist);
            log.info("User {} unsubscribed from artist {}", user.getId(), artistId);
        }
    }

    @Cacheable(value = "artist.subscribed", key = "{#user.id, #artistId}")
    public boolean isUserSubscribed(User user, Long artistId) {
        return artistRepository.existsByIdAndSubscribersId(artistId, user.getId());
    }

    @Cacheable(value = "artist.subscribers.count", key = "#artistId")
    public int getSubscriberCount(Long artistId) {
        return artistRepository.countSubscribersById(artistId);
    }

    @Cacheable(value = "artist.subscribers", key = "#artistId")
    public List<UserDTO> getArtistSubscribers(Long artistId) {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        return artist.getSubscribers().stream()
                .map(globalMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "artist.subscriptions", key = "#user.id")
    public List<ArtistDTO> getUserSubscriptions(User user) {
        return user.getSubscribedArtists().stream()
                .map(globalMapper::toArtistDTO)
                .collect(Collectors.toList());
    }

    private void checkArtistOwnership(Artist artist, User user) throws ForbiddenException {
        if (!artist.getUser().getId().equals(user.getId())
                && !user.getRoles().contains(Role.ADMIN)) {
            throw new ForbiddenException("No permission to modify this artist");
        }
    }

}