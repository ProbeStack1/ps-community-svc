package io.probestack.community.repository;

import io.probestack.community.model.CommunityContent;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface CommunityContentRepository extends MongoRepository<CommunityContent, String> {
    List<CommunityContent> findByContentTypeOrderByPinnedDescUpdatedAtDesc(String contentType);
    List<CommunityContent> findByContentTypeInOrderByPinnedDescUpdatedAtDesc(Collection<String> contentTypes);
    List<CommunityContent> findByContentTypeAndCategoryOrderByPinnedDescUpdatedAtDesc(String contentType, String category);
    List<CommunityContent> findByContentTypeAndStatusOrderByPinnedDescUpdatedAtDesc(String contentType, String status);
    List<CommunityContent> findTop5ByContentTypeOrderByPinnedDescUpdatedAtDesc(String contentType);
    List<CommunityContent> findTop5ByContentTypeAndTrendingTrueOrderByUpdatedAtDesc(String contentType);
    long countByContentType(String contentType);
}
