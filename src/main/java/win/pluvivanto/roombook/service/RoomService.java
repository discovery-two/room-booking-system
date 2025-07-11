package win.pluvivanto.roombook.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class RoomService {
  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;

  public List<Room> listRooms() {
    return roomRepository.findAll();
  }

  public Room getRoom(Long id) {
    return roomRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Room not found with ID " + id.toString()));
  }

  public Room createRoom(Room room) {
    if (!roomRepository.findByName(room.getName()).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate room name found");
    }
    return roomRepository.save(room);
  }

  public void deleteRoom(Long id) {
    roomRepository.deleteById(id);
  }

  public Boolean getAvailability(Long id, LocalDateTime startTime, LocalDateTime endTime) {
    final var room =
        roomRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    return bookingRepository.findConflictingBookings(room, startTime, endTime).isEmpty();
  }
}
