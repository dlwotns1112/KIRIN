package com.ssafy.kirin.service;

import com.ssafy.kirin.dto.StarChallengeDTO;
import com.ssafy.kirin.dto.UserDTO;
import com.ssafy.kirin.dto.request.ChallengeRequestDTO;
import com.ssafy.kirin.dto.request.ChallengeCommentRequestDTO;
import com.ssafy.kirin.dto.request.StarChallengeRequestDTO;
import com.ssafy.kirin.dto.response.*;
import com.ssafy.kirin.entity.*;
import com.ssafy.kirin.repository.*;
import com.ssafy.kirin.entity.User;
import com.ssafy.kirin.util.ChallengeCommentMapStruct;
import com.ssafy.kirin.util.ChallengeMapStruct;
import com.ssafy.kirin.util.NotificationEnum;
import com.ssafy.kirin.util.UserMapStruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final ChallengeCommentRepository challengeCommentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CelebChallengeInfoRepository celebChallengeInfoRepository;
    private final DonationOrganizationRepository donationOrganizationRepository;
    private final EthereumService ethereumService;
    private final ChallengeContractRepository challengeContractRepository;
    private final DonationRepository donationRepository;
    private final ChallengeCommentLikeRepository challengeCommentLikeRepository;
    private final ChallengeRepositoryCustom challengeRepositoryCustom;
    private final SubscribeRepository subscribeRepository;
    @Value("${property.app.upload-path}")
    private String challengeDir;
    @Value("${logo.image}")
    private String kirinStamp;

    @Override
    public List<ChallengeDTO> listStarsByPopularity() {
        return this.challegeListToChallengDTOList(challengeRepository.findByIsOriginalAndIsProceeding(true, true, Sort.by(Sort.Direction.DESC, "participants")));
    }

    @Override
    public List<ChallengeDTO> listStarsByLatest() {
        return this.challegeListToChallengDTOList(challengeRepository.findByIsOriginalAndIsProceeding(true, true, Sort.by(Sort.Direction.DESC, "reg")));
    }

    @Override
    public List<ChallengeDTO> listGeneralByPopularity() {
        return this.challegeListToChallengDTOList(challengeRepository.findByIsOriginalAndIsProceeding(false, true, Sort.by(Sort.Direction.DESC, "participants")));
    }

    @Override
    public List<ChallengeDTO> listGeneralByRandom() {
        List<ChallengeDTO> challenges = this.challegeListToChallengDTOList(challengeRepository.findByIsOriginalAndIsProceeding(false, true));
        Collections.shuffle(challenges);
        return challenges;
    }

    @Override
    public List<ChallengeDTO> listAllByRandom() {
        List<ChallengeDTO> challenges = this.challegeListToChallengDTOList(challengeRepository.findByIsProceeding(true));
        Collections.shuffle(challenges);
        return challenges;
    }

    @Override
    public List<ChallengeDTO> listStarsByRandom() {
        List<ChallengeDTO> challenges = this.challegeListToChallengDTOList(challengeRepository.findByIsOriginalAndIsProceeding(true, true));
        Collections.shuffle(challenges);
        return challenges;
    }

    @Override
    public List<ChallengeDTO> listAllByAlphabet() {
        return this.challegeListToChallengDTOList(challengeRepository.findAll(Sort.by(Sort.Direction.ASC, "title")));
    }

    @Override
    public List<ChallengeDTO> listAllByChallenge(Long challengeId) {
        return this.challegeListToChallengDTOList(challengeRepository.findByChallengeIdOrderByIsOriginalDesc(challengeId));
    }

    @Override
    public List<ChallengeDTO> listAllByUser(Long userId) {
        return this.challegeListToChallengDTOList(challengeRepository.findByUserId(userId));
    }

    @Override
    public List<ChallengeDTO> listUserLike(Long userId) {
        return challengeLikeRepository.findByUserId(userId).stream()
                .map(ChallengeLike::getChallenge).map(this::mapChallengeDTO).collect(Collectors.toList());
    }

    @Override
    public List<ChallengeCommentDTO> getChallengeComment(Long challengeId) {
        UserDTO userDTO = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userDTO!=null) {
            Set<Long> set = challengeCommentLikeRepository.findByUserId(userDTO.getId()) // 본인이 좋아요 했는지 여부
                    .stream().map(ChallengeCommentLike::getChallengeCommentId).collect(Collectors.toSet());

            return challengeCommentRepository.findByChallengeId(challengeId).stream().map(o -> {
                ChallengeCommentDTO dto = ChallengeCommentMapStruct.INSTANCE.mapToChallengeCommentDTO(o);
                dto.setUser(UserMapStruct.INSTANCE.mapToUserDTO(o.getUser()));
                if(set.contains(o.getId()))dto.setLiked(true);
                return dto;
            }).collect(Collectors.toList());
        }

        return challengeCommentRepository.findByChallengeId(challengeId).stream().map(o -> {
            ChallengeCommentDTO dto = ChallengeCommentMapStruct.INSTANCE.mapToChallengeCommentDTO(o);
            dto.setUser(UserMapStruct.INSTANCE.mapToUserDTO(o.getUser()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChallengeCommentDTO> getChallengeRecomment(Long userId, Long commentId) {
        Set<Long> set = challengeCommentLikeRepository.findByUserId(userId) // 본인이 좋아요 했는지 여부
                .stream().map(ChallengeCommentLike::getChallengeCommentId).collect(Collectors.toSet());

        return challengeCommentRepository.findByParentId(commentId).stream().map(o -> {
            ChallengeCommentDTO dto = ChallengeCommentMapStruct.INSTANCE.mapToChallengeCommentDTO(o);
            dto.setUser(UserMapStruct.INSTANCE.mapToUserDTO(o.getUser()));

            if(set.contains(o.getId())) dto.setLiked(true);

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public void writeChallengeComment(Long userId, Long challengeId, ChallengeCommentRequestDTO dto) {
        User user = userRepository.getReferenceById(userId); // 작성자
        Challenge challenge = challengeRepository.getReferenceById(challengeId); // 챌린지

        ChallengeComment challengeComment = ChallengeComment.builder()
                                            .challengeId(challengeId)
                                            .user(user)
                                            .content(dto.content())
                                            .reg(LocalDateTime.now())
                                            .parentId(dto.parentId())
                                            .build();

        challengeCommentRepository.save(challengeComment);

        // 챌린지 게시자에게 알림
        if(!(userId.equals(challenge.getUser().getId())))
                notificationService.addNotification(Notification.builder().userId(challenge.getUser().getId())
                .event(String.format(NotificationEnum.ChallengeCommentAdded.getContent(), challenge.getTitle(), user.getNickname())).isRead(false)
                .image(user.getProfileImg()).link(String.format("/savana/challenge/%s",challenge.getId())).build());

        if (dto.parentId() != 0) { // 대댓글 등록시
            // 챌린지 내 댓글 게시자에게 알림
            Optional<ChallengeComment> tmp = challengeCommentRepository.findById(dto.parentId());
            tmp.ifPresent(comment -> {
                if(!(comment.getUser().getId().equals(userId)))notificationService.addNotification(Notification.builder().userId(comment.getUser().getId())
                        .event(String.format(NotificationEnum.CommentReplied.getContent(), user.getNickname())).isRead(false)
                        .image(user.getProfileImg()).link(String.format("/savana/challenge/%s",challenge.getId())).build());
            });
            // 해당 댓글의 대댓글 게시자 모두에게 알림
            List<Long> list = challengeCommentRepository.findByParentId(dto.parentId()).stream()
                    .map(o -> o.getUser().getId()).collect(Collectors.toSet()).stream().filter(o->!(o.equals(userId))).toList();

            for (Long id : list) {
                notificationService.addNotification(Notification.builder()
                        .event(String.format(NotificationEnum.CommentReplied.getContent(), user.getNickname()))
                        .image(user.getProfileImg()).link(String.format("/savana/challenge/%s",challenge.getId())).userId(id).isRead(false)
                        .build());
            }
        }
    }

    @Override
    public List<ChallengeSelectResponseDTO> selectChallenge() {

        return challengeRepository.findByIsOriginalAndIsProceeding(true, true, Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(o -> {
                    CelebChallengeInfo celebChallengeInfo = celebChallengeInfoRepository.findByChallengeId(o.getId());
                    return new ChallengeSelectResponseDTO(o.getChallengeId(),o.getTitle(), o.getUser().getNickname(),
                            o.getUser().getProfileImg(),celebChallengeInfo.getLength(),
                            celebChallengeInfo.getMusic(), celebChallengeInfo.getMusicTitle());
                }).collect(Collectors.toList());
    }

    @Override
    public ChallengeSelectResponseDTO selectOneChallenge(Long challengeId) {
        Optional<Challenge> challenge = challengeRepository.findById(challengeId);
        if(challenge.isPresent()){
            Challenge c = challenge.get();
            CelebChallengeInfo celebChallengeInfo = celebChallengeInfoRepository.findByChallengeId(challengeId);
            return new ChallengeSelectResponseDTO(challengeId, c.getTitle(),c.getUser().getNickname() ,
                    c.getUser().getProfileImg(),celebChallengeInfo.getLength(),celebChallengeInfo.getMusic() ,celebChallengeInfo.getMusicTitle() );


        }
        return null;
    }
    @Transactional
    @Override
    public void likeChallenge(Long userId, Long challnegeId) {
        challengeLikeRepository.save(ChallengeLike.builder()
                .challenge(challengeRepository.getReferenceById(challnegeId))
                .user(userRepository.getReferenceById(userId))
                .build()
        );
    }
    @Transactional
    @Override
    public void unlikeChallenge(Long userId, Long challnegeId) {


        challengeLikeRepository.deleteByUserIdAndChallengeId(userId,challnegeId);
    }
    @Transactional
    @Override
    public void likeChallnegeComment(Long userId, Long challengeCommentId) {
        challengeCommentLikeRepository.save(ChallengeCommentLike.builder()
                                    .challengeCommentId(challengeCommentId)
                                    .user(userRepository.getReferenceById(userId))
                                    .build()
                        );
    }
    @Transactional
    @Override
    public void unlikeChallnegeComment(Long userId, Long challengeCommentId) {


        challengeCommentLikeRepository.deleteByUserIdAndChallengeCommentId(userId,challengeCommentId);
    }

    @Override
    public ChallengeDetailDTO getChallengeDetail(Long challengeId) {

        boolean existsChallenge = challengeRepository.existsById(challengeId);
        if(existsChallenge){
            Challenge challenge = challengeRepository.getReferenceById(challengeId);
            CelebChallengeInfo celebChallengeInfo = celebChallengeInfoRepository.findByChallengeId(challenge.getChallengeId());
            List<DonationDTO> donationList = donationRepository.findByChallenge_challengeId(challengeId)
                    .stream().filter(o->!(o.getChallenge().getId().equals(celebChallengeInfo.getChallenge().getId()))).sorted((o1, o2) -> (int) (o2.getAmount()-o1.getAmount()))
                    .map(d->new DonationDTO(d.getAmount(),d.getChallenge().getUser().getNickname(),d.getChallenge().getUser().getProfileImg())).collect(Collectors.toList());
            return new ChallengeDetailDTO(celebChallengeInfo.getInfo(),donationList,celebChallengeInfo.getDonationOrganization().getName(),
                    celebChallengeInfo.getStartDate(), celebChallengeInfo.getEndDate(), celebChallengeInfo.getTargetAmount(),
                    celebChallengeInfo.getTargetNum(), celebChallengeInfo.getCurrentNum(), celebChallengeInfo.getCurrentAmount());
        }else return null;

    }

    @Override
    public List<MyChallengeResponseDTO> getMyChallengelist(Long userId) {
        return challengeRepositoryCustom.findMyChallengesByUserId(userId);
    }

    @Override
    public ChallengeDTO getChallengeSavana(Long challengeId) {
        return mapChallengeDTO(challengeRepository.findById(challengeId).get());
    }

    @Scheduled(initialDelay = 1000, fixedRateString = "${challenge.expiration.check-interval}")
    @Async
    @Transactional
    public void scheduleChallenge() {
        // get list of expired stars' challeges
        // expire stars' challenge and expire following challeges along with notification sent
        challengeRepository.findAllById(celebChallengeInfoRepository.findByEndDateBefore(LocalDateTime.now())
                        .stream().map(o->o.getChallenge().getChallengeId())
                        .collect(Collectors.toList()))
                .stream().filter(Challenge::getIsProceeding).forEach(o -> {o.setIsProceeding(false);
                                //get list of following challenges
                                challengeRepository.findByChallengeIdOrderByIsOriginalDesc(o.getId())
                                        .forEach(c -> {
                                            //expire following challenge
                                            c.setIsProceeding(false);
                                            //send notifiaction
                                            notificationService.addNotification(Notification.builder()
                                                    .userId(c.getUser().getId()).image(o.getUser().getProfileImg()).isRead(false)
                                                    .event(String.format(NotificationEnum.ChallengeEnd.getContent(), o.getTitle()))
                                                            .link(String.format("/savana/challenge/%s",o.getId()))
                                                    .build()
                                            );
                    });
        });


    }

    @Override
    public String sessionToDisk(MultipartFile video) throws IOException {
        //copy video file
        String videoExt = video.getOriginalFilename().substring(video.getOriginalFilename().lastIndexOf("."));
        String videoTmpDir = UUID.randomUUID()+videoExt;
        Path videoTmp = Paths.get(challengeDir+videoTmpDir);
        Files.copy(video.getInputStream(), videoTmp);

        return videoTmpDir;
    }

    @Transactional
    @Async
    @Override
    public void createChallenge(UserDTO userDTO, ChallengeRequestDTO challengeRequestDTO, String videoTmpDir) {
        try {
            System.out.println("Creating Challenge!!!!!!!!!!!!!!!!!!\n"+LocalDateTime.now());
            User user = userRepository.getReferenceById(userDTO.getId());
            //토큰 잔액 확인
            if (ethereumService.getTokenAmount(user)<challengeRequestDTO.amount()) throw new Exception();
            // 원 챌린지 음악과 이미지 저장경로
            Challenge forChallenge = challengeRepository.getReferenceById(challengeRequestDTO.challengeId());
            CelebChallengeInfo celebChallengeInfo = celebChallengeInfoRepository.findByChallengeId(forChallenge.getId());
            String musicPath = celebChallengeInfo.getMusic();
            //make thumbnail
            String thumbDir = UUID.randomUUID()+".gif";
            String commandExtractThumbnail = String.format("ffmpeg -y -ss 2 -t 2 -i %s -r 10 -loop 0 %s", (challengeDir+videoTmpDir),(challengeDir+thumbDir));
            Process p = Runtime.getRuntime().exec(commandExtractThumbnail);
            System.out.println("thunbnail extracted!!!!!!!!!!\n"+LocalDateTime.now());
            p.waitFor();
            // wegM to MP4
            String mp4File = UUID.randomUUID() + ".mp4";
            p=Runtime.getRuntime().exec(String.format("ffmpeg -y -i %s %s",(challengeDir+videoTmpDir),(challengeDir+mp4File)));
            p.waitFor();
            System.out.println("video converted!!!!!!!!!!!!!!!!!!!!!\n"+LocalDateTime.now());
            // insert music
            String outputPath = UUID.randomUUID() + ".mp4";
            String commandInsertMusic = String.format("ffmpeg -y -i %s -i %s -map 0:v -map 1:a -c:v copy -shortest %s",
                    (challengeDir+mp4File),(challengeDir+musicPath),(challengeDir+outputPath));
            p = Runtime.getRuntime().exec(commandInsertMusic);
            p.waitFor();
            System.out.println("music inserted!!!!!!!!!!!!!!!!!!!!!\n"+LocalDateTime.now());
            // insert watermark
            String realOutput = UUID.randomUUID() + ".mp4";
            String commandInsertWatermark = String.format("ffmpeg -y -i %s -i %s -filter_complex [1][0]scale2ref=w=oh*mdar:h=ih*0.08[logo][video];[logo]format=argb,geq=r='r(X,Y)':a='0.7*alpha(X,Y)'[soo];[video][soo]overlay=30:30 -map v -map 0:a -c:v libx264 -preset ultrafast -r 23 %s"
                    , (challengeDir+outputPath), kirinStamp, (challengeDir+realOutput));
            p = Runtime.getRuntime().exec(commandInsertWatermark);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while((line=br.readLine())!=null) System.out.println(line);
            p.waitFor();
            System.out.println("Created Challenge!!!!!!!!!!!!!!!!!!\n"+LocalDateTime.now());
            ChallengeContract challengeContract = celebChallengeInfo.getChallengeContract();
            String transactionHash = ethereumService.fundToken(user, challengeContract.getContractHash(), challengeRequestDTO.amount());
            Challenge challenge = challengeRepository.save(
                    Challenge.builder().user(user).isProceeding(true).reg(LocalDateTime.now()).thumbnail(thumbDir)
                               .title(challengeRequestDTO.title()).isOriginal(false).challengeId(challengeRequestDTO.challengeId())
                               .video(realOutput).build()
            );
            Donation donation = Donation.builder()
                    .challenge(challenge)
                    .amount(challengeRequestDTO.amount())
                    .transactionHash(transactionHash)
                    .reg(LocalDateTime.now())
                    .build();
            donationRepository.save(donation);
            challengeContract.setAmount((long)ethereumService.getTokenAmount(user, challengeContract.getContractHash()));
            challengeContract.setParticipateNum(ethereumService.getParticipateNum(challengeContract.getContractHash(), user));
            challengeContractRepository.save(challengeContract);
            notificationService.addNotification(Notification.builder().userId(user.getId())
                                        .image(user.getProfileImg()).isRead(false).link(String.format("/savana/challenge/%s",challenge.getId()))
                    .event(String.format(NotificationEnum.ChallengeUploadCompleted.getContent(), challenge.getTitle())).build());

        } catch (InterruptedException e) {
            notificationService.addNotification(Notification.builder().isRead(false).event(String.format(NotificationEnum.ChallengeUploadFailed.getContent(), challengeRequestDTO.title()))
                    .link("/plus/0").userId(userDTO.getId()).image(userDTO.getProfileImg()).build());
            throw new RuntimeException(e);
        } catch (Exception e){
            notificationService.addNotification(Notification.builder().isRead(false).event(String.format(NotificationEnum.ChallengeUploadFailed.getContent(), challengeRequestDTO.title()))
                    .link("/plus/0").userId(userDTO.getId()).image(userDTO.getProfileImg()).build());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Async
    @Override
    public void createStarChallenge(UserDTO userDTO, StarChallengeRequestDTO starChallengeRequestDTO, String videoTmpDir) {
        try {
            User user = userRepository.getReferenceById(userDTO.getId());
            //토큰 충분한지 체크
            if (ethereumService.getTokenAmount(user)<starChallengeRequestDTO.targetAmount()) throw new Exception();

            DonationOrganization donationOrganization = donationOrganizationRepository.findById(starChallengeRequestDTO.donationOrganizationId()).get();
            //make thumbnail
            String thumbDir = UUID.randomUUID()+".gif";
            String commandExtractThumbnail = String.format("ffmpeg -y -ss 2 -t 2 -i %s -r 10 -loop 0 %s", challengeDir+videoTmpDir,challengeDir+thumbDir);
            Process p = Runtime.getRuntime().exec(commandExtractThumbnail);
            String line;
            StringBuilder sb = new StringBuilder();
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line=br.readLine())!=null) sb.append(line+"\n");
            p.waitFor();

            //extract music
            String musicDir = UUID.randomUUID()+".mp3";
            String commandExtractMusic = String.format("ffmpeg -i %s -q:a 0 -map a %s",challengeDir+videoTmpDir,challengeDir+musicDir);
            p = Runtime.getRuntime().exec(commandExtractMusic);
            p.waitFor();
            // insert watermark
            String videoDir = UUID.randomUUID()+".mp4";
            String commandWatermark = String.format("ffmpeg -y -i %s -i %s -filter_complex [1][0]scale2ref=w=oh*mdar:h=ih*0.08[logo][video];[logo]format=argb,geq=r='r(X,Y)':a='0.8*alpha(X,Y)'[soo];[video][soo]overlay=30:30 %s",
                    challengeDir+videoTmpDir, kirinStamp, challengeDir+videoDir);
            p = Runtime.getRuntime().exec(commandWatermark);
            sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while((line =br.readLine())!=null) sb.append(line+"\n");
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line=br.readLine())!=null) sb.append(line+"\n");
            br.close();
            System.out.println(sb);
            p.waitFor();


            //Contract 생성 및 토큰 전송, 트랜잭션 저장
            StarChallengeDTO starChallengeDTO = ethereumService.createFundContract(
                    user,
                    starChallengeRequestDTO.targetAmount(),
                    BigInteger.valueOf(Timestamp.valueOf(starChallengeRequestDTO.startDate()).getTime()/1000),
                    BigInteger.valueOf(Timestamp.valueOf(starChallengeRequestDTO.endDate()).getTime()/1000),
                    BigInteger.valueOf(starChallengeRequestDTO.targetNum()),
                    donationOrganization.getWallet().getAddress()
            );
            ChallengeContract challengeContract = starChallengeDTO.challengeContract;

            challengeContractRepository.save(challengeContract);

            Challenge challenge = Challenge.builder().user(user).video(videoDir)
                    .isProceeding(true).reg(LocalDateTime.now()).isOriginal(true).thumbnail(thumbDir)
                    .title(starChallengeRequestDTO.title()).build();

            challengeRepository.save(challenge);

            Donation donation = Donation.builder()
                    .amount(starChallengeRequestDTO.targetAmount())
                    .reg(LocalDateTime.now())
                    .transactionHash(starChallengeDTO.donationHash)
                    .challenge(challenge)
                    .build();
            donationRepository.save(donation);

            challenge.setChallengeId(challenge.getId());

            CelebChallengeInfo celebChallengeInfo = CelebChallengeInfo.builder().info(starChallengeRequestDTO.info()).challenge(challenge).targetAmount(starChallengeRequestDTO.targetAmount())
                    .targetNum(starChallengeRequestDTO.targetNum()).music(musicDir).musicTitle(starChallengeRequestDTO.musicTitle()).length(starChallengeRequestDTO.length())
                    .endDate(starChallengeRequestDTO.endDate()).startDate(starChallengeRequestDTO.startDate())
                    .donationOrganization(donationOrganization)
                    .challengeContract(challengeContract)
                    .build();

            celebChallengeInfoRepository.save(celebChallengeInfo);

            subscribeRepository.findByCelebId(userDTO.getId())
                            .forEach(o->notificationService.addNotification(Notification.builder().isRead(false).userId(o.getUserId())
                            .event(String.format(NotificationEnum.ChallengeUpload.getContent(), user.getNickname(),challenge.getTitle()))
                            .image(user.getProfileImg()).link(String.format("/savana/challenge/%s",o.getId())).build()));

        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ChallengeDTO mapChallengeDTO(Challenge challenge) {

        CelebChallengeInfo celebChallengeInfo = celebChallengeInfoRepository.findByChallengeId(challenge.getChallengeId());
        ChallengeDTO dto = ChallengeMapStruct.INSTANCE.mapToChallengeDTO(challenge,celebChallengeInfo);
        dto.setUser(UserMapStruct.INSTANCE.mapToUserDTO(challenge.getUser()));
        return dto;
    }

    public List<ChallengeDTO> challegeListToChallengDTOList(List<Challenge> challengeList){
        Map<Long,Long> celebChallengeDonationInfos = celebChallengeInfoRepository.findAll().stream()
                .collect(Collectors.toMap(o->o.getChallenge().getId(),d->d.getDonationOrganization().getId()));
        Map<Long,CelebChallengeInfo> celebChallengeInfos = celebChallengeInfoRepository.findAll().stream()
                .collect(Collectors.toMap(o->(o.getChallenge().getId()),o->o));
        Map<Long, String> donaOrganMap = donationOrganizationRepository.findAll().stream()
                .collect(Collectors.toMap(DonationOrganization::getId,DonationOrganization::getName));

        UserDTO userDTO = (UserDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userDTO!=null){
            System.out.println("challenge request for user is null");
            Set<Long> challengeIdSet = challengeLikeRepository.findByUserId(userDTO.getId())
                                .stream().map(o->o.getChallenge().getId())
                                .collect(Collectors.toSet());

            return challengeList.stream()
                    .map(o->{
                        ChallengeDTO dto = this.mapChallengeDTO(o);
                        CelebChallengeInfo celebChallengeInfo = celebChallengeInfos.get(dto.getChallengeId());
                        if(challengeIdSet.contains(o.getId())) dto.setLiked(true);
                        dto.setDonationOrganizationName(donaOrganMap.get(celebChallengeDonationInfos.get(o.getChallengeId())));
                        dto.setCurrentAmount(celebChallengeInfo.getCurrentAmount());
                        dto.setCurrentNum(celebChallengeInfo.getCurrentNum());
                        dto.setTargetAmount(celebChallengeInfo.getTargetAmount());
                        dto.setTargetNum(celebChallengeInfo.getTargetNum());
                        dto.setStartDate(celebChallengeInfo.getStartDate());
                        dto.setEndDate(celebChallengeInfo.getEndDate());
                        return dto;
                    })
                    .collect(Collectors.toList());

        }

        return challengeList.stream()
                .map(o->{
                        ChallengeDTO dto = this.mapChallengeDTO(o);
                        dto.setDonationOrganizationName(donaOrganMap.get(celebChallengeDonationInfos.get(o.getChallengeId())));
                        return dto;
                })
                .collect(Collectors.toList());
    }
}
