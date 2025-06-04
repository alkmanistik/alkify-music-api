package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.ArtistDTO;
import com.alkmanistik.alkify_music_api.request.ArtistRequest;
import com.alkmanistik.alkify_music_api.service.ArtistService;
import com.alkmanistik.alkify_music_api.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final SecurityService securityService;

    @GetMapping
    public ResponseEntity<List<ArtistDTO>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ArtistDTO>> getArtistsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(artistService.getUserArtists(userId));
    }

    @PostMapping()
    public ResponseEntity<ArtistDTO> createArtist(
            @RequestPart @Valid ArtistRequest artistRequest,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) image = null;
        var user = securityService.getCurrentUser();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(artistService.createArtist(user, artistRequest, image));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArtistDTO> updateArtist(
            @PathVariable Long id,
            @RequestPart @Valid ArtistRequest artistRequest,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        return ResponseEntity.ok(artistService.updateArtistById(id, artistRequest, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArtistDTO>> searchArtists(
            @RequestParam String name) {
        return ResponseEntity.ok(artistService.searchArtistsByName(name));
    }

    @GetMapping("/subscriptions")
    @ResponseStatus(HttpStatus.OK)
    public List<ArtistDTO> getSubscriptions() {
        var user = securityService.getCurrentUser();
        return artistService.getUserSubscriptions(user);
    }

    @PostMapping("/subscribe-artist/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribeArtist(@PathVariable Long artistId) {
        var user = securityService.getCurrentUser();
        artistService.subscribeToArtist(user, artistId);

    }

    @PostMapping("/unsubscribe-artist/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribeArtist(@PathVariable Long artistId) {
        var user = securityService.getCurrentUser();
        artistService.unsubscribeFromArtist(user, artistId);
    }

}
