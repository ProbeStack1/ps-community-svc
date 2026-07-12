package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntry {
    private int rank;
    private String name;
    private String email;
    private int points;
    private String badge;
    private int contributions;
}
