package com.ssafy.kirin.service;

import com.ssafy.kirin.dto.UserDTO;
import com.ssafy.kirin.dto.request.CommunityCommentRequestDTO;
import com.ssafy.kirin.dto.request.CommunityRequestDTO;
import com.ssafy.kirin.dto.response.CommunityResponseDTO;
import com.ssafy.kirin.entity.*;
import com.ssafy.kirin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
    private final CommunityRepository communityRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityCommnetLikeRepository communityCommentLikeRepository;
    private final UserRepository userRepository;
    @Value("${image.dir}")
    private String communityImageDirectory;

    @Override
    public List<Community> getCommunityList(long starId) {
        return communityRepository.findAllByUserId(starId);
    }

    @Override
    @Transactional
    public void writeCommunity(long starId, UserDTO userDTO, CommunityRequestDTO dto) throws IOException {
        
        User user = userRepository.getReferenceById(userDTO.getId());
        Community community = Community.builder()
                .user(user)
                .title(dto.title())
                .content(dto.content())
                .reg(LocalDateTime.now())
                .build();

        communityRepository.save(community);

        if(!dto.image().isEmpty()) {
            MultipartFile file = dto.image();
            // 파일 디렉토리 + UUID + 확장자로 Path 설정
            String storeName = communityImageDirectory+ UUID.randomUUID() + file.getOriginalFilename();
            Path dir = Paths.get(storeName);
            //지정된 디렉토리에 저장
            Files.copy(file.getInputStream(),dir);
            community.setImg(storeName);
        }
    }

    @Override
    public CommunityResponseDTO getCommunity(long boardId) {

        Community community = communityRepository.findById(boardId);
        List<CommunityComment> commentList = communityCommentRepository.findByCommunityId(boardId);

        return new CommunityResponseDTO(community, commentList);
    }

    @Override
    public boolean likeCommunity(long userId, long boardId) {
        CommunityLike communityLike = CommunityLike.builder()
                .userId(userId).communityId(boardId).build();
        communityLikeRepository.save(communityLike);

        return true;
    }

    @Override
    public boolean unlikeCommunity(long userId, long boardId) {

        communityLikeRepository.deleteByUserIdAndCommunityId(userId, boardId);

        return true;
    }
    @Override
    @Transactional
    public void writeComment(long userId, long communityId, CommunityCommentRequestDTO dto) {

        User user = userRepository.getReferenceById(userId);

        CommunityComment communityComment = CommunityComment.builder()
                .content(dto.content()).reg(LocalDateTime.now())
                .communityId(communityId).user(user).parentId(null)
                .isComment(dto.isComment())
                .build();

        if(dto.parentId()>0) communityComment.setParentId(dto.parentId());

        communityCommentRepository.save(communityComment);
    }

    @Override
    public boolean likeCommunityComment(long userId, long commentId) {
        CommunityCommentLike communityCommentLike = CommunityCommentLike.builder()
                .userId(userId).communityCommentId(commentId).build();
        communityCommentLikeRepository.save(communityCommentLike);

        return true;
    }

    @Override
    public boolean unlikeCommunityComment(long userId, long commentId) {
        communityCommentLikeRepository.deleteByUserIdAndCommunityCommentId(userId, commentId);
        return true;
    }
}
