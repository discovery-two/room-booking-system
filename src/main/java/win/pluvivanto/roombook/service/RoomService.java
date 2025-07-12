package win.pluvivanto.roombook.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.RoomCreateRequest;
import win.pluvivanto.roombook.dto.RoomResponse;
import win.pluvivanto.roombook.exception.ErrorMessages;
import win.pluvivanto.roombook.mapper.RoomMapper;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class RoomService {
  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;

  public List<RoomResponse> listRooms() {
    List<Room> rooms = roomRepository.findAll();
    return RoomMapper.toResponseList(rooms);
  }

  public RoomResponse getRoom(Long id) {
    Room room = roomRepository.findById(id).orElseThrow(() -> ErrorMessages.roomNotFound(id));
    return RoomMapper.toResponse(room);
  }

  public RoomResponse createRoom(RoomCreateRequest request) {
    if (!roomRepository.findByName(request.getName()).isEmpty()) {
      throw ErrorMessages.roomNameAlreadyExists(request.getName());
    }

    Room room = RoomMapper.toEntity(request);
    Room savedRoom = roomRepository.save(room);
    return RoomMapper.toResponse(savedRoom);
  }

  public void deleteRoom(Long id) {
    roomRepository.deleteById(id);
  }

  public Boolean getAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
    Room room =
        roomRepository.findById(roomId).orElseThrow(() -> ErrorMessages.roomNotFound(roomId));

    return bookingRepository.findConflictingBookings(room, startTime, endTime).isEmpty();
  }
}
