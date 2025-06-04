package com.alkmanistik.alkify_music_api.controller;

import com.alkmanistik.alkify_music_api.dto.UserDTO;
import com.alkmanistik.alkify_music_api.request.UserRequest;
import com.alkmanistik.alkify_music_api.service.SecurityService;
import com.alkmanistik.alkify_music_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityService securityService;

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('USER')")
    public UserDTO getYourself() {
        var user = securityService.getCurrentUser();
        return userService.getYourself(user);
    }

    @PutMapping("/")
    @PreAuthorize("hasRole('USER')")
    public UserDTO updateUser(
            @RequestBody @Valid UserRequest userUpdates
    ) {
        var user = securityService.getCurrentUser();
        return userService.updateUser(user, userUpdates);
    }

    @DeleteMapping("/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void deleteYourself() {
        var user = securityService.getCurrentUser();
        userService.deleteUser(user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/add-admin/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void addAdminRole(@PathVariable Long id) {
        userService.addAdminRole(id);
    }

}