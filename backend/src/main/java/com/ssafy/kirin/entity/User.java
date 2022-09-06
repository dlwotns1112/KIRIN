package com.ssafy.kirin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 생성을 데이터베이스에 위임하는 전략 (MySQL의 AI)
    long id;

    String name;
    String nickname;

    @Column(name = "profile_img")
    String profileImg;

    @Column(name = "user_id")
    String userId;

    String password;

    @Column(name = "wallet_hash")
    String walletHash;

    @Column(name = "account_type")
    String accountType;

    @Column(name = "social_id")
    String socialId;

    @Column(name = "is_celeb")
    boolean isCeleb;

    @OneToOne
    @JoinColumn(name = "celeb_info_id")
    CelebInfo celebInfo;
}
