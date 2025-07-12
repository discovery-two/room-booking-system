package win.pluvivanto.roombook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ErrorMessages {

  private ErrorMessages() {}

  public static ResponseStatusException roomNotFound(Long roomId) {
    return new ResponseStatusException(
        HttpStatus.NOT_FOUND, "Room with ID " + roomId + " not found");
  }

  public static ResponseStatusException roomNameAlreadyExists(String roomName) {
    return new ResponseStatusException(
        HttpStatus.CONFLICT, "Room with name '" + roomName + "' already exists");
  }

  public static ResponseStatusException bookingNotFound(Long bookingId) {
    return new ResponseStatusException(
        HttpStatus.NOT_FOUND, "Booking with ID " + bookingId + " not found");
  }

  public static ResponseStatusException bookingConflict() {
    return new ResponseStatusException(HttpStatus.CONFLICT, "Booking conflict detected");
  }
}
