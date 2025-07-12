package win.pluvivanto.roombook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomCreateRequest {

  @NotBlank(message = "Room name is required") @Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters") private String name;
}
