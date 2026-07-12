package io.probestack.community.repository;

import io.probestack.community.model.CommunityVote;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CommunityVoteRepository extends MongoRepository<CommunityVote, String> {
    Optional<CommunityVote> findByContentIdAndUserKeyAndVoteType(String contentId, String userKey, String voteType);
    long countByContentIdAndVoteType(String contentId, String voteType);
    long countByContentId(String contentId);
}
