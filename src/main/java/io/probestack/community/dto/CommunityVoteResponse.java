package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommunityVoteResponse {
    private String contentId;
    private String contentType;
    private String voteType;
    private boolean voted;
    private int votes;
    private int likes;
}
