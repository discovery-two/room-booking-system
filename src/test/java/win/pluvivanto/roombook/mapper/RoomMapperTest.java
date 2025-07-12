package win.pluvivanto.roombook.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.RoomCreateRequest;
import win.pluvivanto.roombook.dto.RoomResponse;

class RoomMapperTest {

  @Test
  void toResponse_shouldMapRoomToResponse() {
    // Given
    Room room = new Room();
    room.setId(1L);
    room.setName("Conference Room A");

    // When
    RoomResponse response = RoomMapper.toResponse(room);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("Conference Room A");
  }

  @Test
  void toResponse_shouldReturnNullWhenRoomIsNull() {
    // When
    RoomResponse response = RoomMapper.toResponse(null);

    // Then
    assertThat(response).isNull();
  }

  @Test
  void toEntity_shouldMapCreateRequestToRoom() {
    // Given
    RoomCreateRequest request = new RoomCreateRequest();
    request.setName("Meeting Room B");

    // When
    Room room = RoomMapper.toEntity(request);

    // Then
    assertThat(room).isNotNull();
    assertThat(room.getId()).isNull(); // ID should not be set for new entities
    assertThat(room.getName()).isEqualTo("Meeting Room B");
  }

  @Test
  void toEntity_shouldReturnNullWhenRequestIsNull() {
    // When
    Room room = RoomMapper.toEntity(null);

    // Then
    assertThat(room).isNull();
  }

  @Test
  void toResponseList_shouldMapListOfRoomsToResponseList() {
    // Given
    Room room1 = new Room();
    room1.setId(1L);
    room1.setName("Room 1");

    Room room2 = new Room();
    room2.setId(2L);
    room2.setName("Room 2");

    List<Room> rooms = Arrays.asList(room1, room2);

    // When
    List<RoomResponse> responses = RoomMapper.toResponseList(rooms);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0).getId()).isEqualTo(1L);
    assertThat(responses.get(0).getName()).isEqualTo("Room 1");
    assertThat(responses.get(1).getId()).isEqualTo(2L);
    assertThat(responses.get(1).getName()).isEqualTo("Room 2");
  }

  @Test
  void toResponseList_shouldReturnEmptyListWhenInputIsEmpty() {
    // Given
    List<Room> rooms = Collections.emptyList();

    // When
    List<RoomResponse> responses = RoomMapper.toResponseList(rooms);

    // Then
    assertThat(responses).isEmpty();
  }

  @Test
  void toResponseList_shouldHandleNullRoomsInList() {
    // Given
    Room room1 = new Room();
    room1.setId(1L);
    room1.setName("Room 1");

    List<Room> rooms = Arrays.asList(room1, null);

    // When
    List<RoomResponse> responses = RoomMapper.toResponseList(rooms);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0)).isNotNull();
    assertThat(responses.get(0).getId()).isEqualTo(1L);
    assertThat(responses.get(1)).isNull();
  }
}
