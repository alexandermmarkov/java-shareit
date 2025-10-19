import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import shareit.ShareItGateway;
import shareit.booking.BookingClient;
import shareit.booking.BookingController;
import shareit.booking.dto.BookingDto;
import shareit.booking.dto.BookingState;
import shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@ContextConfiguration(classes = ShareItGateway.class)
class BookingControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBookingWhenStartIsNull() throws Exception {
        BookingDto invalidDto = new BookingDto(
                null, null, LocalDateTime.now().plusDays(1), 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Дата начала бронирования не может быть пустой")));
    }

    @Test
    void createBookingWhenEndIsNull() throws Exception {
        BookingDto invalidDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), null, 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Дата окончания бронирования не может быть пустой")));
    }

    @Test
    void createBookingWhenItemIdIsNull() throws Exception {
        BookingDto invalidDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Ссылка на бронируемую вещь не может быть пустой")));
    }

    @Test
    void createBookingWhenStartIsInPast() throws Exception {
        BookingDto invalidDto = new BookingDto(
                null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingWhenEndIsNotFuture() throws Exception {
        BookingDto invalidDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1), 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingWithValidData() throws Exception {
        BookingDto validDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 1L, BookingStatus.WAITING);

        Mockito.when(bookingClient.addBooking(anyLong(), any(BookingDto.class)))
                .thenReturn(ResponseEntity.ok(new BookingDto(1L, validDto.getStart(), validDto.getEnd(),
                        validDto.getItemId(), validDto.getBookerId(), validDto.getStatus())));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, times(1)).addBooking(eq(1L), any(BookingDto.class));
    }

    @Test
    void finalizeBookingWithValidData() throws Exception {
        Mockito.when(bookingClient.finalizeBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(new BookingDto(
                        null, LocalDateTime.now().plusDays(1), LocalDateTime.now(), 1L, 1L, BookingStatus.APPROVED)));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, times(1)).finalizeBooking(1L, 1L, true);
    }

    @Test
    void addBookingWhenUserIdIsNegative() throws Exception {
        BookingDto validDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", -1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBookingWhenUserIdIsZero() throws Exception {
        BookingDto validDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByIdWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", -1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByIdWhenBookingIdIsNegative() throws Exception {
        mockMvc.perform(get("/bookings/-1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", -1L)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByOwnerWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", -1L)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizeBookingWhenUserIdIsNegative() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", -1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizeBookingWhenBookingIdIsNegative() throws Exception {
        mockMvc.perform(patch("/bookings/-1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByIdWithValidIds() throws Exception {
        BookingDto responseDto = new BookingDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                1L, 1L, BookingStatus.WAITING);

        Mockito.when(bookingClient.getBookingById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(responseDto));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        Mockito.verify(bookingClient, times(1)).getBookingById(1L, 1L);
    }

    @Test
    void getBookingsWithValidState() throws Exception {
        List<BookingDto> responseList = List.of(
                new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        1L, 1L, BookingStatus.WAITING),
                new BookingDto(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4),
                        2L, 1L, BookingStatus.APPROVED)
        );

        Mockito.when(bookingClient.getBookings(anyLong(), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        Mockito.verify(bookingClient, times(1)).getBookings(1L, BookingState.ALL);
    }

    @Test
    void getBookingsWithDefaultState() throws Exception {
        Mockito.when(bookingClient.getBookings(anyLong(), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, times(1)).getBookings(1L, BookingState.ALL);
    }

    @Test
    void getBookingsByOwnerWithValidState() throws Exception {
        List<BookingDto> responseList = List.of(
                new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        1L, 2L, BookingStatus.WAITING)
        );

        Mockito.when(bookingClient.getBookingsByOwner(anyLong(), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok(responseList));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        Mockito.verify(bookingClient, times(1)).getBookingsByOwner(1L, BookingState.WAITING);
    }

    @Test
    void getBookingsByOwnerWithDefaultState() throws Exception {
        Mockito.when(bookingClient.getBookingsByOwner(anyLong(), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        Mockito.verify(bookingClient, times(1)).getBookingsByOwner(1L, BookingState.ALL);
    }

    @Test
    void getBookingsWithInvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unknown state: INVALID_STATE")));
    }

    @Test
    void getBookingsByOwnerWithInvalidState() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Unknown state: INVALID_STATE")));
    }

    @Test
    void addBookingWithoutUserIdHeader() throws Exception {
        BookingDto validDto = new BookingDto(
                null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 1L, 1L, BookingStatus.WAITING);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingByIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void finalizeBookingWithoutUserIdHeader() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError());
    }
}
