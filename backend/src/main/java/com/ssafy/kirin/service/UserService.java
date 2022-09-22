package com.ssafy.kirin.service;

import com.ssafy.kirin.dto.UserDTO;
import com.ssafy.kirin.dto.request.UserLoginRequestDTO;
import com.ssafy.kirin.dto.request.UserSignupRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {
    void signup(UserSignupRequestDTO userSignupRequestDTO, MultipartFile profileImg, MultipartFile coverImg, PasswordEncoder passwordEncoder) throws Exception;

    void confirmEmail(String email, String authToken);

    UserDTO login(UserLoginRequestDTO userLoginRequestDTO, PasswordEncoder passwordEncoder);

    UserDTO modifyUser(UserDTO userDTO, MultipartFile profileImg) throws IOException;

    UserDTO getUserById(long userId);

    void subscribe(long userId, long celebId);

    List<UserDTO> getCelebListById(long userId);

    boolean checkEmailDuplicate(String email);

    boolean checkNicknameDuplicate(String nickname);

    Map<String, String> validateHandling(Errors errors);
}
