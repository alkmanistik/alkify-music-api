package com.alkmanistik.alkify_music_api.service;

import com.alkmanistik.alkify_music_api.dto.UserDTO;
import com.alkmanistik.alkify_music_api.mapper.GlobalMapper;
import com.alkmanistik.alkify_music_api.model.User;
import com.alkmanistik.alkify_music_api.repository.UserRepository;
import com.alkmanistik.alkify_music_api.request.ArtistRequest;
import com.alkmanistik.alkify_music_api.request.UserRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GlobalMapper globalMapper;
    private final ArtistService artistService;

//    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = "users.all", allEntries = true),
//            @CacheEvict(value = "user.byId", key = "#result.id", condition = "#result != null"),
//            @CacheEvict(value = "user.byEmail", key = "#userRequest.email")})
//    public UserDTO createUser(UserRequest userRequest) {
//        if (userRepository.existsByEmail(userRequest.getEmail())) {
//            throw new IllegalArgumentException("Email already exists");
//        }
//        User user = new User();
//        user.setUsername(userRequest.getUsername());
//        user.setEmail(userRequest.getEmail());
//        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
//        User savedUser = userRepository.save(user);
//        log.info("Created user with id: {}", savedUser.getId());
//
//        if (userRequest.getManagedArtists() != null) {
//            userRequest.getManagedArtists().forEach(artistRequest -> {
//                try {
//                    artistService.createArtist(savedUser.getId(), artistRequest, null);
//                } catch (IOException e) {
//                    throw new RuntimeException("Failed to create artist: " + artistRequest.getArtistName(), e);
//                }
//            });
//        }
//
//        return globalMapper.toUserDTO(savedUser);
//    }




    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#result.id", condition = "#result != null"),
            @CacheEvict(value = "user.byEmail", key = "#user.email")})
    public void createUser(User user, List<ArtistRequest> managedArtists) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User savedUser = userRepository.save(user);
        log.info("Created user with id: {}", savedUser.getId());

        if (managedArtists != null) {
            managedArtists.forEach(artistRequest -> {
                try {
                    artistService.createArtist(savedUser, artistRequest, null);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create artist: " + artistRequest.getArtistName(), e);
                }
            });
        }
    }

    @Cacheable(value = "user.byEmail", key = "#email",
            unless = "#result == null", sync = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return globalMapper.toUserDTO(user);
    }

    @Cacheable(value = "user.byId", key = "#id",
            unless = "#result == null", sync = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return globalMapper.toUserDTO(user);
    }

    @Cacheable(value = "users.all",
            unless = "#result == null")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(globalMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#id"),
            @CacheEvict(value = "user.byEmail", key = "#userUpdates.email")})
    public UserDTO updateUser(Long id, UserRequest userUpdates) {
        User userForUpdate = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));



        if (userUpdates.getUsername() != null) {
            userForUpdate.setUsername(userUpdates.getUsername());
        }

        if (userUpdates.getEmail() != null && !userForUpdate.getEmail().equals(userUpdates.getEmail())) {
            if (userRepository.existsByEmail(userUpdates.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            userForUpdate.setEmail(userUpdates.getEmail());
        }

//        if (userUpdates.getPassword() != null) {
//            userForUpdate.setPassword(passwordEncoder.encode(userUpdates.getPassword()));
//        }

        User updatedUser = userRepository.save(userForUpdate);
        return globalMapper.toUserDTO(updatedUser);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users.all", allEntries = true),
            @CacheEvict(value = "user.byId", key = "#userId"),
            @CacheEvict(value = "user.byEmail", allEntries = true) // Безопаснее инвалидировать весь кеш
    })
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        artistService.deleteAllArtistsByUser(userId);

        userRepository.delete(user);
        log.info("Deleted user with id: {}", userId);
    }

    public User getUserEntityByEmail(@Email @NotBlank(message = "Email is mandatory") String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UserDetailsService userDetailsService() {
        return this::getUserEntityByEmail;
    }

}
