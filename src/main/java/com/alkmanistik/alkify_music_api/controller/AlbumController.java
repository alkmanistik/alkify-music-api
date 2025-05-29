package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.AlbumDTO;
import com.alkmanistik.alkify_music_api.request.AlbumRequest;
import com.alkmanistik.alkify_music_api.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping(value = "/{artist_id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumDTO createAlbum(
            @PathVariable Long artist_id,
            @RequestPart @Valid AlbumRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        return albumService.createAlbum(artist_id, request, image);
    }

    @GetMapping
    public List<AlbumDTO> getAllAlbums(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long artistId) {

        if (title != null) {
            return albumService.searchAlbums(title);
        } else if (artistId != null) {
            return albumService.getAlbumsByArtistId(artistId);
        }
        return albumService.getAllAlbums();
    }

    @GetMapping("/{id}")
    public AlbumDTO getAlbumById(@PathVariable Long id) {
        return albumService.getAlbumById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AlbumDTO updateAlbum(
            @PathVariable Long id,
            @RequestPart @Valid AlbumRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        return albumService.updateAlbum(id, request, image);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
    }

    @PostMapping("/{albumId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    public AlbumDTO addArtistToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long artistId) {
        return albumService.addArtistToAlbum(albumId, artistId);
    }

    @DeleteMapping("/{albumId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeArtistFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long artistId) {
        albumService.removeArtistFromAlbum(albumId, artistId);
    }

    @GetMapping("/search")
    public List<AlbumDTO> searchAlbums(@RequestParam String title) {
        return albumService.searchAlbums(title);
    }
}
