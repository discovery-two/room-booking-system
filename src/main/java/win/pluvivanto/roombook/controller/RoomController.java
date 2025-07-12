package win.pluvivanto.roombook.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import win.pluvivanto.roombook.dto.RoomCreateRequest;
import win.pluvivanto.roombook.dto.RoomResponse;
import win.pluvivanto.roombook.service.RoomService;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {
  private final RoomService roomService;

  @GetMapping("")
  public List<RoomResponse> listRooms() {
    return roomService.listRooms();
  }

  @GetMapping("/{id}")
  public RoomResponse getRoom(@PathVariable Long id) {
    return roomService.getRoom(id);
  }

  @PostMapping("")
  public RoomResponse createRoom(@Valid @RequestBody RoomCreateRequest request) {
    return roomService.createRoom(request);
  }

  @DeleteMapping("/{id}")
  public void deleteRoom(@PathVariable Long id) {
    roomService.deleteRoom(id);
  }

  @GetMapping("/{id}/availability")
  public Boolean getAvailability(
      @PathVariable Long id,
      @RequestParam LocalDateTime startTime,
      @RequestParam LocalDateTime endTime) {
    return roomService.getAvailability(id, startTime, endTime);
  }
}
