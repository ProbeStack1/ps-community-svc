package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiChatResponse {
    private String answer;
    private List<CommunityContentResponse> relatedContent;
}
