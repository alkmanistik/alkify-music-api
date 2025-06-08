package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.TrackDTO;
import com.alkmanistik.alkify_music_api.exception.ForbiddenException;
import com.alkmanistik.alkify_music_api.request.TrackRequest;
import com.alkmanistik.alkify_music_api.service.SecurityService;
import com.alkmanistik.alkify_music_api.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final SecurityService securityService;
    private final TrackService trackService;

    @PreAuthorize("permitAll()")
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

    @PreAuthorize("permitAll()")
    @GetMapping("/{trackId}")
    public TrackDTO getTrackById(@PathVariable Long trackId) {
        return trackService.getById(trackId);
    }

    @DeleteMapping("/{trackId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrackById(@PathVariable Long trackId) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        trackService.deleteTrack(trackId, user);
    }

    @PostMapping("/{albumId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public TrackDTO createTrack(
            @PathVariable Long albumId,
            @RequestPart @Valid TrackRequest request,
            @RequestPart(required = false) MultipartFile audio) throws IOException, ForbiddenException {
        var user = securityService.getCurrentUser();
        return trackService.createTrack(albumId, user, request, audio);
    }

    @PutMapping("/{trackId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public TrackDTO updateTrack(
            @PathVariable Long trackId,
            @RequestPart @Valid TrackRequest request,
            @RequestPart(required = false) MultipartFile image) throws IOException, ForbiddenException {
        var user = securityService.getCurrentUser();
        return trackService.updateTrack(trackId, user, request, image);
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/search")
    public List<TrackDTO> searchTracks(@RequestParam String title) {
        return trackService.searchTracks(title);
    }

    @PostMapping("/{trackId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public TrackDTO addArtistToTrack(
            @PathVariable Long trackId,
            @PathVariable Long artistId) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        return trackService.addArtistToTrack(user, trackId, artistId);
    }

    @DeleteMapping("/{trackId}/artists/{artistId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void removeArtistFromTrack(
            @PathVariable Long trackId,
            @PathVariable Long artistId) throws ForbiddenException {
        var user = securityService.getCurrentUser();
        trackService.removeArtistFromTrack(user, trackId, artistId);
    }

    @PostMapping("/like-track/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void likeTrack(@PathVariable Long trackId) {
        var user = securityService.getCurrentUser();
        trackService.likeTrack(trackId, user);

    }

    @PostMapping("/unlike-track/{trackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void unlikeTrack(@PathVariable Long trackId) {
        var user = securityService.getCurrentUser();
        trackService.unlikeTrack(trackId, user);
    }

    @GetMapping("/liked")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public List<TrackDTO> getLikedTracks() {
        var user = securityService.getCurrentUser();
        return trackService.getLikedTracks(user);
    }

    @GetMapping("/check-like/{trackId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('USER')")
    public boolean checkTrackLike(@PathVariable Long trackId) {
        var user = securityService.getCurrentUser();
        return trackService.isTrackLikedByUser(trackId, user.getId());
    }

}
