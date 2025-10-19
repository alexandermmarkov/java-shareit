package ru.practicum.shareit.booking.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.QBooking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto addBooking(Long userId, BookingDto bookingDto) {
        log.debug("addBooking(userId={}, bookingDto={})", userId, bookingDto);

        User user = getUserIfExists(userId);
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(() -> {
            log.debug(itemRepository.findAll().stream().map(Item::getId).toList().toString());
            return new NotFoundException("Вещь с ID = '" + bookingDto.getItemId() + "' не найдена");
        });

        if (Objects.equals(userId, item.getOwner().getId())) {
            throw new ValidationException("Нельзя бронировать собственную вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с ID = '"
                    + bookingDto.getItemId() + "' на данный момент недоступна для брони");
        }
        bookingDto.setStatus(BookingStatus.WAITING);
        bookingDto.setBookerId(userId);
        Booking booking = repository.save(bookingMapper.toBooking(bookingDto, user, item));

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto finalizeBooking(Long userId, Long bookingId, Boolean approved) {
        log.debug("finilizeBooking(userId={}, bookingId={}, approved={})", userId, bookingId, approved);

        Booking booking = getBookingIfExists(bookingId);
        if (!Objects.equals(userId, booking.getItem().getOwner().getId())) {
            throw new ValidationException("Пользователь с ID='" + userId + "' " +
                    "не является владельцем вещи с ID='" + booking.getItem().getOwner().getId() + "'");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return bookingMapper.toBookingResponseDto(repository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        log.debug("getBookingById(userId={}, bookingId={})", userId, bookingId);

        getUserIfExists(userId);
        Booking booking = getBookingIfExists(bookingId);
        if (!Objects.equals(userId, booking.getBooker().getId())
                && !Objects.equals(userId, booking.getItem().getOwner().getId())) {
            log.debug("User ID = {}, Booker ID = {}, Owner ID = {}",
                    userId,
                    booking.getBooker().getId(),
                    booking.getItem().getOwner().getId());
            throw new ValidationException("Пользователь с ID='" + userId + "' " +
                    "не является владельцем или автором бронирования вещи с ID='" + booking.getItem().getId() + "'");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsByUser(Long userId, String state) {
        log.debug("getBookingByState(userId={}, state={})", userId, state);

        getUserIfExists(userId);
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Значение параметра запроса state '" + state + "' некорректно");
        }

        BooleanExpression byUserId = QBooking.booking.booker.id.eq(userId);
        BooleanExpression byUserIdAndState = switch (bookingState) {
            case ALL -> byUserId;
            case CURRENT -> byUserId.and(
                    QBooking.booking.start.loe(LocalDateTime.now())
                            .and(QBooking.booking.end.goe(LocalDateTime.now()))
            );
            case PAST -> byUserId.and(QBooking.booking.end.lt(LocalDateTime.now()));
            case FUTURE -> byUserId.and(QBooking.booking.start.gt(LocalDateTime.now()));
            case WAITING -> byUserId.and(QBooking.booking.status.eq(BookingStatus.WAITING));
            case REJECTED -> byUserId.and(QBooking.booking.status.eq(BookingStatus.REJECTED));
        };

        Iterable<Booking> foundBookings = repository.findAll(byUserIdAndState);
        List<Booking> foundBookingsList = new ArrayList<>();
        foundBookings.forEach(foundBookingsList::add);

        return bookingMapper.toBookingResponseDtoList(foundBookingsList);
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long userId, String state) {
        log.debug("getBookingsByOwner(userId={}, state={})", userId, state);

        getUserIfExists(userId);
        BookingState bookingState;

        if (itemRepository.findByOwnerId(userId).isEmpty()) {
            throw new NotFoundException("У пользователя с ID = '" + userId + "' нет вещей");
        }

        try {
            bookingState = BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Значение параметра запроса state '" + state + "' некорректно");
        }

        BooleanExpression byOwnerId = QBooking.booking.item.owner.id.eq(userId);
        BooleanExpression byOwnerIdAndState = switch (bookingState) {
            case ALL -> byOwnerId;
            case CURRENT -> byOwnerId.and(
                    QBooking.booking.start.loe(LocalDateTime.now())
                            .and(QBooking.booking.end.goe(LocalDateTime.now()))
            );
            case PAST -> byOwnerId.and(QBooking.booking.end.lt(LocalDateTime.now()));
            case FUTURE -> byOwnerId.and(QBooking.booking.start.gt(LocalDateTime.now()));
            case WAITING -> byOwnerId.and(QBooking.booking.status.eq(BookingStatus.WAITING));
            case REJECTED -> byOwnerId.and(QBooking.booking.status.eq(BookingStatus.REJECTED));
        };

        Iterable<Booking> foundBookings = repository.findAll(byOwnerIdAndState);
        List<Booking> foundBookingsList = new ArrayList<>();
        foundBookings.forEach(foundBookingsList::add);

        return bookingMapper.toBookingResponseDtoList(foundBookingsList);
    }

    public User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.debug(userRepository.findAll().stream().map(User::getId).toList().toString());
                    return new NotFoundException("Пользователь с ID = '" + userId + "' не найден");
                });
    }

    public Booking getBookingIfExists(Long bookingId) {
        return repository.findById(bookingId)
                .orElseThrow(() -> {
                    log.debug(repository.findAll().stream().map(Booking::getId).toList().toString());
                    return new NotFoundException("Бронирование с ID = '" + bookingId + "' не найдено");
                });
    }
}
