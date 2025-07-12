package win.pluvivanto.roombook.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BookingResponse {
  private Long id;
  private RoomResponse room;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String reservedBy;
}
