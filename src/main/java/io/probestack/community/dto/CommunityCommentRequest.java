package io.probestack.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommunityCommentRequest {

    @NotBlank(message = "Comment is required")
    private String body;

    private String parentCommentId;
    private Boolean accepted;
    private ActorDTO actor;
}
