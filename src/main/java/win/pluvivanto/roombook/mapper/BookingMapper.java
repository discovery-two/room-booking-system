package win.pluvivanto.roombook.mapper;

import java.util.List;
import win.pluvivanto.roombook.domain.Booking;
import win.pluvivanto.roombook.domain.Room;
import win.pluvivanto.roombook.dto.BookingCreateRequest;
import win.pluvivanto.roombook.dto.BookingResponse;

public class BookingMapper {

  private BookingMapper() {
    throw new IllegalStateException("Utility class");
  }

  public static BookingResponse toResponse(Booking booking) {
    if (booking == null) {
      return null;
    }

    BookingResponse response = new BookingResponse();
    response.setId(booking.getId());
    response.setRoom(RoomMapper.toResponse(booking.getRoom()));
    response.setStartTime(booking.getStartTime());
    response.setEndTime(booking.getEndTime());
    response.setReservedBy(booking.getReservedBy());
    return response;
  }

  public static Booking toEntity(BookingCreateRequest request, Room room) {
    if (request == null || room == null) {
      return null;
    }

    Booking booking = new Booking();
    booking.setRoom(room);
    booking.setStartTime(request.getStartTime());
    booking.setEndTime(request.getEndTime());
    booking.setReservedBy(request.getReservedBy());

    return booking;
  }

  public static List<BookingResponse> toResponseList(List<Booking> bookings) {
    return bookings.stream().map(BookingMapper::toResponse).toList();
  }
}
