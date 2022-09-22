package com.ssafy.kirin.service;

import com.ssafy.kirin.dto.UserDTO;
import com.ssafy.kirin.dto.request.EmailAuthRequestDTO;
import com.ssafy.kirin.dto.request.UserLoginRequestDTO;
import com.ssafy.kirin.dto.request.UserSignupRequestDTO;
import com.ssafy.kirin.entity.CelebInfo;
import com.ssafy.kirin.entity.EmailAuth;
import com.ssafy.kirin.entity.Subscribe;
import com.ssafy.kirin.entity.User;
import com.ssafy.kirin.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserDetailsService, UserService {
    private final UserRepository userRepository;
    private final CelebInfoRepository celebInfoRepository;
    private final EmailAuthRepository emailAuthRepository;
    private final EmailAuthRepositoryCustom emailAuthRepositoryCustom;
    private final EmailService emailService;
    private final SubscribeRepository subscribeRepository;


    @Override
    public void signup(UserSignupRequestDTO userSignupRequestDTO, PasswordEncoder passwordEncoder) throws Exception {
        User user = null;

        // 일반 회원가입인 경우 : email, password null check && email, nickname 중복 check -> 스타, 일반인
        // 소셜 회원가입인 경우 : socialId null check && nickname 중복 check -> 일반인
//        if(userSignupRequestDTO.getAccountType() == 0){ // 일반 회원가입 accountType:0
            log.info("일반 회원가입");
            if(userSignupRequestDTO.getEmail() == null || userSignupRequestDTO.getPassword() == null){ // email, password null check
                log.info("email, password null");
                throw new Exception();
            }

            if(userRepository.existsByEmail(userSignupRequestDTO.getEmail()) || userRepository.existsByNickname(userSignupRequestDTO.getNickname())){ // email,nickname 중복 check
                log.info("email, nickname 중복");
                throw new Exception();
            }

            userSignupRequestDTO.setPassword(passwordEncoder.encode(userSignupRequestDTO.getPassword()));

            if(userSignupRequestDTO.getIsCeleb()){ // 스타일 경우
                log.info("스타 회원가입");
                System.out.println("회원가입 userSignupRequestDTO: " + userSignupRequestDTO);

                CelebInfo celebInfo = CelebInfo.builder()
                        .coverImg(userSignupRequestDTO.getCoverImg())
                        .info(userSignupRequestDTO.getInfo())
                        .build();

                user = User.builder()
                        .name(userSignupRequestDTO.getName())
                        .nickname(userSignupRequestDTO.getNickname())
                        .profileImg(userSignupRequestDTO.getProfileImg()) // null일수도
                        .email(userSignupRequestDTO.getEmail())
                        .password(userSignupRequestDTO.getPassword())
//                        .accountType(userSignupRequestDTO.getAccountType())
                        .isCeleb(userSignupRequestDTO.getIsCeleb())
                        .walletId(userSignupRequestDTO.getWalletId())
                        .reg(LocalDateTime.now())
                        .build();

                user.setCelebInfo(celebInfoRepository.save(celebInfo));
            } else { // 일반인인 경우
                log.info("일반인 회원가입");
                System.out.println("회원가입 userSignupRequestDTO: " + userSignupRequestDTO);

                user = User.builder()
                        .name(userSignupRequestDTO.getName())
                        .nickname(userSignupRequestDTO.getNickname())
                        .profileImg(userSignupRequestDTO.getProfileImg()) // null일수도
                        .email(userSignupRequestDTO.getEmail())
                        .password(userSignupRequestDTO.getPassword())
//                        .accountType(userSignupRequestDTO.getAccountType())
                        .walletId(userSignupRequestDTO.getWalletId())
                        .isCeleb(userSignupRequestDTO.getIsCeleb())
                        .reg(LocalDateTime.now())
                        .build();
            }
            userRepository.save(user);

            // 이메일 verify 확인
            EmailAuth emailAuth = emailAuthRepository.save(
                    new EmailAuth(userSignupRequestDTO.getEmail(), UUID.randomUUID().toString(), false)
            );

            emailService.sendVerifyMail(emailAuth.getEmail(), emailAuth.getAuthToken()); // 이메일 인증 메일 보내기
//        }
//        else if(userSignupRequestDTO.getAccountType() == 1){ // 카카오 소셜 회원가입 accountType:1
//            log.info("카카오 소셜 회원가입");
//            if(userSignupRequestDTO.getSocialId() == null){ // socialId null check
//                System.out.println("socialId null");
//                throw new Exception();
//            }
//
//            if(userRepository.existsByNickname(userSignupRequestDTO.getNickname())){ // nickname 중복 check
//                log.info("nickname 중복");
//                throw new Exception();
//            }
//
//            if(!userSignupRequestDTO.getIsCeleb()){ // 일반인만
//                log.info("일반인 회원가입");
//                user = User.builder()
//                        .name(userSignupRequestDTO.getName())
//                        .nickname(userSignupRequestDTO.getNickname())
//                        .profileImg(userSignupRequestDTO.getProfileImg()) // null일수도
//                        .socialId(userSignupRequestDTO.getSocialId())
//                        .accountType(userSignupRequestDTO.getAccountType())
//                        .isCeleb(userSignupRequestDTO.getIsCeleb())
//                        .reg(LocalDateTime.now())
//                        .build();
//
//                userRepository.save(user);
//            } else { // 스타는 소셜 회원가입 X
//                throw new Exception();
//            }
//        }
    }

    @Override
    public void confirmEmail(String email, String authToken) {
        EmailAuth emailAuth = emailAuthRepositoryCustom.findValidAuthByEmail(email, authToken, LocalDateTime.now())
                .orElseThrow(() -> new NoSuchElementException("EmailAuth : " + email + " was not found"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User : " + email + " was not found"));
        emailAuth.useToken();
        user.emailVerifiedSuccess();
        emailAuthRepository.save(emailAuth);
        userRepository.save(user);
    }

    @Override
    public UserDTO login(UserLoginRequestDTO userLoginRequestDTO, PasswordEncoder passwordEncoder) {
        System.out.println("login service: " + userLoginRequestDTO);

        User user = userRepository.findByEmail(userLoginRequestDTO.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User : " + userLoginRequestDTO.getEmail() + " was not found"));

        if(!user.getIsEmailVerified()){ // 이메일 인증이 안된 경우
            log.error("login 오류: 이메일 인증 안됨");
            return null;
        }

        if(user.getIsCeleb() && !user.getIsCelebVerified()){ // 스타 계정이지만 스타 인증이 안된 경우
            log.error("login 오류: 스타 인증 안됨");
            return null;
        }

        if(!passwordEncoder.matches(userLoginRequestDTO.getPassword(), user.getPassword())){ // 비밀번호가 일치하지 않는 경우
            log.error("login 오류: 비밀번호 틀림");
            return null;
        }

//        if(user.getIsCeleb()){ // 스타인 경우
////            CelebInfo celebInfo = celebInfoRepository.findById(user.getCelebInfo().getId())
////                    .orElseThrow(() -> new NoSuchElementException("Login Celeb info : " + user.getCelebInfo().getId() + " was not found"));
//
//            return userToCelebDto(user);
//        }
//
//        // 일반인인 경우
        return userToUserDto(user); // 일반인인 경우, 스타인 경우 모두 포함
    }

    @Override
    public UserDTO modifyUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("User : " + userDTO.getId() + " was not found"));

        user.setProfileImgAndNickname(userDTO.getProfileImg(), userDTO.getNickname());

        if(user.getIsCeleb()){ // 스타인 경우, celebInfo update
            user.getCelebInfo().setInfo(userDTO.getInfo());
            celebInfoRepository.save(user.getCelebInfo());
        }

        userRepository.save(user); // user update

        return userToUserDto(user);
    }

    @Override
    public UserDTO getUserById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User : " + userId + " was not found"));

//        if(user.getIsCeleb()){ // 스타인 경우
//            return userToCelebDto(user);
//        }
//
//        // 일반인인 경우
        return userToUserDto(user);
    }

    @Override
    public void subscribe(long userId, long celebId) {
        // user가 스타가 아니고, celeb이 스타인지 확인을 해야 할까?
        User celeb = userRepository.getReferenceById(celebId);

        Subscribe subscribe = Subscribe.builder()
                .userId(userId)
                .celeb(celeb)
                .build();

        subscribeRepository.save(subscribe);
    }

    @Override
    public List<UserDTO> getCelebListById(long userId) {
        List<Subscribe> subscribes = subscribeRepository.findByUserId(userId);
        List<UserDTO> result = new ArrayList<>();

        for(Subscribe subscribe: subscribes){
            UserDTO userDTO = userToUserDto(subscribe.getCeleb());
            result.add(userDTO);
        }

        return result;
    }

    @Override
    public boolean checkEmailDuplicate(String email) {
        boolean isEmailDupliated = userRepository.existsByEmail(email);

        if(isEmailDupliated) return false;

        return true;
    }

    @Override
    public boolean checkNicknameDuplicate(String nickname) {
        boolean isNicknameDupliated = userRepository.existsByNickname(nickname);

        if(isNicknameDupliated) return false;

        return true;
    }

    @Override
    public UserDTO loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserDTO userDTO = getUserById(Long.parseLong(userId));
        // 일반인, 스타 role 지정

        return userDTO;
    }

    @Transactional(readOnly = true)
    public Map<String, String> validateHandling(Errors errors){
        Map<String, String> validatorResult = new HashMap<>();

        // 유효성 검사에 실패한 필드 목록을 받음
        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }

        return validatorResult;
    }

    private UserDTO userToUserDto(User user){
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
//                .accountType(user.getAccountType())
                .isCeleb(user.getIsCeleb())
                .info(user.getCelebInfo() != null ? user.getCelebInfo().getInfo() : null)
                .coverImg(user.getCelebInfo() != null ? user.getCelebInfo().getCoverImg() : null)
                .build();
    }
}