package com.ssafy.kirin.entity;

import lombok.*;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "community")
public class Community {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String title;

    String content;

    String img;

    LocalDateTime reg;

    @Formula("(SELECT COUNT(*) FROM community_like l WHERE l.community_id = id)")
    int likeCnt;

    @Formula("(SELECT COUNT(*) FROM community_comment c WHERE c.community_id = id)")
    int commentCnt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

}
