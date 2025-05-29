package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.AlbumDTO;
import com.alkmanistik.alkify_music_api.mapper.GlobalMapper;
import com.alkmanistik.alkify_music_api.model.Album;
import com.alkmanistik.alkify_music_api.model.Artist;
import com.alkmanistik.alkify_music_api.repository.AlbumRepository;
import com.alkmanistik.alkify_music_api.repository.ArtistRepository;
import com.alkmanistik.alkify_music_api.request.AlbumRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GlobalMapper globalMapper;
    private final TrackService trackService;
    private final FileService fileService;

    @Value("${project.images}")
    private String imagePath;

    @Transactional
    public AlbumDTO createAlbum(Long artistId, AlbumRequest albumRequest, MultipartFile file) throws IOException {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> {
                    log.error("Artist not found with id: {}", artistId);
                    return new EntityNotFoundException("Artist not found with id: " + artistId);
                });
        Album album = new Album();
        album.setTitle(albumRequest.getTitle());
        album.setDescription(albumRequest.getDescription());
        if (file != null && !file.isEmpty()) {
            String fileName = fileService.uploadFile(imagePath, file);
            album.setImageFilePath(fileName);
        }
        album.setArtists(List.of(artist));
        var savedAlbum = albumRepository.save(album);
        log.info("Album created: {}", savedAlbum.getId());
        if (albumRequest.getTracks() != null) {
            albumRequest.getTracks().forEach(trackRequest -> {
                try {
                    trackService.createTrack(savedAlbum.getId(), trackRequest, null);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create track: " + trackRequest.getTitle(), e);
                }
            });
        }
        return globalMapper.toAlbumDTO(savedAlbum);
    }

    public List<AlbumDTO> getAllAlbums() {
        var albums = albumRepository.findAll();
        return albums.stream()
                .map(globalMapper::toAlbumDTO)
                .toList();
    }

    public List<AlbumDTO> getAlbumsByArtistId(Long artistId) {
        if (!artistRepository.existsById(artistId)) {
            throw new EntityNotFoundException("Artist not found with id: " + artistId);
        }

        return albumRepository.findByArtistsId(artistId).stream()
                .map(globalMapper::toAlbumDTO)
                .toList();
    }

    public AlbumDTO getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));
        return globalMapper.toAlbumDTO(album);
    }

    @Transactional
    public AlbumDTO updateAlbum(Long id, AlbumRequest albumRequest, MultipartFile file) throws IOException {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));

        if (albumRequest.getTitle() != null) {
            album.setTitle(albumRequest.getTitle());
        }

        if (albumRequest.getDescription() != null) {
            album.setDescription(albumRequest.getDescription());
        }

        if (file != null && !file.isEmpty()) {
            String fileName = fileService.uploadFile(imagePath, file);
            album.setImageFilePath(fileName);
        }

        Album updatedAlbum = albumRepository.save(album);
        log.info("Album updated: {}", id);
        return globalMapper.toAlbumDTO(updatedAlbum);
    }

    @Transactional
    public void deleteAlbum(Long album_id) {
        Album album = albumRepository.findById(album_id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));

        trackService.deleteTracksByAlbum(album_id);

        if (album.getImageFilePath() != null) {
            fileService.deleteFile(imagePath, album.getImageFilePath());
        }

        albumRepository.delete(album);
        log.info("Album deleted: {}", album_id);
    }

    @Transactional
    public AlbumDTO addArtistToAlbum(Long albumId, Long artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (album.getArtists().contains(artist)) {
            throw new IllegalArgumentException("Artist already exists in this album");
        }

        album.getArtists().add(artist);
        Album updatedAlbum = albumRepository.save(album);
        return globalMapper.toAlbumDTO(updatedAlbum);
    }

    @Transactional
    public AlbumDTO removeArtistFromAlbum(Long albumId, Long artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new EntityNotFoundException("Album not found"));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found"));

        if (!album.getArtists().contains(artist)) {
            throw new IllegalArgumentException("Artist not found in this album");
        }

        album.getArtists().remove(artist);
        Album updatedAlbum = albumRepository.save(album);
        return globalMapper.toAlbumDTO(updatedAlbum);
    }

    public List<AlbumDTO> searchAlbums(String title) {
        if (title == null || title.isBlank()) {
            return getAllAlbums();
        }

        return albumRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(globalMapper::toAlbumDTO)
                .toList();
    }

    @Transactional
    public void deleteAlbumsByArtist(Long artistId) {
        albumRepository.findByArtistsId(artistId).forEach(album -> {
            deleteAlbum(album.getId());
        });
    }

}
