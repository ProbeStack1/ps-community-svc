# ps-community-svc

ProbeStack community microservice for the `/community` UI.

## Runtime

- Spring Boot 3.2.5
- Java 17
- MongoDB
- Default port: `8081`
- Context path: `/probestack-community`

Health check:

```http
GET /probestack-community/actuator/health
```

## Auth Model

Public reads do not require identity.

Mutating actions require a logged-in actor. Until the platform auth token is wired in, the service accepts identity from either request `actor` fields or these headers:

- `X-User-Id`
- `X-User-Email`
- `X-User-Name`
- `X-User-Role`: `USER`, `MODERATOR`, or `ADMIN`

Admin/moderator-only writes:

- Announcements
- Learning resources
- Events
- Moderation fields: `pinned`, `official`, `featured`, `trending`, `accepted`
- Idea status changes
- Marketplace review status and official flag

## Main APIs

Base URL:

```http
/probestack-community/api/v1/community
```

Shared collection endpoints:

```http
GET    /{collection}
POST   /{collection}
GET    /{collection}/{id}
PATCH  /{collection}/{id}
DELETE /{collection}/{id}
POST   /{collection}/{id}/vote
GET    /{collection}/{id}/comments
POST   /{collection}/{id}/comments
```

Supported collections:

- `discussions`
- `questions`
- `ideas`
- `announcements`
- `learning`
- `marketplace`
- `events`

Community-level endpoints:

```http
GET  /summary
GET  /search?q=versioning
GET  /leaderboard
POST /ai/chat
POST /events/{id}/rsvp
DELETE /events/{id}/rsvp
```

## Example Create Discussion

```http
POST /probestack-community/api/v1/community/discussions
X-User-Email: khitish@example.com
X-User-Name: Khitish Mangal
Content-Type: application/json

{
  "category": "practices",
  "title": "Best practices for API versioning?",
  "body": "What are teams doing in production?",
  "tags": ["API Design", "Best Practices"]
}
```

## Notes For UI Integration

- Use `GET /summary` for the Home tab.
- Use each collection endpoint for tab lists.
- Use `POST /questions/{id}/vote` for Q&A upvotes.
- Use `POST /ideas/{id}/vote` for idea upvotes.
- Use `POST /events/{id}/rsvp` for RSVP.
- Login prompts should be handled in the UI before create/vote/comment/RSVP calls.
