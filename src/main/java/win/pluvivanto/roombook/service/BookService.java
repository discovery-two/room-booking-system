package win.pluvivanto.roombook.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@Service
@RequiredArgsConstructor
public class BookService {
  private final BookingRepository bookingRepository;
  private final RoomRepository roomRepository;

  public List<Booking> listBookings() {
    return bookingRepository.findAll();
  }

  public Booking getBooking(Long id) {
    return bookingRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Booking not found with ID " + id));
  }

  public Booking createBooking(Booking booking) {
    final var room =
        roomRepository
            .findById(booking.getRoom().getId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Room not found with id" + booking.getRoom().getId().toString()));
    booking.setRoom(room);
    final var conflicts =
        bookingRepository.findConflictingBookings(
            booking.getRoom(), booking.getStartTime(), booking.getEndTime());
    if (!conflicts.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking conflict detected");
    }
    return bookingRepository.save(booking);
  }

  public void deleteBooking(Long id) {
    bookingRepository.deleteById(id);
  }
}
