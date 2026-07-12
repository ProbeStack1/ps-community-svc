package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommunitySummaryResponse {
    private long members;
    private long discussions;
    private long questions;
    private int answeredPercentage;
    private CommunityContentResponse latestAnnouncement;
    private List<CommunityContentResponse> trendingDiscussions;
    private List<CommunityContentResponse> featuredLearning;
    private List<CommunityContentResponse> upcomingEvents;
    private List<LeaderboardEntry> champions;
}
