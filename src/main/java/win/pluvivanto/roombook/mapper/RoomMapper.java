package win.pluvivanto.roombook.mapper;

import java.util.List;
import java.util.stream.Collectors;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.RoomCreateRequest;
import win.pluvivanto.roombook.dto.RoomResponse;

public class RoomMapper {

  private RoomMapper() {
    throw new IllegalStateException("Utility class");
  }

  public static RoomResponse toResponse(Room room) {
    if (room == null) {
      return null;
    }

    RoomResponse response = new RoomResponse();
    response.setId(room.getId());
    response.setName(room.getName());
    return response;
  }

  public static Room toEntity(RoomCreateRequest request) {
    if (request == null) {
      return null;
    }

    Room room = new Room();
    room.setName(request.getName());

    return room;
  }

  public static List<RoomResponse> toResponseList(List<Room> rooms) {
    return rooms.stream().map(RoomMapper::toResponse).collect(Collectors.toList());
  }
}
