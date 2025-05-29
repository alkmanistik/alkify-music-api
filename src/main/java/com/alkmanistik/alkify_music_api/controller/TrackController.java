package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.TrackDTO;
import com.alkmanistik.alkify_music_api.model.User;
import com.alkmanistik.alkify_music_api.repository.UserRepository;
import com.alkmanistik.alkify_music_api.request.TrackRequest;
import com.alkmanistik.alkify_music_api.service.TrackService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;
    private final UserRepository userRepository;

    @GetMapping()
    public List<TrackDTO> getAllTracks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long artistId
    ) {
        if (title != null) {
            return trackService.searchTracks(title);
        } else if (artistId != null) {
            return trackService.getTracksByAlbumId(artistId);
        }
        return trackService.getAllTracks();
    }

    @GetMapping("/{trackId}")
    private TrackDTO getTrackById(@PathVariable Long trackId) {
        return trackService.getById(trackId);
    }

    @PostMapping("/{albumId}")
    @ResponseStatus(HttpStatus.OK)
    public TrackDTO createTrack(
            @PathVariable Long albumId,
            @RequestPart @Valid TrackRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        return trackService.createTrack(albumId, request, image);
    }

    @PutMapping("/{trackId}")
    @ResponseStatus(HttpStatus.OK)
    public TrackDTO updateTrack(
            @PathVariable Long trackId,
            @RequestPart @Valid TrackRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        return trackService.updateTrack(trackId, request, image);
    }

    @GetMapping("/search")
    public List<TrackDTO> searchTracks(@RequestParam String title) {
        return trackService.searchTracks(title);
    }

    @PostMapping("/{trackId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    public TrackDTO addArtistToAlbum(
            @PathVariable Long trackId,
            @PathVariable Long artistId) {
        return trackService.addArtistToTrack(trackId, artistId);
    }

    @DeleteMapping("/{trackId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeArtistFromAlbum(
            @PathVariable Long trackId,
            @PathVariable Long artistId) {
        trackService.removeArtistFromTrack(trackId, artistId);
    }

    @PostMapping("/like-track/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeTrack(@PathVariable Long trackId) {

        // TODO:Аутентификация

        String email = "erik.fattakhov.04@mail.ru";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        trackService.likeTrack(trackId, user.getId());

    }

    @PostMapping("/unlike-track/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikeTrack(@PathVariable Long trackId) {

        // TODO:Аутентификация

        String email = "erik.fattakhov.04@mail.ru";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        trackService.unlikeTrack(trackId, user.getId());
    }

    @GetMapping("/liked")
    @ResponseStatus(HttpStatus.OK)
    public List<TrackDTO> getLikedTracks() {
        // TODO:Аутентификация

        String email = "erik.fattakhov.04@mail.ru";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return trackService.getLikedTracks(user.getId());
    }

}
