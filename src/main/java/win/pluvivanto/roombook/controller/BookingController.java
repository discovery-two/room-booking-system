package win.pluvivanto.roombook.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.pluvivanto.roombook.dto.BookingCreateRequest;
import win.pluvivanto.roombook.dto.BookingResponse;
import win.pluvivanto.roombook.service.BookingService;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @GetMapping("")
  public List<BookingResponse> listBookings() {
    return bookingService.listBookings();
  }

  @GetMapping("/{id}")
  public BookingResponse getBooking(@PathVariable Long id) {
    return bookingService.getBooking(id);
  }

  @PostMapping("")
  public BookingResponse createBooking(@Valid @RequestBody BookingCreateRequest request) {
    return bookingService.createBooking(request);
  }

  @DeleteMapping("/{id}")
  public void deleteBooking(@PathVariable Long id) {
    bookingService.deleteBooking(id);
  }
}
