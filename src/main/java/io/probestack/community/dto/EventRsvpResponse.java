package io.probestack.community.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventRsvpResponse {
    private String eventId;
    private boolean rsvped;
    private int attendees;
}
