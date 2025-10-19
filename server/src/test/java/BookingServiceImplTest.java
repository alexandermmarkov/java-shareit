import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItServer.class)
public class BookingServiceImplTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private BookingService service;

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

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking savedBooking = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedBooking.getStart()).isEqualTo(bookingDto.getStart());
        assertThat(savedBooking.getEnd()).isEqualTo(bookingDto.getEnd());
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(savedBooking.getItem().getId()).isEqualTo(item.getId());
        assertThat(savedBooking.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void testAddBookingWhenItemNotAvailable() {
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
    void testAddBookingWhenItemNotFound() {
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
    void testAddBookingWhenUserNotFound() {
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
    void testAddBookingWhenBookingOwnItem() {
        User owner = makeUser("owner_own@email.com", "Owner", "Own");
        em.persist(owner);

        Item item = makeItem("Own Item", "Description", owner, true);
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

        assertThrows(ValidationException.class, () -> service.addBooking(owner.getId(), bookingDto));
    }

    @Test
    void testFinalizeBookingApprove() {
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

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking updatedBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void testFinalizeBookingReject() {
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

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking updatedBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFinalizeBookingWhenBookingAlreadyApproved() {
        User owner = makeUser("owner_approved@email.com", "Owner", "Approved");
        em.persist(owner);

        User booker = makeUser("booker_approved@email.com", "Booker", "Approved");
        em.persist(booker);

        Item item = makeItem("Item Approved", "Description", owner, true);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item,
                booker,
                BookingStatus.APPROVED
        );
        em.persist(booking);
        em.flush();

        BookingResponseDto result = service.finalizeBooking(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);

        TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking updatedBooking = query.setParameter("id", booking.getId()).getSingleResult();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void testFinalizeBooking_WhenNotOwner() {
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
    void testFinalizeBookingWhenBookingNotFound() {
        User owner = makeUser("owner_notfound@email.com", "Owner", "NotFound");
        em.persist(owner);
        em.flush();

        assertThrows(NotFoundException.class, () ->
                service.finalizeBooking(owner.getId(), 9999L, true));
    }

    @Test
    void testFinalizeBookingWhenUserNotFound() {
        User owner = makeUser("owner_finalize@email.com", "Owner", "Finalize");
        em.persist(owner);

        User booker = makeUser("booker_finalize@email.com", "Booker", "Finalize");
        em.persist(booker);

        Item item = makeItem("Item Finalize", "Description", owner, true);
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

        assertThrows(ValidationException.class, () ->
                service.finalizeBooking(9999L, booking.getId(), true));
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
        assertThat(resultByBooker.getId()).isEqualTo(booking.getId());

        BookingResponseDto resultByOwner = service.getBookingById(owner.getId(), booking.getId());
        assertThat(resultByOwner.getId()).isEqualTo(booking.getId());
    }

    @Test
    void testGetBookingByIdWhenNotBookerOrOwner() {
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
    void testGetBookingByIdWhenBookingNotFound() {
        User user = makeUser("user_notfound@email.com", "User", "NotFound");
        em.persist(user);
        em.flush();

        assertThrows(NotFoundException.class, () ->
                service.getBookingById(user.getId(), 9999L));
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

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
    }

    @Test
    void testGetBookingsByUserWithDifferentStates() {
        User owner = makeUser("owner_states@email.com", "Owner", "States");
        em.persist(owner);

        User booker = makeUser("booker_states@email.com", "Booker", "States");
        em.persist(booker);

        Item item = makeItem("Item States", "Description", owner, true);
        em.persist(item);

        Booking pastBooking = makeBooking(
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        );
        em.persist(pastBooking);

        Booking currentBooking = makeBooking(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        );
        em.persist(currentBooking);

        Booking futureBooking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(futureBooking);

        Booking rejectedBooking = makeBooking(
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(6),
                item,
                booker,
                BookingStatus.REJECTED
        );
        em.persist(rejectedBooking);

        em.flush();

        assertThat(service.getBookingsByUser(booker.getId(), "ALL")).hasSize(4);
        assertThat(service.getBookingsByUser(booker.getId(), "PAST")).hasSize(1);
        assertThat(service.getBookingsByUser(booker.getId(), "CURRENT")).hasSize(1);
        assertThat(service.getBookingsByUser(booker.getId(), "FUTURE")).hasSize(2);
        assertThat(service.getBookingsByUser(booker.getId(), "WAITING")).hasSize(1);
        assertThat(service.getBookingsByUser(booker.getId(), "REJECTED")).hasSize(1);
    }

    @Test
    void testGetBookingsByUserWithInvalidState() {
        User booker = makeUser("booker_invalid@email.com", "Booker", "Invalid");
        em.persist(booker);
        em.flush();

        assertThrows(ValidationException.class, () ->
                service.getBookingsByUser(booker.getId(), "INVALID_STATE"));
    }

    @Test
    void testGetBookingsByUserWithLowerCaseState() {
        User owner = makeUser("owner_lower@email.com", "Owner", "Lower");
        em.persist(owner);

        User booker = makeUser("booker_lower@email.com", "Booker", "Lower");
        em.persist(booker);

        Item item = makeItem("Item Lower", "Description", owner, true);
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

        List<BookingResponseDto> result = service.getBookingsByUser(booker.getId(), "all");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
    }

    @Test
    void testGetBookingsByUserEmptyResult() {
        User booker = makeUser("booker_empty@email.com", "Booker", "Empty");
        em.persist(booker);
        em.flush();

        List<BookingResponseDto> result = service.getBookingsByUser(booker.getId(), "ALL");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetBookingsByUserWhenUserNotFound() {
        assertThrows(NotFoundException.class, () ->
                service.getBookingsByUser(9999L, "ALL"));
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

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
    }

    @Test
    void testGetBookingsByOwnerWithDifferentStates() {
        User owner = makeUser("owner_states2@email.com", "Owner", "States2");
        em.persist(owner);

        User booker = makeUser("booker_states2@email.com", "Booker", "States2");
        em.persist(booker);

        Item item = makeItem("Item States2", "Description", owner, true);
        em.persist(item);

        Booking pastBooking = makeBooking(
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        );
        em.persist(pastBooking);

        Booking futureBooking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3),
                item,
                booker,
                BookingStatus.WAITING
        );
        em.persist(futureBooking);

        em.flush();

        assertThat(service.getBookingsByOwner(owner.getId(), "ALL")).hasSize(2);
        assertThat(service.getBookingsByOwner(owner.getId(), "PAST")).hasSize(1);
        assertThat(service.getBookingsByOwner(owner.getId(), "FUTURE")).hasSize(1);
        assertThat(service.getBookingsByOwner(owner.getId(), "WAITING")).hasSize(1);
    }

    @Test
    void testGetBookingsByOwnerWhenNoItems() {
        User userWithoutItems = makeUser("noitems@email.com", "NoItems", "User");
        em.persist(userWithoutItems);
        em.flush();

        assertThrows(NotFoundException.class, () -> service.getBookingsByOwner(userWithoutItems.getId(), "ALL"));
    }

    @Test
    void testGetBookingsByOwnerWithInvalidState() {
        User owner = makeUser("owner_invalid@email.com", "Owner", "Invalid");
        em.persist(owner);

        Item item = makeItem("Item Invalid", "Description", owner, true);
        em.persist(item);
        em.flush();

        assertThrows(ValidationException.class, () ->
                service.getBookingsByOwner(owner.getId(), "INVALID_STATE"));
    }

    @Test
    void testGetBookingsByOwnerEmptyResult() {
        User owner = makeUser("owner_empty@email.com", "Owner", "Empty");
        em.persist(owner);

        Item item = makeItem("Item Empty", "Description", owner, true);
        em.persist(item);
        em.flush();

        List<BookingResponseDto> result = service.getBookingsByOwner(owner.getId(), "ALL");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetBookingsByOwnerWhenUserNotFound() {
        assertThrows(NotFoundException.class, () ->
                service.getBookingsByOwner(9999L, "ALL"));
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
