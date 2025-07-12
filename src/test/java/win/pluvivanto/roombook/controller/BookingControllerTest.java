package win.pluvivanto.roombook.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import win.pluvivanto.roombook.dto.BookingCreateRequest;
import win.pluvivanto.roombook.dto.BookingResponse;
import win.pluvivanto.roombook.dto.RoomResponse;
import win.pluvivanto.roombook.service.BookingService;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private BookingService bookingService;

  private BookingResponse bookingResponse;
  private BookingCreateRequest createRequest;

  @BeforeEach
  void setUp() {
    RoomResponse room = new RoomResponse();
    room.setId(1L);
    room.setName("Conference Room A");

    bookingResponse = new BookingResponse();
    bookingResponse.setId(1L);
    bookingResponse.setRoom(room);
    bookingResponse.setStartTime(LocalDateTime.of(2100, 1, 15, 10, 0));
    bookingResponse.setEndTime(LocalDateTime.of(2100, 1, 15, 11, 0));
    bookingResponse.setReservedBy("John Doe");

    createRequest = new BookingCreateRequest();
    createRequest.setRoomId(1L);
    createRequest.setStartTime(LocalDateTime.of(2100, 1, 16, 14, 0));
    createRequest.setEndTime(LocalDateTime.of(2100, 1, 16, 15, 0));
    createRequest.setReservedBy("Jane Smith");
  }

  @Test
  void listBookings_ShouldReturnAllBookings() throws Exception {
    // Given
    RoomResponse room2 = new RoomResponse();
    room2.setId(2L);
    room2.setName("Conference Room B");

    BookingResponse booking2 = new BookingResponse();
    booking2.setId(2L);
    booking2.setRoom(room2);
    booking2.setStartTime(LocalDateTime.of(2100, 1, 17, 12, 0));
    booking2.setEndTime(LocalDateTime.of(2100, 1, 17, 13, 0));
    booking2.setReservedBy("Bob Wilson");

    List<BookingResponse> bookings = List.of(bookingResponse, booking2);
    when(bookingService.listBookings()).thenReturn(bookings);

    // When & Then
    mockMvc
        .perform(get("/booking"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].reservedBy").value("John Doe"))
        .andExpect(jsonPath("$[0].room.id").value(1))
        .andExpect(jsonPath("$[0].room.name").value("Conference Room A"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].reservedBy").value("Bob Wilson"));
  }

  @Test
  void listBookings_WhenNoBookings_ShouldReturnEmptyArray() throws Exception {
    // Given
    when(bookingService.listBookings()).thenReturn(Collections.emptyList());

    // When & Then
    mockMvc
        .perform(get("/booking"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getBooking_WithValidId_ShouldReturnBooking() throws Exception {
    // Given
    when(bookingService.getBooking(1L)).thenReturn(bookingResponse);

    // When & Then
    mockMvc
        .perform(get("/booking/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.reservedBy").value("John Doe"))
        .andExpect(jsonPath("$.room.id").value(1))
        .andExpect(jsonPath("$.startTime").value("2100-01-15T10:00:00"))
        .andExpect(jsonPath("$.endTime").value("2100-01-15T11:00:00"));
  }

  @Test
  void getBooking_WithInvalidId_ShouldReturn404() throws Exception {
    // Given
    when(bookingService.getBooking(999L))
        .thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking with ID 999 not found"));

    // When & Then
    mockMvc.perform(get("/booking/999")).andExpect(status().isNotFound());
  }

  @Test
  void createBooking_WithValidRequest_ShouldCreateBooking() throws Exception {
    // Given
    when(bookingService.createBooking(any(BookingCreateRequest.class))).thenReturn(bookingResponse);

    // When & Then
    mockMvc
        .perform(
            post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.reservedBy").value("John Doe"))
        .andExpect(jsonPath("$.room.id").value(1));
  }

  @Test
  void createBooking_WithInvalidRequest_ShouldReturn400() throws Exception {
    // Given - request with missing required fields
    BookingCreateRequest invalidRequest = new BookingCreateRequest();
    // roomId, startTime, endTime, reservedBy are null - should trigger validation errors

    // When & Then
    mockMvc
        .perform(
            post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createBooking_WithInvalidRoomId_ShouldReturn404() throws Exception {
    // Given
    when(bookingService.createBooking(any(BookingCreateRequest.class)))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Room with ID 1 not found"));

    // When & Then
    mockMvc
        .perform(
            post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createBooking_WithConflictingBooking_ShouldReturn409() throws Exception {
    // Given
    when(bookingService.createBooking(any(BookingCreateRequest.class)))
        .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Booking conflict detected"));

    // When & Then
    mockMvc
        .perform(
            post("/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void deleteBooking_WithValidId_ShouldReturn200() throws Exception {
    // Given
    doNothing().when(bookingService).deleteBooking(1L);

    // When & Then
    mockMvc.perform(delete("/booking/1")).andExpect(status().isOk());
  }

  @Test
  void createBooking_WithMalformedJson_ShouldReturn400() throws Exception {
    // When & Then
    mockMvc
        .perform(post("/booking").contentType(MediaType.APPLICATION_JSON).content("{invalid json}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createBooking_WithInvalidDateFormat_ShouldReturn400() throws Exception {
    // Given - request with invalid date format
    String invalidDateRequest =
        "{"
            + "\"roomId\": 1,"
            + "\"startTime\": \"invalid-date\","
            + "\"endTime\": \"2100-01-16T15:00:00\","
            + "\"reservedBy\": \"Jane Smith\""
            + "}";

    // When & Then
    mockMvc
        .perform(
            post("/booking").contentType(MediaType.APPLICATION_JSON).content(invalidDateRequest))
        .andExpect(status().isBadRequest());
  }
}
