package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.JwtAuthenticationDTO;
import com.alkmanistik.alkify_music_api.dto.UserDTO;
import com.alkmanistik.alkify_music_api.model.User;
import com.alkmanistik.alkify_music_api.request.AuthRequest;
import com.alkmanistik.alkify_music_api.request.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationDTO signUp(UserRequest request) {
        UserDTO userDTO = userService.createUser(request); // Используем UserService
        User user = userService.getUserEntityByEmail(userDTO.getEmail());

        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationDTO(jwt);
    }

    public JwtAuthenticationDTO signIn(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userService.getUserEntityByEmail(request.getEmail());
        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationDTO(jwt);
    }
}