package io.probestack.community.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "community_comments")
@CompoundIndex(name = "comment_content_idx", def = "{'contentId': 1, 'createdAt': 1}")
public class CommunityComment {

    @Id
    private String id;

    @Indexed
    private String contentId;

    private String contentType;
    private String parentCommentId;
    private String body;
    private boolean accepted;

    private String authorUserId;
    private String authorEmail;
    private String authorName;
    private String authorRole;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
