package io.probestack.community.repository;

import io.probestack.community.model.CommunityComment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommunityCommentRepository extends MongoRepository<CommunityComment, String> {
    List<CommunityComment> findByContentIdOrderByCreatedAtAsc(String contentId);
    long countByContentId(String contentId);
    long countByAuthorEmail(String authorEmail);
}
