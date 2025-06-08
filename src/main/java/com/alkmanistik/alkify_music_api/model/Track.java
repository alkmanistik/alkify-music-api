package com.alkmanistik.alkify_music_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tracks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"likedUsers", "artists", "album"})
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String genre;
    private int durationSeconds;
    private String audioFilePath;
    private LocalDateTime releaseDate;
    private boolean isExplicit;
    private int playCount;

    @ManyToMany
    @JoinTable(name = "track_likes",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> likedUsers;

    @ManyToMany
    private List<Artist> artists;

    @ManyToOne

    private Album album;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
