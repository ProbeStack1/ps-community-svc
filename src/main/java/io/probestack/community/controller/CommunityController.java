package io.probestack.community.controller;

import io.probestack.community.dto.ActorDTO;
import io.probestack.community.dto.AiChatRequest;
import io.probestack.community.dto.AiChatResponse;
import io.probestack.community.dto.ApiResponse;
import io.probestack.community.dto.CommunityBootstrapResponse;
import io.probestack.community.dto.CommunityCommentRequest;
import io.probestack.community.dto.CommunityCommentResponse;
import io.probestack.community.dto.CommunityContentRequest;
import io.probestack.community.dto.CommunityContentResponse;
import io.probestack.community.dto.CommunitySummaryResponse;
import io.probestack.community.dto.CommunityVoteResponse;
import io.probestack.community.dto.EventRsvpResponse;
import io.probestack.community.dto.LeaderboardEntry;
import io.probestack.community.service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/community")
@CrossOrigin(origins = "*")
public class CommunityController {

    private static final Map<String, String> COLLECTION_TYPES = Map.of(
            "discussions", CommunityService.DISCUSSION,
            "questions", CommunityService.QUESTION,
            "ideas", CommunityService.IDEA,
            "announcements", CommunityService.ANNOUNCEMENT,
            "learning", CommunityService.LEARNING,
            "marketplace", CommunityService.MARKETPLACE,
            "events", CommunityService.EVENT
    );

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CommunitySummaryResponse>> getSummary() {
        return ResponseEntity.ok(success("Community summary fetched successfully", communityService.getSummary()));
    }

    @GetMapping("/bootstrap")
    public ResponseEntity<ApiResponse<CommunityBootstrapResponse>> getBootstrap() {
        return ResponseEntity.ok(success("Community bootstrap fetched successfully", communityService.getBootstrap()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CommunityContentResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(success("Community search completed successfully", communityService.search(q)));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard() {
        return ResponseEntity.ok(success("Community leaderboard fetched successfully", communityService.getLeaderboard()));
    }

    @PostMapping("/ai/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(success("Community assistant response generated successfully", communityService.chat(request.getMessage())));
    }

    @GetMapping("/{collection}")
    public ResponseEntity<ApiResponse<List<CommunityContentResponse>>> listContent(
            @PathVariable String collection,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "q") String query) {
        return ResponseEntity.ok(success("Community content fetched successfully",
                communityService.listContent(contentType(collection), category, status, query)));
    }

    @PostMapping("/{collection}")
    public ResponseEntity<ApiResponse<CommunityContentResponse>> createContent(
            @PathVariable String collection,
            @Valid @RequestBody CommunityContentRequest request,
            HttpServletRequest httpRequest) {
        CommunityContentResponse content = communityService.createContent(contentType(collection), request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(success("Community content created successfully", content));
    }

    @GetMapping("/{collection}/{id}")
    public ResponseEntity<ApiResponse<CommunityContentResponse>> getContent(
            @PathVariable String collection,
            @PathVariable String id) {
        return ResponseEntity.ok(success("Community content fetched successfully",
                communityService.getContent(contentType(collection), id)));
    }

    @PatchMapping("/{collection}/{id}")
    public ResponseEntity<ApiResponse<CommunityContentResponse>> updateContent(
            @PathVariable String collection,
            @PathVariable String id,
            @RequestBody CommunityContentRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(success("Community content updated successfully",
                communityService.updateContent(contentType(collection), id, request, httpRequest)));
    }

    @DeleteMapping("/{collection}/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable String collection,
            @PathVariable String id,
            @RequestBody(required = false) ActorDTO actor,
            HttpServletRequest httpRequest) {
        communityService.deleteContent(contentType(collection), id, actor, httpRequest);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("SUCCESS")
                .message("Community content deleted successfully")
                .build());
    }

    @PostMapping("/{collection}/{id}/vote")
    public ResponseEntity<ApiResponse<CommunityVoteResponse>> vote(
            @PathVariable String collection,
            @PathVariable String id,
            @RequestBody(required = false) ActorDTO actor,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(success("Community vote recorded successfully",
                communityService.vote(contentType(collection), id, actor, httpRequest)));
    }

    @GetMapping("/{collection}/{id}/comments")
    public ResponseEntity<ApiResponse<List<CommunityCommentResponse>>> getComments(
            @PathVariable String collection,
            @PathVariable String id) {
        return ResponseEntity.ok(success("Community comments fetched successfully",
                communityService.getComments(contentType(collection), id)));
    }

    @PostMapping("/{collection}/{id}/comments")
    public ResponseEntity<ApiResponse<CommunityCommentResponse>> addComment(
            @PathVariable String collection,
            @PathVariable String id,
            @Valid @RequestBody CommunityCommentRequest request,
            HttpServletRequest httpRequest) {
        CommunityCommentResponse comment = communityService.addComment(contentType(collection), id, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(success("Community comment added successfully", comment));
    }

    @PostMapping("/events/{id}/rsvp")
    public ResponseEntity<ApiResponse<EventRsvpResponse>> rsvp(
            @PathVariable String id,
            @RequestBody(required = false) ActorDTO actor,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(success("Event RSVP recorded successfully", communityService.rsvp(id, actor, httpRequest)));
    }

    @DeleteMapping("/events/{id}/rsvp")
    public ResponseEntity<ApiResponse<EventRsvpResponse>> cancelRsvp(
            @PathVariable String id,
            @RequestBody(required = false) ActorDTO actor,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(success("Event RSVP cancelled successfully", communityService.cancelRsvp(id, actor, httpRequest)));
    }

    private String contentType(String collection) {
        String type = COLLECTION_TYPES.get(collection);
        if (type == null) {
            throw new IllegalArgumentException("Unsupported community collection: " + collection);
        }
        return type;
    }

    private <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }
}
