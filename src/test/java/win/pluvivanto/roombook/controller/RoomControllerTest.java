package win.pluvivanto.roombook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.config.TestSecurityConfig;
import win.pluvivanto.roombook.dto.RoomCreateRequest;
import win.pluvivanto.roombook.dto.RoomResponse;
import win.pluvivanto.roombook.service.RoomService;

@WebMvcTest(RoomController.class)
@Import(TestSecurityConfig.class)
class RoomControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private RoomService roomService;

  private RoomResponse roomResponse;
  private RoomCreateRequest createRequest;

  @BeforeEach
  void setUp() {
    roomResponse = new RoomResponse();
    roomResponse.setId(1L);
    roomResponse.setName("Conference Room A");

    createRequest = new RoomCreateRequest();
    createRequest.setName("New Conference Room");
  }

  @Test
  void listRooms_ShouldReturnAllRooms() throws Exception {
    // Given
    RoomResponse room2 = new RoomResponse();
    room2.setId(2L);
    room2.setName("Conference Room B");
    List<RoomResponse> rooms = List.of(roomResponse, room2);

    when(roomService.listRooms()).thenReturn(rooms);

    // When & Then
    mockMvc
        .perform(get("/room"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("Conference Room A"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].name").value("Conference Room B"));
  }

  @Test
  void listRooms_WhenNoRooms_ShouldReturnEmptyArray() throws Exception {
    // Given
    when(roomService.listRooms()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc
        .perform(get("/room"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getRoom_WithValidId_ShouldReturnRoom() throws Exception {
    // Given
    when(roomService.getRoom(1L)).thenReturn(roomResponse);

    // When & Then
    mockMvc
        .perform(get("/room/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Conference Room A"));
  }

  @Test
  void getRoom_WithInvalidId_ShouldReturn404() throws Exception {
    // Given
    when(roomService.getRoom(999L))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Room with ID 999 not found"));

    // When & Then
    mockMvc.perform(get("/room/999")).andExpect(status().isNotFound());
  }

  @Test
  void createRoom_WithValidRequest_ShouldCreateRoom() throws Exception {
    // Given
    when(roomService.createRoom(any(RoomCreateRequest.class))).thenReturn(roomResponse);

    // When & Then
    mockMvc
        .perform(
            post("/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Conference Room A"));
  }

  @Test
  void createRoom_WithInvalidRequest_ShouldReturn400() throws Exception {
    // Given - empty request body
    RoomCreateRequest invalidRequest = new RoomCreateRequest();
    // name is null, should trigger validation error

    // When & Then
    mockMvc
        .perform(
            post("/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createRoom_WithDuplicateName_ShouldReturn409() throws Exception {
    // Given
    when(roomService.createRoom(any(RoomCreateRequest.class)))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.CONFLICT, "Room with name 'New Conference Room' already exists"));

    // When & Then
    mockMvc
        .perform(
            post("/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void deleteRoom_WithValidId_ShouldReturn200() throws Exception {
    // Given
    doNothing().when(roomService).deleteRoom(1L);

    // When & Then
    mockMvc.perform(delete("/room/1")).andExpect(status().isOk());
  }

  @Test
  void getAvailability_WhenRoomAvailable_ShouldReturnTrue() throws Exception {
    // Given
    LocalDateTime startTime = LocalDateTime.of(2100, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2100, 1, 15, 11, 0);

    when(roomService.getAvailability(eq(1L), eq(startTime), eq(endTime))).thenReturn(true);

    // When & Then
    mockMvc
        .perform(
            get("/room/1/availability")
                .param("startTime", "2100-01-15T10:00:00")
                .param("endTime", "2100-01-15T11:00:00"))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  void getAvailability_WhenRoomNotAvailable_ShouldReturnFalse() throws Exception {
    // Given
    LocalDateTime startTime = LocalDateTime.of(2100, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2100, 1, 15, 11, 0);

    when(roomService.getAvailability(eq(1L), eq(startTime), eq(endTime))).thenReturn(false);

    // When & Then
    mockMvc
        .perform(
            get("/room/1/availability")
                .param("startTime", "2100-01-15T10:00:00")
                .param("endTime", "2100-01-15T11:00:00"))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));
  }

  @Test
  void getAvailability_WithInvalidRoomId_ShouldReturn404() throws Exception {
    // Given
    LocalDateTime startTime = LocalDateTime.of(2100, 1, 15, 10, 0);
    LocalDateTime endTime = LocalDateTime.of(2100, 1, 15, 11, 0);

    when(roomService.getAvailability(eq(999L), eq(startTime), eq(endTime)))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Room with ID 999 not found"));

    // When & Then
    mockMvc
        .perform(
            get("/room/999/availability")
                .param("startTime", "2100-01-15T10:00:00")
                .param("endTime", "2100-01-15T11:00:00"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAvailability_WithMissingParameters_ShouldReturn400() throws Exception {
    // When & Then - missing required parameters
    mockMvc.perform(get("/room/1/availability")).andExpect(status().isBadRequest());
  }
}
