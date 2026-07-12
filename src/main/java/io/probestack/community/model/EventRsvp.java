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
@Document(collection = "community_event_rsvps")
@CompoundIndex(name = "event_user_unique_idx", def = "{'eventId': 1, 'userKey': 1}", unique = true)
public class EventRsvp {

    @Id
    private String id;

    @Indexed
    private String eventId;

    private String userKey;
    private String attendeeEmail;
    private String attendeeName;
    private String attendeeUserId;

    @CreatedDate
    private Instant createdAt;
}
