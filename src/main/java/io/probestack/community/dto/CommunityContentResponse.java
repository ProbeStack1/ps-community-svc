package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CommunityContentResponse {
    private String id;
    private String contentType;
    private String category;
    private String title;
    private String body;
    private String summary;
    private String type;
    private String level;
    private String status;
    private String eventDate;
    private String resourceUrl;
    private List<String> tags;
    private boolean pinned;
    private boolean official;
    private boolean featured;
    private boolean trending;
    private boolean accepted;
    private String authorUserId;
    private String authorEmail;
    private String authorName;
    private String authorRole;
    private int votes;
    private int likes;
    private int replies;
    private int answers;
    private int comments;
    private int installs;
    private int attendees;
    private Instant createdAt;
    private Instant updatedAt;
}
