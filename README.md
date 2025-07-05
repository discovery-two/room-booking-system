# roombook

Meeting room booking API. Handles room reservations with basic conflict detection.

## Setup

Requires Java 17 and Docker.

```bash
docker-compose up -d
./gradlew bootRun
```

App runs on port 8081. Adminer available at 8080.

## API

### Rooms
- `GET /room` - list rooms
- `GET /room/{id}` - get room
- `POST /room` - create room
- `DELETE /room/{id}` - delete room
- `GET /room/{id}/availability?startTime=...&endTime=...` - check if available

### Bookings
- `GET /booking` - list bookings
- `GET /booking/{id}` - get booking  
- `POST /booking` - create booking
- `DELETE /booking/{id}` - delete booking

## Examples

Create room:
```bash
curl -X POST localhost:8081/room -H "Content-Type: application/json" -d '{"name": "Room 101"}'
```

Book room:
```bash
curl -X POST localhost:8081/booking -H "Content-Type: application/json" -d '{
  "room": {"id": 1},
  "startTime": "2025-07-06T10:00:00", 
  "endTime": "2025-07-06T11:00:00",
  "reservedBy": "John"
}'
```

## Notes

- Booking conflicts return 409
- Uses standard time overlap detection: `(startA < endB) && (startB < endA)`
- PostgreSQL with JPA/Hibernate
- Lombok for getters/setters
- HTTP tests in `src/test/.http`

## Config

Dev: `application.yml`
Prod: `application-prod.yml` (hides stack traces)

Database creds are hardcoded for local dev. Will need proper secrets management for production deployment.

## Schema

```sql
Room: id, name
Booking: id, room_id, start_time, end_time, reserved_by
```

Unique constraint on room names. Foreign key from booking to room.
