import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import shareit.booking.BookingClient;
import shareit.booking.dto.BookingDto;
import shareit.booking.dto.BookingState;
import shareit.client.BaseClient;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        bookingClient = new BookingClient("http://localhost:8080", new RestTemplateBuilder());
        setRestTemplateField(bookingClient, restTemplate);
    }

    @Test
    void addBookingShouldCallPostWithCorrectParameters() {
        long userId = 1L;
        BookingDto bookingDto = createBookingDto();

        try {
            bookingClient.addBooking(userId, bookingDto);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq(""),
                eq(HttpMethod.POST),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id") &&
                                httpEntity.getBody() == bookingDto
                ),
                eq(Object.class)
        );
    }

    @Test
    void finalizeBookingShouldCallPatchWithCorrectParameters() {
        long userId = 1L;
        long bookingId = 1L;
        boolean approved = true;

        try {
            bookingClient.finalizeBooking(userId, bookingId, approved);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/1?approved={approved}"),
                eq(HttpMethod.PATCH),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id") &&
                                httpEntity.getBody() == null
                ),
                eq(Object.class),
                eq(Map.of("approved", true))
        );
    }

    @Test
    void getBookingByIdShouldCallGetWithCorrectParameters() {
        long userId = 1L;
        long bookingId = 1L;

        try {
            bookingClient.getBookingById(userId, bookingId);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/1"),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class)
        );
    }

    @Test
    void getBookingsShouldCallGetWithCorrectParameters() {
        long userId = 1L;
        BookingState state = BookingState.ALL;

        try {
            bookingClient.getBookings(userId, state);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("?state={state}"),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class),
                eq(Map.of("state", "ALL"))
        );
    }

    @Test
    void getBookingsByOwnerShouldCallGetWithCorrectParameters() {
        long userId = 1L;
        BookingState state = BookingState.ALL;

        try {
            bookingClient.getBookingsByOwner(userId, state);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/owner?state={state}"),
                eq(HttpMethod.GET),
                argThat(httpEntity ->
                        httpEntity != null &&
                                httpEntity.getHeaders().containsKey("X-Sharer-User-Id")
                ),
                eq(Object.class),
                eq(Map.of("state", "ALL"))
        );
    }

    @Test
    void getBookingsWithDifferentStateShouldCallGetWithCorrectState() {
        long userId = 1L;
        BookingState state = BookingState.CURRENT;

        try {
            bookingClient.getBookings(userId, state);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("?state={state}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("state", "CURRENT"))
        );
    }

    @Test
    void getBookingsByOwnerWithDifferentStateShouldCallGetWithCorrectState() {
        long userId = 1L;
        BookingState state = BookingState.FUTURE;

        try {
            bookingClient.getBookingsByOwner(userId, state);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/owner?state={state}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("state", "FUTURE"))
        );
    }

    @Test
    void finalizeBookingWithFalseApprovedShouldCallPatchWithCorrectParameters() {
        long userId = 1L;
        long bookingId = 2L;
        boolean approved = false;

        try {
            bookingClient.finalizeBooking(userId, bookingId, approved);
        } catch (Exception ignored) {

        }

        verify(restTemplate).exchange(
                eq("/2?approved={approved}"),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("approved", false))
        );
    }

    private BookingDto createBookingDto() {
        return new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null,
                null,
                null
        );
    }

    private void setRestTemplateField(BaseClient client, RestTemplate restTemplate) {
        try {
            Field restTemplateField = BaseClient.class.getDeclaredField("rest");
            restTemplateField.setAccessible(true);
            restTemplateField.set(client, restTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set restTemplate field", e);
        }
    }
}
