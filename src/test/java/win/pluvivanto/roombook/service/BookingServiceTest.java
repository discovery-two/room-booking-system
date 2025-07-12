package win.pluvivanto.roombook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import win.pluvivanto.roombook.dto.BookingCreateRequest;
import win.pluvivanto.roombook.dto.BookingResponse;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock private BookingRepository bookingRepository;

  @Mock private RoomRepository roomRepository;

  @InjectMocks private BookingService bookingService;

  private Room testRoom;
  private Booking testBooking;
  private BookingCreateRequest createRequest;

  @BeforeEach
  void setUp() {
    testRoom = new Room();
    testRoom.setId(1L);
    testRoom.setName("Conference Room A");

    testBooking = new Booking();
    testBooking.setId(1L);
    testBooking.setRoom(testRoom);
    testBooking.setStartTime(LocalDateTime.of(2100, 1, 15, 10, 0));
    testBooking.setEndTime(LocalDateTime.of(2100, 1, 15, 11, 0));
    testBooking.setReservedBy("John Doe");

    createRequest = new BookingCreateRequest();
    createRequest.setRoomId(1L);
    createRequest.setStartTime(LocalDateTime.of(2100, 1, 15, 14, 0));
    createRequest.setEndTime(LocalDateTime.of(2100, 1, 15, 15, 0));
    createRequest.setReservedBy("Jane Smith");
  }

  @Test
  void listBookings_ShouldReturnAllBookings() {
    // Given
    Booking booking2 = new Booking();
    booking2.setId(2L);
    booking2.setRoom(testRoom);
    booking2.setStartTime(LocalDateTime.of(2100, 1, 15, 12, 0));
    booking2.setEndTime(LocalDateTime.of(2100, 1, 15, 13, 0));
    booking2.setReservedBy("Bob Wilson");

    List<Booking> bookings = List.of(testBooking, booking2);
    when(bookingRepository.findAll()).thenReturn(bookings);

    // When
    List<BookingResponse> result = bookingService.listBookings();

    // Then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(1L);
    assertThat(result.get(0).getReservedBy()).isEqualTo("John Doe");
    assertThat(result.get(1).getId()).isEqualTo(2L);
    assertThat(result.get(1).getReservedBy()).isEqualTo("Bob Wilson");
  }

  @Test
  void listBookings_WhenNoBookings_ShouldReturnEmptyList() {
    // Given
    when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

    // When
    List<BookingResponse> result = bookingService.listBookings();

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void getBooking_WithValidId_ShouldReturnBooking() {
    // Given
    when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

    // When
    BookingResponse result = bookingService.getBooking(1L);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getReservedBy()).isEqualTo("John Doe");
    assertThat(result.getStartTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 10, 0));
    assertThat(result.getEndTime()).isEqualTo(LocalDateTime.of(2100, 1, 15, 11, 0));
  }

  @Test
  void getBooking_WithInvalidId_ShouldThrowException() {
    // Given
    when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> bookingService.getBooking(999L))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Booking with ID 999 not found");
  }

  @Test
  void createBooking_WithValidRequest_ShouldCreateBooking() {
    // Given
    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(bookingRepository.findConflictingBookings(
            eq(testRoom), eq(createRequest.getStartTime()), eq(createRequest.getEndTime())))
        .thenReturn(Collections.emptyList());
    when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

    // When
    BookingResponse result = bookingService.createBooking(createRequest);

    // Then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getReservedBy()).isEqualTo("John Doe");
    verify(bookingRepository).save(any(Booking.class));
  }

  @Test
  void createBooking_WithInvalidRoomId_ShouldThrowException() {
    // Given
    when(roomRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> bookingService.createBooking(createRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Room with ID 1 not found");

    verify(bookingRepository, never()).save(any(Booking.class));
  }

  @Test
  void createBooking_WithConflictingBooking_ShouldThrowException() {
    // Given
    Booking conflictingBooking = new Booking();
    conflictingBooking.setId(2L);

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(bookingRepository.findConflictingBookings(
            eq(testRoom), eq(createRequest.getStartTime()), eq(createRequest.getEndTime())))
        .thenReturn(List.of(conflictingBooking));

    // When & Then
    assertThatThrownBy(() -> bookingService.createBooking(createRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Booking conflict detected");

    verify(bookingRepository, never()).save(any(Booking.class));
  }

  @Test
  void deleteBooking_WithValidId_ShouldDeleteBooking() {
    // When
    bookingService.deleteBooking(1L);

    // Then
    verify(bookingRepository).deleteById(1L);
  }

  @Test
  void createBooking_WithBoundaryTimes_ShouldCheckConflictsCorrectly() {
    // Given
    LocalDateTime boundaryStart =
        LocalDateTime.of(2100, 1, 15, 11, 0); // Exactly when previous ends
    LocalDateTime boundaryEnd = LocalDateTime.of(2100, 1, 15, 12, 0);

    createRequest.setStartTime(boundaryStart);
    createRequest.setEndTime(boundaryEnd);

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(bookingRepository.findConflictingBookings(
            eq(testRoom), eq(boundaryStart), eq(boundaryEnd)))
        .thenReturn(Collections.emptyList());
    when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

    // When
    BookingResponse result = bookingService.createBooking(createRequest);

    // Then
    assertThat(result).isNotNull();
    verify(bookingRepository)
        .findConflictingBookings(eq(testRoom), eq(boundaryStart), eq(boundaryEnd));
  }

  @Test
  void createBooking_WithMultipleConflicts_ShouldThrowException() {
    // Given
    Booking conflict1 = new Booking();
    conflict1.setId(2L);
    Booking conflict2 = new Booking();
    conflict2.setId(3L);

    when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
    when(bookingRepository.findConflictingBookings(
            eq(testRoom), eq(createRequest.getStartTime()), eq(createRequest.getEndTime())))
        .thenReturn(List.of(conflict1, conflict2));

    // When & Then
    assertThatThrownBy(() -> bookingService.createBooking(createRequest))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Booking conflict detected");
  }
}
