package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CommunityCommentResponse {
    private String id;
    private String contentId;
    private String contentType;
    private String parentCommentId;
    private String body;
    private boolean accepted;
    private String authorUserId;
    private String authorEmail;
    private String authorName;
    private String authorRole;
    private Instant createdAt;
    private Instant updatedAt;
}
