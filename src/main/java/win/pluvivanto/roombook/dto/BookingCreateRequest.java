package win.pluvivanto.roombook.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BookingCreateRequest {

  @NotNull(message = "Room ID is required") private Long roomId;

  @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") private LocalDateTime startTime;

  @NotNull(message = "End time is required") @Future(message = "End time must be in the future") private LocalDateTime endTime;

  @Size(max = 100, message = "Reserved by name cannot exceed 100 characters") private String reservedBy;
}
