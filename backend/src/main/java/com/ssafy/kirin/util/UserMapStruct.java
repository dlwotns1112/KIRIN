package com.ssafy.kirin.util;

import com.ssafy.kirin.dto.UserDTO;
import com.ssafy.kirin.dto.response.UserResponseDTO;
import com.ssafy.kirin.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapStruct {
    UserMapStruct INSTANCE = Mappers.getMapper(UserMapStruct.class);
    UserResponseDTO mapToUserDTO(User user);
}
