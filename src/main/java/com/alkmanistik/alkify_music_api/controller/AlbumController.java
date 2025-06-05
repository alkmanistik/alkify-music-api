package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.AlbumDTO;
import com.alkmanistik.alkify_music_api.exception.ForbiddenException;
import com.alkmanistik.alkify_music_api.request.AlbumRequest;
import com.alkmanistik.alkify_music_api.service.AlbumService;
import com.alkmanistik.alkify_music_api.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final SecurityService securityService;

    @PostMapping(value = "/{artist_id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public AlbumDTO createAlbum(
            @PathVariable Long artist_id,
            @RequestPart @Valid AlbumRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException, ForbiddenException {
        var user = securityService.getCurrentUser();
        return albumService.createAlbum(artist_id, user, request, image);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
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
    @PreAuthorize("permitAll()")
    public AlbumDTO getAlbumById(@PathVariable Long id) {
        return albumService.getAlbumById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public AlbumDTO updateAlbum(
            @PathVariable Long id,
            @RequestPart @Valid AlbumRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException, ForbiddenException {
        var user = securityService.getCurrentUser();
        return albumService.updateAlbum(id, user, request, image);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void deleteAlbum(@PathVariable Long id) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        albumService.deleteAlbum(id, user);
    }

    @PostMapping("/{albumId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public AlbumDTO addArtistToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long artistId) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        return albumService.addArtistToAlbum(user, albumId, artistId);
    }

    @DeleteMapping("/{albumId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void removeArtistFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long artistId) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        albumService.removeArtistFromAlbum(user, albumId, artistId);
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public List<AlbumDTO> searchAlbums(@RequestParam String title) {
        return albumService.searchAlbums(title);
    }
}
