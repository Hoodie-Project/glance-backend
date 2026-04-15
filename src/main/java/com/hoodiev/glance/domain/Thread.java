package com.hoodiev.glance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "threads")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 100)
    private String locationName;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @ElementCollection
    @CollectionTable(
            name = "thread_tags",
            joinColumns = @JoinColumn(name = "thread_id"),
            indexes = @Index(name = "idx_thread_tags_tag", columnList = "tag")
    )
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection(targetClass = AnimalLook.class)
    @CollectionTable(name = "thread_animal_looks", joinColumns = @JoinColumn(name = "thread_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "animal_look", length = 20)
    private Set<AnimalLook> animalLooks = new HashSet<>();

    @ElementCollection(targetClass = VibeStyle.class)
    @CollectionTable(name = "thread_vibe_styles", joinColumns = @JoinColumn(name = "thread_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "vibe_style", length = 30)
    private Set<VibeStyle> vibeStyles = new HashSet<>();

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer commentCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Thread(String nickname, String title, String content, Double latitude, Double longitude,
                  String locationName, String password, Gender gender, List<String> tags,
                  Set<AnimalLook> animalLooks, Set<VibeStyle> vibeStyles) {
        this.nickname = nickname;
        this.title = title;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.password = password;
        this.gender = gender;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.animalLooks = animalLooks != null ? animalLooks : new HashSet<>();
        this.vibeStyles = vibeStyles != null ? vibeStyles : new HashSet<>();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
