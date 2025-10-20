package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {

    @Mapping(target = "item", source = "booking.item")
    @Mapping(target = "booker", source = "booking.booker")
    BookingResponseDto toBookingResponseDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "user")
    Booking toBooking(BookingDto bookingDto, User user, Item item);

    List<BookingResponseDto> toBookingResponseDtoList(List<Booking> bookings);
}
