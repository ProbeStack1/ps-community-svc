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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "community_content")
@CompoundIndex(name = "content_type_category_idx", def = "{'contentType': 1, 'category': 1, 'updatedAt': -1}")
@CompoundIndex(name = "content_type_status_idx", def = "{'contentType': 1, 'status': 1, 'updatedAt': -1}")
public class CommunityContent {

    @Id
    private String id;

    @Indexed
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

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private boolean pinned;
    private boolean official;
    private boolean featured;
    private boolean trending;
    private boolean accepted;

    private String createdByUserId;
    private String createdByEmail;
    private String createdByName;
    private String createdByRole;

    @Builder.Default
    private int votes = 0;

    @Builder.Default
    private int likes = 0;

    @Builder.Default
    private int replies = 0;

    @Builder.Default
    private int answers = 0;

    @Builder.Default
    private int comments = 0;

    @Builder.Default
    private int installs = 0;

    @Builder.Default
    private int attendees = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
