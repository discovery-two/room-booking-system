package win.pluvivanto.roombook.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.service.BookService;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

  private final BookService bookService;

  @GetMapping("")
  public List<Booking> listBookings() {
    return bookService.listBookings();
  }

  @GetMapping("/{id}")
  public Booking getBooking(@PathVariable Long id) {
    return bookService.getBooking(id);
  }

  @PostMapping("")
  public Booking createBooking(@RequestBody Booking booking) {
    return bookService.createBooking(booking);
  }

  @DeleteMapping("/{id}")
  public void deleteBooking(@PathVariable Long id) {
    bookService.deleteBooking(id);
  }
}
