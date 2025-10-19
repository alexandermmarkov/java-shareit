import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@ContextConfiguration(classes = ShareItServer.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = new UserDto(1L, "Booker", "booker@example.com");
    private final UserDto ownerDto = new UserDto(2L, "Owner", "owner@example.com");
    private final ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", ownerDto, true, null);

    private final BookingDto bookingDto = new BookingDto(
            null,
            LocalDateTime.of(2024, 1, 1, 10, 0, 0),
            LocalDateTime.of(2024, 1, 2, 10, 0, 0),
            1L,
            null,
            null);

    private final BookingResponseDto bookingResponseDto = new BookingResponseDto(
            1L,
            LocalDateTime.of(2024, 1, 1, 10, 0, 0),
            LocalDateTime.of(2024, 1, 2, 10, 0, 0),
            itemDto,
            userDto,
            BookingStatus.WAITING);

    @Test
    void addBooking() throws Exception {
        Mockito.when(bookingService.addBooking(anyLong(), any(BookingDto.class)))
                .thenReturn(bookingResponseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingResponseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingResponseDto.getBooker().getId()), Long.class));
    }

    @Test
    void finalizeBookingApprove() throws Exception {
        BookingResponseDto approvedBooking = new BookingResponseDto(
                1L,
                LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0, 0),
                itemDto,
                userDto,
                BookingStatus.APPROVED);

        Mockito.when(bookingService.finalizeBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(approvedBooking);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(approvedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(approvedBooking.getStatus().toString())));
    }

    @Test
    void finalizeBookingReject() throws Exception {
        BookingResponseDto rejectedBooking = new BookingResponseDto(
                1L,
                LocalDateTime.of(2024, 1, 1, 10, 0, 0),
                LocalDateTime.of(2024, 1, 2, 10, 0, 0),
                itemDto,
                userDto,
                BookingStatus.REJECTED);

        Mockito.when(bookingService.finalizeBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(rejectedBooking);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(rejectedBooking.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(rejectedBooking.getStatus().toString())));
    }

    @Test
    void getBookingById() throws Exception {
        Mockito.when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingResponseDto.getStatus().toString())))
                .andExpect(jsonPath("$.item.name", is(bookingResponseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.name", is(bookingResponseDto.getBooker().getName())));
    }

    @Test
    void getBookingsByUserWithState() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        Mockito.when(bookingService.getBookingsByUser(anyLong(), anyString()))
                .thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    void getBookingsByUserWithDefaultState() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        Mockito.when(bookingService.getBookingsByUser(anyLong(), anyString()))
                .thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class));
    }

    @Test
    void getBookingsByOwnerWithState() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        Mockito.when(bookingService.getBookingsByOwner(anyLong(), anyString()))
                .thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "WAITING")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingResponseDto.getStatus().toString())));
    }

    @Test
    void getBookingsByOwnerWithDefaultState() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        Mockito.when(bookingService.getBookingsByOwner(anyLong(), anyString()))
                .thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingResponseDto.getId()), Long.class));
    }

    @Test
    void addBookingWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(bookingService.addBooking(anyLong(), any(BookingDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingWhenServiceThrowsValidationException() throws Exception {
        Mockito.when(bookingService.addBooking(anyLong(), any(BookingDto.class)))
                .thenThrow(new ValidationException("Item is not available"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void finalizeBookingWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(bookingService.finalizeBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("Booking not found"));

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingByIdWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByUserWhenServiceThrowsValidationException() throws Exception {
        Mockito.when(bookingService.getBookingsByUser(anyLong(), anyString()))
                .thenThrow(new ValidationException("Unknown state: INVALID_STATE"));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID_STATE")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByOwnerWhenServiceThrowsNotFoundException() throws Exception {
        Mockito.when(bookingService.getBookingsByOwner(anyLong(), anyString()))
                .thenThrow(new NotFoundException("User has no items"));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addBookingWithoutUserIdHeader() throws Exception {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingByIdWithoutUserIdHeader() throws Exception {
        mvc.perform(get("/bookings/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void finalizeBookingWithoutApprovedParam() throws Exception {
        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
