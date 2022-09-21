package com.ssafy.kirin.service;

import com.ssafy.kirin.dto.request.ChallengeCommentRequestDTO;
import com.ssafy.kirin.entity.Challenge;
import com.ssafy.kirin.entity.ChallengeComment;
import com.ssafy.kirin.entity.ChallengeLike;
import com.ssafy.kirin.entity.User;
import com.ssafy.kirin.repository.ChallengeCommentRepository;
import com.ssafy.kirin.repository.ChallengeLikeRepository;
import com.ssafy.kirin.repository.ChallengeRepository;
import com.ssafy.kirin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final ChallengeCommentRepository challengeCommentRepository;
    private final UserRepository userRepository;

    @Override
    public List<Challenge> listStarsByPopularity() {
        return challengeRepository.findByIsOriginal(true, Sort.by(Sort.Direction.DESC,"likeCnt"));
    }

    @Override
    public List<Challenge> listStarsByLatest() {
        return challengeRepository.findByIsOriginal(true, Sort.by(Sort.Direction.DESC,"id"));
    }

    @Override
    public List<Challenge> listGeneralByPopularity() {
        return challengeRepository.findByIsOriginal(false,Sort.by(Sort.Direction.DESC,"likeCnt"));
    }

    @Override
    public List<Challenge> listGeneralByRandom() {
        List<Challenge> challenges = challengeRepository.findByIsOriginal(false);
        Collections.shuffle(challenges);
        return challenges;
    }

    @Override
    public List<Challenge> listAllByRandom() {
        List<Challenge> challenges = challengeRepository.findAll();
        Collections.shuffle(challenges);
        return challenges;
    }

    @Override
    public List<Challenge> listAllByAlphabet() {
        return challengeRepository.findAll(Sort.by(Sort.Direction.ASC,"title"));
    }

    @Override
    public List<Challenge> listAllByChallenge(long challengeId) {
        return challengeRepository.findByChallengeId(challengeId);
    }

    @Override
    public List<Challenge> listAllByUser(long userId) {
        return challengeRepository.findByUserId(userId);
    }

    @Override
    public List<Challenge> listUserLike(long userId) {
        return challengeLikeRepository.findByUserId(userId).stream()
                .map(ChallengeLike::getChallenge).collect(Collectors.toList());
    }

    @Override
    public List<ChallengeComment> getChallengeComment(long challengeId) {

        return challengeCommentRepository.findByChallengeId(challengeId);
    }

    @Override
    public void writeChallengeComment(long userId, long challengeId, ChallengeCommentRequestDTO dto) {
            User user = userRepository.getReferenceById(userId);
            Challenge challenge = challengeRepository.getReferenceById(challengeId);
            challengeCommentRepository.save(
                    ChallengeComment.builder()
                    .challenge(challenge).user(user).isComment(dto.isComment())
                    .content(dto.content()).parentId(dto.parentId())
                    .build());
    }
}
