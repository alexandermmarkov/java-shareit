import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ShareItServer.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final ItemRequestService service;

    @Test
    void testAddItemRequest() {
        User requestor = makeUser("requestor@email.com", "Requestor");
        em.persist(requestor);
        em.flush();

        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "Need a drill", null, null);

        ItemRequestDto result = service.addItemRequest(requestor.getId(), itemRequestDto);

        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(result.getRequestor().getId(), equalTo(requestor.getId()));
        assertThat(result.getCreated(), notNullValue());

        TypedQuery<ItemRequest> query = em.createQuery("SELECT ir FROM ItemRequest ir WHERE ir.id = :id", ItemRequest.class);
        ItemRequest savedRequest = query.setParameter("id", result.getId()).getSingleResult();

        assertThat(savedRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(savedRequest.getRequestor().getId(), equalTo(requestor.getId()));
        assertThat(savedRequest.getCreated(), notNullValue());
    }

    @Test
    void testAddItemRequestWhenUserNotFound() {
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, "Need a drill", null, null);

        assertThrows(NotFoundException.class, () -> service.addItemRequest(9999L, itemRequestDto));
    }

    @Test
    void testGetItemRequests() {
        User requestor = makeUser("requestor2@email.com", "Requestor2");
        em.persist(requestor);

        ItemRequest request1 = makeItemRequest("Need drill", requestor);
        em.persist(request1);

        ItemRequest request2 = makeItemRequest("Need hammer", requestor);
        em.persist(request2);

        User owner = makeUser("owner@email.com", "Owner");
        em.persist(owner);

        Item item1 = makeItem("Drill", "Powerful drill", owner, true, request1);
        em.persist(item1);

        Item item2 = makeItem("Small Drill", "Compact drill", owner, true, request1);
        em.persist(item2);

        em.flush();

        List<ItemRequestResponseDto> result = service.getItemRequests(requestor.getId());

        assertThat(result, hasSize(2));

        assertThat(result.get(0).getId(), equalTo(request2.getId()));
        assertThat(result.get(1).getId(), equalTo(request1.getId()));

        ItemRequestResponseDto requestWithItems = result.stream()
                .filter(r -> r.getId().equals(request1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(requestWithItems.getItems(), hasSize(2));
        assertThat(requestWithItems.getItems().get(0).getName(), equalTo("Drill"));
        assertThat(requestWithItems.getItems().get(1).getName(), equalTo("Small Drill"));

        ItemRequestResponseDto requestWithoutItems = result.stream()
                .filter(r -> r.getId().equals(request2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(requestWithoutItems.getItems(), hasSize(0));
    }

    @Test
    void testGetItemRequestsWhenNoRequests() {
        User user = makeUser("norequests@email.com", "NoRequests");
        em.persist(user);
        em.flush();

        List<ItemRequestResponseDto> result = service.getItemRequests(user.getId());

        assertThat(result, hasSize(0));
    }

    @Test
    void testGetAllItemRequests() {
        User currentUser = makeUser("current@email.com", "CurrentUser");
        em.persist(currentUser);

        User otherUser1 = makeUser("other1@email.com", "OtherUser1");
        em.persist(otherUser1);

        User otherUser2 = makeUser("other2@email.com", "OtherUser2");
        em.persist(otherUser2);

        ItemRequest request1 = makeItemRequest("Other request 1", otherUser1);
        em.persist(request1);

        ItemRequest request2 = makeItemRequest("Other request 2", otherUser2);
        em.persist(request2);

        ItemRequest ownRequest = makeItemRequest("Own request", currentUser);
        em.persist(ownRequest);

        em.flush();

        List<ItemRequestDto> result = service.getAllItemRequests(currentUser.getId());

        assertThat(result, hasSize(2));

        List<Long> resultIds = result.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
        assertThat(resultIds, containsInAnyOrder(request1.getId(), request2.getId()));
        assertThat(resultIds, not(contains(ownRequest.getId())));

        assertThat(result.get(0).getId(), equalTo(request2.getId()));
        assertThat(result.get(1).getId(), equalTo(request1.getId()));
    }

    @Test
    void testGetAllItemRequestsWhenNoOtherRequests() {
        User user = makeUser("onlyuser@email.com", "OnlyUser");
        em.persist(user);

        ItemRequest ownRequest1 = makeItemRequest("Own request 1", user);
        em.persist(ownRequest1);

        ItemRequest ownRequest2 = makeItemRequest("Own request 2", user);
        em.persist(ownRequest2);

        em.flush();

        List<ItemRequestDto> result = service.getAllItemRequests(user.getId());

        assertThat(result, hasSize(0));
    }

    @Test
    void testGetItemRequestById() {
        User requestor = makeUser("requestor3@email.com", "Requestor3");
        em.persist(requestor);

        User owner = makeUser("owner3@email.com", "Owner3");
        em.persist(owner);

        ItemRequest request = makeItemRequest("Need saw", requestor);
        em.persist(request);

        Item item1 = makeItem("Circular Saw", "Powerful saw", owner, true, request);
        em.persist(item1);

        Item item2 = makeItem("Hand Saw", "Manual saw", owner, true, request);
        em.persist(item2);

        em.flush();

        ItemRequestResponseDto result = service.getItemRequestById(requestor.getId(), request.getId());

        assertThat(result.getId(), equalTo(request.getId()));
        assertThat(result.getDescription(), equalTo(request.getDescription()));
        assertThat(result.getCreated(), equalTo(request.getCreated()));
        assertThat(result.getItems(), hasSize(2));

        List<String> itemNames = result.getItems().stream()
                .map(ItemResponseDto::getName)
                .collect(Collectors.toList());
        assertThat(itemNames, containsInAnyOrder("Circular Saw", "Hand Saw"));
    }

    @Test
    void testGetItemRequestByIdWhenNoItems() {
        User requestor = makeUser("requestor4@email.com", "Requestor4");
        em.persist(requestor);

        ItemRequest request = makeItemRequest("Need something", requestor);
        em.persist(request);

        em.flush();

        ItemRequestResponseDto result = service.getItemRequestById(requestor.getId(), request.getId());

        assertThat(result.getId(), equalTo(request.getId()));
        assertThat(result.getDescription(), equalTo(request.getDescription()));
        assertThat(result.getItems(), hasSize(0));
    }

    @Test
    void testGetItemRequestByIdWhenRequestNotFound() {
        User user = makeUser("user@email.com", "User");
        em.persist(user);
        em.flush();

        assertThrows(NotFoundException.class, () -> service.getItemRequestById(user.getId(), 9999L));
    }

    @Test
    void testGetItemRequestByIdForDifferentUser() {
        User requestor = makeUser("requestor5@email.com", "Requestor5");
        em.persist(requestor);

        User otherUser = makeUser("other@email.com", "OtherUser");
        em.persist(otherUser);

        ItemRequest request = makeItemRequest("Need tool", requestor);
        em.persist(request);

        em.flush();

        ItemRequestResponseDto result = service.getItemRequestById(otherUser.getId(), request.getId());

        assertThat(result.getId(), equalTo(request.getId()));
        assertThat(result.getDescription(), equalTo(request.getDescription()));
    }

    private User makeUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    private ItemRequest makeItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
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
}
