import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItServer.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private final EntityManager em;
    private final BookingService service;

    @Test
    void testAddBooking() {
        User owner = makeUser("owner@email.com", "Owner", "User");
        em.persist(owner);

        User booker = makeUser("booker@email.com", "Booker", "User");
        em.persist(booker);

        Item item = makeItem("Drill", "Powerful drill", owner, true);
        em.persist(item);

        em.flush();

        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId(),
                null,
                null,
                null
        );

        BookingResponseDto result = service.addBooking(booker.getId(), bookingDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking savedBooking = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedBooking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(savedBooking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(savedBooking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(savedBooking.getItem().getId(), equalTo(item.getId()));
        assertThat(savedBooking.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void testAddBooking_WhenItemNotAvailable_ShouldThrowException() {
        User owner = makeUser("owner2@email.com", "Owner2", "User");
        em.persist(owner);

        User booker = makeUser("booker2@email.com", "Booker2", "User");
        em.persist(booker);

        Item unavailableItem = makeItem("Unavailable Item", "Not available", owner, false);
        em.persist(unavailableItem);

        em.flush();

        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                unavailableItem.getId(),
                null,
                null,
                null
        );

        assertThrows(ValidationException.class, () -> service.addBooking(booker.getId(), bookingDto));
    }

    @Test
    void testAddBooking_WhenItemNotFound_ShouldThrowException() {
        User booker = makeUser("booker3@email.com", "Booker3", "User");
        em.persist(booker);
        em.flush();

        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                9999L,
                null,
                null,
                null
        );

        assertThrows(NotFoundException.class, () -> service.addBooking(booker.getId(), bookingDto));
    }

    @Test
    void testAddBooking_WhenUserNotFound_ShouldThrowException() {
        User owner = makeUser("owner4@email.com", "Owner4", "User");
        em.persist(owner);

        Item item = makeItem("Item4", "Description", owner, true);
        em.persist(item);

        em.flush();

        BookingDto bookingDto = makeBookingDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId(),
                null,
                null,
                null
        );

        assertThrows(NotFoundException.class, () -> service.addBooking(9999L, bookingDto));
    }

    @Test
    void testFinalizeBooking_Approve() {
        User owner = makeUser("owner5@email.com", "Owner5", "User");
        em.persist(owner);

        User booker = makeUser("booker5@email.com", "Booker5", "User");
        em.persist(booker);

        Item item = makeItem("Item5", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        BookingResponseDto result = service.finalizeBooking(owner.getId(), booking.getId(), true);

        assertThat(result.getStatus(), equalTo(BookingStatus.APPROVED));

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking updatedBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void testFinalizeBooking_Reject() {
        User owner = makeUser("owner6@email.com", "Owner6", "User");
        em.persist(owner);

        User booker = makeUser("booker6@email.com", "Booker6", "User");
        em.persist(booker);

        Item item = makeItem("Item6", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        BookingResponseDto result = service.finalizeBooking(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED));

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking updatedBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void testFinalizeBooking_WhenNotOwner_ShouldThrowException() {
        User owner = makeUser("owner7@email.com", "Owner7", "User");
        em.persist(owner);

        User booker = makeUser("booker7@email.com", "Booker7", "User");
        em.persist(booker);

        User stranger = makeUser("stranger@email.com", "Stranger", "User");
        em.persist(stranger);

        Item item = makeItem("Item7", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        assertThrows(ValidationException.class, () -> service.finalizeBooking(stranger.getId(), booking.getId(), true));
    }

    @Test
    void testGetBookingById() {
        User owner = makeUser("owner8@email.com", "Owner8", "User");
        em.persist(owner);

        User booker = makeUser("booker8@email.com", "Booker8", "User");
        em.persist(booker);

        Item item = makeItem("Item8", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        BookingResponseDto resultByBooker = service.getBookingById(booker.getId(), booking.getId());
        assertThat(resultByBooker.getId(), equalTo(booking.getId()));

        BookingResponseDto resultByOwner = service.getBookingById(owner.getId(), booking.getId());
        assertThat(resultByOwner.getId(), equalTo(booking.getId()));
    }

    @Test
    void testGetBookingById_WhenNotBookerOrOwner_ShouldThrowException() {
        User owner = makeUser("owner9@email.com", "Owner9", "User");
        em.persist(owner);

        User booker = makeUser("booker9@email.com", "Booker9", "User");
        em.persist(booker);

        User stranger = makeUser("stranger9@email.com", "Stranger9", "User");
        em.persist(stranger);

        Item item = makeItem("Item9", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        assertThrows(ValidationException.class, () -> service.getBookingById(stranger.getId(), booking.getId()));
    }

    @Test
    void testGetBookingsByUser() {
        User owner = makeUser("owner10@email.com", "Owner10", "User");
        em.persist(owner);

        User booker = makeUser("booker10@email.com", "Booker10", "User");
        em.persist(booker);

        Item item = makeItem("Item10", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        List<BookingResponseDto> result = service.getBookingsByUser(booker.getId(), "ALL");

        assertThat(result, hasSize(1));
        assertThat(result.getFirst().getId(), equalTo(booking.getId()));
    }

    @Test
    void testGetBookingsByOwner() {
        User owner = makeUser("owner11@email.com", "Owner11", "User");
        em.persist(owner);

        User booker = makeUser("booker11@email.com", "Booker11", "User");
        em.persist(booker);

        Item item = makeItem("Item11", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(booking);

        em.flush();

        List<BookingResponseDto> result = service.getBookingsByOwner(owner.getId(), "ALL");

        assertThat(result, hasSize(1));
        assertThat(result.getFirst().getId(), equalTo(booking.getId()));
    }

    @Test
    void testGetBookingsByOwner_WhenNoItems_ShouldThrowException() {
        User userWithoutItems = makeUser("noitems@email.com", "NoItems", "User");
        em.persist(userWithoutItems);
        em.flush();

        assertThrows(NotFoundException.class, () -> service.getBookingsByOwner(userWithoutItems.getId(), "ALL"));
    }

    private User makeUser(String email, String name, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Item makeItem(String name, String description, User owner, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setOwner(owner);
        item.setAvailable(available);
        return item;
    }

    private BookingDto makeBookingDto(LocalDateTime start, LocalDateTime end, Long itemId,
                                      Long id, Long bookerId, BookingStatus status) {
        return new BookingDto(id, start, end, itemId, bookerId, status);
    }

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}
