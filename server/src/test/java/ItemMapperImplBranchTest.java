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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ShareItServer.class)
public class ItemMapperImplBranchTest {
    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toItemWithDateDtoShouldCoverAllBranches() {
        Item item = new Item();
        item.setId(1L);

        List<CommentDto> comments = List.of(new CommentDto(1L, "Text", "Author", LocalDateTime.now()));
        LocalDateTime lastBooking = LocalDateTime.now().minusDays(1);
        LocalDateTime nextBooking = LocalDateTime.now().plusDays(1);

        ItemWithDateDto result1 = itemMapper.toItemWithDateDto(item, null, null, null);
        assertThat(result1.getComments()).isNull();
        assertThat(result1.getLastBooking()).isNull();
        assertThat(result1.getNextBooking()).isNull();

        ItemWithDateDto result2 = itemMapper.toItemWithDateDto(item, comments, null, null);
        assertThat(result2.getComments()).hasSize(1);
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
        assertThat(result5.getComments()).hasSize(1);
        assertThat(result5.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result5.getNextBooking()).isEqualTo(nextBooking);
    }

    @Test
    void toItemShouldCoverAllBranches() {
        ItemDto itemDto1 = new ItemDto(1L, "Item1", "Desc1", null, true, null);
        Item result1 = itemMapper.toItem(itemDto1, null, null);
        assertThat(result1.getOwner()).isNull();
        assertThat(result1.getRequest()).isNull();

        ItemRequest request = new ItemRequest();
        request.setId(100L);
        ItemDto itemDto2 = new ItemDto(2L, "Item2", "Desc2", null, false, 100L);
        Item result2 = itemMapper.toItem(itemDto2, null, request);
        assertThat(result2.getOwner()).isNull();
        assertThat(result2.getRequest()).isEqualTo(request);
        assertThat(result2.getAvailable()).isFalse();

        User owner = new User();
        owner.setId(1L);
        ItemDto itemDto3 = new ItemDto(3L, "Item3", "Desc3", null, true, null);
        Item result3 = itemMapper.toItem(itemDto3, owner, null);
        assertThat(result3.getOwner()).isEqualTo(owner);
        assertThat(result3.getRequest()).isNull();

        ItemDto itemDto4 = new ItemDto(4L, "Item4", "Desc4", null, true, 100L);
        Item result4 = itemMapper.toItem(itemDto4, owner, request);
        assertThat(result4.getOwner()).isEqualTo(owner);
        assertThat(result4.getRequest()).isEqualTo(request);
    }

    @Test
    void toItemDtoShouldCoverRequestBranches() {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setRequest(null);
        ItemDto result1 = itemMapper.toItemDto(item1);
        assertThat(result1.getRequestId()).isNull();

        Item item2 = new Item();
        item2.setId(2L);
        ItemRequest requestWithoutId = new ItemRequest();

        item2.setRequest(requestWithoutId);
        ItemDto result2 = itemMapper.toItemDto(item2);
        assertThat(result2.getRequestId()).isNull();

        Item item3 = new Item();
        item3.setId(3L);
        ItemRequest requestWithId = new ItemRequest();
        requestWithId.setId(100L);
        item3.setRequest(requestWithId);
        ItemDto result3 = itemMapper.toItemDto(item3);
        assertThat(result3.getRequestId()).isEqualTo(100L);
    }
}
