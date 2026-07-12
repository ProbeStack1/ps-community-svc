package io.probestack.community.repository;

import io.probestack.community.model.EventRsvp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventRsvpRepository extends MongoRepository<EventRsvp, String> {
    Optional<EventRsvp> findByEventIdAndUserKey(String eventId, String userKey);
    long countByEventId(String eventId);
    void deleteByEventIdAndUserKey(String eventId, String userKey);
}
