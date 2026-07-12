package io.probestack.community.service;

import io.probestack.community.dto.ActorDTO;
import io.probestack.community.dto.AiChatResponse;
import io.probestack.community.dto.CommunityCommentRequest;
import io.probestack.community.dto.CommunityCommentResponse;
import io.probestack.community.dto.CommunityBootstrapResponse;
import io.probestack.community.dto.CommunityContentRequest;
import io.probestack.community.dto.CommunityContentResponse;
import io.probestack.community.dto.CommunitySummaryResponse;
import io.probestack.community.dto.CommunityVoteResponse;
import io.probestack.community.dto.EventRsvpResponse;
import io.probestack.community.dto.LeaderboardEntry;
import io.probestack.community.exception.ForbiddenOperationException;
import io.probestack.community.exception.ResourceNotFoundException;
import io.probestack.community.model.CommunityComment;
import io.probestack.community.model.CommunityContent;
import io.probestack.community.model.CommunityVote;
import io.probestack.community.model.EventRsvp;
import io.probestack.community.repository.CommunityCommentRepository;
import io.probestack.community.repository.CommunityContentRepository;
import io.probestack.community.repository.CommunityVoteRepository;
import io.probestack.community.repository.EventRsvpRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommunityService {

    public static final String DISCUSSION = "DISCUSSION";
    public static final String QUESTION = "QUESTION";
    public static final String IDEA = "IDEA";
    public static final String ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String LEARNING = "LEARNING";
    public static final String MARKETPLACE = "MARKETPLACE";
    public static final String EVENT = "EVENT";

    private static final Set<String> ADMIN_CREATED_TYPES = Set.of(ANNOUNCEMENT, LEARNING, EVENT);
    private static final Set<String> VALID_IDEA_STATUSES = Set.of("Under Review", "Planned", "Shipped");
    private static final Set<String> VALID_MARKETPLACE_STATUSES = Set.of("Pending Review", "Approved", "Rejected");

    private final CommunityContentRepository contentRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityVoteRepository voteRepository;
    private final EventRsvpRepository rsvpRepository;

    public CommunityService(CommunityContentRepository contentRepository,
                            CommunityCommentRepository commentRepository,
                            CommunityVoteRepository voteRepository,
                            EventRsvpRepository rsvpRepository) {
        this.contentRepository = contentRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.rsvpRepository = rsvpRepository;
    }

    public List<CommunityContentResponse> listContent(String contentType, String category, String status, String query) {
        List<CommunityContent> items;
        if (StringUtils.hasText(category)) {
            items = contentRepository.findByContentTypeAndCategoryOrderByPinnedDescUpdatedAtDesc(contentType, category.trim());
        } else if (StringUtils.hasText(status)) {
            items = contentRepository.findByContentTypeAndStatusOrderByPinnedDescUpdatedAtDesc(contentType, status.trim());
        } else {
            items = contentRepository.findByContentTypeOrderByPinnedDescUpdatedAtDesc(contentType);
        }

        return filterByQuery(items, query).stream().map(this::toResponse).toList();
    }

    public CommunityBootstrapResponse getBootstrap() {
        List<String> contentTypes = List.of(DISCUSSION, QUESTION, IDEA, ANNOUNCEMENT, LEARNING, MARKETPLACE, EVENT);
        Map<String, List<CommunityContentResponse>> grouped = new HashMap<>();
        contentTypes.forEach(type -> grouped.put(type, new ArrayList<>()));

        contentRepository.findByContentTypeInOrderByPinnedDescUpdatedAtDesc(contentTypes)
                .forEach(content -> grouped.computeIfAbsent(content.getContentType(), ignored -> new ArrayList<>()).add(toResponse(content)));

        return CommunityBootstrapResponse.builder()
                .announcements(grouped.getOrDefault(ANNOUNCEMENT, List.of()))
                .discussions(grouped.getOrDefault(DISCUSSION, List.of()))
                .questions(grouped.getOrDefault(QUESTION, List.of()))
                .ideas(grouped.getOrDefault(IDEA, List.of()))
                .learning(grouped.getOrDefault(LEARNING, List.of()))
                .marketplace(grouped.getOrDefault(MARKETPLACE, List.of()))
                .events(grouped.getOrDefault(EVENT, List.of()))
                .leaderboard(getLeaderboard())
                .build();
    }

    public CommunityContentResponse getContent(String contentType, String id) {
        return toResponse(findContent(contentType, id));
    }

    public CommunityContentResponse createContent(String contentType, CommunityContentRequest request, HttpServletRequest httpRequest) {
        Actor actor = requireActor(request.getActor(), httpRequest);
        boolean admin = isAdmin(actor.role());
        if (ADMIN_CREATED_TYPES.contains(contentType) && !admin) {
            throw new ForbiddenOperationException("Only admins or moderators can create this content type");
        }

        CommunityContent content = CommunityContent.builder()
                .contentType(contentType)
                .category(defaultIfBlank(request.getCategory(), defaultCategory(contentType)))
                .title(request.getTitle().trim())
                .body(trimToNull(request.getBody()))
                .summary(trimToNull(request.getSummary()))
                .type(defaultIfBlank(request.getType(), defaultSubtype(contentType)))
                .level(trimToNull(request.getLevel()))
                .status(defaultStatus(contentType, request.getStatus(), admin))
                .eventDate(trimToNull(request.getEventDate()))
                .resourceUrl(trimToNull(request.getResourceUrl()))
                .tags(cleanTags(request.getTags()))
                .pinned(Boolean.TRUE.equals(request.getPinned()) && admin)
                .official(Boolean.TRUE.equals(request.getOfficial()) && admin)
                .featured(Boolean.TRUE.equals(request.getFeatured()) && admin)
                .trending(Boolean.TRUE.equals(request.getTrending()) && admin)
                .accepted(Boolean.TRUE.equals(request.getAccepted()) && admin)
                .createdByUserId(actor.userId())
                .createdByEmail(actor.email())
                .createdByName(actor.displayName())
                .createdByRole(actor.role())
                .build();

        return toResponse(contentRepository.save(content));
    }

    public CommunityContentResponse updateContent(String contentType, String id, CommunityContentRequest request, HttpServletRequest httpRequest) {
        CommunityContent content = findContent(contentType, id);
        Actor actor = requireActor(request.getActor(), httpRequest);
        boolean admin = isAdmin(actor.role());
        requireOwnerOrAdmin(content, actor, admin);

        if (StringUtils.hasText(request.getTitle())) content.setTitle(request.getTitle().trim());
        if (request.getBody() != null) content.setBody(trimToNull(request.getBody()));
        if (request.getSummary() != null) content.setSummary(trimToNull(request.getSummary()));
        if (request.getCategory() != null) content.setCategory(defaultIfBlank(request.getCategory(), defaultCategory(contentType)));
        if (request.getType() != null) content.setType(defaultIfBlank(request.getType(), defaultSubtype(contentType)));
        if (request.getLevel() != null) content.setLevel(trimToNull(request.getLevel()));
        if (request.getEventDate() != null) content.setEventDate(trimToNull(request.getEventDate()));
        if (request.getResourceUrl() != null) content.setResourceUrl(trimToNull(request.getResourceUrl()));
        if (request.getTags() != null) content.setTags(cleanTags(request.getTags()));

        if (request.getStatus() != null) {
            if ((IDEA.equals(contentType) || MARKETPLACE.equals(contentType)) && !admin) {
                throw new ForbiddenOperationException("Only admins or moderators can change status");
            }
            content.setStatus(validateStatus(contentType, request.getStatus()));
        }
        if (request.getPinned() != null || request.getOfficial() != null || request.getFeatured() != null || request.getTrending() != null || request.getAccepted() != null) {
            if (!admin) throw new ForbiddenOperationException("Only admins or moderators can change moderation fields");
            if (request.getPinned() != null) content.setPinned(request.getPinned());
            if (request.getOfficial() != null) content.setOfficial(request.getOfficial());
            if (request.getFeatured() != null) content.setFeatured(request.getFeatured());
            if (request.getTrending() != null) content.setTrending(request.getTrending());
            if (request.getAccepted() != null) content.setAccepted(request.getAccepted());
        }

        return toResponse(contentRepository.save(content));
    }

    public void deleteContent(String contentType, String id, ActorDTO actorRequest, HttpServletRequest httpRequest) {
        CommunityContent content = findContent(contentType, id);
        Actor actor = requireActor(actorRequest, httpRequest);
        requireOwnerOrAdmin(content, actor, isAdmin(actor.role()));
        contentRepository.delete(content);
    }

    public CommunityVoteResponse vote(String contentType, String id, ActorDTO actorRequest, HttpServletRequest httpRequest) {
        CommunityContent content = findContent(contentType, id);
        Actor actor = requireActor(actorRequest, httpRequest);
        String voteType = DISCUSSION.equals(contentType) ? "LIKE" : "VOTE";

        boolean voted = voteRepository.findByContentIdAndUserKeyAndVoteType(id, actor.userKey(), voteType).isPresent();
        if (!voted) {
            voteRepository.save(CommunityVote.builder()
                    .contentId(id)
                    .contentType(contentType)
                    .userKey(actor.userKey())
                    .voteType(voteType)
                    .build());
            if ("LIKE".equals(voteType)) {
                content.setLikes(content.getLikes() + 1);
            } else {
                content.setVotes(content.getVotes() + 1);
            }
            contentRepository.save(content);
        }

        return CommunityVoteResponse.builder()
                .contentId(id)
                .contentType(contentType)
                .voteType(voteType)
                .voted(!voted)
                .votes(content.getVotes())
                .likes(content.getLikes())
                .build();
    }

    public List<CommunityCommentResponse> getComments(String contentType, String id) {
        findContent(contentType, id);
        return commentRepository.findByContentIdOrderByCreatedAtAsc(id).stream().map(this::toCommentResponse).toList();
    }

    public CommunityCommentResponse addComment(String contentType, String id, CommunityCommentRequest request, HttpServletRequest httpRequest) {
        CommunityContent content = findContent(contentType, id);
        Actor actor = requireActor(request.getActor(), httpRequest);
        CommunityComment comment = CommunityComment.builder()
                .contentId(id)
                .contentType(contentType)
                .parentCommentId(trimToNull(request.getParentCommentId()))
                .body(request.getBody().trim())
                .accepted(Boolean.TRUE.equals(request.getAccepted()) && isAdmin(actor.role()))
                .authorUserId(actor.userId())
                .authorEmail(actor.email())
                .authorName(actor.displayName())
                .authorRole(actor.role())
                .build();

        CommunityComment saved = commentRepository.save(comment);
        content.setComments((int) commentRepository.countByContentId(id));
        if (QUESTION.equals(contentType)) {
            content.setAnswers(content.getComments());
            if (saved.isAccepted()) content.setAccepted(true);
        } else if (DISCUSSION.equals(contentType)) {
            content.setReplies(content.getComments());
        }
        contentRepository.save(content);
        return toCommentResponse(saved);
    }

    public EventRsvpResponse rsvp(String eventId, ActorDTO actorRequest, HttpServletRequest httpRequest) {
        CommunityContent event = findContent(EVENT, eventId);
        Actor actor = requireActor(actorRequest, httpRequest);
        boolean alreadyRsvped = rsvpRepository.findByEventIdAndUserKey(eventId, actor.userKey()).isPresent();
        if (!alreadyRsvped) {
            rsvpRepository.save(EventRsvp.builder()
                    .eventId(eventId)
                    .userKey(actor.userKey())
                    .attendeeEmail(actor.email())
                    .attendeeName(actor.displayName())
                    .attendeeUserId(actor.userId())
                    .build());
            event.setAttendees((int) rsvpRepository.countByEventId(eventId));
            contentRepository.save(event);
        }
        return EventRsvpResponse.builder()
                .eventId(eventId)
                .rsvped(!alreadyRsvped)
                .attendees(event.getAttendees())
                .build();
    }

    public EventRsvpResponse cancelRsvp(String eventId, ActorDTO actorRequest, HttpServletRequest httpRequest) {
        CommunityContent event = findContent(EVENT, eventId);
        Actor actor = requireActor(actorRequest, httpRequest);
        rsvpRepository.deleteByEventIdAndUserKey(eventId, actor.userKey());
        event.setAttendees((int) rsvpRepository.countByEventId(eventId));
        contentRepository.save(event);
        return EventRsvpResponse.builder().eventId(eventId).rsvped(false).attendees(event.getAttendees()).build();
    }

    public CommunitySummaryResponse getSummary() {
        long discussions = contentRepository.countByContentType(DISCUSSION);
        long questions = contentRepository.countByContentType(QUESTION);
        long answeredQuestions = contentRepository.findByContentTypeAndStatusOrderByPinnedDescUpdatedAtDesc(QUESTION, "Published")
                .stream().filter(CommunityContent::isAccepted).count();
        int answeredPercentage = questions == 0 ? 0 : (int) Math.round((answeredQuestions * 100.0) / questions);
        List<LeaderboardEntry> leaderboard = getLeaderboard();

        return CommunitySummaryResponse.builder()
                .members(leaderboard.size())
                .discussions(discussions)
                .questions(questions)
                .answeredPercentage(answeredPercentage)
                .latestAnnouncement(contentRepository.findTop5ByContentTypeOrderByPinnedDescUpdatedAtDesc(ANNOUNCEMENT)
                        .stream().findFirst().map(this::toResponse).orElse(null))
                .trendingDiscussions(contentRepository.findTop5ByContentTypeAndTrendingTrueOrderByUpdatedAtDesc(DISCUSSION)
                        .stream().map(this::toResponse).toList())
                .featuredLearning(contentRepository.findTop5ByContentTypeOrderByPinnedDescUpdatedAtDesc(LEARNING)
                        .stream().filter(CommunityContent::isFeatured).map(this::toResponse).toList())
                .upcomingEvents(contentRepository.findTop5ByContentTypeOrderByPinnedDescUpdatedAtDesc(EVENT)
                        .stream().map(this::toResponse).toList())
                .champions(leaderboard.stream().limit(5).toList())
                .build();
    }

    public List<CommunityContentResponse> search(String query) {
        return filterByQuery(contentRepository.findAll(), query).stream()
                .sorted(Comparator.comparing(CommunityContent::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .map(this::toResponse)
                .toList();
    }

    public AiChatResponse chat(String message) {
        List<CommunityContentResponse> related = search(message);
        return AiChatResponse.builder()
                .answer("I found " + related.size() + " related community item(s). AI generation is ready to be wired to your LLM provider.")
                .relatedContent(related.stream().limit(5).toList())
                .build();
    }

    public List<LeaderboardEntry> getLeaderboard() {
        Map<String, MutableScore> scores = new HashMap<>();
        contentRepository.findAll().forEach(content -> {
            if (StringUtils.hasText(content.getCreatedByEmail())) {
                MutableScore score = scores.computeIfAbsent(content.getCreatedByEmail(), key ->
                        new MutableScore(content.getCreatedByName(), content.getCreatedByEmail()));
                score.points += 10 + content.getVotes() + content.getLikes() + content.getReplies() + content.getAnswers();
                score.contributions += 1;
            }
        });
        commentRepository.findAll().forEach(comment -> {
            if (StringUtils.hasText(comment.getAuthorEmail())) {
                MutableScore score = scores.computeIfAbsent(comment.getAuthorEmail(), key ->
                        new MutableScore(comment.getAuthorName(), comment.getAuthorEmail()));
                score.points += comment.isAccepted() ? 20 : 3;
                score.contributions += 1;
            }
        });

        List<MutableScore> ordered = scores.values().stream()
                .sorted(Comparator.comparingInt(MutableScore::points).reversed())
                .toList();
        List<LeaderboardEntry> result = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            MutableScore score = ordered.get(i);
            result.add(LeaderboardEntry.builder()
                    .rank(i + 1)
                    .name(defaultIfBlank(score.name, score.email))
                    .email(score.email)
                    .points(score.points)
                    .badge(badgeFor(score.points))
                    .contributions(score.contributions)
                    .build());
        }
        return result;
    }

    private CommunityContent findContent(String contentType, String id) {
        return contentRepository.findById(id)
                .filter(content -> contentType.equals(content.getContentType()))
                .orElseThrow(() -> new ResourceNotFoundException("Community content not found: " + id));
    }

    private List<CommunityContent> filterByQuery(List<CommunityContent> items, String query) {
        if (!StringUtils.hasText(query)) return items;
        String q = query.trim().toLowerCase();
        return items.stream()
                .filter(item -> contains(item.getTitle(), q)
                        || contains(item.getBody(), q)
                        || contains(item.getSummary(), q)
                        || item.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(q)))
                .toList();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private CommunityContentResponse toResponse(CommunityContent content) {
        return CommunityContentResponse.builder()
                .id(content.getId())
                .contentType(content.getContentType())
                .category(content.getCategory())
                .title(content.getTitle())
                .body(content.getBody())
                .summary(content.getSummary())
                .type(content.getType())
                .level(content.getLevel())
                .status(content.getStatus())
                .eventDate(content.getEventDate())
                .resourceUrl(content.getResourceUrl())
                .tags(content.getTags())
                .pinned(content.isPinned())
                .official(content.isOfficial())
                .featured(content.isFeatured())
                .trending(content.isTrending())
                .accepted(content.isAccepted())
                .authorUserId(content.getCreatedByUserId())
                .authorEmail(content.getCreatedByEmail())
                .authorName(content.getCreatedByName())
                .authorRole(content.getCreatedByRole())
                .votes(content.getVotes())
                .likes(content.getLikes())
                .replies(content.getReplies())
                .answers(content.getAnswers())
                .comments(content.getComments())
                .installs(content.getInstalls())
                .attendees(content.getAttendees())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }

    private CommunityCommentResponse toCommentResponse(CommunityComment comment) {
        return CommunityCommentResponse.builder()
                .id(comment.getId())
                .contentId(comment.getContentId())
                .contentType(comment.getContentType())
                .parentCommentId(comment.getParentCommentId())
                .body(comment.getBody())
                .accepted(comment.isAccepted())
                .authorUserId(comment.getAuthorUserId())
                .authorEmail(comment.getAuthorEmail())
                .authorName(comment.getAuthorName())
                .authorRole(comment.getAuthorRole())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private Actor requireActor(ActorDTO requestActor, HttpServletRequest request) {
        Actor actor = resolveActor(requestActor, request);
        if (!StringUtils.hasText(actor.email()) && !StringUtils.hasText(actor.userId())) {
            throw new ForbiddenOperationException("Login is required for this community action");
        }
        return actor;
    }

    private Actor resolveActor(ActorDTO requestActor, HttpServletRequest request) {
        String userId = firstText(header(request, "X-User-Id"), requestActor == null ? null : requestActor.getUserId());
        String email = firstText(header(request, "X-User-Email"), requestActor == null ? null : requestActor.getEmail());
        String name = firstText(header(request, "X-User-Name"), requestActor == null ? null : requestActor.getName(), email);
        String role = firstText(header(request, "X-User-Role"), requestActor == null ? null : requestActor.getRole(), "USER");
        return new Actor(trimToNull(userId), trimToNull(email), trimToNull(name), normalizeRole(role));
    }

    private String header(HttpServletRequest request, String name) {
        return request == null ? null : request.getHeader(name);
    }

    private void requireOwnerOrAdmin(CommunityContent content, Actor actor, boolean admin) {
        if (admin) return;
        String actorKey = actor.userKey();
        if (!actorKey.equalsIgnoreCase(defaultIfBlank(content.getCreatedByUserId(), content.getCreatedByEmail()))) {
            throw new ForbiddenOperationException("You can only change your own community records");
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equals(role) || "MODERATOR".equals(role);
    }

    private String normalizeRole(String role) {
        String normalized = defaultIfBlank(role, "USER").trim().toUpperCase();
        return Set.of("ADMIN", "MODERATOR", "USER").contains(normalized) ? normalized : "USER";
    }

    private String defaultCategory(String contentType) {
        return switch (contentType) {
            case DISCUSSION -> "general";
            case QUESTION -> "qa";
            case IDEA -> "feature-request";
            case ANNOUNCEMENT -> "release";
            case LEARNING -> "guide";
            case MARKETPLACE -> "Template";
            case EVENT -> "Live Q&A";
            default -> "general";
        };
    }

    private String defaultSubtype(String contentType) {
        return switch (contentType) {
            case ANNOUNCEMENT -> "release";
            case LEARNING -> "guide";
            case MARKETPLACE -> "Template";
            case EVENT -> "Live Q&A";
            default -> null;
        };
    }

    private String defaultStatus(String contentType, String requestedStatus, boolean admin) {
        if (IDEA.equals(contentType)) return validateStatus(contentType, defaultIfBlank(requestedStatus, "Under Review"));
        if (MARKETPLACE.equals(contentType)) {
            return validateStatus(contentType, admin ? defaultIfBlank(requestedStatus, "Approved") : "Pending Review");
        }
        return "Published";
    }

    private String validateStatus(String contentType, String status) {
        String normalized = defaultIfBlank(status, "Published");
        if (IDEA.equals(contentType) && !VALID_IDEA_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid idea status: " + status);
        }
        if (MARKETPLACE.equals(contentType) && !VALID_MARKETPLACE_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid marketplace status: " + status);
        }
        return normalized;
    }

    private List<String> cleanTags(List<String> tags) {
        if (tags == null) return new ArrayList<>();
        return tags.stream().filter(StringUtils::hasText).map(String::trim).distinct().toList();
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) return value.trim();
        }
        return null;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String badgeFor(int points) {
        if (points >= 1000) return "Legend";
        if (points >= 500) return "Champion";
        if (points >= 150) return "Expert";
        return "Contributor";
    }

    private record Actor(String userId, String email, String displayName, String role) {
        private String userKey() {
            return StringUtils.hasText(userId) ? userId : email;
        }
    }

    private static class MutableScore {
        private final String name;
        private final String email;
        private int points;
        private int contributions;

        private MutableScore(String name, String email) {
            this.name = name;
            this.email = email;
        }

        private int points() {
            return points;
        }
    }
}
