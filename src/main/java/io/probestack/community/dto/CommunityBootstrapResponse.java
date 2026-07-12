package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommunityBootstrapResponse {
    private List<CommunityContentResponse> announcements;
    private List<CommunityContentResponse> discussions;
    private List<CommunityContentResponse> questions;
    private List<CommunityContentResponse> ideas;
    private List<CommunityContentResponse> learning;
    private List<CommunityContentResponse> marketplace;
    private List<CommunityContentResponse> events;
    private List<LeaderboardEntry> leaderboard;
}
