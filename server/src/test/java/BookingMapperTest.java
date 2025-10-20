import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingMapperTest {

    private final BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toBookingShouldMapCorrectlyWithNonNullValues() {
        BookingDto bookingDto = new BookingDto(1L, LocalDateTime.now().plusSeconds(5),
                LocalDateTime.now().plusDays(5), null, null, BookingStatus.WAITING);
        bookingDto.setId(1L);
        User user = new User();
        Item item = new Item();

        Booking result = bookingMapper.toBooking(bookingDto, user, item);

        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getBooker()).isEqualTo(user);
        assertThat(result.getId()).isNull();
    }

    @Test
    void toBookingResponseDtoListShouldHandleEmptyList() {
        List<Booking> emptyList = List.of();

        List<BookingResponseDto> result = bookingMapper.toBookingResponseDtoList(emptyList);

        assertThat(result).isEmpty();
    }

    @Test
    void toBookingResponseDtoListShouldHandleNullList() {
        List<BookingResponseDto> result = bookingMapper.toBookingResponseDtoList(null);

        assertThat(result).isNull();
    }
}
