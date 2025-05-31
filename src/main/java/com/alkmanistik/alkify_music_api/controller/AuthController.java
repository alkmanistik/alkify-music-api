package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.JwtAuthenticationDTO;
import com.alkmanistik.alkify_music_api.request.AuthRequest;
import com.alkmanistik.alkify_music_api.request.UserRequest;
import com.alkmanistik.alkify_music_api.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public JwtAuthenticationDTO signUp(@RequestBody @Valid UserRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationDTO signIn(@RequestBody @Valid AuthRequest request) {
        return authenticationService.signIn(request);
    }
}