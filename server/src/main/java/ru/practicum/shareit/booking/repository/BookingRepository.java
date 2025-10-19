package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    Booking findByItemId(Long itemId);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    Optional<Booking> findByBookerIdAndItemIdAndEndIsBeforeAndStatus(Long bookerId, Long itemId, LocalDateTime end,
                                                                     BookingStatus status);

    Optional<Booking> findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime end,
                                                                           BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime from);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN ?1 " +
            "AND b.status = ?3 " +
            "AND b.end = (SELECT MAX(b2.end) FROM Booking b2 " +
            "            WHERE b2.item.id = b.item.id " +
            "            AND b2.end < ?2 AND b2.status = ?3)")
    List<Booking> findLastBookingsForItems(List<Long> itemIds,
                                           LocalDateTime now,
                                           BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN ?1 " +
            "AND b.start = (SELECT MIN(b2.start) FROM Booking b2 " +
            "            WHERE b2.item.id = b.item.id " +
            "            AND b2.start > ?2)")
    List<Booking> findNextBookingsForItems(List<Long> itemIds,
                                           LocalDateTime now);
}
