package win.pluvivanto.roombook.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@RestController
@RequestMapping("/room")
public class RoomController {
  @Autowired private RoomRepository roomRepository;
  @Autowired private BookingRepository bookingRepository;

  @GetMapping("")
  public List<Room> listRooms() {
    return roomRepository.findAll();
  }

  @GetMapping("/{id}")
  public Room getRoom(@PathVariable Long id) {
    return roomRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Room not found with ID " + id.toString()));
  }

  @PostMapping("")
  public Room createRoom(@RequestBody Room room) {
    if (!roomRepository.findByName(room.getName()).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate room name found");
    }
    return roomRepository.save(room);
  }

  @DeleteMapping("/{id}")
  public void deleteRoom(@PathVariable Long id) {
    roomRepository.deleteById(id);
  }

  @GetMapping("/{id}/availability")
  public Boolean getAvailability(
      @PathVariable Long id,
      @RequestParam LocalDateTime startTime,
      @RequestParam LocalDateTime endTime) {
    final var room =
        roomRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
    return bookingRepository.findConflictingBookings(room, startTime, endTime).isEmpty();
  }
}
