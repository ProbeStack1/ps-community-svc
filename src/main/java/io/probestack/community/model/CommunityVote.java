package io.probestack.community.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "community_votes")
@CompoundIndex(name = "content_user_vote_unique_idx", def = "{'contentId': 1, 'userKey': 1, 'voteType': 1}", unique = true)
public class CommunityVote {

    @Id
    private String id;

    @Indexed
    private String contentId;

    private String contentType;
    private String userKey;
    private String voteType;

    @CreatedDate
    private Instant createdAt;
}
