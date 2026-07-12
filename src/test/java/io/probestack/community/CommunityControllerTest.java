package io.probestack.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.probestack.community.dto.CommunityContentRequest;
import io.probestack.community.dto.CommunityContentResponse;
import io.probestack.community.dto.CommunitySummaryResponse;
import io.probestack.community.dto.CommunityVoteResponse;
import io.probestack.community.dto.EventRsvpResponse;
import io.probestack.community.service.CommunityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    @Test
    void getSummary_returnsCommunitySummary() throws Exception {
        CommunitySummaryResponse summary = CommunitySummaryResponse.builder()
                .members(3)
                .discussions(8)
                .questions(4)
                .answeredPercentage(75)
                .trendingDiscussions(List.of())
                .featuredLearning(List.of())
                .upcomingEvents(List.of())
                .champions(List.of())
                .build();

        when(communityService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/community/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.members").value(3))
                .andExpect(jsonPath("$.data.answeredPercentage").value(75));
    }

    @Test
    void createDiscussion_validPayload_returns201() throws Exception {
        CommunityContentRequest request = new CommunityContentRequest();
        request.setTitle("Best practices for API versioning?");
        request.setBody("What are teams doing in production?");
        request.setCategory("practices");
        request.setTags(List.of("API Design", "Best Practices"));

        when(communityService.createContent(eq(CommunityService.DISCUSSION), any(), any()))
                .thenReturn(discussion());

        mockMvc.perform(post("/api/v1/community/discussions")
                        .header("X-User-Email", "khitish@example.com")
                        .header("X-User-Name", "Khitish Mangal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("discussion-id"))
                .andExpect(jsonPath("$.data.authorEmail").value("khitish@example.com"));
    }

    @Test
    void voteIdea_validActor_returns200() throws Exception {
        when(communityService.vote(eq(CommunityService.IDEA), eq("idea-id"), any(), any()))
                .thenReturn(CommunityVoteResponse.builder()
                        .contentId("idea-id")
                        .contentType(CommunityService.IDEA)
                        .voteType("VOTE")
                        .voted(true)
                        .votes(10)
                        .likes(0)
                        .build());

        mockMvc.perform(post("/api/v1/community/ideas/idea-id/vote")
                        .header("X-User-Email", "khitish@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.voted").value(true))
                .andExpect(jsonPath("$.data.votes").value(10));
    }

    @Test
    void rsvpEvent_validActor_returns200() throws Exception {
        when(communityService.rsvp(eq("event-id"), any(), any()))
                .thenReturn(EventRsvpResponse.builder()
                        .eventId("event-id")
                        .rsvped(true)
                        .attendees(42)
                        .build());

        mockMvc.perform(post("/api/v1/community/events/event-id/rsvp")
                        .header("X-User-Email", "khitish@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rsvped").value(true))
                .andExpect(jsonPath("$.data.attendees").value(42));
    }

    private CommunityContentResponse discussion() {
        return CommunityContentResponse.builder()
                .id("discussion-id")
                .contentType(CommunityService.DISCUSSION)
                .category("practices")
                .title("Best practices for API versioning?")
                .body("What are teams doing in production?")
                .authorEmail("khitish@example.com")
                .authorName("Khitish Mangal")
                .authorRole("USER")
                .replies(0)
                .likes(0)
                .createdAt(Instant.parse("2026-07-09T10:00:00Z"))
                .updatedAt(Instant.parse("2026-07-09T10:00:00Z"))
                .build();
    }
}
