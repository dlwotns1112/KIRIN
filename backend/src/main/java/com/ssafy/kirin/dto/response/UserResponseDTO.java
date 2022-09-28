package com.ssafy.kirin.dto.response;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserResponseDTO {
    Long id;
    String nickname;
    String profileImg;
}
