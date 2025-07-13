package win.pluvivanto.roombook.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.BookingCreateRequest;
import win.pluvivanto.roombook.dto.BookingResponse;
import win.pluvivanto.roombook.exception.ErrorMessages;
import win.pluvivanto.roombook.mapper.BookingMapper;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;
import win.pluvivanto.roombook.util.AuthUtil;

@Service
@RequiredArgsConstructor
public class BookingService {
  private final BookingRepository bookingRepository;
  private final RoomRepository roomRepository;

  public List<BookingResponse> listBookings() {
    List<Booking> bookings = bookingRepository.findAll();
    return BookingMapper.toResponseList(bookings);
  }

  public BookingResponse getBooking(Long id) {
    Booking booking =
        bookingRepository.findById(id).orElseThrow(() -> ErrorMessages.bookingNotFound(id));
    return BookingMapper.toResponse(booking);
  }

  public BookingResponse createBooking(BookingCreateRequest request) {
    Room room =
        roomRepository
            .findById(request.getRoomId())
            .orElseThrow(() -> ErrorMessages.roomNotFound(request.getRoomId()));

    List<Booking> conflicts =
        bookingRepository.findConflictingBookings(
            room, request.getStartTime(), request.getEndTime());

    if (!conflicts.isEmpty()) {
      throw ErrorMessages.bookingConflict();
    }

    String currentUser = AuthUtil.getCurrentUserEmail();

    Booking booking = BookingMapper.toEntity(request, room);
    booking.setReservedBy(currentUser);

    Booking savedBooking = bookingRepository.save(booking);

    return BookingMapper.toResponse(savedBooking);
  }

  public void deleteBooking(Long id) {
    bookingRepository.deleteById(id);
  }
}
