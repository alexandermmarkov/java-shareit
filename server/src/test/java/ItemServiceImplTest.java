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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
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
class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService service;

    @Test
    void testAddItem() {
        User owner = makeUser("owner@email.com", "Owner");
        em.persist(owner);
        em.flush();

        ItemDto itemDto = makeItemDto("Drill", "Powerful drill", true, null);

        ItemDto result = service.addItem(owner.getId(), itemDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item savedItem = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedItem.getName(), equalTo(itemDto.getName()));
        assertThat(savedItem.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(savedItem.getOwner().getId(), equalTo(owner.getId()));
    }

    @Test
    void testAddItemWithRequest() {
        User owner = makeUser("owner2@email.com", "Owner2");
        em.persist(owner);

        User requestor = makeUser("requestor@email.com", "Requestor");
        em.persist(requestor);

        ItemRequest request = makeItemRequest("Need a drill", requestor);
        em.persist(request);

        em.flush();

        ItemDto itemDto = makeItemDto("Drill", "Powerful drill", true, request.getId());

        ItemDto result = service.addItem(owner.getId(), itemDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getRequestId(), equalTo(request.getId()));

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item savedItem = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedItem.getRequest().getId(), equalTo(request.getId()));
    }

    @Test
    void testAddItemWhenUserNotFound() {
        ItemDto itemDto = makeItemDto("Drill", "Powerful drill", true, null);

        assertThrows(NotFoundException.class, () -> service.addItem(9999L, itemDto));
    }

    @Test
    void testAddItemWhenRequestNotFound() {
        User owner = makeUser("owner3@email.com", "Owner3");
        em.persist(owner);
        em.flush();

        ItemDto itemDto = makeItemDto("Drill", "Powerful drill", true, 9999L);

        assertThrows(NotFoundException.class, () -> service.addItem(owner.getId(), itemDto));
    }

    @Test
    void testUpdateItem() {
        User owner = makeUser("owner4@email.com", "Owner4");
        em.persist(owner);

        Item item = makeItem("Old Drill", "Old description", owner, true, null);
        em.persist(item);

        em.flush();

        ItemDto updateDto = makeItemDto("Updated Drill", "Updated description", false, null);

        ItemDto result = service.updateItem(owner.getId(), item.getId(), updateDto);

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(updateDto.getName()));
        assertThat(result.getDescription(), equalTo(updateDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(updateDto.getAvailable()));

        TypedQuery<Item> query = em.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item updatedItem = query.setParameter("id", item.getId()).getSingleResult();

        assertThat(updatedItem.getName(), equalTo(updateDto.getName()));
        assertThat(updatedItem.getDescription(), equalTo(updateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), equalTo(updateDto.getAvailable()));
    }

    @Test
    void testGetItemWithDateByIdForOwner() {
        User owner = makeUser("owner5@email.com", "Owner5");
        em.persist(owner);

        User booker = makeUser("booker@email.com", "Booker");
        em.persist(booker);

        Item item = makeItem("Item5", "Description", owner, true, null);
        em.persist(item);

        Booking lastBooking = makeBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED
        );
        em.persist(lastBooking);

        Booking nextBooking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.APPROVED
        );
        em.persist(nextBooking);

        Comment comment = makeComment("Great item!", booker, item);
        em.persist(comment);

        em.flush();

        ItemWithDateDto result = service.getItemWithDateById(owner.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getLastBooking(), notNullValue());
        assertThat(result.getNextBooking(), notNullValue());
        assertThat(result.getComments(), hasSize(1));
        assertThat(result.getComments().getFirst().getText(), equalTo("Great item!"));
    }

    @Test
    void testGetItemWithDateByIdForNonOwner() {
        User owner = makeUser("owner6@email.com", "Owner6");
        em.persist(owner);

        User otherUser = makeUser("other@email.com", "Other");
        em.persist(otherUser);

        Item item = makeItem("Item6", "Description", owner, true, null);
        em.persist(item);

        em.flush();

        ItemWithDateDto result = service.getItemWithDateById(otherUser.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getLastBooking(), nullValue());
        assertThat(result.getNextBooking(), nullValue());
        assertThat(result.getComments(), hasSize(0));
    }

    @Test
    void testGetItemWithDateByIdWhenItemNotFound() {
        User user = makeUser("user@email.com", "User");
        em.persist(user);
        em.flush();

        assertThrows(NotFoundException.class, () -> service.getItemWithDateById(user.getId(), 9999L));
    }

    @Test
    void testGetItems() {
        User owner = makeUser("owner7@email.com", "Owner7");
        em.persist(owner);

        Item item1 = makeItem("Item1", "Description1", owner, true, null);
        em.persist(item1);

        Item item2 = makeItem("Item2", "Description2", owner, true, null);
        em.persist(item2);

        User booker = makeUser("booker7@email.com", "Booker7");
        em.persist(booker);

        Booking lastBooking = makeBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item1, booker, BookingStatus.APPROVED
        );
        em.persist(lastBooking);

        Booking nextBooking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.APPROVED
        );
        em.persist(nextBooking);

        Comment comment1 = makeComment("Good item", booker, item1);
        em.persist(comment1);

        em.flush();

        List<ItemWithDateDto> result = service.getItems(owner.getId());

        assertThat(result, hasSize(2));

        ItemWithDateDto itemWithDate1 = result.stream()
                .filter(i -> i.getId().equals(item1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(itemWithDate1.getLastBooking(), notNullValue());
        assertThat(itemWithDate1.getNextBooking(), notNullValue());
        assertThat(itemWithDate1.getComments(), hasSize(1));

        ItemWithDateDto itemWithDate2 = result.stream()
                .filter(i -> i.getId().equals(item2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(itemWithDate2.getLastBooking(), nullValue());
        assertThat(itemWithDate2.getNextBooking(), nullValue());
        assertThat(itemWithDate2.getComments(), hasSize(0));
    }

    @Test
    void testGetItemsWhenUserHasNoItems() {
        User user = makeUser("nouser@email.com", "NoItemsUser");
        em.persist(user);
        em.flush();

        List<ItemWithDateDto> result = service.getItems(user.getId());

        assertThat(result, hasSize(0));
    }

    @Test
    void testSearchItem() {
        User owner = makeUser("owner8@email.com", "Owner8");
        em.persist(owner);

        Item item1 = makeItem("Drill", "Powerful electric drill", owner, true, null);
        em.persist(item1);

        Item item2 = makeItem("Hammer", "Heavy hammer", owner, true, null);
        em.persist(item2);

        Item unavailableItem = makeItem("Broken Drill", "Not working", owner, false, null);
        em.persist(unavailableItem);

        em.flush();

        List<ItemDto> result = service.searchItem("drill");

        assertThat(result, hasSize(1));
        assertThat(result.getFirst().getName(), equalTo("Drill"));
    }

    @Test
    void testSearchItemWithEmptyText() {
        User owner = makeUser("owner9@email.com", "Owner9");
        em.persist(owner);

        Item item = makeItem("Item", "Description", owner, true, null);
        em.persist(item);

        em.flush();

        List<ItemDto> result1 = service.searchItem("");
        List<ItemDto> result2 = service.searchItem("   ");
        List<ItemDto> result3 = service.searchItem(null);

        assertThat(result1, hasSize(0));
        assertThat(result2, hasSize(0));
        assertThat(result3, hasSize(0));
    }

    @Test
    void testSearchItemNoResults() {
        User owner = makeUser("owner10@email.com", "Owner10");
        em.persist(owner);

        Item item = makeItem("Hammer", "Heavy hammer", owner, true, null);
        em.persist(item);

        em.flush();

        List<ItemDto> result = service.searchItem("drill");

        assertThat(result, hasSize(0));
    }

    @Test
    void testAddComment() {
        User owner = makeUser("owner11@email.com", "Owner11");
        em.persist(owner);

        User booker = makeUser("booker11@email.com", "Booker11");
        em.persist(booker);

        Item item = makeItem("Item11", "Description", owner, true, null);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED
        );
        em.persist(booking);

        em.flush();

        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);

        CommentDto result = service.addComment(booker.getId(), item.getId(), commentDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getText(), equalTo("Great item!"));
        assertThat(result.getAuthorName(), equalTo(booker.getName()));
        assertThat(result.getCreated(), notNullValue());

        TypedQuery<Comment> query = em.createQuery("SELECT c FROM Comment c WHERE c.id = :id", Comment.class);
        Comment savedComment = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedComment.getText(), equalTo("Great item!"));
        assertThat(savedComment.getAuthor().getId(), equalTo(booker.getId()));
        assertThat(savedComment.getItem().getId(), equalTo(item.getId()));
    }

    @Test
    void testAddComment_WhenUserNotBookedItem_ShouldThrowException() {
        User owner = makeUser("owner12@email.com", "Owner12");
        em.persist(owner);

        User stranger = makeUser("stranger@email.com", "Stranger");
        em.persist(stranger);

        Item item = makeItem("Item12", "Description", owner, true, null);
        em.persist(item);

        em.flush();

        CommentDto commentDto = new CommentDto(null, "Nice item!", null, null);

        assertThrows(ValidationException.class, () -> service.addComment(stranger.getId(), item.getId(), commentDto));
    }

    @Test
    void testAddComment_WhenBookingNotEnded_ShouldThrowException() {
        User owner = makeUser("owner13@email.com", "Owner13");
        em.persist(owner);

        User booker = makeUser("booker13@email.com", "Booker13");
        em.persist(booker);

        Item item = makeItem("Item13", "Description", owner, true, null);
        em.persist(item);

        Booking booking = makeBooking(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.APPROVED
        );
        em.persist(booking);

        em.flush();

        CommentDto commentDto = new CommentDto(null, "Comment", null, null);

        assertThrows(ValidationException.class, () -> service.addComment(booker.getId(), item.getId(), commentDto));
    }

    @Test
    void testAddComment_WhenItemNotFound_ShouldThrowException() {
        User user = makeUser("user14@email.com", "User14");
        em.persist(user);
        em.flush();

        CommentDto commentDto = new CommentDto(null, "Comment", null, null);

        assertThrows(NotFoundException.class, () -> service.addComment(user.getId(), 9999L, commentDto));
    }

    @Test
    void testAddComment_WhenUserNotFound_ShouldThrowException() {
        User owner = makeUser("owner15@email.com", "Owner15");
        em.persist(owner);

        Item item = makeItem("Item15", "Description", owner, true, null);
        em.persist(item);

        em.flush();

        CommentDto commentDto = new CommentDto(null, "Comment", null, null);

        assertThrows(NotFoundException.class, () -> service.addComment(9999L, item.getId(), commentDto));
    }

    @Test
    void testGetItemById() {
        User owner = makeUser("owner16@email.com", "Owner16");
        em.persist(owner);

        Item item = makeItem("Item16", "Description", owner, true, null);
        em.persist(item);

        em.flush();

        ItemWithDateDto result = service.getItemWithDateById(owner.getId(), item.getId());

        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private Item makeItem(String name, String description, User owner, boolean available, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setOwner(owner);
        item.setAvailable(available);
        item.setRequest(request);
        return item;
    }

    private ItemDto makeItemDto(String name, String description, boolean available, Long requestId) {
        return new ItemDto(null, name, description, null, available, requestId);
    }

    private ItemRequest makeItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
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

    private Comment makeComment(String text, User author, Item item) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}
