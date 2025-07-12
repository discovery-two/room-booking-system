package win.pluvivanto.roombook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.RoomCreateRequest;
import win.pluvivanto.roombook.dto.RoomResponse;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

  @Mock private RoomRepository roomRepository;

  @Mock private BookingRepository bookingRepository;

  @InjectMocks private RoomService roomService;

  private Room testRoom;
  private RoomCreateRequest createRequest;

  @BeforeEach
  void setUp() {
    testRoom = new Room();
    testRoom.setId(1L);
    testRoom.setName("Conference Room A");

    createRequest = new RoomCreateRequest();
    createRequest.setName("New Conference Room");
  }

  @Test
  void listRooms_ShouldReturnAllRooms() {
    // Given
    Room room2 = new Room();
    room2.setId(2L);
    room2.setName("Conference Room B");
    List<Room> rooms = List.of(testRoom, room2);

    when(roomRepository.findAll()).thenReturn(rooms);

    // When
    List<RoomResponse> result = roomService.listRooms();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(0).getName()).isEqualTo("Conference Room A");
    assertThat(result.get(1).getId()).isEqualTo(2L);
    assertThat(result.get(1).getName()).isEqualTo("Conference Room B");
  }

  @Test
  void listRooms_WhenNoRooms_ShouldReturnEmptyList() {
    // Given
    when(roomRepository.findAll()).thenReturn(Collections.emptyList());

    // When
    List<RoomResponse> result = roomService.listRooms();

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void getRoom_WithValidId_ShouldReturnRoom() {
    // Given
    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

    // When
    RoomResponse result = roomService.getRoom(1L);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Conference Room A");
  }

  @Test
  void getRoom_WithInvalidId_ShouldThrowException() {
    // Given
    when(roomRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> roomService.getRoom(999L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Room with ID 999 not found");
  }

  @Test
  void createRoom_WithValidRequest_ShouldCreateRoom() {
    // Given
    when(roomRepository.findByName("New Conference Room")).thenReturn(Collections.emptyList());
    when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

    // When
    RoomResponse result = roomService.createRoom(createRequest);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Conference Room A");
    verify(roomRepository).save(any(Room.class));
  }

  @Test
  void createRoom_WithDuplicateName_ShouldThrowException() {
    // Given
    when(roomRepository.findByName("New Conference Room")).thenReturn(List.of(testRoom));

    // When & Then
    assertThatThrownBy(() -> roomService.createRoom(createRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Room with name 'New Conference Room' already exists");

    verify(roomRepository, never()).save(any(Room.class));
  }

  @Test
  void deleteRoom_WithValidId_ShouldDeleteRoom() {
    // When
    roomService.deleteRoom(1L);

    // Then
    verify(roomRepository).deleteById(1L);
  }

  @Test
  void getAvailability_WhenRoomAvailable_ShouldReturnTrue() {
    // Given
    LocalDateTime startTime = LocalDateTime.of(2100, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2100, 1, 15, 11, 0);

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(bookingRepository.findConflictingBookings(testRoom, startTime, endTime))
        .thenReturn(Collections.emptyList());

    // When
    Boolean result = roomService.getAvailability(1L, startTime, endTime);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void getAvailability_WhenRoomHasConflicts_ShouldReturnFalse() {
    // Given
    LocalDateTime startTime = LocalDateTime.of(2100, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2100, 1, 15, 11, 0);

    Booking conflictingBooking = new Booking();
    conflictingBooking.setId(1L);

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(bookingRepository.findConflictingBookings(testRoom, startTime, endTime))
        .thenReturn(List.of(conflictingBooking));

    // When
    Boolean result = roomService.getAvailability(1L, startTime, endTime);

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void getAvailability_WithInvalidRoomId_ShouldThrowException() {
    // Given
    LocalDateTime startTime = LocalDateTime.of(2100, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2100, 1, 15, 11, 0);

    when(roomRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> roomService.getAvailability(999L, startTime, endTime))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Room with ID 999 not found");
  }
}
