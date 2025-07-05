package win.pluvivanto.roombook.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.repository.BookingRepository;
import win.pluvivanto.roombook.repository.RoomRepository;

@RestController
@RequestMapping("/booking")
public class BookingController {
  @Autowired private BookingRepository bookingRepository;
  @Autowired private RoomRepository roomRepository;

  @GetMapping("")
  public List<Booking> listBookings() {
    return bookingRepository.findAll();
  }

  @GetMapping("/{id}")
  public Booking getBooking(@PathVariable Long id) {
    return bookingRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Booking not found with ID " + id));
  }

  @PostMapping("")
  public Booking createBooking(@RequestBody Booking booking) {
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

  @DeleteMapping("/{id}")
  public void deleteBooking(@PathVariable Long id) {
    bookingRepository.deleteById(id);
  }
}
