package io.probestack.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CommunityContentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String body;
    private String summary;
    private String category;
    private String type;
    private String level;
    private String status;
    private String eventDate;
    private String resourceUrl;
    private List<String> tags = new ArrayList<>();

    private Boolean pinned;
    private Boolean official;
    private Boolean featured;
    private Boolean trending;
    private Boolean accepted;

    private ActorDTO actor;
}
