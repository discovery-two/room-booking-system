package win.pluvivanto.roombook.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.BookingCreateRequest;
import win.pluvivanto.roombook.dto.BookingResponse;

class BookingMapperTest {

  @Test
  void toResponse_shouldMapBookingToResponse() {
    // Given
    Room room = new Room();
    room.setId(1L);
    room.setName("Conference Room A");

    Booking booking = new Booking();
    booking.setId(1L);
    booking.setRoom(room);
    booking.setStartTime(LocalDateTime.of(2100, 1, 15, 10, 0));
    booking.setEndTime(LocalDateTime.of(2100, 1, 15, 11, 0));
    booking.setReservedBy("John Doe");

    // When
    BookingResponse response = BookingMapper.toResponse(booking);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getRoom()).isNotNull();
    assertThat(response.getRoom().getId()).isEqualTo(1L);
    assertThat(response.getRoom().getName()).isEqualTo("Conference Room A");
    assertThat(response.getStartTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 10, 0));
    assertThat(response.getEndTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 11, 0));
    assertThat(response.getReservedBy()).isEqualTo("John Doe");
  }

  @Test
  void toResponse_shouldReturnNullWhenBookingIsNull() {
    // When
    BookingResponse response = BookingMapper.toResponse(null);

    // Then
    assertThat(response).isNull();
  }

  @Test
  void toResponse_shouldHandleBookingWithNullRoom() {
    // Given
    Booking booking = new Booking();
    booking.setId(1L);
    booking.setRoom(null);
    booking.setStartTime(LocalDateTime.of(2100, 1, 15, 10, 0));
    booking.setEndTime(LocalDateTime.of(2100, 1, 15, 11, 0));
    booking.setReservedBy("John Doe");

    // When
    BookingResponse response = BookingMapper.toResponse(booking);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getRoom()).isNull();
    assertThat(response.getStartTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 10, 0));
    assertThat(response.getEndTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 11, 0));
    assertThat(response.getReservedBy()).isEqualTo("John Doe");
  }

  @Test
  void toEntity_shouldMapCreateRequestToBooking() {
    // Given
    BookingCreateRequest request = new BookingCreateRequest();
    request.setStartTime(LocalDateTime.of(2100, 1, 15, 14, 0));
    request.setEndTime(LocalDateTime.of(2100, 1, 15, 15, 0));
    request.setReservedBy("Jane Smith");

    Room room = new Room();
    room.setId(2L);
    room.setName("Meeting Room B");

    // When
    Booking booking = BookingMapper.toEntity(request, room);

    // Then
    assertThat(booking).isNotNull();
    assertThat(booking.getId()).isNull(); // ID should not be set for new entities
    assertThat(booking.getRoom()).isEqualTo(room);
    assertThat(booking.getStartTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 14, 0));
    assertThat(booking.getEndTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 15, 0));
    assertThat(booking.getReservedBy()).isEqualTo("Jane Smith");
  }

  @Test
  void toEntity_shouldReturnNullWhenRequestIsNull() {
    // Given
    Room room = new Room();
    room.setId(1L);

    // When
    Booking booking = BookingMapper.toEntity(null, room);

    // Then
    assertThat(booking).isNull();
  }

  @Test
  void toEntity_shouldReturnNullWhenRoomIsNull() {
    // Given
    BookingCreateRequest request = new BookingCreateRequest();
    request.setStartTime(LocalDateTime.of(2100, 1, 15, 14, 0));
    request.setEndTime(LocalDateTime.of(2100, 1, 15, 15, 0));
    request.setReservedBy("Jane Smith");

    // When
    Booking booking = BookingMapper.toEntity(request, null);

    // Then
    assertThat(booking).isNull();
  }

  @Test
  void toEntity_shouldReturnNullWhenBothParametersAreNull() {
    // When
    Booking booking = BookingMapper.toEntity(null, null);

    // Then
    assertThat(booking).isNull();
  }

  @Test
  void toResponseList_shouldMapListOfBookingsToResponseList() {
    // Given
    Room room1 = new Room();
    room1.setId(1L);
    room1.setName("Room 1");

    Room room2 = new Room();
    room2.setId(2L);
    room2.setName("Room 2");

    Booking booking1 = new Booking();
    booking1.setId(1L);
    booking1.setRoom(room1);
    booking1.setStartTime(LocalDateTime.of(2100, 1, 15, 10, 0));
    booking1.setEndTime(LocalDateTime.of(2100, 1, 15, 11, 0));
    booking1.setReservedBy("User 1");

    Booking booking2 = new Booking();
    booking2.setId(2L);
    booking2.setRoom(room2);
    booking2.setStartTime(LocalDateTime.of(2100, 1, 15, 14, 0));
    booking2.setEndTime(LocalDateTime.of(2100, 1, 15, 15, 0));
    booking2.setReservedBy("User 2");

    List<Booking> bookings = Arrays.asList(booking1, booking2);

    // When
    List<BookingResponse> responses = BookingMapper.toResponseList(bookings);

    // Then
    assertThat(responses).hasSize(2);

    assertThat(responses.get(0).getId()).isEqualTo(1L);
    assertThat(responses.get(0).getRoom().getId()).isEqualTo(1L);
    assertThat(responses.get(0).getReservedBy()).isEqualTo("User 1");

    assertThat(responses.get(1).getId()).isEqualTo(2L);
    assertThat(responses.get(1).getRoom().getId()).isEqualTo(2L);
    assertThat(responses.get(1).getReservedBy()).isEqualTo("User 2");
  }

  @Test
  void toResponseList_shouldReturnEmptyListWhenInputIsEmpty() {
    // Given
    List<Booking> bookings = Collections.emptyList();

    // When
    List<BookingResponse> responses = BookingMapper.toResponseList(bookings);

    // Then
    assertThat(responses).isEmpty();
  }

  @Test
  void toResponseList_shouldHandleNullBookingsInList() {
    // Given
    Room room = new Room();
    room.setId(1L);
    room.setName("Room 1");

    Booking booking = new Booking();
    booking.setId(1L);
    booking.setRoom(room);
    booking.setStartTime(LocalDateTime.of(2100, 1, 15, 10, 0));
    booking.setEndTime(LocalDateTime.of(2100, 1, 15, 11, 0));
    booking.setReservedBy("User 1");

    List<Booking> bookings = Arrays.asList(booking, null);

    // When
    List<BookingResponse> responses = BookingMapper.toResponseList(bookings);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses.get(0)).isNotNull();
    assertThat(responses.get(0).getId()).isEqualTo(1L);
    assertThat(responses.get(1)).isNull();
  }
}
