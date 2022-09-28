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
@Table(name = "celeb_challenge_info")
public class CelebChallengeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    Challenge challenge;

    String music;
    String musicTitle;
    Double length;

    String info;

    @Column(name = "start_date")
    LocalDateTime startDate;

    @Column(name = "end_date")
    LocalDateTime endDate;

    @Column(name = "target_num")
    Integer targetNum;

    @Column(name = "target_amount")
    Integer targetAmount;

    @Formula("(SELECT COUNT(*) FROM donation d WHERE d.challenge_id=challenge_id)")
    Integer currentNum;

    @Formula("(SELECT SUM(d.amount) FROM donation d WHERE d.challenge_id=challenge_id)")
    Integer currentAmount;

    @OneToOne
    @JoinColumn(name = "contract_id")
    ChallengeContract challengeContract;

    @ManyToOne
    @JoinColumn(name = "donation_organization_id")
    DonationOrganization donationOrganization;
}
