package com.hoodiev.glance.region;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String legalCode;

    @Column(nullable = false, length = 20)
    private String sido;

    @Column(nullable = false, length = 30)
    private String sigungu;

    @Column(nullable = false, length = 30)
    private String dong;

    @Builder
    public Region(String legalCode, String sido, String sigungu, String dong) {
        this.legalCode = legalCode;
        this.sido = sido;
        this.sigungu = sigungu;
        this.dong = dong;
    }

    public void updateNames(String sido, String sigungu, String dong) {
        this.sido = sido;
        this.sigungu = sigungu;
        this.dong = dong;
    }
}
