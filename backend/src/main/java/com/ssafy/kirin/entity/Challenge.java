package com.ssafy.kirin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "challenge")
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String title;
    String video;
    String thumbnail;
    LocalDateTime reg;

    @Column(name = "is_original")
    boolean isOriginal;

    @Column(name = "challenge_id")
    long challengeId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Formula("(SELECT COUNT(*) FROM challenge_like l WHERE l.challenge_id = id)")
    int likeCnt;

    @Formula("(SELECT COUNT(*) FROM challenge_comment c WHERE c.challenge_id = id)")
    int commentCnt;

    @OneToOne
    @JoinColumn(name = "celeb_challenge_info_id")
    CelebChallengeInfo celebChallengeInfo;
}
