package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.ArtistDTO;
import com.alkmanistik.alkify_music_api.dto.UserDTO;
import com.alkmanistik.alkify_music_api.exception.ForbiddenException;
import com.alkmanistik.alkify_music_api.request.ArtistRequest;
import com.alkmanistik.alkify_music_api.service.ArtistService;
import com.alkmanistik.alkify_music_api.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ArtistDTO>> getAllArtists() {
        return ResponseEntity.ok(artistService.getAllArtists());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ArtistDTO> getArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.getArtistById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArtistDTO>> getArtistsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(artistService.getUserArtists(userId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ArtistDTO> updateArtist(
            @PathVariable Long id,
            @RequestPart @Valid ArtistRequest artistRequest,
            @RequestPart(required = false) MultipartFile image) throws IOException, ForbiddenException {
        var user = securityService.getCurrentUser();
        return ResponseEntity.ok(artistService.updateArtistById(id, user, artistRequest, image));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        artistService.deleteArtist(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ArtistDTO>> searchArtists(
            @RequestParam String name) {
        return ResponseEntity.ok(artistService.searchArtistsByName(name));
    }

    @GetMapping("/subscriptions")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public List<ArtistDTO> getSubscriptions() {
        var user = securityService.getCurrentUser();
        return artistService.getUserSubscriptions(user);
    }

    @GetMapping("/check-subscription/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public boolean checkSubscription(@PathVariable Long artistId) {
        var user = securityService.getCurrentUser();
        return artistService.isUserSubscribed(user, artistId);
    }

    @PostMapping("/subscribe-artist/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void subscribeArtist(@PathVariable Long artistId) {
        var user = securityService.getCurrentUser();
        artistService.subscribeToArtist(user, artistId);

    }

    @PostMapping("/unsubscribe-artist/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void unsubscribeArtist(@PathVariable Long artistId) {
        var user = securityService.getCurrentUser();
        artistService.unsubscribeFromArtist(user, artistId);
    }

    @GetMapping("/subscribers/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getArtistSubscribers(@PathVariable Long artistId) {
        return artistService.getArtistSubscribers(artistId);
    }

    @GetMapping("/subscribers-count/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public int getSubscriberCount(@PathVariable Long artistId) {
        return artistService.getSubscriberCount(artistId);
    }

}
