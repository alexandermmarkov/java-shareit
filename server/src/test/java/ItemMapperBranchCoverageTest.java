import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithDateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
public class ItemMapperBranchCoverageTest {

    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toItemWithDateDtoShouldHandleAllNullParameters() {
        Item item = new Item();
        item.setId(1L);
        item.setRequest(null);

        ItemWithDateDto result = itemMapper.toItemWithDateDto(item, null, null, null);

        assertThat(result.getRequestId()).isNull();
        assertThat(result.getComments()).isNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void toItemWithDateDtoShouldHandleEmptyComments() {
        Item item = new Item();
        item.setId(1L);

        ItemWithDateDto result = itemMapper.toItemWithDateDto(item, List.of(), null, null);

        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void toItemWithDateDtoShouldHandleNonNullBookingDates() {
        Item item = new Item();
        item.setId(1L);

        LocalDateTime lastBooking = LocalDateTime.now().minusDays(1);
        LocalDateTime nextBooking = LocalDateTime.now().plusDays(1);

        ItemWithDateDto result = itemMapper.toItemWithDateDto(item, null, lastBooking, nextBooking);

        assertThat(result.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result.getNextBooking()).isEqualTo(nextBooking);
    }

    @Test
    void toItemDtoShouldHandleRequestWithId() {
        Item item = new Item();
        item.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(100L);
        item.setRequest(request);

        ItemDto result = itemMapper.toItemDto(item);

        assertThat(result.getRequestId()).isEqualTo(100L);
    }

    @Test
    void toItemShouldHandleAllNullParameters() {
        ItemDto itemDto = new ItemDto(null, null, null, null, null, null);
        itemDto.setId(1L);
        itemDto.setName("Test");
        itemDto.setDescription("Desc");
        itemDto.setAvailable(true);

        Item result = itemMapper.toItem(itemDto, null, null);

        assertThat(result.getOwner()).isNull();
        assertThat(result.getRequest()).isNull();
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getDescription()).isEqualTo("Desc");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void toItemDtoShouldHandleAllBranchCases() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setRequest(null);

        ItemDto result1 = itemMapper.toItemDto(item1);
        assertThat(result1.getRequestId()).isNull();

        Item item2 = new Item();
        item2.setId(2L);
        ItemRequest requestWithNullId = new ItemRequest();
        item2.setRequest(requestWithNullId);

        ItemDto result2 = itemMapper.toItemDto(item2);
        assertThat(result2.getRequestId()).isNull();

        Item item3 = new Item();
        item3.setId(3L);
        ItemRequest validRequest = new ItemRequest();
        validRequest.setId(100L);
        item3.setRequest(validRequest);

        ItemDto result3 = itemMapper.toItemDto(item3);
        assertThat(result3.getRequestId()).isEqualTo(100L);
    }

    @Test
    void toItemWithDateDtoShouldHandleAllParameterCombinations() {
        Item item = new Item();
        item.setId(1L);

        List<CommentDto> comments = List.of(
                new CommentDto(1L, "Great!", "User1", LocalDateTime.now()),
                new CommentDto(2L, "Nice!", "User2", LocalDateTime.now())
        );

        LocalDateTime lastBooking = LocalDateTime.now().minusDays(1);
        LocalDateTime nextBooking = LocalDateTime.now().plusDays(1);

        ItemWithDateDto result1 = itemMapper.toItemWithDateDto(item, null, null, null);
        assertThat(result1.getComments()).isNull();
        assertThat(result1.getLastBooking()).isNull();
        assertThat(result1.getNextBooking()).isNull();

        ItemWithDateDto result2 = itemMapper.toItemWithDateDto(item, comments, null, null);
        assertThat(result2.getComments()).hasSize(2);
        assertThat(result2.getLastBooking()).isNull();
        assertThat(result2.getNextBooking()).isNull();

        ItemWithDateDto result3 = itemMapper.toItemWithDateDto(item, null, lastBooking, null);
        assertThat(result3.getComments()).isNull();
        assertThat(result3.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result3.getNextBooking()).isNull();

        ItemWithDateDto result4 = itemMapper.toItemWithDateDto(item, null, null, nextBooking);
        assertThat(result4.getComments()).isNull();
        assertThat(result4.getLastBooking()).isNull();
        assertThat(result4.getNextBooking()).isEqualTo(nextBooking);

        ItemWithDateDto result5 = itemMapper.toItemWithDateDto(item, comments, lastBooking, nextBooking);
        assertThat(result5.getComments()).hasSize(2);
        assertThat(result5.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result5.getNextBooking()).isEqualTo(nextBooking);
    }

    @Test
    void toItemShouldHandleNullAndEmptyValues() {
        ItemDto itemDto1 = new ItemDto(1L, "Item1", "Desc1", null, true, null);
        Item result1 = itemMapper.toItem(itemDto1, null, null);
        assertThat(result1.getOwner()).isNull();
        assertThat(result1.getRequest()).isNull();

        User owner = new User();
        owner.setId(1L);
        ItemDto itemDto2 = new ItemDto(2L, "Item2", "Desc2", null, true, null);
        Item result2 = itemMapper.toItem(itemDto2, owner, null);
        assertThat(result2.getOwner()).isEqualTo(owner);
        assertThat(result2.getRequest()).isNull();

        ItemRequest request = new ItemRequest();
        request.setId(100L);
        ItemDto itemDto3 = new ItemDto(3L, "Item3", "Desc3", null, true, 100L);
        Item result3 = itemMapper.toItem(itemDto3, null, request);
        assertThat(result3.getOwner()).isNull();
        assertThat(result3.getRequest()).isEqualTo(request);

        ItemDto itemDto4 = new ItemDto(4L, "Item4", "Desc4", null, false, 100L);
        Item result4 = itemMapper.toItem(itemDto4, owner, request);
        assertThat(result4.getOwner()).isEqualTo(owner);
        assertThat(result4.getRequest()).isEqualTo(request);
        assertThat(result4.getAvailable()).isFalse();
    }

    @Test
    void toItemShouldMapAllFieldsCorrectly() {
        User owner = new User();
        owner.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(100L);

        ItemDto itemDto = new ItemDto(5L, "Test Item", "Test Description",
                new UserDto(1L, "Owner", "owner@test.com"),
                true, 100L);

        Item result = itemMapper.toItem(itemDto, owner, request);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getRequest()).isEqualTo(request);
    }

    @Test
    void toItemDtoShouldHandleRequestWithNullId() {
        Item item = new Item();
        item.setId(1L);

        ItemRequest request = new ItemRequest();
        item.setRequest(request);

        ItemDto result = itemMapper.toItemDto(item);
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void toItemWithDateDtoShouldHandleMixedNullParameters() {
        Item item = new Item();
        item.setId(1L);

        List<CommentDto> comments = List.of(new CommentDto(null, null, null, null));
        LocalDateTime lastBooking = LocalDateTime.now().minusDays(1);

        ItemWithDateDto result1 = itemMapper.toItemWithDateDto(item, comments, lastBooking, null);
        assertThat(result1.getComments()).isEqualTo(comments);
        assertThat(result1.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result1.getNextBooking()).isNull();

        ItemWithDateDto result2 = itemMapper.toItemWithDateDto(item, null, null, lastBooking);
        assertThat(result2.getComments()).isNull();
        assertThat(result2.getLastBooking()).isNull();
        assertThat(result2.getNextBooking()).isEqualTo(lastBooking);
    }

    @Test
    void toItem_ShouldHandlePartialDto() {
        ItemDto itemDto = new ItemDto(null, null, null, null, null, null);
        itemDto.setId(1L);

        User owner = new User();

        Item result = itemMapper.toItem(itemDto, owner, null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getAvailable()).isNull();
        assertThat(result.getOwner()).isEqualTo(owner);
    }
}
