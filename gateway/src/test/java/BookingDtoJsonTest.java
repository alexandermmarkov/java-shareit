import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import shareit.ShareItGateway;
import shareit.booking.dto.BookingDto;
import shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ContextConfiguration(classes = ShareItGateway.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingDtoJsonTest {
    private final JacksonTester<BookingDto> json;

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0, 0);

        BookingDto bookingDto = new BookingDto(
                1L,
                start,
                end,
                100L,
                200L,
                BookingStatus.WAITING);

        JsonContent<BookingDto> result = json.write(bookingDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-01-02T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(100);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(200);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testBookingDtoDeserialization() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"start\": \"2024-01-01T10:00:00\"," +
                "\"end\": \"2024-01-02T10:00:00\"," +
                "\"itemId\": 100," +
                "\"bookerId\": 200," +
                "\"status\": \"APPROVED\"" +
                "}";

        BookingDto bookingDto = json.parseObject(content);

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        assertThat(bookingDto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0, 0));
        assertThat(bookingDto.getItemId()).isEqualTo(100L);
        assertThat(bookingDto.getBookerId()).isEqualTo(200L);
        assertThat(bookingDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}
