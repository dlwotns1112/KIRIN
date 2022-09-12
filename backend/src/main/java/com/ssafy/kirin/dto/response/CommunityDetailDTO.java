package com.ssafy.kirin.dto.response;

import com.ssafy.kirin.entity.Community;
import com.ssafy.kirin.entity.CommunityComment;

import java.util.List;

public record CommunityDetailDTO(Community community, List<CommunityComment> commentList) {
}
